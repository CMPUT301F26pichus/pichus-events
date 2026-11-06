package com.example.code.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    userRole: String,
    onBack: () -> Unit,
    onCancelReservation: () -> Unit,
    onRate: (Int)->Unit,
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
                // past reservation.
                // only show the rating UI if the user is entrant
                if (userRole == "Entrant") {
                    Spacer(modifier = Modifier.weight(1f))

                    var currentRating by remember { mutableIntStateOf(reservation.rating) }
                    var hasRated by remember { mutableStateOf(reservation.rating > 0) }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            IconButton(
                                onClick = { currentRating = i },
                                enabled = !hasRated
                            ) {
                                Icon(
                                    imageVector = if (i <= currentRating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Rate $i Stars",
                                    modifier = Modifier.size(40.dp),
                                    tint = if (i <= currentRating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onRate(currentRating)
                            hasRated = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = currentRating > 0 && !hasRated
                    ) {
                        Text(if (hasRated) "RATED!" else "LEAVE RATING!")
                    }
                } else if (userRole.equals("Campsite Owner", ignoreCase = true)) {
                    // placeholder for owner reservation info veiw (US 02.08.01, US 02.11.01)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("RULES & POLICIES\n✅ Camper has accepted rules and policies of campsite")
                    Button(
                        onClick = { /* reject */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("REJECT")
                    }
                }
            }
        }
    }
}