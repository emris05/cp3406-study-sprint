package com.studysprint.app.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysprint.app.data.model.AppSettings
import com.studysprint.app.data.model.BreakSuggestion
import com.studysprint.app.data.model.StudyTask
import com.studysprint.app.data.model.TimerPhase
import com.studysprint.app.data.model.TimerStatus
import com.studysprint.app.data.repository.SessionRepository
import com.studysprint.app.data.repository.SettingsRepository
import com.studysprint.app.data.repository.TaskRepository
import com.studysprint.app.data.repository.WeatherRepository
import com.studysprint.app.timer.BreakController
import com.studysprint.app.timer.TimerEngine
import com.studysprint.app.timer.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Stateless UI-facing snapshot of the focus screen. Everything the Compose layer
 * needs, nothing it doesn't.
 */
data class FocusUiState(
    val phase: TimerPhase = TimerPhase.Focus,
    val remainingSeconds: Long = 0,
    val totalSeconds: Long = 0,
    val status: TimerStatus = TimerStatus.Idle,
    val completedFocusSessions: Int = 0,
    val activeTask: StudyTask? = null,
    val breakSuggestion: BreakSuggestion? = null,
    val isLoadingWeather: Boolean = false,
)

/**
 * Drives the [TimerEngine] and exposes a single [FocusUiState] to Compose.
 *
 * A 1-second ticker coroutine refreshes the displayed countdown while running.
 * The engine itself never sleeps — see [TimerEngine] for why. When a Focus
 * phase completes we persist a [com.studysprint.app.data.model.FocusSession]
 * and credit time to the active task.
 *
 * The active task id is captured *when a focus phase starts* and held for that
 * phase's duration, so crediting goes to the right task even if the user
 * switches tasks mid-session.
 */
@HiltViewModel
class FocusViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val taskRepository: TaskRepository,
    private val sessionRepository: SessionRepository,
    private val weatherRepository: WeatherRepository,
    private val engine: TimerEngine,
    private val breakController: BreakController,
) : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState?>(null)
    private val _breakSuggestion = MutableStateFlow<BreakSuggestion?>(null)
    private val _isLoadingWeather = MutableStateFlow(false)
    /** The task id locked in at the start of the current focus phase, if any. */
    private var focusPhaseTaskId: Long? = null
    /** Latest snapshot of the active task, for display. */
    private val activeTaskFlow = taskRepository.observeActiveTask()
    private var tickJob: Job? = null

    val uiState: StateFlow<FocusUiState> = combine(
        _timerState,
        activeTaskFlow,
        settingsRepository.observe(),
    ) { timer, activeTask, settings ->
        val resolved = timer ?: engine.initialState(settings).also { _timerState.value = it }
        FocusUiState(
            phase = resolved.phase,
            remainingSeconds = resolved.remainingSeconds,
            totalSeconds = resolved.phaseDurationSeconds,
            status = resolved.status,
            completedFocusSessions = resolved.completedFocusSessions,
            activeTask = activeTask,
            breakSuggestion = _breakSuggestion.value,
            isLoadingWeather = _isLoadingWeather.value,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FocusUiState(),
    )

    /** Begin (or resume) the countdown. Captures the active task for this focus phase. */
    fun start() = viewModelScope.launch {
        val settings = settingsRepository.get()
        val current = currentOrInitial(settings)
        if (current.phase == TimerPhase.Focus && current.status == TimerStatus.Idle) {
            focusPhaseTaskId = uiState.value.activeTask?.id
        }
        val started = engine.start(current)
        _timerState.value = started
        if (started.status == TimerStatus.Running) startTicking()
    }

    fun pause() = viewModelScope.launch {
        tickJob?.cancel()
        tickJob = null
        _timerState.value = engine.pause(currentOrInitial(settingsRepository.get()))
    }

    /** Skip the current phase without crediting any focus time. */
    fun skip() = viewModelScope.launch {
        tickJob?.cancel()
        tickJob = null
        val settings = settingsRepository.get()
        val current = currentOrInitial(settings)
        if (current.phase == TimerPhase.Focus) focusPhaseTaskId = null
        _timerState.value = engine.advance(current, settings)
    }

    fun reset() = viewModelScope.launch {
        tickJob?.cancel()
        tickJob = null
        focusPhaseTaskId = null
        _timerState.value = engine.reset(currentOrInitial(settingsRepository.get()), settingsRepository.get())
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(TICK_INTERVAL_MS)
                val settings = settingsRepository.get()
                val current = _timerState.value ?: return@launch
                val ticked = engine.tick(current)
                if (engine.isPhaseComplete(ticked)) {
                    // Record the completed focus phase before advancing.
                    if (ticked.phase == TimerPhase.Focus) recordFocusSession(ticked)
                    val advanced = engine.advance(ticked, settings)
                    _timerState.value = advanced
                    // When a break begins, fetch weather + pick a break activity.
                    if (advanced.phase.isBreak) loadBreakSuggestion(settings.weatherCity)
                    // Auto-start the next phase so the cycle keeps flowing.
                    _timerState.value = engine.start(advanced)
                } else {
                    _timerState.value = ticked
                }
            }
        }
    }

    private suspend fun recordFocusSession(state: TimerState) {
        sessionRepository.record(
            taskId = if (state.phase == TimerPhase.Focus) focusPhaseTaskId else null,
            durationSeconds = state.phaseDurationSeconds,
        )
        focusPhaseTaskId?.let { taskRepository.addFocusSeconds(it, state.phaseDurationSeconds) }
        if (state.phase == TimerPhase.Focus) focusPhaseTaskId = null
    }

    /**
     * Fetch weather for [city] and pick a matching break activity. If the fetch
     * fails (offline, bad key) we still suggest something from the offline
     * library — never leave the user staring at an empty break screen.
     */
    private fun loadBreakSuggestion(city: String) {
        // Clear the old card first so a stale suggestion never lingers.
        _breakSuggestion.value = null
        _isLoadingWeather.value = true
        viewModelScope.launch {
            val weather = weatherRepository.getCurrentWeather(city)
            _breakSuggestion.value = breakController.suggest(weather)
            _isLoadingWeather.value = false
        }
    }

    private suspend fun currentOrInitial(settings: AppSettings): TimerState =
        _timerState.value ?: engine.initialState(settings).also { _timerState.value = it }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }

    private companion object {
        const val TICK_INTERVAL_MS = 1000L
    }
}
