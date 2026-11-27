package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Stores all campsites in FireBase, and allows retrieval, update and addition functionalities
 *
 * Outstanding Issues: N/A
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
     * Adds a campsite to the database, also allows updates
     *
     * @param campsite campsite to be added
     * @return Unit
     */
    fun addCampsite(
        campsite: Campsite,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        campsitesRef.document(campsite.id)
            .set(campsite)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Deletes a campsite from the database
     *
     * @param campsite campsite to be deleted
     * @return Unit
     */
    fun deleteCampsite(
        campsite: Campsite,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        campsitesRef.document(campsite.id)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Removes all campsites of a given user in the database
     *
     * @param user the user whose campsites are to be removed
     * @return Unit
     */
    fun removeAll(user: User) {
        val campsitesToRemove = _campsites.filter { it.ownerID == user.userID}
        for (campsite in campsitesToRemove) {
            deleteCampsite(campsite)
        }
    }

    /**
     * Updates a campsite's occupancy in the database
     *
     * @param newOccupancy new occupancy
     * @param campsite campsite to be updated
     * @return Unit
     */
    fun updateOccupancy(newOccupancy: String, campsite: Campsite) {
        if (newOccupancy.toIntOrNull() != null && newOccupancy.toInt() > 0) {
            campsite.occupancy = newOccupancy.toInt()
            addCampsite(campsite) // updates the occupancy
        }
    }

    /**
     * Updates a campsite's price in the database
     *
     * @param newPrice new price
     * @param campsite campsite to be updated
     * @return Unit
     */
    fun updatePrice(newPrice: String, campsite: Campsite) {
        if (newPrice.toDoubleOrNull() != null && newPrice.toDouble() > 0.0) {
            campsite.price = newPrice.toDouble()
            addCampsite(campsite) // updates the price
        }
    }

    /**
     * Updates a campsite's policy in the database
     *
     * @param newPolicy new policy
     * @param campsite campsite to be updated
     * @return Unit
     */
    fun updatePolicy(newPolicy: String, campsite: Campsite) {
            campsite.policy = newPolicy
            addCampsite(campsite) // updates the policy
    }

    /**
     * Updates a campsite's description in the database
     *
     * @param newDescription new description
     * @param campsite campsite to be updated
     * @return Unit
     */
    fun updateDescription(newDescription: String, campsite: Campsite) {
        campsite.description = newDescription
        addCampsite(campsite) // updates the description
    }

    /**
     * Updates a campsite's tags in the database
     *
     * @param newTags new tags
     * @param campsite campsite to be updated
     * @return Unit
     */
    fun updateTags(newTags: String, campsite: Campsite) {
        campsite.tags = newTags
        addCampsite(campsite) // updates the tags
    }

    /**
     * Updates a campsite's average rating in the database
     *
     * @param campsiteId ID of the campsite to update
     * @param newRating calculated average rating
     */
    fun updateRating(
        campsiteId: String,
        newRating: Double,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        campsitesRef.document(campsiteId)
            .update("rating", newRating)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
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