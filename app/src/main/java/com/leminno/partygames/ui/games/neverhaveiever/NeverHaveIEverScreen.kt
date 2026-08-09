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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundNavySlate)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitGame) {
                    Text("❌", fontSize = 20.sp)
                }

                Text(
                    text = "NEVER HAVE I EVER 🖐️",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FF007F))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("❤️ $livesRemaining Lives", color = Color(0xFFFF007F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Center Prompt Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceGlassDark)
                    .border(2.dp, Color(0x66FF007F), RoundedCornerShape(28.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NEVER HAVE I EVER...",
                        color = Color(0xFFFF007F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = prompts[currentPromptIndex % prompts.size],
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                }
            }

            // Lives Tracker Hearts Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TAP HEART IF YOU HAVE DONE THIS:", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(10) { index ->
                        val isAlive = index < livesRemaining
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isAlive) Color(0xFFFF007F) else Color(0x22FFFFFF))
                                .clickable {
                                    if (livesRemaining > 0) {
                                        livesRemaining--
                                        haptics.performWarningThud()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isAlive) "❤️" else "🖤", fontSize = 14.sp)
                        }
                    }
                }
            }

            // Next Prompt Button
            Button(
                onClick = {
                    currentPromptIndex++
                    haptics.performPop()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE))
            ) {
                Text("NEXT PROMPT ▶", color = BackgroundObsidian, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
