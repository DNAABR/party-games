package com.leminno.partygames.ui.games.neverhaveiever

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
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
        title = "NEVER HAVE I EVER",
        titleColor = PixelMagentaHot,
        gameId = "never_have_i_ever",
        onExitGame = onExitGame
    ) {
        if (livesRemaining > 0) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Lives Badge
                Box(
                    modifier = Modifier
                        .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                        .background(PixelVioletElevated)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PixelIcons.Heart,
                            contentDescription = null,
                            tint = PixelAlertRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$livesRemaining LIVES LEFT",
                            color = PixelAlertRed,
                            fontFamily = PressStart2PFont,
                            fontSize = 10.sp
                        )
                    }
                }

                // CRT Prompt Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 14.dp)
                        .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                        .background(
                            brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                        )
                        .crtScanlines()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, PixelOutlineBlack)
                                .background(PixelMagentaHot)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "NEVER HAVE I EVER...",
                                color = PixelOutlineBlack,
                                fontFamily = PressStart2PFont,
                                fontSize = 8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = prompts[currentPromptIndex % prompts.size],
                            color = PixelCrtCyan,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }

                // Lives Tracker Hearts Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TAP HEART IF DONE:",
                        color = TextMuted,
                        fontFamily = PressStart2PFont,
                        fontSize = 8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(10) { index ->
                            val isAlive = index < livesRemaining
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                                    .background(if (isAlive) PixelVioletElevated else PixelVioletDark)
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
                                    tint = if (isAlive) PixelAlertRed else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Next Prompt Push Button
                PrimaryPartyButton(
                    text = "NEXT PROMPT",
                    icon = PixelIcons.Zap,
                    accentColor = PixelCrtCyan,
                    onClick = {
                        currentPromptIndex++
                        haptics.performPop()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "ALL LIVES LOST!",
                subtitle = "You survived ${currentPromptIndex + 1} rounds!",
                onPlayAgain = {
                    livesRemaining = 10
                    currentPromptIndex = 0
                },
                onBackToHub = onExitGame
            )
        }
    }
}
