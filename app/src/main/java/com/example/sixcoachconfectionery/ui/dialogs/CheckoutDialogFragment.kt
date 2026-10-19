package com.example.sixcoachconfectionery.ui.dialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.sixcoachconfectionery.data.models.CartItem
import com.example.sixcoachconfectionery.data.model.KitchenOrder
import com.example.sixcoachconfectionery.data.model.KitchenOrderItem
import com.example.sixcoachconfectionery.data.model.Order as FirestoreOrder
import com.example.sixcoachconfectionery.data.model.OrderItem as FirestoreOrderItem
import com.example.sixcoachconfectionery.data.model.Payment as FirestorePayment
import com.example.sixcoachconfectionery.data.repository.OrderRepository
import com.example.sixcoachconfectionery.databinding.DialogCheckoutBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckoutDialogFragment(
    private val cartItems: List<CartItem>,
    private val onPaymentComplete: () -> Unit
) : DialogFragment() {

    private var _binding: DialogCheckoutBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()
    private var totalAmount: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        totalAmount = cartItems.sumOf { it.totalPrice }
        binding.tvDialogOrderTotal.text = "R %.2f".format(totalAmount)

        val currentUser = FirebaseAuth.getInstance().currentUser
        binding.tvCashierInfo.text = "Cashier: ${currentUser?.email ?: "Staff"}"

        setupListeners()
    }

    private fun setupListeners() {
        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbCard.id) {
                binding.layoutCashSection.visibility = View.GONE
            } else {
                binding.layoutCashSection.visibility = View.VISIBLE
            }
            updateChangeCalculation()
        }

        binding.etAmountTendered.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateChangeCalculation()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Quick Cash Buttons
        binding.btnQuickExact.setOnClickListener {
            binding.etAmountTendered.setText("%.2f".format(totalAmount))
        }
        binding.btnQuick10.setOnClickListener { addQuickCash(10.0) }
        binding.btnQuick20.setOnClickListener { addQuickCash(20.0) }
        binding.btnQuick50.setOnClickListener { addQuickCash(50.0) }
        binding.btnQuick100.setOnClickListener { addQuickCash(100.0) }

        binding.btnCancelCheckout.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmPayment.setOnClickListener {
            processPayment()
        }
    }

    private fun addQuickCash(amount: Double) {
        val currentStr = binding.etAmountTendered.text.toString().trim()
        val currentVal = currentStr.toDoubleOrNull() ?: 0.0
        val newVal = currentVal + amount
        binding.etAmountTendered.setText("%.2f".format(newVal))
    }

    private fun updateChangeCalculation() {
        binding.tvCheckoutError.visibility = View.GONE
        if (binding.rbCard.isChecked) {
            binding.tvChangeDue.text = "R 0.00"
            return
        }

        val tenderedStr = binding.etAmountTendered.text.toString().trim()
        val tendered = tenderedStr.toDoubleOrNull() ?: 0.0
        val change = tendered - totalAmount

        if (change >= 0) {
            binding.tvChangeDue.text = "R %.2f".format(change)
        } else {
            binding.tvChangeDue.text = "R 0.00"
        }
    }

    private fun processPayment() {
        val paymentMethod = if (binding.rbCard.isChecked) "Card" else "Cash"
        val tenderedStr = binding.etAmountTendered.text.toString().trim()
        val tendered = if (paymentMethod == "Cash") tenderedStr.toDoubleOrNull() ?: 0.0 else totalAmount

        if (paymentMethod == "Cash" && tendered < totalAmount) {
            binding.tvCheckoutError.text = "Amount tendered (R %.2f) is less than order total (R %.2f).".format(tendered, totalAmount)
            binding.tvCheckoutError.visibility = View.VISIBLE
            return
        }

        binding.btnConfirmPayment.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "staff_default"
            val orderId = "ord_${System.currentTimeMillis()}"

            val firestoreOrder = FirestoreOrder(
                orderId = orderId,
                orderDate = System.currentTimeMillis(),
                totalAmount = totalAmount,
                status = "Pending",
                staffId = uid,
                customerId = uid
            )

            val firestoreOrderItems = cartItems.map { ci ->
                FirestoreOrderItem(
                    orderItemId = "",
                    orderId = orderId,
                    productId = ci.item.id,
                    quantity = ci.quantity,
                    unitPrice = ci.item.price,
                    subtotal = ci.totalPrice
                )
            }

            val firestorePayment = FirestorePayment(
                paymentId = "",
                orderId = orderId,
                paymentMethod = paymentMethod,
                amount = totalAmount,
                paymentDate = System.currentTimeMillis(),
                reference = "TXN_${System.currentTimeMillis()}",
                status = "Success"
            )

            val kitchenOrder = KitchenOrder(
                kitchenOrderId = "",
                orderId = orderId,
                status = "Pending",
                sentToKitchenAt = System.currentTimeMillis()
            )

            val kitchenItems = cartItems.map { ci ->
                KitchenOrderItem(
                    kitchenOrderItemId = "",
                    kitchenOrderId = "",
                    productId = ci.item.id,
                    quantity = ci.quantity,
                    status = "Pending"
                )
            }

            val result = orderRepository.placeOrderWithTransaction(
                firestoreOrder,
                firestoreOrderItems,
                firestorePayment,
                kitchenOrder,
                kitchenItems
            )

            withContext(Dispatchers.Main) {
                binding.btnConfirmPayment.isEnabled = true
                if (result.isSuccess) {
                    onPaymentComplete()
                    dismiss()
                } else {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to process payment & update stock."
                    binding.tvCheckoutError.text = err
                    binding.tvCheckoutError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
