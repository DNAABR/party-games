package com.leminno.partygames.ui.games.undercover

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
fun UndercoverSpyScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    val locations = remember {
        listOf("Submarine", "Airplane", "Space Station", "Movie Studio", "Hospital", "Casino", "Supermarket", "Circus", "Polar Station")
    }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var secretLocation by remember { mutableStateOf(locations.random()) }
    var spyIndex by remember { mutableIntStateOf((0 until playerCount).random()) }

    var currentPlayerTurn by remember { mutableIntStateOf(0) }
    var isRevealingRole by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    // Observe Remote Undercover State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteLoc = room.gameState["location"] as? String
                    val remoteSpy = (room.gameState["spyIdx"] as? Number)?.toInt()
                    val remotePhase = room.gameState["phase"] as? String

                    if (!remoteLoc.isNullOrBlank()) secretLocation = remoteLoc
                    if (remoteSpy != null) spyIndex = remoteSpy
                    if (remotePhase != null && remotePhase != gamePhase) {
                        gamePhase = remotePhase
                    }
                }
            }
        }
    }

    fun syncRemote(loc: String, spyIdx: Int, phase: String) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("location" to loc, "spyIdx" to spyIdx, "phase" to phase)
                )
            }
        }
    }

    GameScaffold(
        title = "Undercover Spy",
        titleColor = TextPrimary,
        gameId = "undercover_spy",
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose how your group will pass roles",
                    color = TextSecondary,
                    fontFamily = ModernSansFont,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pass & Play Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            isRemoteMode = false
                            gamePhase = "SETUP"
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
                            Text(text = "🕵️", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Pass & Play",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Hold scanner to reveal secret role on single phone",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Remote Play Card
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
                            Text(text = "🌐", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Remote Multiplayer",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Multi-device room code sync",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else if (!isFinished) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(BrandPrimaryContainer)
                        .border(1.dp, BrandPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Player ${currentPlayerTurn + 1} of $playerCount",
                        color = BrandPrimary,
                        fontFamily = ModernSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (gamePhase == "SETUP") {
                    val isSpy = currentPlayerTurn == spyIndex
                    val targetBgColor by animateColorAsState(
                        targetValue = if (isRevealingRole) {
                            if (isSpy) AlertContainer else MysteryContainer
                        } else SurfaceLight,
                        animationSpec = tween(150),
                        label = "targetBgColor"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .subtleCardShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(targetBgColor)
                            .border(
                                1.dp,
                                if (isRevealingRole) {
                                    if (isSpy) AlertRed.copy(alpha = 0.4f) else MysteryPrimary.copy(alpha = 0.4f)
                                } else BorderSubtle,
                                RoundedCornerShape(24.dp)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isRevealingRole = true
                                        haptics.performPop()
                                        tryAwaitRelease()
                                        isRevealingRole = false
                                    }
                                )
                            }
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRevealingRole) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(if (isSpy) AlertRed.copy(alpha = 0.15f) else MysteryPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = if (isSpy) "🕵️" else "📍", fontSize = 28.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (isSpy) "You Are The Spy!" else "Location: $secretLocation",
                                    color = if (isSpy) AlertRed else MysteryText,
                                    fontFamily = ModernSansFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isSpy) "Blend in and deduce the secret location!" else "Ask subtle questions to expose the spy!",
                                    color = TextSecondary,
                                    fontFamily = ModernSansFont,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MysteryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "👆", fontSize = 32.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Hold to Reveal Role",
                                    color = TextPrimary,
                                    fontFamily = ModernSansFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Keep screen hidden from other players!",
                                    color = TextSecondary,
                                    fontFamily = ModernSansFont,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    PrimaryPartyButton(
                        text = if (currentPlayerTurn < playerCount - 1) "Next Player" else "Start Discussion",
                        icon = PixelIcons.Zap,
                        accentColor = MysteryPrimary,
                        onClick = {
                            if (currentPlayerTurn < playerCount - 1) {
                                currentPlayerTurn++
                            } else {
                                gamePhase = "DISCUSSION"
                                syncRemote(secretLocation, spyIndex, "DISCUSSION")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Discussion Phase Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .subtleCardShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MysteryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "💬", fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Group Discussion",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Question each other to spot the Undercover Spy!",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    PrimaryPartyButton(
                        text = "Reveal The Spy",
                        icon = PixelIcons.Eye,
                        accentColor = AlertRed,
                        onClick = { isFinished = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "The Spy was Player ${spyIndex + 1}!",
                subtitle = "Secret Location: $secretLocation",
                onPlayAgain = {
                    secretLocation = locations.random()
                    spyIndex = (0 until playerCount).random()
                    currentPlayerTurn = 0
                    isFinished = false
                    gamePhase = "MODE_SELECT"
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "undercover_spy",
                gameName = "Undercover Spy",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "SETUP"
                    showRemoteSheet = false
                    if (hostFlag) {
                        syncRemote(secretLocation, spyIndex, "SETUP")
                    }
                }
            )
        }
    }
}

