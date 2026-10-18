package com.example.sixcoachconfectionery.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.data.models.CartItem
import com.example.sixcoachconfectionery.databinding.DialogCheckoutBinding

class CheckoutDialogFragment(
    private val cartItems: List<CartItem>,
    private val onPaymentComplete: () -> Unit
) : DialogFragment() {

    private var _binding: DialogCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository
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
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())

        totalAmount = cartItems.sumOf { it.totalPrice }
        binding.tvDialogOrderTotal.text = "R %.2f".format(totalAmount)

        val currentUser = repository.getCurrentUser()
        binding.tvCashierInfo.text = "Cashier: ${currentUser?.username ?: "Staff"}"

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

        val result = repository.createOrder(cartItems, paymentMethod, tendered)
        if (result.first) {
            onPaymentComplete()
            dismiss()
        } else {
            binding.tvCheckoutError.text = result.second
            binding.tvCheckoutError.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
