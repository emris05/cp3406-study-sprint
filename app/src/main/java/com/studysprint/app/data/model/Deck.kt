package com.studysprint.app.data.model

import com.studysprint.app.spacedrepetition.CardSchedule

/** A named deck of flashcards. */
data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val cardCount: Int = 0,
    val dueCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

/** A single flashcard with its SM-2 scheduling state. */
data class Flashcard(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val schedule: CardSchedule = CardSchedule(),
    val createdAt: Long = System.currentTimeMillis(),
)
