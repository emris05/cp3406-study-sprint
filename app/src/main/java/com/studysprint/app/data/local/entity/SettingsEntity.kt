package com.studysprint.app.data.local.entity

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
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val soundEnabled: Boolean = true,
    val darkModeOrdinal: Int = 0,
    val weatherCity: String = "Cairns",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
)
