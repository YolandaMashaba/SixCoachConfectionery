package com.example.sixcoachconfectionery

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sixcoachconfectionery.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {

    private val authRepository = AuthRepository()

    @Test
    fun testGetAllUsers() = runBlocking {
        val result = authRepository.getAllUsers()
        assertNotNull(result)
    }
}
