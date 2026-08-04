package com.studysprint.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a study task. Only one task may be "active" at a time;
 * the Repository enforces that invariant when setting a task active.
 *
 * @ColumnInfo maps the Kotlin camelCase property names to snake_case column
 * names, which is what the DAO queries and table indices reference.
 */
@Entity(
    tableName = "tasks",
    indices = [Index("is_active")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    @ColumnInfo(name = "is_active") val isActive: Boolean = false,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "total_focus_seconds") val totalFocusSeconds: Long = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
