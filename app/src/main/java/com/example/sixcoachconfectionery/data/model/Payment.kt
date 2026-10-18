package com.example.sixcoachconfectionery.data.model

data class Payment(
    val paymentId: String = "",
    val orderId: String = "",
    val paymentMethod: String = "Cash", // Cash, Card, EFT, Other
    val amount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val reference: String = "",
    val status: String = "Success" // Success, Pending, Failed
)
