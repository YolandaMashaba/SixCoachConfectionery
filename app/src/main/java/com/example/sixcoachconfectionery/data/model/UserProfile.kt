package com.example.sixcoachconfectionery.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val role: String = "Staff", // "Admin" or "Staff"
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
