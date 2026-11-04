package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

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
     * Queries database to only return a user's campsites
     *
     * @param owner used to only return a specific user's campsites
     * @return the list of owned campsites, or an empty list
     */
    suspend fun viewOwnCampsites(owner: User): List<Campsite> {
        return try {
            val querySnapshot = campsitesRef
                .whereEqualTo("ownerID", owner.userID)
                .get()
                .await()

            querySnapshot?.toObjects(Campsite::class.java) ?: emptyList() // converts query snapshot to a list of campsites
        } catch (e: Exception) {
            emptyList() // return an empty list if any issues occur
        }
    }

    /**
     * Queries database to only campsites meeting an occupancy requirement
     *
     * @param occupancy used to find relevant campsites
     * @return the list of relevant campsites, or an empty list
     */
    suspend fun filterByOccupancy(occupancy: Int): List<Campsite> {
        return try {
            val querySnapshot = campsitesRef
                .whereGreaterThanOrEqualTo("occupancy", occupancy)
                .get()
                .await()

            querySnapshot?.toObjects(Campsite::class.java) ?: emptyList() // converts query snapshot to a list of campsites
        } catch (e: Exception) {
            emptyList() // return an empty list if any issues occur
        }
    }

    suspend fun filterByLocation() {} // implement

    suspend fun filterByDates() {} // implement

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