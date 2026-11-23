package com.example.code.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Campsite
import com.example.code.Image
import com.example.code.ImageDB
import com.example.code.components.BackButton
import com.example.code.components.ImageUtils
import com.example.code.data.SampleCampsites
import com.example.code.ui.theme.DarkBrown
import com.example.code.ui.theme.PichusTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import android.net.Uri
import androidx.compose.ui.draw.clip
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * US 02.01.03: As a campsite owner, I want to update details about my campsite.
 * Pre-filled form allowing owners to edit campsite name, location, description, occupancy, etc.
 */
@Composable
fun UpdateCampsiteScreen(
    campsite: Campsite,
    imageDB: ImageDB,
    onCampsiteUpdated: (Campsite) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var location by rememberSaveable { mutableStateOf(campsite.locationName) }
    var description by rememberSaveable { mutableStateOf(campsite.policy) }
    var occupancyText by rememberSaveable { mutableStateOf(if (campsite.occupancy > 0) campsite.occupancy.toString() else "") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val removedImageIds = remember { mutableStateListOf<String>() }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    // Get all images belonging to this campsite.
    val existingImages = imageDB.images.filter { image ->
        image.imageType == "CAMPSITE" &&
                image.campsiteId == campsite.id
    }

    // Open the gallery and allow selecting multiple images.
    val imagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImageUris = (selectedImageUris + uris).distinct()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.Start)
        )

        Text(
            text = "UPDATE CAMPSITE",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // The campsite name is displayed but not editable because the campsite ID is based on its name
        UpdatePillTextField(
            value = campsite.name,
            onValueChange = {},
            placeholderText = "CAMPSITE NAME",
            enabled = false
        )

        UpdatePillTextField(
            value = location,
            onValueChange = { location = it },
            placeholderText = "Location"
        )

        UpdatePillTextField(
            value = description,
            onValueChange = { description = it },
            placeholderText = "Description / Rules"
        )

        UpdatePillTextField(
            value = occupancyText,
            onValueChange = { occupancyText = it.filter { char -> char.isDigit() } },
            placeholderText = "Max Occupancy: #",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )


        // Existing campsite images
        Text(
            text = "CURRENT IMAGES",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
        )

        if (existingImages.isEmpty()) {
            Text(text = "This campsite does not have any images yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            existingImages.forEachIndexed { index, image ->
                // Convert the Firestore Blob into a Bitmap.
                val bitmap = remember(image.id, image.imageData) {
                    image.imageData?.toBytes()?.let { bytes ->
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Image thumbnail
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Campsite image ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text(text = "Unable to display image ${index + 1}", modifier = Modifier.padding(16.dp))
                    }

                    // Remove / undo button
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (image.id in removedImageIds) {
                            Button(onClick = { removedImageIds.remove(image.id) }) {
                                Text("UNDO")
                            }
                        } else {
                            Button(
                                onClick = { removedImageIds.add(image.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("REMOVE")
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "${existingImages.size - removedImageIds.size} existing image(s) will be kept.",
            style = MaterialTheme.typography.bodySmall
        )


        Text(text = "${existingImages.size - removedImageIds.size} existing image(s) will be kept.", style = MaterialTheme.typography.bodySmall)

        // Add new images
        Text(
            text = "ADD NEW IMAGES",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
        )

        Button(
            onClick = { imagePicker.launch("image/*") },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SELECT IMAGES")
        }
        Text(text = "${selectedImageUris.size} new image(s) selected.", style = MaterialTheme.typography.bodyMedium)

        if (selectedImageUris.isNotEmpty()) {
            Button(
                onClick = { selectedImageUris = emptyList() },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CLEAR SELECTED IMAGES")
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Save all campsite and image changes
        Button(
            onClick = {
                if (location.isBlank()) {
                    errorMessage = "Campsite location is required."
                    return@Button
                }

                val occupancy = occupancyText.toIntOrNull() ?: campsite.occupancy

                if (occupancy < 0) {
                    errorMessage = "Please enter a valid occupancy."
                    return@Button
                }

                errorMessage = null
                isSaving = true

                coroutineScope.launch {
                    try {
                        // Delete images that the owner marked for removal.
                        for (imageId in removedImageIds.toList()) {
                            imageDB.deleteImageAwait(imageId)
                        }

                        // Upload newly selected images.
                        for (uri in selectedImageUris) {
                            val compressedBytes = withContext(Dispatchers.IO) {
                                ImageUtils.compressImage(context, uri)
                            }

                            val imageId =
                                "campsite_${campsite.id}_${UUID.randomUUID()}"

                            val newImage = Image(
                                id = imageId,
                                imageData = imageDB.createBlob(compressedBytes),
                                imageType = "CAMPSITE",
                                userId = campsite.ownerID.toString(),
                                campsiteId = campsite.id
                            )

                            imageDB.addImageAwait(newImage)
                        }

                        // Update the campsite details
                        campsite.locationName = location.trim()
                        campsite.occupancy = occupancy
                        campsite.policy = description.trim()

                        onCampsiteUpdated(campsite)

                    } catch (exception: Exception) {
                        errorMessage =
                            "Unable to save changes: ${exception.message}"
                    } finally {
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBrown,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(48.dp)
        ) {
            Text(
                text = if (isSaving) "SAVING..." else "SAVE CHANGES",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun UpdatePillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = {
            Text(
                text = placeholderText,
                color = Color(0xFF8A7F6E)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        ),
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Waits for Firestore to finish adding an image.
 */
private suspend fun ImageDB.addImageAwait(image: Image) {
    suspendCancellableCoroutine<Unit> { continuation ->
        addImage(
            image = image,
            onSuccess = {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            },
            onFailure = { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
}

/**
 * Waits for Firestore to finish deleting an image.
 */
private suspend fun ImageDB.deleteImageAwait(imageId: String) {
    suspendCancellableCoroutine<Unit> { continuation ->
        deleteImage(
            id = imageId,
            onSuccess = {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            },
            onFailure = { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
}

