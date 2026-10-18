package com.example.sixcoachconfectionery.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.models.Item
import com.example.sixcoachconfectionery.databinding.ItemProductCardBinding

class ProductAdapter(
    private var items: List<Item>,
    private var selectedItemId: String? = null,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvCategoryBadge.text = item.category.uppercase()
            tvProductName.text = item.name
            tvProductPrice.text = "R %.2f".format(item.price)
            
            if (item.stock > 0) {
                tvProductStock.text = "Stock ${item.stock}"
                tvProductStock.setTextColor(
                    ContextCompat.getColor(root.context, R.color.bakery_text_muted)
                )
                cardProduct.alpha = 1.0f
                cardProduct.isEnabled = true
            } else {
                tvProductStock.text = "Out of Stock"
                tvProductStock.setTextColor(
                    ContextCompat.getColor(root.context, R.color.bakery_red)
                )
                cardProduct.alpha = 0.6f
                cardProduct.isEnabled = false
            }

            if (item.id == selectedItemId) {
                cardProduct.strokeColor = ContextCompat.getColor(root.context, R.color.bakery_brown)
                cardProduct.strokeWidth = 4
                cardProduct.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, R.color.bakery_tan_light)
                )
            } else {
                cardProduct.strokeColor = ContextCompat.getColor(root.context, R.color.bakery_card_border)
                cardProduct.strokeWidth = 2
                cardProduct.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, R.color.white)
                )
            }

            cardProduct.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Item>, selectedId: String? = null) {
        items = newItems
        selectedItemId = selectedId
        notifyDataSetChanged()
    }
}
