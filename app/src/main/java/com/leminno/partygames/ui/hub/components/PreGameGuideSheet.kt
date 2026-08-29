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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.model.GameCategory
import com.leminno.partygames.ui.components.AiPromptGeneratorSheet
import com.leminno.partygames.ui.components.PrimaryPartyButton
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

    val categoryToken = CategoryThemeToken.forCategory(game.category)

    val categoryIcon = when (game.category) {
        GameCategory.TRIVIA -> PixelIcons.Lightbulb
        GameCategory.ACTION -> PixelIcons.Zap
        GameCategory.MYSTERY -> PixelIcons.Eye
        GameCategory.BOARD -> PixelIcons.Dice
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        scrimColor = Color(0x660F172A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
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
                        fontFamily = ModernSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = game.setupType.label,
                        color = BrandPrimary,
                        fontFamily = ModernSansFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(categoryToken.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryToken.textColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Anti-Cheat / Security Notice (If present)
            if (game.antiCheatNotice != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WarningContainer)
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PixelIcons.Shield,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = game.antiCheatNotice,
                            color = Color(0xFF92400E),
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Visual Rule Carousel
            if (game.rules.isNotEmpty()) {
                Text(
                    text = "HOW TO PLAY",
                    color = TextSecondary,
                    fontFamily = ModernSansFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val pagerState = rememberPagerState(pageCount = { game.rules.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                ) { page ->
                    val rule = game.rules[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceSubtle)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${rule.stepNumber}",
                                    color = TextOnPrimary,
                                    fontFamily = ModernSansFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = rule.title,
                                    color = TextPrimary,
                                    fontFamily = ModernSansFont,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = rule.description,
                                    color = TextSecondary,
                                    fontFamily = ModernSansFont,
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
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(game.rules.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(if (isSelected) 16.dp else 6.dp, 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) BrandPrimary else BorderSubtle)
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
                    color = TextSecondary,
                    fontFamily = ModernSansFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )

                TextButton(onClick = { showAiGenerator = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✨", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (customPromptsCount > 0) "Custom ($customPromptsCount)" else "AI Prompt Pack",
                            color = BrandPrimary,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
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
                Text("Players", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (selectedPlayerCount > game.minPlayers) selectedPlayerCount-- },
                        enabled = selectedPlayerCount > game.minPlayers,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SurfaceSubtle)
                    ) {
                        Text("-", color = TextPrimary, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(
                        text = "$selectedPlayerCount",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    IconButton(
                        onClick = { if (selectedPlayerCount < game.maxPlayers) selectedPlayerCount++ },
                        enabled = selectedPlayerCount < game.maxPlayers,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SurfaceSubtle)
                    ) {
                        Text("+", color = TextPrimary, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                Text("Round Timer", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90).forEach { timerSec ->
                        val isSel = selectedTimerSec == timerSec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) BrandPrimary else SurfaceSubtle)
                                .border(1.dp, if (isSel) BrandPrimary else BorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { selectedTimerSec = timerSec }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${timerSec}s",
                                color = if (isSel) TextOnPrimary else TextSecondary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
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
                Text("Deck Intensity", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Clean", "Party", "Extreme").forEach { intensity ->
                        val isSel = selectedIntensity == intensity
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) BrandPrimary else SurfaceSubtle)
                                .border(1.dp, if (isSel) BrandPrimary else BorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { selectedIntensity = intensity }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = intensity,
                                color = if (isSel) TextOnPrimary else TextSecondary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Start Game CTA Button
            PrimaryPartyButton(
                text = "Start Game",
                accentColor = BrandPrimary,
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

