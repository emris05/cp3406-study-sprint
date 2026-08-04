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
 * Stateless UI-facing snapshot of the focus screen.
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
 * The timer state is re-derived whenever settings change (so adjusting the
 * focus length in Settings updates the running timer). The engine itself is
 * driven off the system clock — see [TimerEngine] for why.
 */
@HiltViewModel
class FocusViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val taskRepository: TaskRepository,
    private val sessionRepository: SessionRepository,
    private val weatherRepository: WeatherRepository,
    private val engine: TimerEngine,
    private val breakController: BreakController,
    private val phaseAlerter: com.studysprint.app.util.PhaseAlerter,
) : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState?>(null)
    private val _breakSuggestion = MutableStateFlow<BreakSuggestion?>(null)
    private val _isLoadingWeather = MutableStateFlow(false)
    private var focusPhaseTaskId: Long? = null
    private var tickJob: Job? = null

    val uiState: StateFlow<FocusUiState> = combine(
        _timerState,
        taskRepository.observeActiveTask(),
        settingsRepository.observe(),
        _breakSuggestion,
        _isLoadingWeather,
    ) { timer, activeTask, settings, suggestion, loading ->
        // Apply the latest settings to the timer state whenever they change.
        // When the timer isn't running, this updates the displayed duration so
        // the user sees their new focus length immediately. The guard against
        // writing when unchanged prevents a feedback loop.
        val resolved = when {
            timer == null -> engine.initialState(settings)
            timer.status != TimerStatus.Running -> engine.applySettings(timer, settings)
            else -> timer
        }
        if (resolved != timer) _timerState.value = resolved
        FocusUiState(
            phase = resolved.phase,
            remainingSeconds = resolved.remainingSeconds,
            totalSeconds = resolved.phaseDurationSeconds,
            status = resolved.status,
            completedFocusSessions = resolved.completedFocusSessions,
            activeTask = activeTask,
            breakSuggestion = suggestion,
            isLoadingWeather = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FocusUiState(),
    )

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

    /**
     * Quick-set the focus duration from a preset chip on the Focus screen.
     * Updates settings; the uiState combine picks up the change and refreshes
     * the displayed duration (when the timer isn't running).
     */
    fun setFocusMinutes(minutes: Int) = viewModelScope.launch {
        settingsRepository.update { it.copy(focusMinutes = minutes.coerceIn(AppSettings.FOCUS_RANGE)) }
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
                    if (ticked.phase == TimerPhase.Focus) recordFocusSession(ticked)
                    val advanced = engine.advance(ticked, settings)
                    _timerState.value = advanced
                    phaseAlerter.phaseComplete(soundEnabled = settings.soundEnabled)
                    if (advanced.phase.isBreak) loadBreakSuggestion(settings.weatherCity)
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

    private fun loadBreakSuggestion(city: String) {
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
