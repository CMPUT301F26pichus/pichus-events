package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.code.Campsite
import com.example.code.components.CampsiteCard
import com.example.code.data.SampleCampsites
import com.example.code.ui.theme.PichusTheme

/**
 * Entrant home screen. Entrants can search campsites by keyword (US 01.01.01)
 * and see the list of campsites (US 01.01.03)
 */

@Composable
fun BrowseScreen(
    campsites: List<Campsite>, // Campsites to show
    onCampsiteClick: (Campsite) -> Unit,
    modifier: Modifier = Modifier
) {
    // Remembers what user typed in search bar. If changes, screen redraws itself. Survives screen rotation (android rebuilding the screen - keeps value)
    // Learned from Android Developers, "Save UI state in Compose", https://developer.android.com/develop/ui/compose/state-saving
    var searchText by rememberSaveable { mutableStateOf("") }

    // The list that gets shown (recalculated whenever searchText or campsites change)
    val results = if (searchText.isBlank()) {
        campsites // Show all campsites if no search
    } else {
        campsites.filter { it.name.contains(searchText, ignoreCase = true) } // Filter - only keeps campsites whose name contains the search and ignore upper/lower case
    }

    // Search bar on top, with the list of campsites below it
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Beige search pill
        OutlinedTextField(
            value = searchText, // Shows whatever is typed so far
            onValueChange = { searchText = it }, // Updates searchText on every key press
            placeholder = { Text("Search...") },
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search") // search icon
            },
            singleLine = true,
            shape = RoundedCornerShape(50), // Fully rounded edges to get the pill shape
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )

        // Scrollable list of the campsites
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp), // Gap between the cards
            modifier = Modifier.fillMaxSize()
        ) {
            items(results) { campsite ->
                CampsiteCard(
                    campsite = campsite,
                    onInfoClick = { onCampsiteClick(campsite) } // Pass tapped campsite to whoever opens details
                )
            }
        }
    }
}

// Preview of screen for reference
@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun BrowseScreenPreview() {
    PichusTheme {
        BrowseScreen(campsites = SampleCampsites.all(), onCampsiteClick = {})
    }
}