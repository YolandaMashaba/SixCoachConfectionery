package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import com.example.sixcoachconfectionery.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductRepositoryTest {

    private val productRepository = ProductRepository()
    private val authRepository = AuthRepository()

    @Before
    fun setUp() = runBlocking {
        try {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("admin@email.com", "admin@123").await()
        } catch (e: Exception) {
            authRepository.seedAdminAccount("admin@email.com", "admin@123")
            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword("admin@email.com", "admin@123").await()
            } catch (_: Exception) {}
        }
    }

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

    @Test
    fun testAddNewInventoryItem() = runBlocking {
        val result = productRepository.addNewInventoryItem("Test Muffin", "Bakery", 15.00, 25)
        assertNotNull(result)
        if (result.isFailure) {
            println("Failed to add inventory item: ${result.exceptionOrNull()?.message}")
        }
        org.junit.Assert.assertTrue(result.isSuccess)
        val product = result.getOrNull()
        assertNotNull(product)
        org.junit.Assert.assertEquals("Test Muffin", product?.productName)
        org.junit.Assert.assertEquals("Bakery", product?.categoryId)
        org.junit.Assert.assertEquals(15.00, product?.price ?: 0.0, 0.01)
        org.junit.Assert.assertEquals(25, product?.quantityInStock)
        org.junit.Assert.assertEquals("In Stock", product?.availability)
    }
}
