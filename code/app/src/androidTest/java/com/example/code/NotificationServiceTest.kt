// Instrumented tests for NotificationDB and NotificationService against Firestore. Run on the emulator.
// Each test uses fake receiver IDs and deletes what it sent.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationServiceTest {
    private val userDB = UserDB()
    private val notificationDB = NotificationDB()
    private val service = NotificationService(notificationDB, userDB)
    private val first = fakeID()
    private val second = fakeID()

    @After
    fun cleanUp() {
        deleteNotificationsFor(first)
        deleteNotificationsFor(second)
        Firebase.firestore.collection("users").document(first.toString()).delete()
    }

    @Test
    fun addNotificationSaves() {
        notificationDB.addNotification(
            Notification(Message(1, first, "hello", Timestamp.now()), NotificationType.OWNER_ANNOUNCEMENT)
        )
        waitUntil { notificationsFor(first).isNotEmpty() }
        val saved = notificationsFor(first).single()
        assertEquals("hello", saved.message.content)
        assertEquals(NotificationType.OWNER_ANNOUNCEMENT, saved.type)
    }

    @Test
    fun deleteNotificationRemovesIt() {
        val notification = Notification(Message(1, first, "bye", Timestamp.now()), NotificationType.LOTTERY_LOST)
        notificationDB.addNotification(notification)
        waitUntil { notificationsFor(first).isNotEmpty() }
        notificationDB.deleteNotification(notification)
        waitUntil { notificationsFor(first).isEmpty() }
        assertTrue(notificationsFor(first).isEmpty())
    }

    @Test
    fun sendToAllReachesEveryReceiver() {
        service.sendToAll(listOf(first, second), NotificationType.LOTTERY_WON, "You won")
        waitUntil { notificationsFor(first).isNotEmpty() && notificationsFor(second).isNotEmpty() }
        listOf(first, second).forEach { id ->
            val saved = notificationsFor(id).single()
            assertEquals(NotificationService.SYSTEM_ID, saved.message.senderID)
            assertEquals(NotificationType.LOTTERY_WON, saved.type)
        }
    }

    @Test
    fun optedOutUserGetsNothing() {
        Firebase.firestore.collection("users").document(first.toString())
            .set(User(name = "Quiet", userID = first, role = "Entrant", notificationsEnabled = false))
        waitUntil { userDB.users.any { it.userID == first } }
        service.send(Notification(Message(1, first, "hi", Timestamp.now()), NotificationType.OWNER_ANNOUNCEMENT))
        service.send(Notification(Message(1, second, "hi", Timestamp.now()), NotificationType.OWNER_ANNOUNCEMENT))
        waitUntil { notificationsFor(second).isNotEmpty() }
        Thread.sleep(1000)
        assertTrue(notificationsFor(first).isEmpty())
        assertEquals(1, notificationsFor(second).size)
    }
}
