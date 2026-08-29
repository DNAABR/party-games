package com.leminno.partygames.ui.games.wouldyourather

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
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
        title = "Would You Rather",
        titleColor = TextPrimary,
        gameId = "would_you_rather",
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
                Text(
                    text = "Select Play Mode",
                    color = TextPrimary,
                    fontFamily = ModernSansFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Choose solo dilemmas or multi-screen live voting", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                                .background(ActionContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚖️", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Solo / Same Phone",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Tap option to reveal global choice percentage",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp
                            )
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
                            Text(
                                text = "Remote Play (Multi-Device)",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Synchronized dilemmas & live polling across screens",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp
                            )
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandPrimaryContainer)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Dilemma #${currentIndex + 1}",
                        color = BrandPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option A Card
                val isASelected = selectedOption == "A"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .subtleCardShadow(elevation = if (isASelected) 3.dp else 1.dp, shape = RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isASelected) ActionContainer else SurfaceLight)
                        .border(1.dp, if (isASelected) ActionBorder else BorderSubtle, RoundedCornerShape(22.dp))
                        .clickable {
                            selectedOption = "A"
                            haptics.performPop()
                            syncRemote(currentIndex, "A")
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OPTION A",
                            color = ActionText,
                            fontFamily = ModernSansFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentScenario.optionA,
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (selectedOption != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ActionBorder)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${animatedPercentA.toInt()}% Voted This",
                                    color = ActionText,
                                    fontFamily = ModernSansFont,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option B Card
                val isBSelected = selectedOption == "B"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .subtleCardShadow(elevation = if (isBSelected) 3.dp else 1.dp, shape = RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isBSelected) MysteryContainer else SurfaceLight)
                        .border(1.dp, if (isBSelected) MysteryBorder else BorderSubtle, RoundedCornerShape(22.dp))
                        .clickable {
                            selectedOption = "B"
                            haptics.performPop()
                            syncRemote(currentIndex, "B")
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OPTION B",
                            color = MysteryText,
                            fontFamily = ModernSansFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentScenario.optionB,
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (selectedOption != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MysteryBorder)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${100 - animatedPercentA.toInt()}% Voted This",
                                    color = MysteryText,
                                    fontFamily = ModernSansFont,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                PrimaryPartyButton(
                    text = "Next Dilemma ▶",
                    icon = Icons.Rounded.ArrowForward,
                    accentColor = BrandPrimary,
                    enabled = selectedOption != null,
                    onClick = {
                        selectedOption = null
                        currentIndex++
                        syncRemote(currentIndex, null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "would_you_rather",
                gameName = "Would You Rather",
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

