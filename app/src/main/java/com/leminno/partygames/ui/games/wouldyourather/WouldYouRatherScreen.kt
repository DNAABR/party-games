package com.leminno.partygames.ui.games.wouldyourather

import androidx.compose.animation.core.animateFloatAsState
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

data class WouldYouRatherScenario(val optionA: String, val optionB: String, val percentA: Int)

@Composable
fun WouldYouRatherScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    val scenarios = remember {
        listOf(
            WouldYouRatherScenario("Be able to fly at 10mph", "Teleport once per day anywhere", 62),
            WouldYouRatherScenario("Always speak your mind out loud", "Never be able to speak again", 81),
            WouldYouRatherScenario("Live in a world without music", "Live in a world without movies", 74),
            WouldYouRatherScenario("Have infinite free coffee for life", "Have infinite free pizza for life", 58),
            WouldYouRatherScenario("Explore deep ocean trenches", "Explore outer space planets", 69)
        ).shuffled()
    }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, PLAYING
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    val currentScenario = scenarios[currentIndex % scenarios.size]

    val animatedPercentA by animateFloatAsState(
        targetValue = if (selectedOption != null) currentScenario.percentA.toFloat() else 0f,
        label = "percentA"
    )

    // Observe Remote State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteIdx = (room.gameState["index"] as? Number)?.toInt()
                    val remoteOpt = room.gameState["selectedOption"] as? String

                    if (remoteIdx != null) currentIndex = remoteIdx
                    if (remoteOpt != null) selectedOption = remoteOpt
                }
            }
        }
    }

    fun syncRemote(idx: Int, opt: String?) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("index" to idx, "selectedOption" to opt)
                )
            }
        }
    }

    GameScaffold(
        title = "Would You Rather ⚖️",
        titleColor = Color(0xFF00F2FE),
        gameId = "would_you_rather",
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
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
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
                                Text("Solo / Same Phone", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Tap option to reveal global choice percentage", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(20.dp))
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
                                Text("Remote Play (Multi-Device)", color = Color(0xFFFF007F), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Synchronized dilemmas & live polling across screens", color = TextMuted, fontSize = 12.sp)
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGlassDark)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Dilemma #${currentIndex + 1}", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option A Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedOption == "A") Color(0x4400F2FE) else SurfaceGlassDark)
                        .border(2.dp, if (selectedOption == "A") Color(0xFF00F2FE) else BorderGlassDefault, RoundedCornerShape(24.dp))
                        .clickable {
                            selectedOption = "A"
                            haptics.performPop()
                            syncRemote(currentIndex, "A")
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("OPTION A 🅰️", color = Color(0xFF00F2FE), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentScenario.optionA,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (selectedOption != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${animatedPercentA.toInt()}% Voted This",
                                color = Color(0xFF00F2FE),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option B Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedOption == "B") Color(0x44FF007F) else SurfaceGlassDark)
                        .border(2.dp, if (selectedOption == "B") Color(0xFFFF007F) else BorderGlassDefault, RoundedCornerShape(24.dp))
                        .clickable {
                            selectedOption = "B"
                            haptics.performPop()
                            syncRemote(currentIndex, "B")
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("OPTION B 🅱️", color = Color(0xFFFF007F), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentScenario.optionB,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (selectedOption != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${100 - animatedPercentA.toInt()}% Voted This",
                                color = Color(0xFFFF007F),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        selectedOption = null
                        currentIndex++
                        syncRemote(currentIndex, null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedOption != null
                ) {
                    Text("NEXT DILEMMA ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "would_you_rather",
                gameName = "Would You Rather ⚖️",
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
                        syncRemote(currentIndex, selectedOption)
                    }
                }
            )
        }
    }
}
