package com.leminno.partygames.ui.games.truthordare

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
import com.leminno.partygames.ui.theme.*

@Composable
fun TruthOrDareScreen(
    playerCount: Int = 4,
    viewModel: TruthOrDareViewModel = viewModel(),
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(playerCount) {
        viewModel.initGame(playerCount)
    }

    GameScaffold(
        title = "TRUTH OR DARE",
        titleColor = PixelCrtCyan,
        gameId = "truth_or_dare",
        onExitGame = onExitGame
    ) {
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

            Spacer(modifier = Modifier.height(14.dp))

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
                    .padding(20.dp),
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
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SELECT TRUTH OR DARE BELOW",
                            color = TextMuted,
                            fontFamily = PressStart2PFont,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val promptColor = if (uiState.activePromptType == "TRUTH") PixelCrtCyan else PixelMagentaHot
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, PixelOutlineBlack)
                                .background(promptColor)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = uiState.activePromptType ?: "",
                                color = PixelOutlineBlack,
                                fontFamily = PressStart2PFont,
                                fontSize = 9.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = uiState.activePrompt ?: "",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stepped Action Push Buttons
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
        }
    }
}
