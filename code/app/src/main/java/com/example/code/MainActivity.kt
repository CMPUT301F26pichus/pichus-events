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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.code.screens.BrowseScreen
import com.example.code.screens.CampsiteDetailsScreen
import com.example.code.ui.theme.PichusTheme

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

                // See which campsite is currently opened, null means we're still on browse screen (no campsite opened yet)
                var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }

                // Back button goes to previous page browse screen instead of closing the app
                BackHandler(enabled = selectedCampsite != null) {
                    selectedCampsite = null
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val current = selectedCampsite
                    if (current == null) {
                        // No campsite picked, just show full list
                        BrowseScreen(
                            campsites = campsiteDB.campsites, // live list from firestore
                            onCampsiteClick = { selectedCampsite = it }, // Remember which one was tapped
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        // A campsite was picked, show its details
                        CampsiteDetailsScreen(
                            campsite = current,
                            onBack = { selectedCampsite = null }, // Back to the list
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}