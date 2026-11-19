package com.example.code.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

/**
 * Displays the user's profile picture placeholder.
 */
@Composable
fun ProfilePicture(
    userId: Int,
    onImageUploaded: (String) -> Unit
) {
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Opens the phone's image gallery
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isUploading = true
            errorMessage = null

            // Store each user's profile picture in their own Storage location
            val imageRef = Firebase.storage.reference
                .child("profile_pictures/$userId.jpg")

            imageRef.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception
                            ?: Exception("Image upload failed.")
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    onImageUploaded(downloadUri.toString())
                    isUploading = false
                }
                .addOnFailureListener { exception ->
                    errorMessage = exception.message
                        ?: "Could not upload profile picture."
                    isUploading = false
                }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isUploading) {
                    imagePicker.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator()
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Select profile picture",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = if (isUploading) {
                "Uploading..."
            } else {
                "Tap to upload profile picture"
            },
            style = MaterialTheme.typography.bodySmall
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}