package com.example.sixcoachconfectionery.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.model.KitchenOrder
import com.example.sixcoachconfectionery.data.model.Order as FirestoreOrder
import com.example.sixcoachconfectionery.data.repository.OrderRepository
import com.example.sixcoachconfectionery.data.repository.ProductRepository
import com.example.sixcoachconfectionery.databinding.DialogKitchenOrderDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class KitchenOrderDetailsDialogFragment(
    private val kitchenOrder: KitchenOrder,
    private val firestoreOrder: FirestoreOrder,
    private val onActionComplete: () -> Unit
) : DialogFragment() {

    private var _binding: DialogKitchenOrderDetailsBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()
    private val productRepository = ProductRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogKitchenOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDetailsTitle.text = "Order Details (#${kitchenOrder.kitchenOrderId.takeLast(6)})"

        val elapsedMillis = System.currentTimeMillis() - kitchenOrder.sentToKitchenAt
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
        binding.tvDetailsTime.text = "Elapsed: %dm %ds".format(minutes, seconds)

        binding.tvOrderDetailsTotal.text = "R %.2f".format(firestoreOrder.totalAmount)

        loadOrderItems()

        binding.btnDetailsComplete.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                orderRepository.updateKitchenOrderStatus(kitchenOrder.kitchenOrderId, "Completed")
                orderRepository.updateOrderStatus(firestoreOrder.orderId, "Completed")
                withContext(Dispatchers.Main) {
                    onActionComplete()
                    dismiss()
                }
            }
        }

        binding.btnDetailsClear.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                orderRepository.updateKitchenOrderStatus(kitchenOrder.kitchenOrderId, "Completed")
                orderRepository.updateOrderStatus(firestoreOrder.orderId, "Completed")
                withContext(Dispatchers.Main) {
                    onActionComplete()
                    dismiss()
                }
            }
        }
    }

    private fun loadOrderItems() {
        lifecycleScope.launch(Dispatchers.IO) {
            val itemsResult = orderRepository.getOrderItems(firestoreOrder.orderId)
            if (itemsResult.isSuccess) {
                val orderItems = itemsResult.getOrNull() ?: emptyList()
                withContext(Dispatchers.Main) {
                    binding.layoutOrderDetailsItemsContainer.removeAllViews()
                    val context = requireContext()
                    orderItems.forEach { oi ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val productResult = productRepository.getProductById(oi.productId)
                            val productName = productResult.getOrNull()?.productName ?: "Item"
                            withContext(Dispatchers.Main) {
                                val tvItem = TextView(context).apply {
                                    text = "%dx %s".format(oi.quantity, productName)
                                    textSize = 14f
                                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_primary))
                                }
                                val tvPrice = TextView(context).apply {
                                    text = "R %.2f".format(oi.subtotal)
                                    textSize = 14f
                                    setTextColor(ContextCompat.getColor(context, R.color.bakery_text_secondary))
                                }

                                val row = LinearLayout(context).apply {
                                    orientation = LinearLayout.HORIZONTAL
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).apply { setMargins(0, 4, 0, 4) }
                                    addView(tvItem, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                                    addView(tvPrice, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                                }
                                binding.layoutOrderDetailsItemsContainer.addView(row)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
