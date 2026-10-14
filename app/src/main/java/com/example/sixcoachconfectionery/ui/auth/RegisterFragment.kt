package com.example.sixcoachconfectionery.ui.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sixcoachconfectionery.MainActivity
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository

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
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())

        setupFields()

        binding.btnDoRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvNavSignIn.setOnClickListener {
            (activity as? MainActivity)?.navigateToLogin()
        }
    }

    private fun setupFields() {
        binding.inputRegUsername.setLabel("Username *")
        binding.inputRegUsername.setHint("Choose a username or email")

        binding.inputRegPassword.setLabel("Password (min 8 characters) *")
        binding.inputRegPassword.setHint("Enter password")
        binding.inputRegPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        binding.inputRegConfirmPassword.setLabel("Confirm password *")
        binding.inputRegConfirmPassword.setHint("Re-enter password")
        binding.inputRegConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    }

    private fun performRegistration() {
        binding.tvRegisterError.visibility = View.GONE

        val username = binding.inputRegUsername.getText()
        val password = binding.inputRegPassword.getText()
        val confirmPassword = binding.inputRegConfirmPassword.getText()

        var hasError = false

        if (username.isEmpty()) {
            binding.inputRegUsername.setError("Username is required")
            hasError = true
        }

        if (password.length < 8) {
            binding.inputRegPassword.setError("Password must be at least 8 characters")
            hasError = true
        }

        if (password != confirmPassword) {
            binding.inputRegConfirmPassword.setError("Passwords do not match")
            hasError = true
        }

        if (hasError) return

        val result = repository.registerUser(username, password)
        if (result.first) {
            Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.navigateToSales()
        } else {
            binding.tvRegisterError.text = result.second
            binding.tvRegisterError.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
