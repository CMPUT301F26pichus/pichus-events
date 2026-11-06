// Instrumented tests for ReservationDB against Firestore. Run on the emulator.
// Uses fake customer and owner IDs. ReservationDB has no delete, so cleanup
// removes the test document straight from Firestore.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ReservationDBTest {
    private val reservationDB = ReservationDB()
    private val entrant = User(name = "Test Entrant", userID = fakeID(), role = "Entrant")
    private val owner = User(name = "Test Owner", userID = fakeID(), role = "Campsite Owner")
    private val campsiteID = "TestSite_${System.currentTimeMillis()}"
    private val day = 24L * 60 * 60 * 1000

    private fun daysFromNow(days: Int) = Timestamp(Date(System.currentTimeMillis() + days * day))

    private fun makeReservation(startDays: Int, endDays: Int) = Reservation(
        customerID = entrant.userID, ownerID = owner.userID, campsiteID = campsiteID,
        startDate = daysFromNow(startDays), endDate = daysFromNow(endDays), guestCount = 2
    )

    private fun isTest(r: Reservation) = r.campsiteID == campsiteID

    @After
    fun cleanUp() {
        val docID = entrant.userID.toString() + "_" + campsiteID
        Tasks.await(Firebase.firestore.collection("reservations").document(docID).delete())
    }

    @Test
    fun futureReservationIsActiveForEntrantAndOwner() {
        reservationDB.addReservation(makeReservation(3, 7))
        waitUntil { reservationDB.viewActiveReservations(entrant).any(::isTest) }
        assertTrue(reservationDB.viewActiveReservations(entrant).any(::isTest))
        assertTrue(reservationDB.viewActiveReservations(owner).any(::isTest))
        assertFalse(reservationDB.viewPastReservations(entrant).any(::isTest))
    }

    @Test
    fun endedReservationIsPast() {
        reservationDB.addReservation(makeReservation(-7, -3))
        waitUntil { reservationDB.viewPastReservations(entrant).any(::isTest) }
        assertTrue(reservationDB.viewPastReservations(entrant).any(::isTest))
        assertTrue(reservationDB.viewPastReservations(owner).any(::isTest))
        assertFalse(reservationDB.viewActiveReservations(entrant).any(::isTest))
    }

    @Test
    fun cancelReservationMovesItToCancelled() {
        val reservation = makeReservation(3, 7)
        reservationDB.addReservation(reservation)
        waitUntil { reservationDB.viewActiveReservations(entrant).any(::isTest) }
        reservationDB.cancelReservation(reservation)
        waitUntil { reservationDB.viewCancelledReservations(entrant).any(::isTest) }
        assertTrue(reservationDB.viewCancelledReservations(entrant).any(::isTest))
        assertTrue(reservationDB.viewCancelledReservations(owner).any(::isTest))
        assertFalse(reservationDB.viewActiveReservations(entrant).any(::isTest))
    }

    @Test
    fun rateReservationSavesRating() {
        val reservation = makeReservation(-7, -3)
        reservationDB.addReservation(reservation)
        waitUntil { reservationDB.viewPastReservations(entrant).any(::isTest) }
        reservationDB.rateReservation(reservation, 4)
        waitUntil { reservationDB.reservations.any { isTest(it) && it.rating == 4 } }
        assertEquals(4, reservationDB.reservations.first(::isTest).rating)
    }
}
