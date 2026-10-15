package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.repository.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductRepositoryTest {

    private val productRepository = ProductRepository()

    @Test
    fun testGetProducts() = runBlocking {
        val result = productRepository.getProducts()
        assertNotNull(result)
    }

    @Test
    fun testGetCategories() = runBlocking {
        val result = productRepository.getCategories()
        assertNotNull(result)
    }
}
