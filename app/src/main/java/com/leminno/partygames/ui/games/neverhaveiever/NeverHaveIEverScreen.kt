package com.leminno.partygames.ui.games.neverhaveiever

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.SecondaryPartyButton
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*

@Composable
fun NeverHaveIEverScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    val prompts = remember {
        listOf(
            "Never have I ever fallen asleep during a movie at the theater.",
            "Never have I ever pretended to know a song I actually had never heard before.",
            "Never have I ever sent a message to the wrong person.",
            "Never have I ever eaten food that fell on the floor after 5 seconds.",
            "Never have I ever gotten lost in my own hometown.",
            "Never have I ever blamed a bad smell on a pet or someone else.",
            "Never have I ever locked myself out of my own house or car.",
            "Never have I ever accidentally liked an old photo on social media.",
            "Never have I ever tried to cut my own hair.",
            "Never have I ever sang out loud in the shower."
        ).shuffled()
    }

    var currentPromptIndex by remember { mutableIntStateOf(0) }
    var livesRemaining by remember { mutableIntStateOf(10) }

    GameScaffold(
        title = "Never Have I Ever",
        titleColor = TextPrimary,
        gameId = "never_have_i_ever",
        onExitGame = onExitGame
    ) {
        if (livesRemaining > 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Lives Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(AlertContainer)
                        .border(1.dp, AlertRed.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PixelIcons.Heart,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$livesRemaining Lives Left",
                            color = AlertRed,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Prompt Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .subtleCardShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandPrimaryContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "NEVER HAVE I EVER...",
                                color = BrandPrimary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = prompts[currentPromptIndex % prompts.size],
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )
                    }
                }

                // Lives Tracker Hearts Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tap a heart if you've done this:",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(10) { index ->
                            val isAlive = index < livesRemaining
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isAlive) AlertContainer else SurfaceSubtle)
                                    .clickable {
                                        if (livesRemaining > 0) {
                                            livesRemaining--
                                            haptics.performWarningThud()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAlive) PixelIcons.Heart else PixelIcons.HeartBorder,
                                    contentDescription = null,
                                    tint = if (isAlive) AlertRed else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryPartyButton(
                        text = "I've Done This (-1)",
                        onClick = {
                            if (livesRemaining > 0) {
                                livesRemaining--
                                haptics.performWarningThud()
                            }
                            currentPromptIndex++
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryPartyButton(
                        text = "Next Prompt",
                        icon = PixelIcons.Zap,
                        accentColor = BrandPrimary,
                        onClick = {
                            currentPromptIndex++
                            haptics.performPop()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "All Lives Lost!",
                subtitle = "You survived $currentPromptIndex rounds!",
                onPlayAgain = {
                    livesRemaining = 10
                    currentPromptIndex = 0
                },
                onBackToHub = onExitGame
            )
        }
    }
}

