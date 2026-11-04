package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlin.collections.emptyList


/**
 * Stores all reservations in FireBase, and allows retrieval, update and addition functionalities
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
     * Queries database to only return a user's upcoming reservations
     *
     * @param user used to only return a specific user's upcoming reservations
     * @return the list of upcoming reservations, or an empty list
     */
    suspend fun viewUpcomingReservations(user: User): List<Reservation> {
        if (user.role == "Entrant") {
            return try {
                val querySnapshot = reservationsRef
                    .whereEqualTo("customerID", user.userID)
                    .whereGreaterThan("startDate", Timestamp.now())
                    .whereEqualTo("status", "ACTIVE")
                    .get()
                    .await()

                querySnapshot?.toObjects(Reservation::class.java) ?: emptyList()  // convert query snapshot to a list of reservations
            } catch (e: Exception) {
                emptyList() // return an empty list if any issues occur
            }
        }
        else if (user.role == "Campsite Owner") {
            return try {
                val querySnapshot = reservationsRef
                    .whereEqualTo("ownerID", user.userID)
                    .whereGreaterThan("startDate", Timestamp.now())
                    .whereEqualTo("status", "ACTIVE")
                    .get()
                    .await()

                querySnapshot?.toObjects(Reservation::class.java) ?: emptyList() // convert query snapshot to a list of reservations
            } catch (e: Exception) {
                emptyList() // return an empty list if any issues occur
            }
        }
        return emptyList()
    }

    /**
     * Queries database to only return a user's past reservations
     *
     * @param user used to only return a specific user's past reservations
     * @return the list of past reservations, or an empty list
     */
    suspend fun viewPastReservations(user: User): List<Reservation> {
        if (user.role == "Entrant") {
            return try {
                val querySnapshot = reservationsRef
                    .whereEqualTo("customerID", user.userID)
                    .whereLessThan("endDate", Timestamp.now())
                    .whereEqualTo("status", "ACTIVE")
                    .get()
                    .await()

                querySnapshot?.toObjects(Reservation::class.java) ?: emptyList() // convert query snapshot to a list of reservations
            } catch (e: Exception) {
                emptyList() // return an empty list if any issues occur
            }
        }
        else if (user.role == "Campsite Owner") {
            return try {
                val querySnapshot = reservationsRef
                    .whereEqualTo("ownerID", user.userID)
                    .whereLessThan("endDate", Timestamp.now())
                    .whereEqualTo("status", "ACTIVE")
                    .get()
                    .await()

                querySnapshot?.toObjects(Reservation::class.java) ?: emptyList() // convert query snapshot to a list of reservations
            } catch (e: Exception) {
                emptyList() // return an empty list if any issues occur
            }
        }
        return emptyList()
    }

    /**
     * Queries database to only return a user's cancelled reservations
     *
     * @param user used to only return a specific user's cancelled reservations
     * @return the list of cancelled reservations, or an empty list
     */
   suspend fun viewCancelledReservations(user: User): List<Reservation> {
        if (user.role == "Entrant") {
            return try {
                val querySnapshot = reservationsRef
                    .whereEqualTo("customerID", user.userID)
                    .whereEqualTo("status", "CANCELLED")
                    .get()
                    .await()

                querySnapshot?.toObjects(Reservation::class.java) ?: emptyList() // convert query snapshot to a list of reservations
            } catch (e: Exception) {
                emptyList() // return an empty list if any issues occur
            }
        }
        else if (user.role == "Campsite Owner") {
            return try {
                val querySnapshot = reservationsRef
                    .whereEqualTo("ownerID", user.userID)
                    .whereEqualTo("status", "CANCELLED")
                    .get()
                    .await()

                querySnapshot?.toObjects(Reservation::class.java) ?: emptyList() // convert query snapshot to a list of reservations
            } catch (e: Exception) {
                emptyList() // return an empty list if any issues occur
            }
        }
        return emptyList()
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