package com.studysprint.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralised spacing and sizing tokens. Using these everywhere keeps the
 * layout rhythm consistent (4/8/16/24/32) and makes global tweaks a one-line
 * change instead of hunting through composables.
 */
object Dimens {
    // Spacing rhythm
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp

    // Screen edge padding
    val screenPadding = md

    // Card padding
    val cardPadding = lg

    // Touch targets (Material min is 48dp)
    val touchTarget = 48.dp

    // Timer ring
    val ringDiameter = 280.dp
    val ringStroke = 14.dp

    // Common corner radii
    val cornerSmall = 12.dp
    val cornerMedium = 20.dp
    val cornerLarge = 28.dp

    // Elevated card elevation
    val cardElevation = 2.dp
}
