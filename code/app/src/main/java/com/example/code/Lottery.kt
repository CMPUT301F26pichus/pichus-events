package com.example.code

import com.google.firebase.Timestamp

/**
 * A campsite lottery. One lottery can cover several campsites (US 02.04.02).
 * Entrants join its waiting list during registration, winners are drawn after it ends (US 02.05.01).
 *
 * Outstanding Issues: N/A
 *
 * @property lotteryID unique ID, also the document ID in Firestore
 * @property ownerID ID of the campsite owner running the lottery
 * @property campsiteIDs campsites in this lottery (same as Campsite.id)
 * @property registrationStart when entrants can start joining
 * @property registrationEnd when joining closes and the draw can run
 * @property bookingStart first date winners can book
 * @property bookingEnd last date winners can book
 * @property numWinners how many entrants get picked (US 02.04.05)
 * @property status OPEN until the draw runs, then COMPLETE
 * @property winnerIDs entrants picked by the draw, empty until then
 * @property waitingList entrants who joined this lottery
 */
class Lottery(
    var lotteryID: String = "",
    var ownerID: Int = -1,
    var campsiteIDs: List<String> = listOf(),
    var registrationStart: Timestamp = Timestamp.now(),
    var registrationEnd: Timestamp = Timestamp.now(),
    var bookingStart: Timestamp = Timestamp.now(),
    var bookingEnd: Timestamp = Timestamp.now(),
    var numWinners: Int = 1,
    var status: LotteryStatus = LotteryStatus.OPEN,
    var winnerIDs: List<Int> = listOf(),
    var waitingList: WaitingList = WaitingList()
) {
    /**
     * Checks if entrants can join right now.
     *
     * @param now current time, defaults to now (passed in by tests)
     * @return true if the lottery is OPEN and now is inside the registration period
     */
    fun isRegistrationOpen(now: Timestamp = Timestamp.now()): Boolean {
        return status == LotteryStatus.OPEN && now >= registrationStart && now <= registrationEnd
    }
}
