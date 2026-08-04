package com.studysprint.app.ui.focus

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.studysprint.app.ui.theme.Amber
import com.studysprint.app.ui.theme.Dimens
import com.studysprint.app.ui.theme.Indigo
import com.studysprint.app.ui.theme.IndigoSoft

/**
 * The main "activity" screen — the focus timer.
 */
@Composable
fun FocusScreen(
    onNavigateToTasks: () -> Unit,
    viewModel: FocusViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val ringColour = when (state.phase) {
        TimerPhase.Focus -> IndigoSoft
        TimerPhase.ShortBreak, TimerPhase.LongBreak -> Amber
    }
    val phaseLabel = when (state.phase) {
        TimerPhase.Focus -> stringResource(R.string.focus_phase_focus)
        TimerPhase.ShortBreak -> stringResource(R.string.focus_phase_short_break)
        TimerPhase.LongBreak -> stringResource(R.string.focus_phase_long_break)
    }
    val phaseColour by animateColorAsState(
        targetValue = if (state.phase == TimerPhase.Focus) Indigo else Amber,
        label = "phaseColour",
    )
    val description = "${phaseLabel}, ${state.remainingSeconds / 60} minutes ${state.remainingSeconds % 60} seconds remaining"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        phaseColour.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .padding(horizontal = Dimens.lg, vertical = Dimens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.lg, Alignment.CenterVertically),
    ) {
        // Phase pill
        PhasePill(phaseLabel, phaseColour, state.completedFocusSessions + 1)

        // The ring
        CountdownRing(
            totalSeconds = state.totalSeconds,
            remainingSeconds = state.remainingSeconds,
            progressColour = ringColour,
            contentDescription = description,
        )

        // Quick duration presets — only when idle, so a running timer isn't disrupted.
        if (state.phase == TimerPhase.Focus && state.status == TimerStatus.Idle) {
            FocusPresets(
                currentMinutes = (state.totalSeconds / 60).toInt(),
                onSelect = viewModel::setFocusMinutes,
            )
        }

        // Active task chip
        AssistChip(
            onClick = onNavigateToTasks,
            label = {
                Text(
                    state.activeTask?.title ?: stringResource(R.string.tasks_set_active),
                    fontWeight = FontWeight.Medium,
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                labelColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        // Break suggestion
        if (state.phase.isBreak) {
            BreakSuggestionCard(
                suggestion = state.breakSuggestion,
                isLoading = state.isLoadingWeather,
            )
        }

        Spacer(Modifier.height(Dimens.xs))

        // Controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.lg, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = viewModel::reset,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(Dimens.cornerMedium),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.focus_reset))
            }

            PrimaryTimerButton(
                status = state.status,
                phaseColour = phaseColour,
                onStart = viewModel::start,
                onPause = viewModel::pause,
            )

            FilledIconButton(
                onClick = viewModel::skip,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(Dimens.cornerMedium),
            ) {
                Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.focus_skip))
            }
        }
    }
}

@Composable
private fun PhasePill(label: String, colour: Color, sessionNumber: Int) {
    Row(
        modifier = Modifier
            .background(colour.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = Dimens.md, vertical = Dimens.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = colour,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "· Session $sessionNumber",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrimaryTimerButton(
    status: TimerStatus,
    phaseColour: Color,
    onStart: () -> Unit,
    onPause: () -> Unit,
) {
    val (icon, label, action) = when (status) {
        TimerStatus.Running -> Triple(Icons.Outlined.Pause, stringResource(R.string.focus_pause), onPause)
        TimerStatus.Paused -> Triple(Icons.Outlined.PlayArrow, stringResource(R.string.focus_resume), onStart)
        TimerStatus.Idle -> Triple(Icons.Outlined.PlayArrow, stringResource(R.string.focus_start), onStart)
    }
    Button(
        onClick = action,
        modifier = Modifier.size(width = 160.dp, height = 64.dp),
        shape = RoundedCornerShape(Dimens.cornerLarge),
        colors = ButtonDefaults.buttonColors(containerColor = phaseColour),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.size(Dimens.sm))
        Text(label, fontWeight = FontWeight.Bold)
    }
}

/**
 * Quick duration presets shown below the ring when the timer is idle. Tapping
 * one sets the focus length in settings; the combine picks it up and the ring
 * updates immediately.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusPresets(
    currentMinutes: Int,
    onSelect: (Int) -> Unit,
) {
    val presets = listOf(5, 15, 25, 45)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        presets.forEach { minutes ->
            FilterChip(
                selected = minutes == currentMinutes,
                onClick = { onSelect(minutes) },
                label = { Text("${minutes}m") },
            )
        }
    }
}
