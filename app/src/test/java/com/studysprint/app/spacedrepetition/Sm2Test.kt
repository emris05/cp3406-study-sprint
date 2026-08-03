package com.studysprint.app.spacedrepetition

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for the SM-2 spaced-repetition algorithm. Pure JVM, no Android.
 * Every test injects a fixed nowMillis so the intervals are deterministic.
 */
class Sm2Test {

    private val now = 1_000_000_000L

    @Test
    fun `a brand-new card is due immediately`() {
        val schedule = Sm2.initialSchedule()
        assertThat(Sm2.isDue(schedule, now)).isTrue()
    }

    @Test
    fun `failing a card resets the streak and shows it tomorrow`() {
        val reviewed = Sm2.review(Sm2.initialSchedule(), ReviewQuality.Again, now)
        assertThat(reviewed.repetitions).isEqualTo(0)
        assertThat(reviewed.intervalDays).isEqualTo(1)
        assertThat(Sm2.isDue(reviewed, now)).isFalse() // due tomorrow, not now
        assertThat(Sm2.isDue(reviewed, now + 2L * 24 * 60 * 60 * 1000)).isTrue()
    }

    @Test
    fun `first correct review schedules for tomorrow`() {
        val reviewed = Sm2.review(Sm2.initialSchedule(), ReviewQuality.OK, now)
        assertThat(reviewed.repetitions).isEqualTo(1)
        assertThat(reviewed.intervalDays).isEqualTo(1)
    }

    @Test
    fun `second correct review schedules for 6 days`() {
        val afterFirst = Sm2.review(Sm2.initialSchedule(), ReviewQuality.OK, now)
        val afterSecond = Sm2.review(afterFirst, ReviewQuality.OK, now)
        assertThat(afterSecond.repetitions).isEqualTo(2)
        assertThat(afterSecond.intervalDays).isEqualTo(6)
    }

    @Test
    fun `third correct review multiplies by ease factor`() {
        var schedule = Sm2.initialSchedule()
        repeat(3) { schedule = Sm2.review(schedule, ReviewQuality.OK, now) }
        // rep 3+ : interval = round(prev * ease)
        // default ease 2.5, but OK (q=4) nudges ease to ~2.5
        assertThat(schedule.intervalDays).isAtLeast(13) // 6 * 2.5 = 15, allow slack
    }

    @Test
    fun `easy rating increases the ease factor`() {
        val reviewed = Sm2.review(Sm2.initialSchedule(), ReviewQuality.Easy, now)
        assertThat(reviewed.easeFactor).isGreaterThan(2.5)
    }

    @Test
    fun `hard rating decreases the ease factor but never below 1_3`() {
        var schedule = Sm2.initialSchedule()
        repeat(20) { schedule = Sm2.review(schedule, ReviewQuality.Hard, now) }
        assertThat(schedule.easeFactor).isAtLeast(1.3)
    }

    @Test
    fun `isCorrect distinguishes pass from fail`() {
        assertThat(ReviewQuality.isCorrect(ReviewQuality.Again)).isFalse()
        assertThat(ReviewQuality.isCorrect(ReviewQuality.Hard)).isTrue()
        assertThat(ReviewQuality.isCorrect(ReviewQuality.OK)).isTrue()
        assertThat(ReviewQuality.isCorrect(ReviewQuality.Easy)).isTrue()
    }
}
