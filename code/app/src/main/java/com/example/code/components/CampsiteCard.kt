package com.example.code.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.code.Campsite
import androidx.compose.ui.tooling.preview.Preview
import com.example.code.data.SampleCampsites
import com.example.code.ui.theme.DarkBrown
import com.example.code.ui.theme.PichusTheme

/**
 * One campsite in the browse list. Shows summary info (US 01.01.04):
 * 1. Name
 * 2. Location
 * 3. Rating
 * 4. Max occupancy
 * Tapping it will open up the campsite's details (US 01.01.05)
 */

@Composable
fun CampsiteCard(
    campsite: Campsite, // Campsite this card shows
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    matchScore: Int? = null
) {
    Card(
        onClick = onInfoClick, // Shows ripple effect when clicked (to show it's been clicked) and then opens details
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            //.clickable(onClick = onInfoClick) // whole card is clickable
    ) {
        // Text on the left, icon on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp)
        ) {
            // Want the 4 details stacked
            // weight(1f) takes up all leftover space and pushes the icon to the right
            Column(modifier = Modifier.weight(1f)) {
                if (matchScore != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = DarkBrown,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "$matchScore% Match",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = campsite.name.uppercase(), // Make all caps
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = campsite.locationName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (campsite.rating <= 0.0) {
                        "No ratings yet"
                    } else {
                        "★ ${"%.1f".format(campsite.rating)}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Max Occupancy: ${campsite.occupancy}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // The i button on right for info icon
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "View Details"
                )
            }
        }
    }
}

// Shows one sample card in preview pane for reference
@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)

@Composable
private fun CampsiteCardPreview() {
    PichusTheme {
        CampsiteCard(
            campsite = SampleCampsites.all().first(),
            onInfoClick = {}
        )
    }
}