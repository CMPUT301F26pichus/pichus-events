package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Stores all users (admin, entrants, campsite owner) in FireBase, and allows retrieval, update,
 * addition, and deletion functionalities
 *
 * Outstanding Issues: N/A
 */
class ReviewDB {
    private val db = Firebase.firestore
    private val reviewsRef = db.collection("reviews")

    private val _reviews = mutableStateListOf<Review>()

    /**
     * Returns local reviews list
     *
     * @return list of all reviews
     */
    val reviews: List<Review>
        get() = _reviews

    /**
     * Adds a review to the database
     *
     *
     * @param review review to be added
     * @return Unit
     */
    fun addReview(review: Review) {
        reviewsRef.document(review.id).set(review)
    }

    /**
     * Deletes a review from the database
     *
     * @param review review to be deleted
     * @return Unit
     */
    fun deleteReview(review: Review) {
        reviewsRef.document(review.id).delete()
    }

    /**
     * Returns all reviews for a given campsite from the database
     *
     * @param campsite campsite for which reviews are being queried
     * @return a list of reviews
     */
    fun getCampsiteReviews(campsite: Campsite): List<Review> {
        return _reviews.filter { it.campsiteID == campsite.id }
    }

    /**
     * Updates local _reviews list
     */
    init {
        reviewsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _reviews.clear()
            snapshot?.documents?.forEach { document ->
                val review = document.toObject(Review::class.java)
                if (review != null) {
                    _reviews.add(review)
                }
            }
        }
    }
}