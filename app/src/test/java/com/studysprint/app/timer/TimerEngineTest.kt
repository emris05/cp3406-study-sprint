package com.studysprint.app.timer

import com.google.common.truth.Truth.assertThat
import com.studysprint.app.data.model.AppSettings
import com.studysprint.app.data.model.TimerPhase
import com.studysprint.app.data.model.TimerStatus
import org.junit.Test

/**
 * Unit tests for the timer state machine. Uses a fake clock so every transition
 * is deterministic — no real time involved.
 */
class TimerEngineTest {

    private var fakeNow = 0L
    private val clock = { fakeNow }
    private val engine = TimerEngine(clock)
    private val settings = AppSettings(focusMinutes = 25, shortBreakMinutes = 5, longBreakMinutes = 15, sessionsBeforeLongBreak = 4)

    @Test
    fun `initial state is idle focus with full duration`() {
        val state = engine.initialState(settings)
        assertThat(state.phase).isEqualTo(TimerPhase.Focus)
        assertThat(state.status).isEqualTo(TimerStatus.Idle)
        assertThat(state.remainingSeconds).isEqualTo(25 * 60)
    }

    @Test
    fun `start transitions idle to running`() {
        var state = engine.initialState(settings)
        state = engine.start(state)
        assertThat(state.status).isEqualTo(TimerStatus.Running)
        assertThat(state.epochStartMillis).isEqualTo(fakeNow)
    }

    @Test
    fun `tick recomputes remaining seconds based on elapsed wall-clock time`() {
        var state = engine.initialState(settings)
        state = engine.start(state) // starts at fakeNow = 0
        fakeNow = 30_000 // 30s later
        state = engine.tick(state)
        assertThat(state.remainingSeconds).isEqualTo(25 * 60 - 30)
    }

    @Test
    fun `pause freezes remaining and resume continues from there`() {
        var state = engine.initialState(settings)
        state = engine.start(state)
        fakeNow = 60_000 // 1 min in
        state = engine.tick(state)
        state = engine.pause(state)
        assertThat(state.status).isEqualTo(TimerStatus.Paused)
        val pausedRemaining = state.remainingSeconds

        fakeNow = 3_600_000 // way later, but paused
        state = engine.tick(state) // no-op when paused
        assertThat(state.remainingSeconds).isEqualTo(pausedRemaining)

        state = engine.start(state) // resume
        fakeNow = 3_600_010 // 10s after resume
        state = engine.tick(state)
        assertThat(state.remainingSeconds).isEqualTo(pausedRemaining - 10)
    }

    @Test
    fun `isPhaseComplete is true when remaining hits zero`() {
        var state = engine.initialState(settings)
        state = engine.start(state)
        fakeNow = 25 * 60 * 1000L // exactly the focus duration
        state = engine.tick(state)
        assertThat(engine.isPhaseComplete(state)).isTrue()
        assertThat(state.remainingSeconds).isEqualTo(0)
    }

    @Test
    fun `advance from focus goes to short break and counts the session`() {
        var state = engine.initialState(settings)
        state = engine.advance(state, settings)
        assertThat(state.phase).isEqualTo(TimerPhase.ShortBreak)
        assertThat(state.completedFocusSessions).isEqualTo(1)
        assertThat(state.remainingSeconds).isEqualTo(5 * 60)
    }

    @Test
    fun `long break triggers after N sessions`() {
        var state = engine.initialState(settings)
        // Simulate N focus phases.
        repeat(4) {
            state = engine.advance(state, settings) // focus -> break
            state = engine.advance(state, settings) // break -> focus
        }
        // After 4 sessions the break we just came from should have been long.
        // Walk it step by step to be sure.
        var check = engine.initialState(settings)
        check = engine.advance(check, settings) // s1 -> short
        assertThat(check.phase).isEqualTo(TimerPhase.ShortBreak)
        check = engine.advance(check, settings) // short -> focus
        check = engine.advance(check, settings) // s2 -> short
        check = engine.advance(check, settings) // short -> focus
        check = engine.advance(check, settings) // s3 -> short
        check = engine.advance(check, settings) // short -> focus
        check = engine.advance(check, settings) // s4 -> LONG
        assertThat(check.phase).isEqualTo(TimerPhase.LongBreak)
        assertThat(check.completedFocusSessions).isEqualTo(4)
    }

    @Test
    fun `reset returns to focus idle preserving completed sessions`() {
        var state = engine.initialState(settings)
        state = engine.advance(state, settings) // 1 session, now break
        state = engine.reset(state, settings)
        assertThat(state.phase).isEqualTo(TimerPhase.Focus)
        assertThat(state.status).isEqualTo(TimerStatus.Idle)
        assertThat(state.completedFocusSessions).isEqualTo(1) // preserved
    }
}
