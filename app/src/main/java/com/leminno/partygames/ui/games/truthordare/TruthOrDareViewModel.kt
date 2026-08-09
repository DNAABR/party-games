package com.leminno.partygames.ui.games.truthordare

import androidx.lifecycle.ViewModel
import com.leminno.partygames.data.repository.GameContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TruthOrDareUiState(
    val playerCount: Int = 4,
    val selectedDeck: String = "Clean",
    val activePrompt: String? = null,
    val activePromptType: String? = null, // "TRUTH" or "DARE"
    val truthIndex: Int = 0,
    val dareIndex: Int = 0,
    val truthPrompts: List<String> = emptyList(),
    val darePrompts: List<String> = emptyList()
)

class TruthOrDareViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TruthOrDareUiState())
    val uiState: StateFlow<TruthOrDareUiState> = _uiState.asStateFlow()

    fun initGame(playerCount: Int, deck: String = "Clean") {
        val truths = GameContentRepository.getTruthPrompts(deck).shuffled()
        val dares = GameContentRepository.getDarePrompts(deck).shuffled()

        _uiState.update {
            TruthOrDareUiState(
                playerCount = playerCount,
                selectedDeck = deck,
                truthPrompts = truths,
                darePrompts = dares
            )
        }
    }

    fun selectDeck(deck: String) {
        val truths = GameContentRepository.getTruthPrompts(deck).shuffled()
        val dares = GameContentRepository.getDarePrompts(deck).shuffled()

        _uiState.update {
            it.copy(
                selectedDeck = deck,
                activePrompt = null,
                activePromptType = null,
                truthIndex = 0,
                dareIndex = 0,
                truthPrompts = truths,
                darePrompts = dares
            )
        }
    }

    fun drawTruth() {
        val state = _uiState.value
        if (state.truthPrompts.isEmpty()) return

        val prompt = state.truthPrompts[state.truthIndex % state.truthPrompts.size]
        _uiState.update {
            it.copy(
                activePromptType = "TRUTH",
                activePrompt = prompt,
                truthIndex = it.truthIndex + 1
            )
        }
    }

    fun drawDare() {
        val state = _uiState.value
        if (state.darePrompts.isEmpty()) return

        val prompt = state.darePrompts[state.dareIndex % state.darePrompts.size]
        _uiState.update {
            it.copy(
                activePromptType = "DARE",
                activePrompt = prompt,
                dareIndex = it.dareIndex + 1
            )
        }
    }
}
