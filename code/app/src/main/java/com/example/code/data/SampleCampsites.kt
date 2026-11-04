package com.example.code.data

import com.example.code.Campsite

// Temporary placeholder campsites until Firestore is figured out, now used for screen previews

object SampleCampsites {
    fun all(): List<Campsite> = listOf(
        Campsite(
            name = "Rainbow Valley",
            locationName = "Edmonton, AB",
            occupancy = 8,
            price = 45.0,
            rating = 4.5,
            policy = "No fires after 11 pm.",
            ownerID = 1
        ),
        Campsite(
            name = "Shakers Acres",
            locationName = "Edmonton, AB",
            occupancy = 4,
            price = 60.0,
            rating = 4.8,
            policy = "Quiet hours after 11 pm.",
            ownerID = 1
        ),
        Campsite(
            name = "Winterburn",
            locationName = "Edmonton, AB",
            occupancy = 5,
            price = 50.0,
            rating = 4.6,
            policy = "No fires during fire bans.",
            ownerID = 1
        ),
        Campsite(
            name = "Elk Island Retreat",
            locationName = "Edmonton, AB",
            occupancy = 6,
            price = 40.0,
            rating = 4.3,
            policy = "No fireworks.",
            ownerID = 1
        ),
        Campsite(
            name = "Astotin Lake",
            locationName = "Edmonton, AB",
            occupancy = 4,
            price = 50.0,
            rating = 4.6,
            policy = "No glass near lake.",
            ownerID = 1
        ),
        Campsite(
            name = "Tunnel Mountain",
            locationName = "Banff, AB",
            occupancy = 6,
            price = 70.0,
            rating = 4.9,
            policy = "Keep food locked up overnight.",
            ownerID = 2
        ),
        Campsite(
            name = "Whistlers",
            locationName = "Jasper, AB",
            occupancy = 8,
            price = 65.0,
            rating = 4.7,
            policy = "No generators. Quiet hours after 10 pm.",
            ownerID = 1
        ),
        Campsite(
            name = "Spray Valley",
            locationName = "Canmore, AB",
            occupancy = 5,
            price = 55.0,
            rating = 4.4,
            policy = "Pets welcome on leash.",
            ownerID = 4
        ),
        Campsite(
            name = "Dinosaur Badlands",
            locationName = "Drumheller, AB",
            occupancy = 6,
            price = 40.0,
            rating = 4.2,
            policy = "Stay on marked trails. No fossil collecting.",
            ownerID = 4
        ),
    )
}
