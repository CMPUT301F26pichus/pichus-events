package com.example.code

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.code.screens.BrowseScreen
import com.example.code.screens.CampsiteDetailsScreen
import com.example.code.ui.theme.PichusTheme
import com.example.code.components.AppTab
import com.example.code.components.BottomNavigationBar
/**
 * App entry point. Wraps all content in the pichus theme.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PichusTheme {
                // Connects to firestore once and keeps watching for campsite changes
                val campsiteDB = remember { CampsiteDB() }

                // Remember which bottom navigation tab is selected
                var selectedTab by rememberSaveable { mutableStateOf(AppTab.SEARCH) }

                // See which campsite is currently opened, null means we're still on browse screen (no campsite opened yet)
                var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }

                // Back button goes to previous page browse screen instead of closing the app
                BackHandler(enabled = selectedCampsite != null) {
                    selectedCampsite = null
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    // Bottom navigation bar
                    bottomBar = {
                        BottomNavigationBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab ->
                                selectedTab = tab
                            }
                        )
                    }
                ) { innerPadding ->
                    // Decide which screen to display
                    when (selectedTab) {

                        AppTab.SEARCH -> {

                            if (selectedCampsite == null) {
                                BrowseScreen(
                                    campsites = campsiteDB.campsites,
                                    onCampsiteClick = { selectedCampsite = it },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            else {
                                CampsiteDetailsScreen(
                                    campsite = selectedCampsite!!,
                                    onBack = { selectedCampsite = null },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        AppTab.RESERVATIONS -> {
                            PlaceholderScreen(
                                title = "Reservations",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppTab.NOTIFICATIONS -> {
                            PlaceholderScreen(
                                title = "Notifications",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppTab.PROFILE -> {
                            PlaceholderScreen(
                                title = "Profile",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Temporary placeholder until the actual screens are implemented
@androidx.compose.runtime.Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier
) { }