package com.studysprint.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row settings table. The single row always has id = 1 and is created
 * on first access (see [com.studysprint.app.data.local.AppDatabase]).
 *
 * Dark mode is stored as its ordinal so we avoid storing enum class names
 * (which break under obfuscation / refactor).
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "focus_minutes") val focusMinutes: Int = 25,
    @ColumnInfo(name = "short_break_minutes") val shortBreakMinutes: Int = 5,
    @ColumnInfo(name = "long_break_minutes") val longBreakMinutes: Int = 15,
    @ColumnInfo(name = "sessions_before_long_break") val sessionsBeforeLongBreak: Int = 4,
    @ColumnInfo(name = "sound_enabled") val soundEnabled: Boolean = true,
    @ColumnInfo(name = "dark_mode_ordinal") val darkModeOrdinal: Int = 0,
    @ColumnInfo(name = "weather_city") val weatherCity: String = "Cairns",
    @ColumnInfo(name = "reminder_enabled") val reminderEnabled: Boolean = false,
    @ColumnInfo(name = "reminder_hour") val reminderHour: Int = 9,
    @ColumnInfo(name = "reminder_minute") val reminderMinute: Int = 0,
)
