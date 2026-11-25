
package com.example.code.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.ImageDB
import com.example.code.User
import com.example.code.components.ProfilePicture
import com.example.code.ui.theme.ButtonCream
import com.example.code.ui.theme.DeepSage
import androidx.compose.material3.ButtonDefaults

/**
 * Profile screen. Displays the current user's profile information.
 */
@Composable
fun ProfileScreen(
    user: User?,
    imageDB: ImageDB,
    onEditProfile: () -> Unit,
    onAdminTools: () -> Unit,
    onLogout: () -> Unit,
    onProfilePictureUploaded: (String) -> Unit,
    onProfilePictureRemoved: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "ACCOUNT",
            style = MaterialTheme.typography.titleLarge
        )

        if (user != null) {

            ProfilePicture(
                userId = user.userID,
                profilePictureId = user.profilePictureId,
                imageDB = imageDB,
                onImageUploaded = onProfilePictureUploaded,
                onImageRemoved = onProfilePictureRemoved
            )

            Text(
                text = user.name.ifBlank { "Unnamed User" },
                style = MaterialTheme.typography.titleMedium
            )

            ProfileInformationCard(user)

            Button(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )

                Spacer(Modifier.size(8.dp))

                Text("EDIT PROFILE")
            }

        } else {

            Text(
                text = "No user profile available.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (user?.role == "Admin") {

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAdminTools,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ADMIN TOOLS")
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepSage,
                contentColor = ButtonCream
            )
        ) {
            Text("LOG OUT")
        }
    }
}

/**
 * Displays the user's email, phone number and account type.
 */
@Composable
private fun ProfileInformationCard(user: User) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            ProfileInformation(
                label = "Email",
                value = user.email.ifBlank { "Not provided" }
            )

            ProfileInformation(
                label = "Phone Number",
                value = if (user.phoneNum > 0) {
                    user.phoneNum.toString()
                } else {
                    "Not provided"
                }
            )

            ProfileInformation(
                label = "Account Type",
                value = user.role.ifBlank { "Not specified" }
            )
        }
    }
}

/**
 * Displays one piece of profile information.
 */
@Composable
private fun ProfileInformation(
    label: String,
    value: String
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
