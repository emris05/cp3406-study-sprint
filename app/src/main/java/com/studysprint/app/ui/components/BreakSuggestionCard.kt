package com.studysprint.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studysprint.app.data.model.BreakSuggestion

/**
 * Card shown when a break phase begins. Displays the suggested activity and,
 * if weather was available, the conditions that informed the pick.
 *
 * When [isLoading] is true we show a small spinner — the fetch is non-blocking
 * and the card never blocks the timer.
 */
@Composable
fun BreakSuggestionCard(
    suggestion: BreakSuggestion?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    if (suggestion == null && !isLoading) return // Nothing to show yet.

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .semantics { contentDescription = "Break suggestion" },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (suggestion?.weather?.isNiceOutdoors == true) {
                        Icons.Outlined.LightMode
                    } else {
                        Icons.Outlined.SelfImprovement
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = suggestion?.activity?.title ?: "Picking a break…",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            suggestion?.activity?.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            // Weather context line — only when we actually got weather back.
            suggestion?.weather?.let { w ->
                Text(
                    text = "It's ${w.tempCelsius}° and ${w.condition.lowercase()} in ${w.city}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            if (isLoading && suggestion == null) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 4.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}
