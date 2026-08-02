package com.studysprint.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studysprint.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY is_active DESC, created_at DESC")
    fun observeActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE is_active = 1 LIMIT 1")
    fun observeActiveTask(): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET is_active = 0")
    suspend fun clearActiveFlag()

    @Query("UPDATE tasks SET total_focus_seconds = total_focus_seconds + :seconds WHERE id = :taskId")
    suspend fun addFocusSeconds(taskId: Long, seconds: Long)

    @Query("UPDATE tasks SET is_completed = 1, is_active = 0 WHERE id = :taskId")
    suspend fun markCompleted(taskId: Long)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun delete(taskId: Long)
}
