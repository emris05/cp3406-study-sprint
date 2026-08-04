package com.studysprint.app.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysprint.app.data.model.Deck
import com.studysprint.app.data.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the deck list screen. */
data class DeckListUiState(
    val decks: List<Deck> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class DeckListViewModel @Inject constructor(
    private val repository: FlashcardRepository,
) : ViewModel() {

    val uiState: StateFlow<DeckListUiState> = repository.observeDecks()
        .map { decks -> DeckListUiState(decks = decks, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DeckListUiState(),
        )

    fun createDeck(name: String, description: String) = viewModelScope.launch {
        if (name.isNotBlank()) repository.createDeck(name, description)
    }

    fun deleteDeck(deckId: Long) = viewModelScope.launch {
        repository.deleteDeck(deckId)
    }
}
