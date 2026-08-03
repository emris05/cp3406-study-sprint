package com.studysprint.app.timer

import com.studysprint.app.data.model.AppSettings
import com.studysprint.app.data.model.TimerPhase
import com.studysprint.app.data.model.TimerStatus

/**
 * The current, fully-derived snapshot of the timer at any instant.
 *
 * [remainingSeconds] is computed from the wall clock, not stored — so a running
 * timer keeps the right value even after rotation or backgrounding.
 */
data class TimerState(
    val phase: TimerPhase = TimerPhase.Focus,
    val status: TimerStatus = TimerStatus.Idle,
    val phaseDurationSeconds: Long = 0,
    val remainingSeconds: Long = 0,
    val completedFocusSessions: Int = 0,
    /** Wall-clock millis when the current run started/resumed; -1 when paused. */
    val epochStartMillis: Long = -1,
)

/**
 * Pure timer state machine — no Android, no coroutines, no I/O.
 *
 * Time is tracked against [System.currentTimeMillis] (injected via [clock]) so
 * that running timers stay accurate across configuration changes and the app
 * going to the background. A coroutine in the ViewModel just ticks ~1/s to
 * refresh the display; the engine never sleeps to count down.
 *
 * Phase order: Focus → ShortBreak → (repeat N-1) → Focus → LongBreak → Focus …
 * where N = [AppSettings.sessionsBeforeLongBreak].
 */
class TimerEngine(
    private val clock: () -> Long = System::currentTimeMillis,
) {

    /** Build the initial idle state for a fresh focus phase. */
    fun initialState(settings: AppSettings): TimerState {
        val duration = settings.focusMinutes.toLong() * 60
        return TimerState(
            phase = TimerPhase.Focus,
            status = TimerStatus.Idle,
            phaseDurationSeconds = duration,
            remainingSeconds = duration,
            completedFocusSessions = 0,
            epochStartMillis = -1,
        )
    }

    /** Start a phase from idle, or resume from paused. */
    fun start(state: TimerState, now: Long = clock()): TimerState {
        if (state.status == TimerStatus.Running) return state
        val elapsed = if (state.status == TimerStatus.Paused) state.phaseDurationSeconds - state.remainingSeconds else 0
        return state.copy(
            status = TimerStatus.Running,
            epochStartMillis = now - elapsed * 1000,
            remainingSeconds = state.remainingSeconds,
        )
    }

    /** Pause a running timer — freeze the remaining time. */
    fun pause(state: TimerState, now: Long = clock()): TimerState {
        if (state.status != TimerStatus.Running) return state
        return state.copy(
            status = TimerStatus.Paused,
            remainingSeconds = remainingAt(state, now),
            epochStartMillis = -1,
        )
    }

    /** Recompute remaining seconds for a running timer. Returns state unchanged if idle/paused. */
    fun tick(state: TimerState, now: Long = clock()): TimerState {
        if (state.status != TimerStatus.Running) return state
        val remaining = remainingAt(state, now)
        return if (remaining <= 0) state.copy(remainingSeconds = 0)
        else state.copy(remainingSeconds = remaining)
    }

    /** Whether the current phase has reached zero (only meaningful when running). */
    fun isPhaseComplete(state: TimerState, now: Long = clock()): Boolean =
        state.status == TimerStatus.Running && remainingAt(state, now) <= 0

    /**
     * Advance to the next phase. Called by the ViewModel once [isPhaseComplete]
     * is true (or on an explicit skip). Returns the new state, idle and ready
     * to [start]. Also bumps [TimerState.completedFocusSessions] when leaving
     * a Focus phase.
     */
    fun advance(state: TimerState, settings: AppSettings): TimerState {
        val (nextPhase, completed) = nextPhaseFor(state.phase, state.completedFocusSessions, settings)
        val duration = phaseDurationSeconds(nextPhase, settings)
        return TimerState(
            phase = nextPhase,
            status = TimerStatus.Idle,
            phaseDurationSeconds = duration,
            remainingSeconds = duration,
            completedFocusSessions = completed,
            epochStartMillis = -1,
        )
    }

    /** Reset back to a fresh Focus phase, preserving the completed-session count. */
    fun reset(state: TimerState, settings: AppSettings): TimerState {
        val duration = settings.focusMinutes.toLong() * 60
        return state.copy(
            phase = TimerPhase.Focus,
            status = TimerStatus.Idle,
            phaseDurationSeconds = duration,
            remainingSeconds = duration,
            epochStartMillis = -1,
        )
    }

    /** Recompute the focus duration in place when the user changes settings mid-session. */
    fun applySettings(state: TimerState, settings: AppSettings, now: Long = clock()): TimerState {
        val expectedDuration = phaseDurationSeconds(state.phase, settings)
        if (expectedDuration == state.phaseDurationSeconds) return state
        // If the user is partway through, recompute remaining from the same elapsed fraction.
        val remaining = if (state.status == TimerStatus.Running) {
            (remainingAt(state, now).coerceAtLeast(0)).coerceAtMost(expectedDuration)
        } else {
            state.remainingSeconds.coerceAtMost(expectedDuration)
        }
        return state.copy(phaseDurationSeconds = expectedDuration, remainingSeconds = remaining)
    }

    private fun remainingAt(state: TimerState, now: Long): Long {
        val elapsedSeconds = (now - state.epochStartMillis) / 1000
        return (state.phaseDurationSeconds - elapsedSeconds).coerceAtLeast(0)
    }

    private fun phaseDurationSeconds(phase: TimerPhase, settings: AppSettings): Long = when (phase) {
        TimerPhase.Focus -> settings.focusMinutes.toLong() * 60
        TimerPhase.ShortBreak -> settings.shortBreakMinutes.toLong() * 60
        TimerPhase.LongBreak -> settings.longBreakMinutes.toLong() * 60
    }

    /**
     * Pure phase-transition logic. After N focus sessions, take a long break
     * instead of a short one. Returns (next phase, new completed focus count).
     */
    private fun nextPhaseFor(
        current: TimerPhase,
        completedFocusSessions: Int,
        settings: AppSettings,
    ): Pair<TimerPhase, Int> = when (current) {
        TimerPhase.Focus -> {
            val newCompleted = completedFocusSessions + 1
            val isLongBreakDue = newCompleted % settings.sessionsBeforeLongBreak == 0
            val next = if (isLongBreakDue) TimerPhase.LongBreak else TimerPhase.ShortBreak
            next to newCompleted
        }
        TimerPhase.ShortBreak, TimerPhase.LongBreak -> TimerPhase.Focus to completedFocusSessions
    }
}
