package com.example.code

/**
 * Where a lottery is in its life.
 * OPEN: registration running or winners not drawn yet. COMPLETE: winners drawn (US 02.05.01).
 *
 * Outstanding Issues: N/A
 */
enum class LotteryStatus {
    OPEN,
    COMPLETE
}
