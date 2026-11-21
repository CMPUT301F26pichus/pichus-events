
package com.example.code.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.code.Image
import com.example.code.ImageDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * Allows a user to upload, display, update and remove
 * their profile picture.
 */
@Composable
fun ProfilePicture(
    userId: Int,
    profilePictureId: String,
    imageDB: ImageDB,
    onImageUploaded: (String) -> Unit,
    onImageRemoved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val profileBitmap = remember(imageBytes) {
        imageBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }

    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOptions by remember { mutableStateOf(false) }

    // Retrieve the existing profile picture from Firestore.
    LaunchedEffect(profilePictureId) {
        if (profilePictureId.isNotEmpty()) {
            imageDB.getImage(
                id = profilePictureId,
                onSuccess = { image ->
                    val retrievedBytes = image?.imageData?.toBytes()

                    if (retrievedBytes != null) {
                        imageBytes = retrievedBytes
                        errorMessage = null
                    }
                },
                onFailure = { exception ->
                    errorMessage = exception.message
                }
            )
        } else {
            imageBytes = null
        }
    }

    // Opens the device's image gallery.
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        if (uri != null) {

            isUploading = true
            errorMessage = null

            scope.launch {

                try {

                    // Compress image in the background.
                    val compressedBytes = withContext(Dispatchers.IO) {
                        compressImage(context, uri)
                    }

                    // Use a predictable ID so each user has one profile picture.
                    val imageId = "profile_$userId"

                    val image = Image(
                        id = imageId,
                        imageData = imageDB.createBlob(compressedBytes),
                        imageType = "PROFILE",
                        userId = userId.toString(),
                        campsiteId = ""
                    )

                    // Replace existing image or create a new one.
                    imageDB.addImage(
                        image = image,

                        onSuccess = {
                            imageBytes = compressedBytes
                            onImageUploaded(imageId)
                            isUploading = false
                        },

                        onFailure = { exception ->
                            errorMessage = exception.message
                                ?: "Image upload failed."

                            isUploading = false
                        }
                    )

                } catch (exception: Exception) {

                    errorMessage = exception.message
                        ?: "Could not process image."

                    isUploading = false
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Profile picture display.
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isUploading) {
                    showOptions = true
                },
            contentAlignment = Alignment.Center
        ) {

            if (isUploading) {

                CircularProgressIndicator()

            } else if (profileBitmap != null) {

                ComposeImage(
                    bitmap = profileBitmap,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

            } else {

                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile picture placeholder",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = if (isUploading) {
                "Uploading..."
            } else if (imageBytes != null) {
                "Tap to change profile picture"
            } else {
                "Tap to upload profile picture"
            },
            style = MaterialTheme.typography.bodySmall
        )

        // Image options.
        if (showOptions) {

            AlertDialog(
                onDismissRequest = { showOptions = false },

                title = {
                    Text("Profile Picture")
                },

                text = {
                    Text("What would you like to do?")
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            showOptions = false
                            imagePicker.launch("image/*")
                        }
                    ) {
                        Text(
                            if (imageBytes != null) "Change Picture"
                            else "Upload Picture"
                        )
                    }
                },

                dismissButton = {

                    Row {

                        if (imageBytes != null) {

                            TextButton(
                                onClick = {

                                    showOptions = false
                                    isUploading = true

                                    imageDB.deleteImage(
                                        id = profilePictureId,

                                        onSuccess = {
                                            imageBytes = null
                                            onImageRemoved()
                                            isUploading = false
                                        },

                                        onFailure = { exception ->
                                            errorMessage = exception.message
                                                ?: "Could not remove picture."

                                            isUploading = false
                                        }
                                    )
                                }
                            ) {
                                Text("Remove")
                            }
                        }

                        TextButton(
                            onClick = {
                                showOptions = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }

        if (errorMessage != null) {

            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Resizes and compresses an image into JPEG bytes.
 *
 * Keeps the longest dimension at or below 1024 pixels
 * and attempts to keep the image below 900 KB.
 */
private fun compressImage(
    context: android.content.Context,
    uri: android.net.Uri
): ByteArray {

    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw Exception("Could not open selected image.")

    val bitmap = inputStream.use {
        BitmapFactory.decodeStream(it)
    } ?: throw Exception("Could not decode selected image.")

    var resizedBitmap = bitmap

    val maxDimension = 1024

    if (bitmap.width > maxDimension || bitmap.height > maxDimension) {

        val scale = minOf(
            maxDimension.toFloat() / bitmap.width,
            maxDimension.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()

        resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            newWidth,
            newHeight,
            true
        )
    }

    var quality = 85
    var bytes: ByteArray

    do {

        val outputStream = ByteArrayOutputStream()

        resizedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            quality,
            outputStream
        )

        bytes = outputStream.toByteArray()
        quality -= 10

    } while (bytes.size > 900 * 1024 && quality >= 45)

    if (bitmap !== resizedBitmap) {
        bitmap.recycle()
    }

    resizedBitmap.recycle()

    if (bytes.size > 900 * 1024) {
        throw Exception("Image is too large. Please select a smaller image.")
    }

    return bytes
}
