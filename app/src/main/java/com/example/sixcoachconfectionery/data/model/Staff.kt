package com.example.sixcoachconfectionery.data.model

data class Staff(
    val staffId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "Cashier", // Cashier, Baker, Manager, Admin
    val phoneNumber: String = "",
    val dateCreated: Long = System.currentTimeMillis()
)
