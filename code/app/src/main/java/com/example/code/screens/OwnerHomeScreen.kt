package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

/**
 * Owner home screen. Owners can view all their campsites (US 02.03.01) and register new ones (US 02.01.01).
 */

@Composable
fun OwnerHomeScreen(
    campsites: List<Campsite>, // Campsites to show
    onCampsiteClick: (Campsite) -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Remembers what user typed in search bar. If changes, screen redraws itself. Also survives screen rotation
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
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // MY CAMPSITES heading
        Text(
            text = "MY CAMPSITES",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF29241F),
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )

        // Register New button
        Button(
            onClick = onRegisterClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF403B36),
                contentColor = Color.White
            ),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .height(36.dp),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 4.dp
            )
        ) {
            Text(
                text = "Register New",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search...") },
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Scrollable campsite list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(results) { campsite ->
                CampsiteCard(
                    campsite = campsite,
                    onInfoClick = { onCampsiteClick(campsite) }
                )
            }
        }
    }
}

// Preview of screen for reference
@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun OwnerHomeScreenPreview() {
    PichusTheme {
        OwnerHomeScreen(
            campsites = SampleCampsites.all().filter { it.ownerID == 1 },
            onCampsiteClick = {},
            onRegisterClick = {}
        )
    }
}