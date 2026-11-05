package com.example.code

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Covers joining (US 01.02.01) and leaving (US 01.02.02) a waiting list.
class WaitingListTest {

    @Test
    fun joinAddsEntrant() {
        val waitingList = WaitingList(campsiteID = "1_Lakeview")
        assertTrue(waitingList.join(5))
        assertTrue(waitingList.contains(5))
        assertEquals(1, waitingList.entrantIDs.size)
    }

    @Test
    fun joinReturnsFalseWhenFull() {
        val waitingList = WaitingList(campsiteID = "1_Lakeview", capacity = 2)
        waitingList.join(1)
        waitingList.join(2)
        assertTrue(waitingList.isFull())
        assertFalse(waitingList.join(3))
        assertFalse(waitingList.contains(3))
    }

    @Test
    fun joinTwiceDoesNotAddTwice() {
        val waitingList = WaitingList(campsiteID = "1_Lakeview")
        waitingList.join(5)
        assertFalse(waitingList.join(5))
        assertEquals(1, waitingList.entrantIDs.size)
    }

    @Test
    fun leaveRemovesEntrant() {
        val waitingList = WaitingList(campsiteID = "1_Lakeview")
        waitingList.join(5)
        assertTrue(waitingList.leave(5))
        assertFalse(waitingList.contains(5))
    }

    @Test
    fun leaveReturnsFalseWhenNotOnList() {
        val waitingList = WaitingList(campsiteID = "1_Lakeview")
        assertFalse(waitingList.leave(5))
    }

    @Test
    fun noCapacityMeansNeverFull() {
        val waitingList = WaitingList(campsiteID = "1_Lakeview")
        for (id in 1..100) {
            waitingList.join(id)
        }
        assertFalse(waitingList.isFull())
    }
}