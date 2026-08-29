package com.leminno.partygames.ui.games.two_truths_and_a_lie

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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

data class StatementItem(
    val text: String,
    val isLie: Boolean,
    val originalIndex: Int
)

val presetDecks = listOf(
    Triple("I have jumped out of an airplane", "I have met a celebrity", "I have lived in 5 different countries"),
    Triple("I can play 3 musical instruments", "I have never broken a bone", "I won a national chess tournament"),
    Triple("I have eaten fried grasshoppers", "I can speak 4 languages fluently", "I ran a full marathon"),
    Triple("I have been on national TV", "I can juggle 4 balls", "I owned a pet monkey as a child")
)

@Composable
fun TwoTruthsAndALieScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var truth1 by remember { mutableStateOf("") }
    var truth2 by remember { mutableStateOf("") }
    var lieInput by remember { mutableStateOf("") }

    var shuffledStatements by remember { mutableStateOf<List<StatementItem>>(emptyList()) }
    var selectedVoteIndex by remember { mutableStateOf<Int?>(null) }

    // Observe Remote Game State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remotePhase = room.gameState["phase"] as? String
                    val remoteVote = (room.gameState["selectedVote"] as? Number)?.toInt()

                    if (remoteVote != null) selectedVoteIndex = remoteVote
                    if (remotePhase != null && remotePhase != gamePhase) {
                        gamePhase = remotePhase
                    }
                }
            }
        }
    }

    fun syncRemote(phase: String, voteIdx: Int? = null) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("phase" to phase, "selectedVote" to voteIdx)
                )
            }
        }
    }

    fun loadPreset() {
        val preset = presetDecks.random()
        truth1 = preset.first
        truth2 = preset.second
        lieInput = preset.third
    }

    GameScaffold(
        title = "Two Truths & A Lie",
        titleColor = TextPrimary,
        gameId = "two_truths_and_a_lie",
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
                Text("Choose single phone pass or live multi-device voting", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                            gamePhase = "INPUT"
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
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Type statements & pass device for voting", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                            Text("Type on your phone, group votes remotely", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                if (gamePhase == "INPUT") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MysteryContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Active Player Entry",
                                color = MysteryText,
                                fontFamily = ModernSansFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter 2 genuine truths and 1 believable lie below:",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = truth1,
                            onValueChange = { truth1 = it },
                            placeholder = { Text("Truth #1", color = TextSecondary, fontFamily = ModernSansFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight,
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = truth2,
                            onValueChange = { truth2 = it },
                            placeholder = { Text("Truth #2", color = TextSecondary, fontFamily = ModernSansFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight,
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = lieInput,
                            onValueChange = { lieInput = it },
                            placeholder = { Text("The Lie 🤥", color = TextSecondary, fontFamily = ModernSansFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight,
                                focusedBorderColor = AlertRed,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = {
                            haptics.performTick(composeHaptics)
                            loadPreset()
                        }) {
                            Text("🎲 Quick Load Sample Preset", color = BrandPrimary, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    PrimaryPartyButton(
                        text = "Shuffle & Submit For Voting ▶",
                        accentColor = BrandPrimary,
                        onClick = {
                            if (truth1.isNotBlank() && truth2.isNotBlank() && lieInput.isNotBlank()) {
                                haptics.performPop()
                                shuffledStatements = listOf(
                                    StatementItem(truth1, false, 0),
                                    StatementItem(truth2, false, 1),
                                    StatementItem(lieInput, true, 2)
                                ).shuffled()
                                gamePhase = "VOTING"
                                syncRemote("VOTING")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "VOTING" || gamePhase == "REVEAL") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (gamePhase == "VOTING") "Guess The Lie! 🎭" else "The Lie Was Revealed!",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            shuffledStatements.forEachIndexed { idx, item ->
                                val isSelected = selectedVoteIndex == idx
                                val cardBg = when {
                                    gamePhase == "REVEAL" && item.isLie -> AlertContainer
                                    gamePhase == "REVEAL" && !item.isLie -> SuccessContainer
                                    isSelected -> BrandPrimaryContainer
                                    else -> SurfaceLight
                                }

                                val cardBorder = when {
                                    gamePhase == "REVEAL" && item.isLie -> AlertRed
                                    gamePhase == "REVEAL" && !item.isLie -> SuccessGreen
                                    isSelected -> BrandPrimary
                                    else -> BorderSubtle
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(cardBg)
                                        .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                        .clickable(enabled = gamePhase == "VOTING") {
                                            haptics.performTick(composeHaptics)
                                            selectedVoteIndex = idx
                                            syncRemote("VOTING", idx)
                                        }
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.text,
                                            color = TextPrimary,
                                            fontFamily = ModernSansFont,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (gamePhase == "REVEAL") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (item.isLie) AlertRed else SuccessGreen)
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (item.isLie) "Lie 🤥" else "Truth 🟢",
                                                    color = Color.White,
                                                    fontFamily = ModernSansFont,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (gamePhase == "VOTING") {
                        PrimaryPartyButton(
                            text = "Lock Final Vote & Reveal 🔒",
                            accentColor = BrandPrimary,
                            onClick = {
                                if (selectedVoteIndex != null) {
                                    haptics.performHeavyBurst()
                                    gamePhase = "REVEAL"
                                    syncRemote("REVEAL", selectedVoteIndex)
                                }
                            },
                            enabled = selectedVoteIndex != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        PrimaryPartyButton(
                            text = "Play Next Player Round 🔄",
                            accentColor = BrandPrimary,
                            onClick = {
                                truth1 = ""
                                truth2 = ""
                                lieInput = ""
                                selectedVoteIndex = null
                                shuffledStatements = emptyList()
                                gamePhase = "MODE_SELECT"
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "two_truths_and_a_lie",
                gameName = "Two Truths & A Lie",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "INPUT"
                    showRemoteSheet = false
                }
            )
        }
    }
}

