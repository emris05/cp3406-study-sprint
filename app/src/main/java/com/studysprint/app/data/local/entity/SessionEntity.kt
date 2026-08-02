package com.studysprint.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a completed focus session. taskId is nullable because the
 * user may focus without selecting a task.
 */
@Entity(
    tableName = "sessions",
    indices = [Index("completed_at"), Index("task_id")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val durationSeconds: Long,
    val completedAt: Long = System.currentTimeMillis(),
)
