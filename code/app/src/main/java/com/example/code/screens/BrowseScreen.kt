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
import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.example.code.Availability
import com.example.code.Reservation
import com.example.code.ui.theme.ButtonCream
import com.example.code.ui.theme.DeepSage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Entrant home screen. Entrants can search campsites by keyword (US 01.01.01)
 * filter by location, date, and number of guests (US 01.01.02),
 * and see the list of campsites (US 01.01.03)
 */

@Composable
fun BrowseScreen(
    campsites: List<Campsite>, // Campsites to show
    onCampsiteClick: (Campsite) -> Unit,
    modifier: Modifier = Modifier,
    availabilities: List<Availability> = emptyList(),
    reservations: List<Reservation> = emptyList()
) {
    // Remembers what user typed in search bar. If changes, screen redraws itself. Survives screen rotation (android rebuilding the screen - keeps value)
    // Learned from Android Developers, "Save UI state in Compose", https://developer.android.com/develop/ui/compose/state-saving
    var searchText by rememberSaveable { mutableStateOf("") }

    // The list that gets shown (recalculated whenever searchText or campsites change)
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var locationFilter by rememberSaveable { mutableStateOf("") }
    var guestsFilter by rememberSaveable { mutableStateOf("") }
    var selectedDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }

    val context = LocalContext.current
    val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val hasActiveFilters = locationFilter.isNotBlank() || guestsFilter.isNotBlank() || selectedDateMillis != null
    val guestCount = guestsFilter.toIntOrNull()

    // Filter campsites (US 01.01.02) and rank by Match Score (#83)
    // Search by name (US 01.01.01), then rank all results by Match Score (US 01.01.02 & #83)
    val rankedResults: List<Pair<Campsite, Int?>> = campsites
        .filter { campsite ->
            searchText.isBlank() || campsite.name.contains(searchText, ignoreCase = true)
        }
        .map { campsite ->
            val score = if (hasActiveFilters) {
                val isDateAvailable = selectedDateMillis == null ||
                        isCampsiteAvailableOnDate(campsite, selectedDateMillis!!, availabilities, reservations)

                calculateMatchScore(
                    campsite = campsite,
                    locationFilter = locationFilter,
                    guestCount = guestCount,
                    hasDateFilter = selectedDateMillis != null,
                    isDateAvailable = isDateAvailable
                )
            } else {
                null
            }
            campsite to score
        }
        .let { list ->
            if (hasActiveFilters) {
                // Hide campsites that match none of the filters (0%), then rank best match first
                list.filter { (it.second ?: 0) > 0 }
                    .sortedByDescending { it.second ?: 0 }
            } else {
                list
            }
        }

    // Search bar on top, with the list of campsites below it
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Beige search pill
        // Search bar + Filter toggle button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
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
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            FilledTonalButton(onClick = { showFilters = !showFilters },
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(if (hasActiveFilters) "FILTER •" else "FILTER")
            }
        }

        // Collapsible filter panel (US 01.01.02)
        if (showFilters) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = locationFilter,
                        onValueChange = { locationFilter = it },
                        label = { Text("Location (e.g., Jasper)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = guestsFilter,
                            onValueChange = { input ->
                                guestsFilter = input.filter { it.isDigit() }
                            },
                            label = { Text("Guests") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                if (selectedDateMillis != null) {
                                    cal.timeInMillis = selectedDateMillis!!
                                }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val picked = Calendar.getInstance().apply {
                                            set(year, month, day, 0, 0, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        selectedDateMillis = picked.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.weight(1.3f),
                            // Add outline
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = DeepSage,
                                contentColor = ButtonCream
                            ),
                        ) {
                            Text(
                                text = selectedDateMillis?.let {
                                    displayDateFormat.format(Date(it))
                                } ?: "PICK DATE"
                            )
                        }
                    }

                    if (hasActiveFilters) {
                        TextButton(
                            onClick = {
                                locationFilter = ""
                                guestsFilter = ""
                                selectedDateMillis = null
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text("CLEAR")
                        }
                    }
                }
            }
        }

        // Scrollable list of the campsites
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (rankedResults.isEmpty()) {
                item {
                    Text(
                        text = "No campsites match your filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                items(rankedResults) { (campsite, score) ->
                    CampsiteCard(
                        campsite = campsite,
                        matchScore = score,
                        onInfoClick = { onCampsiteClick(campsite) }
                    )
                }
            }
        }
    }
}

/**
 * Checks if a campsite is available on the chosen day.
 * Uses AvailabilityDB if the campsite has availability dates configured;
 * otherwise falls back to checking that no ACTIVE reservation overlaps that day.
 */
private fun isCampsiteAvailableOnDate(
    campsite: Campsite,
    selectedDateMillis: Long,
    availabilities: List<Availability>,
    reservations: List<Reservation>
): Boolean {
    val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedDay = dayFormat.format(Date(selectedDateMillis))

    val configuredDates = availabilities
        .filter { it.campsiteID == campsite.id }
        .flatMap { it.dates.orEmpty() }

    if (configuredDates.isNotEmpty()) {
        return configuredDates.any { dayFormat.format(it.toDate()) == selectedDay }
    }

    // Fallback when AvailabilityDB has no entries yet: ensure no active reservation overlaps
    val hasOverlappingReservation = reservations.any { res ->
        res.campsiteID == campsite.id &&
                res.status == "ACTIVE" &&
                res.startDate != null &&
                res.endDate != null &&
                selectedDay >= dayFormat.format(res.startDate.toDate()) &&
                selectedDay <= dayFormat.format(res.endDate.toDate())
    }
    return !hasOverlappingReservation
}

/**
 * Calculates a 0–100% match score across all active filters and ranks from best match to worst match.
 */
private fun calculateMatchScore(
    campsite: Campsite,
    locationFilter: String,
    guestCount: Int?,
    hasDateFilter: Boolean,
    isDateAvailable: Boolean
): Int {
    val scores = mutableListOf<Double>()

    if (locationFilter.isNotBlank()) {
        val query = locationFilter.trim()
        val locScore = when {
            campsite.locationName.equals(query, ignoreCase = true) -> 100.0
            campsite.locationName.contains(query, ignoreCase = true) -> 85.0
            else -> 0.0
        }
        scores.add(locScore)
    }

    if (guestCount != null && guestCount > 0) {
        val guestScore = when {
            campsite.occupancy == guestCount -> 100.0
            campsite.occupancy > guestCount -> (100.0 - (campsite.occupancy - guestCount) * 5.0).coerceAtLeast(75.0)
            else -> ((campsite.occupancy.toDouble() / guestCount) * 70.0).coerceIn(0.0, 70.0)
        }
        scores.add(guestScore)
    }

    if (hasDateFilter) {
        scores.add(if (isDateAvailable) 100.0 else 0.0)
    }

    if (scores.isEmpty()) return 100
    return scores.average().roundToInt().coerceIn(0, 100)
}

// Preview of screen for reference
@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun BrowseScreenPreview() {
    PichusTheme {
        BrowseScreen(campsites = SampleCampsites.all(), onCampsiteClick = {})
    }
}