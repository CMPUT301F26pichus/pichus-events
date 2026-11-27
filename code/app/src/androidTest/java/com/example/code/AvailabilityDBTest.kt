// Instrumented tests for AvailabilityDB against Firestore. Run on the emulator.
// Uses a fake owner id and campsite to make an availability and deletes every availability it adds.

package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AvailabilityDBTest {
    private val availabilityDB = AvailabilityDB()
    private val ownerID = fakeID()
    private val campsite = Campsite(name = "TestSite_${System.currentTimeMillis()}", ownerID = ownerID)
    private val day = 24L * 60 * 60 * 1000

    private val availability = Availability(campsite.id,
        mutableListOf(Timestamp(Date(System.currentTimeMillis() + 1 * day)),
                              Timestamp(Date(System.currentTimeMillis() + 2 * day)))
    )

    private fun findTestAvailability(): Availability? = availabilityDB.availabilities.find { it.campsiteID == campsite.id }

    @After
    fun cleanUp() {
        availabilityDB.deleteAvailability(campsite.id)
    }
    private fun isInOwnAvailabilities() = availabilityDB.viewCampsiteAvailability(campsite).any { it.campsiteID == campsite.id }

    @Test
    fun addAvailabilityAddsAvailability() {
        availabilityDB.addAvailability(availability)
        waitUntil { isInOwnAvailabilities() }
        assertTrue(isInOwnAvailabilities())
    }

    @Test
    fun deleteAvailabilityRemovesIt() {
        availabilityDB.addAvailability(availability)
        waitUntil { isInOwnAvailabilities() }
        availabilityDB.deleteAvailability(campsite.id)
        waitUntil { !isInOwnAvailabilities() }
        assertFalse(isInOwnAvailabilities())
    }

    @Test
    fun addDateAddsIt() {
        availabilityDB.addAvailability(availability)
        waitUntil { isInOwnAvailabilities() }
        val testDate = Timestamp(Date(System.currentTimeMillis() + 3 * day))
        availabilityDB.addDate(campsite,testDate)
        assertEquals(true, findTestAvailability()?.dates?.contains(testDate))
    }

    @Test
    fun removeDateAddsIt() {
        availabilityDB.addAvailability(availability)
        waitUntil { isInOwnAvailabilities() }
        val dateToRemove = Timestamp(Date(System.currentTimeMillis() + 2 * day))
        availabilityDB.removeDate(campsite,dateToRemove)
        assertEquals(false, findTestAvailability()?.dates?.contains(dateToRemove))
    }

}
