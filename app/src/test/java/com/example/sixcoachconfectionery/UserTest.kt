package com.example.sixcoachconfectionery

import com.example.sixcoachconfectionery.data.models.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTest {

    @Test
    fun newUser_isNotAdminByDefault() {
        val user = User("usr_1", "staff@example.com", "hash")
        assertFalse(user.isAdmin)
    }

    @Test
    fun adminFlag_canBeSetOnCreation() {
        val user = User("usr_2", "admin@example.com", "hash", isAdmin = true)
        assertTrue(user.isAdmin)
    }

    @Test
    fun promotingUser_doesNotChangeTheOriginal() {
        val staff = User("usr_3", "staff@example.com", "hash")
        val promoted = staff.copy(isAdmin = true)
        assertTrue(promoted.isAdmin)
        assertFalse(staff.isAdmin)
    }

    @Test
    fun createdAt_defaultsToCurrentTime() {
        val before = System.currentTimeMillis()
        val user = User("usr_4", "staff@example.com", "hash")
        val after = System.currentTimeMillis()
        assertTrue(user.createdAt in before..after)
    }

    @Test
    fun generatedId_startsWithUsrPrefix() {
        assertTrue(User.generatedId().startsWith("usr_"))
    }

    @Test
    fun usersWithSameFields_areEqual() {
        val a = User("usr_5", "same@example.com", "hash", false, 1000L)
        val b = User("usr_5", "same@example.com", "hash", false, 1000L)
        assertEquals(a, b)
    }
}