package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.repository.OrderRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrderRepositoryTest {

    private val orderRepository = OrderRepository()

    @Test
    fun testGetOrders() = runBlocking {
        val result = orderRepository.getOrders()
        assertNotNull(result)
    }
}
