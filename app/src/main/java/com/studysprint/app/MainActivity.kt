package com.studysprint.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
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
 * dark-mode choice before any screen mounts. Also requests POST_NOTIFICATIONS
 * permission on Android 13+ if the user has reminders enabled, since the daily
 * reminder worker cannot post without it.
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

            // Request notification permission when reminders are enabled (Android 13+).
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { _ -> /* Result is reflected in whether notifications appear; nothing to do here. */ }
            LaunchedEffect(settings?.reminderEnabled) {
                val reminderOn = settings?.reminderEnabled == true
                if (reminderOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            StudySprintTheme(
                darkModeChoice = settings?.darkMode ?: DarkModeChoice.System,
            ) {
                StudySprintApp()
            }
        }
    }
}
