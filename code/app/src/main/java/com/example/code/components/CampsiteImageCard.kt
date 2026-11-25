
package com.example.code.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.code.Image as CampsiteImage

/**
 * Displays a campsite image and its associated information.
 */
@Composable
fun CampsiteImageCard(
    image: CampsiteImage,
    onDeleteClick: () -> Unit
) {
    // Convert the stored image data into a bitmap.
    val bitmap = remember(image.id, image.imageData) {
        image.imageData?.toBytes()?.let { bytes ->
            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )?.asImageBitmap()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Campsite image thumbnail.
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
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
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Image information.
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Campsite Image",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Campsite: ${image.campsiteId}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Owner ID: ${image.userId}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Image ID: ${image.id}",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2
                )
            }

            // Delete image button.
            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete campsite image",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
