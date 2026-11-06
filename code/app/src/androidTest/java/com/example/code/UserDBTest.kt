// Instrumented tests for UserDB against Firestore. Run on the emulator.
// Each test uses a unique email and deletes the user it made.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDBTest {
    private val userDB = UserDB()
    private val email = "test_${System.currentTimeMillis()}@test.com"

    private fun findTestUser(): User? = userDB.users.find { it.email == email }

    /** Adds a valid test user and waits until it shows up. */
    private fun addTestUser(): User {
        userDB.addUser("Test User", email, "7805550000", "Entrant")
        waitUntil { findTestUser() != null }
        val user = findTestUser()
        assertNotNull("test user never showed up", user)
        return user!!
    }

    @After
    fun cleanUp() {
        userDB.users.filter { it.email.startsWith("test_") && it.email.endsWith("@test.com") }
            .forEach { userDB.deleteUser(it) }
    }

    @Test
    fun addUserShowsInUsers() {
        val user = addTestUser()
        assertEquals("Test User", user.name)
        assertEquals(7805550000L, user.phoneNum)
        assertEquals("Entrant", user.role)
    }

    @Test
    fun addUserRejectsBadPhoneNumber() {
        userDB.addUser("Test User", email, "12345", "Entrant")
        Thread.sleep(2000)
        assertFalse(userDB.users.any { it.email == email })
    }

    @Test
    fun addUserRejectsBadRole() {
        userDB.addUser("Test User", email, "7805550000", "Admin")
        Thread.sleep(2000)
        assertFalse(userDB.users.any { it.email == email })
    }

    @Test
    fun deleteUserRemovesUser() {
        val user = addTestUser()
        userDB.deleteUser(user)
        waitUntil { findTestUser() == null }
        assertTrue(findTestUser() == null)
    }

    @Test
    fun updateNameChangesName() {
        val user = addTestUser()
        userDB.updateName(user, "New Name")
        waitUntil { findTestUser()?.name == "New Name" }
        assertEquals("New Name", findTestUser()?.name)
    }

    @Test
    fun updateEmailChangesEmail() {
        val user = addTestUser()
        val newEmail = "test_new_${System.currentTimeMillis()}@test.com"
        userDB.updateEmail(user, newEmail)
        waitUntil { userDB.users.any { it.email == newEmail } }
        assertTrue(userDB.users.any { it.email == newEmail && it.userID == user.userID })
    }

    @Test
    fun updatePhoneNumChangesNumber() {
        val user = addTestUser()
        userDB.updatePhoneNum(user, "5875551234")
        waitUntil { findTestUser()?.phoneNum == 5875551234L }
        assertEquals(5875551234L, findTestUser()?.phoneNum)
    }

    @Test
    fun updatePhoneNumIgnoresBadNumber() {
        val user = addTestUser()
        userDB.updatePhoneNum(user, "123")
        Thread.sleep(2000)
        assertEquals(7805550000L, findTestUser()?.phoneNum)
    }
}
