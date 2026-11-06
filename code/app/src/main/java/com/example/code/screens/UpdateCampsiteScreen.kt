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
import com.example.code.data.SampleCampsites
import com.example.code.ui.theme.DarkBrown
import com.example.code.ui.theme.PichusTheme

/**
 * US 02.01.03: As a campsite owner, I want to update details about my campsite.
 * Pre-filled form allowing owners to edit campsite name, location, description, occupancy, etc.
 */
@Composable
fun UpdateCampsiteScreen(
    campsite: Campsite,
    onCampsiteUpdated: (Campsite) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf(campsite.name) }
    var location by rememberSaveable { mutableStateOf(campsite.locationName) }
    var description by rememberSaveable { mutableStateOf(campsite.policy) }
    var occupancyText by rememberSaveable { mutableStateOf(if (campsite.occupancy > 0) campsite.occupancy.toString() else "") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.Start)
        )

        Text(
            text = "UPDATE CAMPSITE",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        UpdatePillTextField(
            value = name,
            onValueChange = { name = it },
            placeholderText = "CAMPSITE NAME"
        )

        UpdatePillTextField(
            value = location,
            onValueChange = { location = it },
            placeholderText = "Location"
        )

        UpdatePillTextField(
            value = description,
            onValueChange = { description = it },
            placeholderText = "Description / Rules"
        )

        UpdatePillTextField(
            value = occupancyText,
            onValueChange = { occupancyText = it.filter { char -> char.isDigit() } },
            placeholderText = "Max Occupancy: #",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                if (name.isBlank() || location.isBlank()) {
                    errorMessage = "Campsite name and location are required."
                    return@Button
                }

                val occupancy = occupancyText.toIntOrNull() ?: campsite.occupancy
                campsite.name = name.trim()
                campsite.locationName = location.trim()
                campsite.occupancy = occupancy
                campsite.policy = description.trim()

                onCampsiteUpdated(campsite)
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
                text = "SAVE CHANGES",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun UpdatePillTextField(
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
private fun UpdateCampsiteScreenPreview() {
    PichusTheme {
        UpdateCampsiteScreen(
            campsite = SampleCampsites.all().first(),
            onCampsiteUpdated = {},
            onBack = {}
        )
    }
}
