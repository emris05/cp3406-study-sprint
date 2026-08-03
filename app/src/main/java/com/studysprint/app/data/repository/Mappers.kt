package com.studysprint.app.data.repository

import com.studysprint.app.data.local.entity.SessionEntity
import com.studysprint.app.data.local.entity.SettingsEntity
import com.studysprint.app.data.local.entity.TaskEntity
import com.studysprint.app.data.model.AppSettings
import com.studysprint.app.data.model.FocusSession
import com.studysprint.app.data.model.StudyTask
import com.studysprint.app.data.model.WeatherInfo
import com.studysprint.app.data.remote.dto.WeatherResponse
import com.studysprint.app.ui.theme.DarkModeChoice

/** Entity <-> domain mapping, isolated for testability. */

internal fun TaskEntity.toDomain() = StudyTask(
    id = id,
    title = title,
    notes = notes,
    isActive = isActive,
    isCompleted = isCompleted,
    totalFocusSeconds = totalFocusSeconds,
    createdAt = createdAt,
)

internal fun StudyTask.toEntity() = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    isActive = isActive,
    isCompleted = isCompleted,
    totalFocusSeconds = totalFocusSeconds,
    createdAt = createdAt,
)

internal fun SessionEntity.toDomain() = FocusSession(
    id = id,
    taskId = taskId,
    durationSeconds = durationSeconds,
    completedAt = completedAt,
)

internal fun SettingsEntity.toDomain() = AppSettings(
    focusMinutes = focusMinutes,
    shortBreakMinutes = shortBreakMinutes,
    longBreakMinutes = longBreakMinutes,
    sessionsBeforeLongBreak = sessionsBeforeLongBreak,
    soundEnabled = soundEnabled,
    darkMode = DarkModeChoice.entries.getOrElse(darkModeOrdinal) { DarkModeChoice.System },
    weatherCity = weatherCity,
    reminderEnabled = reminderEnabled,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
)

internal fun AppSettings.toEntity() = SettingsEntity(
    id = 1,
    focusMinutes = focusMinutes,
    shortBreakMinutes = shortBreakMinutes,
    longBreakMinutes = longBreakMinutes,
    sessionsBeforeLongBreak = sessionsBeforeLongBreak,
    soundEnabled = soundEnabled,
    darkModeOrdinal = darkMode.ordinal,
    weatherCity = weatherCity,
    reminderEnabled = reminderEnabled,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
)

internal fun WeatherResponse.toDomain(): WeatherInfo {
    val temp = main.temp.toInt()
    val condId = weather.firstOrNull()?.id ?: 0
    // OpenWeather condition codes: 2xx/3xx/5xx/6xx = rain/storm/snow (not nice),
    // 7xx = atmosphere (mist/smoke), 800 = clear, 80x = clouds.
    val isNice = temp in 15..28 && condId !in 200..699
    return WeatherInfo(
        city = name ?: "",
        tempCelsius = temp,
        feelsLikeCelsius = main.feelsLike.toInt(),
        condition = weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
        isNiceOutdoors = isNice,
    )
}
