package com.example.sixcoachconfectionery.ui.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sixcoachconfectionery.MainActivity
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import com.example.sixcoachconfectionery.databinding.FragmentLoginBinding
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
                authRepository.seedAdminAccount("admin@email.com", "admin@123")
            } catch (_: Exception) {}
        }

        binding.btnDoLogin.setOnClickListener {
            performLogin()
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
