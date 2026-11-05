package com.example.code

/**
 * Represents all users (admin, entrants, campsite owner) that use the campsite booking app
 *
 * A user has a name, user ID, email, phone number and role.
 *
 * @property name the name of the user
 * @property userID used to connect the user to other classes (ex. reservations, campsites, messages, etc.)
 * @property email email address
 * @property phoneNum phone number
 * @property role used to display the correct views and allow the user to perform the relevant operations
 *
 */
class User(val name: String = "",
           val userID: Int = -1,
           var email: String = "",
           var phoneNum: Long = -1,
           var role: String = "")

