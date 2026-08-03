package com.studysprint.app.di

import android.content.Context
import androidx.room.Room
import com.studysprint.app.data.local.AppDatabase
import com.studysprint.app.data.local.dao.SessionDao
import com.studysprint.app.data.local.dao.SettingsDao
import com.studysprint.app.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "studysprint.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()
}
