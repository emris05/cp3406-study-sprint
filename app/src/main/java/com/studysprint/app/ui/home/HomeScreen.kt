package com.studysprint.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studysprint.app.ui.components.GradientScreen
import com.studysprint.app.ui.components.SectionHeading
import com.studysprint.app.ui.components.formatClock
import com.studysprint.app.ui.theme.Amber
import com.studysprint.app.ui.theme.Dimens
import com.studysprint.app.ui.theme.Indigo
import com.studysprint.app.ui.theme.IndigoDark
import com.studysprint.app.ui.theme.IndigoSoft

@Composable
fun HomeScreen(
    onNavigateToFocus: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToFlashcards: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    GradientScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            // Greeting
            Text(
                "Study Sprint",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Focus, break, repeat. Review your flashcards. Build the habit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimens.sm))

            // Hero gradient card with progress
            HeroProgressCard(
                focusSeconds = state.todayFocusSeconds,
                sessions = state.totalSessions,
                streakDays = state.streakDays,
                onStartFocus = onNavigateToFocus,
            )

            // Active task
            state.activeTask?.let { task ->
                ActiveTaskCard(task.title, formatClock(task.totalFocusSeconds))
            }

            SectionHeading("Quick actions")

            // Primary CTA
            FilledTonalButton(
                onClick = onNavigateToFocus,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(Dimens.cornerMedium),
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(Dimens.sm))
                Text("Start a focus session", fontWeight = FontWeight.SemiBold)
            }

            // Secondary actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                ActionCard(
                    label = "Tasks",
                    subtitle = "Plan your work",
                    icon = Icons.Outlined.TaskAlt,
                    accent = IndigoSoft,
                    onClick = onNavigateToTasks,
                    modifier = Modifier.weight(1f),
                )
                ActionCard(
                    label = "Flashcards",
                    subtitle = "Review due cards",
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    accent = Amber,
                    onClick = onNavigateToFlashcards,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Dimens.xl))
        }
    }
}

@Composable
private fun HeroProgressCard(
    focusSeconds: Long,
    sessions: Int,
    streakDays: Int,
    onStartFocus: () -> Unit,
) {
    Card(
        onClick = onStartFocus,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cornerLarge),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Indigo, IndigoDark),
                    ),
                    RoundedCornerShape(Dimens.cornerLarge),
                )
                .padding(Dimens.xl),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                Text(
                    "YOUR PROGRESS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Amber,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    formatClock(focusSeconds),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    "focused today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(Modifier.height(Dimens.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StatPill(Icons.Outlined.Bolt, "$sessions", "sessions")
                    StatPill(Icons.Outlined.Whatshot, "${streakDays}d", "streak")
                }
            }
        }
    }
}

@Composable
private fun StatPill(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Amber, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(Dimens.xs))
        Text(
            "$value $label",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ActiveTaskCard(title: String, timeLogged: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.TaskAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.padding(start = Dimens.md)) {
                Text("ACTIVE TASK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("$timeLogged focused", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ActionCard(
    label: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = label, tint = accent)
            }
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
