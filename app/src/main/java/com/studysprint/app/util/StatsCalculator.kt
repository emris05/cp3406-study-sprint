package com.studysprint.app.util

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Pure statistics calculations — no Android deps, fully unit-testable.
 */
object StatsCalculator {

    private val DAY_MILLIS = TimeUnit.DAYS.toMillis(1)

    /**
     * Compute the current consecutive-day streak from a list of session
     * completion timestamps. A day "counts" if it has at least one session.
     *
     * The streak includes today if there's been a session today, otherwise it
     * starts from yesterday (so a streak isn't broken the moment midnight hits).
     *
     * @param timestamps session completion times, in epoch millis
     * @param nowMillis the reference "now" (injected for testability)
     */
    fun calculateStreak(timestamps: List<Long>, nowMillis: Long): Int {
        if (timestamps.isEmpty()) return 0

        // Normalise each timestamp to its day (midnight) and dedupe.
        val activeDays = timestamps
            .map { startOfDay(it) }
            .toSet()

        val today = startOfDay(nowMillis)
        val yesterday = today - DAY_MILLIS

        // Streak can start from today OR yesterday (grace for "haven't studied yet today").
        val streakStart = when {
            today in activeDays -> today
            yesterday in activeDays -> yesterday
            else -> return 0
        }

        var streak = 0
        var cursor = streakStart
        while (cursor in activeDays) {
            streak++
            cursor -= DAY_MILLIS
        }
        return streak
    }

    /** Collapse a timestamp to midnight of its day, in the default timezone. */
    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
