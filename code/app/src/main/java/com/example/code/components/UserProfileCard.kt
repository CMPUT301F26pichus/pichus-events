
package com.example.code.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.code.ImageDB
import com.example.code.User

/**
 * Displays the information and profile picture belonging to one user.
 */
@Composable
fun UserProfileCard(
    user: User,
    imageDB: ImageDB?,
    onDeleteClick: () -> Unit,
    onDeletePictureClick: () -> Unit
) {
    var imageBytes by remember(user.userID) {
        mutableStateOf<ByteArray?>(null)
    }

    // Retrieve this user's profile picture from Firestore.
    LaunchedEffect(user.profilePictureId, imageDB) {
        imageBytes = null

        if (user.profilePictureId.isNotEmpty() && imageDB != null) {
            imageDB.getImage(
                id = user.profilePictureId,
                onSuccess = { image ->
                    imageBytes = image?.imageData?.toBytes()
                }
            )
        }
    }

    // Convert the retrieved bytes into a Compose bitmap.
    val profileBitmap = remember(imageBytes) {
        imageBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture or default placeholder.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(
                        enabled = user.profilePictureId.isNotEmpty(),
                        onClick = onDeletePictureClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap,
                        contentDescription = "${user.name}'s profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default profile picture",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = user.role)
                Text(text = user.email)
                Text(text = user.phoneNum.toString())
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete user",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
