package com.example.code

/**
 * Represents all reviews of a campsite
 *
 * A review has a campsiteID, reviewerID and content.
 *
 * Outstanding Issues: N/A
 *
 * @property campsiteID ID of the associated campsite
 * @property reviewerID ID of the user posting the review
 * @property content content of the review
 */
class Review(val campsiteID: String = "",
             val reviewerID: Int = -1,
             val content: String = "") {
    val id = reviewerID.toString() + "_" + campsiteID
}