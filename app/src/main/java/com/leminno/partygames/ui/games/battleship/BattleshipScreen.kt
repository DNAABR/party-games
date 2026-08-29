package com.leminno.partygames.ui.games.battleship

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

enum class BattleCellState {
    EMPTY, SHIP, HIT, MISS
}

@Composable
fun BattleshipScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    val gridSize = 6 // 6x6 grid for fast mobile party play
    var player1Board by remember { mutableStateOf(List(gridSize * gridSize) { BattleCellState.EMPTY }) }
    var player2Board by remember { mutableStateOf(List(gridSize * gridSize) { BattleCellState.EMPTY }) }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }

    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }
    var localPlayerId by remember { mutableStateOf("") }

    var winnerText by remember { mutableStateOf<String?>(null) }
    var actionLog by remember { mutableStateOf("Place 4 ships on your grid!") }

    // Observe Remote Room State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val p1List = (room.gameState["p1Board"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
                    val p2List = (room.gameState["p2Board"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }

                    if (p1List != null && p1List.size == 36) {
                        player1Board = p1List.map { BattleCellState.entries[it] }
                    }
                    if (p2List != null && p2List.size == 36) {
                        player2Board = p2List.map { BattleCellState.entries[it] }
                    }

                    val remotePhase = room.gameState["phase"] as? String
                    if (remotePhase != null && remotePhase != gamePhase) {
                        gamePhase = remotePhase
                    }
                }
            }
        }
    }

    fun syncRemoteState(newP1: List<BattleCellState>, newP2: List<BattleCellState>, newPhase: String) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf(
                        "p1Board" to newP1.map { it.ordinal },
                        "p2Board" to newP2.map { it.ordinal },
                        "phase" to newPhase
                    )
                )
            }
        }
    }

    fun placeShip(board: List<BattleCellState>, index: Int): List<BattleCellState> {
        val currentShipCount = board.count { it == BattleCellState.SHIP }
        if (currentShipCount >= 4 && board[index] != BattleCellState.SHIP) return board

        val mutable = board.toMutableList()
        mutable[index] = if (mutable[index] == BattleCellState.SHIP) BattleCellState.EMPTY else BattleCellState.SHIP
        return mutable
    }

    fun fireSalvo(targetBoard: List<BattleCellState>, index: Int): Pair<List<BattleCellState>, Boolean> {
        val mutable = targetBoard.toMutableList()
        val cell = mutable[index]
        if (cell == BattleCellState.HIT || cell == BattleCellState.MISS) return Pair(targetBoard, false)

        val isHit = cell == BattleCellState.SHIP
        mutable[index] = if (isHit) BattleCellState.HIT else BattleCellState.MISS
        return Pair(mutable, isHit)
    }

    GameScaffold(
        title = "Battleship",
        titleColor = TextPrimary,
        gameId = "battleship",
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
                Text("Choose local device pass or remote room", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                            gamePhase = "P1_PLACEMENT"
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
                            Text("Shared screen with privacy pass covers", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                                .background(BrandPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Remote Play (2 Devices)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Play on your own screens via room code", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else if (gamePhase != "GAME_OVER") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (gamePhase == "PASS_PRIVACY" && !isRemoteMode) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BrandPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔒", fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text("Pass Phone to Next Player", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Screen hidden to protect fleet positions", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                    }

                    PrimaryPartyButton(
                        text = "Ready! Unlock Screen",
                        accentColor = BrandPrimary,
                        onClick = {
                            haptics.performPop()
                            gamePhase = if (player2Board.count { it == BattleCellState.SHIP } < 4) "P2_PLACEMENT" else "BATTLE_P1"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "P1_PLACEMENT" || gamePhase == "P2_PLACEMENT") {
                    val isP1 = if (isRemoteMode) isHost else gamePhase == "P1_PLACEMENT"
                    val activeBoard = if (isP1) player1Board else player2Board
                    val shipCount = activeBoard.count { it == BattleCellState.SHIP }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isP1) "Player 1: Deploy Fleet" else "Player 2: Deploy Fleet",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap cells to place 4 ships ($shipCount/4 placed)", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (row in 0 until gridSize) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (col in 0 until gridSize) {
                                        val idx = row * gridSize + col
                                        val cell = activeBoard[idx]

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (cell == BattleCellState.SHIP) ActionContainer else SurfaceSubtle)
                                                .border(1.dp, if (cell == BattleCellState.SHIP) ActionBorder else BorderSubtle, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    haptics.performTick()
                                                    val updatedBoard = placeShip(activeBoard, idx)
                                                    if (isP1) {
                                                        player1Board = updatedBoard
                                                        syncRemoteState(updatedBoard, player2Board, gamePhase)
                                                    } else {
                                                        player2Board = updatedBoard
                                                        syncRemoteState(player1Board, updatedBoard, gamePhase)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (cell == BattleCellState.SHIP) {
                                                Text("🚢", fontSize = 18.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PrimaryPartyButton(
                        text = if (isRemoteMode) "Confirm Fleet" else "Lock Fleet & Pass Device",
                        accentColor = BrandPrimary,
                        onClick = {
                            haptics.performPop()
                            if (isRemoteMode) {
                                val nextPhase = if (isHost) "P2_PLACEMENT" else "BATTLE_P1"
                                gamePhase = nextPhase
                                syncRemoteState(player1Board, player2Board, nextPhase)
                            } else {
                                gamePhase = "PASS_PRIVACY"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "BATTLE_P1" || gamePhase == "P2_BATTLE") {
                    val isP1Turn = gamePhase == "BATTLE_P1"
                    val targetBoard = if (isP1Turn) player2Board else player1Board

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isP1Turn) "Player 1's Turn" else "Player 2's Turn",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSubtle)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(actionLog, color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (row in 0 until gridSize) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (col in 0 until gridSize) {
                                        val idx = row * gridSize + col
                                        val cell = targetBoard[idx]

                                        val (bg, borderCol, icon) = when (cell) {
                                            BattleCellState.HIT -> Triple(AlertContainer, AlertRed.copy(alpha = 0.3f), "💥")
                                            BattleCellState.MISS -> Triple(SurfaceSubtle, BorderSubtle, "💧")
                                            else -> Triple(SurfaceLight, BorderSubtle, "")
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(bg)
                                                .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    if (cell == BattleCellState.EMPTY || cell == BattleCellState.SHIP) {
                                                        val (updated, isHit) = fireSalvo(targetBoard, idx)
                                                        var newP1 = player1Board
                                                        var newP2 = player2Board

                                                        if (isP1Turn) {
                                                            player2Board = updated
                                                            newP2 = updated
                                                        } else {
                                                            player1Board = updated
                                                            newP1 = updated
                                                        }

                                                        if (isHit) {
                                                            haptics.performHeavyBurst()
                                                            actionLog = "Direct Hit! 💥 Fire again or end turn."
                                                        } else {
                                                            haptics.performWarningThud()
                                                            actionLog = "Splash! Miss! 💧 Turn passes."
                                                        }

                                                        val remainingHitsNeeded = updated.count { it == BattleCellState.SHIP }
                                                        var targetPhase = gamePhase
                                                        if (remainingHitsNeeded == 0) {
                                                            winnerText = if (isP1Turn) "Player 1 Sunk The Fleet!" else "Player 2 Sunk The Fleet!"
                                                            targetPhase = "GAME_OVER"
                                                            gamePhase = "GAME_OVER"
                                                        }
                                                        syncRemoteState(newP1, newP2, targetPhase)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(icon, fontSize = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PrimaryPartyButton(
                        text = if (isRemoteMode) "Pass Turn to Opponent" else "End Turn & Pass Phone",
                        accentColor = BrandPrimary,
                        onClick = {
                            haptics.performPop()
                            val nextPhase = if (isP1Turn) "P2_BATTLE" else "BATTLE_P1"
                            gamePhase = nextPhase
                            syncRemoteState(player1Board, player2Board, nextPhase)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = winnerText ?: "Victory!",
                subtitle = "Supreme Naval Commander!",
                onPlayAgain = {
                    player1Board = List(gridSize * gridSize) { BattleCellState.EMPTY }
                    player2Board = List(gridSize * gridSize) { BattleCellState.EMPTY }
                    gamePhase = "MODE_SELECT"
                    winnerText = null
                    actionLog = "Place 4 ships on your grid!"
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "battleship",
                gameName = "Battleship",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, pid ->
                    roomCode = code
                    isHost = hostFlag
                    localPlayerId = pid
                    isRemoteMode = true
                    gamePhase = if (hostFlag) "P1_PLACEMENT" else "P2_PLACEMENT"
                    showRemoteSheet = false
                }
            )
        }
    }
}

