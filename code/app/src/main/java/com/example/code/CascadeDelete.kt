package com.example.code

/**
 * Cleans up everything tied to a campsite or a profile, then deletes it.
 *
 * Campsite: active reservations cancelled, taken out of its lottery (its waiting list goes too),
 * availability and images deleted.
 * Profile: removed from every waiting list, active reservations cancelled (as guest or owner),
 * profile picture deleted, every campsite they own removed like above.
 */
class CascadeDelete(
    private val userDB: UserDB,
    private val campsiteDB: CampsiteDB,
    private val reservationDB: ReservationDB,
    private val waitingListDB: WaitingListDB,
    private val lotteryDB: LotteryDB,
    private val availabilityDB: AvailabilityDB,
    private val imageDB: ImageDB
) {
    /**
     * Deletes a campsite and everything that points to it.
     *
     * @param campsite campsite being deleted
     * @param onSuccess runs once the campsite itself is deleted
     * @param onFailure runs if the images or the campsite could not be deleted
     */
    fun deleteCampsite(
        campsite: Campsite,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        reservationDB.cancelCampsiteReservations(campsite)
        lotteryDB.removeCampsite(campsite.id, waitingListDB)
        availabilityDB.deleteAvailability(campsite.id)
        imageDB.deleteCampsiteImages(
            campsiteId = campsite.id,
            onSuccess = { campsiteDB.deleteCampsite(campsite, onSuccess, onFailure) },
            onFailure = onFailure
        )
    }

    /**
     * Deletes a profile and everything that points to it.
     *
     * @param user profile being deleted
     */
    fun deleteUser(user: User) {
        waitingListDB.removeEntrantFromAll(user.userID)
        reservationDB.cancelAll(user)
        campsiteDB.campsites.filter { it.ownerID == user.userID }.forEach { deleteCampsite(it) }
        if (user.profilePictureId.isNotEmpty()) imageDB.deleteImage(user.profilePictureId)
        userDB.deleteUser(user)
    }
}
