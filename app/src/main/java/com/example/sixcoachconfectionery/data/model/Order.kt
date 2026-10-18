package com.example.sixcoachconfectionery.data.model

data class Order(
    val orderId: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val status: String = "Pending", // Pending, Preparing, Completed, Cancelled
    val staffId: String = "",
    val customerId: String = ""
)
