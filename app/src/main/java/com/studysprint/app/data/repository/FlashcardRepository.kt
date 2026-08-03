package com.studysprint.app.data.repository

import com.studysprint.app.data.local.dao.CardDao
import com.studysprint.app.data.local.dao.DeckDao
import com.studysprint.app.data.local.entity.CardEntity
import com.studysprint.app.data.local.entity.DeckEntity
import com.studysprint.app.data.model.Deck
import com.studysprint.app.data.model.Flashcard
import com.studysprint.app.spacedrepetition.CardSchedule
import com.studysprint.app.spacedrepetition.ReviewQuality
import com.studysprint.app.spacedrepetition.Sm2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for flashcard decks, cards, and reviews. The review
 * scheduler surfaces only cards whose due date has passed, so the UI never
 * shows a card before its scheduled interval.
 */
interface FlashcardRepository {
    fun observeDecks(): Flow<List<Deck>>
    fun observeDeck(deckId: Long): Flow<Deck?>
    fun observeCards(deckId: Long): Flow<List<Flashcard>>
    fun observeCardCount(deckId: Long): Flow<Int>
    suspend fun createDeck(name: String, description: String): Long
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(deckId: Long)
    suspend fun addCard(deckId: Long, front: String, back: String): Long
    suspend fun updateCard(card: Flashcard)
    suspend fun deleteCard(cardId: Long)
    suspend fun getDueCards(deckId: Long, nowMillis: Long): List<Flashcard>
    suspend fun getDueCount(deckId: Long, nowMillis: Long): Int
    /** Apply an SM-2 review to a card and persist the updated schedule. */
    suspend fun review(cardId: Long, quality: ReviewQuality, nowMillis: Long)
}

@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
) : FlashcardRepository {

    override fun observeDecks(): Flow<List<Deck>> = deckDao.observeAll().map { decks ->
        decks.map { it.toDomain() }
    }

    override fun observeDeck(deckId: Long): Flow<Deck?> = deckDao.observeById(deckId).map { it?.toDomain() }

    override fun observeCards(deckId: Long): Flow<List<Flashcard>> =
        cardDao.observeByDeck(deckId).map { cards -> cards.map { it.toDomain() } }

    override fun observeCardCount(deckId: Long): Flow<Int> = cardDao.observeCardCount(deckId)

    override suspend fun createDeck(name: String, description: String): Long =
        deckDao.insert(DeckEntity(name = name.trim(), description = description.trim()))

    override suspend fun updateDeck(deck: Deck) = deckDao.update(deck.toEntity())

    override suspend fun deleteDeck(deckId: Long) = deckDao.delete(deckId)

    override suspend fun addCard(deckId: Long, front: String, back: String): Long =
        cardDao.insert(
            CardEntity(
                deckId = deckId,
                front = front.trim(),
                back = back.trim(),
                dueEpochMillis = 0L, // new cards are due immediately
            )
        )

    override suspend fun updateCard(card: Flashcard) = cardDao.update(card.toEntity())

    override suspend fun deleteCard(cardId: Long) = cardDao.delete(cardId)

    override suspend fun getDueCards(deckId: Long, nowMillis: Long): List<Flashcard> =
        cardDao.getDueCards(deckId, nowMillis).map { it.toDomain() }

    override suspend fun getDueCount(deckId: Long, nowMillis: Long): Int =
        cardDao.getDueCount(deckId, nowMillis)

    override suspend fun review(cardId: Long, quality: ReviewQuality, nowMillis: Long) {
        val entity = cardDao.getById(cardId) ?: return
        val current = entity.toDomain().schedule
        val updated = Sm2.review(current, quality, nowMillis)
        cardDao.update(
            entity.copy(
                repetitions = updated.repetitions,
                easeFactor = updated.easeFactor,
                intervalDays = updated.intervalDays,
                dueEpochMillis = updated.dueEpochMillis,
            )
        )
    }
}
