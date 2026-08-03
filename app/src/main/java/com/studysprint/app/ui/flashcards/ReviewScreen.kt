package com.studysprint.app.ui.flashcards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studysprint.app.spacedrepetition.ReviewQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isFinished) "Session complete"
                        else "Card ${state.reviewedCount + 1} of ${state.totalToReview}"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Exit review")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            if (state.isFinished) {
                Text(
                    "🎉 ${state.reviewedCount} cards reviewed",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "SM-2 has scheduled each card for its optimal next review.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Done")
                }
            } else {
                val current = state.current
                if (current != null) {
                    FlashcardView(
                        front = current.front,
                        back = current.back,
                        isRevealed = state.isAnswerRevealed,
                        onReveal = viewModel::revealAnswer,
                        onRate = viewModel::rate,
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardView(
    front: String,
    back: String,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onRate: (ReviewQuality) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("QUESTION", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                front,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )

            AnimatedVisibility(visible = isRevealed, enter = fadeIn()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "ANSWER",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        back,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }

    if (!isRevealed) {
        Button(onClick = onReveal, modifier = Modifier.fillMaxWidth()) {
            Text("Show answer")
        }
    } else {
        Text(
            "How well did you recall it?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RatingButton("Again", "<1m", ReviewQuality.Again, onRate, Modifier.weight(1f))
            RatingButton("Hard", "6m", ReviewQuality.Hard, onRate, Modifier.weight(1f))
            RatingButton("OK", "1d", ReviewQuality.OK, onRate, Modifier.weight(1f))
            RatingButton("Easy", "4d", ReviewQuality.Easy, onRate, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RatingButton(
    label: String,
    interval: String,
    quality: ReviewQuality,
    onRate: (ReviewQuality) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = { onRate(quality) },
        modifier = modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(interval, style = MaterialTheme.typography.bodySmall)
        }
    }
}
