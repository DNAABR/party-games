package com.leminno.partygames.ui.games.ultimate_ttt

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*

enum class CellState(val symbol: String, val color: Color) {
    EMPTY("", Color.Transparent),
    PLAYER_X("❌", Color(0xFF00F2FE)),
    PLAYER_O("⭕", Color(0xFFFF007F))
}

enum class TttPowerUp(val title: String, val icon: String, val desc: String) {
    ERASE("Erase", "🧹", "Wipe 1 opponent cell!"),
    SHIELD("Shield", "🛡️", "Lock 1 cell from erase!"),
    EXTRA_TURN("Extra Turn", "⚡", "Take 2 moves in a row!")
}

data class BoardCell(
    val state: CellState = CellState.EMPTY,
    val isShielded: Boolean = false
)

@Composable
fun PowerUpTTTScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var board by remember { mutableStateOf(List(9) { BoardCell() }) }
    var isPlayerXTurn by remember { mutableStateOf(true) }
    var selectedPowerUp by remember { mutableStateOf<TttPowerUp?>(null) }

    // Inventory of power ups per player (1 each per game)
    var playerXPowers by remember { mutableStateOf(setOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)) }
    var playerOPowers by remember { mutableStateOf(setOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)) }

    var winner by remember { mutableStateOf<CellState?>(null) }
    var winningLine by remember { mutableStateOf<List<Int>?>(null) }

    val currentTurnState = if (isPlayerXTurn) CellState.PLAYER_X else CellState.PLAYER_O
    val currentPowerInventory = if (isPlayerXTurn) playerXPowers else playerOPowers

    fun checkWinCondition(currentBoard: List<BoardCell>): Pair<CellState?, List<Int>?> {
        val winningCombinations = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )

        for (combo in winningCombinations) {
            val (a, b, c) = combo
            if (currentBoard[a].state != CellState.EMPTY &&
                currentBoard[a].state == currentBoard[b].state &&
                currentBoard[a].state == currentBoard[c].state
            ) {
                return Pair(currentBoard[a].state, combo)
            }
        }
        return Pair(null, null)
    }

    fun handleCellClick(index: Int) {
        val cell = board[index]

        if (selectedPowerUp != null) {
            when (selectedPowerUp) {
                TttPowerUp.ERASE -> {
                    if (cell.state != CellState.EMPTY && cell.state != currentTurnState && !cell.isShielded) {
                        haptics.performHeavyBurst()
                        val updated = board.toMutableList()
                        updated[index] = BoardCell(CellState.EMPTY, false)
                        board = updated
                        if (isPlayerXTurn) playerXPowers = playerXPowers - TttPowerUp.ERASE else playerOPowers = playerOPowers - TttPowerUp.ERASE
                        selectedPowerUp = null
                        isPlayerXTurn = !isPlayerXTurn
                    }
                }
                TttPowerUp.SHIELD -> {
                    if (cell.state == currentTurnState && !cell.isShielded) {
                        haptics.performPop()
                        val updated = board.toMutableList()
                        updated[index] = cell.copy(isShielded = true)
                        board = updated
                        if (isPlayerXTurn) playerXPowers = playerXPowers - TttPowerUp.SHIELD else playerOPowers = playerOPowers - TttPowerUp.SHIELD
                        selectedPowerUp = null
                        isPlayerXTurn = !isPlayerXTurn
                    }
                }
                TttPowerUp.EXTRA_TURN -> {
                    if (cell.state == CellState.EMPTY) {
                        haptics.performTick(composeHaptics)
                        val updated = board.toMutableList()
                        updated[index] = BoardCell(currentTurnState, false)
                        board = updated
                        val (win, line) = checkWinCondition(updated)
                        if (win != null) {
                            winner = win
                            winningLine = line
                        } else {
                            if (isPlayerXTurn) playerXPowers = playerXPowers - TttPowerUp.EXTRA_TURN else playerOPowers = playerOPowers - TttPowerUp.EXTRA_TURN
                            selectedPowerUp = null
                        }
                    }
                }
                null -> {}
            }
        } else {
            if (cell.state == CellState.EMPTY && winner == null) {
                haptics.performTick(composeHaptics)
                val updated = board.toMutableList()
                updated[index] = BoardCell(currentTurnState, false)
                board = updated

                val (win, line) = checkWinCondition(updated)
                if (win != null) {
                    haptics.performPop()
                    winner = win
                    winningLine = line
                } else {
                    isPlayerXTurn = !isPlayerXTurn
                }
            }
        }
    }

    GameScaffold(
        title = "POWER-UP TIC-TAC-TOE ⚡",
        titleColor = currentTurnState.color,
        gameId = "ultimate_ttt",
        onExitGame = onExitGame
    ) {
        if (winner == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Turn Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(currentTurnState.color.copy(alpha = 0.2f))
                        .border(1.5.dp, currentTurnState.color, RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "TURN: ${currentTurnState.symbol} ${currentTurnState.name.replace("_", " ")}",
                        color = currentTurnState.color,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // 3x3 Grid Canvas Board
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0..2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                val cell = board[index]
                                val isHighlighted = winningLine?.contains(index) == true

                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isHighlighted) currentTurnState.color.copy(alpha = 0.4f) else SurfaceGlassDark)
                                        .border(
                                            width = if (isHighlighted) 3.dp else 1.5.dp,
                                            color = if (isHighlighted) currentTurnState.color else BorderGlassDefault,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .clickable { handleCellClick(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell.state.symbol,
                                        fontSize = 38.sp
                                    )

                                    if (cell.isShielded) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                        ) {
                                            Text("🛡️", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Power-Ups Inventory Bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "AVAILABLE POWER-UPS (${currentTurnState.symbol})",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TttPowerUp.entries.forEach { power ->
                            val isAvailable = currentPowerInventory.contains(power)
                            val isSelected = selectedPowerUp == power

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) currentTurnState.color.copy(alpha = 0.3f) else SurfaceGlassDark)
                                    .border(
                                        1.dp,
                                        if (isSelected) currentTurnState.color else BorderGlassDefault,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = isAvailable) {
                                        haptics.performTick(composeHaptics)
                                        selectedPowerUp = if (selectedPowerUp == power) null else power
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = power.icon, fontSize = 20.sp)
                                    Text(
                                        text = power.title,
                                        color = if (isAvailable) TextPrimary else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "PLAYER ${winner?.symbol} WINS! 🎉",
                subtitle = "Tic-Tac-Toe Mastermind!",
                onPlayAgain = {
                    board = List(9) { BoardCell() }
                    winner = null
                    winningLine = null
                    isPlayerXTurn = true
                    selectedPowerUp = null
                    playerXPowers = setOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)
                    playerOPowers = setOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)
                },
                onBackToHub = onExitGame
            )
        }
    }
}
