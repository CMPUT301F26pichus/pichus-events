
package com.example.code.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

// Represents the available navigation tabs
enum class AppTab(
    val title: String,
    val icon: ImageVector
) {
    SEARCH("Search", Icons.Default.Search),
    RESERVATIONS("Reservations", Icons.Default.DateRange),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    PROFILE("Profile", Icons.Default.Person)
}

// Displays the bottom navigation bar
@Composable
fun BottomNavigationBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar {

        AppTab.entries.forEach { tab ->

            NavigationBarItem(
                selected = selectedTab == tab,

                onClick = {
                    onTabSelected(tab)
                },

                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },

                label = {
                    Text(tab.title)
                }
            )
        }
    }
}
