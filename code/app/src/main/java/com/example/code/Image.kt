package com.example.code

import com.google.firebase.firestore.Blob

data class Image(
    val id: String = "",
    val imageData: Blob? = null,
    val imageType: String = "",
    val userId: String = "",
    val campsiteId: String = ""
)
