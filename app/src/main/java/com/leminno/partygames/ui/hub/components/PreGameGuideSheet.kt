package com.leminno.partygames.ui.hub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.model.GameCategory
import com.leminno.partygames.ui.components.AiPromptGeneratorSheet
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.SecondaryPartyButton
import com.leminno.partygames.ui.model.GameItem
import com.leminno.partygames.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreGameGuideSheet(
    game: GameItem,
    onDismissRequest: () -> Unit,
    onStartGame: (playerCount: Int, roundTimerSec: Int, intensityDeck: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedPlayerCount by remember { mutableIntStateOf(game.minPlayers.coerceAtLeast(2)) }
    var selectedTimerSec by remember { mutableIntStateOf(60) }
    var selectedIntensity by remember { mutableStateOf("Party") }
    var showAiGenerator by remember { mutableStateOf(false) }
    var customPromptsCount by remember { mutableIntStateOf(0) }

    val categoryIcon = when (game.category) {
        GameCategory.TRIVIA -> PixelIcons.Lightbulb
        GameCategory.ACTION -> PixelIcons.Zap
        GameCategory.MYSTERY -> PixelIcons.Eye
        GameCategory.BOARD -> PixelIcons.Dice
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = PixelVioletElevated,
        scrimColor = Color(0xCC000000),
        shape = RoundedCornerShape(2.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Game Header Title & Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = game.title.uppercase(),
                        color = PixelCrtCyan,
                        fontFamily = PressStart2PFont,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = game.setupType.label.uppercase(),
                        color = PixelMagentaHot,
                        fontFamily = PressStart2PFont,
                        fontSize = 8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.5.dp, PixelOutlineBlack)
                        .background(PixelMagentaHot),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = PixelOutlineBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Anti-Cheat / Security Notice (If present)
            if (game.antiCheatNotice != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, PixelAmberGold, RoundedCornerShape(2.dp))
                        .background(PixelVioletDark)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PixelIcons.Shield,
                            contentDescription = null,
                            tint = PixelAmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANTI-CHEAT: ${game.antiCheatNotice}",
                            color = PixelAmberGold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Visual Rule Carousel
            if (game.rules.isNotEmpty()) {
                Text(
                    text = "HOW TO PLAY",
                    color = TextMuted,
                    fontFamily = PressStart2PFont,
                    fontSize = 8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val pagerState = rememberPagerState(pageCount = { game.rules.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                ) { page ->
                    val rule = game.rules[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 2.dp)
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(
                                brush = pixelBandedVertical(
                                    listOf(PixelVioletBase, PixelVioletDark)
                                )
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .border(1.dp, PixelOutlineBlack)
                                    .background(PixelCrtCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${rule.stepNumber}",
                                    color = PixelOutlineBlack,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = rule.title,
                                    color = TextPrimary,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = rule.description,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Page Indicator Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(game.rules.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) PixelCrtCyan else PixelVioletDark
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(8.dp)
                                .border(1.dp, PixelOutlineBlack)
                                .background(color)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Setup Controls Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAME SETUP",
                    color = TextMuted,
                    fontFamily = PressStart2PFont,
                    fontSize = 8.sp
                )

                TextButton(onClick = { showAiGenerator = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PixelIcons.Zap,
                            contentDescription = null,
                            tint = PixelCrtCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (customPromptsCount > 0) "CUSTOM ($customPromptsCount)" else "AI PACK",
                            color = PixelCrtCyan,
                            fontFamily = PressStart2PFont,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            // Player Count Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Players:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (selectedPlayerCount > game.minPlayers) selectedPlayerCount-- },
                        enabled = selectedPlayerCount > game.minPlayers
                    ) {
                        Text("-", color = TextPrimary, fontFamily = PressStart2PFont, fontSize = 14.sp)
                    }
                    Text(
                        text = "$selectedPlayerCount",
                        color = PixelCrtCyan,
                        fontFamily = PressStart2PFont,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { if (selectedPlayerCount < game.maxPlayers) selectedPlayerCount++ },
                        enabled = selectedPlayerCount < game.maxPlayers
                    ) {
                        Text("+", color = TextPrimary, fontFamily = PressStart2PFont, fontSize = 14.sp)
                    }
                }
            }

            // Timer Duration Selector
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Timer:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(30, 60, 90).forEach { timerSec ->
                        val isSel = selectedTimerSec == timerSec
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                                .background(if (isSel) PixelCrtCyan else PixelVioletBase)
                                .clickable { selectedTimerSec = timerSec }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${timerSec}s",
                                color = if (isSel) PixelOutlineBlack else TextMuted,
                                fontFamily = PressStart2PFont,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            // Deck Intensity Selector
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deck:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Clean", "Party", "Extreme").forEach { intensity ->
                        val isSel = selectedIntensity == intensity
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                                .background(if (isSel) PixelMagentaHot else PixelVioletBase)
                                .clickable { selectedIntensity = intensity }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = intensity.uppercase(),
                                color = if (isSel) PixelOutlineBlack else TextMuted,
                                fontFamily = PressStart2PFont,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Game CTA Button
            PrimaryPartyButton(
                text = "START GAME",
                accentColor = PixelCrtCyan,
                onClick = {
                    onStartGame(selectedPlayerCount, selectedTimerSec, selectedIntensity)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
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
