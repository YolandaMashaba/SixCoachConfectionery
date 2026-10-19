package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.DashboardMetrics
import com.example.sixcoachconfectionery.data.model.Order
import com.example.sixcoachconfectionery.data.model.OrderItem
import com.example.sixcoachconfectionery.data.model.SalesAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AnalyticsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val analyticsCollection = firestore.collection("sales_analytics")
    private val ordersCollection = firestore.collection("orders")
    private val orderItemsCollection = firestore.collection("order_items")

    suspend fun getAnalyticsForDate(dateStr: String): Result<SalesAnalytics?> {
        return try {
            val snapshot = analyticsCollection.whereEqualTo("date", dateStr).get().await()
            val list = snapshot.toObjects(SalesAnalytics::class.java)
            Result.success(list.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveAnalytics(analytics: SalesAnalytics): Result<Unit> {
        return try {
            val id = analytics.analyticsId.ifEmpty { analyticsCollection.document().id }
            val toSave = analytics.copy(analyticsId = id)
            analyticsCollection.document(id).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllAnalytics(): Result<List<SalesAnalytics>> {
        return try {
            val snapshot = analyticsCollection.orderBy("date").get().await()
            Result.success(snapshot.toObjects(SalesAnalytics::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Dynamically calculates Dashboard Metrics (Total Orders, Total Revenue, Items Sold, Average Order Value)
     * by querying orders and order items from Firestore.
     */
    suspend fun calculateDashboardMetrics(): Result<DashboardMetrics> {
        return try {
            val ordersSnapshot = ordersCollection.get().await()
            val orders = ordersSnapshot.toObjects(Order::class.java)

            val itemsSnapshot = orderItemsCollection.get().await()
            val orderItems = itemsSnapshot.toObjects(OrderItem::class.java)

            val totalOrders = orders.size
            val totalRevenue = orders.sumOf { it.totalAmount }
            val itemsSold = orderItems.sumOf { it.quantity }
            val averageOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0

            val metrics = DashboardMetrics(
                totalOrders = totalOrders,
                totalRevenue = totalRevenue,
                itemsSold = itemsSold,
                averageOrderValue = averageOrderValue
            )

            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
