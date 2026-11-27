package com.example.code.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.Notification
import com.example.code.NotificationType
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Notifications screen. Shows the current user's notifications, newest first,
 * including lottery win (US 01.03.01) and lose (US 01.03.02) results.
 */
@Composable
fun NotificationsScreen(
    notifications: List<Notification>, // Current user's notifications, newest first
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "NOTIFICATIONS",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 12.dp)
        )

        if (notifications.isEmpty()) {
            Text(
                text = "No notifications yet.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            // Scrollable list of notifications
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications) { notification ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = notificationTitle(notification.type),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = notification.message.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            // Show when it was sent, if we have a time
                            notification.message.timestamp?.let { time ->
                                Text(
                                    text = dateFormat.format(time.toDate()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Picks a heading for a notification based on what it is about.
 *
 * @param type what the notification is about
 * @return heading shown at the top of the notification card
 */
fun notificationTitle(type: NotificationType): String {
    return when (type) {
        NotificationType.LOTTERY_WON -> "Lottery Results: You Won!"
        NotificationType.LOTTERY_LOST -> "Lottery Results: Not Selected"
        NotificationType.JOINED_WAITLIST -> "Joined Waitlist"
        NotificationType.RESERVATION_CONFIRMED -> "Reservation Confirmed"
        NotificationType.RESERVATION_CANCELLED -> "Reservation Cancelled"
        NotificationType.OWNER_ANNOUNCEMENT -> "Notification From Owner"
        NotificationType.ENTRANT_MESSAGE -> "New Message"
    }
}