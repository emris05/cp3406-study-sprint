package com.studysprint.app.spacedrepetition

import kotlin.math.roundToInt

/**
 * Quality rating for a single flashcard review, per the SM-2 specification.
 * 0 = complete blackout, 5 = perfect recall. We expose four user-facing
 * buttons (Again/Hard/OK/Easy) and map them onto this 0-5 scale.
 */
enum class ReviewQuality(val sm2Value: Int) {
    Again(0),  // complete blackout, must see again today
    Hard(3),   // recalled with serious difficulty
    OK(4),     // correct response after a hesitation
    Easy(5);   // perfect, instantaneous recall

    companion object {
        /** True when the response was good enough to advance the interval (q >= 3). */
        fun isCorrect(q: ReviewQuality) = q.sm2Value >= 3
    }
}

/**
 * The scheduling state attached to a flashcard, updated after each review.
 *
 * @param repetitions consecutive correct reviews (resets to 0 on a fail)
 * @param easeFactor multiplicative difficulty factor, starts at 2.5, floored at 1.3
 * @param intervalDays how many days until the card is shown again
 * @param dueEpochMillis wall-clock time when the card becomes due
 */
data class CardSchedule(
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val dueEpochMillis: Long = 0L,
)

/**
 * A pure implementation of the SM-2 spaced-repetition algorithm (the one Anki
 * is based on). No Android, no I/O, no time-of-day side effects — the caller
 * passes in [nowMillis], which makes every rule deterministic and unit-testable.
 *
 * Reference: https://www.supermemo.com/en/blog/application-of-a-computer-to-improve-the-results-obtained-in-working-with-the-supermemo-method
 */
object Sm2 {

    /**
     * Schedule the next review for a card based on the user's quality rating.
     *
     * Rules (SM-2):
     *  - If the response was incorrect (q < 3): reset repetitions to 0, interval = 1 day.
     *  - Otherwise: interval grows with the number of consecutive reps
     *      • rep 0 → 1 day
     *      • rep 1 → 6 days
     *      • rep ≥ 2 → interval = round(previousInterval × easeFactor)
     *  - Ease factor is adjusted by (q - 3) and clamped to a 1.3 minimum.
     */
    fun review(
        current: CardSchedule,
        quality: ReviewQuality,
        nowMillis: Long,
    ): CardSchedule {
        val q = quality.sm2Value

        val newEase = (current.easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
            .coerceAtLeast(1.3)

        val newRepetitions: Int
        val newInterval: Int

        if (!ReviewQuality.isCorrect(quality)) {
            // Failed recall — start over, see it again tomorrow.
            newRepetitions = 0
            newInterval = 1
        } else {
            newRepetitions = current.repetitions + 1
            newInterval = when (newRepetitions) {
                1 -> 1
                2 -> 6
                else -> (current.intervalDays * newEase).roundToInt().coerceAtLeast(1)
            }
        }

        val dueMillis = nowMillis + newInterval.toDaysMillis()
        return CardSchedule(
            repetitions = newRepetitions,
            easeFactor = roundTo2(newEase),
            intervalDays = newInterval,
            dueEpochMillis = dueMillis,
        )
    }

    /**
     * Convenience: build the initial schedule for a brand-new card.
     * Due immediately (epoch 0) so it surfaces in the first review session.
     */
    fun initialSchedule(): CardSchedule = CardSchedule(
        repetitions = 0,
        easeFactor = 2.5,
        intervalDays = 0,
        dueEpochMillis = 0L,
    )

    /** Is a card due for review at [nowMillis]? A never-reviewed card is always due. */
    fun isDue(schedule: CardSchedule, nowMillis: Long): Boolean =
        schedule.dueEpochMillis <= nowMillis

    private fun roundTo2(value: Double): Double =
        (value * 100).roundToInt() / 100.0

    private fun Int.toDaysMillis(): Long = this * 24L * 60L * 60L * 1000L
}
