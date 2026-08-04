package com.studysprint.app.ui.flashcards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysprint.app.data.model.Flashcard
import com.studysprint.app.data.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeckDetailUiState(
    val deckId: Long = 0,
    val deckName: String = "",
    val cards: List<Flashcard> = emptyList(),
    val dueCount: Int = 0,
)

/** Manages a single deck: list its cards, add/edit/delete, count due. */
@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FlashcardRepository,
) : ViewModel() {

    private val deckId: Long = savedStateHandle.get<String>("deckId")?.toLongOrNull() ?: 0L

    val uiState: StateFlow<DeckDetailUiState> = repository.observeCards(deckId)
        .map { cards ->
            val now = System.currentTimeMillis()
            DeckDetailUiState(
                deckId = deckId,
                cards = cards,
                dueCount = cards.count { it.schedule.dueEpochMillis <= now },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DeckDetailUiState(),
        )

    fun addCard(front: String, back: String) = viewModelScope.launch {
        if (front.isNotBlank() && back.isNotBlank()) repository.addCard(deckId, front, back)
    }

    fun deleteCard(cardId: Long) = viewModelScope.launch {
        repository.deleteCard(cardId)
    }
}
