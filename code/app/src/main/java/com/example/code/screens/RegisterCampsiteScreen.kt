package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.code.Campsite
import com.example.code.components.BackButton
import com.example.code.ui.theme.DarkBrown
import com.example.code.ui.theme.PichusTheme
import com.example.code.ImageDB
import com.example.code.Image
import com.example.code.components.ImageUtils
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.runtime.rememberCoroutineScope
import com.example.code.ui.theme.DeepSage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Owner register campsite screen. Owners can register new campsites (US 02.01.01).
 */

@Composable
fun RegisterCampsiteScreen(
    currentUserId: Int,
    imageDB: ImageDB,
    onCampsiteRegistered: (Campsite) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var occupancyText by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
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
        // Back Button
        BackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.Start)
        )

        Text(
            text = "REGISTER CAMPSITE",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1. CAMPSITE NAME
        CampsitePillTextField(
            value = name,
            onValueChange = { name = it },
            placeholderText = "CAMPSITE NAME"
        )

        // 2. Location
        CampsitePillTextField(
            value = location,
            onValueChange = { location = it },
            placeholderText = "Location"
        )

        // 3. Description
        CampsitePillTextField(
            value = description,
            onValueChange = { description = it },
            placeholderText = "Description"
        )

        // 4. Max Occupancy: #
        CampsitePillTextField(
            value = occupancyText,
            onValueChange = { occupancyText = it.filter { char -> char.isDigit() } },
            placeholderText = "Max Occupancy: #",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Optional campsite image
        Text(text = "CAMPSITE IMAGE (OPTIONAL)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .clickable(enabled = !isUploading) { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUris.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${selectedImageUris.size} image(s) selected",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Tap to select more images",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )

                    Text("Tap to select campsite images")
                }
            }
        }

        if (selectedImageUris.isNotEmpty()) {
            TextButton(onClick = { selectedImageUris = emptyList() }, enabled = !isUploading) {
                Text("Remove selected images")
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Submit Button
        Button(
            onClick = {
                if (name.isBlank() || location.isBlank()) {
                    errorMessage = "Campsite name and location are required."
                    return@Button
                }

                val occupancy = occupancyText.toIntOrNull()
                if (occupancy == null || occupancy <= 0) {
                    errorMessage = "Please enter a valid maximum occupancy."
                    return@Button
                }

                val newCampsite = Campsite(
                    name = name.trim(),
                    locationName = location.trim(),
                    description = description.trim(),
                    occupancy = occupancy,
                    price = 0.0,
                    rating = 0.0,
                    policy = "",
                    ownerID = currentUserId
                )

                if (selectedImageUris.isEmpty()) {
                    onCampsiteRegistered(newCampsite)
                } else {
                    isUploading = true
                    errorMessage = null
                    val imageUris = selectedImageUris // Keep a copy of the selected URIs for this upload

                    scope.launch {
                        try { // Upload images one at a time
                            for (uri in imageUris) {
                                val compressedBytes = withContext(Dispatchers.IO) {
                                    ImageUtils.compressImage(context, uri)
                                }
                                val imageId = "campsite_${newCampsite.id}_${UUID.randomUUID()}"
                                val campsiteImage = Image(
                                    id = imageId,
                                    imageData = imageDB.createBlob(compressedBytes),
                                    imageType = "CAMPSITE",
                                    userId = currentUserId.toString(),
                                    campsiteId = newCampsite.id
                                )
                                imageDB.addImageAwait(campsiteImage)
                            }
                            // Register the campsite after all images upload successfully.
                            isUploading = false
                            onCampsiteRegistered(newCampsite)

                        } catch (exception: Exception) {
                            isUploading = false
                            errorMessage = exception.message
                                ?: "Could not upload campsite images."
                        }
                    }
                }
            },
            enabled = !isUploading, // Prevent multiple submissions while uploading
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
                text = if (isUploading) "Uploading Images..." else "REGISTER CAMPSITE",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Custom rounded pill text field matching app design mockup
 */
@Composable
private fun CampsitePillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
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

@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun RegisterCampsiteScreenPreview() {
    PichusTheme {
        RegisterCampsiteScreen(
            currentUserId = 1,
            imageDB = ImageDB(),
            onCampsiteRegistered = {},
            onBack = {}
        )
    }
}
