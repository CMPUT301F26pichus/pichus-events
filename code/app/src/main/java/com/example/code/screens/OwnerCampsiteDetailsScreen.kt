package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Campsite
import com.example.code.ImageDB
import com.example.code.Reservation
import com.example.code.User
import com.example.code.WaitingList
import com.example.code.components.BackButton
import com.example.code.components.CampsiteImageCarousel
import com.example.code.components.CampsiteMapView
import com.example.code.data.SampleCampsites
import com.example.code.ui.theme.DeepSage
import com.example.code.ui.theme.PichusGreen
import com.example.code.ui.theme.PichusTheme
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * US 02.01.04: As a campsite owner, I want to view details of my campsite (including map location, images, tags).
 * US 02.06.01: As a campsite owner, I want to view currently booked dates.
 * US 02.04.03, 02.04.07: As a campsite owner, I want to set a registration period & capacity for my waitlist.
 * US 02.09.01: As a campsite owner, I want to view a list of entrants who have joined the waitlist.
 */
@Composable
fun OwnerCampsiteDetailsScreen(
    campsite: Campsite,
    modifier: Modifier = Modifier,
    imageDB: ImageDB? = null,
    onBack: () -> Unit,
    onUpdateClick: () -> Unit,
    bookedReservations: List<Reservation> = emptyList(),
    waitingList: WaitingList? = null,
    users: List<User> = emptyList(),
    onCreateWaitlistClick: () -> Unit = {}
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

        // Campsite Images Carousel (US 02.01.02)
        if (imageDB != null) {
            CampsiteImageCarousel(campsiteId = campsite.id, imageDB = imageDB)
            Spacer(Modifier.height(4.dp))
        }

        // Update Button (US 02.01.03)
        Button(
            onClick = onUpdateClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = PichusGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
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

        // Details Card (Bubble 1: Location, Max Occupancy, Price, Rating, Description, Tags)
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
                DetailRow(
                    label = "Rating",
                    value = if (campsite.rating <= 0.0) {
                        "No ratings yet"
                    } else {
                        "★ ${"%.1f".format(campsite.rating)}"
                    }
                )
                DetailRow(label = "Description / Rules", value = campsite.policy.ifBlank { "None" })

                // Tags (US 02.01.02)
                if (campsite.tags.isNotBlank()) {
                    val tagList = campsite.tags.split(",", " ").map { it.trim() }.filter { it.isNotBlank() }
                    if (tagList.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                tagList.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Map Location Card (Bubble 2: Map Location below Details Card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Map Location",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                val lat = campsite.location?.latitude ?: 53.5461
                val lon = campsite.location?.longitude ?: -113.4938
                CampsiteMapView(
                    latitude = lat,
                    longitude = lon,
                    title = campsite.name.ifBlank { "Campsite Location" }
                )
                if (campsite.location == null) {
                    Text(
                        text = "Default location shown. Update campsite to set custom GPS coordinates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Lottery Settings Section (US 02.04.03, US 02.04.07)
        Text(
            text = "Lottery Settings:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 8.dp, bottom = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (waitingList == null) {
                    Text(
                        text = "No lottery configured for this campsite.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    DetailRow(
                        label = "Capacity",
                        value = waitingList.capacity?.toString() ?: "Unlimited"
                    )
                    DetailRow(
                        label = "Registration Deadline",
                        value = formatDeadline(waitingList.registrationDeadline)
                    )
                    DetailRow(
                        label = "Current Entrants",
                        value = "${waitingList.entrantIDs.size} joined"
                    )
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onCreateWaitlistClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepSage,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (waitingList == null) "CREATE LOTTERY" else "EDIT LOTTERY",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Entrants List Section (US 02.09.01)
        Text(
            text = "Entrants on Waitlist:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 8.dp, bottom = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if ((waitingList == null) || (waitingList.entrantIDs.isEmpty())) {
                    Text(
                        text = "No entrants have joined this lottery yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    waitingList.entrantIDs.forEach { entrantID ->
                        val user = users.find { it.userID == entrantID }
                        val displayName = user?.name?.ifBlank { null } ?: "Entrant #$entrantID"

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Booked Dates Section (US 02.06.01)
        Text(
            text = "Booked dates:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 8.dp, bottom = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (bookedReservations.isEmpty()) {
                    Text(
                        text = "No booked dates for this campsite.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    bookedReservations.forEach { res ->
                        val start = formatDate(res.startDate)
                        val end = formatDate(res.endDate)
                        Text(
                            text = "• $start  to  $end",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun formatDeadline(timestamp: Timestamp?): String {
    if (timestamp == null) return "No deadline"
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp.toDate())
}

private fun formatDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "N/A"
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp.toDate())
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
            imageDB = null,
            bookedReservations = emptyList(),
            waitingList = null,
            users = emptyList(),
            onBack = {},
            onUpdateClick = {},
            onCreateWaitlistClick = {}
        )
    }
}
