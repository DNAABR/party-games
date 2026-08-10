package com.leminno.partygames.ui.games.connectfour

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*

@Composable
fun ConnectFourScreen(
    viewModel: ConnectFourViewModel = viewModel(),
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    val uiState by viewModel.uiState.collectAsState()

    var selectedMode by remember { mutableStateOf<String?>(null) } // null, LOCAL, REMOTE
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }

    val turnColor by animateColorAsState(
        targetValue = when {
            uiState.winnerPlayer == 1 -> Color(0xFFFF0055)
            uiState.winnerPlayer == 2 -> Color(0xFFFFD166)
            uiState.isDraw -> Color.White
            uiState.isRedTurn -> Color(0xFFFF0055)
            else -> Color(0xFFFFD166)
        },
        label = "turnColor"
    )

    GameScaffold(
        title = "CONNECT FOUR 🔴🟡",
        titleColor = Color(0xFFFF6B6B),
        gameId = "connect_four",
        onExitGame = onExitGame
    ) {
        if (selectedMode == null) {
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
                            .border(1.5.dp, Color(0xFFFF6B6B), RoundedCornerShape(20.dp))
                            .clickable {
                                selectedMode = "LOCAL"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFFFF6B6B), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Shared screen disc drop on single phone", color = TextMuted, fontSize = 12.sp)
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
                                selectedMode = "REMOTE"
                                showRemoteSheet = true
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Remote Play (Multi-Device)", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Real-time turn disc drops synced across screens", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (uiState.winnerPlayer == null && !uiState.isDraw) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Turn Status Header
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceGlassDark)
                        .border(1.dp, turnColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when {
                            uiState.isRedTurn -> "RED TURN 🔴"
                            else -> "YELLOW TURN 🟡"
                        },
                        color = turnColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6x7 Grid Board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(7f / 6f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E293B))
                        .border(3.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        haptics.performTick(composeHaptics)
                                        viewModel.dropDisc(
                                            col = col,
                                            onWin = { haptics.performHeavyBurst() },
                                            onDraw = { haptics.performWarningThud() }
                                        )
                                    },
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                for (row in 0..5) {
                                    val cellVal = uiState.grid[row][col]
                                    val cellColor = when (cellVal) {
                                        1 -> Color(0xFFFF0055) // Red
                                        2 -> Color(0xFFFFD166) // Yellow
                                        else -> Color(0xFF0F172A) // Empty Slot
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(cellColor)
                                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        haptics.performPop()
                        viewModel.resetGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("RESTART MATCH 🔄", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        } else {
            val winnerTitle = when {
                uiState.winnerPlayer != null -> "PLAYER ${uiState.winnerPlayer} WINS!"
                else -> "IT'S A DRAW!"
            }
            VictoryCeremonyOverlay(
                winnerTitle = winnerTitle,
                subtitle = "Connect Four Champions",
                onPlayAgain = { viewModel.resetGame() },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "connect_four",
                gameName = "Connect Four 🔴🟡",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) selectedMode = null
                },
                onRoomJoined = { code, _, _ ->
                    roomCode = code
                    selectedMode = "REMOTE"
                    showRemoteSheet = false
                }
            )
        }
    }
}
