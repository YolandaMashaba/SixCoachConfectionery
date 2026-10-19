package com.example.sixcoachconfectionery

import android.content.Context
import android.content.ContextWrapper
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.data.models.CartItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SixCoachRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var folder: File
    private lateinit var repo: SixCoachConfectioneryRepository

    private fun newRepo(dir: File): SixCoachConfectioneryRepository {
        val context = object : ContextWrapper(null) {
            override fun getFilesDir(): File = dir
        }
        val constructor = SixCoachConfectioneryRepository::class.java
            .getDeclaredConstructor(Context::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(context)
    }

    @Before
    fun setUp() {
        folder = tempFolder.newFolder()
        repo = newRepo(folder)
    }

    @Test
    fun freshStore_isSeededWithDefaultData() {
        assertEquals(2, repo.getUsers().size)
        assertEquals(2, repo.getAllItems().size)
        assertEquals(2, repo.getOrders().size)
    }

    @Test
    fun login_withCorrectCredentials_succeeds() {
        val result = repo.loginUser("Zwide", "password123")
        assertTrue(result.first)
    }

    @Test
    fun login_withWrongPassword_fails() {
        val result = repo.loginUser("Zwide", "wrong")
        assertFalse(result.first)
        assertEquals("Invalid password", result.second)
    }

    @Test
    fun login_withUnknownUser_fails() {
        val result = repo.loginUser("nobody", "password123")
        assertFalse(result.first)
        assertEquals("User not found", result.second)
    }

    @Test
    fun register_duplicateUsername_isRejected() {
        val result = repo.registerUser("zwide", "abc")
        assertFalse(result.first)
        assertEquals(2, repo.getUsers().size)
    }

    @Test
    fun register_newUser_succeedsAndIsNotAdmin() {
        val result = repo.registerUser("newcashier", "abc")
        assertTrue(result.first)
        assertEquals(3, repo.getUsers().size)
        assertFalse(repo.getCurrentUser()!!.isAdmin)
    }

    @Test
    fun createOrder_emptyCart_fails() {
        val result = repo.createOrder(emptyList(), "Cash", 10.0)
        assertFalse(result.first)
        assertEquals("Cart is empty.", result.second)
    }

    @Test
    fun createOrder_deductsStock_andAddsPendingOrder() {
        val gwinya = repo.getAllItems().first { it.id == "item_1" }
        val result = repo.createOrder(listOf(CartItem(gwinya, 2)), "Cash", 20.0)
        assertTrue(result.first)
        assertEquals(12, repo.getAllItems().first { it.id == "item_1" }.stock)
        assertEquals(3, repo.getOrders().size)
        assertEquals(1, repo.getPendingOrders().size)
    }

    @Test
    fun createOrder_notEnoughStock_fails() {
        val gwinya = repo.getAllItems().first { it.id == "item_1" }
        val result = repo.createOrder(listOf(CartItem(gwinya, 100)), "Cash", 1000.0)
        assertFalse(result.first)
    }

    @Test
    fun createOrder_cashTooLow_fails() {
        val gwinya = repo.getAllItems().first { it.id == "item_1" }
        val result = repo.createOrder(listOf(CartItem(gwinya, 1)), "Cash", 1.0)
        assertFalse(result.first)
    }

    @Test
    fun completeOrder_marksOrderCompleted() {
        val gwinya = repo.getAllItems().first { it.id == "item_1" }
        repo.createOrder(listOf(CartItem(gwinya, 1)), "Card", 5.0)
        val newId = repo.getPendingOrders().first().id
        assertTrue(repo.completeOrder(newId))
        assertEquals(0, repo.getPendingOrders().size)
    }

    @Test
    fun data_persistsAcrossRepositoryInstances() {
        repo.registerUser("persisted", "abc")
        val reloaded = newRepo(folder)
        assertTrue(reloaded.getUsers().any { it.username == "persisted" })
    }
}