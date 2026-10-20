package com.example.sixcoachconfectionery.data.model

data class SalesAnalytics(
    val analyticsId: String = "",
    val date: String = "", // e.g. "YYYY-MM-DD"
    val totalSales: Double = 0.0,
    val totalOrders: Int = 0,
    val totalItemsSold: Int = 0,
    val averageOrderValue: Double = 0.0
)
