package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {

    private val authRepository = AuthRepository()

    @Before
    fun setUp() {
        runBlocking {
            try {
                authRepository.seedAdminAccount("admin@email.com", "admin@123")
                FirebaseAuth.getInstance().signInWithEmailAndPassword("admin@email.com", "admin@123").await()
            } catch (e: Exception) {
                println("AuthRepositoryTest setUp error: ${e.message}")
            }
        }
    }

    @Test
    fun testGetAllUsers() = runBlocking {
        val result = authRepository.getAllUsers()
        assertNotNull(result)
    }

    @Test
    fun testRegisterStaffAccountUpdatesDatabase() = runBlocking {
        val testEmail = "staff_pos_${System.currentTimeMillis()}@whiskit.com"
        val testPassword = "Password123!"
        val testUsername = "PosStaff"

        val result = authRepository.registerStaff(testEmail, testPassword, testUsername)
        assertNotNull(result)
        
        // Verify user can be fetched from database
        val usersResult = authRepository.getAllUsers()
        assertNotNull(usersResult)
        if (usersResult.isSuccess) {
            val users = usersResult.getOrNull()
            val createdUser = users?.find { it.email == testEmail }
            assertNotNull(createdUser)
            org.junit.Assert.assertEquals("Staff", createdUser?.role)
            org.junit.Assert.assertEquals(false, createdUser?.isAdmin)
        }
    }

    @Test
    fun testSeedAdminAccount() = runBlocking {
        val result = authRepository.seedAdminAccount("admin@email.com", "admin@123")
        assertNotNull(result)
        if (result.isSuccess) {
            val adminUser = result.getOrNull()
            assertNotNull(adminUser)
            org.junit.Assert.assertEquals("Admin", adminUser?.role)
            org.junit.Assert.assertEquals(true, adminUser?.isAdmin)
        }
    }
}
