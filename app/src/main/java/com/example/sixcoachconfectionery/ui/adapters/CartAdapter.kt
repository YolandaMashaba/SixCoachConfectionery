package com.example.sixcoachconfectionery.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sixcoachconfectionery.data.models.CartItem
import com.example.sixcoachconfectionery.databinding.ItemCartEntryBinding

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(val binding: ItemCartEntryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        with(holder.binding) {
            tvCartItemName.text = cartItem.item.name
            tvCartItemCalc.text = "R %.2f x %d".format(cartItem.item.price, cartItem.quantity)
            tvCartQuantity.text = cartItem.quantity.toString()
            tvCartItemTotal.text = "R %.2f".format(cartItem.totalPrice)

            btnCartMinus.setOnClickListener {
                onQuantityChanged(cartItem, cartItem.quantity - 1)
            }

            btnCartPlus.setOnClickListener {
                onQuantityChanged(cartItem, cartItem.quantity + 1)
            }

            btnCartRemove.setOnClickListener {
                onRemoveItem(cartItem)
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateData(newCartItems: List<CartItem>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }
}
