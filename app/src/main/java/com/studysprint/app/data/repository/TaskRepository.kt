package com.studysprint.app.data.repository

import com.studysprint.app.data.local.dao.TaskDao
import com.studysprint.app.data.model.StudyTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for study tasks. Wraps [TaskDao] and enforces the
 * "only one active task" invariant.
 */
interface TaskRepository {
    fun observeActiveTasks(): Flow<List<StudyTask>>
    fun observeActiveTask(): Flow<StudyTask?>
    suspend fun create(title: String, notes: String): Long
    suspend fun setActive(taskId: Long)
    suspend fun addFocusSeconds(taskId: Long, seconds: Long)
    suspend fun markCompleted(taskId: Long)
    suspend fun delete(taskId: Long)
    suspend fun getById(taskId: Long): StudyTask?
    suspend fun clearAll()
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
) : TaskRepository {

    override fun observeActiveTasks(): Flow<List<StudyTask>> =
        dao.observeActiveTasks().map { list -> list.map { it.toDomain() } }

    override fun observeActiveTask(): Flow<StudyTask?> =
        dao.observeActiveTask().map { it?.toDomain() }

    override suspend fun create(title: String, notes: String): Long =
        dao.insert(TaskDaoSeed.newTask(title, notes))

    override suspend fun setActive(taskId: Long) {
        dao.clearActiveFlag()
        dao.getById(taskId)?.let { dao.update(it.copy(isActive = true)) }
    }

    override suspend fun addFocusSeconds(taskId: Long, seconds: Long) =
        dao.addFocusSeconds(taskId, seconds)

    override suspend fun markCompleted(taskId: Long) = dao.markCompleted(taskId)

    override suspend fun delete(taskId: Long) = dao.delete(taskId)

    override suspend fun getById(taskId: Long): StudyTask? = dao.getById(taskId)?.toDomain()

    override suspend fun clearAll() = dao.clearAll()
}

/** Helper for creating a fresh task entity without leaking Room types into callers. */
internal object TaskDaoSeed {
    fun newTask(title: String, notes: String) =
        com.studysprint.app.data.local.entity.TaskEntity(title = title.trim(), notes = notes.trim())
}
