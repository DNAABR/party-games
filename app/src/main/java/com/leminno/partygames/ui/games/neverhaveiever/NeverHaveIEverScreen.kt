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

    val players = remember(playerCount) {
        com.leminno.partygames.data.repository.UserPreferencesRepository.getActiveRoster(playerCount)
    }

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
            "Never have I ever sang out loud in the shower.",
            "Never have I ever ghosted someone after a first date.",
            "Never have I ever stayed up for 24 hours straight.",
            "Never have I ever lied about my age to get into a venue.",
            "Never have I ever regifted a present I didn't want.",
            "Never have I ever looked through someone else's phone without permission."
        ).shuffled()
    }

    var currentPromptIndex by remember { mutableIntStateOf(0) }
    val currentReaderIndex = currentPromptIndex % players.size
    val currentReaderName = players.getOrElse(currentReaderIndex) { "Player 1" }

    // Map each player to their remaining lives (starts at 5 lives each)
    var playerLives by remember(players) {
        mutableStateOf(players.associateWith { 5 })
    }

    var showScoreboard by remember { mutableStateOf(false) }

    val isGameOver = playerLives.values.all { it <= 0 }

    GameScaffold(
        title = "NEVER HAVE I EVER",
        titleColor = PixelMagentaHot,
        gameId = "never_have_i_ever",
        onExitGame = onExitGame
    ) {
        if (!isGameOver) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Player Turn Banner
                com.leminno.partygames.ui.components.InGamePlayerHeader(
                    currentPlayerName = currentReaderName,
                    playerIndex = currentReaderIndex,
                    totalPlayers = players.size,
                    onOpenScoreboard = { showScoreboard = true }
                )

                // CRT Prompt Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 10.dp)
                        .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                        .background(
                            brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                        )
                        .crtScanlines()
                        .padding(18.dp),
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = prompts[currentPromptIndex % prompts.size],
                            color = PixelCrtCyan,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Player Lives Tap Badges (Who did it? Tap to forfeit life)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TAP PLAYER IF THEY DID IT (-1 ❤️):",
                        color = TextMuted,
                        fontFamily = PressStart2PFont,
                        fontSize = 7.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        players.forEachIndexed { index, player ->
                            val lives = playerLives[player] ?: 0
                            val isAlive = lives > 0
                            val color = com.leminno.partygames.ui.components.PlayerAvatarColors.getOrElse(index % com.leminno.partygames.ui.components.PlayerAvatarColors.size) { PixelCrtCyan }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.5.dp, if (isAlive) PixelOutlineBlack else PixelAlertRed, RoundedCornerShape(2.dp))
                                    .background(if (isAlive) PixelVioletElevated else PixelVioletDark)
                                    .clickable {
                                        if (isAlive) {
                                            val updated = playerLives.toMutableMap()
                                            updated[player] = lives - 1
                                            playerLives = updated
                                            haptics.performWarningThud()
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = player.take(6).uppercase(),
                                        color = if (isAlive) color else TextMuted,
                                        fontFamily = PressStart2PFont,
                                        fontSize = 7.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isAlive) PixelIcons.Heart else PixelIcons.HeartBorder,
                                            contentDescription = null,
                                            tint = if (isAlive) PixelAlertRed else TextMuted,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "$lives",
                                            color = if (isAlive) TextPrimary else TextMuted,
                                            fontFamily = PressStart2PFont,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Next Prompt Push Button
                PrimaryPartyButton(
                    text = "NEXT PROMPT ▶",
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
                subtitle = "Party survived ${currentPromptIndex + 1} confession rounds!",
                onPlayAgain = {
                    playerLives = players.associateWith { 5 }
                    currentPromptIndex = 0
                },
                onBackToHub = onExitGame
            )
        }

        if (showScoreboard) {
            com.leminno.partygames.ui.components.InGameScoreboardModal(
                players = players,
                onDismissRequest = { showScoreboard = false }
            )
        }
    }
}

