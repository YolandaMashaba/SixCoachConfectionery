package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.model.KitchenOrder
import com.example.sixcoachconfectionery.data.model.KitchenOrderItem
import com.example.sixcoachconfectionery.data.model.Order
import com.example.sixcoachconfectionery.data.model.OrderItem
import com.example.sixcoachconfectionery.data.model.Payment
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import com.example.sixcoachconfectionery.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrderRepositoryTest {

    private val orderRepository = OrderRepository()
    private val authRepository = AuthRepository()

    @Before
    fun setUp() {
        runBlocking {
            try {
                // Seed admin account with the specified email and await sign-in task
                authRepository.seedAdminAccount("ST10385107@rcconnect.edu.za", "admin@123")
                FirebaseAuth.getInstance().signInWithEmailAndPassword("ST10385107@rcconnect.edu.za", "admin@123").await()
            } catch (e: Exception) {
                println("OrderRepositoryTest setUp error: ${e.message}")
            }
        }
    }

    @Test
    fun testGetOrders() = runBlocking {
        val result = orderRepository.getOrders()
        assertNotNull(result)
    }

    @Test
    fun testPlaceOrderFlow() = runBlocking {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: "test_user_uid"

        val order = Order(
            orderId = "",
            orderDate = System.currentTimeMillis(),
            totalAmount = 50.0,
            status = "Pending",
            staffId = uid,
            customerId = uid
        )
        val items = listOf(
            OrderItem(orderItemId = "", orderId = "", productId = "prod_1", quantity = 2, unitPrice = 25.0, subtotal = 50.0)
        )
        val payment = Payment(
            paymentId = "",
            orderId = "",
            paymentMethod = "Cash",
            amount = 50.0,
            status = "Success"
        )
        val kitchenOrder = KitchenOrder(
            kitchenOrderId = "",
            orderId = "",
            status = "Pending",
            sentToKitchenAt = System.currentTimeMillis()
        )
        val kitchenItems = listOf(
            KitchenOrderItem(kitchenOrderItemId = "", kitchenOrderId = "", productId = "prod_1", quantity = 2, status = "Pending")
        )

        // Test creating order storage in Firestore
        val result = orderRepository.createCompleteOrder(order, items, payment, kitchenOrder, kitchenItems)
        assertNotNull(result)
        if (result.isFailure) {
            println("Failed to store order in Firestore: ${result.exceptionOrNull()?.message}")
        }
        org.junit.Assert.assertTrue(result.isSuccess)
    }
}
