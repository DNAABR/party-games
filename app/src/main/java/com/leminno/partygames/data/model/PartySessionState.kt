package com.leminno.partygames.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerScore(
    val name: String,
    var score: Int = 0
)

object PartySessionManager {
    private val _players = MutableStateFlow<List<PlayerScore>>(
        listOf(
            PlayerScore("Player 1", 0),
            PlayerScore("Player 2", 0),
            PlayerScore("Player 3", 0),
            PlayerScore("Player 4", 0)
        )
    )
    val players: StateFlow<List<PlayerScore>> = _players.asStateFlow()

    fun updatePlayers(names: List<String>) {
        val currentMap = _players.value.associate { it.name to it.score }
        val newScores = names.map { name ->
            PlayerScore(name = name, score = currentMap[name] ?: 0)
        }
        _players.value = newScores
    }

    fun incrementScore(name: String, delta: Int = 1) {
        _players.value = _players.value.map {
            if (it.name.equals(name, ignoreCase = true)) {
                it.copy(score = (it.score + delta).coerceAtLeast(0))
            } else it
        }
    }

    fun decrementScore(name: String) {
        incrementScore(name, -1)
    }

    fun resetScores() {
        _players.value = _players.value.map { it.copy(score = 0) }
    }
}
