
package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.code.components.BackButton
import com.example.code.ui.theme.PichusTheme

/**
 * Menu containing the tools available to an administrator.
 *
 * Navigation is handled by MainActivity through the callbacks.
 */
@Composable
fun AdminToolsScreen(
    onBrowseProfiles: () -> Unit,
    onBrowseImages: () -> Unit,
    onBack: () -> Unit, // For back arrow option
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Admin Tools",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AdminToolCard(
            title = "Browse Profiles",
            description = "View entrant, campsite owner, and admin profiles.",
            icon = {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null
                )
            },
            onClick = onBrowseProfiles
        )
    }
}

/**
 * Reusable card representing one admin tool.
 */
@Composable
private fun AdminToolCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                icon()

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open $title"
            )
        }
    }
}

// Preview of screen for reference
@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun AdminToolsScreenPreview() {
    PichusTheme {
        AdminToolsScreen(
            onBrowseProfiles = {},
            onBrowseImages = {},
            onBack = {}
        )
    }
}