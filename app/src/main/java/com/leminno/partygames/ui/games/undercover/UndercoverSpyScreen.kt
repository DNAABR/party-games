package com.leminno.partygames.ui.games.undercover

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, SETUP, DISCUSSION
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
        title = "Undercover Spy 🕵️",
        titleColor = Color(0xFF00E676),
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
                    Text("SELECT PLAY MODE", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = false
                                gamePhase = "SETUP"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFF00E676), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Hold scanner to reveal role on single phone", color = TextMuted, fontSize = 12.sp)
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
                                Text("Roles & locations sent directly to separate phone screens", color = TextMuted, fontSize = 12.sp)
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3300E676))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Player ${currentPlayerTurn + 1} / $playerCount", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                if (gamePhase == "SETUP") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(SurfaceGlassDark)
                            .border(2.dp, if (isRevealingRole) Color(0xFF00E676) else BorderGlassDefault, RoundedCornerShape(28.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isRevealingRole = true
                                        haptics.performPop()
                                        tryAwaitRelease()
                                        isRevealingRole = false
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRevealingRole) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val isSpy = currentPlayerTurn == spyIndex
                                Text(
                                    text = if (isSpy) "YOU ARE THE SPY! 🕵️" else "LOCATION: $secretLocation",
                                    color = if (isSpy) Color(0xFFFF007F) else Color(0xFF00E676),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (isSpy) "Try to blend in and guess the location!" else "Ask subtle questions to spot the imposter!",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👆 HOLD TO REVEAL ROLE", color = Color(0xFF00E676), fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Keep screen secret from other players!", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (currentPlayerTurn < playerCount - 1) {
                                currentPlayerTurn++
                            } else {
                                gamePhase = "DISCUSSION"
                                syncRemote(secretLocation, spyIndex, "DISCUSSION")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = if (currentPlayerTurn < playerCount - 1) "NEXT PLAYER ▶" else "START GROUP DISCUSSION 💬",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    // Discussion Phase
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GROUP DISCUSSION IN PROGRESS 💬", color = Color(0xFF00E676), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Ask each other questions to spot the Undercover Spy!", color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }

                    Button(
                        onClick = { isFinished = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("REVEAL THE SPY 🕵️", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "THE SPY WAS PLAYER ${spyIndex + 1}! 🕵️",
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
                gameName = "Undercover Spy 🕵️",
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
