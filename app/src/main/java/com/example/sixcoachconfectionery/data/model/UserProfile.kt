package com.example.sixcoachconfectionery.data.model

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val role: String = "Staff", // "Admin", "Counter Staff", or "Kitchen Staff"
    @get:PropertyName("isAdmin")
    @set:PropertyName("isAdmin")
    var isAdmin: Boolean = false,
    @get:PropertyName("branch")
    @set:PropertyName("branch")
    var branch: String = "Roodepoort", // Roodepoort, Killarney, Flora Centre
    val createdAt: Long = System.currentTimeMillis()
)
