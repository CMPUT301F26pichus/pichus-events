// Instrumented tests for CascadeDelete against Firestore. Run on the emulator.
// Uses fake owner and entrant IDs, so real data is never touched.
// Cleanup waits for every delete, so nothing is left behind after the last test.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CascadeDeleteTest {
    private val userDB = UserDB()
    private val campsiteDB = CampsiteDB()
    private val reservationDB = ReservationDB()
    private val waitingListDB = WaitingListDB()
    private val lotteryDB = LotteryDB()
    private val availabilityDB = AvailabilityDB()
    private val imageDB = ImageDB()
    private val cascadeDelete = CascadeDelete(
        userDB, campsiteDB, reservationDB, waitingListDB, lotteryDB, availabilityDB, imageDB
    )

    private val owner = User(name = "Test Owner", userID = fakeID(), role = "Campsite Owner")
    private val entrant = User(name = "Test Entrant", userID = fakeID(), role = "Entrant")
    private val campsite = Campsite(name = "TestSite_${System.currentTimeMillis()}", occupancy = 10, ownerID = owner.userID)
    private val lotteryID = "test_${owner.userID}_lottery"
    private val reservationDocID = "${entrant.userID}_${campsite.id}"

    private val firestore = Firebase.firestore

    private fun campsiteExists() = campsiteDB.campsites.any { it.id == campsite.id }
    private fun reservation() = reservationDB.reservations.find { it.customerID == entrant.userID && it.campsiteID == campsite.id }
    private fun lotteryExists() = lotteryDB.viewOwnLotteries(owner).any { it.lotteryID == lotteryID }
    private fun availabilityExists() = availabilityDB.availabilities.any { it.campsiteID == campsite.id }
    private fun userExists(user: User) = userDB.users.any { it.userID == user.userID }

    /**
     * The owner has one campsite. That campsite has a lottery, a waiting list with the entrant on it,
     * an active reservation by the entrant, and availability. Waits until every DB sees it.
     */
    @Before
    fun setUp() {
        Tasks.await(firestore.collection("users").document(owner.userID.toString()).set(owner))
        Tasks.await(firestore.collection("users").document(entrant.userID.toString()).set(entrant))
        campsiteDB.addCampsite(campsite)
        lotteryDB.addLottery(Lottery(lotteryID = lotteryID, ownerID = owner.userID, campsiteIDs = listOf(campsite.id)))
        waitingListDB.addWaitingList(WaitingList(campsiteID = campsite.id, entrantIDs = mutableListOf(entrant.userID)))
        reservationDB.addReservation(
            Reservation(
                customerID = entrant.userID, ownerID = owner.userID, campsiteID = campsite.id,
                startDate = Timestamp.now(), endDate = Timestamp.now(), guestCount = 2
            )
        )
        availabilityDB.addAvailability(Availability(campsite.id, mutableListOf(Timestamp.now())))

        waitUntil(10000) {
            userExists(owner) && userExists(entrant) && campsiteExists() && lotteryExists() &&
                waitingListDB.getWaitingList(campsite.id) != null &&
                reservation() != null && availabilityExists()
        }
        assertTrue("test data never showed up", campsiteExists() && lotteryExists() && reservation() != null)
    }

    @After
    fun cleanUp() {
        listOf(
            firestore.collection("users").document(owner.userID.toString()),
            firestore.collection("users").document(entrant.userID.toString()),
            firestore.collection("campsites").document(campsite.id),
            firestore.collection("lotteries").document(lotteryID),
            firestore.collection("waitingLists").document(campsite.id),
            firestore.collection("reservations").document(reservationDocID),
            firestore.collection("availabilities").document(campsite.id)
        ).forEach { Tasks.await(it.delete()) }
    }

    @Test
    fun deleteCampsiteCleansUpEverythingTiedToIt() {
        var done = false
        cascadeDelete.deleteCampsite(campsite, onSuccess = { done = true })
        waitUntil(10000) { done && !campsiteExists() && !lotteryExists() && !availabilityExists() }

        assertTrue(done)
        assertFalse(campsiteExists())
        assertFalse(lotteryExists())
        assertNull(waitingListDB.getWaitingList(campsite.id))
        assertFalse(availabilityExists())
        assertEquals("CANCELLED", reservation()?.status)
    }

    @Test
    fun deleteOwnerRemovesTheirCampsites() {
        cascadeDelete.deleteUser(owner)
        waitUntil(10000) { !userExists(owner) && !campsiteExists() && !lotteryExists() }

        assertFalse(userExists(owner))
        assertFalse(campsiteExists())
        assertFalse(lotteryExists())
        assertNull(waitingListDB.getWaitingList(campsite.id))
        assertFalse(availabilityExists())
        assertEquals("CANCELLED", reservation()?.status)
    }

    @Test
    fun deleteEntrantLeavesWaitingListsAndCancelsReservations() {
        cascadeDelete.deleteUser(entrant)
        waitUntil(10000) {
            !userExists(entrant) && waitingListDB.getWaitingList(campsite.id)?.contains(entrant.userID) == false
        }

        assertFalse(userExists(entrant))
        assertFalse(waitingListDB.getWaitingList(campsite.id)!!.contains(entrant.userID))
        assertEquals("CANCELLED", reservation()?.status)
        // the entrant's delete must not touch the owner's campsite
        assertTrue(campsiteExists())
    }
}
