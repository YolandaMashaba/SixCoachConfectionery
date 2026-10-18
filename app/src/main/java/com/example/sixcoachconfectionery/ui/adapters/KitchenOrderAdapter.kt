package com.example.sixcoachconfectionery.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.models.Order
import com.example.sixcoachconfectionery.databinding.ItemKitchenOrderBinding
import java.util.concurrent.TimeUnit

class KitchenOrderAdapter(
    private var orders: List<Order>,
    private val onCompleteOrder: (Order) -> Unit
) : RecyclerView.Adapter<KitchenOrderAdapter.KitchenViewHolder>() {

    class KitchenViewHolder(val binding: ItemKitchenOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KitchenViewHolder {
        val binding = ItemKitchenOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return KitchenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KitchenViewHolder, position: Int) {
        val order = orders[position]
        with(holder.binding) {
            tvKitchenOrderId.text = "Order #${order.id}"

            // Elapsed time calculation
            val elapsedMillis = System.currentTimeMillis() - order.timestamp
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
            tvKitchenElapsedTime.text = "%dm %ds".format(minutes, seconds)

            // Render order items dynamically
            layoutKitchenItemsContainer.removeAllViews()
            val context = root.context
            order.items.forEach { cartItem ->
                val tvItem = TextView(context).apply {
                    text = "%dx %s".format(cartItem.quantity, cartItem.item.name)
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_primary))
                }
                val tvPrice = TextView(context).apply {
                    text = "R %.2f".format(cartItem.totalPrice)
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_secondary))
                }

                val row = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 4, 0, 4)
                    }
                    addView(tvItem, android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    addView(tvPrice, android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT))
                }

                layoutKitchenItemsContainer.addView(row)
            }

            tvKitchenOrderTotal.text = "R %.2f".format(order.total)

            btnCompleteOrder.setOnClickListener {
                onCompleteOrder(order)
            }
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
