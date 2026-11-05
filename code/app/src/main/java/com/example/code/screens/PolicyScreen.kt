package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Policy and guidelines screen (US 01.11.01). Shows the app's rules for lottery, reservations,
 * cancellations, community and data
 */

@Composable
fun PolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Stacked top to bottom and scrolls if text doens't fit
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(text = "POLICY AND GUIDELINES", style = MaterialTheme.typography.titleLarge)

        PolicySection("How the lottery works", "Join a waitlist during the registration period. Winners are drawn at random and notified in the app.")
        PolicySection("Reservations", "Winners may  reserve suitable dates or decline. Read each campsite's rules before entering")
        PolicySection("Cancellations and refunds", "Cancel any time. If an owner rejects a reservation, you are refunded.")
        PolicySection("Community guidelines", "Keep listings accurate and appropriate. Admins may remove content that violates policy.")
        PolicySection("Your data", "Your profile is linked to your device. Deleting your profile removes your data.")
    }
}

// Makes one heading with text underneath
@Composable
private fun PolicySection(title: String, body: String) {
    Column{
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
    }
}