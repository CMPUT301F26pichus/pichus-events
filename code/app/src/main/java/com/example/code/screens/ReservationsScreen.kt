package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.CampsiteDB
import com.example.code.ReservationDB
import com.example.code.User
import com.example.code.components.CampsiteCard
import com.example.code.data.UserSession


 //Entrant Reservations Screen US 01.06.01, US 01.06.02
 //displays list of active and past reservations using toggle at the top

@Composable
fun ReservationsScreen(
    currentUser: User,
    reservationDB: ReservationDB,
    campsiteDB: CampsiteDB,
    onReservationClick: (String) -> Unit, // campsite ID to open details for US 01.07.01 later
    modifier: Modifier = Modifier
) {
    // 0 = Active Tab, 1 = Past Tab
    var selectedTabIndex by remember { mutableStateOf(0) }

    val reservations = if (selectedTabIndex == 0) {
        reservationDB.viewActiveReservations(currentUser)
    } else {
        reservationDB.viewPastReservations(currentUser)
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Active", style = MaterialTheme.typography.titleMedium) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Past", style = MaterialTheme.typography.titleMedium) }
            )
        }

        // list of reservations
        if (reservations.isEmpty()) {
            Text(
                text = "No reservations found.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reservations) { reservation ->
                    // find matching campsite object using ID in reservation
                    val campsite = campsiteDB.campsites.find { it.id == reservation.campsiteID }

                    if (campsite != null) {
                        CampsiteCard(
                            campsite = campsite,
                            onInfoClick = { onReservationClick(reservation.campsiteID) }
                        )
                    }
                }
            }
        }
    }
}