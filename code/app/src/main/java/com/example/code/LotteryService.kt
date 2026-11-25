package com.example.code

import com.google.firebase.Timestamp

/**
 * Runs the lottery draw once registration ends and tells entrants the result (US 02.05.01).
 *
 * Outstanding Issues: N/A
 *
 * @property lotteryDB where lotteries are saved
 * @property reservationDB where winners' reservations will go (US 01.04.01, not built yet)
 * @property notificationService sends win and lose notifications
 */
class LotteryService(
    private val lotteryDB: LotteryDB,
    private val reservationDB: ReservationDB,
    private val notificationService: NotificationService
) {
    /**
     * Picks winners at random from the lottery's waiting list.
     * Picks numWinners entrants, or everyone if fewer joined. Does nothing if already COMPLETE.
     *
     * @param lottery lottery to draw
     * @return the winners
     */
    fun drawWinners(lottery: Lottery): List<Int> {
        if (lottery.status == LotteryStatus.COMPLETE) {
            return lottery.winnerIDs
        }
        lottery.winnerIDs = lottery.waitingList.entrantIDs.shuffled().take(lottery.numWinners.coerceAtLeast(0))
        lottery.status = LotteryStatus.COMPLETE
        return lottery.winnerIDs
    }

    /**
     * Draws the lottery if registration has ended, saves it, and notifies winners and losers.
     * Does nothing while registration is still running or if the draw already ran.
     *
     * @param lottery lottery to check
     * @param now current time, defaults to now (passed in by tests)
     */
    fun closeIfDue(lottery: Lottery, now: Timestamp = Timestamp.now()) {
        if (lottery.status != LotteryStatus.OPEN || now <= lottery.registrationEnd) {
            return
        }
        val winners = drawWinners(lottery)
        val losers = lottery.waitingList.entrantIDs.filter { it !in winners }
        lotteryDB.updateLottery(lottery)
        notificationService.sendToAll(winners, NotificationType.LOTTERY_WON,
            "You won the lottery! Pick your dates in Reservations.")
        notificationService.sendToAll(losers, NotificationType.LOTTERY_LOST,
            "You were not picked in the lottery this time.")
    }
}
