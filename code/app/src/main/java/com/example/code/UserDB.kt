package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Stores all users (admin, entrants, campsite owner) in FireBase, and allows retrieval, update, addition, and deletion functionalities
 */
class UserDB {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    private val _users = mutableStateListOf<User>()

    /**
     * Returns local users list
     *
     * @return list of all users
     */
    val users: List<User> // can use this for user sign in, as well as allowing admin to delete profiles
        get() = _users

    /**
     * Adds a user to the database
     *
     * Validates entries before adding to database
     *
     * @param name name of the user
     * @param email email of the user
     * @param phoneNum phone number of the user
     * @param role the role of the user (entrant, campsite owner, or admin)
     * @return Unit
     */
    fun addUser(name: String, email: String, phoneNum: String, role: String) {
        if (name.isEmpty() || email.isEmpty() || (role != "Campsite Owner" && role != "Entrant") || phoneNum.length != 10) {
            return
        }
        val newUser = User(name, (0..1000000).random(), email, phoneNum.toLong(), role) // is there a better solution for generating user ids?
        usersRef.document(newUser.userID.toString()).set(newUser)
    }

    /**
     * Deletes a user from the database
     *
     * @param user user wanting to delete their profile
     * @return Unit
     */
    fun deleteUser(user: User) {
        usersRef.document(user.userID.toString()).delete()
    }

    /**
     * Updates user's name in the database
     *
     * Validates name before updating database
     *
     * @param user user wanting to update their name
     * @param newName the new name they want to use
     * @return Unit
     */
    fun updateName(user: User, newName: String) {
        if (newName.isNotEmpty()) {
            val updatedProfile =
                User(newName, user.userID, user.email, user.phoneNum, user.role)
            usersRef.document(user.userID.toString()).set(updatedProfile)
        }
    }

    /**
     * Updates user's email in the database
     *
     * Validates email before updating database
     *
     * @param user user wanting to update their email
     * @param newEmail the new email they want to use
     * @return Unit
     */
    fun updateEmail(user: User, newEmail: String) {
        if (newEmail.isNotEmpty()) {
            val updatedProfile =
                User(user.name, user.userID, newEmail, user.phoneNum, user.role)
            usersRef.document(user.userID.toString()).set(updatedProfile)
        }
    }

    /**
     * Updates user's email in the database
     *
     * Validates phone number before updating database
     * @param user user wanting to update their phone number
     * @param newNum the new phone number they want to use
     * @return Unit
     */
    fun updatePhoneNum(user: User, newNum: String) {
        if (newNum.length == 10) {
            val updatedProfile = User(user.name, user.userID, user.email, newNum.toLong(), user.role)
            usersRef.document(user.userID.toString()).set(updatedProfile)
        }
    }

    /**
     * Updates local _users list
     */
    init {
        usersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _users.clear()
            snapshot?.documents?.forEach { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _users.add(user)
                }
            }
        }
    }
}