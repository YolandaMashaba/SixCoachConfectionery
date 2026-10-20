package com.example.sixcoachconfectionery.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import com.example.sixcoachconfectionery.databinding.FragmentAdminUsersBinding
import com.example.sixcoachconfectionery.ui.adapters.UserAdapter
import com.example.sixcoachconfectionery.ui.dialogs.StaffEditorDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserManagementSubFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()

        binding.btnAddStaff.setOnClickListener {
            val staffDialog = StaffEditorDialogFragment(null) {
                refreshUsers()
                Toast.makeText(requireContext(), "Staff account created successfully!", Toast.LENGTH_SHORT).show()
            }
            staffDialog.show(parentFragmentManager, "StaffEditorDialog")
        }

        refreshUsers()
    }

    private fun setupAdapter() {
        userAdapter = UserAdapter(
            emptyList(),
            { user ->
                // Open edit staff dialog pre-filled with user details
                val staffDialog = StaffEditorDialogFragment(user) {
                    refreshUsers()
                    Toast.makeText(requireContext(), "Staff role updated successfully!", Toast.LENGTH_SHORT).show()
                }
                staffDialog.show(parentFragmentManager, "StaffEditorDialog")
            },
            { user ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = authRepository.deleteUser(user.uid)
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            Toast.makeText(requireContext(), "Deleted user '${user.username.ifEmpty { user.email }}'", Toast.LENGTH_SHORT).show()
                            refreshUsers()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete user", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )

        binding.rvUsersList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsersList.adapter = userAdapter
    }

    private fun refreshUsers() {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = authRepository.getAllUsers()
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val users = result.getOrNull() ?: emptyList()
                    userAdapter.updateData(users)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
