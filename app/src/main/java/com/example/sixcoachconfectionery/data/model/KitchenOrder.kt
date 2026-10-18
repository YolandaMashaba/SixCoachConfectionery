package com.example.sixcoachconfectionery.data.model

data class KitchenOrder(
    val kitchenOrderId: String = "",
    val orderId: String = "",
    val status: String = "Pending", // Pending, Preparing, Completed
    val sentToKitchenAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val notes: String = ""
)
