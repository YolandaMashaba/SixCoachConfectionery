package com.example.sixcoachconfectionery.ui.auth

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sixcoachconfectionery.MainActivity
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import com.example.sixcoachconfectionery.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
        private const val GOOGLE_WEB_CLIENT_ID = "337377997144-bjd8tsh7vksnuk7sd04hinojrekrn0eu.apps.googleusercontent.com"
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()
    private lateinit var oneTapClient: SignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken

            if (idToken.isNullOrBlank()) {
                Log.e(TAG, "Google Sign-In returned a credential without a Google ID token.")
                binding.btnGoogleSignIn.isEnabled = true
                Toast.makeText(requireContext(), "Google sign-in failed: ID Token was missing. Check the Web Client ID configuration.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            Log.d(TAG, "Google Sign-In credential received. Forwarding token to Firebase auth.")
            lifecycleScope.launch(Dispatchers.IO) {
                val authResult = authRepository.signInWithGoogle(idToken)
                withContext(Dispatchers.Main) {
                    binding.btnGoogleSignIn.isEnabled = true
                    if (authResult.isSuccess) {
                        val userProfile = authResult.getOrNull()
                        Toast.makeText(requireContext(), "Signed in with Google as ${userProfile?.username ?: "User"}!", Toast.LENGTH_SHORT).show()
                        if (userProfile != null) {
                            (activity as? MainActivity)?.onUserLoggedIn(userProfile)
                        }
                    } else {
                        val err = authResult.exceptionOrNull()?.localizedMessage ?: "Google sign-in failed"
                        Log.e(TAG, "Firebase Google auth failed: ${authResult.exceptionOrNull()}", authResult.exceptionOrNull())
                        Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle Google One Tap result", e)
            binding.btnGoogleSignIn.isEnabled = true
            Toast.makeText(requireContext(), "Google sign-in failed: ${e.localizedMessage ?: "Unexpected error"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun buildGoogleSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    private fun handleGoogleSignInResult() {
        binding.btnGoogleSignIn.isEnabled = false

        val request = buildGoogleSignInRequest()
        oneTapClient.beginSignIn(request)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    googleSignInLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to launch Google One Tap Sign-In", e)
                    binding.btnGoogleSignIn.isEnabled = true
                    Toast.makeText(requireContext(), "Google sign-in failed: ${e.localizedMessage ?: "Unable to open sign-in dialog"}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "No Google account available for One Tap sign-in. This can happen when the device has no Google account or Play Services is not configured correctly.", e)
                binding.btnGoogleSignIn.isEnabled = true
                Toast.makeText(requireContext(), "No Google account available on this device. Please add a Google account in Settings and try again.", Toast.LENGTH_LONG).show()
            }
    }

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

        oneTapClient = Identity.getSignInClient(requireContext())
        setupFields()

        // Seed default admin account asynchronously on background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                authRepository.seedAdminAccount("admin@email.com", "admin@123")
            } catch (_: Exception) {}
        }

        binding.btnDoLogin.setOnClickListener {
            performLogin()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            handleGoogleSignInResult()
        }

        binding.root.findViewById<View>(com.example.sixcoachconfectionery.R.id.tvNavRegister)?.setOnClickListener {
            (activity as? MainActivity)?.navigateToRegister()
        }
    }

    private fun setupFields() {
        binding.inputLoginUsername.setLabel("Email *")
        binding.inputLoginUsername.setHint("e.g. admin@email.com or staff email")

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
                    val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Login failed. Check credentials."
                    binding.tvLoginError.text = errorMsg
                    binding.tvLoginError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
