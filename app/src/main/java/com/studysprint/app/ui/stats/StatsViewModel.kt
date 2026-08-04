package com.studysprint.app.ui.stats

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
import javax.inject.Inject

data class StatsUiState(
    val totalFocusSeconds: Long = 0,
    val totalSessions: Int = 0,
    val streakDays: Int = 0,
    val perTaskSeconds: Map<Long, Long> = emptyMap(),
    val tasks: List<StudyTask> = emptyList(),
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    taskRepository: TaskRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        sessionRepository.observeTotalFocusSeconds(),
        sessionRepository.observeSessionCount(),
        // Combine the per-task + tasks + timestamps into a single derived flow first
        // so we stay within combine's 5-arg limit.
        combine(
            sessionRepository.observePerTaskSeconds(),
            taskRepository.observeActiveTasks(),
            sessionRepository.observeCompletionTimestamps(),
        ) { perTask, tasks, timestamps ->
            Triple(perTask, tasks, timestamps)
        },
    ) { total, count, (perTask, tasks, timestamps) ->
        StatsUiState(
            totalFocusSeconds = total,
            totalSessions = count,
            streakDays = StatsCalculator.calculateStreak(timestamps, System.currentTimeMillis()),
            perTaskSeconds = perTask,
            tasks = tasks,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )
}
