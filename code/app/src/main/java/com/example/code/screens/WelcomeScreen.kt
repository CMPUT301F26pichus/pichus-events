package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import com.example.code.User

/**
 * Welcome screen. This is the screen users see when they first start up/launch the app.
 * Asks the user for their user ID so the app knows who they are
 * (temporary until we implement device identification)
 */

@Composable
fun WelcomeScreen(
    onContinue: (Int) -> Unit, // When continue is tapped - carries entered ID
    modifier: Modifier = Modifier,
    users: List<User>
) {
    // What user typed in input box
    var idText by rememberSaveable { mutableStateOf("") }

    // Center everything
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "WELCOME", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = idText,
            onValueChange = { idText = it },
            placeholder = { Text("Enter your user ID") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // pop up number keyboard
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val id = idText.toIntOrNull() // Text to number, null if it isn't a number
                if (id != null && users.any { it.userID == id }) onContinue(id) // Only continue if it's an existing userID
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CONTINUE")
        }
    }
}