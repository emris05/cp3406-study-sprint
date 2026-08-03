package com.studysprint.app.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class StatsCalculatorTest {

    private val now = System.currentTimeMillis()
    private val dayMillis = TimeUnit.DAYS.toMillis(1)

    @Test
    fun `empty timestamps gives zero streak`() {
        assertThat(StatsCalculator.calculateStreak(emptyList(), now)).isEqualTo(0)
    }

    @Test
    fun `session today counts as a 1-day streak`() {
        val streak = StatsCalculator.calculateStreak(listOf(now), now)
        assertThat(streak).isEqualTo(1)
    }

    @Test
    fun `consecutive days build a streak`() {
        val today = startOfDay(now)
        val yesterday = today - dayMillis
        val twoDaysAgo = today - 2 * dayMillis
        val timestamps = listOf(twoDaysAgo + 1000, yesterday + 1000, today + 1000)
        assertThat(StatsCalculator.calculateStreak(timestamps, now)).isEqualTo(3)
    }

    @Test
    fun `a gap breaks the streak`() {
        val today = startOfDay(now)
        val threeDaysAgo = today - 3 * dayMillis
        val timestamps = listOf(threeDaysAgo + 1000, today + 1000)
        // today counts (1), yesterday missing -> streak stops
        assertThat(StatsCalculator.calculateStreak(timestamps, now)).isEqualTo(1)
    }

    @Test
    fun `streak is not broken at midnight if yesterday was active`() {
        // "now" is just past midnight; last session was yesterday evening.
        val today = startOfDay(now)
        val yesterday = today - dayMillis
        val timestamps = listOf(yesterday + 1000)
        assertThat(StatsCalculator.calculateStreak(timestamps, now)).isEqualTo(1)
    }

    @Test
    fun `multiple sessions on the same day count as one streak day`() {
        val today = startOfDay(now)
        val timestamps = listOf(today + 1000, today + 5000, today + 9000)
        assertThat(StatsCalculator.calculateStreak(timestamps, now)).isEqualTo(1)
    }

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
