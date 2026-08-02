package com.studysprint.app.data.model

/**
 * A single completed focus session — one full countdown of a Focus phase.
 * Used to compute statistics (total time, streak, per-task breakdown).
 */
data class FocusSession(
    val id: Long = 0,
    val taskId: Long? = null,
    val durationSeconds: Long,
    val completedAt: Long = System.currentTimeMillis(),
)
