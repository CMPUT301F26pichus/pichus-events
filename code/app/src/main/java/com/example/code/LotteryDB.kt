package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Stores lotteries in Firestore, keyed by lottery ID.
 * Keeps a local copy of every lottery so lookups need no suspend functions.
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
     * Finds every lottery an owner runs, from the local copy.
     *
     * @param owner campsite owner to look up
     * @return the owner's lotteries, empty if none
     */
    fun viewOwnLotteries(owner: User): List<Lottery> {
        return _lotteries.filter { it.ownerID == owner.userID }
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
