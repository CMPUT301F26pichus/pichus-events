package com.example.code

import com.google.firebase.Timestamp


/**
 * Represents all campsites' availabilities
 *
 * A reservation has a campsiteID, and a list of available dates.
 *
 * Outstanding Issues: N/A
 * @property campsiteID ID of the associated campsite
 * @property dates list of available dates
 */
class Availability(val campsiteID: String = "",
                   val dates: MutableList<Timestamp>? = null)

