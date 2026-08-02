package com.studysprint.app.data.repository

import com.studysprint.app.data.local.dao.SettingsDao
import com.studysprint.app.data.local.entity.SettingsEntity
import com.studysprint.app.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for app settings. The DB row is created lazily on
 * first access — if it's missing we persist and return the defaults.
 */
interface SettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun get(): AppSettings
    suspend fun update(transform: (AppSettings) -> AppSettings)
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dao: SettingsDao,
) : SettingsRepository {

    override fun observe(): Flow<AppSettings> =
        dao.observe().map { entity -> (entity ?: AppSettings().toEntity()).toDomain() }

    /** One-shot read. Seeds the default row on first call. */
    override suspend fun get(): AppSettings = ensureRowExists()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(ensureRowExists())
        dao.upsert(updated.toEntity())
    }

    private suspend fun ensureRowExists(): AppSettings {
        val current = dao.observe().first()
        val settings = (current ?: AppSettings().toEntity()).toDomain()
        if (current == null) dao.upsert(settings.toEntity())
        return settings
    }
}
