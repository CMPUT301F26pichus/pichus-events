package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore

/**
 * Stores all campsite availabilities in FireBase, and allows retrieval, update and addition functionalities
 *
 * Outstanding Issues: N/A
 */
class AvailabilityDB {
    private val db = Firebase.firestore
    private val availabilitiesRef = db.collection("availabilities")

    private val _availabilities = mutableStateListOf<Availability>()

    /**
     * Returns local availabilities list
     *
     * @return list of all availabilities
     */
    val availabilities: List<Availability>
        get() = _availabilities

    /**
     * Adds a campsite's available dates to the database
     *
     * @param availability list of dates to be added
     * @return Unit
     */
    fun addAvailability(availability: Availability) {
        availabilitiesRef.document(availability.campsiteID).set(availability)
    }

    /**
     * Deletes a campsite's availability from the database
     *
     * @param campsiteID id of campsite for which availability is to be deleted
     * @return Unit
     */
    fun deleteAvailability(campsiteID: String) {
        availabilitiesRef.document(campsiteID).delete()
    }

    /**
     * Deletes a date from the campsite's availability in the database
     *
     * @param timestamp date for which availability is to be deleted
     * @return Unit
     */
    fun removeDate(campsite: Campsite, timestamp: Timestamp) {
        val availabilityToModify = availabilities.filter { it.campsiteID == campsite.id }
        for (availability in availabilityToModify) {
            availability.dates?.remove(timestamp)
            addAvailability(availability)
        }
    }

    /**
     * Adds a date to the campsite's availability in the database
     *
     * @param timestamp date to be added
     * @return Unit
     */
    fun addDate(campsite: Campsite, timestamp: Timestamp) {
        val availabilityToModify = availabilities.filter { it.campsiteID == campsite.id }
        for (availability in availabilityToModify) {
            availability.dates?.add(timestamp)
            addAvailability(availability)
        }
    }

    /**
     * Obtains a list of available dates for a given campsite
     *
     * @param campsite used to only return a specific campsites availability
     * @return list of dates
     */
    fun viewCampsiteAvailability(campsite: Campsite): List<Availability> {
        return _availabilities.filter { it.campsiteID == campsite.id }
    }

    /**
     * Updates local _availabilities list
     */
    init {
        availabilitiesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _availabilities.clear()
            snapshot?.documents?.forEach { document ->
                val availability = document.toObject(Availability::class.java)
                if (availability != null) {
                    _availabilities.add(availability)
                }
            }
        }
    }
}