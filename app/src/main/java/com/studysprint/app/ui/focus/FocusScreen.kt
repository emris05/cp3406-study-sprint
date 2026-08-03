package com.studysprint.app.ui.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studysprint.app.R
import com.studysprint.app.data.model.TimerPhase
import com.studysprint.app.data.model.TimerStatus
import com.studysprint.app.ui.components.BreakSuggestionCard
import com.studysprint.app.ui.components.CountdownRing

/**
 * The main "activity" screen — the focus timer.
 *
 * Layout: phase label up top, the big countdown ring in the centre, the active
 * task chip below it, then the start/pause and skip/reset controls.
 */
@Composable
fun FocusScreen(
    onNavigateToTasks: () -> Unit,
    viewModel: FocusViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val ringColour = when (state.phase) {
        TimerPhase.Focus -> MaterialTheme.colorScheme.primary
        TimerPhase.ShortBreak, TimerPhase.LongBreak -> MaterialTheme.colorScheme.secondary
    }
    val phaseLabel = when (state.phase) {
        TimerPhase.Focus -> stringResource(R.string.focus_phase_focus)
        TimerPhase.ShortBreak -> stringResource(R.string.focus_phase_short_break)
        TimerPhase.LongBreak -> stringResource(R.string.focus_phase_long_break)
    }
    val description = "${phaseLabel}, ${state.remainingSeconds / 60} minutes ${state.remainingSeconds % 60} seconds remaining"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        // Phase label + session counter.
        Text(
            text = phaseLabel,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Session ${state.completedFocusSessions + 1}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // The ring itself.
        CountdownRing(
            totalSeconds = state.totalSeconds,
            remainingSeconds = state.remainingSeconds,
            progressColour = ringColour,
            contentDescription = description,
        )

        // Active task chip — tappable to pick/change a task.
        ActiveTaskChip(
            taskTitle = state.activeTask?.title,
            onPickTask = onNavigateToTasks,
        )

        // Break suggestion card — only relevant during a break phase.
        if (state.phase.isBreak) {
            BreakSuggestionCard(
                suggestion = state.breakSuggestion,
                isLoading = state.isLoadingWeather,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Primary controls.
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::reset) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.focus_reset))
            }
            PrimaryTimerButton(
                status = state.status,
                onStart = viewModel::start,
                onPause = viewModel::pause,
            )
            IconButton(onClick = viewModel::skip) {
                Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.focus_skip))
            }
        }
    }
}

/** The big start/pause/resume button in the middle of the controls row. */
@Composable
private fun PrimaryTimerButton(
    status: TimerStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
) {
    when (status) {
        TimerStatus.Running -> FilledTonalButton(
            onClick = onPause,
            modifier = Modifier.size(width = 140.dp, height = 56.dp),
        ) {
            Text(stringResource(R.string.focus_pause), fontWeight = FontWeight.SemiBold)
        }
        TimerStatus.Paused -> FilledTonalButton(
            onClick = onStart,
            modifier = Modifier.size(width = 140.dp, height = 56.dp),
        ) {
            Text(stringResource(R.string.focus_resume), fontWeight = FontWeight.SemiBold)
        }
        TimerStatus.Idle -> FilledTonalButton(
            onClick = onStart,
            modifier = Modifier.size(width = 140.dp, height = 56.dp),
        ) {
            Icon(Icons.Outlined.PlayArrow, contentDescription = null)
            Text(stringResource(R.string.focus_start), fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Small chip showing the active task, or a prompt to pick one. */
@Composable
private fun ActiveTaskChip(
    taskTitle: String?,
    onPickTask: () -> Unit,
) {
    AssistChip(
        onClick = onPickTask,
        label = {
            Text(taskTitle ?: stringResource(R.string.tasks_set_active))
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
