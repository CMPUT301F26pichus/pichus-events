package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

/**
 * Stores waiting lists in Firestore, one per campsite, keyed by campsite ID.
 * Keeps a local copy of every list so lookups need no suspend functions.
 *
 * Outstanding Issues: N/A
 */
class WaitingListDB {
    private val db = Firebase.firestore
    private val waitingListsRef = db.collection("waitingLists")

    private val _waitingLists = mutableStateListOf<WaitingList>()

    /**
     * Returns local waiting lists
     *
     * @return list of all waiting lists
     */
    val waitingLists: List<WaitingList>
        get() = _waitingLists

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
     * Sets the registration deadline for a campsite's waiting list (US 02.04.03).
     * Creates the list if the campsite has none yet.
     *
     * @param campsiteID campsite whose deadline is set
     * @param deadline last moment entrants can join, null removes the deadline
     */
    fun setRegistrationDeadline(campsiteID: String, deadline: Timestamp?) {
        waitingListsRef.document(campsiteID).set(
            mapOf("campsiteID" to campsiteID, "registrationDeadline" to deadline),
            SetOptions.merge()
        )
    }

    /**
     * Gets a campsite's waiting list from the local copy.
     *
     * @param campsiteID campsite to look up
     * @return the list, or null if the campsite has none yet
     */
    fun getWaitingList(campsiteID: String): WaitingList? {
        return _waitingLists.toList().find { it.campsiteID == campsiteID }
    }

    /**
     * Adds an entrant to a campsite's waiting list. Creates the list if needed.
     * Checks the local copy for the answer, then saves in a transaction that checks
     * again, so simultaneous joins still respect the capacity and deadline.
     *
     * @param campsiteID campsite to join
     * @param entrantID entrant joining
     * @return false if registration is closed, the list is full, or the entrant already joined
     */
    fun joinWaitingList(campsiteID: String, entrantID: Int): Boolean {
        val local = getWaitingList(campsiteID)
        val check = WaitingList(
            campsiteID,
            local?.entrantIDs?.toMutableList() ?: mutableListOf(),
            local?.capacity,
            local?.registrationDeadline
        )
        if (!check.join(entrantID)) {
            return false
        }

        val docRef = waitingListsRef.document(campsiteID)
        db.runTransaction { transaction ->
            val waitingList = transaction.get(docRef).toObject(WaitingList::class.java)
                ?: WaitingList(campsiteID = campsiteID)
            if (waitingList.join(entrantID)) {
                transaction.set(docRef, waitingList)
            }
        }
        return true
    }

    /**
     * Removes an entrant from a campsite's waiting list.
     *
     * @param campsiteID campsite to leave
     * @param entrantID entrant leaving
     * @return false if the entrant is not on the list
     */
    fun leaveWaitingList(campsiteID: String, entrantID: Int): Boolean {
        if (getWaitingList(campsiteID)?.contains(entrantID) != true) {
            return false
        }

        val docRef = waitingListsRef.document(campsiteID)
        db.runTransaction { transaction ->
            val waitingList = transaction.get(docRef).toObject(WaitingList::class.java)
            if (waitingList != null && waitingList.leave(entrantID)) {
                transaction.set(docRef, waitingList)
            }
        }
        return true
    }

    /**
     * Finds every waiting list an entrant has joined, from the local copy.
     *
     * @param entrant user to look up
     * @return the lists, or an empty list if the entrant has joined none
     */
    fun viewJoinedWaitingLists(entrant: User): List<WaitingList> {
        return _waitingLists.toList().filter { it.contains(entrant.userID) }
    }

    /**
     * Pools the entrants of several campsites, for a lottery that covers all of them (US 02.04.02).
     * Someone who joined two of the campsites counts once.
     *
     * @param campsiteIDs campsites in the lottery
     * @return every entrant across those lists, no duplicates, in join order
     */
    fun getEntrantsFor(campsiteIDs: List<String>): List<Int> {
        return campsiteIDs
            .mapNotNull { getWaitingList(it) }
            .flatMap { it.entrantIDs }
            .distinct()
    }

    /**
     * Removes an entrant from every waiting list they joined.
     * Call this when a user profile is deleted.
     * Each list is updated in a transaction, so entrants joining at the same time are not lost.
     *
     * @param entrantID entrant being deleted
     * @return campsite IDs of the lists the entrant was removed from
     */
    fun removeEntrantFromAll(entrantID: Int): List<String> {
        val campsiteIDs = _waitingLists.toList().filter { it.contains(entrantID) }.map { it.campsiteID }
        campsiteIDs.forEach { campsiteID ->
            val docRef = waitingListsRef.document(campsiteID)
            db.runTransaction { transaction ->
                val waitingList = transaction.get(docRef).toObject(WaitingList::class.java)
                if (waitingList != null && waitingList.leave(entrantID)) {
                    transaction.set(docRef, waitingList)
                }
            }
        }
        return campsiteIDs
    }

    /**
     * Updates local _waitingLists list
     */
    init {
        waitingListsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _waitingLists.clear()
            snapshot?.documents?.forEach { document ->
                val waitingList = document.toObject(WaitingList::class.java)
                if (waitingList != null) {
                    _waitingLists.add(waitingList)
                }
            }
        }
    }
}
