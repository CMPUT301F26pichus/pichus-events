package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.User
import com.example.code.UserDB

/**
 * Edit profile screen. Entrants can update their name, email, and phone number (US 01.07.03) or delete their profile (US 01.08.01)
 */

@Composable
fun EditProfileScreen (
    user: User,
    userDB: UserDB, // to save changes in firstore
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Should start with user's current info already filled in
    var name by remember { mutableStateOf(user.name) }
    var email by remember {mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phoneNum.toString()) }

    var showDeleteDialog by remember { mutableStateOf(false) } // Are you sure dialog

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Text(text = "EDIT PROFILE", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())

        // Save changes - should be green
        Button(
            onClick = {
                //Each userDB update will rebuild whole user, each step will pass in already updated info (otherwise will overwrite last one)
                val withNewName = User(name, user.userID, user.email, user.phoneNum, user.role)
                val withNewEmail = User(name, user.userID, email, user.phoneNum, user.role)
                userDB.updateName(user, name)
                userDB.updateEmail(withNewName, email)
                userDB.updatePhoneNum(withNewEmail, phone)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SAVE")
        }

        // red button to alert - irreversible - delete profile
        Button(
            onClick = { showDeleteDialog = true }, // Should show Are you sure?
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("DELETE ACCOUNT")
        }
    }

    // "Are you sure" popup before deleting
    if (showDeleteDialog) {
        // Learned how to confirm popup from Android Developers, "Dialog", https://developer.android.com/develop/ui/compose/components/dialog
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account?")},
            text = { Text("This permanently deletes your profile.") },
            confirmButton = {
                TextButton(onClick = {
                    userDB.deleteUser(user)
                    showDeleteDialog = false
                    onDeleted()
                }) { Text("DELETE")
                   }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false })
                { Text("CANCEL")}
            }
        )
    }
}