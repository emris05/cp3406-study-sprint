package com.studysprint.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studysprint.app.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY createdAt DESC")
    fun observeByDeck(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND dueEpochMillis <= :nowMillis ORDER BY dueEpochMillis ASC")
    suspend fun getDueCards(deckId: Long, nowMillis: Long): List<CardEntity>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    fun observeCardCount(deckId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND dueEpochMillis <= :nowMillis")
    suspend fun getDueCount(deckId: Long, nowMillis: Long): Int

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun delete(id: Long)
}
