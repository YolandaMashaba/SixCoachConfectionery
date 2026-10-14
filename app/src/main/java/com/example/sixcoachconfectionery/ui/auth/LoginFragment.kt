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
import com.example.sixcoachconfectionery.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository

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
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())

        setupFields()

        binding.btnDoLogin.setOnClickListener {
            performLogin()
        }

        binding.tvNavRegister.setOnClickListener {
            (activity as? MainActivity)?.navigateToRegister()
        }
    }

    private fun setupFields() {
        binding.inputLoginUsername.setLabel("Username *")
        binding.inputLoginUsername.setHint("Enter your username or email")

        binding.inputLoginPassword.setLabel("Password *")
        binding.inputLoginPassword.setHint("Enter your password")
        binding.inputLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    }

    private fun performLogin() {
        binding.tvLoginError.visibility = View.GONE

        val username = binding.inputLoginUsername.getText()
        val password = binding.inputLoginPassword.getText()

        var hasError = false
        if (username.isEmpty()) {
            binding.inputLoginUsername.setError("Username is required")
            hasError = true
        }
        if (password.isEmpty()) {
            binding.inputLoginPassword.setError("Password is required")
            hasError = true
        }

        if (hasError) return

        val result = repository.loginUser(username, password)
        if (result.first) {
            Toast.makeText(requireContext(), "Welcome back, $username!", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.navigateToSales()
        } else {
            binding.tvLoginError.text = result.second
            binding.tvLoginError.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
