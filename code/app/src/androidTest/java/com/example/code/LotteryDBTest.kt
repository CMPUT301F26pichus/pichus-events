// Instrumented tests for LotteryDB against Firestore. Run on the emulator.
// Each test uses a fake owner ID and deletes the lottery it made.
package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LotteryDBTest {
    private val lotteryDB = LotteryDB()
    private val owner = User(name = "Test Owner", userID = fakeID(), role = "Campsite Owner")
    private val lotteryID = "test_${owner.userID}_Lakeview"

    private fun local(): Lottery? = lotteryDB.viewOwnLotteries(owner).find { it.lotteryID == lotteryID }

    @After
    fun cleanUp() {
        Firebase.firestore.collection("lotteries").document(lotteryID).delete()
    }

    @Test
    fun addLotteryShowsForOwner() {
        lotteryDB.addLottery(Lottery(lotteryID = lotteryID, ownerID = owner.userID, campsiteIDs = listOf(lotteryID)))
        waitUntil { local() != null }
        assertNotNull(local())
        assertEquals(LotteryStatus.OPEN, local()!!.status)
    }

    @Test
    fun otherOwnerDoesNotSeeLottery() {
        lotteryDB.addLottery(Lottery(lotteryID = lotteryID, ownerID = owner.userID))
        waitUntil { local() != null }
        val other = User(userID = fakeID())
        assertTrue(lotteryDB.viewOwnLotteries(other).none { it.lotteryID == lotteryID })
    }

    @Test
    fun updateLotterySavesWinners() {
        val lottery = Lottery(lotteryID = lotteryID, ownerID = owner.userID)
        lotteryDB.addLottery(lottery)
        waitUntil { local() != null }
        lottery.winnerIDs = listOf(5, 6)
        lottery.status = LotteryStatus.COMPLETE
        lotteryDB.updateLottery(lottery)
        waitUntil { local()?.status == LotteryStatus.COMPLETE }
        assertEquals(listOf(5, 6), local()!!.winnerIDs)
    }

    @Test
    fun waitingListIsSavedInsideLottery() {
        val waitingList = WaitingList(campsiteID = lotteryID, entrantIDs = mutableListOf(1, 2, 3))
        lotteryDB.addLottery(Lottery(lotteryID = lotteryID, ownerID = owner.userID, waitingList = waitingList))
        waitUntil { local() != null }
        assertEquals(listOf(1, 2, 3), local()!!.waitingList.entrantIDs)
    }
}
