package com.studysprint.app.data.model

/**
 * A study task the user can focus on. Time logged against it accumulates as
 * focus sessions are completed while the task is active.
 */
data class StudyTask(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val totalFocusSeconds: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
