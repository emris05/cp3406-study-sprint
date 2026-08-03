package com.studysprint.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import com.studysprint.app.data.local.dao.CardDao
import com.studysprint.app.data.local.dao.DeckDao
import com.studysprint.app.data.local.dao.SessionDao
import com.studysprint.app.data.local.dao.TaskDao
import com.studysprint.app.data.local.dao.SettingsDao
import com.studysprint.app.data.local.entity.CardEntity
import com.studysprint.app.data.local.entity.DeckEntity
import com.studysprint.app.data.local.entity.SessionEntity
import com.studysprint.app.data.local.entity.SettingsEntity
import com.studysprint.app.data.local.entity.TaskEntity

/**
 * The app's Room database. All user data lives here, on-device only.
 *
 * Versioning: bump [version] for any schema change and add a migration or
 * fallback strategy in [di.DatabaseModule].
 */
@Database(
    entities = [
        TaskEntity::class,
        SessionEntity::class,
        SettingsEntity::class,
        DeckEntity::class,
        CardEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun sessionDao(): SessionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
}
