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

        binding.inputRegPassword.setLabel("Password (min 6 characters) *")
        binding.inputRegPassword.setHint("Enter password")
        binding.inputRegPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        binding.inputRegConfirmPassword.setLabel("Confirm password *")
        binding.inputRegConfirmPassword.setHint("Re-enter password")
        binding.inputRegConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
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

        if (password.length < 6) {
            binding.inputRegPassword.setError("Password must be at least 6 characters")
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
