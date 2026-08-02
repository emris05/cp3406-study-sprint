package com.studysprint.app.data.model

/**
 * The phase the timer is currently in. The cycle is:
 * Focus → ShortBreak → (repeat N times) → LongBreak → back to Focus.
 */
enum class TimerPhase(val displayKey: String) {
    Focus("focus_phase_focus"),
    ShortBreak("focus_phase_short_break"),
    LongBreak("focus_phase_long_break");

    val isBreak: Boolean get() = this != Focus
}

/**
 * The high-level state of the focus timer.
 * Idle = not started, Running = counting down, Paused = held mid-phase.
 */
enum class TimerStatus { Idle, Running, Paused }
