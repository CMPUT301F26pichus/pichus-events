package com.example.code

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Stores waiting lists in Firestore, one per campsite, keyed by campsite ID.
 */
class WaitingListDB {
    private val db = Firebase.firestore
    private val waitingListsRef = db.collection("waitingLists")

    /**
     * Saves a waiting list. Replaces the existing list if the campsite already has one.
     *
     * @param waitingList list to save
     */
    fun addWaitingList(waitingList: WaitingList) {
        waitingListsRef.document(waitingList.campsiteID).set(waitingList)
    }

    /**
     * Deletes a campsite's waiting list.
     *
     * @param campsiteID campsite whose list is deleted
     */
    fun deleteWaitingList(campsiteID: String) {
        waitingListsRef.document(campsiteID).delete()
    }

    /**
     * Gets a campsite's waiting list.
     *
     * @param campsiteID campsite to look up
     * @return the list, or null if the list is missing or the read fails
     */
    suspend fun getWaitingList(campsiteID: String): WaitingList? {
        return try {
            waitingListsRef.document(campsiteID).get().await()
                .toObject(WaitingList::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Adds an entrant to a campsite's waiting list. Creates the list if needed.
     *
     * @param campsiteID campsite to join
     * @param entrantID entrant joining
     * @return false if the list is full, the entrant already joined, or the transaction fails
     */
    suspend fun joinWaitingList(campsiteID: String, entrantID: Int): Boolean {
        val docRef = waitingListsRef.document(campsiteID)
        return try {
            // Check capacity and add the entrant together so simultaneous joins respect the limit.
            db.runTransaction { transaction ->
                val waitingList = transaction.get(docRef).toObject(WaitingList::class.java)
                    ?: WaitingList(campsiteID = campsiteID)
                val joined = waitingList.join(entrantID)
                if (joined) {
                    transaction.set(docRef, waitingList)
                }
                joined
            }.await()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Removes an entrant from a campsite's waiting list.
     *
     * @param campsiteID campsite to leave
     * @param entrantID entrant leaving
     * @return false if the entrant is absent or the transaction fails
     */
    suspend fun leaveWaitingList(campsiteID: String, entrantID: Int): Boolean {
        val docRef = waitingListsRef.document(campsiteID)
        return try {
            db.runTransaction { transaction ->
                val waitingList = transaction.get(docRef).toObject(WaitingList::class.java)
                val left = waitingList?.leave(entrantID) ?: false
                if (left) {
                    transaction.set(docRef, waitingList!!)
                }
                left
            }.await()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Finds every waiting list an entrant has joined.
     *
     * @param entrant user to look up
     * @return the lists, or an empty list if the entrant has joined none or the query fails
     */
    suspend fun viewJoinedWaitingLists(entrant: User): List<WaitingList> {
        return try {
            val querySnapshot = waitingListsRef
                .whereArrayContains("entrantIDs", entrant.userID)
                .get()
                .await()

            querySnapshot?.toObjects(WaitingList::class.java) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
