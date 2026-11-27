package com.example.code

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.provider.Settings
import android.widget.Toast
import com.example.code.screens.BrowseScreen
import com.example.code.screens.CampsiteDetailsScreen
import com.example.code.screens.CreateLotteryScreen
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
import com.example.code.screens.BrowseImagesScreen
import com.example.code.screens.EditProfileScreen
import com.example.code.screens.NotificationsScreen
import com.example.code.screens.PolicyScreen
import com.example.code.screens.ReservationDetailsScreen
import com.example.code.screens.ReservationsScreen
import com.example.code.screens.WelcomeScreen
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text


/**
 * App entry point. Wraps all content in the pichus theme.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PichusTheme {
                // Connects to Firestore, keeps watching for changes
                val campsiteDB = remember { CampsiteDB() }
                val userDB = remember { UserDB() }
                val imageDB = remember { ImageDB() }
                val waitingListDB = remember {WaitingListDB()}
                val reservationDB = remember {ReservationDB()}
                val reviewDB = remember { ReviewDB() }
                val availabilityDB = remember {AvailabilityDB()}
                val lotteryDB = remember { LotteryDB() }
                val notificationDB = remember { NotificationDB() }
                val notificationService = remember { NotificationService(notificationDB, userDB) }
                val lotteryService = remember { LotteryService(lotteryDB, reservationDB, notificationService) }
                val messageDB = remember { MessageDB() }
                //Track reservations
                var selectedReservationId by remember { mutableStateOf<String?>(null) }

                // Remember which bottom navigation tab is selected
                var selectedTab by rememberSaveable { mutableStateOf(AppTab.SEARCH) }

                // See which campsite is currently opened, null means we're still on browse screen (no campsite opened yet)
                var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }

                // Tracks whether an Admin has selected the My Campsites section.
                var isAdminViewingMyCampsites by rememberSaveable { mutableStateOf(false) }

                // Controls whether the campsite registration form is open
                var isRegisteringCampsite by rememberSaveable { mutableStateOf(false) }

                // Controls whether the campsite update form is open
                var isUpdatingCampsite by rememberSaveable { mutableStateOf(false) }

                // Controls whether the waitlist creation/edit screen is open
                var isCreatingWaitlist by rememberSaveable { mutableStateOf(false) }

                // Tracks which admin screen is currently open
                var adminPage by rememberSaveable { mutableStateOf<AdminPage?>(null) }

                // True until user enters ID
                var showWelcome by remember { mutableStateOf(true) }

                // This phone's unique Android ID, used to remember who the user is
                val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                // If this phone is already linked to a user, skip the welcome screen straight to homescreen
                val linkedUser = userDB.getUser(deviceID)

                LaunchedEffect(linkedUser) {
                    if (linkedUser != null && showWelcome) {
                        UserSession.currentUserId = linkedUser.userID
                        showWelcome = false
                    }
                }

                // True while policy screen is open
                var showPolicy by rememberSaveable { mutableStateOf(false) }

                // See when edit profile screen is open
                var editingProfile by rememberSaveable { mutableStateOf(false) }


                // Back button goes to previous page browse screen instead of closing the app
                BackHandler(
                    enabled = adminPage != null ||
                            selectedCampsite != null ||
                            isRegisteringCampsite ||
                            isUpdatingCampsite ||
                            isCreatingWaitlist ||
                            (selectedTab == AppTab.SEARCH && isAdminViewingMyCampsites)
                ) {
                    when {
                        adminPage != null -> {
                            adminPage = when (adminPage) {
                                AdminPage.MENU -> null
                                AdminPage.PROFILES, AdminPage.IMAGES -> AdminPage.MENU
                                null -> null
                            }
                        }
                        isRegisteringCampsite -> { isRegisteringCampsite = false }
                        isUpdatingCampsite -> { isUpdatingCampsite = false }
                        isCreatingWaitlist -> { isCreatingWaitlist = false }
                        selectedCampsite != null -> { selectedCampsite = null }
                        isAdminViewingMyCampsites -> { isAdminViewingMyCampsites = false }
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
                                    isCreatingWaitlist = false
                                    selectedCampsite = null
                                    isAdminViewingMyCampsites = false
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
                                userDB.users.find { it.userID == id }?.let { userDB.updateDeviceID(it, deviceID) } // Link this phone
                                showWelcome = false // Go into app
                            },
                            onPolicyClick = { showPolicy = true }, // Opens policy and guidelines screen
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        // Look up currently logged-in user profile (or fallback to owner matching UserSession.currentUserId)
                        val currentUser = userDB.users.find { it.userID == UserSession.currentUserId }
                            ?: User(name = "Owner", userID = UserSession.currentUserId, role = "Campsite Owner")

                        val isAdmin = currentUser.role.equals("Admin", ignoreCase = true)
                        val isCampsiteOwner = currentUser.role.equals("Campsite Owner", ignoreCase = true)
                        // An Admin can temporarily enter owner mode without changing their role
                        val isOwnerMode = isCampsiteOwner || (isAdmin && isAdminViewingMyCampsites)

                        // US 02.05.01: while an owner is signed in, draw their lotteries once registration ends.
                        // The lottery draws from the pooled waiting lists of all its campsites (US 02.04.02).
                        LaunchedEffect(currentUser.userID) {
                            while (true) {
                                lotteryDB.viewOwnLotteries(currentUser).forEach { lottery ->
                                    lottery.waitingList = WaitingList(
                                        lottery.lotteryID,
                                        waitingListDB.getEntrantsFor(lottery.campsiteIDs).toMutableList()
                                    )
                                    lotteryService.closeIfDue(lottery)
                                }
                                delay(60_000)
                            }
                        }

                        // Decide which screen to display
                        when (selectedTab) {

                            AppTab.SEARCH -> {

                                if (isRegisteringCampsite) {
                                    RegisterCampsiteScreen(
                                        currentUserId = UserSession.currentUserId,
                                        imageDB = imageDB,
                                        onCampsiteRegistered = { newCampsite ->
                                            campsiteDB.addCampsite(
                                                campsite = newCampsite,
                                                onSuccess = { isRegisteringCampsite = false },
                                                onFailure = { exception ->
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Could not save campsite: ${exception.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )
                                        },
                                        onBack = { isRegisteringCampsite = false },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                } else if (isUpdatingCampsite && selectedCampsite != null) {
                                    UpdateCampsiteScreen(
                                        campsite = selectedCampsite!!,
                                        imageDB = imageDB,
                                        onCampsiteUpdated = { updatedCampsite ->
                                            campsiteDB.addCampsite(updatedCampsite)
                                            isUpdatingCampsite = false
                                            selectedCampsite = null
                                        },
                                        onBack = { isUpdatingCampsite = false },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                } else if (isCreatingWaitlist) {
                                    CreateLotteryScreen(
                                        ownerCampsites = campsiteDB.campsites.filter { it.ownerID == currentUser.userID },
                                        ownerID = currentUser.userID,
                                        existingLottery = selectedCampsite?.let { campsite ->
                                            lotteryDB.viewOwnLotteries(currentUser).find { campsite.id in it.campsiteIDs }
                                        },
                                        onSaveLottery = { lottery ->
                                            lotteryDB.addLottery(lottery)
                                            lottery.campsiteIDs.forEach { campID ->
                                                waitingListDB.addWaitingList(
                                                    WaitingList(
                                                        campsiteID = campID,
                                                        capacity = lottery.waitingList.capacity,
                                                        registrationDeadline = lottery.registrationEnd
                                                    )
                                                )
                                            }
                                            isCreatingWaitlist = false
                                            selectedCampsite = null
                                        },
                                        onBack = { isCreatingWaitlist = false },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                } else if (selectedCampsite == null) {
                                    if (isOwnerMode) {
                                        // Admins see tabs so they can switch between Browse and My Campsites.
                                        if (isAdmin) {
                                            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                                PrimaryTabRow(selectedTabIndex = 1) {
                                                    Tab(selected = false,
                                                        onClick = { isAdminViewingMyCampsites = false },
                                                        text = { Text("BROWSE") })
                                                    Tab(selected = true,
                                                        onClick = {},
                                                        text = { Text("MY CAMPSITES") })
                                                }
                                                OwnerHomeScreen(
                                                    campsites = campsiteDB.campsites.filter { it.ownerID == currentUser.userID },
                                                    onCampsiteClick = { selectedCampsite = it },
                                                    onRegisterClick = { isRegisteringCampsite = true },
                                                    onCreateLotteryClick = {
                                                        selectedCampsite = null
                                                        isCreatingWaitlist = true
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        } else {
                                            OwnerHomeScreen(
                                                campsites = campsiteDB.campsites.filter { it.ownerID == currentUser.userID },
                                                onCampsiteClick = { selectedCampsite = it },
                                                onRegisterClick = { isRegisteringCampsite = true },
                                                onCreateLotteryClick = {
                                                    selectedCampsite = null
                                                    isCreatingWaitlist = true
                                                },
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                        }
                                    } else {
                                        BrowseScreen(
                                            campsites = campsiteDB.campsites,
                                            availabilities = availabilityDB.availabilities,
                                            reservations = reservationDB.reservations,
                                            onCampsiteClick = { selectedCampsite = it },
                                            // US 01.12.01: the QR code holds the campsite ID, so open that campsite
                                            onQrScanned = { scannedId ->
                                                val scannedCampsite = campsiteDB.campsites.find { it.id == scannedId.trim() }
                                                if (scannedCampsite != null) {
                                                    selectedCampsite = scannedCampsite
                                                } else {
                                                    Toast.makeText(this@MainActivity, "No campsite found for this QR code", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            isAdmin = isAdmin,
                                            onMyCampsitesClick = { isAdminViewingMyCampsites = true },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }
                                } else {
                                    val campsite = selectedCampsite!!
                                    if (isOwnerMode) {
                                        OwnerCampsiteDetailsScreen(
                                            campsite = campsite,
                                            imageDB = imageDB,
                                            bookedReservations = reservationDB.reservations.filter { it.campsiteID == campsite.id && it.status == "ACTIVE" },
                                            waitingList = waitingListDB.getWaitingList(campsite.id),
                                            users = userDB.users,
                                            onBack = { selectedCampsite = null },
                                            onUpdateClick = { isUpdatingCampsite = true },
                                            onCreateWaitlistClick = { isCreatingWaitlist = true },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    } else {
                                        val waitlist = waitingListDB.getWaitingList(campsite.id)
                                        val isWaitlistOpen = waitlist != null
                                        val isOnWaitingList = waitlist?.contains(UserSession.currentUserId) == true
                                        CampsiteDetailsScreen(
                                            campsite = campsite,
                                            onBack = { selectedCampsite = null },
                                            isOnWaitingList = isOnWaitingList,
                                            imageDB = imageDB,
                                            isWaitlistOpen = isWaitlistOpen,
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
                                            isAdmin = isAdmin,
                                            onDeleteCampsite = {
                                                imageDB.deleteCampsiteImages( // delete all images belonging to this campsite
                                                    campsiteId = campsite.id,
                                                    onSuccess = {
                                                        campsiteDB.deleteCampsite(
                                                            campsite = campsite,
                                                            onSuccess = { selectedCampsite = null },
                                                            onFailure = { exception ->
                                                                android.util.Log.e(
                                                                    "MainActivity",
                                                                    "Failed to delete campsite",
                                                                    exception
                                                                )
                                                            }
                                                        )
                                                    },

                                                    onFailure = { exception ->
                                                        android.util.Log.e(
                                                            "MainActivity",
                                                            "Failed to delete campsite images",
                                                            exception
                                                        )
                                                    }
                                                )
                                            },
                                            onDeleteReview = { review -> reviewDB.deleteReview(review) },
                                            reviews = reviewDB.getCampsiteReviews(campsite),
                                            users = userDB.users,
                                            reservations = reservationDB.reservations.filter { it.campsiteID == campsite.id },
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
                                        val existingReview = reviewDB
                                            .getCampsiteReviews(campsite)
                                            .find { it.reviewerID == UserSession.currentUserId }
                                            ?.content

                                        ReservationDetailsScreen(
                                            reservation = reservation,
                                            campsite = campsite,
                                            userRole = currentUser.role,
                                            onBack = { selectedReservationId = null },
                                            onCancelReservation = {
                                                reservationDB.cancelReservation(reservation)
                                                selectedReservationId = null
                                            },
                                            onRate = { selectedRating ->
                                                reservationDB.rateReservation(reservation, selectedRating)

                                                // Recalculate average rating from all rated reservations for this campsite
                                                val otherRatings = reservationDB.reservations
                                                    .filter {
                                                        it.campsiteID == campsite.id &&
                                                                it.customerID != reservation.customerID &&
                                                                it.rating > 0
                                                    }
                                                    .map { it.rating }

                                                val allRatings = otherRatings + selectedRating
                                                val averageRating = Math.round(allRatings.average() * 10.0) / 10.0
                                                campsiteDB.updateRating(campsite.id, averageRating)
                                            },
                                            existingReview = existingReview,
                                            onPostReview = { content ->
                                                reviewDB.addReview(
                                                    Review(
                                                        campsiteID = campsite.id,
                                                        reviewerID = UserSession.currentUserId,
                                                        content = content
                                                    )
                                                )
                                            },
                                            onMessageOwner = { text ->
                                                // Save the message and notify the owner
                                                messageDB.sendMessage(
                                                    Message(
                                                        senderID = UserSession.currentUserId,
                                                        receiverID = campsite.ownerID,
                                                        content = text,
                                                        timestamp = Timestamp.now()
                                                    )
                                                )
                                                notificationService.sendToAll(
                                                    listOf(campsite.ownerID),
                                                    NotificationType.ENTRANT_MESSAGE,
                                                    "New message about ${campsite.name}: $text"
                                                )
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
                                // US 01.03.01, US 01.03.02: shows lottery results and other notifications
                                NotificationsScreen(
                                    notifications = notificationDB.getNotificationsFor(currentUser.userID),
                                    modifier = Modifier.padding(innerPadding)
                                )
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
                                                imageDB = imageDB,
                                                onEditProfile = { editingProfile = true },
                                                onAdminTools = { adminPage = AdminPage.MENU },
                                                onLogout = {
                                                    userDB.updateDeviceID(currentUser, "") // Unlink this phone so someone else can log in
                                                    UserSession.currentUserId = -1
                                                    selectedTab = AppTab.SEARCH
                                                    selectedCampsite = null
                                                    isAdminViewingMyCampsites = false
                                                    isRegisteringCampsite = false
                                                    adminPage = null
                                                    editingProfile = false
                                                    showPolicy = false
                                                    showWelcome = true
                                                },
                                                onProfilePictureUploaded = { imageId -> userDB.updateProfilePicture(currentUser, imageId) },
                                                onProfilePictureRemoved = { userDB.updateProfilePicture(currentUser, "") },
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
                                            imageDB = imageDB,
                                            onBack = { adminPage = AdminPage.MENU },
                                            onDeleteUser = { user -> userDB.deleteUser(user) },
                                            onDeleteProfilePicture = { user ->
                                                imageDB.deleteImage(
                                                    id = user.profilePictureId,
                                                    onSuccess = { userDB.updateProfilePicture(user, "") },
                                                    onFailure = { exception ->
                                                        android.util.Log.e(
                                                            "BrowseProfiles",
                                                            "Failed to delete profile picture",
                                                            exception
                                                        )
                                                    }
                                                )
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }
                                    AdminPage.IMAGES -> {
                                        BrowseImagesScreen(
                                            images = imageDB.images,
                                            onBack = { adminPage = AdminPage.MENU },
                                            onDeleteImage = { image ->
                                                imageDB.deleteImage(image.id)
                                            }
                                        )
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