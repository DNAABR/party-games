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
    var playerXPowers by remember { mutableStateOf(mutableSetOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)) }
    var playerOPowers by remember { mutableStateOf(mutableSetOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)) }

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
                    // Cannot erase empty or shielded cell
                    if (cell.state != CellState.EMPTY && cell.state != currentTurnState && !cell.isShielded) {
                        haptics.performHeavyClick(composeHaptics)
                        val updated = board.toMutableList()
                        updated[index] = BoardCell(CellState.EMPTY, false)
                        board = updated
                        if (isPlayerXTurn) playerXPowers.remove(TttPowerUp.ERASE) else playerOPowers.remove(TttPowerUp.ERASE)
                        selectedPowerUp = null
                        isPlayerXTurn = !isPlayerXTurn
                    }
                }
                TttPowerUp.SHIELD -> {
                    if (cell.state == currentTurnState && !cell.isShielded) {
                        haptics.performSuccess()
                        val updated = board.toMutableList()
                        updated[index] = cell.copy(isShielded = true)
                        board = updated
                        if (isPlayerXTurn) playerXPowers.remove(TttPowerUp.SHIELD) else playerOPowers.remove(TttPowerUp.SHIELD)
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
                            if (isPlayerXTurn) playerXPowers.remove(TttPowerUp.EXTRA_TURN) else playerOPowers.remove(TttPowerUp.EXTRA_TURN)
                            selectedPowerUp = null
                            // Keep turn for extra move!
                        }
                    }
                }
                null -> {}
            }
        } else {
            // Normal placement
            if (cell.state == CellState.EMPTY && winner == null) {
                haptics.performTick(composeHaptics)
                val updated = board.toMutableList()
                updated[index] = BoardCell(currentTurnState, false)
                board = updated

                val (win, line) = checkWinCondition(updated)
                if (win != null) {
                    haptics.performSuccess()
                    winner = win
                    winningLine = line
                } else {
                    isPlayerXTurn = !isPlayerXTurn
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitGame) {
                    Text("✕", color = TextSecondary, fontSize = 22.sp)
                }
                Text(
                    text = "POWER-UP TIC-TAC-TOE ⚡",
                    color = currentTurnState.color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Turn Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(currentTurnState.color.copy(alpha = 0.2f))
                    .border(1.5.dp, currentTurnState.color, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (winner != null) "WINNER: ${winner?.symbol} 🎉" else "TURN: ${currentTurnState.symbol} ${currentTurnState.name.replace("_", " ")}",
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
            if (winner == null) {
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
            } else {
                Button(
                    onClick = {
                        board = List(9) { BoardCell() }
                        winner = null
                        winningLine = null
                        isPlayerXTurn = true
                        selectedPowerUp = null
                        playerXPowers = mutableSetOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)
                        playerOPowers = mutableSetOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("RESTART MATCH 🔄", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
