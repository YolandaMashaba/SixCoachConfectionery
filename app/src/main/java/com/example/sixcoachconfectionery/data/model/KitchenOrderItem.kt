package com.example.sixcoachconfectionery.data.model

data class KitchenOrderItem(
    val kitchenOrderItemId: String = "",
    val kitchenOrderId: String = "",
    val productId: String = "",
    val quantity: Int = 1,
    val status: String = "Pending" // Pending, Completed
)
