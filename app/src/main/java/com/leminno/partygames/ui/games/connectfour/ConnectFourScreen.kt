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
import com.leminno.partygames.ui.components.GameScaffold
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
        onExitGame = onExitGame
    ) {
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
                        uiState.winnerPlayer != null -> "PLAYER ${uiState.winnerPlayer} WINS! 🏆"
                        uiState.isDraw -> "IT'S A DRAW! 🤝"
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

            // Bottom Reset Button
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
    }
}
