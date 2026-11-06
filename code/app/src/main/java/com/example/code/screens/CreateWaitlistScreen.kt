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
import com.example.code.WaitingList
import com.example.code.components.BackButton
import com.example.code.data.SampleCampsites
import com.example.code.ui.theme.DarkBrown
import com.example.code.ui.theme.PichusTheme
import com.google.firebase.Timestamp
import java.util.Date

/**
 * US 02.04.01, US 02.04.02, US 02.04.03, US 02.04.07:
 * As a campsite owner, I want to set a registration period and optional capacity for my waitlist.
 */
@Composable
fun CreateWaitlistScreen(
    campsite: Campsite,
    existingWaitlist: WaitingList?,
    onSaveWaitlist: (WaitingList) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var capacityText by rememberSaveable {
        mutableStateOf(existingWaitlist?.capacity?.toString() ?: "")
    }

    var daysText by rememberSaveable {
        mutableStateOf(
            if (existingWaitlist?.registrationDeadline != null) {
                val diffMillis = existingWaitlist.registrationDeadline!!.toDate().time - System.currentTimeMillis()
                val days = (diffMillis / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
                days.toString()
            } else {
                "7"
            }
        )
    }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.Start)
        )

        Text(
            text = "CREATE LOTTERY",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Campsite: ${campsite.name}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Registration Period (Days from now)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            WaitlistPillTextField(
                value = daysText,
                onValueChange = { daysText = it.filter { char -> char.isDigit() } },
                placeholderText = "e.g. 7 days",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Optional Capacity (leave blank for unlimited)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            WaitlistPillTextField(
                value = capacityText,
                onValueChange = { capacityText = it.filter { char -> char.isDigit() } },
                placeholderText = "Optional max capacity",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                val days = daysText.toIntOrNull()
                if ((days == null) || (days <= 0)) {
                    errorMessage = "Please enter a valid registration period in days."
                    return@Button
                }

                val capacity = capacityText.toIntOrNull()
                val deadlineMillis = System.currentTimeMillis() + (days.toLong() * 24 * 60 * 60 * 1000)
                val deadlineTimestamp = Timestamp(Date(deadlineMillis))

                val waitlist = WaitingList(
                    campsiteID = campsite.id,
                    entrantIDs = existingWaitlist?.entrantIDs ?: mutableListOf(),
                    capacity = capacity,
                    registrationDeadline = deadlineTimestamp
                )

                onSaveWaitlist(waitlist)
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBrown,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(48.dp)
        ) {
            Text(
                text = "SAVE LOTTERY",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun WaitlistPillTextField(
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
private fun CreateWaitlistScreenPreview() {
    PichusTheme {
        CreateWaitlistScreen(
            campsite = SampleCampsites.all().first(),
            existingWaitlist = null,
            onSaveWaitlist = {},
            onBack = {}
        )
    }
}
