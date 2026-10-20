package com.example.sixcoachconfectionery.data.model

data class InventoryTransaction(
    val transactionId: String = "",
    val productId: String = "",
    val staffId: String = "",
    val transactionType: String = "Sale", // Add, Sale, Adjust, Return
    val quantity: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
