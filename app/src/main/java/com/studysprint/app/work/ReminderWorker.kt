package com.studysprint.app.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studysprint.app.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Posts a single, one-shot "time to focus" notification. Scheduled daily by
 * [ReminderScheduler] when the user enables reminders in Settings.
 *
 * This worker only displays the notification; the scheduling (and rescheduling
 * when the user changes the time) lives in [ReminderScheduler]. This keeps the
 * worker cheap to run and easy to test.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Respect Do Not Disturb — never nag the user when they've muted notifications.
        val manager = context.getSystemService(NotificationManager::class.java) ?: return Result.success()
        if (manager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
            return Result.success()
        }

        // Android 13+ requires runtime POST_NOTIFICATIONS permission. If it was
        // revoked between scheduling and firing, just bail rather than crash.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        ensureChannel(manager)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification) }
        return Result.success()
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.reminder_channel_desc)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private companion object {
        const val CHANNEL_ID = "focus_reminders"
        const val NOTIFICATION_ID = 1001
    }
}
