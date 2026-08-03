package com.studysprint.app.ui.flashcards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysprint.app.data.model.Flashcard
import com.studysprint.app.data.repository.FlashcardRepository
import com.studysprint.app.spacedrepetition.ReviewQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Snapshot of a review session in progress. */
data class ReviewUiState(
    val remaining: List<Flashcard> = emptyList(),
    val current: Flashcard? = null,
    val isAnswerRevealed: Boolean = false,
    val reviewedCount: Int = 0,
    val totalToReview: Int = 0,
    val isFinished: Boolean = false,
)

/**
 * Drives a review session: loads due cards, presents them one at a time, and
 * applies an SM-2 review when the user rates their recall.
 */
@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FlashcardRepository,
) : ViewModel() {

    private val deckId: Long = savedStateHandle.get<Long>("deckId") ?: 0L
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init { loadDueCards() }

    private fun loadDueCards() = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val due = repository.getDueCards(deckId, now)
        _uiState.value = ReviewUiState(
            remaining = due.drop(1),
            current = due.firstOrNull(),
            totalToReview = due.size,
        )
    }

    fun revealAnswer() {
        _uiState.value = _uiState.value.copy(isAnswerRevealed = true)
    }

    /** Rate the current card, persist via SM-2, and advance to the next due card. */
    fun rate(quality: ReviewQuality) = viewModelScope.launch {
        val current = _uiState.value.current ?: return@launch
        val now = System.currentTimeMillis()
        repository.review(current.id, quality, now)

        val state = _uiState.value
        val nextRemaining = state.remaining
        _uiState.value = state.copy(
            current = nextRemaining.firstOrNull(),
            remaining = nextRemaining.drop(1),
            isAnswerRevealed = false,
            reviewedCount = state.reviewedCount + 1,
            isFinished = nextRemaining.isEmpty(),
        )
    }
}
