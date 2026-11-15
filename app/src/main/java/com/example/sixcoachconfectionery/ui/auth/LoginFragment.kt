package com.example.sixcoachconfectionery.ui.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sixcoachconfectionery.MainActivity
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import com.example.sixcoachconfectionery.databinding.FragmentLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFields()

        // Seed default admin account asynchronously on background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                authRepository.seedAdminAccount("admin@sixcoachconfectionery.co.za", "admin@123")
            } catch (_: Exception) {}
        }

        binding.btnDoLogin.setOnClickListener {
            performLogin()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // Registration link removed as registration page is no longer required
        binding.root.findViewById<View>(com.example.sixcoachconfectionery.R.id.tvNavRegister)?.visibility = View.GONE
    }

    private fun setupFields() {
        binding.inputLoginUsername.setLabel("Company Email *")
        binding.inputLoginUsername.setHint("e.g. admin@sixcoachconfectionery.co.za")

        binding.inputLoginPassword.setLabel("Password *")
        binding.inputLoginPassword.setHint("Enter your password")
        binding.inputLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    }

    private fun performLogin() {
        binding.tvLoginError.visibility = View.GONE

        val email = binding.inputLoginUsername.getText().trim()
        val password = binding.inputLoginPassword.getText()

        var hasError = false
        if (email.isEmpty()) {
            binding.inputLoginUsername.setError("Email is required")
            hasError = true
        } else if (!email.endsWith("@sixcoachconfectionery.co.za", ignoreCase = true) && !email.equals("admin@sixcoachconfectionery.co.za", ignoreCase = true) && !email.equals("ST10385107@rcconnect.edu.za", ignoreCase = true)) {
            binding.inputLoginUsername.setError("Only emails ending with @sixcoachconfectionery.co.za are allowed")
            hasError = true
        }

        if (password.isEmpty()) {
            binding.inputLoginPassword.setError("Password is required")
            hasError = true
        }

        if (hasError) return

        binding.btnDoLogin.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.loginUser(email, password)
            withContext(Dispatchers.Main) {
                binding.btnDoLogin.isEnabled = true
                if (result.isSuccess) {
                    val userProfile = result.getOrNull()
                    Toast.makeText(requireContext(), "Welcome back, ${userProfile?.username ?: email}!", Toast.LENGTH_SHORT).show()
                    if (userProfile != null) {
                        (activity as? MainActivity)?.onUserLoggedIn(userProfile)
                    } else {
                        (activity as? MainActivity)?.navigateToSales()
                    }
                } else {
                    val rawError = result.exceptionOrNull() ?: Exception("Login failed")
                    val errorMsg = translateAuthError(rawError)
                    binding.tvLoginError.text = errorMsg
                    binding.tvLoginError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun signInWithGoogle() {
        binding.btnGoogleSignIn.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val serverClientId = "337377997144-bjd8tsh7vksnuk7sd04hinojrekrn0eu.apps.googleusercontent.com"

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(requireContext())
                val result = credentialManager.getCredential(requireContext(), request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleIdTokenCredential.idToken

                    val authResult = authRepository.signInWithGoogle(googleIdToken)
                    withContext(Dispatchers.Main) {
                        binding.btnGoogleSignIn.isEnabled = true
                        if (authResult.isSuccess) {
                            val userProfile = authResult.getOrNull()
                            Toast.makeText(requireContext(), "Signed in with Google as ${userProfile?.username ?: "User"}!", Toast.LENGTH_SHORT).show()
                            if (userProfile != null) {
                                (activity as? MainActivity)?.onUserLoggedIn(userProfile)
                            }
                        } else {
                            val rawError = authResult.exceptionOrNull() ?: Exception("Google sign-in failed")
                            Toast.makeText(requireContext(), translateAuthError(rawError), Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.btnGoogleSignIn.isEnabled = true
                        Toast.makeText(requireContext(), "Unexpected credential type received", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                withContext(Dispatchers.Main) {
                    binding.btnGoogleSignIn.isEnabled = true
                    Toast.makeText(requireContext(), "Google Sign-In requires a valid Web Client ID configured in Firebase Console / google-services.xml, or Google accounts added in device settings.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnGoogleSignIn.isEnabled = true
                    Toast.makeText(requireContext(), "Google sign-in error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun translateAuthError(e: Throwable): String {
        val msg = e.localizedMessage?.lowercase() ?: ""
        if (msg.contains("restricted") || msg.contains("@sixcoachconfectionery.co.za")) {
            return e.localizedMessage ?: "Only email addresses ending with @sixcoachconfectionery.co.za are allowed."
        }
        return when (e) {
            is FirebaseAuthInvalidUserException -> "Account does not exist"
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password. Please check your credentials and try again."
            is FirebaseAuthUserCollisionException -> "An account with this email address already exists."
            else -> {
                when {
                    msg.contains("no user") || msg.contains("user-not-found") || msg.contains("not found") || msg.contains("no record") ->
                        "Account does not exist"
                    msg.contains("password") || msg.contains("credentials") || msg.contains("wrong-password") ->
                        "Incorrect email or password. Please check your credentials and try again."
                    msg.contains("network") || msg.contains("connect") || msg.contains("unknownhost") ->
                        "Network connection error. Please check your internet connection and try again."
                    else ->
                        e.localizedMessage ?: "Authentication failed. Please check your details and try again."
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
