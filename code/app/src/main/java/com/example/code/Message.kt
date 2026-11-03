package com.example.code

import com.google.firebase.Timestamp

/**
 * Represents all messages sent in the app
 *
 * A message has a sender and receiver ID, content and timestamp.
 *
 * @property senderID id of sender
 * @property receiverID id of receiver
 * @property content content of the message
 * @property timestamp time that the message was sent
 */
class Message(val senderID: Int = -1,
              val receiverID: Int = -1,
              val content: String = "",
              val timestamp: Timestamp? = null) // Learned that Timestamp is the best data type to store times from Google, Gemini, "what is the ideal way to store dates in firestore when using kotlin?", 2026-11-02

