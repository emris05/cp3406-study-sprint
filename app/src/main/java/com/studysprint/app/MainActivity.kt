package com.studysprint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studysprint.app.data.repository.SettingsRepository
import com.studysprint.app.ui.nav.StudySprintApp
import com.studysprint.app.ui.theme.DarkModeChoice
import com.studysprint.app.ui.theme.StudySprintTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The single Activity hosting the entire Compose UI.
 *
 * Hilt injects the [SettingsRepository] so the theme can react to the user's
 * dark-mode choice before any screen mounts. All screens live inside the
 * Compose navigation graph; no other activities are used.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.observe().collectAsStateWithLifecycle(
                initialValue = null,
            )
            StudySprintTheme(
                darkModeChoice = settings?.darkMode ?: DarkModeChoice.System,
            ) {
                StudySprintApp()
            }
        }
    }
}
