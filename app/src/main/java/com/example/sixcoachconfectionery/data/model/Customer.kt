package com.example.sixcoachconfectionery.data.model

data class Customer(
    val customerId: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val dateCreated: Long = System.currentTimeMillis()
)
