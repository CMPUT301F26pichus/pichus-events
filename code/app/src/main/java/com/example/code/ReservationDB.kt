package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore


/**
 * Stores all reservations in FireBase, and allows retrieval, update and addition functionalities
 *
 * Outstanding Issues: N/A
 */
class ReservationDB {
    private val db = Firebase.firestore
    private val reservationsRef = db.collection("reservations")

    private val _reservations = mutableStateListOf<Reservation>()

    /**
     * Returns local reservations list
     *
     * @return list of all reservations
     */
    val reservations: List<Reservation> // can be used by admin to see all reservations
        get() = _reservations

    /**
     * Adds a reservation to the database
     *
     * @param reservation reservation to be added
     * @return Unit
     */
    fun addReservation(reservation: Reservation) {
        reservationsRef.document(reservation.customerID.toString() + "_" + reservation.campsiteID).set(reservation)
    }

    /**
     * Updates an existing reservation in the database, setting it to 'CANCELLED'
     *
     * @param reservation reservation to be cancelled
     * @return Unit
     */
    fun cancelReservation(reservation: Reservation) {
        val cancelledRes = Reservation(reservation.customerID, reservation.ownerID, reservation.campsiteID, reservation.startDate, reservation.endDate, reservation.guestCount, status = "CANCELLED")
        reservationsRef.document(reservation.customerID.toString() + "_" + reservation.campsiteID).set(cancelledRes)
    }

    /**
     * Cancels all reservations of a given user in the database, setting it to 'CANCELLED'
     *
     * @param user the user whose reservations are to be cancelled
     * @return Unit
     */
    fun cancelAll(user: User) {
        var reservationsToCancel = _reservations.filter { it.customerID == user.userID && it.status == "ACTIVE"}
        for (reservation in reservationsToCancel) {
            cancelReservation(reservation)
        }
        reservationsToCancel =  _reservations.filter { it.ownerID == user.userID && it.status == "ACTIVE"}
        for (reservation in reservationsToCancel) {
            cancelReservation(reservation)
        }
    }

    /**
     * Cancels all reservations of a given campsite, setting it to 'CANCELLED'
     *
     * @param campsite the campsite which reservations are to be cancelled
     * @return Unit
     */
    fun cancelCampsiteReservations(campsite: Campsite) {
        val reservationsToCancel = _reservations.filter { it.campsiteID == campsite.id && it.status == "ACTIVE"}
        for (reservation in reservationsToCancel) {
            cancelReservation(reservation)
        }
    }

    /**
     * Obtains a user's active reservations
     *
     * @param user used to only return a specific user's active reservations
     * @return the list of active reservations (current + upcoming)
     */
    fun viewActiveReservations(user: User): List<Reservation> {
        return if (user.role == "Entrant") {
            // Check that the end date is in the future (has not ended yet)
            _reservations.filter { it.customerID == user.userID && it.status == "ACTIVE" && it.endDate!! >= Timestamp.now() }
        } else {
            _reservations.filter { it.ownerID == user.userID && it.status == "ACTIVE" && it.endDate!! >= Timestamp.now() }
        }
    }

    /**
     * Obtains a user's past reservations
     *
     * @param user used to only return a specific user's past reservations
     * @return the list of past reservations (that were not cancelled)
     */
    fun viewPastReservations(user: User): List<Reservation> {
        return if (user.role == "Entrant") {
            _reservations.filter { it.customerID == user.userID && it.status == "ACTIVE" && it.endDate!! < Timestamp.now() }
        } else {
            _reservations.filter { it.ownerID == user.userID && it.status == "ACTIVE" && it.endDate!! < Timestamp.now() }
        }
    }

    /**
     * Obtains a user's cancelled reservations
     *
     * @param user used to only return a specific user's cancelled reservations
     * @return the list of cancelled reservations
     */
    fun viewCancelledReservations(user: User): List<Reservation> {
        return if (user.role == "Entrant") {
            _reservations.filter { it.customerID == user.userID && it.status == "CANCELLED" }
        } else {
            _reservations.filter { it.ownerID == user.userID && it.status == "CANCELLED" }
        }
    }

    /**
     * Updates the rating for a past reservation
     */
    fun rateReservation(reservation: Reservation, newRating: Int) {
        val docId = reservation.customerID.toString() + "_" + reservation.campsiteID
        reservationsRef.document(docId).update("rating", newRating)
    }

    /**
     * Deletes a reservation's rating and calculates the campsite's new average.
     * The written review is stored separately and is not affected.
     */
    fun deleteRatingAndCalculateAverage(
        reservation: Reservation,
        onSuccess: (Double) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val docId = reservation.customerID.toString() + "_" + reservation.campsiteID

        // First remove only the star rating from this reservation
        reservationsRef.document(docId)
            .update("rating", 0)
            .addOnSuccessListener {

                // Retrieve the campsite's reservations after the rating was removed
                reservationsRef
                    .whereEqualTo("campsiteID", reservation.campsiteID)
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val remainingRatings = snapshot.documents.mapNotNull { document ->
                            document.toObject(Reservation::class.java)?.rating
                        }.filter { it > 0 }

                        // Calculate the new average. If no ratings remain, use 0.0
                        val newAverage = if (remainingRatings.isEmpty()) {
                            0.0
                        } else {
                            Math.round(remainingRatings.average() * 10.0) / 10.0
                        }
                        onSuccess(newAverage)
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Updates local _reservations list
     */
    init {
        reservationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _reservations.clear()
            snapshot?.documents?.forEach { document ->
                val reservation = document.toObject(Reservation::class.java)
                if (reservation != null) {
                    _reservations.add(reservation)
                }
            }
        }
    }
}