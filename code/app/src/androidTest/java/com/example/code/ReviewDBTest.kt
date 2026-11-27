// Instrumented tests for UserDB against Firestore. Run on the emulator.
// Test reviews are deleted from the database.

package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewDBTest {
    private val reviewDB = ReviewDB()
    private val ownerID = (-1000..-1).random()

    private val reviewerID = -1
    private val campsite = Campsite(name = "TestSite_${System.currentTimeMillis()}", ownerID = ownerID)

    private fun findTestReview(): Review? = reviewDB.reviews.find { it.campsiteID == campsite.id }

    /** Adds a valid test review and waits until it shows up. */
    private fun addTestReview(): Review {
        reviewDB.addReview(Review(campsite.id, reviewerID, "Unit_test_review"))
        waitUntil { findTestReview() != null }
        val review = findTestReview()
        assertNotNull("test review never showed up", review)
        return review!!
    }

    @After
    fun cleanUp() {
        val docID = reviewerID.toString() + "_" + campsite.id
        Tasks.await(Firebase.firestore.collection("reviews").document(docID).delete())

    }

    @Test
    fun addReviewShowsInReviews() {
        val review = addTestReview()
        assertEquals(campsite.id, review.campsiteID)
        assertEquals(reviewerID, review.reviewerID)
        assertEquals("Unit_test_review", review.content)
    }

    @Test
    fun deleteReviewRemovesReview() {
        val review = addTestReview()
        reviewDB.deleteReview(review)
        waitUntil { findTestReview() == null }
        assertTrue(findTestReview() == null)
    }

    @Test
    fun getCampsiteReviewsShowsReviews() {
        val review = addTestReview()
        val result = reviewDB.getCampsiteReviews(campsite)
        Thread.sleep(2000)
        assertEquals(1, result.size)
    }

    @Test
    fun getCampsiteReviewsShowsNoReviews() {
        val result = reviewDB.getCampsiteReviews(campsite)
        Thread.sleep(2000)
        assertEquals(0, result.size)
    }

}
