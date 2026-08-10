package com.leminno.partygames.ui.games.fake_it

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

data class FakeItQuestion(
    val prompt: String,
    val realAnswer: String
)

val fakeItQuestions = listOf(
    FakeItQuestion("In 1923, what was uniquely used as currency in Germany during hyperinflation?", "Stamps"),
    FakeItQuestion("What is the official state toy of Mississippi?", "Teddy Bear"),
    FakeItQuestion("What unexpected ingredient was originally in 19th century ketchup?", "Fish Gut Extract"),
    FakeItQuestion("What animal holds the record for the longest sleep cycle (3 years)?", "Desert Snail")
)

data class AnswerCard(
    val text: String,
    val isReal: Boolean,
    val author: String
)

@Composable
fun FakeItScreen(
    playerCount: Int = 3,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, BLUFF_INPUT, VOTING, REVEAL
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var currentQuestion by remember { mutableStateOf(fakeItQuestions.random()) }
    var currentPlayerIdx by remember { mutableIntStateOf(0) }
    var bluffInputText by remember { mutableStateOf("") }
    var playerBluffs by remember { mutableStateOf<List<AnswerCard>>(emptyList()) }
    var selectedVoteCard by remember { mutableStateOf<AnswerCard?>(null) }

    // Observe Remote Game State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remotePrompt = room.gameState["prompt"] as? String
                    val remoteRealAns = room.gameState["realAns"] as? String
                    val remotePhase = room.gameState["phase"] as? String

                    if (!remotePrompt.isNullOrBlank() && !remoteRealAns.isNullOrBlank()) {
                        currentQuestion = FakeItQuestion(remotePrompt, remoteRealAns)
                    }
                    if (remotePhase != null && remotePhase != gamePhase) {
                        gamePhase = remotePhase
                    }
                }
            }
        }
    }

    fun syncRemote(prompt: String, realAns: String, phase: String) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("prompt" to prompt, "realAns" to realAns, "phase" to phase)
                )
            }
        }
    }

    GameScaffold(
        title = "FAKE IT (BLUFF) 🤥",
        titleColor = Color(0xFFFF007F),
        gameId = "fake_it",
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
                            .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = false
                                gamePhase = "BLUFF_INPUT"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFFFF007F), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Type fake bluffs & pass single device", color = TextMuted, fontSize = 12.sp)
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
                                Text("Balderdash style! Type bluffs on your screen, group votes", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (gamePhase != "REVEAL") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Question Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceGlassDark)
                        .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("OBSCURE TRIVIA PROMPT", color = Color(0xFFFF007F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentQuestion.prompt,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (gamePhase == "BLUFF_INPUT") {
                    val activePlayer = "Player ${currentPlayerIdx + 1}"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PASS PHONE TO $activePlayer",
                            color = Color(0xFFFFD166),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = bluffInputText,
                            onValueChange = { bluffInputText = it },
                            label = { Text("Write a believable fake answer...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF007F), unfocusedBorderColor = BorderGlassDefault)
                        )
                    }

                    Button(
                        onClick = {
                            if (bluffInputText.isNotBlank()) {
                                haptics.performPop()
                                val card = AnswerCard(bluffInputText, false, activePlayer)
                                playerBluffs = playerBluffs + card
                                bluffInputText = ""

                                if (currentPlayerIdx < playerCount - 1) {
                                    currentPlayerIdx++
                                } else {
                                    // Add Real Answer into list and shuffle
                                    val realCard = AnswerCard(currentQuestion.realAnswer, true, "TRUTH")
                                    playerBluffs = (playerBluffs + realCard).shuffled()
                                    gamePhase = "VOTING"
                                    syncRemote(currentQuestion.prompt, currentQuestion.realAnswer, "VOTING")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("LOCK BLUFF & PASS DEVICE ▶", color = Color.White, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "VOTING") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SPOT THE REAL TRUTH!", color = Color(0xFF00F2FE), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(playerBluffs) { card ->
                                val isSel = selectedVoteCard == card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSel) Color(0xFF00F2FE).copy(alpha = 0.3f) else SurfaceGlassDark)
                                        .border(1.5.dp, if (isSel) Color(0xFF00F2FE) else BorderGlassDefault, RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptics.performTick()
                                            selectedVoteCard = card
                                        }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = card.text,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedVoteCard != null) {
                                haptics.performHeavyBurst()
                                gamePhase = "REVEAL"
                                syncRemote(currentQuestion.prompt, currentQuestion.realAnswer, "REVEAL")
                            }
                        },
                        enabled = selectedVoteCard != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("LOCK FINAL VOTE 🔒", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            val isSuccess = selectedVoteCard?.isReal == true
            VictoryCeremonyOverlay(
                winnerTitle = if (isSuccess) "SPOTTED THE TRUTH! 🏆" else "FOOLED BY A BLUFF! 🤥",
                subtitle = "Real Answer Was: ${currentQuestion.realAnswer}",
                onPlayAgain = {
                    currentQuestion = fakeItQuestions.random()
                    currentPlayerIdx = 0
                    bluffInputText = ""
                    playerBluffs = emptyList()
                    selectedVoteCard = null
                    gamePhase = "MODE_SELECT"
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "fake_it",
                gameName = "Fake It (Bluff) 🤥",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "BLUFF_INPUT"
                    showRemoteSheet = false
                    if (hostFlag) {
                        syncRemote(currentQuestion.prompt, currentQuestion.realAnswer, "BLUFF_INPUT")
                    }
                }
            )
        }
    }
}
