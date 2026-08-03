package com.studysprint.app.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Alerts the user when a timer phase completes. Plays a short tone (when sound
 * is enabled) and a subtle haptic vibration (always — vibrations are quiet and
 * appropriate for a focus app used in a library).
 *
 * Kept behind an interface so tests can substitute a no-op implementation
 * instead of needing a real Android audio service on the JVM.
 */
interface PhaseAlerter {
    fun phaseComplete(soundEnabled: Boolean)
}

@Singleton
class PhaseAlerterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PhaseAlerter {

    // ToneGenerator is Android-only but cheap to create. Failures (e.g. in
    // unit tests) are swallowed so a missing audio service never crashes the app.
    override fun phaseComplete(soundEnabled: Boolean) {
        vibrate()
        if (soundEnabled) playTone()
    }

    private fun playTone() {
        runCatching {
            // TONE_PROP_BEEP is a short, gentle chime — less jarring than a ringtone.
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80).use { tone ->
                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            }
        }
    }

    private fun vibrate() {
        runCatching {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService<VibratorManager>()?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService<Vibrator>()
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(150)
                }
            }
        }
    }
}

/** A no-op alerter for unit tests, so tests never touch real audio/haptics. */
class NoOpPhaseAlerter : PhaseAlerter {
    override fun phaseComplete(soundEnabled: Boolean) { /* no-op */ }
}
