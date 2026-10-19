package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductRepositoryTest {

    private val productRepository = ProductRepository()

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
