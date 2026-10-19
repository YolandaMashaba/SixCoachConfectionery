package com.example.sixcoachconfectionery.data.model

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val role: String = "Staff", // "Admin" or "Staff"
    @get:PropertyName("isAdmin")
    @set:PropertyName("isAdmin")
    var isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
