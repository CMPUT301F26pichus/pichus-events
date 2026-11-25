package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Stores lotteries in Firestore, keyed by lottery ID.
 * Keeps a local copy of every lottery so lookups need no suspend functions.
 *
 * Outstanding Issues: N/A
 */
class LotteryDB {
    private val db = Firebase.firestore
    private val lotteriesRef = db.collection("lotteries")

    private val _lotteries = mutableStateListOf<Lottery>()

    /**
     * Saves a new lottery.
     *
     * @param lottery lottery to save
     */
    fun addLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.lotteryID).set(lottery)
    }

    /**
     * Saves changes to a lottery, for example the drawn winners.
     *
     * @param lottery lottery with its new values
     */
    fun updateLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.lotteryID).set(lottery)
    }

    /**
     * Deletes a whole lottery and the waiting list of every campsite in it.
     *
     * @param lottery lottery to delete
     * @param waitingListDB where the campsites' waiting lists live
     */
    fun deleteLottery(lottery: Lottery, waitingListDB: WaitingListDB) {
        lottery.campsiteIDs.forEach { waitingListDB.deleteWaitingList(it) }
        lotteriesRef.document(lottery.lotteryID).delete()
    }

    /**
     * Takes one campsite out of the lottery it is in and deletes that campsite's waiting list.
     * Part 4 cascade: call this when a campsite is deleted.
     * Entrants who also joined another campsite in the lottery stay in the draw.
     * A lottery left with no campsites is deleted.
     *
     * @param campsiteID campsite being removed
     * @param waitingListDB where the campsite's waiting list lives
     * @return IDs of the lotteries the campsite was taken out of
     */
    fun removeCampsite(campsiteID: String, waitingListDB: WaitingListDB): List<String> {
        waitingListDB.deleteWaitingList(campsiteID)
        val affected = _lotteries.toList().filter { campsiteID in it.campsiteIDs }
        affected.forEach { lottery ->
            lottery.campsiteIDs = lottery.campsiteIDs - campsiteID
            if (lottery.campsiteIDs.isEmpty()) {
                lotteriesRef.document(lottery.lotteryID).delete()
            } else {
                updateLottery(lottery)
            }
        }
        return affected.map { it.lotteryID }
    }

    /**
     * Finds every lottery an owner runs, from the local copy.
     *
     * @param owner campsite owner to look up
     * @return the owner's lotteries, empty if none
     */
    fun viewOwnLotteries(owner: User): List<Lottery> {
        return _lotteries.toList().filter { it.ownerID == owner.userID }
    }

    /**
     * Updates local _lotteries list
     */
    init {
        lotteriesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _lotteries.clear()
            snapshot?.documents?.forEach { document ->
                val lottery = document.toObject(Lottery::class.java)
                if (lottery != null) {
                    _lotteries.add(lottery)
                }
            }
        }
    }
}
