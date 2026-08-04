package com.studysprint.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import com.studysprint.app.ui.theme.Dimens
import com.studysprint.app.ui.theme.DarkGradientBottom
import com.studysprint.app.ui.theme.DarkGradientTop
import com.studysprint.app.ui.theme.LightGradientBottom
import com.studysprint.app.ui.theme.LightGradientTop

/**
 * A subtle vertical-gradient screen background. Gives the dark theme depth
 * without distracting from content. The gradient direction is chosen from the
 * active colour scheme's surface luminance, so it respects the user's setting
 * (not just the system default).
 */
@Composable
fun GradientScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val top = if (isDark) DarkGradientTop else LightGradientTop
    val bottom = if (isDark) DarkGradientBottom else LightGradientBottom

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(top, bottom)))
            .padding(horizontal = Dimens.screenPadding),
        content = content,
    )
}

/**
 * Consistent section heading used across screens.
 */
@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = Dimens.lg, bottom = Dimens.sm),
    )
}
