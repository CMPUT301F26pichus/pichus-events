// Instrumented tests for CampsiteDB against Firestore. Run on the emulator.
// Uses a fake owner ID and deletes every campsite it adds.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampsiteDBTest {
    private val campsiteDB = CampsiteDB()
    private val ownerID = fakeID()
    private val owner = User(name = "Test Owner", userID = ownerID, role = "Campsite Owner")
    private val campsite = Campsite(name = "TestSite_${System.currentTimeMillis()}",
        occupancy = 50, price = 25.99, policy = "This is a policy.", description = "This is a description.",
        ownerID = ownerID, tags = "#test", rating = 0.0)

    private fun isInOwnCampsites() = campsiteDB.viewOwnCampsites(owner).any { it.name == campsite.name }

    private fun findTestCampsite(): Campsite? = campsiteDB.campsites.find { it.ownerID == ownerID }

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
//
//    @Test
//    fun updateOccupancyUpdatesProperly() {
//        campsiteDB.addCampsite(campsite)
//        waitUntil { isInOwnCampsites() }
//        campsiteDB.updateOccupancy("40", campsite)
//        assertEquals(40, findTestCampsite()?.occupancy)
//    }
//
//    @Test
//    fun updateOccupancyRejectsInvalidValue() {
//        campsiteDB.addCampsite(campsite)
//        waitUntil { isInOwnCampsites() }
//        campsiteDB.updateOccupancy("Forty", campsite)
//        assertEquals(50, findTestCampsite()?.occupancy)
//    }
//
//    @Test
//    fun updatePriceUpdatesProperly() {
//        campsiteDB.addCampsite(campsite)
//        waitUntil { isInOwnCampsites() }
//        campsiteDB.updatePrice("40.00", campsite)
//        assertEquals(40.00, findTestCampsite()?.price)
//    }
//
//    @Test
//    fun updatePolicyUpdatesProperly() {
//        campsiteDB.addCampsite(campsite)
//        waitUntil { isInOwnCampsites() }
//        campsiteDB.updatePolicy("This is a revised policy.", campsite)
//        assertEquals("This is a revised policy.", findTestCampsite()?.policy)
//    }
//
//    @Test
//    fun updateDescriptionUpdatesProperly() {
//        campsiteDB.addCampsite(campsite)
//        waitUntil { isInOwnCampsites() }
//        campsiteDB.updateDescription("This is a revised description.", campsite)
//        assertEquals("This is a revised description.", findTestCampsite()?.description)
//    }
//
//    @Test
//    fun updateTagsUpdatesProperly() {
//        campsiteDB.addCampsite(campsite)
//        waitUntil { isInOwnCampsites() }
////        campsiteDB.updateTags("#update", campsite)
//        assertEquals("#update", findTestCampsite()?.tags)
//    }
//
    @Test
    fun updateRatingsUpdatesProperly() {
        campsiteDB.addCampsite(campsite)
        waitUntil { isInOwnCampsites() }
        campsiteDB.updateRating(campsite.id, 4.5)
        assertEquals(4.5, findTestCampsite()?.rating)
    }
}
