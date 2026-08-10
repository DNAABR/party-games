package com.leminno.partygames.ui.games.scrabble_league

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
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

data class TileCell(
    val letter: Char? = null,
    val points: Int = 0
)

val tileLettersBag = listOf(
    'A', 'A', 'B', 'C', 'D', 'E', 'E', 'F', 'G', 'H', 'I', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
)

@Composable
fun ScrabbleLeagueScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, PLAYING
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    val boardSize = 5
    var boardGrid by remember { mutableStateOf(List(boardSize * boardSize) { TileCell() }) }
    var player1Score by remember { mutableIntStateOf(0) }
    var player2Score by remember { mutableIntStateOf(0) }
    var isPlayer1Turn by remember { mutableStateOf(true) }

    var player1Rack by remember { mutableStateOf((1..7).map { tileLettersBag.random() }) }
    var player2Rack by remember { mutableStateOf((1..7).map { tileLettersBag.random() }) }
    var selectedRackTileIndex by remember { mutableStateOf<Int?>(null) }

    // Observe Remote Board
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteTurn = room.gameState["isP1Turn"] as? Boolean
                    val p1Sc = (room.gameState["p1Score"] as? Number)?.toInt()
                    val p2Sc = (room.gameState["p2Score"] as? Number)?.toInt()

                    if (remoteTurn != null) isPlayer1Turn = remoteTurn
                    if (p1Sc != null) player1Score = p1Sc
                    if (p2Sc != null) player2Score = p2Sc
                }
            }
        }
    }

    fun syncRemote(isP1Turn: Boolean, p1Sc: Int, p2Sc: Int) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("isP1Turn" to isP1Turn, "p1Score" to p1Sc, "p2Score" to p2Sc)
                )
            }
        }
    }

    GameScaffold(
        title = "LETTER LEAGUE 🔠",
        titleColor = Color(0xFFFFD166),
        gameId = "scrabble_league",
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
                            .border(1.5.dp, Color(0xFFFFD166), RoundedCornerShape(20.dp))
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
                                Text("Pass & Play (Same Phone)", color = Color(0xFFFFD166), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Place word tiles on shared 5x5 board", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
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
                                Text("Remote Play (Multi-Device)", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Private tile racks & live board state across 2 phones", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Scoreboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF00F2FE).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF00F2FE), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("P1 SCORE: $player1Score", color = Color(0xFF00F2FE), fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }

                    Text(
                        text = if (isPlayer1Turn) "TURN: PLAYER 1 🔵" else "TURN: PLAYER 2 🔴",
                        color = if (isPlayer1Turn) Color(0xFF00F2FE) else Color(0xFFFF0055),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF0055).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("P2 SCORE: $player2Score", color = Color(0xFFFF0055), fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }

                // 5x5 Scrabble Board Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until boardSize) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (col in 0 until boardSize) {
                                val idx = row * boardSize + col
                                val cell = boardGrid[idx]

                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (cell.letter != null) Color(0xFFFFD166) else SurfaceGlassDark)
                                        .border(1.dp, BorderGlassDefault, RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (selectedRackTileIndex != null) {
                                                val activeRack = if (isPlayer1Turn) player1Rack else player2Rack
                                                val letter = activeRack[selectedRackTileIndex!!]

                                                val updatedGrid = boardGrid.toMutableList()
                                                updatedGrid[idx] = TileCell(letter, 10)
                                                boardGrid = updatedGrid

                                                if (isPlayer1Turn) {
                                                    player1Score += 10
                                                    player1Rack = player1Rack.filterIndexed { i, _ -> i != selectedRackTileIndex } + tileLettersBag.random()
                                                } else {
                                                    player2Score += 10
                                                    player2Rack = player2Rack.filterIndexed { i, _ -> i != selectedRackTileIndex } + tileLettersBag.random()
                                                }

                                                selectedRackTileIndex = null
                                                isPlayer1Turn = !isPlayer1Turn
                                                haptics.performPop()
                                                syncRemote(isPlayer1Turn, player1Score, player2Score)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell.letter?.toString() ?: "",
                                        color = if (cell.letter != null) Color.Black else TextPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                // Tile Rack Widget
                val currentRack = if (isPlayer1Turn) player1Rack else player2Rack
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YOUR TILE RACK", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        currentRack.forEachIndexed { i, char ->
                            val isSel = selectedRackTileIndex == i
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF00F2FE) else Color(0xFFFFD166))
                                    .clickable { selectedRackTileIndex = if (isSel) null else i },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$char", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "scrabble_league",
                gameName = "Letter League 🔠",
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
