package com.leminno.partygames.ui.games.scrabble_league

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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
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
        title = "Letter League",
        titleColor = TextPrimary,
        gameId = "scrabble_league",
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
                Text("Choose single phone pass or live remote board", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                                .background(BoardContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Place word tiles on shared 5x5 board", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                                .background(ActionContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Remote Play (Multi-Device)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Private tile racks & live board across 2 phones", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
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
                            .background(ActionContainer)
                            .border(1.dp, ActionBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("P1: $player1Score", color = ActionText, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPlayer1Turn) ActionContainer else AlertContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isPlayer1Turn) "Player 1's Turn 🔵" else "Player 2's Turn 🔴",
                            color = if (isPlayer1Turn) ActionText else AlertRed,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AlertContainer)
                            .border(1.dp, AlertRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("P2: $player2Score", color = AlertRed, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // 5x5 Scrabble Board Grid
                Box(
                    modifier = Modifier
                        .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
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
                                            .background(if (cell.letter != null) BoardContainer else SurfaceSubtle)
                                            .border(1.dp, if (cell.letter != null) BoardBorder else BorderSubtle, RoundedCornerShape(10.dp))
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
                                            color = BoardText,
                                            fontFamily = ModernSansFont,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tile Rack Widget
                val currentRack = if (isPlayer1Turn) player1Rack else player2Rack
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Tile Rack", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentRack.forEachIndexed { i, char ->
                                val isSel = selectedRackTileIndex == i
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) BrandPrimaryContainer else BoardContainer)
                                        .border(1.5.dp, if (isSel) BrandPrimary else BoardBorder, RoundedCornerShape(10.dp))
                                        .clickable { selectedRackTileIndex = if (isSel) null else i },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$char",
                                        color = if (isSel) BrandPrimary else BoardText,
                                        fontFamily = ModernSansFont,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "scrabble_league",
                gameName = "Letter League",
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

