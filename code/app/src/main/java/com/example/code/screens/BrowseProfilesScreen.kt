
package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import com.example.code.components.BackButton
import com.example.code.ui.theme.PichusTheme
import androidx.compose.ui.unit.dp
import com.example.code.User

/**
 * Allows an administrator to browse all user profiles.
 *
 * @param users List of users retrieved from UserDB
 */
@Composable
fun BrowseProfilesScreen(
    users: List<User>,
    onBack: () -> Unit,
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
                    UserProfileCard(user = user)
                }
            }
        }
    }
}

/**
 * Displays the information belonging to one user.
 */
@Composable
private fun UserProfileCard(user: User) {
    Card( modifier = Modifier.fillMaxWidth() ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = user.name.ifBlank { "Unnamed User" },
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Role: ${user.role.ifBlank { "Not specified" }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Email: ${user.email.ifBlank { "Not provided" }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Phone: ${
                    if (user.phoneNum > 0) user.phoneNum.toString()
                    else "Not provided"
                }",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Preview of screen for reference
@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun BrowseProfilesScreenPreview() {
    PichusTheme {
        BrowseProfilesScreen(
            users = listOf(
                User(
                    name = "Alex Johnson",
                    userID = 1001,
                    email = "alex@gmail.com",
                    phoneNum = 7801234567,
                    role = "Entrant"
                ),
                User(
                    name = "Sarah Smith",
                    userID = 1002,
                    email = "sarah@gmail.com",
                    phoneNum = 7802345678,
                    role = "Campsite Owner"
                ),
                User(
                    name = "Michael Brown",
                    userID = 1003,
                    email = "michael@gmail.com",
                    phoneNum = 7803456789,
                    role = "Entrant"
                ),
                User(
                    name = "Emma Wilson",
                    userID = 1004,
                    email = "emma@gmail.com",
                    phoneNum = 7804567890,
                    role = "Campsite Owner"
                )
            ),
            onBack = {}
        )
    }
}