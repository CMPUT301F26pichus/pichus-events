package com.example.code.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Campsite
import com.example.code.Image
import com.example.code.ImageDB
import com.example.code.components.BackButton
import com.example.code.components.ImageUtils
import com.example.code.ui.theme.DeepSage
import com.example.code.ui.theme.PichusTheme
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * US 02.01.03: As a campsite owner, I want to update details about my campsite.
 * US 02.01.02: Update map location (geolocation), description, max occupancy, tags, and images.
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
    var tags by rememberSaveable { mutableStateOf(campsite.tags) }
    var latText by rememberSaveable { mutableStateOf(campsite.location?.latitude?.toString() ?: "") }
    var lonText by rememberSaveable { mutableStateOf(campsite.location?.longitude?.toString() ?: "") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val removedImageIds = remember { mutableStateListOf<String>() }
    var isSelectingImagesToRemove by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val existingImages = imageDB.images.filter { image ->
        image.imageType == "CAMPSITE" &&
                image.campsiteId == campsite.id
    }

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

        UpdatePillTextField(
            value = tags,
            onValueChange = { tags = it },
            placeholderText = "Tags"
        )

        Text(
            text = "MAP LOCATION (GEOLOCATION)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
        )

        UpdatePillTextField(
            value = latText,
            onValueChange = { latText = it },
            placeholderText = "Latitude (e.g. 53.5461)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        UpdatePillTextField(
            value = lonText,
            onValueChange = { lonText = it },
            placeholderText = "Longitude (e.g. -113.4938)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text(
            text = "IMAGES",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 8.dp)
        )

        val visibleExistingImages = existingImages.filter { image ->
            image.id !in removedImageIds
        }

        val totalVisibleImages = visibleExistingImages.size + selectedImageUris.size

        if (totalVisibleImages == 0) {
            Text(
                text = "No images uploaded yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val rows = (totalVisibleImages + 2) / 3

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height((rows * 113 - 8).dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {

                items(
                    items = visibleExistingImages,
                    key = { it.id }
                ) { image ->

                    val bitmap = remember(image.id, image.imageData) {
                        image.imageData?.toBytes()?.let { bytes ->
                            BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size
                            )?.asImageBitmap()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .then(
                                if (isSelectingImagesToRemove) {
                                    Modifier.clickable {
                                        removedImageIds.add(image.id)
                                    }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Campsite image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Image unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                items(
                    items = selectedImageUris,
                    key = { "new_$it" }
                ) { uri ->

                    val bitmap = remember(uri) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)?.asImageBitmap()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 2.dp,
                                color = DeepSage,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "New campsite image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Preview unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Text(
                            text = "NEW",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(
                                    DeepSage,
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        bottomEnd = 8.dp
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        if (existingImages.isNotEmpty()) {
            Button(
                onClick = {
                    if (isSelectingImagesToRemove) {
                        // Cancel selection and restore hidden images.
                        isSelectingImagesToRemove = false
                        removedImageIds.clear()
                    } else if (removedImageIds.isNotEmpty()) {
                        // Undo images previously selected for removal.
                        removedImageIds.clear()
                    } else {
                        // Enter image removal mode.
                        isSelectingImagesToRemove = true
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        isSelectingImagesToRemove -> "CANCEL REMOVE"
                        removedImageIds.isNotEmpty() -> "UNDO REMOVALS"
                        else -> "REMOVE IMAGES"
                    }
                )
            }
        }

        if (isSelectingImagesToRemove) {
            Text(text = "${removedImageIds.size} image(s) selected.", style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = { isSelectingImagesToRemove = false },
                enabled = removedImageIds.isNotEmpty() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DELETE SELECTED")
            }
        }

        Button(
            onClick = { imagePicker.launch("image/*") },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ADD IMAGES")
        }

        if (selectedImageUris.isNotEmpty()) {
            Text(
                text = "${selectedImageUris.size} new image(s) selected.",
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = { selectedImageUris = emptyList() },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CLEAR NEW IMAGES")
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

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

                val lat = latText.toDoubleOrNull()
                val lon = lonText.toDoubleOrNull()
                val geoPoint = if (lat != null && lon != null) GeoPoint(lat, lon) else null

                errorMessage = null
                isSaving = true

                coroutineScope.launch {
                    try {
                        for (imageId in removedImageIds.toList()) {
                            imageDB.deleteImageAwait(imageId)
                        }

                        for (uri in selectedImageUris) {
                            val compressedBytes = withContext(Dispatchers.IO) {
                                ImageUtils.compressImage(context, uri)
                            }

                            val imageId = "campsite_${campsite.id}_${UUID.randomUUID()}"

                            val newImage = Image(
                                id = imageId,
                                imageData = imageDB.createBlob(compressedBytes),
                                imageType = "CAMPSITE",
                                userId = campsite.ownerID.toString(),
                                campsiteId = campsite.id
                            )

                            imageDB.addImageAwait(newImage)
                        }

                        campsite.locationName = location.trim()
                        campsite.occupancy = occupancy
                        campsite.policy = description.trim()
                        campsite.tags = tags.trim()
                        campsite.location = geoPoint

                        onCampsiteUpdated(campsite)

                    } catch (exception: Exception) {
                        errorMessage = "Unable to save changes: ${exception.message}"
                    } finally {
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepSage,
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
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline
        ),
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth()
    )
}

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
