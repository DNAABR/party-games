package com.leminno.partygames.ui.games.undercover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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

    val players = remember(playerCount) {
        com.leminno.partygames.data.repository.UserPreferencesRepository.getActiveRoster(playerCount)
    }

    var showScoreboard by remember { mutableStateOf(false) }

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
    val currentName = players.getOrElse(currentPlayerTurn) { "Player ${currentPlayerTurn + 1}" }
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
        title = "UNDERCOVER SPY",
        titleColor = PixelEmeraldGreen,
        gameId = "undercover_spy",
        onExitGame = onExitGame
    ) {
        if (gamePhase == "MODE_SELECT") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SELECT PLAY MODE",
                        color = PixelEmeraldGreen,
                        fontFamily = PressStart2PFont,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Pass & Play Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(
                                brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                            )
                            .clickable {
                                isRemoteMode = false
                                gamePhase = "SETUP"
                            }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.5.dp, PixelOutlineBlack)
                                    .background(PixelEmeraldGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PixelIcons.Eye,
                                    contentDescription = null,
                                    tint = PixelOutlineBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "PASS & PLAY",
                                    color = PixelEmeraldGreen,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Hold scanner to reveal role on single phone",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Remote Play Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(
                                brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                            )
                            .clickable {
                                isRemoteMode = true
                                showRemoteSheet = true
                            }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.5.dp, PixelOutlineBlack)
                                    .background(PixelCrtCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PixelIcons.Users,
                                    contentDescription = null,
                                    tint = PixelOutlineBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "REMOTE PLAY",
                                    color = PixelCrtCyan,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Multi-device room code sync",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        } else if (!isFinished) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                        .background(PixelVioletElevated)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚡ PASS TO ${currentName.uppercase()} (${currentPlayerTurn + 1} OF $playerCount)",
                        color = PixelEmeraldGreen,
                        fontFamily = PressStart2PFont,
                        fontSize = 9.sp
                    )
                }

                if (gamePhase == "SETUP") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 14.dp)
                            .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(
                                if (isRevealingRole) PixelCrtDarkCanvas else PixelVioletElevated
                            )
                            .crtScanlines()
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
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRevealingRole) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val isSpy = currentPlayerTurn == spyIndex
                                Icon(
                                    imageVector = if (isSpy) PixelIcons.Eye else PixelIcons.Spy,
                                    contentDescription = null,
                                    tint = if (isSpy) PixelAlertRed else PixelEmeraldGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (isSpy) "${currentName.uppercase()}: YOU ARE THE SPY!" else "LOCATION: ${secretLocation.uppercase()}",
                                    color = if (isSpy) PixelAlertRed else PixelEmeraldGreen,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isSpy) "Blend in and deduce the location!" else "Ask subtle questions to spot the spy!",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = PixelIcons.Eye,
                                    contentDescription = null,
                                    tint = PixelEmeraldGreen,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "HOLD TO SCAN FOR ${currentName.uppercase()}",
                                    color = PixelEmeraldGreen,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Keep screen hidden from other players!",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    PrimaryPartyButton(
                        text = if (currentPlayerTurn < playerCount - 1) "PASS TO ${players.getOrElse(currentPlayerTurn + 1) { "NEXT" }.uppercase()} ▶" else "START DISCUSSION 🎙️",
                        icon = PixelIcons.Zap,
                        accentColor = PixelEmeraldGreen,
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
                    // Discussion Phase CRT Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 14.dp)
                            .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(PixelCrtDarkCanvas)
                            .crtScanlines()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = PixelIcons.Users,
                                contentDescription = null,
                                tint = PixelEmeraldGreen,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "GROUP DISCUSSION",
                                color = PixelEmeraldGreen,
                                fontFamily = PressStart2PFont,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Interrogate suspects to find the Undercover Spy!",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    PrimaryPartyButton(
                        text = "REVEAL THE SPY",
                        icon = PixelIcons.Eye,
                        accentColor = PixelMagentaHot,
                        onClick = { isFinished = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            val spyName = players.getOrElse(spyIndex) { "Player ${spyIndex + 1}" }
            VictoryCeremonyOverlay(
                winnerTitle = "THE SPY WAS ${spyName.uppercase()}! 🕵️",
                subtitle = "Secret Location Was: $secretLocation",
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

        if (showScoreboard) {
            com.leminno.partygames.ui.components.InGameScoreboardModal(
                players = players,
                onDismissRequest = { showScoreboard = false }
            )
        }
    }
}
