package com.studysprint.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studysprint.app.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY completed_at DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity): Long

    @Query("SELECT IFNULL(SUM(duration_seconds), 0) FROM sessions")
    fun observeTotalFocusSeconds(): Flow<Long>

    @Query("SELECT COUNT(*) FROM sessions")
    fun observeSessionCount(): Flow<Int>

    @Query(
        """
        SELECT task_id AS taskId, SUM(duration_seconds) AS totalSeconds
        FROM sessions
        WHERE task_id IS NOT NULL
        GROUP BY task_id
        """
    )
    fun observePerTaskSeconds(): Flow<List<TaskDurationProjection>>

    @Query("SELECT completed_at FROM sessions ORDER BY completed_at DESC")
    suspend fun getAllCompletionTimestamps(): List<Long>

    @Query("DELETE FROM sessions")
    suspend fun clear()
}

/** Result of the per-task duration aggregation. */
data class TaskDurationProjection(
    val taskId: Long,
    val totalSeconds: Long,
)
