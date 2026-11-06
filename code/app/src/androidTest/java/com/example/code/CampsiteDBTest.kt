// Instrumented tests for CampsiteDB against Firestore. Run on the emulator.
// Uses a fake owner ID and deletes every campsite it adds.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampsiteDBTest {
    private val campsiteDB = CampsiteDB()
    private val ownerID = fakeID()
    private val owner = User(name = "Test Owner", userID = ownerID, role = "Campsite Owner")
    private val campsite = Campsite(name = "TestSite_${System.currentTimeMillis()}", occupancy = 50, ownerID = ownerID)

    private fun isInOwnCampsites() = campsiteDB.viewOwnCampsites(owner).any { it.name == campsite.name }

    @After
    fun cleanUp() {
        campsiteDB.deleteCampsite(campsite)
    }

    @Test
    fun addCampsiteShowsInOwnCampsites() {
        campsiteDB.addCampsite(campsite)
        waitUntil { isInOwnCampsites() }
        assertTrue(isInOwnCampsites())
    }

    @Test
    fun otherOwnerDoesNotSeeCampsite() {
        campsiteDB.addCampsite(campsite)
        waitUntil { isInOwnCampsites() }
        val otherOwner = User(userID = fakeID(), role = "Campsite Owner")
        assertFalse(campsiteDB.viewOwnCampsites(otherOwner).any { it.name == campsite.name })
    }

    @Test
    fun deleteCampsiteRemovesIt() {
        campsiteDB.addCampsite(campsite)
        waitUntil { isInOwnCampsites() }
        campsiteDB.deleteCampsite(campsite)
        waitUntil { !isInOwnCampsites() }
        assertFalse(isInOwnCampsites())
    }

    @Test
    fun filterByOccupancyKeepsBigEnoughSites() {
        campsiteDB.addCampsite(campsite)
        waitUntil { isInOwnCampsites() }
        assertTrue(campsiteDB.filterByOccupancy(40).any { it.name == campsite.name })
        assertTrue(campsiteDB.filterByOccupancy(50).any { it.name == campsite.name })
        assertFalse(campsiteDB.filterByOccupancy(60).any { it.name == campsite.name })
    }
}
