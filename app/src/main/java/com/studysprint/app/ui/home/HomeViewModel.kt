package com.studysprint.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysprint.app.data.model.StudyTask
import com.studysprint.app.data.repository.SessionRepository
import com.studysprint.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    val uiState: StateFlow<HomeUiState> = combine(
        sessionRepository.observeTotalFocusSeconds(),
        sessionRepository.observeSessionCount(),
        taskRepository.observeActiveTask(),
    ) { total, count, task ->
        HomeUiState(
            todayFocusSeconds = total, // simplified — full today-only calc in stats
            totalSessions = count,
            activeTask = task,
            streakDays = 0, // computed in StatsViewModel; kept simple here
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
