package com.studysprint.app.data.repository

import com.studysprint.app.data.local.dao.SessionDao
import com.studysprint.app.data.local.entity.SessionEntity
import com.studysprint.app.data.model.FocusSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for completed focus sessions and the statistics
 * derived from them.
 */
interface SessionRepository {
    fun observeAll(): Flow<List<FocusSession>>
    fun observeTotalFocusSeconds(): Flow<Long>
    fun observeFocusSecondsSince(sinceMillis: Long): Flow<Long>
    fun observeSessionCount(): Flow<Int>
    fun observePerTaskSeconds(): Flow<Map<Long, Long>>
    suspend fun record(taskId: Long?, durationSeconds: Long)
    suspend fun getAllCompletionTimestamps(): List<Long>
    fun observeCompletionTimestamps(): Flow<List<Long>>
    suspend fun clear()
}

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao,
) : SessionRepository {

    override fun observeAll(): Flow<List<FocusSession>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeTotalFocusSeconds(): Flow<Long> = dao.observeTotalFocusSeconds()

    override fun observeFocusSecondsSince(sinceMillis: Long): Flow<Long> =
        dao.observeFocusSecondsSince(sinceMillis)

    override fun observeSessionCount(): Flow<Int> = dao.observeSessionCount()

    override fun observePerTaskSeconds(): Flow<Map<Long, Long>> =
        dao.observePerTaskSeconds().map { rows -> rows.associate { it.taskId to it.totalSeconds } }

    override suspend fun record(taskId: Long?, durationSeconds: Long) {
        dao.insert(SessionEntity(taskId = taskId, durationSeconds = durationSeconds))
    }

    override suspend fun getAllCompletionTimestamps(): List<Long> = dao.getAllCompletionTimestamps()

    override fun observeCompletionTimestamps(): Flow<List<Long>> = dao.observeCompletionTimestamps()

    override suspend fun clear() = dao.clear()
}
