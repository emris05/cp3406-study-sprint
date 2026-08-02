package com.studysprint.app.data.model

import com.studysprint.app.ui.theme.DarkModeChoice

/**
 * All user-configurable settings in one immutable object. Persisted via Room
 * (single-row table — see [com.studysprint.app.data.local.entity.SettingsEntity]).
 *
 * Defaults follow the classic Pomodoro structure: 25 / 5 / 15 minutes, long
 * break every 4 sessions.
 */
data class AppSettings(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val soundEnabled: Boolean = true,
    val darkMode: DarkModeChoice = DarkModeChoice.System,
    val weatherCity: String = "Cairns",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
) {
    companion object {
        /** Sensible bounds used by SettingsViewModel to validate input. */
        val FOCUS_RANGE = 5..90
        val SHORT_BREAK_RANGE = 1..30
        val LONG_BREAK_RANGE = 5..60
        val SESSIONS_RANGE = 2..8
    }
}
