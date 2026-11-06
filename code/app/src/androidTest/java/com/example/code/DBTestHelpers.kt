// Shared helpers for the database tests. Firestore answers asynchronously,
// so tests wait for the snapshot listener before checking anything.
package com.example.code

/** Fake IDs above the real range (UserDB picks 0..1000000), so real data is never touched. */
fun fakeID(): Int = 2_000_000 + (0..999_999).random()

/** Waits up to [timeoutMs] for [condition] to become true. */
fun waitUntil(timeoutMs: Long = 5000, condition: () -> Boolean) {
    val end = System.currentTimeMillis() + timeoutMs
    while (!condition() && System.currentTimeMillis() < end) Thread.sleep(100)
}
