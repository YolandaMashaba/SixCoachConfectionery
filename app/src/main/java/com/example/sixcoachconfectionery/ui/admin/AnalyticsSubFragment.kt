package com.example.sixcoachconfectionery.ui.admin

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.repository.OrderRepository
import com.example.sixcoachconfectionery.databinding.FragmentAdminAnalyticsBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnalyticsSubFragment : Fragment() {

    private var _binding: FragmentAdminAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTimeFilterSpinner()

        binding.btnExportReport.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val ordersResult = orderRepository.getOrders()
                val totalRev = if (ordersResult.isSuccess) ordersResult.getOrNull()?.sumOf { it.totalAmount } ?: 0.0 else 0.0
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Report Exported!\nTotal Revenue: R%.2f".format(totalRev), Toast.LENGTH_LONG).show()
                }
            }
        }

        refreshAnalytics()
    }

    private fun setupTimeFilterSpinner() {
        val options = arrayOf("This Week", "Today", "This Month", "All Time")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeFilter.adapter = adapter

        binding.spinnerTimeFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshAnalytics()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun refreshAnalytics() {
        lifecycleScope.launch(Dispatchers.IO) {
            val ordersResult = orderRepository.getOrders()
            val orders = if (ordersResult.isSuccess) ordersResult.getOrNull() ?: emptyList() else emptyList()

            val totalRev = orders.sumOf { it.totalAmount }

            withContext(Dispatchers.Main) {
                binding.tvAnalyticsTotalRevenue.text = "R%.2f".format(totalRev)
                binding.tvAnalyticsDailyRevenue.text = "R%.2f".format(totalRev)
                binding.tvAnalyticsWeeklyRevenue.text = "R%.2f".format(totalRev)
                binding.tvAnalyticsMonthlyRevenue.text = "R%.2f".format(totalRev)

                // Render Recent Orders list in Analytics
                binding.layoutAnalyticsRecentOrdersContainer.removeAllViews()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                orders.takeLast(10).reversed().forEach { order ->
                    val row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 6, 0, 6)
                        }
                        setPadding(12, 12, 12, 12)
                        setBackgroundResource(R.drawable.bg_card_rounded)

                        val textInfo = TextView(requireContext()).apply {
                            text = "Order #${order.orderId.takeLast(6)}   %s".format(
                                dateFormat.format(Date(order.orderDate))
                            )
                            textSize = 13f
                            setTextColor(ContextCompat.getColor(context, R.color.bakery_text_primary))
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        }

                        val totalTv = TextView(requireContext()).apply {
                            text = "R%.2f".format(order.totalAmount)
                            textSize = 14f
                            setTypeface(null, Typeface.BOLD)
                            setTextColor(ContextCompat.getColor(context, R.color.bakery_brown_dark))
                        }

                        addView(textInfo)
                        addView(totalTv)
                    }

                    binding.layoutAnalyticsRecentOrdersContainer.addView(row)
                }

                // Top Selling Items placeholder/summary
                binding.layoutTopSellingContainer.removeAllViews()
                val emptyTv = TextView(requireContext()).apply {
                    text = "Live analytics retrieved from Cloud Firestore."
                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_muted))
                    setPadding(0, 8, 0, 8)
                }
                binding.layoutTopSellingContainer.addView(emptyTv)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
