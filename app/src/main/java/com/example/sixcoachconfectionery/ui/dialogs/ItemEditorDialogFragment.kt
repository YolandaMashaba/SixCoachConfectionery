package com.example.sixcoachconfectionery.ui.dialogs

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.data.models.Item
import com.example.sixcoachconfectionery.databinding.DialogItemEditorBinding

class ItemEditorDialogFragment(
    private val existingItem: Item? = null,
    private val onItemSaved: () -> Unit
) : DialogFragment() {

    private var _binding: DialogItemEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogItemEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())

        setupFields()

        if (existingItem != null) {
            binding.tvEditorTitle.text = "Edit Bakery Item"
            binding.inputItemName.setText(existingItem.name)
            binding.inputItemCategory.setText(existingItem.category)
            binding.inputItemPrice.setText("%.2f".format(existingItem.price))
            binding.inputItemStock.setText(existingItem.stock.toString())
        } else {
            binding.tvEditorTitle.text = "Add Bakery Item"
        }

        binding.btnCancelItem.setOnClickListener {
            dismiss()
        }

        binding.btnSaveItem.setOnClickListener {
            saveItem()
        }
    }

    private fun setupFields() {
        binding.inputItemName.setLabel("Item Name *")
        binding.inputItemName.setHint("e.g. gwinya, latte")

        binding.inputItemCategory.setLabel("Category *")
        binding.inputItemCategory.setHint("e.g. Bread, Coffee, Pastry")

        binding.inputItemPrice.setLabel("Price (R) *")
        binding.inputItemPrice.setHint("e.g. 5.00")
        binding.inputItemPrice.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        binding.inputItemStock.setLabel("Initial Stock *")
        binding.inputItemStock.setHint("e.g. 15")
        binding.inputItemStock.setInputType(InputType.TYPE_CLASS_NUMBER)
    }

    private fun saveItem() {
        binding.tvEditorError.visibility = View.GONE

        val name = binding.inputItemName.getText()
        val category = binding.inputItemCategory.getText()
        val priceStr = binding.inputItemPrice.getText()
        val stockStr = binding.inputItemStock.getText()

        var hasError = false

        if (name.isEmpty()) {
            binding.inputItemName.setError("Name cannot be empty")
            hasError = true
        }

        if (category.isEmpty()) {
            binding.inputItemCategory.setError("Category cannot be empty")
            hasError = true
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.inputItemPrice.setError("Enter a valid price > 0")
            hasError = true
        }

        val stock = stockStr.toIntOrNull()
        if (stock == null || stock < 0) {
            binding.inputItemStock.setError("Enter a valid stock >= 0")
            hasError = true
        }

        if (hasError) return

        val itemToSave = Item(
            id = existingItem?.id ?: Item.generatedId(),
            name = name,
            category = category,
            price = price!!,
            stock = stock!!,
            isActive = true
        )

        repository.saveItem(itemToSave)
        onItemSaved()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
