package com.leminno.partygames.ui.games.truthordare

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
        title = "Truth or Dare",
        titleColor = TextPrimary,
        gameId = "truth_or_dare",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Deck Selector Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceSubtle)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Clean", "Party", "Extreme").forEach { deck ->
                    val isSelected = uiState.selectedDeck == deck
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) BrandPrimary else Color.Transparent)
                            .clickable {
                                haptics.performTick(composeHaptics)
                                viewModel.selectDeck(deck)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = deck,
                            color = if (isSelected) TextOnPrimary else TextSecondary,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Screen Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .subtleCardShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceLight)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.activePrompt == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(BrandPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✨", fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Choose Your Fate",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pick Truth or Dare below to reveal your challenge",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val isTruth = uiState.activePromptType == "TRUTH"
                        val badgeBg = if (isTruth) TriviaContainer else ActionContainer
                        val badgeText = if (isTruth) TriviaText else ActionText
                        val badgeBorder = if (isTruth) TriviaBorder else ActionBorder

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeBg)
                                .border(1.dp, badgeBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = uiState.activePromptType ?: "",
                                color = badgeText,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = uiState.activePrompt ?: "",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
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
                    text = "Truth",
                    icon = PixelIcons.Lightbulb,
                    accentColor = BrandPrimary,
                    onClick = {
                        haptics.performPop()
                        viewModel.drawTruth()
                    },
                    modifier = Modifier.weight(1f)
                )

                PrimaryPartyButton(
                    text = "Dare",
                    icon = PixelIcons.Zap,
                    accentColor = BoardPrimary,
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

