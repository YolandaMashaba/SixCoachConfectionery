package com.example.sixcoachconfectionery.ui.dialogs

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import com.example.sixcoachconfectionery.databinding.DialogStaffEditorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StaffEditorDialogFragment(
    private val onStaffSaved: () -> Unit
) : DialogFragment() {

    private var _binding: DialogStaffEditorBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStaffEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFields()

        binding.btnCancelStaff.setOnClickListener {
            dismiss()
        }

        binding.btnSaveStaff.setOnClickListener {
            saveStaffMember()
        }
    }

    private fun setupFields() {
        binding.inputStaffEmail.setLabel("Staff Email *")
        binding.inputStaffEmail.setHint("e.g. staff@whiskit.com")

        binding.inputStaffPassword.setLabel("Password (min 8 chars, Upper, Lower, Number, Special) *")
        binding.inputStaffPassword.setHint("Enter strong password")
        binding.inputStaffPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
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
            return Pair(false, "Password must contain at least one special character.")
        }
        return Pair(true, "")
    }

    private fun saveStaffMember() {
        binding.tvStaffError.visibility = View.GONE

        val email = binding.inputStaffEmail.getText().trim()
        val password = binding.inputStaffPassword.getText()
        val role = when (binding.rgStaffRole.checkedRadioButtonId) {
            R.id.rbStaffAdmin -> "Admin"
            R.id.rbStaffKitchen -> "Kitchen Staff"
            else -> "Counter Staff"
        }

        var hasError = false

        if (email.isEmpty()) {
            binding.inputStaffEmail.setError("Email is required")
            hasError = true
        }

        val passCheck = isPasswordStrong(password)
        if (!passCheck.first) {
            binding.inputStaffPassword.setError(passCheck.second)
            binding.tvStaffError.text = passCheck.second
            binding.tvStaffError.visibility = View.VISIBLE
            hasError = true
        }

        if (hasError) return

        binding.btnSaveStaff.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            val isAdmin = (role == "Admin")
            val result = if (isAdmin) {
                authRepository.seedAdminAccount(email, password)
            } else {
                authRepository.registerStaff(email, password, email.substringBefore("@"), role)
            }

            withContext(Dispatchers.Main) {
                binding.btnSaveStaff.isEnabled = true
                if (result.isSuccess) {
                    onStaffSaved()
                    dismiss()
                } else {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to save staff member"
                    binding.tvStaffError.text = err
                    binding.tvStaffError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
