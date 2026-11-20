package com.example.code

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test

// Covers the Notification model and its types.
class NotificationTest {

    @Test
    fun keepsMessageAndType() {
        val now = Timestamp.now()
        val notification = Notification(Message(1, 2, "You won", now), NotificationType.LOTTERY_WON)
        assertEquals(1, notification.message.senderID)
        assertEquals(2, notification.message.receiverID)
        assertEquals("You won", notification.message.content)
        assertEquals(now, notification.message.timestamp)
        assertEquals(NotificationType.LOTTERY_WON, notification.type)
    }

    @Test
    fun entrantCanMessageOwner() {
        val notification = Notification(Message(5, 9, "Is the site pet friendly?", Timestamp.now()), NotificationType.ENTRANT_MESSAGE)
        assertEquals(5, notification.message.senderID)
        assertEquals(9, notification.message.receiverID)
        assertEquals(NotificationType.ENTRANT_MESSAGE, notification.type)
    }

    @Test
    fun defaultIsOwnerAnnouncement() {
        assertEquals(NotificationType.OWNER_ANNOUNCEMENT, Notification().type)
    }

    @Test
    fun typesMatchTheUml() {
        assertEquals(
            listOf("LOTTERY_WON", "LOTTERY_LOST", "JOINED_WAITLIST",
                "RESERVATION_CONFIRMED", "RESERVATION_CANCELLED", "OWNER_ANNOUNCEMENT", "ENTRANT_MESSAGE"),
            NotificationType.entries.map { it.name }
        )
    }
}
