package com.studysprint.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studysprint.app.data.model.BreakSuggestion
import com.studysprint.app.ui.theme.Amber
import com.studysprint.app.ui.theme.Dimens

/**
 * Card shown when a break phase begins. Displays the suggested activity and,
 * if weather was available, the conditions that informed the pick.
 */
@Composable
fun BreakSuggestionCard(
    suggestion: BreakSuggestion?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    if (suggestion == null && !isLoading) return

    val isOutdoor = suggestion?.weather?.isNiceOutdoors == true
    val accent = if (isOutdoor) Amber else MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isOutdoor) Icons.Outlined.LightMode else Icons.Outlined.SelfImprovement,
                        contentDescription = null,
                        tint = accent,
                    )
                }
                Spacer(Modifier.size(Dimens.sm))
                Text(
                    text = suggestion?.activity?.title ?: "Picking a break…",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            suggestion?.activity?.description?.let { desc ->
                Text(
                    desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            suggestion?.weather?.let { w ->
                Spacer(Modifier.size(Dimens.xs))
                Text(
                    text = "It's ${w.tempCelsius}° and ${w.condition.lowercase()} in ${w.city}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = accent,
                    fontWeight = FontWeight.Medium,
                )
            }

            if (isLoading && suggestion == null) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = Dimens.xs),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}
