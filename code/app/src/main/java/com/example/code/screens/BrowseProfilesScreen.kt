
package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import com.example.code.components.BackButton
import com.example.code.components.UserProfileCard
import com.example.code.ui.theme.PichusTheme
import androidx.compose.ui.unit.dp
import com.example.code.User
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.code.ImageDB

/**
 * Allows an administrator to browse all user profiles.
 *
 * @param users List of users retrieved from UserDB
 */
@Composable
fun BrowseProfilesScreen(
    users: List<User>,
    imageDB: ImageDB? = null,
    onBack: () -> Unit,
    onDeleteUser: (User) -> Unit,
    onDeleteProfilePicture: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by rememberSaveable { mutableStateOf("") }

    // Filter profiles based on the search text
    val filteredUsers = users.filter { user ->
        searchText.isBlank() ||
                user.name.contains(searchText, ignoreCase = true) ||
                user.email.contains(searchText, ignoreCase = true) ||
                user.role.contains(searchText, ignoreCase = true) ||
                user.phoneNum.toString().contains(searchText)
    }

    var userToDelete by remember { mutableStateOf<User?>(null) }

    var userPicToDelete by remember { mutableStateOf<User?>(null) }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete User") },
            text = {
                Text("Are you sure you want to delete ${user.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteUser(user)
                        userToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { userToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    userPicToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = {
                userPicToDelete = null
            },
            title = {
                Text("Remove Profile Picture")
            },
            text = {
                Text("Are you sure you want to remove ${user.name}'s profile picture?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfilePicture(user)
                        userPicToDelete = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        userPicToDelete = null
                    }
                ) {
                    Text("Cancel")
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
            text = "Browse Profiles",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Search profiles by name, email, phone number, or role
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search profiles") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Text(
            text = "${filteredUsers.size} profiles found",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (filteredUsers.isEmpty()) {
            Text(text =
                if (users.isEmpty()) {
                    "No user profiles found."
                }
                else {
                    "No profiles match your search."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
        else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredUsers) { user ->
                    Column {
                        UserProfileCard(
                            user = user,
                            imageDB = imageDB,
                            onDeleteClick = {
                                userToDelete = user
                            },
                            onDeletePictureClick = { userPicToDelete = user }
                        )
                    }
                }
            }
        }
    }
}
