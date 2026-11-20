// Instrumented tests for LotteryService against Firestore. Run on the emulator.
// Each test uses fake entrant IDs and deletes the lottery and notifications it made.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class LotteryServiceTest {
    private val lotteryDB = LotteryDB()
    private val service = LotteryService(lotteryDB, ReservationDB(), NotificationService(NotificationDB(), UserDB()))
    private val owner = User(userID = fakeID(), role = "Campsite Owner")
    private val entrants = List(4) { fakeID() }
    private val lotteryID = "test_${owner.userID}_Lakeview"

    private fun at(millis: Long) = Timestamp(Date(millis))

    /** Registration ended a minute ago, four entrants joined. */
    private fun closedLottery(numWinners: Int = 1) = Lottery(
        lotteryID = lotteryID,
        ownerID = owner.userID,
        campsiteIDs = listOf(lotteryID),
        registrationStart = at(System.currentTimeMillis() - 120_000),
        registrationEnd = at(System.currentTimeMillis() - 60_000),
        numWinners = numWinners,
        waitingList = WaitingList(campsiteID = lotteryID, entrantIDs = entrants.toMutableList())
    )

    private fun saved(): Lottery? = lotteryDB.viewOwnLotteries(owner).find { it.lotteryID == lotteryID }

    @After
    fun cleanUp() {
        Firebase.firestore.collection("lotteries").document(lotteryID).delete()
        entrants.forEach { deleteNotificationsFor(it) }
    }

    @Test
    fun drawPicksNumWinnersFromWaitingList() {
        val lottery = closedLottery(numWinners = 2)
        val winners = service.drawWinners(lottery)
        assertEquals(2, winners.size)
        assertEquals(2, winners.toSet().size)
        assertTrue(entrants.containsAll(winners))
        assertEquals(LotteryStatus.COMPLETE, lottery.status)
    }

    @Test
    fun drawTakesEveryoneWhenFewerThanNumWinners() {
        assertEquals(4, service.drawWinners(closedLottery(numWinners = 10)).size)
    }

    @Test
    fun drawOnlyRunsOnce() {
        val lottery = closedLottery()
        val first = service.drawWinners(lottery)
        assertEquals(first, service.drawWinners(lottery))
    }

    @Test
    fun closeIfDueWaitsForRegistrationToEnd() {
        val lottery = closedLottery()
        lottery.registrationEnd = at(System.currentTimeMillis() + 60_000)
        service.closeIfDue(lottery)
        assertEquals(LotteryStatus.OPEN, lottery.status)
        assertTrue(lottery.winnerIDs.isEmpty())
    }

    @Test
    fun closeIfDueSavesAndNotifiesEveryone() {
        val lottery = closedLottery(numWinners = 1)
        lotteryDB.addLottery(lottery)
        waitUntil { saved() != null }
        service.closeIfDue(lottery)
        waitUntil { saved()?.status == LotteryStatus.COMPLETE }
        assertEquals(1, saved()!!.winnerIDs.size)

        waitUntil(10_000) { entrants.all { notificationsFor(it).isNotEmpty() } }
        val winner = saved()!!.winnerIDs.single()
        entrants.forEach { id ->
            val type = notificationsFor(id).single().type
            assertEquals(if (id == winner) NotificationType.LOTTERY_WON else NotificationType.LOTTERY_LOST, type)
        }
    }
}
