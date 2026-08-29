package com.leminno.partygames.ui.games.silent_library

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.SecondaryPartyButton
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

    GameScaffold(
        title = "Silent Library",
        titleColor = TextPrimary,
        gameId = "silent_library",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Task Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceLight)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MysteryContainer)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SILENT CHALLENGE",
                            color = MysteryText,
                            fontFamily = ModernSansFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentTask,
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
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
                        text = "${timerSeconds}s",
                        color = if (timerSeconds <= 5) AlertRed else TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Distraction Sound Triggers",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ActionContainer)
                                .border(1.dp, ActionBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    haptics.performPop()
                                    playDistractionSound(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🦆 Quack", color = ActionText, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BoardContainer)
                                .border(1.dp, BoardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    haptics.performPop()
                                    playDistractionSound(ToneGenerator.TONE_PROP_BEEP)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📢 Horn", color = BoardText, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AlertContainer)
                                .border(1.dp, AlertRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable {
                                    haptics.performPop()
                                    playDistractionSound(ToneGenerator.TONE_SUP_DIAL)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤡 Siren", color = AlertRed, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryPartyButton(
                        text = if (isTimerRunning) "Pause ⏱️" else "Start Challenge ▶",
                        accentColor = BrandPrimary,
                        onClick = { isTimerRunning = !isTimerRunning },
                        modifier = Modifier.weight(1f)
                    )

                    SecondaryPartyButton(
                        text = "Laughed! 😅",
                        onClick = {
                            isTimerRunning = false
                            challengeCompleted = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Group Evaluation View
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "Did they maintain a straight face?",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(140.dp, 56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SuccessContainer)
                                .border(1.dp, SuccessGreen, RoundedCornerShape(16.dp))
                                .clickable {
                                    haptics.performPop()
                                    playerLaughed = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Success! 🤐", color = SuccessGreen, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .size(140.dp, 56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AlertContainer)
                                .border(1.dp, AlertRed, RoundedCornerShape(16.dp))
                                .clickable {
                                    haptics.performHeavyBurst()
                                    playerLaughed = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Failed! 🤣", color = AlertRed, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold)
                        }
                    }

                    playerLaughed?.let { laughed ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!laughed) SuccessContainer else AlertContainer)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (!laughed) "Master of Focus! +1 Point!" else "Broke Character! Penalty point to group!",
                                color = if (!laughed) SuccessGreen else AlertRed,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                PrimaryPartyButton(
                    text = "Draw Next Card ▶",
                    accentColor = BrandPrimary,
                    onClick = {
                        currentTask = silentLibraryTasks.random()
                        timerSeconds = 30
                        isTimerRunning = false
                        challengeCompleted = false
                        playerLaughed = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

