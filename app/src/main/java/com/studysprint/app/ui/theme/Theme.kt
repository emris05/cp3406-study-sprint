package com.studysprint.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * App theme. Dark-first by design ("Deep Focus"), but respects the user's choice
 * in Settings or the system default.
 *
 * @param darkModeChoice the user's dark-mode preference; defaults to system.
 */
@Composable
fun StudySprintTheme(
    darkModeChoice: DarkModeChoice = DarkModeChoice.System,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (darkModeChoice) {
        DarkModeChoice.System -> systemDark
        DarkModeChoice.AlwaysDark -> true
        DarkModeChoice.AlwaysLight -> false
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = Indigo,
            onPrimary = DarkOnSurface,
            primaryContainer = IndigoDark,
            onPrimaryContainer = DarkOnSurface,
            secondary = Amber,
            onSecondary = DarkBackground,
            secondaryContainer = AmberDark,
            onSecondaryContainer = DarkOnSurface,
            background = DarkBackground,
            onBackground = DarkOnBackground,
            surface = DarkSurface,
            onSurface = DarkOnSurface,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkOutline,
            outline = DarkOutline,
        )
    } else {
        lightColorScheme(
            primary = Indigo,
            onPrimary = LightSurface,
            primaryContainer = IndigoDark,
            onPrimaryContainer = LightSurface,
            secondary = Amber,
            onSecondary = LightOnBackground,
            secondaryContainer = LightSurfaceVariant,
            onSecondaryContainer = LightOnBackground,
            background = LightBackground,
            onBackground = LightOnBackground,
            surface = LightSurface,
            onSurface = LightOnSurface,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightOutline,
            outline = LightOutline,
        )
    }

    // Match the status / nav bar to the background for an immersive feel.
    val view = LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StudySprintTypography,
        content = content,
    )
}

/** User's dark-mode preference stored in Settings. */
enum class DarkModeChoice { System, AlwaysDark, AlwaysLight }
