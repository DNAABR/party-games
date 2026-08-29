package com.leminno.partygames.ui.games.codenames

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
import com.leminno.partygames.ui.components.SecondaryPartyButton
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

enum class CodenameCardType(val title: String, val containerColor: Color, val accentColor: Color) {
    RED("Red Team", AlertContainer, AlertRed),
    BLUE("Blue Team", ActionContainer, ActionPrimary),
    CIVILIAN("Civilian", SurfaceSubtle, TextSecondary),
    ASSASSIN("Assassin ☠️", TextPrimary, SurfaceLight)
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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
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
                if (redScore >= 9) gameOverWinner = "Red Team Wins! 🎉"
                if (!isRedTurn) isRedTurn = true
            }
            CodenameCardType.BLUE -> {
                blueScore++
                if (blueScore >= 8) gameOverWinner = "Blue Team Wins! 🎉"
                if (isRedTurn) isRedTurn = false
            }
            CodenameCardType.CIVILIAN -> {
                isRedTurn = !isRedTurn
            }
            CodenameCardType.ASSASSIN -> {
                haptics.performHeavyBurst()
                gameOverWinner = if (isRedTurn) "Blue Team Wins! (Red hit Assassin ☠️)" else "Red Team Wins! (Blue hit Assassin ☠️)"
            }
        }
    }

    GameScaffold(
        title = "Codenames",
        titleColor = TextPrimary,
        gameId = "codenames",
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
                Text("Choose local device pass or multi-device sync", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                                .background(AlertContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Same Device (Pass & View)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Toggle Spymaster view on single phone", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                            Text("Spymaster views key on their phone, operatives tap theirs", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else if (gameOverWinner == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Score & Turn Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AlertContainer)
                            .border(1.dp, AlertRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Red: $redScore/9", color = AlertRed, fontFamily = ModernSansFont, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isRedTurn) AlertContainer else ActionContainer)
                            .border(1.dp, if (isRedTurn) AlertRed.copy(alpha = 0.4f) else ActionPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isRedTurn) "Red's Turn 🔴" else "Blue's Turn 🔵",
                            color = if (isRedTurn) AlertRed else ActionText,
                            fontFamily = ModernSansFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ActionContainer)
                            .border(1.dp, ActionPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Blue: $blueScore/8", color = ActionText, fontFamily = ModernSansFont, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 5x5 Grid Board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until 5) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (col in 0 until 5) {
                                    val idx = row * 5 + col
                                    val card = gridCards[idx]

                                    val (cardBg, borderCol, textColor) = when {
                                        card.isRevealed -> Triple(
                                            card.type.containerColor,
                                            card.type.accentColor.copy(alpha = 0.6f),
                                            if (card.type == CodenameCardType.ASSASSIN) Color.White else card.type.accentColor
                                        )
                                        isSpymasterView -> Triple(
                                            card.type.containerColor.copy(alpha = 0.7f),
                                            card.type.accentColor.copy(alpha = 0.4f),
                                            if (card.type == CodenameCardType.ASSASSIN) TextPrimary else card.type.accentColor
                                        )
                                        else -> Triple(SurfaceSubtle, BorderSubtle, TextPrimary)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(cardBg)
                                            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                            .clickable { handleCardClick(idx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card.word,
                                            color = textColor,
                                            fontFamily = ModernSansFont,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryPartyButton(
                        text = if (isSpymasterView) "👁️ Hide Key" else "🔑 Key View",
                        onClick = { isSpymasterView = !isSpymasterView },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryPartyButton(
                        text = "End Turn",
                        accentColor = if (isRedTurn) AlertRed else ActionPrimary,
                        onClick = { isRedTurn = !isRedTurn },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = gameOverWinner ?: "Game Over",
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
                gameName = "Codenames",
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

