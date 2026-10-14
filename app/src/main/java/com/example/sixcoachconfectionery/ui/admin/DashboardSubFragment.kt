package com.example.sixcoachconfectionery.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.databinding.FragmentAdminDashboardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardSubFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())

        setupActionButtons()
        loadDashboardData()
    }

    private fun setupActionButtons() {
        binding.btnNavInventory.setOnClickListener {
            (parentFragment as? AdminFragment)?.selectTab(1)
        }
        binding.btnNavAnalytics.setOnClickListener {
            (parentFragment as? AdminFragment)?.selectTab(2)
        }
        binding.btnNavUsers.setOnClickListener {
            (parentFragment as? AdminFragment)?.selectTab(3)
        }
    }

    private fun loadDashboardData() {
        val orders = repository.getOrders()
        val totalOrdersCount = orders.size
        val totalRevenue = orders.sumOf { it.total }
        val totalItemsSold = orders.sumOf { it.totalItemsCount }
        val avgOrderValue = if (totalOrdersCount > 0) totalRevenue / totalOrdersCount else 0.0

        binding.tvStatTotalOrders.text = totalOrdersCount.toString()
        binding.tvStatRevenue.text = "R%.2f".format(totalRevenue)
        binding.tvStatItemsSold.text = totalItemsSold.toString()
        binding.tvStatAvgOrder.text = "R%.2f".format(avgOrderValue)

        // Populate Recent Orders list
        binding.layoutRecentOrdersContainer.removeAllViews()
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        orders.takeLast(5).reversed().forEach { order ->
            val row = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }

                val textInfo = TextView(requireContext()).apply {
                    text = "Order #${order.id} — R%.2f\n%s — %s".format(
                        order.total,
                        dateFormat.format(Date(order.timestamp)),
                        order.status.lowercase()
                    )
                    textSize = 13f
                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_primary))
                }

                val statusDot = View(requireContext()).apply {
                    val size = (10 * resources.displayMetrics.density).toInt()
                    layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    setBackgroundResource(R.drawable.bg_pill_selected)
                    backgroundTintList = ContextCompat.getColorStateList(
                        context,
                        if (order.status == "Completed") R.color.bakery_green else R.color.bakery_gold
                    )
                }

                addView(textInfo, android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(statusDot)
            }

            binding.layoutRecentOrdersContainer.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
