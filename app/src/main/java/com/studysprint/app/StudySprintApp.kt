package com.studysprint.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Annotated with [HiltAndroidApp] so Hilt generates the
 * dependency-injection graph at build time. Also configures WorkManager so that
 * [com.studysprint.app.work.ReminderWorker] can be constructed with its injected deps.
 */
@HiltAndroidApp
class StudySprintApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
