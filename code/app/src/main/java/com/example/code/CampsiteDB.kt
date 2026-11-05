package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Stores all campsites in FireBase, and allows retrieval, update and addition functionalities
 */
class CampsiteDB {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")

    private val _campsites = mutableStateListOf<Campsite>()

    /**
     * Returns local campsites list
     *
     * @return list of all campsites
     */
    val campsites: List<Campsite> // admin can use this to see all campsites
        get() = _campsites

    /**
     * Adds a campsite to the database
     *
     * @param campsite campsite to be added
     * @return Unit
     */
    fun addCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.ownerID.toString() + "_" + campsite.name).set(campsite)
    }

    /**
     * Deletes a campsite from the database
     *
     * @param campsite campsite to be deleted
     * @return Unit
     */
    fun deleteCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.ownerID.toString() + "_" + campsite.name).delete()
    }

    /**
     * Obtains an owner's own campsites
     *
     * @param owner used to only return a specific owner's own campsites
     * @return the list of owned campsites
     */
    fun viewOwnCampsites(owner: User): List<Campsite> {
        return _campsites.filter { it.ownerID == owner.userID }
    }


    /**
     * Obtains campsites meeting an occupancy requirement
     *
     * @param occupancy used to find applicable campsites
     * @return the list of relevant campsites
     */
    fun filterByOccupancy(occupancy: Int): List<Campsite> {
        return _campsites.filter { it.occupancy >= occupancy }
    }

    /**
     * Updates local _campsites list
     */
    init {
        campsitesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _campsites.clear()
            snapshot?.documents?.forEach { document ->
                val campsite = document.toObject(Campsite::class.java)
                if (campsite != null) {
                    _campsites.add(campsite)
                }
            }
        }
    }
}