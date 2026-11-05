package com.example.code

import com.google.firebase.firestore.Exclude

/**
 * The waiting list for one campsite.
 * Entrant IDs are stored in join order. A null capacity means no limit.
 *
 * @property campsiteID ID of the campsite this list belongs to (same as Campsite.id)
 * @property entrantIDs entrants on the list, in join order
 * @property capacity max entrants allowed, null means no limit
 */
class WaitingList(
    var campsiteID: String = "",
    var entrantIDs: MutableList<Int> = mutableListOf(),
    var capacity: Int? = null
) {
    /**
     * Adds an entrant to the list.
     *
     * @param entrantID entrant joining
     * @return false if the list is full or the entrant has already joined
     */
    fun join(entrantID: Int): Boolean {
        if (isFull() || contains(entrantID)) {
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
