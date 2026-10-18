package com.example.sixcoachconfectionery.ui.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.data.models.CartItem
import com.example.sixcoachconfectionery.data.models.Item
import com.example.sixcoachconfectionery.databinding.FragmentSalesBinding
import com.example.sixcoachconfectionery.ui.adapters.CartAdapter
import com.example.sixcoachconfectionery.ui.adapters.ProductAdapter
import com.example.sixcoachconfectionery.ui.dialogs.CheckoutDialogFragment

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartAdapter: CartAdapter

    private var selectedCategory: String = "All"
    private val cartItems = mutableListOf<CartItem>()

    private val repoChangeListener = {
        if (isAdded) {
            refreshProducts()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())
        repository.addChangeListener(repoChangeListener)

        setupAdapters()
        setupCategoryChips()
        refreshProducts()
        updateCartUI()

        binding.btnClearCart.setOnClickListener {
            cartItems.clear()
            updateCartUI()
        }

        binding.btnPay.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val checkoutDialog = CheckoutDialogFragment(cartItems) {
                cartItems.clear()
                updateCartUI()
                refreshProducts()
                Toast.makeText(requireContext(), "Payment Complete! Order sent to Kitchen.", Toast.LENGTH_LONG).show()
            }
            checkoutDialog.show(parentFragmentManager, "CheckoutDialog")
        }
    }

    private fun setupAdapters() {
        productAdapter = ProductAdapter(emptyList()) { item ->
            addToCart(item)
        }
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = productAdapter

        cartAdapter = CartAdapter(cartItems,
            onQuantityChanged = { cartItem, newQty ->
                if (newQty <= 0) {
                    cartItems.remove(cartItem)
                } else {
                    val storeItem = repository.getActiveItems().find { it.id == cartItem.item.id }
                    if (storeItem != null && newQty <= storeItem.stock) {
                        cartItem.quantity = newQty
                    } else {
                        Toast.makeText(requireContext(), "Cannot exceed available stock (${storeItem?.stock ?: 0})", Toast.LENGTH_SHORT).show()
                    }
                }
                updateCartUI()
            },
            onRemoveItem = { cartItem ->
                cartItems.remove(cartItem)
                updateCartUI()
            }
        )
        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun setupCategoryChips() {
        val categories = repository.getItemCategories()
        binding.layoutCategoryChips.removeAllViews()

        categories.forEach { cat ->
            val button = Button(requireContext(), null, com.google.android.material.R.attr.materialButtonStyle).apply {
                text = cat
                textSize = 13f
                isAllCaps = false
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 12, 0)
                }
                layoutParams = params

                if (cat == selectedCategory) {
                    setBackgroundResource(R.drawable.bg_pill_selected)
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    setBackgroundResource(R.drawable.bg_pill_unselected)
                    setTextColor(ContextCompat.getColor(context, R.color.bakery_brown))
                }

                setOnClickListener {
                    selectedCategory = cat
                    setupCategoryChips()
                    refreshProducts()
                }
            }
            binding.layoutCategoryChips.addView(button)
        }
    }

    private fun refreshProducts() {
        val allActive = repository.getActiveItems()
        val filtered = if (selectedCategory == "All") {
            allActive
        } else {
            allActive.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        productAdapter.updateData(filtered)
    }

    private fun addToCart(item: Item) {
        if (item.stock <= 0) {
            Toast.makeText(requireContext(), "'${item.name}' is out of stock!", Toast.LENGTH_SHORT).show()
            return
        }

        val existing = cartItems.find { it.item.id == item.id }
        if (existing != null) {
            if (existing.quantity < item.stock) {
                existing.quantity++
            } else {
                Toast.makeText(requireContext(), "Reached maximum available stock (${item.stock})", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            cartItems.add(CartItem(item, 1))
        }
        updateCartUI()
    }

    private fun updateCartUI() {
        cartAdapter.updateData(cartItems.toList())

        if (cartItems.isEmpty()) {
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.rvCartItems.visibility = View.GONE
            binding.btnClearCart.visibility = View.GONE
            binding.tvCartTotal.text = "R 0.00"
            binding.btnPay.isEnabled = false
            binding.btnPay.alpha = 0.5f
        } else {
            binding.tvEmptyCart.visibility = View.GONE
            binding.rvCartItems.visibility = View.VISIBLE
            binding.btnClearCart.visibility = View.VISIBLE
            val total = cartItems.sumOf { it.totalPrice }
            binding.tvCartTotal.text = "R %.2f".format(total)
            binding.btnPay.isEnabled = true
            binding.btnPay.alpha = 1.0f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        repository.removeChangeListener(repoChangeListener)
        _binding = null
    }
}
