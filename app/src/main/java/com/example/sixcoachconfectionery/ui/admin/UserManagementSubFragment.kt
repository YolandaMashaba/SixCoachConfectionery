package com.example.sixcoachconfectionery.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.databinding.FragmentAdminUsersBinding
import com.example.sixcoachconfectionery.ui.adapters.UserAdapter

class UserManagementSubFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository
    private lateinit var userAdapter: UserAdapter

    private val repoChangeListener = {
        if (isAdded) {
            refreshUsers()
        }
    }

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
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())
        repository.addChangeListener(repoChangeListener)

        setupAdapter()
        refreshUsers()
    }

    private fun setupAdapter() {
        userAdapter = UserAdapter(emptyList()) { user ->
            val updated = repository.toggleUserAdminStatus(user.id)
            if (updated) {
                Toast.makeText(requireContext(), "Updated admin status for '${user.username}'", Toast.LENGTH_SHORT).show()
                refreshUsers()
            }
        }
        binding.rvUsersList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsersList.adapter = userAdapter
    }

    private fun refreshUsers() {
        val users = repository.getUsers()
        userAdapter.updateData(users)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        repository.removeChangeListener(repoChangeListener)
        _binding = null
    }
}
