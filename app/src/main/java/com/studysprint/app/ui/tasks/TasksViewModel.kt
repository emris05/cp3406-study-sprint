package com.studysprint.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysprint.app.data.model.StudyTask
import com.studysprint.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasksUiState(
    val tasks: List<StudyTask> = emptyList(),
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TaskRepository,
) : ViewModel() {

    val uiState: StateFlow<TasksUiState> = repository.observeActiveTasks()
        .map { tasks -> TasksUiState(tasks = tasks) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TasksUiState(),
        )

    fun addTask(title: String) = viewModelScope.launch {
        if (title.isNotBlank()) repository.create(title, "")
    }

    fun setActive(taskId: Long) = viewModelScope.launch {
        repository.setActive(taskId)
    }

    fun markCompleted(taskId: Long) = viewModelScope.launch {
        repository.markCompleted(taskId)
    }

    fun delete(taskId: Long) = viewModelScope.launch {
        repository.delete(taskId)
    }
}
