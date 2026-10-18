package com.example.sixcoachconfectionery

import com.example.sixcoachconfectionery.data.models.CartItem
import com.example.sixcoachconfectionery.data.models.Item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartItemTest {

    private val gwinya = Item("1", "gwinya", "Bread", 5.0, 14)
    private val latte = Item("2", "latte", "Coffee", 24.0, 11)

    @Test
    fun totalPrice_isPriceTimesQuantity() {
        val line = CartItem(latte, 3)
        assertEquals(72.0, line.totalPrice, 0.001)
    }

    @Test
    fun totalPrice_isZeroWhenQuantityIsZero() {
        val line = CartItem(gwinya, 0)
        assertEquals(0.0, line.totalPrice, 0.001)
    }

    @Test
    fun totalPrice_updatesWhenQuantityChanges() {
        val line = CartItem(gwinya, 1)
        line.quantity = 4
        assertEquals(20.0, line.totalPrice, 0.001)
    }

    @Test
    fun cartTotal_sumsAllLines() {
        val cart = listOf(CartItem(gwinya, 2), CartItem(latte, 1))
        assertEquals(34.0, cart.sumOf { it.totalPrice }, 0.001)
    }

    @Test
    fun emptyCart_totalIsZero() {
        val cart = emptyList<CartItem>()
        assertEquals(0.0, cart.sumOf { it.totalPrice }, 0.001)
    }

    @Test
    fun item_isActiveByDefault() {
        assertTrue(gwinya.isActive)
    }
}