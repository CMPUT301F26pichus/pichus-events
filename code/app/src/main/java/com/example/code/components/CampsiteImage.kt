
package com.example.code.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.code.ImageDB

/**
 * Displays an optional campsite image retrieved from Firestore.
 */
@Composable
fun CampsiteImage(
    campsiteId: String,
    imageDB: ImageDB,
    modifier: Modifier = Modifier
) {

    var imageBytes by remember(campsiteId) {
        mutableStateOf<ByteArray?>(null)
    }

    var isLoading by remember(campsiteId) {
        mutableStateOf(true)
    }

    // Each campsite has at most one primary image.
    val imageId = "campsite_$campsiteId"

    // Retrieve the image from Firestore.
    LaunchedEffect(imageId) {

        imageBytes = null
        isLoading = true

        imageDB.getImage(
            id = imageId,

            onSuccess = { image ->
                imageBytes = image?.imageData?.toBytes()
                isLoading = false
            },

            onFailure = {
                imageBytes = null
                isLoading = false
            }
        )
    }

    val bitmap = remember(imageBytes) {
        imageBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )?.asImageBitmap()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {

        when {

            isLoading -> {
                CircularProgressIndicator()
            }

            bitmap != null -> {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Image of campsite",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "No campsite image available",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}
