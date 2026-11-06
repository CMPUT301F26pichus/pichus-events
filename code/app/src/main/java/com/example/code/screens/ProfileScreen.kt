
package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.User
import com.example.code.components.ProfilePicture


/**
 * Profile screen. Displays the current user's profile information.
 */
@Composable
fun ProfileScreen(
    user: User?,
    onEditProfile: () -> Unit,
    onAdminTools: () -> Unit,
    onLogout: () -> Unit,
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
            text = "Account",
            style = MaterialTheme.typography.titleLarge
        )

        // Profile picture
        ProfilePicture()

        if (user == null) {
            Text(
                text = "No user profile available.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            // User's name
            Text(
                text = user.name.ifBlank { "Unnamed User" },
                style = MaterialTheme.typography.titleMedium
            )

            // User's profile information
            ProfileInformationCard(user)

            // Edit profile button
            Button(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
                Spacer(Modifier.size(8.dp))
                Text("Edit Profile")
            }
        }

        // Admin tools are only available to Admin users
        if (user?.role == "Admin") {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAdminTools,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Admin Tools")
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out")
        }
    }
}

/**
 * Displays the user's email, phone number, and account type.
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
