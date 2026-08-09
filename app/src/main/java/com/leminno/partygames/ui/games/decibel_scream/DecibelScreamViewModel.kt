package com.leminno.partygames.ui.games.decibel_scream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DecibelChallengeMode(val title: String, val desc: String, val targetGoal: String) {
    WHISPER("Quiet Whisper", "Stay under 20dB while whispering your secret!", "< 20 dB"),
    MAX_SCREAM("Max Volume Spike", "Scream as loud as possible in 3 seconds!", "Peak dB"),
    STEADY_HUM("Steady Hum", "Hold a consistent hum tone for 5 seconds!", "Consistent dB")
}

data class DecibelScreamUiState(
    val selectedMode: DecibelChallengeMode = DecibelChallengeMode.MAX_SCREAM,
    val isListening: Boolean = false,
    val currentDb: Float = 0f,
    val peakDb: Float = 0f,
    val timerRemaining: Int = 5,
    val challengeComplete: Boolean = false
)

class DecibelScreamViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DecibelScreamUiState())
    val uiState: StateFlow<DecibelScreamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun selectMode(mode: DecibelChallengeMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun startChallenge(onChallengeComplete: () -> Unit = {}) {
        timerJob?.cancel()

        _uiState.update {
            it.copy(
                isListening = true,
                currentDb = 0f,
                peakDb = 0f,
                timerRemaining = 5,
                challengeComplete = false
            )
        }

        timerJob = viewModelScope.launch {
            var time = 5
            while (time > 0 && _uiState.value.isListening) {
                delay(1000L)
                time--
                _uiState.update { it.copy(timerRemaining = time) }
            }
            if (time <= 0) {
                _uiState.update {
                    it.copy(
                        isListening = false,
                        challengeComplete = true
                    )
                }
                onChallengeComplete()
            }
        }
    }

    fun updateDbLevel(db: Float) {
        val calculatedDb = db.coerceIn(0f, 120f)
        _uiState.update { state ->
            val newPeak = if (calculatedDb > state.peakDb) calculatedDb else state.peakDb
            state.copy(
                currentDb = calculatedDb,
                peakDb = newPeak
            )
        }
    }

    fun resetChallenge() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isListening = false,
                challengeComplete = false,
                currentDb = 0f,
                peakDb = 0f
            )
        }
    }
}
