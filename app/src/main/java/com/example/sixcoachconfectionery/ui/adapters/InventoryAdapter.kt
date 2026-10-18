package com.example.sixcoachconfectionery.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sixcoachconfectionery.data.models.Item
import com.example.sixcoachconfectionery.databinding.ItemInventoryRowBinding

class InventoryAdapter(
    private var items: List<Item>,
    private val onEditItem: (Item) -> Unit,
    private val onDeleteItem: (Item) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    class InventoryViewHolder(val binding: ItemInventoryRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvInventoryName.text = item.name
            tvInventoryCategory.text = item.category
            tvInventoryPrice.text = "Price: R %.2f".format(item.price)
            tvInventoryStock.text = "Stock: ${item.stock}"

            btnEditItem.setOnClickListener { onEditItem(item) }
            btnDeleteItem.setOnClickListener { onDeleteItem(item) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}
