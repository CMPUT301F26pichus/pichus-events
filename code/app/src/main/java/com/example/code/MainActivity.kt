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
import com.example.code.screens.AdminToolsScreen
import com.example.code.screens.BrowseProfilesScreen
import com.example.code.screens.ProfileScreen
import com.example.code.ui.theme.PichusTheme
import com.example.code.components.AppTab
import com.example.code.components.BottomNavigationBar
import com.example.code.components.AdminPage
import com.example.code.data.UserSession
import com.example.code.screens.PolicyScreen
import com.example.code.screens.WelcomeScreen

/**
 * App entry point. Wraps all content in the pichus theme.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PichusTheme {
                // Connects to Firestore, keeps watching for campsite changes
                val campsiteDB = remember { CampsiteDB() }
                // Connect to Firestore, listens for user profile changes
                val userDB = remember { UserDB() }

                // Remember which bottom navigation tab is selected
                var selectedTab by rememberSaveable { mutableStateOf(AppTab.SEARCH) }

                // See which campsite is currently opened, null means we're still on browse screen (no campsite opened yet)
                var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }

                // Tracks which admin screen is currently open
                var adminPage by rememberSaveable { mutableStateOf<AdminPage?>(null) }

                // True until user enters ID
                var showWelcome by remember { mutableStateOf(true) }

                // True while policy screen is open
                var showPolicy by rememberSaveable { mutableStateOf(false) }


                // Back button goes to previous page browse screen instead of closing the app
                BackHandler(enabled = adminPage != null || selectedCampsite != null) {
                    when {
                        adminPage != null -> {
                            adminPage = when (adminPage) {
                                AdminPage.MENU -> null
                                AdminPage.PROFILES, AdminPage.IMAGES -> AdminPage.MENU
                                null -> null
                            }
                        }

                        selectedCampsite != null -> {
                            selectedCampsite = null
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    // Bottom navigation bar
                    bottomBar = {
                        if (!showWelcome) {
                            BottomNavigationBar(
                                selectedTab = selectedTab,
                                onTabSelected = { tab ->
                                    selectedTab = tab
                                    adminPage = null
                                }
                            )
                        }
                    }

                ) { innerPadding ->
                    if (showPolicy) {
                        PolicyScreen(
                            onBack = { showPolicy = false }, // Back to previous
                            modifier = Modifier.padding(innerPadding)
                        )
                    }  else if (showWelcome) {
                        // First screen, user enters ID
                        WelcomeScreen(
                            users = userDB.users, // Registered users to check id against
                            onContinue = { id ->
                                UserSession.currentUserId = id // Remember who user is
                                showWelcome = false // Go into app
                            },
                            onPolicyClick = { showPolicy = true }, // Opens policy and guidelines screen
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        // Decide which screen to display
                        when (selectedTab) {

                            AppTab.SEARCH -> {

                                if (selectedCampsite == null) {
                                    BrowseScreen(
                                        campsites = campsiteDB.campsites,
                                        onCampsiteClick = { selectedCampsite = it },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                } else {
                                    CampsiteDetailsScreen(
                                        campsite = selectedCampsite!!,
                                        onBack = { selectedCampsite = null },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            AppTab.RESERVATIONS -> {
                                PlaceholderScreen()
                            }

                            AppTab.NOTIFICATIONS -> {
                                PlaceholderScreen()
                            }

                            AppTab.PROFILE -> {
                                when (adminPage) {
                                    null -> {
                                        ProfileScreen(
                                            user = User(
                                                name = "Pichu",
                                                userID = 1001,
                                                email = "pichu@gmail.com",
                                                phoneNum = 7801234567,
                                                role = "Admin"
                                            ),
                                            onEditProfile = {
                                                // Implement Edit Profile later
                                            },
                                            onAdminTools = {
                                                adminPage = AdminPage.MENU
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    AdminPage.MENU -> {
                                        AdminToolsScreen(
                                            onBrowseProfiles = { adminPage = AdminPage.PROFILES },
                                            onBrowseImages = { adminPage = AdminPage.IMAGES },
                                            onBack = { adminPage = null },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    AdminPage.PROFILES -> {
                                        BrowseProfilesScreen(
                                            users = userDB.users,
                                            onBack = {
                                                adminPage = AdminPage.MENU
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    AdminPage.IMAGES -> {
                                        PlaceholderScreen()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Temporary placeholder until the actual screens are implemented
@androidx.compose.runtime.Composable
private fun PlaceholderScreen() { }