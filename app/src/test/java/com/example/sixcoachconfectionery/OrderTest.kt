package com.example.sixcoachconfectionery

import com.example.sixcoachconfectionery.data.models.CartItem
import com.example.sixcoachconfectionery.data.models.Item
import com.example.sixcoachconfectionery.data.models.Order
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderTest {

    private val gwinya = Item("1", "gwinya", "Bread", 5.0, 14)
    private val latte = Item("2", "latte", "Coffee", 24.0, 11)

    private fun emptyOrder() = Order(
        id = 1,
        customer = "Walk-in",
        timestamp = 0L,
        items = emptyList(),
        total = 0.0
    )

    @Test
    fun totalItemsCount_sumsAllQuantities() {
        val order = Order(
            id = 2,
            customer = "Test",
            timestamp = 0L,
            items = listOf(CartItem(gwinya, 2), CartItem(latte, 1)),
            total = 34.0
        )
        assertEquals(3, order.totalItemsCount)
    }

    @Test
    fun totalItemsCount_isZeroForEmptyOrder() {
        assertEquals(0, emptyOrder().totalItemsCount)
    }

    @Test
    fun newOrder_defaultsToPendingStatus() {
        assertEquals("Pending", emptyOrder().status)
    }

    @Test
    fun newOrder_defaultsToCashPayment() {
        assertEquals("Cash", emptyOrder().paymentMethod)
    }

    @Test
    fun newOrder_hasNoTenderedAmountOrChangeByDefault() {
        val order = emptyOrder()
        assertEquals(0.0, order.amountTendered, 0.001)
        assertEquals(0.0, order.change, 0.001)
    }

    @Test
    fun status_canBeMarkedCompleted() {
        val order = emptyOrder()
        order.status = "Completed"
        assertEquals("Completed", order.status)
    }
}