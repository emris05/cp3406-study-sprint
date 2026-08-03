package com.studysprint.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A circular progress ring with the countdown time drawn in the centre.
 *
 * The ring depletes clockwise as the phase progresses. Colour follows the
 * Material theme so it adapts to focus vs. break phases (set by the caller).
 *
 * @param totalSeconds phase length
 * @param remainingSeconds seconds left
 * @param progressColour ring colour (indigo for focus, amber for breaks)
 * @param contentDescription spoken to screen-reader users (e.g. "12 minutes 30 seconds remaining")
 */
@Composable
fun CountdownRing(
    totalSeconds: Long,
    remainingSeconds: Long,
    progressColour: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 280.dp,
    strokeWidth: Dp = 14.dp,
) {
    val fraction = if (totalSeconds <= 0) 0f else (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    val trackColour = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .size(diameter)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = Stroke(width = strokeWidth.toPx())
            val arcSize = Size(size.minDimension, size.minDimension)
            val topLeft = Offset(0f, 0f)

            // Background track (full circle).
            drawArc(
                color = trackColour,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            // Progress arc — shrinks as time runs out.
            drawArc(
                color = progressColour,
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        // mm:ss label in the centre.
        Text(
            text = formatClock(remainingSeconds),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Format seconds as m:ss (or h:mm:ss over an hour). Pure so it's testable. */
fun formatClock(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val hours = s / 3600
    val minutes = (s % 3600) / 60
    val seconds = s % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}
