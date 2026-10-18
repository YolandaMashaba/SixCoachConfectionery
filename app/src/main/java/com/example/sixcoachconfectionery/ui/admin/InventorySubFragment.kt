package com.example.sixcoachconfectionery.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.databinding.FragmentAdminInventoryBinding
import com.example.sixcoachconfectionery.ui.adapters.InventoryAdapter
import com.example.sixcoachconfectionery.ui.dialogs.ItemEditorDialogFragment

class InventorySubFragment : Fragment() {

    private var _binding: FragmentAdminInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: SixCoachConfectioneryRepository
    private lateinit var inventoryAdapter: InventoryAdapter

    private val repoChangeListener = {
        if (isAdded) {
            refreshInventory()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = SixCoachConfectioneryRepository.getInstance(requireContext())
        repository.addChangeListener(repoChangeListener)

        setupAdapter()

        binding.btnAddItem.setOnClickListener {
            val editorDialog = ItemEditorDialogFragment(null) {
                refreshInventory()
                Toast.makeText(requireContext(), "Item added successfully!", Toast.LENGTH_SHORT).show()
            }
            editorDialog.show(parentFragmentManager, "ItemEditorDialog")
        }

        refreshInventory()
    }

    private fun setupAdapter() {
        inventoryAdapter = InventoryAdapter(emptyList(),
            onEditItem = { item ->
                val editorDialog = ItemEditorDialogFragment(item) {
                    refreshInventory()
                    Toast.makeText(requireContext(), "Item updated!", Toast.LENGTH_SHORT).show()
                }
                editorDialog.show(parentFragmentManager, "ItemEditorDialog")
            },
            onDeleteItem = { item ->
                repository.deleteItem(item.id)
                Toast.makeText(requireContext(), "'${item.name}' deactivated.", Toast.LENGTH_SHORT).show()
                refreshInventory()
            }
        )

        binding.rvInventory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInventory.adapter = inventoryAdapter
    }

    private fun refreshInventory() {
        val allItems = repository.getAllItems()
        inventoryAdapter.updateData(allItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        repository.removeChangeListener(repoChangeListener)
        _binding = null
    }
}
