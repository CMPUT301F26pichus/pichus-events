package com.example.code.screens

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    modifier: Modifier = Modifier,
    isOnWaitingList: Boolean = false,
    onJoinLottery: () -> Boolean = {true},
    onLeaveWaitlist: () -> Boolean = {true},
    isAdmin: Boolean = false,
    onDeleteCampsite: () -> Unit = {}
) {
    var errorMessage by remember(campsite.id, isOnWaitingList) {mutableStateOf<String?>(null)}

    var showDeleteConfirmation by remember { mutableStateOf(false) }

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

        if (isAdmin) {
            // Only admins can remove campsites
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("REMOVE CAMPSITE")
            }
        } else if (isOnWaitingList) {
            // shows joined if entrant is alr on the waiting list
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "JOINED",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    errorMessage = null
                    onLeaveWaitlist()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LEAVE WAITLIST")
            }
        } else {
            Button(
                onClick = {
                    val success = onJoinLottery()
                    errorMessage = if (!success) {
                        "Unable to join: waiting list is full or registration is closed."
                    } else {
                        null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("JOIN LOTTERY")
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Remove Campsite?") },
            text = { Text("Are you sure you want to permanently remove ${campsite.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteCampsite()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("REMOVE")
                }
            },
            dismissButton = {
                Button( onClick = { showDeleteConfirmation = false } ) {
                    Text("CANCEL")
                }
            }
        )
    }
}