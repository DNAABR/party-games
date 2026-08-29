package com.leminno.partygames.ui.games.write_funny

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

val funnyPrompts = listOf(
    "The worst thing to say right after a job interview handshake",
    "A strange title for a self-help book written by a cat",
    "An unconvincing excuse for being 3 hours late to a wedding",
    "The secret warning label hidden on cheap sunglasses",
    "What aliens really think about human reality TV shows"
)

@Composable
fun WriteFunnyScreen(
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

    var currentPrompt by remember { mutableStateOf(funnyPrompts.random()) }
    var answer1 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }
    var selectedWinner by remember { mutableIntStateOf(0) }

    // Observe Remote Quiplash State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remotePrompt = room.gameState["prompt"] as? String
                    val remoteAns1 = room.gameState["ans1"] as? String
                    val remoteAns2 = room.gameState["ans2"] as? String
                    val remotePhase = room.gameState["phase"] as? String
                    val remoteWinner = (room.gameState["winner"] as? Number)?.toInt()

                    if (!remotePrompt.isNullOrBlank()) currentPrompt = remotePrompt
                    if (!remoteAns1.isNullOrBlank()) answer1 = remoteAns1
                    if (!remoteAns2.isNullOrBlank()) answer2 = remoteAns2
                    if (remoteWinner != null && remoteWinner != 0) selectedWinner = remoteWinner
                    if (remotePhase != null && remotePhase != gamePhase) {
                        gamePhase = remotePhase
                    }
                }
            }
        }
    }

    fun syncRemote(prompt: String, a1: String, a2: String, phase: String, winner: Int = 0) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf(
                        "prompt" to prompt,
                        "ans1" to a1,
                        "ans2" to a2,
                        "phase" to phase,
                        "winner" to winner
                    )
                )
            }
        }
    }

    GameScaffold(
        title = "Write Funny",
        titleColor = TextPrimary,
        gameId = "write_funny",
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
                Text("Choose single phone pass or party room voting", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                            gamePhase = "PLAYER1_INPUT"
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
                            Text("Type response secretly & pass device", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                            Text("Answer on your phone, group votes remotely", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else if (gamePhase != "REVEAL") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Prompt Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(TriviaContainer)
                                .border(1.dp, TriviaBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("PROMPT", color = TriviaText, fontFamily = ModernSansFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentPrompt,
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (gamePhase == "PLAYER1_INPUT" || gamePhase == "PLAYER2_INPUT") {
                    val isPlayer1 = gamePhase == "PLAYER1_INPUT"
                    val activeTitle = if (isPlayer1) "Player 1 Response" else "Player 2 Response"
                    val activeValue = if (isPlayer1) answer1 else answer2

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandPrimaryContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = activeTitle,
                                color = BrandPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = activeValue,
                            onValueChange = { if (isPlayer1) answer1 = it else answer2 = it },
                            placeholder = { Text("Write your funniest punchline...", color = TextSecondary, fontFamily = ModernSansFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight,
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )
                    }

                    PrimaryPartyButton(
                        text = if (isPlayer1) "Lock Answer 1 ▶" else "Submit & Vote ▶",
                        accentColor = BrandPrimary,
                        onClick = {
                            if (activeValue.isNotBlank()) {
                                haptics.performPop()
                                if (isPlayer1) {
                                    gamePhase = "PLAYER2_INPUT"
                                    syncRemote(currentPrompt, answer1, answer2, "PLAYER2_INPUT")
                                } else {
                                    gamePhase = "GROUP_VOTE"
                                    syncRemote(currentPrompt, answer1, answer2, "GROUP_VOTE")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "GROUP_VOTE") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Vote for the funniest answer!", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(ActionContainer)
                                .border(1.dp, ActionBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    haptics.performHeavyBurst()
                                    selectedWinner = 1
                                    gamePhase = "REVEAL"
                                    syncRemote(currentPrompt, answer1, answer2, "REVEAL", 1)
                                }
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Option A: \"$answer1\"", color = ActionText, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(MysteryContainer)
                                .border(1.dp, MysteryBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    haptics.performHeavyBurst()
                                    selectedWinner = 2
                                    gamePhase = "REVEAL"
                                    syncRemote(currentPrompt, answer1, answer2, "REVEAL", 2)
                                }
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Option B: \"$answer2\"", color = MysteryText, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        } else {
            val winText = if (selectedWinner == 1) "Option A: \"$answer1\" wins!" else "Option B: \"$answer2\" wins!"
            VictoryCeremonyOverlay(
                winnerTitle = "Punchline Winner! 🏆",
                subtitle = winText,
                onPlayAgain = {
                    answer1 = ""
                    answer2 = ""
                    currentPrompt = funnyPrompts.random()
                    selectedWinner = 0
                    gamePhase = "MODE_SELECT"
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "write_funny",
                gameName = "Write Funny",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "PLAYER1_INPUT"
                    showRemoteSheet = false
                }
            )
        }
    }
}

