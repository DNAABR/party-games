package com.leminno.partygames.ui.games.ultimate_ttt

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    PLAYER_X("❌", BrandPrimary),
    PLAYER_O("⭕", ActionPrimary)
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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
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
        title = "Power-Up Tic Tac Toe",
        titleColor = TextPrimary,
        gameId = "ultimate_ttt",
        onExitGame = onExitGame
    ) {
        if (gamePhase == "MODE_SELECT") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Select Play Mode", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Choose single phone duel or live remote room", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            isRemoteMode = false
                            gamePhase = "PLAYING"
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ActionContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Use Erase/Shield power-ups on single device", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            isRemoteMode = true
                            showRemoteSheet = true
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MysteryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Remote Play (Multi-Device)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Real-time power-up grid moves across 2 phones", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else if (winner == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Turn Indicator Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPlayerXTurn) ActionContainer else MysteryContainer)
                        .border(1.dp, if (isPlayerXTurn) ActionBorder else MysteryBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Turn: ${currentTurnState.symbol} ${if (isPlayerXTurn) "Player X" else "Player O"}",
                        color = if (isPlayerXTurn) ActionText else MysteryText,
                        fontFamily = ModernSansFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3x3 Grid Board
                Box(
                    modifier = Modifier
                        .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0..2) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (col in 0..2) {
                                    val idx = row * 3 + col
                                    val cell = board[idx]

                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(SurfaceSubtle)
                                            .border(
                                                1.dp,
                                                if (cell.isShielded) BoardBorder else BorderSubtle,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { handleCellClick(idx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cell.state.symbol + if (cell.isShielded) "🛡️" else "",
                                            fontSize = 30.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Power Up Inventory Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Power-Ups", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TttPowerUp.entries.forEach { power ->
                                val isAvailable = currentPowerInventory.contains(power)
                                val isSelected = selectedPowerUp == power

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) BrandPrimaryContainer else if (isAvailable) SurfaceSubtle else SurfaceSubtle.copy(alpha = 0.5f))
                                        .border(1.dp, if (isSelected) BrandPrimary else BorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable(enabled = isAvailable) {
                                            selectedPowerUp = if (isSelected) null else power
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                    Text(
                                        text = "${power.icon} ${power.title}",
                                        color = if (isSelected) BrandPrimary else if (isAvailable) TextPrimary else TextMuted,
                                        fontFamily = ModernSansFont,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "Winner: ${winner?.symbol} 🎉",
                subtitle = "Power-Up Tic Tac Toe Completed!",
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
                gameName = "Power-Up Tic Tac Toe",
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

