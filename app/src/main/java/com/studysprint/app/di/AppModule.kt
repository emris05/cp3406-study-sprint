package com.studysprint.app.di

import com.studysprint.app.data.repository.SessionRepository
import com.studysprint.app.data.repository.SessionRepositoryImpl
import com.studysprint.app.data.repository.SettingsRepository
import com.studysprint.app.data.repository.SettingsRepositoryImpl
import com.studysprint.app.data.repository.TaskRepository
import com.studysprint.app.data.repository.TaskRepositoryImpl
import com.studysprint.app.data.repository.WeatherRepository
import com.studysprint.app.data.repository.WeatherRepositoryImpl
import com.studysprint.app.timer.BreakController
import com.studysprint.app.timer.TimerEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their implementations. Keeping this separate
 * from the [DatabaseModule]/[NetworkModule] makes the contract -> impl mapping
 * easy to scan and swap (e.g. for fakes in tests).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    companion object {
        /** Provided here (not constructed in the ViewModel) so tests can swap a fake engine. */
        @Provides @Singleton
        fun provideTimerEngine(): TimerEngine = TimerEngine()

        /** BreakController is deterministic given a seed; default Random is fine for prod. */
        @Provides @Singleton
        fun provideBreakController(): BreakController = BreakController()
    }
}
