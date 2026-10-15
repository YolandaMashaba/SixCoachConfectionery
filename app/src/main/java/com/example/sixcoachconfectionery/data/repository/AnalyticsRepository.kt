package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.SalesAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AnalyticsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val analyticsCollection = firestore.collection("sales_analytics")

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
}
