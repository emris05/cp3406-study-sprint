package com.studysprint.app.ui.flashcards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studysprint.app.spacedrepetition.ReviewQuality
import com.studysprint.app.ui.theme.Amber
import com.studysprint.app.ui.theme.Dimens
import com.studysprint.app.ui.theme.IndigoSoft
import com.studysprint.app.ui.theme.SuccessGreen

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
                        else "Card ${state.reviewedCount + 1} of ${state.totalToReview}",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Exit review")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.lg, Alignment.CenterVertically),
        ) {
            // Progress bar
            if (!state.isFinished && state.totalToReview > 0) {
                LinearProgressIndicator(
                    progress = { state.reviewedCount.toFloat() / state.totalToReview.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = IndigoSoft,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            if (state.isFinished) {
                CompletionCard(state.reviewedCount, onBack)
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
private fun CompletionCard(reviewedCount: Int, onBack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cornerLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            Text(
                "$reviewedCount",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen,
            )
            Text("cards reviewed", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimens.sm))
            Text(
                "SM-2 has scheduled each card for its optimal next review.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.md))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(Dimens.cornerMedium),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoSoft),
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
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
        shape = RoundedCornerShape(Dimens.cornerLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Text(
                "QUESTION",
                style = MaterialTheme.typography.labelLarge,
                color = IndigoSoft,
                fontWeight = FontWeight.Bold,
            )
            Text(
                front,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )

            AnimatedVisibility(
                visible = isRevealed,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(Dimens.sm))
                    Text(
                        "ANSWER",
                        style = MaterialTheme.typography.labelLarge,
                        color = Amber,
                        fontWeight = FontWeight.Bold,
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
        Button(
            onClick = onReveal,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.cornerMedium),
        ) {
            Text("Show answer", fontWeight = FontWeight.SemiBold)
        }
    } else {
        Text(
            "How well did you recall it?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
        ) {
            RatingButton("Again", "<1m", ReviewQuality.Again, onRate, Modifier.weight(1f), Color(0xFFEF5350))
            RatingButton("Hard", "6m", ReviewQuality.Hard, onRate, Modifier.weight(1f), Amber)
            RatingButton("OK", "1d", ReviewQuality.OK, onRate, Modifier.weight(1f), IndigoSoft)
            RatingButton("Easy", "4d", ReviewQuality.Easy, onRate, Modifier.weight(1f), SuccessGreen)
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
    accent: Color,
) {
    OutlinedButton(
        onClick = { onRate(quality) },
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.cornerSmall),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.SemiBold, color = accent)
            Text(interval, style = MaterialTheme.typography.bodySmall)
        }
    }
}
