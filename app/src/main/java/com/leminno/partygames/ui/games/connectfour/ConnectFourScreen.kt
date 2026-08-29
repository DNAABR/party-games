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
import com.leminno.partygames.ui.components.PrimaryPartyButton
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

    var selectedMode by remember { mutableStateOf<String?>(null) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }

    val turnContainerColor by animateColorAsState(
        targetValue = when {
            uiState.winnerPlayer == 1 -> AlertContainer
            uiState.winnerPlayer == 2 -> BoardContainer
            uiState.isDraw -> SurfaceSubtle
            uiState.isRedTurn -> AlertContainer
            else -> BoardContainer
        },
        label = "turnContainerColor"
    )

    val turnTextColor by animateColorAsState(
        targetValue = when {
            uiState.winnerPlayer == 1 -> AlertRed
            uiState.winnerPlayer == 2 -> BoardText
            uiState.isDraw -> TextPrimary
            uiState.isRedTurn -> AlertRed
            else -> BoardText
        },
        label = "turnTextColor"
    )

    GameScaffold(
        title = "Connect Four",
        titleColor = TextPrimary,
        gameId = "connect_four",
        onExitGame = onExitGame
    ) {
        if (selectedMode == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Select Play Mode", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Choose single device pass or remote multiplayer", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            selectedMode = "LOCAL"
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AlertContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Shared screen disc drop on single phone", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                            selectedMode = "REMOTE"
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
                            Text("Real-time turn disc drops synced across screens", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else if (uiState.winnerPlayer == null && !uiState.isDraw) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Turn Status Header
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(turnContainerColor)
                        .border(1.dp, turnTextColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when {
                            uiState.isRedTurn -> "Red's Turn 🔴"
                            else -> "Yellow's Turn 🟡"
                        },
                        color = turnTextColor,
                        fontFamily = ModernSansFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6x7 Grid Board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(7f / 6f)
                        .subtleCardShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(14.dp)
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
                                    val (cellColor, borderCol) = when (cellVal) {
                                        1 -> Pair(AlertRed, AlertRed.copy(alpha = 0.3f))
                                        2 -> Pair(Color(0xFFEAB308), Color(0xFFCA8A04))
                                        else -> Pair(SurfaceSubtle, BorderSubtle)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(cellColor)
                                            .border(1.dp, borderCol, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                PrimaryPartyButton(
                    text = "Restart Match",
                    accentColor = BrandPrimary,
                    onClick = {
                        haptics.performPop()
                        viewModel.resetGame()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            val winnerTitle = when {
                uiState.winnerPlayer != null -> "Player ${uiState.winnerPlayer} Wins!"
                else -> "It's a Draw!"
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
                gameName = "Connect Four",
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

