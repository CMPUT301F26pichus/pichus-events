// Helpers for the notification tests: read and clean the notifications collection directly.
package com.example.code

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.concurrent.TimeUnit

/** Every notification stored for [receiverID]. */
fun notificationsFor(receiverID: Int): List<Notification> {
    val result = Tasks.await(
        Firebase.firestore.collection("notifications")
            .whereEqualTo("message.receiverID", receiverID).get(),
        10, TimeUnit.SECONDS
    )
    return result.documents.mapNotNull { it.toObject(Notification::class.java) }
}

/** Deletes every notification stored for [receiverID]. */
fun deleteNotificationsFor(receiverID: Int) {
    val result = Tasks.await(
        Firebase.firestore.collection("notifications")
            .whereEqualTo("message.receiverID", receiverID).get(),
        10, TimeUnit.SECONDS
    )
    result.documents.forEach { Tasks.await(it.reference.delete(), 10, TimeUnit.SECONDS) }
}
