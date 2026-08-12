package com.leminno.partygames.ui.games.neverhaveiever

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
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
        titleColor = AccentMagenta,
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentMagenta.copy(alpha = 0.2f))
                        .border(1.dp, AccentMagenta.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = AccentMagenta,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$livesRemaining LIVES REMAINING",
                            color = AccentMagenta,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Center Prompt Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceGlassDark)
                        .border(1.5.dp, AccentMagenta.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentMagenta.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "NEVER HAVE I EVER...",
                                color = AccentMagenta,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = prompts[currentPromptIndex % prompts.size],
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                    }
                }

                // Lives Tracker Hearts Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TAP HEART IF YOU HAVE DONE THIS:",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
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
                                    .background(if (isAlive) AccentMagenta.copy(alpha = 0.2f) else SurfaceGlassLight)
                                    .border(1.dp, if (isAlive) AccentMagenta else BorderGlassDefault, CircleShape)
                                    .clickable {
                                        if (livesRemaining > 0) {
                                            livesRemaining--
                                            haptics.performWarningThud()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAlive) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isAlive) AccentMagenta else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Next Prompt Button
                PrimaryPartyButton(
                    text = "NEXT PROMPT",
                    icon = Icons.Rounded.ArrowForward,
                    accentColor = AccentCyan,
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
