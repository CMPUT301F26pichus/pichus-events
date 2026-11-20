package com.example.code

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

// Covers the Lottery model: registration period and starting values (US 02.04.01, US 02.05.01).
class LotteryTest {
    private fun at(millis: Long) = Timestamp(Date(millis))

    /** Registration runs from 1000 to 2000. */
    private fun lottery() = Lottery(
        lotteryID = "1_Lakeview",
        ownerID = 1,
        campsiteIDs = listOf("1_Lakeview"),
        registrationStart = at(1000),
        registrationEnd = at(2000)
    )

    @Test
    fun openDuringRegistration() {
        assertTrue(lottery().isRegistrationOpen(at(1500)))
    }

    @Test
    fun openOnStartAndEndExactly() {
        assertTrue(lottery().isRegistrationOpen(at(1000)))
        assertTrue(lottery().isRegistrationOpen(at(2000)))
    }

    @Test
    fun closedBeforeStart() {
        assertFalse(lottery().isRegistrationOpen(at(999)))
    }

    @Test
    fun closedAfterEnd() {
        assertFalse(lottery().isRegistrationOpen(at(2001)))
    }

    @Test
    fun closedOnceComplete() {
        val lottery = lottery()
        lottery.status = LotteryStatus.COMPLETE
        assertFalse(lottery.isRegistrationOpen(at(1500)))
    }

    @Test
    fun newLotteryStartsOpenWithOneWinnerAndNoWinners() {
        val lottery = Lottery()
        assertEquals(LotteryStatus.OPEN, lottery.status)
        assertEquals(1, lottery.numWinners)
        assertTrue(lottery.winnerIDs.isEmpty())
        assertTrue(lottery.waitingList.entrantIDs.isEmpty())
    }

    @Test
    fun coversSeveralCampsites() {
        val lottery = Lottery(campsiteIDs = listOf("1_Lakeview", "1_Ridge"))
        assertEquals(2, lottery.campsiteIDs.size)
    }
}
