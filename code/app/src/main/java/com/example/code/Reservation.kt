package com.example.code

import com.google.firebase.Timestamp

/**
 * Represents all reservations (past, active, canceled)
 *
 * A reservation has a customer (entrant) ID, owner ID, campsite ID, start and end dates, guest count, and status.
 *
 * @property customerID ID of the entrant making the reservation
 * @property ownerID ID of the campsite owner
 * @property campsiteID ID of the campsite
 * @property startDate when the booking starts
 * @property endDate when the booking ends
 * @property guestCount number of guests that are going to use the campsite
 * @property status one of 'ACTIVE' or 'CANCELLED' to organize reservations
 */
class Reservation(val customerID: Int = -1,
                  val ownerID: Int = -1,
                  val campsiteID: String = "",
                  val startDate: Timestamp? = null,
                  val endDate: Timestamp? = null,
                  var guestCount: Int = -1,
                  var status: String = "ACTIVE",
                  var rating: Int = 0)