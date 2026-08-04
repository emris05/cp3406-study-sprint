package com.studysprint.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single flashcard. Belongs to a [DeckEntity]. Carries its SM-2 scheduling
 * state inline (repetitions, ease, interval, due) so reviews are fast and the
 * deck table stays lightweight.
 *
 * Foreign key cascade: deleting a deck deletes its cards.
 */
@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("deck_id"), Index("due_epoch_millis")],
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "deck_id") val deckId: Long,
    val front: String,
    val back: String,
    // SM-2 scheduling state
    val repetitions: Int = 0,
    @ColumnInfo(name = "ease_factor") val easeFactor: Double = 2.5,
    @ColumnInfo(name = "interval_days") val intervalDays: Int = 0,
    @ColumnInfo(name = "due_epoch_millis") val dueEpochMillis: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
