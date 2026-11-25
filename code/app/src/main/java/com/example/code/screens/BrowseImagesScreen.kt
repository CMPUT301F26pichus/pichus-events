
package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.Image
import com.example.code.components.BackButton
import com.example.code.components.CampsiteImageCard

/**
 * Allows an administrator to browse uploaded campsite images
 * and remove images when necessary.
 *
 * @param images List of images retrieved from ImageDB
 * @param onBack Returns to the previous screen
 * @param onDeleteImage Deletes the selected image
 */
@Composable
fun BrowseImagesScreen(
    images: List<Image>,
    onBack: () -> Unit,
    onDeleteImage: (Image) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by rememberSaveable { mutableStateOf("") }
    var imageToDelete by remember { mutableStateOf<Image?>(null) }

    // Only display campsite images.
    val campsiteImages = images.filter { image ->
        image.imageType == "CAMPSITE"
    }

    // Search by image ID, campsite ID, or owner ID.
    val filteredImages = campsiteImages.filter { image ->
        searchText.isBlank() ||
                image.id.contains(searchText, ignoreCase = true) ||
                image.campsiteId.contains(searchText, ignoreCase = true) ||
                image.userId.contains(searchText, ignoreCase = true)
    }

    // Confirm image deletion.
    imageToDelete?.let { image ->
        AlertDialog(
            onDismissRequest = {
                imageToDelete = null
            },
            title = {
                Text("Delete Image")
            },
            text = {
                Text(
                    "Are you sure you want to delete this campsite image?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteImage(image)
                        imageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        imageToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        BackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.Start)
        )

        Text(
            text = "BROWSE IMAGES",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Search images by image ID, campsite ID, or owner ID.
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = {
                Text("Search images")
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Text(
            text = "${filteredImages.size} images found",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (filteredImages.isEmpty()) {
            Text(
                text = if (campsiteImages.isEmpty()) {
                    "No campsite images found."
                } else {
                    "No images match your search."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = filteredImages, key = { it.id }) { image ->
                    CampsiteImageCard(
                        image = image,
                        onDeleteClick = { imageToDelete = image }
                    )
                }
            }
        }
    }
}
