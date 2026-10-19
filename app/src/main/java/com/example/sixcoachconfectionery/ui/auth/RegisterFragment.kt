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
import com.example.sixcoachconfectionery.databinding.FragmentRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFields()

        binding.btnDoRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvNavSignIn.setOnClickListener {
            (activity as? MainActivity)?.navigateToLogin()
        }
    }

    private fun setupFields() {
        binding.inputRegUsername.setLabel("Email / Username *")
        binding.inputRegUsername.setHint("e.g. staff@whiskit.com")

        binding.inputRegPassword.setLabel("Password (min 8 chars, Upper, Lower, Number, Special) *")
        binding.inputRegPassword.setHint("Enter strong password")
        binding.inputRegPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        binding.inputRegConfirmPassword.setLabel("Confirm password *")
        binding.inputRegConfirmPassword.setHint("Re-enter password")
        binding.inputRegConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    }

    private fun isPasswordStrong(password: String): Pair<Boolean, String> {
        if (password.length < 8) {
            return Pair(false, "Password must be at least 8 characters long.")
        }
        if (!password.any { it.isUpperCase() }) {
            return Pair(false, "Password must contain at least one uppercase letter.")
        }
        if (!password.any { it.isLowerCase() }) {
            return Pair(false, "Password must contain at least one lowercase letter.")
        }
        if (!password.any { it.isDigit() }) {
            return Pair(false, "Password must contain at least one number.")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return Pair(false, "Password must contain at least one special character (e.g. !@#\$%^&*).")
        }
        return Pair(true, "")
    }

    private fun performRegistration() {
        binding.tvRegisterError.visibility = View.GONE

        val email = binding.inputRegUsername.getText().trim()
        val password = binding.inputRegPassword.getText()
        val confirmPassword = binding.inputRegConfirmPassword.getText()
        val selectedRole = if (binding.rbKitchenStaff.isChecked) "Kitchen Staff" else "Counter Staff"

        var hasError = false

        if (email.isEmpty()) {
            binding.inputRegUsername.setError("Email is required")
            hasError = true
        }

        val passwordCheck = isPasswordStrong(password)
        if (!passwordCheck.first) {
            binding.inputRegPassword.setError(passwordCheck.second)
            binding.tvRegisterError.text = passwordCheck.second
            binding.tvRegisterError.visibility = View.VISIBLE
            hasError = true
        }

        if (password != confirmPassword) {
            binding.inputRegConfirmPassword.setError("Passwords do not match")
            hasError = true
        }

        if (hasError) return

        binding.btnDoRegister.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.registerStaff(email, password, email.substringBefore("@"), selectedRole)
            withContext(Dispatchers.Main) {
                binding.btnDoRegister.isEnabled = true
                if (result.isSuccess) {
                    val userProfile = result.getOrNull()
                    Toast.makeText(requireContext(), "Account created as $selectedRole!", Toast.LENGTH_SHORT).show()
                    if (userProfile != null) {
                        (activity as? MainActivity)?.onUserLoggedIn(userProfile)
                    } else if (selectedRole == "Kitchen Staff") {
                        (activity as? MainActivity)?.navigateToKitchen()
                    } else {
                        (activity as? MainActivity)?.navigateToSales()
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Registration failed"
                    binding.tvRegisterError.text = errorMsg
                    binding.tvRegisterError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
