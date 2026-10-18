package com.example.sixcoachconfectionery.ui.kitchen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.databinding.FragmentKitchenBinding
import com.example.sixcoachconfectionery.ui.adapters.KitchenOrderAdapter

class KitchenFragment : Fragment() {

    private var _binding: FragmentKitchenBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository
    private lateinit var orderAdapter: KitchenOrderAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val refreshTimerRunnable = object : Runnable {
        override fun run() {
            if (isAdded) {
                refreshOrders()
                handler.postDelayed(this, 10000) // refresh timer every 10 sec
            }
        }
    }

    private val repoChangeListener = {
        if (isAdded) {
            refreshOrders()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKitchenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())
        repository.addChangeListener(repoChangeListener)

        setupAdapter()

        binding.btnRefreshKitchen.setOnClickListener {
            refreshOrders()
            Toast.makeText(requireContext(), "Kitchen display refreshed", Toast.LENGTH_SHORT).show()
        }

        binding.swipeRefreshKitchen.setOnRefreshListener {
            refreshOrders()
            binding.swipeRefreshKitchen.isRefreshing = false
        }

        refreshOrders()
        handler.post(refreshTimerRunnable)
    }

    private fun setupAdapter() {
        orderAdapter = KitchenOrderAdapter(emptyList()) { order ->
            repository.completeOrder(order.id)
            Toast.makeText(requireContext(), "Order #${order.id} completed!", Toast.LENGTH_SHORT).show()
            refreshOrders()
        }
        binding.rvPendingOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingOrders.adapter = orderAdapter
    }

    private fun refreshOrders() {
        val pending = repository.getPendingOrders()
        orderAdapter.updateData(pending)

        if (pending.isEmpty()) {
            binding.layoutEmptyKitchen.visibility = View.VISIBLE
            binding.swipeRefreshKitchen.visibility = View.GONE
        } else {
            binding.layoutEmptyKitchen.visibility = View.GONE
            binding.swipeRefreshKitchen.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(refreshTimerRunnable)
        repository.removeChangeListener(repoChangeListener)
        _binding = null
    }
}
