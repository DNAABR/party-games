package com.leminno.partygames.ui.hub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.AiPromptGeneratorSheet
import com.leminno.partygames.ui.model.GameItem
import com.leminno.partygames.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreGameGuideSheet(
    game: GameItem,
    onDismissRequest: () -> Unit,
    onStartGame: (playerCount: Int, roundTimerSec: Int, intensityDeck: String) -> Unit
) {
    val categoryToken = CategoryThemeToken.forCategory(game.category)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedPlayerCount by remember { mutableIntStateOf(game.minPlayers.coerceAtLeast(2)) }
    var selectedTimerSec by remember { mutableIntStateOf(60) }
    var selectedIntensity by remember { mutableStateOf("Party") }
    var showAiGenerator by remember { mutableStateOf(false) }
    var customPromptsCount by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = BackgroundNavySlate,
        scrimColor = Color(0xCC000000),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x66FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Game Header Title & Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = game.title,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${game.setupType.badgeIcon} ${game.setupType.label}",
                        color = categoryToken.primaryAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(categoryToken.primaryAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = game.category.iconSymbol,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anti-Cheat / Security Notice (If present)
            if (game.antiCheatNotice != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x22FFD700))
                        .border(1.dp, Color(0x80FFD700), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️ ", fontSize = 16.sp)
                        Text(
                            text = "ANTI-CHEAT NOTICE: ${game.antiCheatNotice}",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Visual Rule Carousel (3 Slides)
            if (game.rules.isNotEmpty()) {
                Text(
                    text = "HOW TO PLAY",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val pagerState = rememberPagerState(pageCount = { game.rules.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) { page ->
                    val rule = game.rules[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceGlassDark)
                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rule.iconSymbol,
                                fontSize = 32.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column {
                                Text(
                                    text = "Step ${rule.stepNumber}: ${rule.title}",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = rule.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Page Indicator Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(game.rules.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) categoryToken.primaryAccent else Color(0x33FFFFFF)
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player Setup Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAME SETUP",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                TextButton(onClick = { showAiGenerator = true }) {
                    Text(
                        text = if (customPromptsCount > 0) "✨ Custom Pack ($customPromptsCount prompts)" else "✨ AI Custom Pack",
                        color = Color(0xFF00F2FE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Player Count Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Players:", color = TextSecondary, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (selectedPlayerCount > game.minPlayers) selectedPlayerCount-- },
                        enabled = selectedPlayerCount > game.minPlayers
                    ) {
                        Text("-", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "$selectedPlayerCount",
                        color = categoryToken.primaryAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { if (selectedPlayerCount < game.maxPlayers) selectedPlayerCount++ },
                        enabled = selectedPlayerCount < game.maxPlayers
                    ) {
                        Text("+", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Timer Duration Selector
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Timer:", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(30, 60, 90).forEach { timerSec ->
                        val isSel = selectedTimerSec == timerSec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) categoryToken.primaryAccent else SurfaceGlassDark)
                                .clickable { selectedTimerSec = timerSec }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${timerSec}s",
                                color = if (isSel) Color.White else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Deck Intensity Selector
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deck:", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Clean", "Party", "Extreme").forEach { intensity ->
                        val isSel = selectedIntensity == intensity
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) categoryToken.primaryAccent else SurfaceGlassDark)
                                .clickable { selectedIntensity = intensity }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = intensity,
                                color = if (isSel) Color.White else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Game CTA Button
            Button(
                onClick = {
                    onStartGame(selectedPlayerCount, selectedTimerSec, selectedIntensity)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                categoryToken.primaryAccent,
                                categoryToken.secondaryAccent
                            )
                        )
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text(
                    text = "START GAME 🎮",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showAiGenerator) {
            AiPromptGeneratorSheet(
                gameTitle = game.title,
                onPromptsGenerated = { prompts ->
                    customPromptsCount = prompts.size
                },
                onDismissRequest = { showAiGenerator = false }
            )
        }
    }
}
