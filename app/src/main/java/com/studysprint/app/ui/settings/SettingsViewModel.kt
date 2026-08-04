package com.studysprint.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysprint.app.data.model.AppSettings
import com.studysprint.app.data.repository.SessionRepository
import com.studysprint.app.data.repository.SettingsRepository
import com.studysprint.app.data.repository.TaskRepository
import com.studysprint.app.ui.theme.DarkModeChoice
import com.studysprint.app.work.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val uiState: StateFlow<AppSettings?> = settingsRepository.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun setFocusMinutes(value: Int) = update { it.copy(focusMinutes = value.coerceIn(AppSettings.FOCUS_RANGE)) }
    fun setShortBreak(value: Int) = update { it.copy(shortBreakMinutes = value.coerceIn(AppSettings.SHORT_BREAK_RANGE)) }
    fun setLongBreak(value: Int) = update { it.copy(longBreakMinutes = value.coerceIn(AppSettings.LONG_BREAK_RANGE)) }
    fun setSessionsBeforeLong(value: Int) = update { it.copy(sessionsBeforeLongBreak = value.coerceIn(AppSettings.SESSIONS_RANGE)) }
    fun setSoundEnabled(value: Boolean) = update { it.copy(soundEnabled = value) }
    fun setDarkMode(value: DarkModeChoice) = update { it.copy(darkMode = value) }
    fun setWeatherCity(value: String) = update { it.copy(weatherCity = value) }

    fun setReminderEnabled(value: Boolean) = viewModelScope.launch {
        settingsRepository.update { it.copy(reminderEnabled = value) }
        if (value) {
            val s = settingsRepository.get()
            reminderScheduler.schedule(s.reminderHour, s.reminderMinute)
        } else {
            reminderScheduler.cancel()
        }
    }

    fun setReminderTime(hour: Int, minute: Int) = viewModelScope.launch {
        settingsRepository.update { it.copy(reminderHour = hour, reminderMinute = minute) }
        // Re-schedule if currently enabled, so the new time takes effect.
        if (settingsRepository.get().reminderEnabled) reminderScheduler.schedule(hour, minute)
    }

    fun clearAllData() = viewModelScope.launch {
        sessionRepository.clear()
        taskRepository.clearAll()
    }

    private fun update(transform: (AppSettings) -> AppSettings) = viewModelScope.launch {
        settingsRepository.update(transform)
    }
}
