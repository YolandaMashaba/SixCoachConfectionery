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

    /**
     * Places an order atomically using a Cloud Firestore Transaction.
     * Validates product stock availability, decrements stock, records inventory transactions,
     * and saves the complete order, order items, payment, and kitchen order.
     * Automatically handles rollbacks and contention retries.
     */
    suspend fun placeOrderWithTransaction(
        order: Order,
        items: List<OrderItem>,
        payment: Payment,
        kitchenOrder: KitchenOrder,
        kitchenItems: List<KitchenOrderItem>
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val productDocs = mutableMapOf<String, com.example.sixcoachconfectionery.data.model.Product>()

                // 1. Read product stocks and validate sufficient stock availability
                for (item in items) {
                    val productRef = firestore.collection("products").document(item.productId)
                    val snapshot = transaction.get(productRef)
                    val product = snapshot.toObject(com.example.sixcoachconfectionery.data.model.Product::class.java)
                        ?: throw IllegalStateException("Product not found: ${item.productId}")

                    if (product.quantityInStock < item.quantity) {
                        throw IllegalStateException("Insufficient stock for '${product.productName}'. Available: ${product.quantityInStock}, Requested: ${item.quantity}")
                    }
                    productDocs[item.productId] = product
                }

                // 2. Prepare IDs
                val orderId = order.orderId.ifEmpty { ordersCollection.document().id }
                val orderToSave = order.copy(orderId = orderId)

                val paymentId = payment.paymentId.ifEmpty { paymentsCollection.document().id }
                val paymentToSave = payment.copy(paymentId = paymentId, orderId = orderId)

                val kitchenOrderId = kitchenOrder.kitchenOrderId.ifEmpty { kitchenOrdersCollection.document().id }
                val kitchenOrderToSave = kitchenOrder.copy(kitchenOrderId = kitchenOrderId, orderId = orderId)

                // 3. Decrement stock levels and record inventory transactions
                for (item in items) {
                    val product = productDocs[item.productId]!!
                    val newQuantity = product.quantityInStock - item.quantity
                    val newAvailability = if (newQuantity > 0) "In Stock" else "Out of Stock"

                    val productRef = firestore.collection("products").document(item.productId)
                    transaction.update(
                        productRef,
                        mapOf(
                            "quantityInStock" to newQuantity,
                            "availability" to newAvailability
                        )
                    )

                    val transRef = firestore.collection("inventory_transactions").document()
                    val transactionRecord = com.example.sixcoachconfectionery.data.model.InventoryTransaction(
                        transactionId = transRef.id,
                        productId = item.productId,
                        staffId = order.staffId,
                        transactionType = "Sale",
                        quantity = item.quantity,
                        notes = "Order transaction: $orderId"
                    )
                    transaction.set(transRef, transactionRecord)
                }

                // 4. Save Order, Order Items, Payment, Kitchen Order, Kitchen Order Items
                val orderRef = ordersCollection.document(orderId)
                transaction.set(orderRef, orderToSave)

                for (item in items) {
                    val itemRef = orderItemsCollection.document()
                    val itemToSave = item.copy(orderItemId = itemRef.id, orderId = orderId)
                    transaction.set(itemRef, itemToSave)
                }

                val paymentRef = paymentsCollection.document(paymentId)
                transaction.set(paymentRef, paymentToSave)

                val kOrderRef = kitchenOrdersCollection.document(kitchenOrderId)
                transaction.set(kOrderRef, kitchenOrderToSave)

                for (kItem in kitchenItems) {
                    val kItemRef = kitchenOrderItemsCollection.document()
                    val kItemToSave = kItem.copy(kitchenOrderItemId = kItemRef.id, kitchenOrderId = kitchenOrderId)
                    transaction.set(kItemRef, kItemToSave)
                }

                null
            }.await()

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
