package com.studysprint.app.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studysprint.app.ui.components.GradientScreen
import com.studysprint.app.ui.components.SectionHeading
import com.studysprint.app.ui.components.formatClock
import com.studysprint.app.ui.theme.Amber
import com.studysprint.app.ui.theme.Dimens
import com.studysprint.app.ui.theme.IndigoSoft
import com.studysprint.app.ui.theme.SuccessGreen

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    GradientScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Text("Statistics", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

            // Headline stat cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                BigStatCard(
                    value = formatClock(state.totalFocusSeconds),
                    label = "Total focus",
                    icon = null,
                    accent = IndigoSoft,
                    modifier = Modifier.weight(1.4f),
                )
                BigStatCard(
                    value = state.totalSessions.toString(),
                    label = "Sessions",
                    icon = null,
                    accent = SuccessGreen,
                    modifier = Modifier.weight(1f),
                )
                BigStatCard(
                    value = "${state.streakDays}d",
                    label = "Streak",
                    icon = null,
                    accent = Amber,
                    modifier = Modifier.weight(1f),
                )
            }

            if (state.perTaskSeconds.isNotEmpty()) {
                SectionHeading("Time per task")
                val maxSeconds = state.perTaskSeconds.values.maxOrNull() ?: 1L
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                    state.perTaskSeconds.forEach { (taskId, seconds) ->
                        val taskName = state.tasks.firstOrNull { it.id == taskId }?.title ?: "Deleted task"
                        PerTaskBar(
                            label = taskName,
                            seconds = seconds,
                            fraction = seconds.toFloat() / maxSeconds.toFloat(),
                        )
                    }
                }
            } else {
                EmptyStatsHint()
            }
        }
    }
}

@Composable
private fun BigStatCard(
    value: String,
    label: String,
    icon: ImageVector?,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.xs),
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PerTaskBar(label: String, seconds: Long, fraction: Float) {
    val barBrush = Brush.horizontalGradient(listOf(IndigoSoft, IndigoSoft.copy(alpha = 0.6f)))
    val trackColour = MaterialTheme.colorScheme.surfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                formatClock(seconds),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
            val corner = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            drawRoundRect(color = trackColour, size = size, cornerRadius = corner)
            drawRoundRect(
                brush = barBrush,
                size = Size(size.width * fraction.coerceIn(0f, 1f), size.height),
                topLeft = Offset(0f, 0f),
                cornerRadius = corner,
            )
        }
    }
}

@Composable
private fun EmptyStatsHint() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            "Complete a focus session to see your stats here.",
            modifier = Modifier.padding(Dimens.lg),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
