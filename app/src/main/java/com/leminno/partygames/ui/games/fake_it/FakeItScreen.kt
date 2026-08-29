package com.leminno.partygames.ui.games.fake_it

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
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
        title = "Fake It (Bluff)",
        titleColor = TextPrimary,
        gameId = "fake_it",
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
                Text("Choose single phone pass or multi-device voting", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                            gamePhase = "BLUFF_INPUT"
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(TriviaContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Type fake bluffs & pass single device", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                            Text("Type bluffs on your screen, group votes", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                // Question Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                        .padding(20.dp),
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
                            Text("OBSCURE TRIVIA PROMPT", color = TriviaText, fontFamily = ModernSansFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentQuestion.prompt,
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (gamePhase == "BLUFF_INPUT") {
                    val activePlayer = "Player ${currentPlayerIdx + 1}"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandPrimaryContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Pass Phone to $activePlayer",
                                color = BrandPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = bluffInputText,
                            onValueChange = { bluffInputText = it },
                            placeholder = { Text("Write a believable fake answer...", color = TextSecondary, fontFamily = ModernSansFont) },
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
                        text = "Lock Bluff & Pass Device",
                        accentColor = BrandPrimary,
                        onClick = {
                            if (bluffInputText.isNotBlank()) {
                                haptics.performPop()
                                val card = AnswerCard(bluffInputText, false, activePlayer)
                                playerBluffs = playerBluffs + card
                                bluffInputText = ""

                                if (currentPlayerIdx < playerCount - 1) {
                                    currentPlayerIdx++
                                } else {
                                    val realCard = AnswerCard(currentQuestion.realAnswer, true, "TRUTH")
                                    playerBluffs = (playerBluffs + realCard).shuffled()
                                    gamePhase = "VOTING"
                                    syncRemote(currentQuestion.prompt, currentQuestion.realAnswer, "VOTING")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "VOTING") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Spot The Real Truth!",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                                        .subtleCardShadow(elevation = if (isSel) 3.dp else 1.dp, shape = RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSel) BrandPrimaryContainer else SurfaceLight)
                                        .border(
                                            1.5.dp,
                                            if (isSel) BrandPrimary.copy(alpha = 0.5f) else BorderSubtle,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            haptics.performTick()
                                            selectedVoteCard = card
                                        }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = card.text,
                                        color = TextPrimary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    PrimaryPartyButton(
                        text = "Lock Final Vote 🔒",
                        accentColor = BrandPrimary,
                        onClick = {
                            if (selectedVoteCard != null) {
                                haptics.performHeavyBurst()
                                gamePhase = "REVEAL"
                                syncRemote(currentQuestion.prompt, currentQuestion.realAnswer, "REVEAL")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            val isSuccess = selectedVoteCard?.isReal == true
            VictoryCeremonyOverlay(
                winnerTitle = if (isSuccess) "Spotted The Truth! 🏆" else "Fooled By A Bluff! 🤥",
                subtitle = "Real Answer: ${currentQuestion.realAnswer}",
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
                gameName = "Fake It (Bluff)",
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

