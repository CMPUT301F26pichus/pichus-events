package com.example.code.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.code.Campsite
import com.example.code.Reservation
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailsScreen(
    reservation: Reservation,
    campsite: Campsite,
    onBack: () -> Unit,
    onCancelReservation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val startString = reservation.startDate?.toDate()?.let { dateFormat.format(it) } ?: "TBD"
    val endString = reservation.endDate?.toDate()?.let { dateFormat.format(it) } ?: "TBD"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(campsite.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Location: ${campsite.locationName}")
            Text(text = "Number of Guests: ${reservation.guestCount}")
            Text(text = "Confirmed Dates:\n$startString - $endString")
            Text(text = "Details:\n${campsite.policy}")

            if (reservation.status == "ACTIVE" && reservation.endDate != null && reservation.endDate >= com.google.firebase.Timestamp.now()) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onCancelReservation,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("CANCEL RESERVATION")
                }
            } else if (reservation.status == "CANCELLED") {
                Text(
                    text = "CANCELLED",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                //stars!
            }
        }
    }
}