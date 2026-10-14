package com.example.sixcoachconfectionery.ui.admin

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
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
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.databinding.FragmentAdminAnalyticsBinding
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnalyticsSubFragment : Fragment() {

    private var _binding: FragmentAdminAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository

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
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())

        setupTimeFilterSpinner()

        binding.btnExportReport.setOnClickListener {
            val totalRev = repository.getOrders().sumOf { it.total }
            Toast.makeText(requireContext(), "Report Exported!\nTotal Revenue: R%.2f".format(totalRev), Toast.LENGTH_LONG).show()
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
        val orders = repository.getOrders()

        val totalRev = orders.sumOf { it.total }
        binding.tvAnalyticsTotalRevenue.text = "R%.2f".format(totalRev)
        binding.tvAnalyticsDailyRevenue.text = "R%.2f".format(totalRev)
        binding.tvAnalyticsWeeklyRevenue.text = "R%.2f".format(totalRev)
        binding.tvAnalyticsMonthlyRevenue.text = "R%.2f".format(totalRev)

        // Calculate Top Selling Items
        val productSalesMap = mutableMapOf<String, Pair<Int, Double>>() // Name -> (Quantity, Revenue)
        orders.forEach { order ->
            order.items.forEach { cartItem ->
                val name = cartItem.item.name
                val existing = productSalesMap[name] ?: Pair(0, 0.0)
                productSalesMap[name] = Pair(existing.first + cartItem.quantity, existing.second + cartItem.totalPrice)
            }
        }

        binding.layoutTopSellingContainer.removeAllViews()
        val sortedProducts = productSalesMap.entries.sortedByDescending { it.value.first }

        if (sortedProducts.isEmpty()) {
            val emptyTv = TextView(requireContext()).apply {
                text = "No sales recorded yet."
                setTextColor(ContextCompat.getColor(context, R.color.bakery_text_muted))
                setPadding(0, 8, 0, 8)
            }
            binding.layoutTopSellingContainer.addView(emptyTv)
        } else {
            sortedProducts.forEachIndexed { index, entry ->
                val rank = index + 1
                val name = entry.key
                val unitsSold = entry.value.first
                val revenue = entry.value.second

                val card = MaterialCardView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 6, 0, 6) }
                    radius = 12f * resources.displayMetrics.density
                    strokeWidth = (1 * resources.displayMetrics.density).toInt()
                    setStrokeColor(ContextCompat.getColor(context, R.color.bakery_card_border))
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))

                    val container = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(16, 16, 16, 16)

                        val topRow = LinearLayout(context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL

                            val rankBadge = TextView(context).apply {
                                text = rank.toString()
                                textSize = 14f
                                setTypeface(null, Typeface.BOLD)
                                setTextColor(ContextCompat.getColor(context, R.color.white))
                                setBackgroundResource(R.drawable.bg_pill_selected)
                                setPadding(16, 8, 16, 8)
                            }

                            val nameInfo = LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                                    setMargins(16, 0, 16, 0)
                                }

                                val tvName = TextView(context).apply {
                                    text = name
                                    textSize = 15f
                                    setTypeface(null, Typeface.BOLD)
                                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_primary))
                                }

                                val tvUnits = TextView(context).apply {
                                    text = "$unitsSold units sold"
                                    textSize = 12f
                                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_secondary))
                                }

                                addView(tvName)
                                addView(tvUnits)
                            }

                            addView(rankBadge)
                            addView(nameInfo)

                            if (rank == 1) {
                                val bestBadge = TextView(context).apply {
                                    text = "#1 BEST"
                                    textSize = 10f
                                    setTypeface(null, Typeface.BOLD)
                                    setTextColor(ContextCompat.getColor(context, R.color.bakery_brown_dark))
                                    setBackgroundResource(R.drawable.bg_badge_gold)
                                    setPadding(12, 4, 12, 4)
                                }
                                addView(bestBadge)
                            }
                        }

                        val botRow = LinearLayout(context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                setMargins(0, 12, 0, 0)
                            }

                            val tvRevLabel = TextView(context).apply {
                                text = "Revenue"
                                textSize = 12f
                                setTextColor(ContextCompat.getColor(context, R.color.bakery_text_secondary))
                                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            }

                            val tvRevVal = TextView(context).apply {
                                text = "R%.2f".format(revenue)
                                textSize = 15f
                                setTypeface(null, Typeface.BOLD)
                                setTextColor(ContextCompat.getColor(context, R.color.bakery_text_primary))
                            }

                            addView(tvRevLabel)
                            addView(tvRevVal)
                        }

                        addView(topRow)
                        addView(botRow)
                    }

                    addView(container)
                }

                binding.layoutTopSellingContainer.addView(card)
            }
        }

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
                    text = "Order #%d   %s   📦 %d items".format(
                        order.id,
                        dateFormat.format(Date(order.timestamp)),
                        order.totalItemsCount
                    )
                    textSize = 13f
                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_primary))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val totalTv = TextView(requireContext()).apply {
                    text = "R%.2f".format(order.total)
                    textSize = 14f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.bakery_brown_dark))
                }

                addView(textInfo)
                addView(totalTv)
            }

            binding.layoutAnalyticsRecentOrdersContainer.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
