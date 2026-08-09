package com.leminno.partygames.ui.games.most_likely_to

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
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay

val samplePrompts = listOf(
    "Who is most likely to survive a zombie apocalypse?",
    "Who is most likely to become a secret billionaire?",
    "Who is most likely to lock themselves out of their own home?",
    "Who is most likely to win a reality TV show?",
    "Who is most likely to cry during a funny cartoon movie?",
    "Who is most likely to spend all their money on food?",
    "Who is most likely to forget their own birthday?",
    "Who is most likely to get arrested for something silly?",
    "Who is most likely to start a viral TikTok trend?",
    "Who is most likely to accidentally text the wrong person?"
)

@Composable
fun MostLikelyToScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var currentPrompt by remember { mutableStateOf(samplePrompts.random()) }
    var countdownValue by remember { mutableStateOf<Int?>(null) }
    var votingPhase by remember { mutableStateOf(false) }
    var revealResults by remember { mutableStateOf(false) }

    // Simulated players list based on playerCount
    val playerNames = remember(playerCount) {
        List(playerCount) { idx -> "Player ${idx + 1}" }
    }

    var votesMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Countdown animation loop
    LaunchedEffect(countdownValue) {
        if (countdownValue != null && countdownValue!! > 0) {
            haptics.performTick(composeHaptics)
            delay(1000)
            countdownValue = countdownValue!! - 1
            if (countdownValue == 0) {
                haptics.performSuccess()
                countdownValue = null
                votingPhase = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF180224))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitGame) {
                    Text("✕", color = TextSecondary, fontSize = 22.sp)
                }
                Text(
                    text = "MOST LIKELY TO 🗳️",
                    color = Color(0xFFE0AFA0),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Prompt Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceGlassDark)
                    .border(2.dp, Color(0xFFE0AFA0), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "QUESTION CARD",
                        color = Color(0xFFE0AFA0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentPrompt,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                }
            }

            if (countdownValue != null) {
                // Animated 3-2-1 Countdown
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${countdownValue}",
                        color = Color(0xFFFF007F),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "POINT AT YOUR TARGET!",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (!votingPhase && !revealResults) {
                // Start Countdown Trigger
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Read the prompt out loud, then tap START COUNTDOWN to point together!",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            countdownValue = 3
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("START 3-2-1 COUNTDOWN ⏱️", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            } else if (votingPhase && !revealResults) {
                // Vote Tally Matrix
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TAP WHO RECEIVED VOTES",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        playerNames.forEach { player ->
                            val currentVotes = votesMap[player] ?: 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceGlassDark)
                                    .border(1.dp, BorderGlassDefault, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = player,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        if (currentVotes > 0) {
                                            haptics.performPop()
                                            votesMap = votesMap + (player to currentVotes - 1)
                                        }
                                    }) {
                                        Text("-", color = TextSecondary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = "$currentVotes",
                                        color = Color(0xFFE0AFA0),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(onClick = {
                                        haptics.performTick(composeHaptics)
                                        votesMap = votesMap + (player to currentVotes + 1)
                                    }) {
                                        Text("+", color = Color(0xFF00E676), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        haptics.performSuccess()
                        revealResults = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0AFA0)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("SHOW PERCENTAGE CHART 📊", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else {
                // Live Percentage Bar Chart Reveal
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VOTE BREAKDOWN 📊",
                        color = Color(0xFFE0AFA0),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val totalVotes = votesMap.values.sum().coerceAtLeast(1)

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        playerNames.forEach { player ->
                            val votes = votesMap[player] ?: 0
                            val pct = (votes.toFloat() / totalVotes.toFloat())

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = player, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${(pct * 100).toInt()}% ($votes votes)",
                                        color = Color(0xFFE0AFA0),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = Color(0xFFE0AFA0),
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        currentPrompt = samplePrompts.random()
                        countdownValue = null
                        votingPhase = false
                        revealResults = false
                        votesMap = emptyMap()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("NEXT QUESTION ▶", color = Color.White, fontWeight = FontWeight.Black)
                }
            }

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
