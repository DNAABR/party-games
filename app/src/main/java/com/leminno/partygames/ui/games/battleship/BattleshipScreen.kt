package com.leminno.partygames.ui.games.battleship

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

enum class BattleCellState {
    EMPTY, SHIP, HIT, MISS
}

@Composable
fun BattleshipScreen(
    playerCount: Int = 2,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    val players = remember {
        com.leminno.partygames.data.repository.UserPreferencesRepository.getActiveRoster(2)
    }
    val p1Name = players.getOrElse(0) { "Player 1" }
    val p2Name = players.getOrElse(1) { "Player 2" }

    val gridSize = 6 // 6x6 grid for fast mobile party play
    var player1Board by remember { mutableStateOf(List(gridSize * gridSize) { BattleCellState.EMPTY }) }
    var player2Board by remember { mutableStateOf(List(gridSize * gridSize) { BattleCellState.EMPTY }) }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, P1_PLACEMENT, PASS_PRIVACY, P2_PLACEMENT, BATTLE_P1, P2_BATTLE, GAME_OVER
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
        if (currentShipCount >= 4) return board

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
        title = "BATTLESHIP ⚓",
        titleColor = Color(0xFF00F2FE),
        gameId = "battleship",
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
                                gamePhase = "P1_PLACEMENT"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Shared screen with privacy pass covers", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF9D4EDD), RoundedCornerShape(20.dp))
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
                                Text("Remote Play (2 Devices)", color = Color(0xFF9D4EDD), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Play on your own screens via Room Code & Link", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (gamePhase != "GAME_OVER") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (gamePhase == "PASS_PRIVACY" && !isRemoteMode) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔒 PASS PHONE TO NEXT PLAYER", color = Color(0xFF00F2FE), fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Screen obscured to protect secret fleet positions!", color = TextMuted, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            gamePhase = if (player2Board.count { it == BattleCellState.SHIP } < 4) "P2_PLACEMENT" else "BATTLE_P1"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("READY! UNLOCK SCREEN ▶", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "P1_PLACEMENT" || gamePhase == "P2_PLACEMENT") {
                    val isP1 = if (isRemoteMode) isHost else gamePhase == "P1_PLACEMENT"
                    val activeBoard = if (isP1) player1Board else player2Board
                    val shipCount = activeBoard.count { it == BattleCellState.SHIP }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isP1) "${p1Name.uppercase()}: DEPLOY FLEET" else "${p2Name.uppercase()}: DEPLOY FLEET",
                            color = Color(0xFF00F2FE),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap grid cells to place 4 ships ($shipCount/4 placed)", color = TextMuted, fontSize = 12.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until gridSize) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (col in 0 until gridSize) {
                                    val idx = row * gridSize + col
                                    val cell = activeBoard[idx]

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (cell == BattleCellState.SHIP) Color(0xFF00F2FE) else SurfaceGlassDark)
                                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(8.dp))
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
                                            Text("🚢", fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = shipCount == 4
                    ) {
                        Text(if (isRemoteMode) "CONFIRM FLEET 🚀" else "LOCK FLEET & PASS TO ${if (isP1) p2Name.uppercase() else p1Name.uppercase()} ▶", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "BATTLE_P1" || gamePhase == "P2_BATTLE") {
                    val isP1Turn = gamePhase == "BATTLE_P1"
                    val targetBoard = if (isP1Turn) player2Board else player1Board

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isP1Turn) "${p1Name.uppercase()}: CALL SALVO STRIKE 💥" else "${p2Name.uppercase()}: CALL SALVO STRIKE 💥",
                            color = Color(0xFFFF0055),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(actionLog, color = TextPrimary, fontSize = 13.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until gridSize) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (col in 0 until gridSize) {
                                    val idx = row * gridSize + col
                                    val cell = targetBoard[idx]

                                    val (bg, icon) = when (cell) {
                                        BattleCellState.HIT -> Pair(Color(0xFFFF0055), "💥")
                                        BattleCellState.MISS -> Pair(Color(0xFF8D99AE).copy(alpha = 0.3f), "💧")
                                        else -> Pair(SurfaceGlassDark, "")
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(8.dp))
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
                                                        actionLog = "DIRECT HIT! 💥 Fire again or end turn."
                                                    } else {
                                                        haptics.performWarningThud()
                                                        actionLog = "SPLASH! MISS! 💧 Turn passes."
                                                    }

                                                    val remainingHitsNeeded = updated.count { it == BattleCellState.SHIP }
                                                    var targetPhase = gamePhase
                                                    if (remainingHitsNeeded == 0) {
                                                        val winnerName = if (isP1Turn) p1Name else p2Name
                                                        winnerText = "${winnerName.uppercase()} SUNK THE FLEET! VICTORY! ⚓🏆"
                                                        com.leminno.partygames.data.repository.UserPreferencesRepository.updatePlayerScore(winnerName, 3)
                                                        targetPhase = "GAME_OVER"
                                                        gamePhase = "GAME_OVER"
                                                    }
                                                    syncRemoteState(newP1, newP2, targetPhase)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(icon, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            val nextPhase = if (isP1Turn) "P2_BATTLE" else "BATTLE_P1"
                            gamePhase = nextPhase
                            syncRemoteState(player1Board, player2Board, nextPhase)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(if (isRemoteMode) "PASS TURN TO OPPONENT 🔒" else "END TURN & PASS TO ${if (isP1Turn) p2Name.uppercase() else p1Name.uppercase()} 🔒", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = winnerText ?: "VICTORY! 🏆",
                subtitle = "$p1Name vs $p2Name Naval Battle",
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
                gameName = "Battleship ⚓",
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
