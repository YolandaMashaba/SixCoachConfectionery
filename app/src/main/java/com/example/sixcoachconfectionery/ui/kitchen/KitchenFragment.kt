package com.example.sixcoachconfectionery.ui.kitchen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sixcoachconfectionery.data.models.Order as LocalOrder
import com.example.sixcoachconfectionery.data.repository.OrderRepository
import com.example.sixcoachconfectionery.databinding.FragmentKitchenBinding
import com.example.sixcoachconfectionery.ui.adapters.KitchenOrderAdapter
import com.example.sixcoachconfectionery.ui.dialogs.KitchenOrderDetailsDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KitchenFragment : Fragment() {

    private var _binding: FragmentKitchenBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()
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

        setupAdapter()

        binding.btnRefreshKitchen.setOnClickListener {
            refreshOrders()
            Toast.makeText(requireContext(), "Kitchen display refreshed", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearMockKitchen.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val kOrdersResult = orderRepository.getKitchenOrders()
                if (kOrdersResult.isSuccess) {
                    val kOrders = kOrdersResult.getOrNull() ?: emptyList()
                    kOrders.forEach { ko ->
                        orderRepository.updateKitchenOrderStatus(ko.kitchenOrderId, "Completed")
                        orderRepository.updateOrderStatus(ko.orderId, "Completed")
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "All mockup/pending orders cleared!", Toast.LENGTH_SHORT).show()
                    refreshOrders()
                }
            }
        }

        binding.swipeRefreshKitchen.setOnRefreshListener {
            refreshOrders()
            binding.swipeRefreshKitchen.isRefreshing = false
        }

        refreshOrders()
        handler.post(refreshTimerRunnable)
    }

    private fun setupAdapter() {
        orderAdapter = KitchenOrderAdapter(
            emptyList(),
            onCompleteOrder = { order ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val kOrdersResult = orderRepository.getKitchenOrders()
                    if (kOrdersResult.isSuccess) {
                        val kOrders = kOrdersResult.getOrNull() ?: emptyList()
                        val target = kOrders.find { it.kitchenOrderId.hashCode() == order.id || it.orderId.hashCode() == order.id }
                        if (target != null) {
                            orderRepository.updateKitchenOrderStatus(target.kitchenOrderId, "Completed")
                            orderRepository.updateOrderStatus(target.orderId, "Completed")
                        }
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Order completed & sent to Dashboard!", Toast.LENGTH_SHORT).show()
                        refreshOrders()
                    }
                }
            },
            onOrderClick = { order ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val kOrdersResult = orderRepository.getKitchenOrders()
                    val ordersResult = orderRepository.getOrders()
                    if (kOrdersResult.isSuccess && ordersResult.isSuccess) {
                        val kOrders = kOrdersResult.getOrNull() ?: emptyList()
                        val allOrders = ordersResult.getOrNull() ?: emptyList()
                        val targetKo = kOrders.find { ko -> ko.kitchenOrderId.hashCode() == order.id || ko.orderId.hashCode() == order.id }
                        val targetOrder = allOrders.find { o -> o.orderId == targetKo?.orderId }

                        if (targetKo != null && targetOrder != null) {
                            withContext(Dispatchers.Main) {
                                val detailsDialog = KitchenOrderDetailsDialogFragment(targetKo, targetOrder) {
                                    refreshOrders()
                                }
                                detailsDialog.show(parentFragmentManager, "KitchenOrderDetailsDialog")
                            }
                        }
                    }
                }
            }
        )
        binding.rvPendingOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingOrders.adapter = orderAdapter
    }

    private fun refreshOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            val kOrdersResult = orderRepository.getKitchenOrders()
            val ordersResult = orderRepository.getOrders()

            withContext(Dispatchers.Main) {
                if (kOrdersResult.isSuccess && ordersResult.isSuccess) {
                    val pendingKitchen = kOrdersResult.getOrNull() ?: emptyList()
                    val allOrders = ordersResult.getOrNull() ?: emptyList()

                    val mappedOrders = pendingKitchen.mapNotNull { ko ->
                        val matchingOrder = allOrders.find { it.orderId == ko.orderId }
                        if (matchingOrder != null) {
                            LocalOrder(
                                id = ko.kitchenOrderId.hashCode(),
                                customer = "Customer",
                                timestamp = ko.sentToKitchenAt,
                                items = emptyList(),
                                total = matchingOrder.totalAmount,
                                paymentMethod = "Cash",
                                amountTendered = matchingOrder.totalAmount,
                                change = 0.0,
                                status = ko.status
                            )
                        } else null
                    }

                    orderAdapter.updateData(mappedOrders)

                    if (mappedOrders.isEmpty()) {
                        binding.layoutEmptyKitchen.visibility = View.VISIBLE
                        binding.swipeRefreshKitchen.visibility = View.GONE
                    } else {
                        binding.layoutEmptyKitchen.visibility = View.GONE
                        binding.swipeRefreshKitchen.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(refreshTimerRunnable)
        _binding = null
    }
}
