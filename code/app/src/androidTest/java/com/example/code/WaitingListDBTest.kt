// Instrumented tests for WaitingListDB against Firestore. Run on the emulator.
// Each test uses fake campsite and entrant IDs and deletes the lists it made.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaitingListDBTest {
    private val waitingListDB = WaitingListDB()
    private val entrant = fakeID()
    private val other = fakeID()
    private val first = "test_${fakeID()}"
    private val second = "test_${fakeID()}"
    private val third = "test_${fakeID()}"

    private fun local(campsiteID: String): WaitingList? = waitingListDB.getWaitingList(campsiteID)

    /** Saves a list with the given entrants and waits until it shows up. */
    private fun addList(campsiteID: String, vararg entrantIDs: Int) {
        waitingListDB.addWaitingList(WaitingList(campsiteID, entrantIDs.toMutableList()))
        waitUntil { local(campsiteID)?.entrantIDs?.size == entrantIDs.size }
    }

    @After
    fun cleanUp() {
        listOf(first, second, third).forEach { waitingListDB.deleteWaitingList(it) }
    }

    @Test
    fun removeEntrantFromAllTakesThemOffEveryList() {
        addList(first, entrant, other)
        addList(second, entrant)
        val removedFrom = waitingListDB.removeEntrantFromAll(entrant)
        assertEquals(setOf(first, second), removedFrom.toSet())
        // Two transactions, the first run after a cold start can be slow
        waitUntil(10_000) { local(first)?.contains(entrant) == false && local(second)?.contains(entrant) == false }
        assertFalse(local(first)!!.contains(entrant))
        assertFalse(local(second)!!.contains(entrant))
    }

    @Test
    fun removeEntrantFromAllKeepsOtherEntrants() {
        addList(first, entrant, other)
        waitingListDB.removeEntrantFromAll(entrant)
        waitUntil { local(first)?.contains(entrant) == false }
        assertTrue(local(first)!!.contains(other))
        assertEquals(1, local(first)!!.entrantIDs.size)
    }

    @Test
    fun removeEntrantFromAllLeavesListsTheyNeverJoined() {
        addList(third, other)
        assertTrue(waitingListDB.removeEntrantFromAll(entrant).none { it == third })
        Thread.sleep(1000)
        assertEquals(listOf(other), local(third)!!.entrantIDs)
    }

    @Test
    fun removeEntrantFromAllWithNoListsDoesNothing() {
        assertTrue(waitingListDB.removeEntrantFromAll(fakeID()).isEmpty())
    }

    @Test
    fun getEntrantsForPoolsListsWithoutDuplicates() {
        addList(first, entrant, other)
        addList(second, other)
        assertEquals(listOf(entrant, other), waitingListDB.getEntrantsFor(listOf(first, second)))
    }

    @Test
    fun getEntrantsForSkipsCampsitesWithNoList() {
        addList(first, entrant)
        assertEquals(listOf(entrant), waitingListDB.getEntrantsFor(listOf(first, "test_none_${fakeID()}")))
    }
}
