package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.SalesAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AnalyticsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val analyticsCollection = firestore.collection("sales_analytics")

    suspend fun getAnalyticsForDate(dateStr: String): SalesAnalytics? {
        val snapshot = analyticsCollection.whereEqualTo("date", dateStr).get().await()
        val list = snapshot.toObjects(SalesAnalytics::class.java)
        return list.firstOrNull()
    }

    suspend fun saveAnalytics(analytics: SalesAnalytics) {
        val id = analytics.analyticsId.ifEmpty { analyticsCollection.document().id }
        val toSave = analytics.copy(analyticsId = id)
        analyticsCollection.document(id).set(toSave).await()
    }

    suspend fun getAllAnalytics(): List<SalesAnalytics> {
        val snapshot = analyticsCollection.orderBy("date").get().await()
        return snapshot.toObjects(SalesAnalytics::class.java)
    }
}
