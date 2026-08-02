package com.studysprint.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a study task. Only one task may be "active" at a time;
 * the Repository enforces that invariant when setting a task active.
 */
@Entity(
    tableName = "tasks",
    indices = [Index("is_active")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    @JvmName("isActive") val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val totalFocusSeconds: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
