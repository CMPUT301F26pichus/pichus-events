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
import com.example.code.screens.OwnerCampsiteDetailsScreen
import com.example.code.screens.OwnerHomeScreen
import com.example.code.screens.RegisterCampsiteScreen
import com.example.code.screens.UpdateCampsiteScreen
import com.example.code.screens.AdminToolsScreen
import com.example.code.screens.BrowseProfilesScreen
import com.example.code.screens.ProfileScreen
import com.example.code.ui.theme.PichusTheme
import com.example.code.components.AppTab
import com.example.code.components.BottomNavigationBar
import com.example.code.components.AdminPage
import com.example.code.data.UserSession
import com.example.code.screens.EditProfileScreen
import com.example.code.screens.PolicyScreen
import com.example.code.screens.ReservationDetailsScreen
import com.example.code.screens.ReservationsScreen
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
                // Connect to Firestore, listen to waiting list changes
                val waitingListDB = remember {WaitingListDB()}
                // Connect to Firestore, listen for reservation changes
                val reservationDB = remember {ReservationDB()}
                //Track resrvations
                var selectedReservationId by remember { mutableStateOf<String?>(null) }

                // Remember which bottom navigation tab is selected
                var selectedTab by rememberSaveable { mutableStateOf(AppTab.SEARCH) }

                // See which campsite is currently opened, null means we're still on browse screen (no campsite opened yet)
                var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }

                // Controls whether the campsite registration form is open
                var isRegisteringCampsite by rememberSaveable { mutableStateOf(false) }

                // Controls whether the campsite update form is open
                var isUpdatingCampsite by rememberSaveable { mutableStateOf(false) }

                // Tracks which admin screen is currently open
                var adminPage by rememberSaveable { mutableStateOf<AdminPage?>(null) }

                // True until user enters ID
                var showWelcome by remember { mutableStateOf(true) }

                // True while policy screen is open
                var showPolicy by rememberSaveable { mutableStateOf(false) }

                // See when edit profile screen is open
                var editingProfile by rememberSaveable { mutableStateOf(false) }


                // Back button goes to previous page browse screen instead of closing the app
                BackHandler(enabled = adminPage != null || selectedCampsite != null || isRegisteringCampsite || isUpdatingCampsite) {
                    when {
                        adminPage != null -> {
                            adminPage = when (adminPage) {
                                AdminPage.MENU -> null
                                AdminPage.PROFILES, AdminPage.IMAGES -> AdminPage.MENU
                                null -> null
                            }
                        }

                        isRegisteringCampsite -> {
                            isRegisteringCampsite = false
                        }

                        isUpdatingCampsite -> {
                            isUpdatingCampsite = false
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
                                    isRegisteringCampsite = false
                                    isUpdatingCampsite = false
                                    selectedCampsite = null
                                }
                            )
                        }
                    }

                ) { innerPadding ->
                    // Signed-in user (found by id entered on welcome screen) - put here so it updates after signing in
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
                        // Look up currently logged-in user profile (or fallback to owner matching UserSession.currentUserId)
                        val currentUser = userDB.users.find { it.userID == UserSession.currentUserId }
                            ?: User(name = "Owner", userID = UserSession.currentUserId, role = "Campsite Owner")

                        // Decide which screen to display
                        when (selectedTab) {

                            AppTab.SEARCH -> {

                                if (isRegisteringCampsite) {
                                    RegisterCampsiteScreen(
                                        currentUserId = UserSession.currentUserId,
                                        onCampsiteRegistered = { newCampsite ->
                                            campsiteDB.addCampsite(newCampsite)
                                            isRegisteringCampsite = false
                                        },
                                        onBack = { isRegisteringCampsite = false },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                } else if (isUpdatingCampsite && selectedCampsite != null) {
                                    UpdateCampsiteScreen(
                                        campsite = selectedCampsite!!,
                                        onCampsiteUpdated = { updatedCampsite ->
                                            campsiteDB.addCampsite(updatedCampsite)
                                            isUpdatingCampsite = false
                                            selectedCampsite = null
                                        },
                                        onBack = { isUpdatingCampsite = false },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                } else if (selectedCampsite == null) {
                                    if (currentUser.role.equals("Campsite Owner", ignoreCase = true)) {
                                        OwnerHomeScreen(
                                            campsites = campsiteDB.campsites.filter { it.ownerID == currentUser.userID },
                                            onCampsiteClick = { selectedCampsite = it },
                                            onRegisterClick = { isRegisteringCampsite = true },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    } else {
                                        BrowseScreen(
                                            campsites = campsiteDB.campsites,
                                            onCampsiteClick = { selectedCampsite = it },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }
                                } else {
                                    val campsite = selectedCampsite!!
                                    if (currentUser.role.equals("Campsite Owner", ignoreCase = true)) {
                                        OwnerCampsiteDetailsScreen(
                                            campsite = campsite,
                                            onBack = { selectedCampsite = null },
                                            onUpdateClick = { isUpdatingCampsite = true },
                                            bookedReservations = reservationDB.reservations.filter { it.campsiteID == campsite.id && it.status == "ACTIVE" },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    } else {
                                        val isOnWaitingList = waitingListDB
                                            .getWaitingList(campsite.id)
                                            ?.contains(UserSession.currentUserId) == true
                                        CampsiteDetailsScreen(
                                            campsite = selectedCampsite!!,
                                            onBack = { selectedCampsite = null },
                                            isOnWaitingList = isOnWaitingList,
                                            onJoinLottery = {
                                                waitingListDB.joinWaitingList(
                                                    campsite.id,
                                                    UserSession.currentUserId
                                                )
                                            },
                                            onLeaveWaitlist = {
                                                waitingListDB.leaveWaitingList(
                                                    campsite.id,
                                                    UserSession.currentUserId
                                                )
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }
                                   }
                            }

                            AppTab.RESERVATIONS -> {
                                if (selectedReservationId !=null){
                                    val currentUser = User("", UserSession.currentUserId, "", 0L, "Entrant")
                                    val allUserReservations = reservationDB.viewActiveReservations(currentUser) +
                                            reservationDB.viewPastReservations(currentUser) +
                                            reservationDB.viewCancelledReservations(currentUser)

                                    val reservation = allUserReservations.find { it.campsiteID == selectedReservationId }
                                    val campsite = campsiteDB.campsites.find { it.id == selectedReservationId }

                                    if (reservation != null && campsite != null) {
                                        ReservationDetailsScreen(
                                            reservation = reservation,
                                            campsite = campsite,
                                            onBack = { selectedReservationId = null },
                                            onCancelReservation = {
                                                reservationDB.cancelReservation(reservation)
                                                selectedReservationId = null // Navigate back to list after cancelling
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    } else {
                                        // Fallback if data is missing
                                        selectedReservationId = null
                                    }
                                } else {
                                    ReservationsScreen(
                                        currentUser = currentUser,
                                        reservationDB = reservationDB,
                                        campsiteDB = campsiteDB,
                                        onReservationClick = { campsiteId ->
                                            selectedReservationId = campsiteId
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            AppTab.NOTIFICATIONS -> {
                                PlaceholderScreen()
                            }

                            AppTab.PROFILE -> {
                                when (adminPage) {
                                    null -> {
                                        if (editingProfile) {
                                            EditProfileScreen(
                                                user = currentUser,
                                                userDB = userDB,
                                                onBack = { editingProfile = false },
                                                onDeleted = {
                                                    editingProfile = false
                                                    selectedTab = AppTab.SEARCH
                                                    UserSession.currentUserId = -1
                                                    showWelcome = true // Deleted, go back to welcome screen
                                                },
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                        } else {
                                            ProfileScreen(
                                                user = currentUser,
                                                onEditProfile = { editingProfile = true }, // opens edit profile
                                                onAdminTools = { adminPage = AdminPage.MENU },
                                                onLogout = {
                                                    // Clear the current user session
                                                    UserSession.currentUserId = -1

                                                    // Reset navigation state
                                                    selectedTab = AppTab.SEARCH
                                                    selectedCampsite = null
                                                    isRegisteringCampsite = false
                                                    adminPage = null
                                                    editingProfile = false
                                                    showPolicy = false

                                                    // Return to the welcome screen
                                                    showWelcome = true
                                                },
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                        }
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
                                            onDeleteUser = { user ->
                                                userDB.deleteUser(user)
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