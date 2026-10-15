package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.repository.AnalyticsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InventoryRepositoryTest {

    private val analyticsRepository = AnalyticsRepository()

    @Test
    fun testGetAllAnalytics() = runBlocking {
        val result = analyticsRepository.getAllAnalytics()
        assertNotNull(result)
    }
}
