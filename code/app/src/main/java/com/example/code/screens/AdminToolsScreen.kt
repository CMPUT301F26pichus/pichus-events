
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
import com.example.code.components.AdminToolCard
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
            icon = { Icon(imageVector = Icons.Default.People, contentDescription = null) },
            onClick = onBrowseProfiles
        )
        AdminToolCard(
            title = "Browse Images",
            description = "View and manage images uploaded to campsites.",
            icon = { Icon(imageVector = Icons.Default.Image, contentDescription = null) },
            onClick = onBrowseImages
        )
    }
}