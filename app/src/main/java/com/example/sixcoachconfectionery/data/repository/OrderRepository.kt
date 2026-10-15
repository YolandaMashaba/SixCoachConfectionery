package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.KitchenOrder
import com.example.sixcoachconfectionery.data.model.KitchenOrderItem
import com.example.sixcoachconfectionery.data.model.Order
import com.example.sixcoachconfectionery.data.model.OrderItem
import com.example.sixcoachconfectionery.data.model.Payment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val ordersCollection = firestore.collection("orders")
    private val orderItemsCollection = firestore.collection("order_items")
    private val paymentsCollection = firestore.collection("payments")
    private val kitchenOrdersCollection = firestore.collection("kitchen_orders")
    private val kitchenOrderItemsCollection = firestore.collection("kitchen_order_items")

    suspend fun createCompleteOrder(
        order: Order,
        items: List<OrderItem>,
        payment: Payment,
        kitchenOrder: KitchenOrder,
        kitchenItems: List<KitchenOrderItem>
    ): Result<Unit> {
        return try {
            val orderId = order.orderId.ifEmpty { ordersCollection.document().id }
            val orderToSave = order.copy(orderId = orderId)

            val paymentId = payment.paymentId.ifEmpty { paymentsCollection.document().id }
            val paymentToSave = payment.copy(paymentId = paymentId, orderId = orderId)

            val kitchenOrderId = kitchenOrder.kitchenOrderId.ifEmpty { kitchenOrdersCollection.document().id }
            val kitchenOrderToSave = kitchenOrder.copy(kitchenOrderId = kitchenOrderId, orderId = orderId)

            val batch = firestore.batch()

            // Save Order
            batch.set(ordersCollection.document(orderId), orderToSave)

            // Save Order Items
            for (item in items) {
                val itemId = item.orderItemId.ifEmpty { orderItemsCollection.document().id }
                val itemToSave = item.copy(orderItemId = itemId, orderId = orderId)
                batch.set(orderItemsCollection.document(itemId), itemToSave)
            }

            // Save Payment
            batch.set(paymentsCollection.document(paymentId), paymentToSave)

            // Save Kitchen Order
            batch.set(kitchenOrdersCollection.document(kitchenOrderId), kitchenOrderToSave)

            // Save Kitchen Order Items
            for (kItem in kitchenItems) {
                val kItemId = kItem.kitchenOrderItemId.ifEmpty { kitchenOrderItemsCollection.document().id }
                val kItemToSave = kItem.copy(kitchenOrderItemId = kItemId, kitchenOrderId = kitchenOrderId)
                batch.set(kitchenOrderItemsCollection.document(kItemId), kItemToSave)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrders(): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection.get().await()
            Result.success(snapshot.toObjects(Order::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getKitchenOrders(): Result<List<KitchenOrder>> {
        return try {
            val snapshot = kitchenOrdersCollection.whereEqualTo("status", "Pending").get().await()
            Result.success(snapshot.toObjects(KitchenOrder::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateKitchenOrderStatus(kitchenOrderId: String, newStatus: String): Result<Unit> {
        return try {
            val updateMap = mutableMapOf<String, Any>("status" to newStatus)
            if (newStatus == "Completed") {
                updateMap["completedAt"] = System.currentTimeMillis()
            }
            kitchenOrdersCollection.document(kitchenOrderId).update(updateMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
