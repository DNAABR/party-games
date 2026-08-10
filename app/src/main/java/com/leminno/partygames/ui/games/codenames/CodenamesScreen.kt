package com.leminno.partygames.ui.games.codenames

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

enum class CodenameCardType(val title: String, val color: Color) {
    RED("Red Team", Color(0xFFFF0055)),
    BLUE("Blue Team", Color(0xFF00F2FE)),
    CIVILIAN("Civilian", Color(0xFF8D99AE)),
    ASSASSIN("Assassin ☠️", Color.Black)
}

data class CodenameCard(
    val word: String,
    val type: CodenameCardType,
    val isRevealed: Boolean = false
)

val codenameWordPool = listOf(
    "APPLE", "AGENT", "BANK", "BEACH", "BERLIN", "BOTTLE", "BRIDGE", "CAMERA",
    "CASTLE", "DRAGON", "EAGLE", "ENGINE", "FLIGHT", "FOREST", "HAWK", "HOTEL",
    "ICE", "KNIGHT", "LASER", "MATCH", "MOON", "NIGHT", "OCEAN", "PIPES", "QUEEN"
)

@Composable
fun CodenamesScreen(
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

    var isSpymasterView by remember { mutableStateOf(false) }
    var redScore by remember { mutableIntStateOf(0) }
    var blueScore by remember { mutableIntStateOf(0) }
    var isRedTurn by remember { mutableStateOf(true) }
    var gameOverWinner by remember { mutableStateOf<String?>(null) }

    fun generateGrid(): List<CodenameCard> {
        val shuffledWords = codenameWordPool.shuffled()
        val types = (List(9) { CodenameCardType.RED } +
                List(8) { CodenameCardType.BLUE } +
                List(7) { CodenameCardType.CIVILIAN } +
                listOf(CodenameCardType.ASSASSIN)).shuffled()

        return shuffledWords.mapIndexed { idx, word ->
            CodenameCard(word, types[idx])
        }
    }

    var gridCards by remember { mutableStateOf(generateGrid()) }

    // Observe Remote Grid State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val revealedIndices = (room.gameState["revealedIndices"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toSet()
                    if (revealedIndices != null) {
                        gridCards = gridCards.mapIndexed { idx, card ->
                            card.copy(isRevealed = revealedIndices.contains(idx))
                        }
                    }
                }
            }
        }
    }

    fun syncRemoteRevealed() {
        if (isRemoteMode && roomCode.isNotBlank()) {
            val revealedIndices = gridCards.mapIndexedNotNull { idx, card -> if (card.isRevealed) idx else null }
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("revealedIndices" to revealedIndices)
                )
            }
        }
    }

    fun handleCardClick(index: Int) {
        if (gameOverWinner != null || gridCards[index].isRevealed) return

        haptics.performPop()
        val card = gridCards[index]
        val updated = gridCards.toMutableList()
        updated[index] = card.copy(isRevealed = true)
        gridCards = updated
        syncRemoteRevealed()

        when (card.type) {
            CodenameCardType.RED -> {
                redScore++
                if (redScore >= 9) gameOverWinner = "RED TEAM WINS! 🎉"
                if (!isRedTurn) isRedTurn = true
            }
            CodenameCardType.BLUE -> {
                blueScore++
                if (blueScore >= 8) gameOverWinner = "BLUE TEAM WINS! 🎉"
                if (isRedTurn) isRedTurn = false
            }
            CodenameCardType.CIVILIAN -> {
                isRedTurn = !isRedTurn
            }
            CodenameCardType.ASSASSIN -> {
                haptics.performHeavyBurst()
                gameOverWinner = if (isRedTurn) "BLUE TEAM WINS! (Red hit Assassin ☠️)" else "RED TEAM WINS! (Blue hit Assassin ☠️)"
            }
        }
    }

    GameScaffold(
        title = "CODENAMES 🕵️‍♂️",
        titleColor = Color(0xFFFF0055),
        gameId = "codenames",
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
                            .border(1.5.dp, Color(0xFFFF0055), RoundedCornerShape(20.dp))
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
                                Text("Same Device (Pass & View)", color = Color(0xFFFF0055), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Toggle Spymaster view on single phone", color = TextMuted, fontSize = 12.sp)
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
                                Text("Spymaster views key on their phone, operatives tap theirs", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (gameOverWinner == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Score & Turn Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RED: $redScore/9", color = Color(0xFFFF0055), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = if (isRedTurn) "RED TURN 🔴" else "BLUE TURN 🔵",
                        color = if (isRedTurn) Color(0xFFFF0055) else Color(0xFF00F2FE),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("BLUE: $blueScore/8", color = Color(0xFF00F2FE), fontSize = 16.sp, fontWeight = FontWeight.Black)
                }

                // 5x5 Grid Board
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until 5) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (col in 0 until 5) {
                                val idx = row * 5 + col
                                val card = gridCards[idx]

                                val cardBg = when {
                                    card.isRevealed -> card.type.color
                                    isSpymasterView -> card.type.color.copy(alpha = 0.35f)
                                    else -> SurfaceGlassDark
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cardBg)
                                        .border(1.dp, if (card.isRevealed) card.type.color else BorderGlassDefault, RoundedCornerShape(8.dp))
                                        .clickable { handleCardClick(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = card.word,
                                        color = if (card.isRevealed) Color.White else TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { isSpymasterView = !isSpymasterView },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isSpymasterView) "👁️ Hide Key" else "🔑 Spymaster Key View", color = TextPrimary)
                    }

                    Button(
                        onClick = { isRedTurn = !isRedTurn },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRedTurn) Color(0xFFFF0055) else Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("END TURN ▶", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = gameOverWinner ?: "GAME OVER",
                subtitle = "Codenames Intelligence Ops!",
                onPlayAgain = {
                    gridCards = generateGrid()
                    redScore = 0
                    blueScore = 0
                    isRedTurn = true
                    gameOverWinner = null
                    gamePhase = "MODE_SELECT"
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "codenames",
                gameName = "Codenames 🕵️‍♂️",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    isSpymasterView = hostFlag
                    gamePhase = "PLAYING"
                    showRemoteSheet = false
                }
            )
        }
    }
}
