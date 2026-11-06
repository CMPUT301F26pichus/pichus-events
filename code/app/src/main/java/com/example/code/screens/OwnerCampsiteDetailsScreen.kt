package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Campsite
import com.example.code.components.BackButton
import com.example.code.data.SampleCampsites
import com.example.code.ui.theme.DarkBrown
import com.example.code.ui.theme.PichusTheme

/**
 * US 02.01.04: As a campsite owner, I want to view details of my campsite.
 * Displays campsite details and an Update button for editing (US 02.01.03).
 */
@Composable
fun OwnerCampsiteDetailsScreen(
    campsite: Campsite,
    onBack: () -> Unit,
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            text = campsite.name.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Update Button (US 02.01.03)
        Button(
            onClick = onUpdateClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBrown,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(48.dp)
        ) {
            Text(
                text = "UPDATE",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(label = "Location", value = campsite.locationName.ifBlank { "Not specified" })
                DetailRow(
                    label = "Max Occupancy",
                    value = if (campsite.occupancy > 0) campsite.occupancy.toString() else "Not specified"
                )
                if (campsite.price > 0) {
                    DetailRow(label = "Price", value = "$${"%.2f".format(campsite.price)} / night")
                }
                if (campsite.rating > 0) {
                    DetailRow(label = "Rating", value = "★ ${campsite.rating}")
                }
                DetailRow(label = "Description / Rules", value = campsite.policy.ifBlank { "None" })
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun OwnerCampsiteDetailsScreenPreview() {
    PichusTheme {
        OwnerCampsiteDetailsScreen(
            campsite = SampleCampsites.all().first(),
            onBack = {},
            onUpdateClick = {}
        )
    }
}
