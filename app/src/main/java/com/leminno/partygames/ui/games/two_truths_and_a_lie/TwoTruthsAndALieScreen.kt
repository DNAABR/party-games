package com.leminno.partygames.ui.games.two_truths_and_a_lie

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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
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

    val players = remember(playerCount) {
        com.leminno.partygames.data.repository.UserPreferencesRepository.getActiveRoster(playerCount)
    }

    var activePlayerIndex by remember { mutableIntStateOf(0) }
    val activePlayerName = players.getOrElse(activePlayerIndex % players.size) { "Player 1" }
    var showScoreboard by remember { mutableStateOf(false) }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, INPUT, VOTING, REVEAL
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
        title = "Two Truths & A Lie 🎭",
        titleColor = Color(0xFFFF007F),
        gameId = "two_truths_and_a_lie",
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
                                gamePhase = "INPUT"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFFFF007F), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Type statements & pass device for voting", color = TextMuted, fontSize = 12.sp)
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
                                Text("Type 2 truths & 1 lie on your phone, group votes remotely", color = TextMuted, fontSize = 12.sp)
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
                com.leminno.partygames.ui.components.InGamePlayerHeader(
                    currentPlayerName = activePlayerName,
                    playerIndex = activePlayerIndex % players.size,
                    totalPlayers = players.size,
                    onOpenScoreboard = { showScoreboard = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (gamePhase == "INPUT") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PASS PHONE TO $activePlayerName",
                            color = Color(0xFFFF007F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter 2 genuine truths and 1 believable lie below:",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = truth1,
                            onValueChange = { truth1 = it },
                            label = { Text("Truth #1") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF007F), unfocusedBorderColor = BorderGlassDefault)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = truth2,
                            onValueChange = { truth2 = it },
                            label = { Text("Truth #2") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF007F), unfocusedBorderColor = BorderGlassDefault)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = lieInput,
                            onValueChange = { lieInput = it },
                            label = { Text("The Lie 🤥") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF0055), unfocusedBorderColor = BorderGlassDefault)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = {
                            haptics.performTick(composeHaptics)
                            loadPreset()
                        }) {
                            Text("🎲 Quick Load Sample Preset", color = Color(0xFF00F2FE), fontSize = 13.sp)
                        }
                    }

                    Button(
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("SHUFFLE & SUBMIT FOR VOTING ▶", color = Color.White, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "VOTING" || gamePhase == "REVEAL") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (gamePhase == "VOTING") "GUESS THE LIE! 🎭" else "THE LIE WAS REVEALED!",
                            color = Color(0xFFFF007F),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            shuffledStatements.forEachIndexed { idx, item ->
                                val isSelected = selectedVoteIndex == idx
                                val cardBg = when {
                                    gamePhase == "REVEAL" && item.isLie -> Color(0xFFFF0055).copy(alpha = 0.35f)
                                    gamePhase == "REVEAL" && !item.isLie -> Color(0xFF00E676).copy(alpha = 0.2f)
                                    isSelected -> Color(0xFFFF007F).copy(alpha = 0.3f)
                                    else -> SurfaceGlassDark
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(cardBg)
                                        .border(
                                            1.5.dp,
                                            when {
                                                gamePhase == "REVEAL" && item.isLie -> Color(0xFFFF0055)
                                                gamePhase == "REVEAL" && !item.isLie -> Color(0xFF00E676)
                                                isSelected -> Color(0xFFFF007F)
                                                else -> BorderGlassDefault
                                            },
                                            RoundedCornerShape(16.dp)
                                        )
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
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (gamePhase == "REVEAL") {
                                            Text(
                                                text = if (item.isLie) "LIE! 🤥" else "TRUTH 🟢",
                                                color = if (item.isLie) Color(0xFFFF0055) else Color(0xFF00E676),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (gamePhase == "VOTING") {
                        Button(
                            onClick = {
                                if (selectedVoteIndex != null) {
                                    haptics.performHeavyBurst()
                                    gamePhase = "REVEAL"
                                    syncRemote("REVEAL", selectedVoteIndex)
                                }
                            },
                            enabled = selectedVoteIndex != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("LOCK FINAL VOTE & REVEAL 🔒", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Button(
                            onClick = {
                                activePlayerIndex++
                                truth1 = ""
                                truth2 = ""
                                lieInput = ""
                                selectedVoteIndex = null
                                shuffledStatements = emptyList()
                                gamePhase = "INPUT"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("NEXT PLAYER TURN (${players.getOrElse((activePlayerIndex + 1) % players.size) { "Player" }}) 🔄", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "two_truths_and_a_lie",
                gameName = "Two Truths & A Lie 🎭",
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

        if (showScoreboard) {
            com.leminno.partygames.ui.components.InGameScoreboardModal(
                players = players,
                onDismissRequest = { showScoreboard = false }
            )
        }
    }
}
