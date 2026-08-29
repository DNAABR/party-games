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

import com.leminno.partygames.data.repository.UserPreferencesRepository

data class TruthOrDareUiState(
    val playerCount: Int = 4,
    val players: List<String> = emptyList(),
    val selectedDeck: String = "Clean",
    val timerDurationSec: Int = 60,
    val timeRemaining: Int = 60,
    val isTimerRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isTimesUp: Boolean = false,
    val currentPlayerIndex: Int = 0,
    val activePrompt: String? = null,
    val activePromptType: String? = null, // "TRUTH" or "DARE"
    val truthIndex: Int = 0,
    val dareIndex: Int = 0,
    val truthPrompts: List<String> = emptyList(),
    val darePrompts: List<String> = emptyList()
) {
    val currentPlayerName: String
        get() = players.getOrElse(currentPlayerIndex) { "Player ${currentPlayerIndex + 1}" }
    val currentPlayerTurn: Int
        get() = currentPlayerIndex + 1

    val truthPoints: Int
        get() = when (selectedDeck) {
            "Extreme" -> 2
            else -> 1
        }

    val darePoints: Int
        get() = when (selectedDeck) {
            "Extreme" -> 3
            "Party" -> 2
            else -> 1
        }

    val penaltyPoints: Int
        get() = when (selectedDeck) {
            "Extreme" -> 2
            "Party" -> 1
            else -> 0
        }

    val activePointsEarned: Int
        get() = when (activePromptType) {
            "DARE" -> darePoints
            "TRUTH" -> truthPoints
            else -> 0
        }
}

class TruthOrDareViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TruthOrDareUiState())
    val uiState: StateFlow<TruthOrDareUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun initGame(playerCount: Int, deck: String = "Clean", timerSec: Int = 60) {
        timerJob?.cancel()
        val truths = GameContentRepository.getTruthPrompts(deck).shuffled()
        val dares = GameContentRepository.getDarePrompts(deck).shuffled()
        val activePlayers = UserPreferencesRepository.getActiveRoster(playerCount)

        _uiState.update {
            TruthOrDareUiState(
                playerCount = playerCount.coerceAtLeast(1),
                players = activePlayers,
                selectedDeck = deck,
                timerDurationSec = timerSec,
                timeRemaining = timerSec,
                currentPlayerIndex = 0,
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

        val currentPrompts = state.truthPrompts
        val currentIndex = state.truthIndex

        val (activeList, nextIndex, prompt) = if (currentIndex >= currentPrompts.size) {
            val lastPrompt = currentPrompts.lastOrNull()
            var reshuffled = currentPrompts.shuffled()
            if (reshuffled.size > 1 && reshuffled.first() == lastPrompt) {
                val swapIdx = (1 until reshuffled.size).random()
                val mutable = reshuffled.toMutableList()
                val temp = mutable[0]
                mutable[0] = mutable[swapIdx]
                mutable[swapIdx] = temp
                reshuffled = mutable
            }
            Triple(reshuffled, 1, reshuffled[0])
        } else {
            Triple(currentPrompts, currentIndex + 1, currentPrompts[currentIndex])
        }

        _uiState.update {
            it.copy(
                truthPrompts = activeList,
                activePromptType = "TRUTH",
                activePrompt = prompt,
                truthIndex = nextIndex,
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

        val currentPrompts = state.darePrompts
        val currentIndex = state.dareIndex

        val (activeList, nextIndex, prompt) = if (currentIndex >= currentPrompts.size) {
            val lastPrompt = currentPrompts.lastOrNull()
            var reshuffled = currentPrompts.shuffled()
            if (reshuffled.size > 1 && reshuffled.first() == lastPrompt) {
                val swapIdx = (1 until reshuffled.size).random()
                val mutable = reshuffled.toMutableList()
                val temp = mutable[0]
                mutable[0] = mutable[swapIdx]
                mutable[swapIdx] = temp
                reshuffled = mutable
            }
            Triple(reshuffled, 1, reshuffled[0])
        } else {
            Triple(currentPrompts, currentIndex + 1, currentPrompts[currentIndex])
        }

        _uiState.update {
            it.copy(
                darePrompts = activeList,
                activePromptType = "DARE",
                activePrompt = prompt,
                dareIndex = nextIndex,
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
            val numPlayers = state.players.size.coerceAtLeast(1)
            val nextIndex = (state.currentPlayerIndex + 1) % numPlayers
            state.copy(
                activePrompt = null,
                activePromptType = null,
                isTimerRunning = false,
                isPaused = false,
                isTimesUp = false,
                timeRemaining = state.timerDurationSec,
                currentPlayerIndex = nextIndex
            )
        }
    }

    fun nextTurn() {
        timerJob?.cancel()
        _uiState.update { state ->
            val numPlayers = state.players.size.coerceAtLeast(1)
            val nextIndex = (state.currentPlayerIndex + 1) % numPlayers
            state.copy(
                activePrompt = null,
                activePromptType = null,
                isTimerRunning = false,
                isPaused = false,
                isTimesUp = false,
                timeRemaining = state.timerDurationSec,
                currentPlayerIndex = nextIndex
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
