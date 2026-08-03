package com.studysprint.app.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studysprint.app.ui.components.formatClock

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Statistics", style = MaterialTheme.typography.headlineLarge)

        // Headline numbers.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                value = formatClock(state.totalFocusSeconds),
                label = "Total focus",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = state.totalSessions.toString(),
                label = "Sessions",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = "${state.streakDays}d",
                label = "Streak",
                modifier = Modifier.weight(1f),
            )
        }

        // Per-task breakdown.
        if (state.perTaskSeconds.isNotEmpty()) {
            Text("Time per task", style = MaterialTheme.typography.titleMedium)
            val maxSeconds = state.perTaskSeconds.values.maxOrNull() ?: 1L
            state.perTaskSeconds.forEach { (taskId, seconds) ->
                val taskName = state.tasks.firstOrNull { it.id == taskId }?.title ?: "Deleted task"
                PerTaskBar(
                    label = taskName,
                    seconds = seconds,
                    fraction = seconds.toFloat() / maxSeconds.toFloat(),
                )
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PerTaskBar(label: String, seconds: Long, fraction: Float) {
    val barColour = MaterialTheme.colorScheme.primary
    val trackColour = MaterialTheme.colorScheme.surfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(formatClock(seconds), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            drawRect(color = trackColour, size = size)
            drawRect(
                color = barColour,
                size = Size(size.width * fraction.coerceIn(0f, 1f), size.height),
                topLeft = Offset(0f, 0f),
            )
        }
    }
}
