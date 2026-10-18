package com.example.sixcoachconfectionery.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.databinding.FragmentAdminBinding
import com.google.android.material.tabs.TabLayout

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())

        val currentUser = repository.getCurrentUser()
        if (currentUser == null || !currentUser.isAdmin) {
            Toast.makeText(requireContext(), "Admin privileges required. Please sign in as admin.", Toast.LENGTH_LONG).show()
        }

        setupTabs()
        switchSubFragment(0)
    }

    private fun setupTabs() {
        with(binding.tabLayoutAdmin) {
            removeAllTabs()
            addTab(newTab().setText("Overview"))
            addTab(newTab().setText("Inventory"))
            addTab(newTab().setText("Analytics"))
            addTab(newTab().setText("Users"))

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.position?.let { switchSubFragment(it) }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    fun selectTab(position: Int) {
        binding.tabLayoutAdmin.getTabAt(position)?.select()
    }

    private fun switchSubFragment(position: Int) {
        val targetFragment: Fragment = when (position) {
            0 -> DashboardSubFragment()
            1 -> InventorySubFragment()
            2 -> AnalyticsSubFragment()
            3 -> UserManagementSubFragment()
            else -> DashboardSubFragment()
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.adminSubFragmentContainer, targetFragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
