package com.example.code.screens

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.components.BackButton
import com.example.code.Campsite

/**
 * Campsite details screen (US 01.01.05). Shows more info about the campsite the entrant selects:
 * 1. Campsite name
 * 2. Location
 * 3. Rating
 * 4. Max occupancy
 * 5. Price
 * 6. Rules/policies
 */

@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite, // Campsite to show
    onBack: () -> Unit, // For back arrow option
    modifier: Modifier = Modifier
) {
    // Everything should be stacked top to bottom with small gaps between each line
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center everything
    ) {
        // Back arrow to go back to previous browse screen
        BackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.Start)
        )

        Text(text = campsite.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(text = campsite.locationName, style = MaterialTheme.typography.bodyMedium)
        Text(text = "★ ${campsite.rating}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Max Occupancy: ${campsite.occupancy}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Price: $${"%.2f".format(campsite.price)} / night", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Rules: ${campsite.policy}", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(16.dp)) //extra space before button

        // Placeholder to join lottery but doesn't do anything yet (whoever's doing waitlist hook this up)
        Button(
            onClick = {},
                modifier = Modifier.fillMaxWidth()
        ) {
            Text("JOIN LOTTERY")
        }
    }
}