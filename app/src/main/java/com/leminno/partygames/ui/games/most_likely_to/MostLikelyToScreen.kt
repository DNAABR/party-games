package com.leminno.partygames.ui.games.most_likely_to

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val samplePrompts = listOf(
    "Who is most likely to survive a zombie apocalypse?",
    "Who is most likely to become a secret billionaire?",
    "Who is most likely to lock themselves out of their own home?",
    "Who is most likely to win a reality TV show?",
    "Who is most likely to cry during a funny cartoon movie?",
    "Who is most likely to spend all their money on food?",
    "Who is most likely to forget their own birthday?",
    "Who is most likely to get arrested for something silly?",
    "Who is most likely to start a viral TikTok trend?",
    "Who is most likely to accidentally text the wrong person?"
)

@Composable
fun MostLikelyToScreen(
    playerCount: Int = 4,
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

    var currentPrompt by remember { mutableStateOf(samplePrompts.random()) }
    var countdownValue by remember { mutableStateOf<Int?>(null) }
    var votingPhase by remember { mutableStateOf(false) }
    var revealResults by remember { mutableStateOf(false) }

    val playerNames = remember(playerCount) {
        List(playerCount) { idx -> "Player ${idx + 1}" }
    }

    var votesMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Observe Remote State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remotePrompt = room.gameState["prompt"] as? String
                    if (!remotePrompt.isNullOrBlank()) currentPrompt = remotePrompt
                }
            }
        }
    }

    // Countdown animation loop
    LaunchedEffect(countdownValue) {
        if (countdownValue != null && countdownValue!! > 0) {
            haptics.performTick(composeHaptics)
            delay(1000)
            countdownValue = countdownValue!! - 1
            if (countdownValue == 0) {
                haptics.performPop()
                countdownValue = null
                votingPhase = true
            }
        }
    }

    GameScaffold(
        title = "Most Likely To 🗳️",
        titleColor = Color(0xFFE0AFA0),
        gameId = "most_likely_to",
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
                            .border(1.5.dp, Color(0xFFE0AFA0), RoundedCornerShape(20.dp))
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
                                Text("Pass & Play (Same Phone)", color = Color(0xFFE0AFA0), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("3-second countdown then point fingers together!", color = TextMuted, fontSize = 12.sp)
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
                                Text("Cast votes on separate screens & reveal winner stats", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (!revealResults) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Prompt Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceGlassDark)
                        .border(2.dp, Color(0xFFE0AFA0), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUESTION CARD", color = Color(0xFFE0AFA0), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentPrompt,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (countdownValue != null) {
                    Text(
                        text = "$countdownValue",
                        color = Color(0xFFE0AFA0),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black
                    )
                } else if (!votingPhase) {
                    Button(
                        onClick = { countdownValue = 3 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0AFA0)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("START 3-SEC COUNTDOWN ⏳", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else {
                    // Voting list
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TAP PLAYER TO VOTE!", color = Color(0xFFE0AFA0), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(playerNames) { name ->
                                val count = votesMap[name] ?: 0
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SurfaceGlassDark)
                                        .border(1.dp, BorderGlassDefault, RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptics.performPop()
                                            val mutable = votesMap.toMutableMap()
                                            mutable[name] = count + 1
                                            votesMap = mutable
                                        }
                                        .padding(14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("$count Votes", color = Color(0xFFE0AFA0), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            haptics.performHeavyBurst()
                            revealResults = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0AFA0)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("REVEAL WINNER 🏆", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            val topVoted = votesMap.maxByOrNull { it.value }?.key ?: "Everyone"
            VictoryCeremonyOverlay(
                winnerTitle = "MOST LIKELY: $topVoted! 👑",
                subtitle = currentPrompt,
                onPlayAgain = {
                    currentPrompt = samplePrompts.random()
                    votingPhase = false
                    revealResults = false
                    votesMap = emptyMap()
                    gamePhase = "MODE_SELECT"
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            com.leminno.partygames.ui.components.RemoteRoomSetupSheet(
                gameId = "most_likely_to",
                gameName = "Most Likely To 🗳️",
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
                    if (hostFlag) {
                        scope.launch {
                            RemoteRoomRepository.updateGameState(
                                roomCode,
                                mapOf("prompt" to currentPrompt)
                            )
                        }
                    }
                }
            )
        }
    }
}
