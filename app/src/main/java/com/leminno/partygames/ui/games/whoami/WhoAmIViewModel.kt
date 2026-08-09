package com.leminno.partygames.ui.games.whoami

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leminno.partygames.data.repository.GameContentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WhoAmIUiState(
    val wordList: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val skips: Int = 0,
    val timeRemaining: Int = 60,
    val isGameOver: Boolean = false,
    val cardFeedbackState: String? = null, // "CORRECT", "SKIP", null
    val isWaitingForUpright: Boolean = false
)

class WhoAmIViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WhoAmIUiState())
    val uiState: StateFlow<WhoAmIUiState> = _uiState.asStateFlow()

    fun initGame(timerSec: Int) {
        _uiState.update {
            WhoAmIUiState(
                wordList = GameContentRepository.whoAmIWords.shuffled(),
                timeRemaining = timerSec
            )
        }
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && !_uiState.value.isGameOver) {
                delay(1000L)
                if (!_uiState.value.isWaitingForUpright) {
                    _uiState.update { currentState ->
                        val nextTime = currentState.timeRemaining - 1
                        currentState.copy(
                            timeRemaining = nextTime,
                            isGameOver = nextTime <= 0
                        )
                    }
                }
            }
        }
    }

    fun onSensorTilt(z: Float) {
        val currentState = _uiState.value
        if (currentState.isGameOver) return

        if (!currentState.isWaitingForUpright) {
            if (z > 7.5f) {
                // Tilted DOWN = Correct
                _uiState.update {
                    it.copy(
                        score = it.score + 1,
                        cardFeedbackState = "CORRECT",
                        isWaitingForUpright = true
                    )
                }
            } else if (z < -7.5f) {
                // Tilted UP = Skip
                _uiState.update {
                    it.copy(
                        skips = it.skips + 1,
                        cardFeedbackState = "SKIP",
                        isWaitingForUpright = true
                    )
                }
            }
        } else {
            // Reset when returned to upright forehead position (~2.0 to 6.5)
            if (z in 2.0f..6.5f) {
                advanceWord()
            }
        }
    }

    fun onManualGotIt() {
        if (_uiState.value.isGameOver) return
        _uiState.update {
            it.copy(
                score = it.score + 1,
                cardFeedbackState = "CORRECT"
            )
        }
        advanceWord()
    }

    fun onManualSkip() {
        if (_uiState.value.isGameOver) return
        _uiState.update {
            it.copy(
                skips = it.skips + 1,
                cardFeedbackState = "SKIP"
            )
        }
        advanceWord()
    }

    private fun advanceWord() {
        _uiState.update { state ->
            val nextIndex = state.currentIndex + 1
            val gameOver = nextIndex >= state.wordList.size
            state.copy(
                currentIndex = nextIndex,
                isWaitingForUpright = false,
                cardFeedbackState = null,
                isGameOver = gameOver
            )
        }
    }
}
