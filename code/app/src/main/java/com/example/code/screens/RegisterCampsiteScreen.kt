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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Campsite
import com.example.code.components.BackButton
import com.example.code.ui.theme.DarkBrown
import com.example.code.ui.theme.PichusTheme

/**
 * Owner register campsite screen. Owners can register new campsites (US 02.01.01).
 */

@Composable
fun RegisterCampsiteScreen(
    currentUserId: Int,
    onCampsiteRegistered: (Campsite) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var occupancyText by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

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

        // 5. Tags
        CampsitePillTextField(
            value = tags,
            onValueChange = { tags = it },
            placeholderText = "Tags"
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Submit Button
        Button(
            onClick = {
                if (name.isBlank() || location.isBlank()) {
                    errorMessage = "Campsite name and location are required."
                    return@Button
                }

                val occupancy = occupancyText.toIntOrNull() ?: 1
                val newCampsite = Campsite(
                    name = name.trim(),
                    locationName = location.trim(),
                    occupancy = occupancy,
                    price = 0.0,
                    rating = 0.0,
                    policy = description.trim(),
                    ownerID = currentUserId
                )

                onCampsiteRegistered(newCampsite)
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBrown,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(48.dp)
        ) {
            Text(
                text = "Register Campsite",
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
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun RegisterCampsiteScreenPreview() {
    PichusTheme {
        RegisterCampsiteScreen(
            currentUserId = 1,
            onCampsiteRegistered = {},
            onBack = {}
        )
    }
}
