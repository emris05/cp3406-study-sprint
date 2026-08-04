package com.studysprint.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysprint.app.data.model.StudyTask
import com.studysprint.app.data.repository.SessionRepository
import com.studysprint.app.data.repository.TaskRepository
import com.studysprint.app.util.StatsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val todayFocusSeconds: Long = 0,
    val totalSessions: Int = 0,
    val activeTask: StudyTask? = null,
    val streakDays: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    taskRepository: TaskRepository,
) : ViewModel() {

    private val startOfTodayMillis: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val uiState: StateFlow<HomeUiState> = combine(
        sessionRepository.observeFocusSecondsSince(startOfTodayMillis),
        sessionRepository.observeSessionCount(),
        taskRepository.observeActiveTask(),
        sessionRepository.observeCompletionTimestamps(),
    ) { todaySeconds, count, task, timestamps ->
        HomeUiState(
            todayFocusSeconds = todaySeconds,
            totalSessions = count,
            activeTask = task,
            streakDays = StatsCalculator.calculateStreak(timestamps, System.currentTimeMillis()),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
