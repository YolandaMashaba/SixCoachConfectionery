package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.model.KitchenOrder
import com.example.sixcoachconfectionery.data.model.KitchenOrderItem
import com.example.sixcoachconfectionery.data.model.Order
import com.example.sixcoachconfectionery.data.model.OrderItem
import com.example.sixcoachconfectionery.data.model.Payment
import com.example.sixcoachconfectionery.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrderRepositoryTest {

    private val orderRepository = OrderRepository()

    @Before
    fun setUp() {
        runBlocking {
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()

            val uid = try {
                val createResult = auth.createUserWithEmailAndPassword("admin@email.com", "admin@123").await()
                createResult.user?.uid ?: throw IllegalStateException("User is null")
            } catch (e: Exception) {
                val signInResult = auth.signInWithEmailAndPassword("admin@email.com", "admin@123").await()
                signInResult.user?.uid ?: throw IllegalStateException("User is null after sign in")
            }

            val adminProfile = mapOf(
                "uid" to uid,
                "email" to "admin@email.com",
                "username" to "Admin",
                "role" to "Admin",
                "isAdmin" to true,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(uid).set(adminProfile).await()
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
