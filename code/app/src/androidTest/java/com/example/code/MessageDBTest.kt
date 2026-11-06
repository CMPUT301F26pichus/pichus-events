// Instrumented tests for MessageDB against Firestore. Run on the emulator.
// Uses fake sender and receiver IDs. MessageDB has no delete, so cleanup
// removes the test messages straight from Firestore.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageDBTest {
    private val messageDB = MessageDB()
    private val sender = User(name = "Test Sender", userID = fakeID(), role = "Campsite Owner")
    private val receiver = User(name = "Test Receiver", userID = fakeID(), role = "Entrant")
    private val content = "Test_${System.currentTimeMillis()}"

    private fun isTest(m: Message) = m.content == content

    @After
    fun cleanUp() {
        val messages = Firebase.firestore.collection("messages")
        val docs = Tasks.await(messages.whereEqualTo("senderID", sender.userID).get())
        docs.documents.forEach { Tasks.await(it.reference.delete()) }
    }

    @Test
    fun sendMessageShowsForSenderAndReceiver() {
        messageDB.sendMessage(Message(sender.userID, receiver.userID, content, Timestamp.now()))
        waitUntil { messageDB.viewReceivedMessages(receiver).any(::isTest) }
        assertTrue(messageDB.viewReceivedMessages(receiver).any(::isTest))
        assertTrue(messageDB.viewSentMessages(sender).any(::isTest))
    }

    @Test
    fun messageOnlyGoesOneWay() {
        messageDB.sendMessage(Message(sender.userID, receiver.userID, content, Timestamp.now()))
        waitUntil { messageDB.viewReceivedMessages(receiver).any(::isTest) }
        assertFalse(messageDB.viewReceivedMessages(sender).any(::isTest))
        assertFalse(messageDB.viewSentMessages(receiver).any(::isTest))
    }
}
