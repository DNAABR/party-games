package com.leminno.partygames.ui.games.truthordare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leminno.partygames.data.repository.GameContentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TruthOrDareUiState(
    val playerCount: Int = 4,
    val selectedDeck: String = "Clean",
    val timerDurationSec: Int = 60,
    val timeRemaining: Int = 60,
    val isTimerRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isTimesUp: Boolean = false,
    val currentPlayerTurn: Int = 1,
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

    private var timerJob: Job? = null

    fun initGame(playerCount: Int, deck: String = "Clean", timerSec: Int = 60) {
        timerJob?.cancel()
        val truths = GameContentRepository.getTruthPrompts(deck).shuffled()
        val dares = GameContentRepository.getDarePrompts(deck).shuffled()

        _uiState.update {
            TruthOrDareUiState(
                playerCount = playerCount.coerceAtLeast(1),
                selectedDeck = deck,
                timerDurationSec = timerSec,
                timeRemaining = timerSec,
                currentPlayerTurn = 1,
                truthPrompts = truths,
                darePrompts = dares
            )
        }
    }

    fun selectDeck(deck: String) {
        timerJob?.cancel()
        val truths = GameContentRepository.getTruthPrompts(deck).shuffled()
        val dares = GameContentRepository.getDarePrompts(deck).shuffled()

        _uiState.update {
            it.copy(
                selectedDeck = deck,
                activePrompt = null,
                activePromptType = null,
                isTimerRunning = false,
                isPaused = false,
                isTimesUp = false,
                timeRemaining = it.timerDurationSec,
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
                truthIndex = it.truthIndex + 1,
                isTimesUp = false,
                isPaused = false,
                timeRemaining = it.timerDurationSec,
                isTimerRunning = true
            )
        }
        startTimer()
    }

    fun drawDare() {
        val state = _uiState.value
        if (state.darePrompts.isEmpty()) return

        val prompt = state.darePrompts[state.dareIndex % state.darePrompts.size]
        _uiState.update {
            it.copy(
                activePromptType = "DARE",
                activePrompt = prompt,
                dareIndex = it.dareIndex + 1,
                isTimesUp = false,
                isPaused = false,
                timeRemaining = it.timerDurationSec,
                isTimerRunning = true
            )
        }
        startTimer()
    }

    fun togglePauseTimer() {
        _uiState.update {
            it.copy(isPaused = !it.isPaused)
        }
    }

    fun completeFate() {
        timerJob?.cancel()
        _uiState.update { state ->
            val nextTurn = if (state.playerCount > 0) (state.currentPlayerTurn % state.playerCount) + 1 else 1
            state.copy(
                activePrompt = null,
                activePromptType = null,
                isTimerRunning = false,
                isPaused = false,
                isTimesUp = false,
                timeRemaining = state.timerDurationSec,
                currentPlayerTurn = nextTurn
            )
        }
    }

    fun nextTurn() {
        timerJob?.cancel()
        _uiState.update { state ->
            val nextTurn = if (state.playerCount > 0) (state.currentPlayerTurn % state.playerCount) + 1 else 1
            state.copy(
                activePrompt = null,
                activePromptType = null,
                isTimerRunning = false,
                isPaused = false,
                isTimesUp = false,
                timeRemaining = state.timerDurationSec,
                currentPlayerTurn = nextTurn
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && _uiState.value.isTimerRunning && !_uiState.value.isTimesUp) {
                delay(1000L)
                if (!_uiState.value.isPaused) {
                    _uiState.update { state ->
                        val nextTime = state.timeRemaining - 1
                        val timesUp = nextTime <= 0
                        state.copy(
                            timeRemaining = nextTime.coerceAtLeast(0),
                            isTimesUp = timesUp,
                            isTimerRunning = !timesUp
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
