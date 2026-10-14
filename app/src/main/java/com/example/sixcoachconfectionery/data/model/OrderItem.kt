package com.example.sixcoachconfectionery.data.model

data class OrderItem(
    val orderItemId: String = "",
    val orderId: String = "",
    val productId: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val subtotal: Double = 0.0
)
