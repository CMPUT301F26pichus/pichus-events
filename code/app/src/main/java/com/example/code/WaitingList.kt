package com.example.code

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

/**
 * The waiting list for one campsite.
 * Entrant IDs are stored in join order. A null capacity means no limit.
 *
 * @property campsiteID ID of the campsite this list belongs to (same as Campsite.id)
 * @property entrantIDs entrants on the list, in join order
 * @property capacity max entrants allowed, null means no limit
 * @property registrationDeadline last moment entrants can join, set by the owner (US 02.04.03), null means no deadline
 */
class WaitingList(
    var campsiteID: String = "",
    var entrantIDs: MutableList<Int> = mutableListOf(),
    var capacity: Int? = null,
    var registrationDeadline: Timestamp? = null
) {
    /**
     * Adds an entrant to the list.
     *
     * @param entrantID entrant joining
     * @param now current time, defaults to now (passed in by tests)
     * @return false if registration is closed, the list is full, or the entrant has already joined
     */
    fun join(entrantID: Int, now: Timestamp = Timestamp.now()): Boolean {
        if (!isRegistrationOpen(now) || isFull() || contains(entrantID)) {
            return false
        }
        entrantIDs.add(entrantID)
        return true
    }

    /**
     * Removes an entrant from the list.
     *
     * @param entrantID entrant leaving
     * @return false if the entrant was not on the list
     */
    fun leave(entrantID: Int): Boolean {
        return entrantIDs.remove(entrantID)
    }

    /**
     * Checks if entrants can still join.
     *
     * @param now current time, defaults to now
     * @return true if there is no deadline or it has not passed
     */
    fun isRegistrationOpen(now: Timestamp = Timestamp.now()): Boolean {
        val deadline = registrationDeadline ?: return true
        return now <= deadline
    }

    /**
     * Checks if the list has hit its capacity.
     * Excluded from Firestore so it is not saved as a "full" field.
     *
     * @return true if a capacity is set and reached
     */
    @Exclude
    fun isFull(): Boolean {
        val max = capacity ?: return false
        return entrantIDs.size >= max
    }

    /**
     * Checks if an entrant is on the list.
     *
     * @param entrantID entrant to check
     * @return true if the entrant has joined
     */
    fun contains(entrantID: Int): Boolean {
        return entrantID in entrantIDs
    }
}
