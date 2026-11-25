package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.Blob
/**
 * Stores all images in FireBase, and allows retrieval, update and addition functionalities
 *
 * Outstanding Issues: N/A
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
    fun addImage(
        image: Image,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        imagesRef.document(image.id)
            .set(image)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Updates an existing image in Firestore.
     *
     * Uses the same document ID, so the existing image is replaced.
     */
    fun updateImage(
        image: Image,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        addImage(image, onSuccess, onFailure)
    }

    /**
     * Converts a byte array into a Firestore Blob.
     */
    fun createBlob(imageBytes: ByteArray): Blob {
        return Blob.fromBytes(imageBytes)
    }

    /**
     * Deletes an image from the database
     *
     * @param id image to be deleted
     * @return Unit
     */
    fun deleteImage(
        id: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        imagesRef.document(id)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Deletes all images associated with a campsite.
     */
    fun deleteCampsiteImages(
        campsiteId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        imagesRef
            .whereEqualTo("campsiteId", campsiteId)
            .get()
            .addOnSuccessListener { documents ->

                val batch = db.batch()

                for (document in documents.documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Obtains a user's profile picture or a campsite image from the database
     *
     * @param id image to be accessed
     * @return Unit
     */
    fun getImage(
        id: String,
        onSuccess: (Image?) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        imagesRef.document(id)
            .get()
            .addOnSuccessListener { document ->
                val image = document.toObject(Image::class.java)
                onSuccess(image)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
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