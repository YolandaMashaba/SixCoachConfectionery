package com.example.sixcoachconfectionery

import com.example.sixcoachconfectionery.data.models.CartItem
import com.example.sixcoachconfectionery.data.models.Item
import com.example.sixcoachconfectionery.data.models.Order
import com.example.sixcoachconfectionery.data.models.User
import org.junit.Assert.*
import org.junit.Test

class SixCoachConfectioneryAppUnitTest {

    @Test
    fun testUserAdminCheck() {
        val adminUser = User(
            id = "usr_test",
            username = "ST10385107@rcconnect.edu.za",
            passwordHash = "password123",
            isAdmin = true
        )

        val staffUser = User(
            id = "usr_staff",
            username = "StaffMember",
            passwordHash = "password123",
            isAdmin = false
        )

        assertEquals("usr_test", adminUser.id)
        assertEquals("ST10385107@rcconnect.edu.za", adminUser.username)
        assertTrue(adminUser.isAdmin)
        assertFalse(staffUser.isAdmin)
    }

    @Test
    fun testItemPriceAndStockCalculation() {
        val gwinya = Item("1", "gwinya", "Bread", 5.0, 14)
        val latte = Item("2", "latte", "Coffee", 24.0, 11)

        val cartItem1 = CartItem(gwinya, 2)
        val cartItem2 = CartItem(latte, 1)

        assertEquals(10.0, cartItem1.totalPrice, 0.001)
        assertEquals(24.0, cartItem2.totalPrice, 0.001)

        val totalCart = listOf(cartItem1, cartItem2).sumOf { it.totalPrice }
        assertEquals(34.0, totalCart, 0.001)
    }

    @Test
    fun testOrderPaymentChangeCalculation() {
        val gwinya = Item("1", "gwinya", "Bread", 5.0, 14)
        val latte = Item("2", "latte", "Coffee", 24.0, 11)

        val items = listOf(CartItem(gwinya, 1), CartItem(latte, 1))
        val total = items.sumOf { it.totalPrice } // 29.00
        assertEquals(29.0, total, 0.001)

        val amountTendered = 50.00
        val change = amountTendered - total // 21.00
        assertEquals(21.0, change, 0.001)

        val order = Order(
            id = 3,
            customer = "ST10385107@rcconnect.edu.za",
            timestamp = System.currentTimeMillis(),
            items = items,
            total = total,
            paymentMethod = "Cash",
            amountTendered = amountTendered,
            change = change,
            status = "Pending"
        )

        assertEquals("Pending", order.status)
        order.status = "Completed"
        assertEquals("Completed", order.status)
    }
}
