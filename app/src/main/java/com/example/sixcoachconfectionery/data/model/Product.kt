package com.example.sixcoachconfectionery.data.model

data class Product(
    val productId: String = "",
    val productName: String = "",
    val categoryId: String = "",
    val price: Double = 0.0,
    val costPrice: Double = 0.0,
    val quantityInStock: Int = 0,
    val availability: String = "In Stock", // In Stock / Out of Stock
    val imageURL: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)
