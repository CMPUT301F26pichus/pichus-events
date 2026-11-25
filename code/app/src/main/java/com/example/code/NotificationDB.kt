package com.example.code

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Stores notifications in Firestore.
 * Every notification ever sent stays here, so admins can review them (US 03.08.01).
 *
 * Outstanding Issues: N/A
 */
class NotificationDB {
    private val db = Firebase.firestore
    private val notifications = db.collection("notifications")

    /**
     * Saves a notification.
     *
     * @param notification notification to save
     */
    fun addNotification(notification: Notification) {
        notifications.document().set(notification)
    }

    /**
     * Deletes a notification. Matches on receiver, time and type, since notifications have no ID.
     *
     * @param notification notification to delete
     */
    fun deleteNotification(notification: Notification) {
        notifications
            .whereEqualTo("message.receiverID", notification.message.receiverID)
            .whereEqualTo("message.timestamp", notification.message.timestamp)
            .whereEqualTo("type", notification.type.name)
            .get()
            .addOnSuccessListener { result ->
                result.documents.forEach { it.reference.delete() }
            }
    }
}
