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
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, PLAYING
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var board by remember { mutableStateOf(List(9) { BoardCell() }) }
    var isPlayerXTurn by remember { mutableStateOf(true) }
    var selectedPowerUp by remember { mutableStateOf<TttPowerUp?>(null) }

    var playerXPowers by remember { mutableStateOf(setOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)) }
    var playerOPowers by remember { mutableStateOf(setOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)) }

    var winner by remember { mutableStateOf<CellState?>(null) }
    var winningLine by remember { mutableStateOf<List<Int>?>(null) }

    val currentTurnState = if (isPlayerXTurn) CellState.PLAYER_X else CellState.PLAYER_O
    val currentPowerInventory = if (isPlayerXTurn) playerXPowers else playerOPowers

    // Observe Remote Board State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteTurn = room.gameState["isXTurn"] as? Boolean
                    if (remoteTurn != null) isPlayerXTurn = remoteTurn
                }
            }
        }
    }

    fun syncRemote(isXTurn: Boolean) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("isXTurn" to isXTurn)
                )
            }
        }
    }

    fun checkWinCondition(currentBoard: List<BoardCell>): Pair<CellState?, List<Int>?> {
        val winningCombinations = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
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
                        syncRemote(isPlayerXTurn)
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
                        syncRemote(isPlayerXTurn)
                    }
                }
                TttPowerUp.EXTRA_TURN -> {}
                else -> {}
            }
        } else {
            if (cell.state == CellState.EMPTY) {
                haptics.performTick(composeHaptics)
                val updated = board.toMutableList()
                updated[index] = BoardCell(currentTurnState, false)
                board = updated

                val (winState, line) = checkWinCondition(updated)
                if (winState != null) {
                    haptics.performHeavyBurst()
                    winner = winState
                    winningLine = line
                    return
                }

                isPlayerXTurn = !isPlayerXTurn
                syncRemote(isPlayerXTurn)
            }
        }
    }

    GameScaffold(
        title = "POWER-UP TIC TAC TOE ❌⭕",
        titleColor = Color(0xFF00F2FE),
        gameId = "ultimate_ttt",
        onExitGame = onExitGame
    ) {
        if (gamePhase == "MODE_SELECT") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SELECT PLAY MODE", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = false
                                gamePhase = "PLAYING"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Use Erase/Shield power-ups on single device", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = true
                                showRemoteSheet = true
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Remote Play (Multi-Device)", color = Color(0xFFFF007F), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Real-time power-up grid moves across 2 phones", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (winner == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Turn Indicator Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceGlassDark)
                        .border(1.dp, currentTurnState.color, RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "TURN: ${currentTurnState.symbol} ${if (isPlayerXTurn) "PLAYER X" else "PLAYER O"}",
                        color = currentTurnState.color,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3x3 Grid Board
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0..2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (col in 0..2) {
                                val idx = row * 3 + col
                                val cell = board[idx]

                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SurfaceGlassDark)
                                        .border(
                                            2.dp,
                                            if (cell.isShielded) Color(0xFFFFD166) else BorderGlassDefault,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { handleCellClick(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell.state.symbol + if (cell.isShielded) "🛡️" else "",
                                        fontSize = 32.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Power Up Inventory Bar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ACTIVE POWER-UPS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TttPowerUp.entries.forEach { power ->
                            val isAvailable = currentPowerInventory.contains(power)
                            val isSelected = selectedPowerUp == power

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) currentTurnState.color else SurfaceGlassDark)
                                    .border(1.dp, if (isAvailable) currentTurnState.color else BorderGlassDefault, RoundedCornerShape(12.dp))
                                    .clickable(enabled = isAvailable) {
                                        selectedPowerUp = if (isSelected) null else power
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${power.icon} ${power.title}",
                                    color = if (isAvailable) Color.White else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "WINNER: ${winner?.symbol} 🎉",
                subtitle = "Power-Up Tic Tac Toe Champions!",
                onPlayAgain = {
                    board = List(9) { BoardCell() }
                    isPlayerXTurn = true
                    selectedPowerUp = null
                    playerXPowers = setOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)
                    playerOPowers = setOf(TttPowerUp.ERASE, TttPowerUp.SHIELD, TttPowerUp.EXTRA_TURN)
                    winner = null
                    winningLine = null
                    gamePhase = "MODE_SELECT"
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "ultimate_ttt",
                gameName = "Power-Up Tic Tac Toe ❌⭕",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "PLAYING"
                    showRemoteSheet = false
                }
            )
        }
    }
}
