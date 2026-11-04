package com.example.code
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Stores all messages in FireBase, allows for retrieval and addition
 */
class MessageDB {
    private val db = Firebase.firestore
    private val messagesRef = db.collection("messages")


    private val _messages = mutableStateListOf<Message>()

    /**
     * Returns local messages list
     *
     * @return list of all messages
     */
    val messages: List<Message> // admin can use this to see all messages
        get() = _messages

    /**
     * Adds a message to the database
     *
     * @param message message to be added
     * @return Unit
     */
    fun sendMessage(message: Message) {
        messagesRef.document().set(message)
    }

    // The following function (viewMessages(user: User) is a revised version from what I (Kundan) wrote from Google, Gemini, "how to display a kotlin firestore query snapshot", 2026-11-03
    // My original function implemented the same query, but returned the query snapshot without converting it to a list
    // The other query functions were re-implemented using the same structure + operations
    /**
     * Queries database to only return a user's received messages
     *
     * @param user used to only return messages received by a specific user
     * @return the list of relevant messages, or an empty list
     */
    suspend fun viewReceivedMessages(user: User): List<Message> {
        return try {
            val querySnapshot = messagesRef
                .whereEqualTo("receiverID", user.userID)

                .get()
                .await()

            querySnapshot?.toObjects(Message::class.java) ?: emptyList() // converts query snapshot to a list of messages
        } catch (e: Exception) {
            emptyList() // return an empty list if any issues occur
        }
    }

    /**
     * Queries database to only return a user's sent messages
     *
     * @param user used to only return messages sent by a specific user
     * @return the list of relevant messages, or an empty list
     */
    suspend fun viewSentMessages(user: User): List<Message> {
        return try {
            val querySnapshot = messagesRef
                .whereEqualTo("senderID", user.userID)

                .get()
                .await()

            querySnapshot?.toObjects(Message::class.java) ?: emptyList() // converts query snapshot to a list of messages
        } catch (e: Exception) {
            emptyList() // return an empty list if any issues occur
        }
    }

    /**
     * Updates local _messages list
     */
    init {
        messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _messages.clear()
            snapshot?.documents?.forEach { document ->
                val message = document.toObject(Message::class.java)
                if (message != null) {
                    _messages.add(message)
                }
            }
        }
    }
}