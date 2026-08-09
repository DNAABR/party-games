package com.leminno.partygames.ui.games.connectfour

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ConnectFourUiState(
    val grid: List<IntArray> = List(6) { IntArray(7) },
    val isRedTurn: Boolean = true,
    val winnerPlayer: Int? = null, // 1, 2, or null
    val isDraw: Boolean = false
)

class ConnectFourViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectFourUiState())
    val uiState: StateFlow<ConnectFourUiState> = _uiState.asStateFlow()

    fun dropDisc(col: Int, onWin: () -> Unit = {}, onDraw: () -> Unit = {}) {
        val currentState = _uiState.value
        if (currentState.winnerPlayer != null || currentState.isDraw) return

        val newGrid = currentState.grid.map { it.clone() }
        for (row in 5 downTo 0) {
            if (newGrid[row][col] == 0) {
                val targetPlayer = if (currentState.isRedTurn) 1 else 2
                newGrid[row][col] = targetPlayer

                if (checkWin(newGrid, row, col, targetPlayer)) {
                    _uiState.update {
                        it.copy(
                            grid = newGrid,
                            winnerPlayer = targetPlayer
                        )
                    }
                    onWin()
                } else if (newGrid.all { r -> r.all { it != 0 } }) {
                    _uiState.update {
                        it.copy(
                            grid = newGrid,
                            isDraw = true
                        )
                    }
                    onDraw()
                } else {
                    _uiState.update {
                        it.copy(
                            grid = newGrid,
                            isRedTurn = !it.isRedTurn
                        )
                    }
                }
                break
            }
        }
    }

    fun resetGame() {
        _uiState.update {
            ConnectFourUiState(
                grid = List(6) { IntArray(7) },
                isRedTurn = true,
                winnerPlayer = null,
                isDraw = false
            )
        }
    }

    private fun checkWin(grid: List<IntArray>, row: Int, col: Int, player: Int): Boolean {
        val directions = listOf(
            Pair(0, 1),  // Horizontal
            Pair(1, 0),  // Vertical
            Pair(1, 1),  // Diagonal right
            Pair(1, -1)  // Diagonal left
        )

        for ((dr, dc) in directions) {
            var count = 1
            // Check positive direction
            var r = row + dr
            var c = col + dc
            while (r in 0..5 && c in 0..6 && grid[r][c] == player) {
                count++
                r += dr
                c += dc
            }
            // Check negative direction
            r = row - dr
            c = col - dc
            while (r in 0..5 && c in 0..6 && grid[r][c] == player) {
                count++
                r -= dr
                c -= dc
            }

            if (count >= 4) return true
        }
        return false
    }
}
