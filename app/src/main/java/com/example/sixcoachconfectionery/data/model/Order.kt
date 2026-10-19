package com.example.sixcoachconfectionery.data.model

data class Order(
    val orderId: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val status: String = "Pending", // Pending, Preparing, Completed, Cancelled
    val staffId: String = "",
    val customerId: String = ""
) {
    fun withAuthenticatedCustomer(uid: String): Order {
        return copy(
            orderDate = if (orderDate > 0L) orderDate else System.currentTimeMillis(),
            totalAmount = totalAmount.coerceAtLeast(0.0),
            customerId = uid,
            staffId = if (staffId.isBlank()) uid else staffId
        )
    }
}
