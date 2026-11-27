package com.example.code

import androidx.compose.runtime.mutableStateListOf
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

    // Local copy of all notifications, kept up to date by the listener in init
    private val _notifications = mutableStateListOf<Notification>()

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

    /**
     * Gets one user's notifications, newest first (US 01.03.01, US 01.03.02).
     *
     * @param userID user whose notifications to get
     * @return that user's notifications, newest first
     */
    fun getNotificationsFor(userID: Int): List<Notification> {
        return _notifications
            .filter { it.message.receiverID == userID }
            .sortedByDescending { it.message.timestamp }
    }

    /**
     * Updates local _notifications list
     */
    init {
        notifications.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _notifications.clear()
            snapshot?.documents?.forEach { document ->
                val notification = document.toObject(Notification::class.java)
                if (notification != null) {
                    _notifications.add(notification)
                }
            }
        }
    }
}
