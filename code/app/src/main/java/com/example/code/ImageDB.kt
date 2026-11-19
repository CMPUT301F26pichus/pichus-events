package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Stores all images in FireBase, and allows retrieval, update and addition functionalities
 *
 */

/* To implement the front-end of user picking images to upload, refer to:
    https://www.youtube.com/watch?v=mIQLQrXS_go&t=186s
 */

/* To implement the front-end of user viewing images, refer to:
    https://www.youtube.com/watch?v=5rMtMfoM5AI
 */

class ImageDB {
    private val db = Firebase.firestore
    private val imagesRef = db.collection("images")

    private val _images = mutableStateListOf<Image>()

    /**
     * Returns local images list
     *
     * @return list of all images
     */
    val images: List<Image> // admin can use this to see all campsites
        get() = _images

    /**
     * Adds an image to the database
     *
     * @param image image to be added
     *
     * @return Unit
     */
    fun addImage(image: Image) {
        imagesRef.document(image.id).set(image)
    }

    /**
     * Deletes an image from the database
     *
     * @param id image to be deleted
     * @return Unit
     */
    fun deleteImage(id: String) {
        imagesRef.document(id).delete()
    }

    /**
     * Obtains a user's profile picture or a campsite image from the database
     *
     * @param id image to be accessed
     * @return Unit
     */
    fun getImage(id: String): List<Image> {
        return _images.filter {it.id == id}
    }

    /**
     * Updates local _images list
     */
    init {
        imagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _images.clear()
            snapshot?.documents?.forEach { document ->
                val image = document.toObject(Image::class.java)
                if (image != null) {
                    _images.add(image)
                }
            }
        }
    }
}