package com.example.code
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

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

    /**
     * Obtains a user's received messages
     *
     * @param user used to only return messages received by a specific user
     * @return the list of received messages
     */
    fun viewReceivedMessages(user: User): List<Message> {
        return _messages.filter { it.receiverID == user.userID }
    }

    /**
     * Obtains a user's sent messages
     *
     * @param user used to only return messages sent by a specific user
     * @return the list of messages sent by the user
     */
    fun viewSentMessages(user: User): List<Message> {
        return _messages.filter { it.senderID == user.userID }
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