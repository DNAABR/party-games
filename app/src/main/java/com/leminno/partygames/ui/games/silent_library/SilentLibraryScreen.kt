package com.leminno.partygames.ui.games.silent_library

import android.media.AudioManager
import android.media.ToneGenerator
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

val silentLibraryTasks = listOf(
    "Say 'I love raw onions' 5 times with a deadpan face.",
    "Stare continuously into the active player's eyes without smiling for 30 seconds.",
    "Do 5 jumping jacks while whispering 'I am a gentle butterfly'.",
    "Gently pat your own head while rubbing your belly, keeping a completely serious expression.",
    "Say your full name backwards with absolute seriousness.",
    "Act like a robot asking for oil without laughing.",
    "Make the most ridiculous confused face without smiling."
)

@Composable
fun SilentLibraryScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var currentTask by remember { mutableStateOf(silentLibraryTasks.random()) }
    var timerSeconds by remember { mutableIntStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var challengeCompleted by remember { mutableStateOf(false) }
    var playerLaughed by remember { mutableStateOf<Boolean?>(null) }

    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }

    fun playDistractionSound(toneType: Int) {
        try {
            toneGenerator.startTone(toneType, 300)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGenerator.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Timer countdown
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timerSeconds > 0 && isTimerRunning) {
                delay(1000)
                timerSeconds--
            }
            if (timerSeconds <= 0) {
                isTimerRunning = false
                challengeCompleted = true
                haptics.performPop()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
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
                    text = "SILENT LIBRARY 🤫",
                    color = Color(0xFFFFD166),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Task Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceGlassDark)
                    .border(2.dp, Color(0xFFFFD166), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CHALLENGE CARD",
                        color = Color(0xFFFFD166),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentTask,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                }
            }

            if (!challengeCompleted) {
                // Live Timer & Distraction Sound Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⏱️ ${timerSeconds}s",
                        color = if (timerSeconds <= 5) Color(0xFFFF0055) else Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "DISTRACTION SOUND TRIGGERS 🔊",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                haptics.performPop()
                                playDistractionSound(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🦆 Quack", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                haptics.performPop()
                                playDistractionSound(ToneGenerator.TONE_PROP_BEEP)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("📢 Horn", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Button(
                            onClick = {
                                haptics.performPop()
                                playDistractionSound(ToneGenerator.TONE_SUP_DIAL)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🤡 Siren", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            isTimerRunning = !isTimerRunning
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTimerRunning) Color(0xFFFFB300) else Color(0xFFFFD166)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Text(
                            text = if (isTimerRunning) "PAUSE ⏱️" else "START CHALLENGE ▶",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = {
                            isTimerRunning = false
                            challengeCompleted = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Text("LAUGHED! 😅", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                // Group Evaluation View
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DID THEY MAINTAIN STRAIGHT FACE?",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                haptics.performPop()
                                playerLaughed = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(130.dp, 60.dp)
                        ) {
                            Text("SUCCESS! 🤐", color = Color.Black, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                haptics.performHeavyBurst()
                                playerLaughed = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(130.dp, 60.dp)
                        ) {
                            Text("FAILED! 🤣", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }

                    playerLaughed?.let { laughed ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (!laughed) "Master of Iron Focus! +1 Point!" else "Broke Character! Penalty point to group!",
                            color = if (!laughed) Color(0xFF00E676) else Color(0xFFFF0055),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        currentTask = silentLibraryTasks.random()
                        timerSeconds = 30
                        isTimerRunning = false
                        challengeCompleted = false
                        playerLaughed = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("DRAW NEXT CARD ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
