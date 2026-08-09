package com.leminno.partygames.ui.games.truthordare

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
        title = "TRUTH OR DARE 🔮",
        titleColor = Color(0xFF9D4EDD),
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
                            .background(if (isSelected) Color(0xFF9D4EDD) else SurfaceGlassDark)
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
                            fontSize = 13.sp,
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
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceGlassDark)
                    .border(2.dp, Color(0xFF9D4EDD), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.activePrompt == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔮", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "CHOOSE YOUR FATE",
                            color = Color(0xFF9D4EDD),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select Truth or Dare below to generate a card!",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.activePromptType ?: "",
                            color = if (uiState.activePromptType == "TRUTH") Color(0xFF9D4EDD) else Color(0xFFFF007F),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = uiState.activePrompt ?: "",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        haptics.performPop()
                        viewModel.drawTruth()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
                ) {
                    Text("TRUTH 🔮", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        haptics.performPop()
                        viewModel.drawDare()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F))
                ) {
                    Text("DARE 🔥", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
