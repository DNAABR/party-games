package com.leminno.partygames.ui.games.truthordare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.SecondaryPartyButton
import com.leminno.partygames.ui.theme.*

@Composable
fun TruthOrDareScreen(
    playerCount: Int = 4,
    timerSec: Int = 60,
    viewModel: TruthOrDareViewModel = viewModel(),
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(playerCount, timerSec) {
        viewModel.initGame(playerCount = playerCount, timerSec = timerSec)
    }

    // Haptic cues on critical timer states
    LaunchedEffect(uiState.isTimesUp) {
        if (uiState.isTimesUp) {
            haptics.performHeavyBurst()
        }
    }

    LaunchedEffect(uiState.timeRemaining) {
        if (uiState.isTimerRunning && !uiState.isPaused && uiState.timeRemaining in 1..5) {
            haptics.performWarningThud()
        }
    }

    GameScaffold(
        title = "TRUTH OR DARE",
        titleColor = PixelCrtCyan,
        gameId = "truth_or_dare",
        onExitGame = onExitGame
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Deck Selector Tabs (8-Bit Banded Buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Clean", "Party", "Extreme").forEach { deck ->
                        val isSelected = uiState.selectedDeck == deck
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                                .background(if (isSelected) PixelCrtCyan else PixelVioletElevated)
                                .clickable {
                                    haptics.performTick(composeHaptics)
                                    viewModel.selectDeck(deck)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = deck.uppercase(),
                                color = if (isSelected) PixelOutlineBlack else TextMuted,
                                fontFamily = PressStart2PFont,
                                fontSize = 8.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Player Turn Indicator Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                        .background(PixelVioletDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PixelIcons.Users,
                            contentDescription = null,
                            tint = PixelCrtCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PLAYER ${uiState.currentPlayerTurn}'S FATE",
                            color = PixelCrtCyan,
                            fontFamily = PressStart2PFont,
                            fontSize = 9.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .border(1.dp, PixelOutlineBlack)
                            .background(PixelVioletElevated)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${uiState.playerCount} PLAYERS",
                            color = TextMuted,
                            fontFamily = PressStart2PFont,
                            fontSize = 7.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Center CRT Screen Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                        .background(
                            brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                        )
                        .crtScanlines()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.activePrompt == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .border(1.5.dp, PixelOutlineBlack)
                                    .background(PixelMagentaHot)
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = PixelIcons.Zap,
                                    contentDescription = null,
                                    tint = PixelOutlineBlack,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "CHOOSE YOUR FATE",
                                color = PixelCrtCyan,
                                fontFamily = PressStart2PFont,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SELECT TRUTH OR DARE BELOW",
                                color = TextMuted,
                                fontFamily = PressStart2PFont,
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .border(1.dp, PixelOutlineBlack)
                                    .background(PixelVioletDark)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "⏱️ ${uiState.timerDurationSec}s TIMER",
                                    color = PixelAmberGold,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Prompt Header: Type Badge & Live Countdown Timer Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val promptColor = if (uiState.activePromptType == "TRUTH") PixelCrtCyan else PixelMagentaHot
                                Box(
                                    modifier = Modifier
                                        .border(1.5.dp, PixelOutlineBlack)
                                        .background(promptColor)
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = uiState.activePromptType ?: "",
                                        color = PixelOutlineBlack,
                                        fontFamily = PressStart2PFont,
                                        fontSize = 9.sp
                                    )
                                }

                                val timerColor = when {
                                    uiState.isPaused -> PixelAmberGold
                                    uiState.timeRemaining <= 5 -> PixelMagentaHot
                                    uiState.timeRemaining <= 15 -> PixelAmberGold
                                    else -> PixelCrtCyan
                                }

                                Box(
                                    modifier = Modifier
                                        .border(1.5.dp, PixelOutlineBlack)
                                        .background(if (uiState.timeRemaining <= 5 && !uiState.isPaused) PixelMagentaHot else PixelVioletDark)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = PixelIcons.Clock,
                                            contentDescription = null,
                                            tint = if (uiState.timeRemaining <= 5 && !uiState.isPaused) PixelOutlineBlack else timerColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (uiState.isPaused) "PAUSED" else "${uiState.timeRemaining}s",
                                            color = if (uiState.timeRemaining <= 5 && !uiState.isPaused) PixelOutlineBlack else timerColor,
                                            fontFamily = PressStart2PFont,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            // Active Prompt Card Text
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.activePrompt ?: "",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }

                            // Progress Bar for Timer
                            val progress = if (uiState.timerDurationSec > 0) {
                                (uiState.timeRemaining.toFloat() / uiState.timerDurationSec.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            val progressColor = when {
                                uiState.isPaused -> PixelAmberGold
                                uiState.timeRemaining <= 5 -> PixelMagentaHot
                                uiState.timeRemaining <= 15 -> PixelAmberGold
                                else -> PixelCrtCyan
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .border(1.dp, PixelOutlineBlack)
                                    .background(PixelVioletDark)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = progress)
                                        .background(progressColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                if (uiState.activePrompt == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PrimaryPartyButton(
                            text = "TRUTH",
                            icon = PixelIcons.Lightbulb,
                            accentColor = PixelCrtCyan,
                            onClick = {
                                haptics.performPop()
                                viewModel.drawTruth()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PrimaryPartyButton(
                            text = "DARE",
                            icon = PixelIcons.Zap,
                            accentColor = PixelMagentaHot,
                            onClick = {
                                haptics.performPop()
                                viewModel.drawDare()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecondaryPartyButton(
                            text = if (uiState.isPaused) "RESUME" else "PAUSE",
                            icon = PixelIcons.Clock,
                            onClick = {
                                haptics.performTick(composeHaptics)
                                viewModel.togglePauseTimer()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PrimaryPartyButton(
                            text = "DONE",
                            icon = PixelIcons.Trophy,
                            accentColor = PixelEmeraldGreen,
                            onClick = {
                                haptics.performPop()
                                viewModel.completeFate()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Retro "TIME'S UP / FATE FORFEIT" Modal Overlay
            AnimatedVisibility(
                visible = uiState.isTimesUp,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xEE000000))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, PixelMagentaHot, RoundedCornerShape(2.dp))
                            .background(
                                brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                            )
                            .crtScanlines()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .border(2.dp, PixelOutlineBlack)
                                .background(PixelMagentaHot)
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = PixelIcons.Zap,
                                contentDescription = null,
                                tint = PixelOutlineBlack,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "TIME'S UP!",
                            color = PixelMagentaHot,
                            fontFamily = PressStart2PFont,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "FATE FORFEITED!",
                            color = PixelAmberGold,
                            fontFamily = PressStart2PFont,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "PLAYER ${uiState.currentPlayerTurn} RAN OUT OF TIME!",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryPartyButton(
                                text = "EXIT",
                                onClick = onExitGame,
                                modifier = Modifier.weight(1f)
                            )

                            PrimaryPartyButton(
                                text = "NEXT TURN",
                                accentColor = PixelCrtCyan,
                                onClick = {
                                    haptics.performPop()
                                    viewModel.nextTurn()
                                },
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
