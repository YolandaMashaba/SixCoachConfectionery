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

    private fun waitUntil(timeoutMs: Long = 5000, condition: () -> Boolean): Boolean {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            if (condition()) return true
            Thread.sleep(50)
        }
        return condition()
    }

    private fun storeFile(dir: File) = File(dir, "six_coach_confectionery_store.json")

    @Before
    fun setUp() {
        folder = tempFolder.newFolder()
        repo = newRepo(folder)
        // The repository loads in the background; wait until it has written its store file.
        waitUntil { storeFile(folder).exists() && storeFile(folder).length() > 0 }
        Thread.sleep(200)
    }

    @Test
    fun freshStore_isSeededWithDefaultItemsAndOrders() {
        assertEquals(2, repo.getAllItems().size)
        assertEquals(2, repo.getOrders().size)
        assertEquals(0, repo.getUsers().size)
    }

    @Test
    fun register_firstUser_becomesAdmin() {
        val result = repo.registerUser("boss", "abc")
        assertTrue(result.first)
        assertTrue(repo.getCurrentUser()!!.isAdmin)
    }

    @Test
    fun register_secondUser_isNotAdmin() {
        repo.registerUser("boss", "abc")
        val result = repo.registerUser("cashier", "abc")
        assertTrue(result.first)
        assertEquals(2, repo.getUsers().size)
        assertFalse(repo.getCurrentUser()!!.isAdmin)
    }

    @Test
    fun register_duplicateUsername_isRejected() {
        repo.registerUser("cashier", "abc")
        val result = repo.registerUser("CASHIER", "abc")
        assertFalse(result.first)
        assertEquals(1, repo.getUsers().size)
    }

    @Test
    fun login_withCorrectCredentials_succeeds() {
        repo.registerUser("cashier", "pass123")
        repo.logout()
        val result = repo.loginUser("cashier", "pass123")
        assertTrue(result.first)
    }

    @Test
    fun login_withWrongPassword_fails() {
        repo.registerUser("cashier", "pass123")
        repo.logout()
        val result = repo.loginUser("cashier", "wrong")
        assertFalse(result.first)
        assertEquals("Invalid password", result.second)
    }

    @Test
    fun login_withUnknownUser_fails() {
        val result = repo.loginUser("nobody", "pass123")
        assertFalse(result.first)
        assertEquals("User not found", result.second)
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
        // Saving happens in the background; wait until it reaches the file.
        waitUntil { storeFile(folder).readText().contains("persisted") }
        val reloaded = newRepo(folder)
        // Loading also happens in the background; wait for the user to appear.
        val found = waitUntil { reloaded.getUsers().any { it.username == "persisted" } }
        assertTrue(found)
    }
}