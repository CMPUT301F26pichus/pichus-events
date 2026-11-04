package com.example.code

import android.location.Location
import android.media.Image

/**
 * Represents all campsites in the campsite booking app
 *
 * A campsite has a name, location, occupancy, price, rating, image, policy and owner ID.
 *
 * @property name the name of the campsite
 * @property location geolocation of the campsite
 * @property locationName readable location shown on cards and details
 * @property occupancy max occupancy of the campsite
 * @property price cost per night to book the campsite
 * @property rating rating (1-5) of the campsite
 * @property image promotional images for the campsite
 * @property policy rules set by the owner of the campsite
 * @property ownerID id of the campsite owner
 */
class Campsite(var name: String = "",
               var location: Location? = null,
               var locationName: String = "", // Readable location for display
               var occupancy: Int = -1,
               var price: Double = -1.0,
               var rating: Double = 0.0,
               var image: Image? = null,
               var policy: String = "",
               var ownerID: Int = -1) { // current implementation is a placeholder - needs final implementation with locations and images implemented
    val id: String = ownerID.toString() + "_" + this.name
}