package com.example.code

import com.google.firebase.Timestamp

/**
 * Sends notifications and skips users who opted out (US 01.10.01).
 * Used for lottery results (US 01.03.01, US 01.03.02) and owner messages (US 02.10.01).
 *
 * Outstanding Issues: N/A
 *
 * @property notificationDB where notifications are saved
 * @property userDB used to check who opted out
 */
class NotificationService(
    private val notificationDB: NotificationDB,
    private val userDB: UserDB
) {
    companion object {
        /** Sender ID for notifications the app sends itself. UserDB never picks a negative ID. */
        const val SYSTEM_ID = -2
    }

    /**
     * Sends one notification, unless the receiver opted out.
     *
     * @param notification notification to send
     */
    fun send(notification: Notification) {
        if (isOptedIn(notification.message.receiverID)) {
            notificationDB.addNotification(notification)
        }
    }

    /**
     * Sends the same text to every user in a list, from the app itself.
     *
     * @param receiverIDs users to notify
     * @param type what the notification is about
     * @param text what the notification says
     */
    fun sendToAll(receiverIDs: List<Int>, type: NotificationType, text: String) {
        val now = Timestamp.now()
        receiverIDs.forEach { id ->
            send(Notification(Message(SYSTEM_ID, id, text, now), type))
        }
    }

    /**
     * Checks if a user still gets notifications. Unknown users get them.
     *
     * @param userID user to check
     * @return false only if the user opted out
     */
    private fun isOptedIn(userID: Int): Boolean {
        return userDB.users.find { it.userID == userID }?.notificationsEnabled ?: true
    }
}
