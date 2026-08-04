package com.studysprint.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studysprint.app.R
import com.studysprint.app.ui.theme.DarkModeChoice
import com.studysprint.app.ui.theme.Dimens

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearConfirm by remember { mutableStateOf(false) }

    val s = settings ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.md, vertical = Dimens.lg),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        SettingsGroup("Timer") {
            SliderRow(
                label = stringResource(R.string.settings_focus_length) + " — ${s.focusMinutes}m",
                value = s.focusMinutes.toFloat(),
                range = 5f..90f,
                onChange = { viewModel.setFocusMinutes(it.toInt()) },
            )
            SliderRow(
                label = stringResource(R.string.settings_short_break) + " — ${s.shortBreakMinutes}m",
                value = s.shortBreakMinutes.toFloat(),
                range = 1f..30f,
                onChange = { viewModel.setShortBreak(it.toInt()) },
            )
            SliderRow(
                label = stringResource(R.string.settings_long_break) + " — ${s.longBreakMinutes}m",
                value = s.longBreakMinutes.toFloat(),
                range = 5f..60f,
                onChange = { viewModel.setLongBreak(it.toInt()) },
            )
            SliderRow(
                label = stringResource(R.string.settings_sessions_before_long) + " — ${s.sessionsBeforeLongBreak}",
                value = s.sessionsBeforeLongBreak.toFloat(),
                range = 2f..8f,
                onChange = { viewModel.setSessionsBeforeLong(it.toInt()) },
            )
            SwitchRow(
                label = stringResource(R.string.settings_sound),
                checked = s.soundEnabled,
                onChange = viewModel::setSoundEnabled,
            )
        }

        SettingsGroup("Appearance") {
            Text("Dark mode", style = MaterialTheme.typography.bodyMedium)
            val choices = DarkModeChoice.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                choices.forEachIndexed { index, choice ->
                    SegmentedButton(
                        selected = s.darkMode == choice,
                        onClick = { viewModel.setDarkMode(choice) },
                        shape = SegmentedButtonDefaults.itemShape(index, choices.size),
                    ) {
                        Text(when (choice) {
                            DarkModeChoice.System -> "System"
                            DarkModeChoice.AlwaysDark -> "Dark"
                            DarkModeChoice.AlwaysLight -> "Light"
                        })
                    }
                }
            }
        }

        SettingsGroup("Weather") {
            OutlinedTextField(
                value = s.weatherCity,
                onValueChange = viewModel::setWeatherCity,
                label = { Text(stringResource(R.string.settings_city)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text("Used for break suggestions. No location permission required.")
                },
            )
        }

        SettingsGroup("Reminders") {
            SwitchRow(
                label = stringResource(R.string.settings_reminder_enabled),
                checked = s.reminderEnabled,
                onChange = viewModel::setReminderEnabled,
            )
            if (s.reminderEnabled) {
                Text(
                    "Reminder at %02d:%02d".format(s.reminderHour, s.reminderMinute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SettingsGroup("Your data") {
            Text(
                stringResource(R.string.settings_privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = { showClearConfirm = true },
                shape = RoundedCornerShape(Dimens.cornerMedium),
            ) {
                Text(stringResource(R.string.settings_clear_data))
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all data?") },
            text = { Text("This permanently deletes all your sessions and statistics. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showClearConfirm = false
                }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.cornerMedium),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(value = value, valueRange = range, onValueChange = onChange)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
