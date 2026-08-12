package com.leminno.partygames.ui.games.truthordare

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
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
        titleColor = AccentViolet,
        gameId = "truth_or_dare",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Deck Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Clean", "Party", "Extreme").forEach { deck ->
                    val isSelected = uiState.selectedDeck == deck
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentViolet else SurfaceGlassDark)
                            .border(1.dp, if (isSelected) AccentViolet else BorderGlassDefault, RoundedCornerShape(12.dp))
                            .clickable {
                                haptics.performTick(composeHaptics)
                                viewModel.selectDeck(deck)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = deck.uppercase(),
                            color = if (isSelected) Color.White else TextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceGlassDark)
                    .border(1.5.dp, AccentViolet.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.activePrompt == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(AccentViolet.copy(alpha = 0.2f))
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = AccentViolet,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "CHOOSE YOUR FATE",
                            color = AccentViolet,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select Truth or Dare below to generate a prompt!",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val promptColor = if (uiState.activePromptType == "TRUTH") AccentViolet else AccentMagenta
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(promptColor.copy(alpha = 0.2f))
                                .border(1.dp, promptColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = uiState.activePromptType ?: "",
                                color = promptColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = uiState.activePrompt ?: "",
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryPartyButton(
                    text = "TRUTH",
                    icon = Icons.Rounded.Psychology,
                    accentColor = AccentViolet,
                    onClick = {
                        haptics.performPop()
                        viewModel.drawTruth()
                    },
                    modifier = Modifier.weight(1f)
                )

                PrimaryPartyButton(
                    text = "DARE",
                    icon = Icons.Rounded.LocalFireDepartment,
                    accentColor = AccentMagenta,
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
