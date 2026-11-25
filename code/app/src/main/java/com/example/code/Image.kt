package com.example.code

import com.google.firebase.firestore.Blob

/**
 * Represents all images in the app (promotional images, profile pictures)
 *
 * An image has an id, imageData, imageType, user id and campsite id.
 *
 * Outstanding Issues: N/A
 *
 * @property id ID of the image
 * @property imageData stored version of the image
 * @property imageType purpose of the image
 * @property userId user id of the corresponding image
 * @property campsiteId campsite id of the corresponding image
 */

data class Image(
    val id: String = "",
    val imageData: Blob? = null,
    val imageType: String = "",
    val userId: String = "",
    val campsiteId: String = ""
)
