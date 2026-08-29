package com.leminno.partygames.ui.games.i_want_to_be

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

data class CareerItem(
    val title: String,
    val category: String,
    val exampleClues: List<String>
)

val sampleCareers = listOf(
    CareerItem("Astronaut", "Science & Space", listOf("I work high up", "Special suit required", "Zero gravity")),
    CareerItem("Formula 1 Driver", "Sports", listOf("Very fast machine", "Helmet & racing suit", "Pit stops")),
    CareerItem("Brain Surgeon", "Medical", listOf("High precision", "Scrubs and scalpels", "Works in operating room")),
    CareerItem("Chocolatier", "Culinary", listOf("Sweet creations", "Melts ingredients", "Tasting tests")),
    CareerItem("Private Detective", "Investigation", listOf("Magnifying glass", "Solves mysteries", "Gathers clues")),
    CareerItem("Stunt Double", "Entertainment", listOf("Takes big risks", "Appears in movies", "Looks like main actor")),
    CareerItem("Architect", "Design", listOf("Draws blueprints", "Designs buildings", "Works with hard hats")),
    CareerItem("Submarine Captain", "Maritime", listOf("Under ocean", "Uses periscope", "Sonar signals"))
)

@Composable
fun IWantToBeScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var currentCareer by remember { mutableStateOf(sampleCareers.random()) }
    var isRevealed by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var clueStep by remember { mutableIntStateOf(1) }
    var timerSeconds by remember { mutableIntStateOf(30) }
    var timerRunning by remember { mutableStateOf(false) }
    var gameFinished by remember { mutableStateOf(false) }
    var groupGuessedRight by remember { mutableStateOf<Boolean?>(null) }

    // Hold-to-reveal gesture loop
    LaunchedEffect(isRevealed, holdProgress) {
        if (!isRevealed && holdProgress > 0f && holdProgress < 1f) {
            delay(50)
            holdProgress += 0.05f
            if (holdProgress >= 1f) {
                haptics.performPop()
                isRevealed = true
            }
        }
    }

    // Timer loop
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (timerSeconds > 0 && timerRunning) {
                delay(1000)
                timerSeconds--
            }
            if (timerSeconds <= 0) {
                timerRunning = false
                haptics.performHeavyBurst()
            }
        }
    }

    GameScaffold(
        title = "I Want To Be...",
        titleColor = TextPrimary,
        gameId = "i_want_to_be",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!isRevealed) {
                // Anti-Cheat Hold-To-Reveal Card
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Secret Role Card",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(260.dp)
                            .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        holdProgress = 0.1f
                                        tryAwaitRelease()
                                        if (!isRevealed) holdProgress = 0f
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🔒", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Hold Screen To Reveal",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Anti-cheat: Prevents nearby players from peeking while passing phone.",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            if (holdProgress > 0f) {
                                Spacer(modifier = Modifier.height(16.dp))
                                LinearProgressIndicator(
                                    progress = { holdProgress },
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .clip(RoundedCornerShape(8.dp)),
                                    color = BrandPrimary,
                                    trackColor = SurfaceSubtle
                                )
                            }
                        }
                    }
                }
            } else if (!gameFinished) {
                // Secret Revealed & Clue Stage
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Career Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MysteryContainer)
                                    .border(1.dp, MysteryBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentCareer.category.uppercase(),
                                    color = MysteryText,
                                    fontFamily = ModernSansFont,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentCareer.title,
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timer & Clues Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⏱️ ${timerSeconds}s",
                                color = if (timerSeconds <= 5) AlertRed else TextPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Give 3 subtle clues to your friends out loud!",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Sample Hints Helper
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentCareer.exampleClues.forEachIndexed { idx, clue ->
                                    val isUnlocked = idx + 1 <= clueStep
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isUnlocked) BrandPrimaryContainer else SurfaceSubtle)
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Clue #${idx + 1}: ${if (isUnlocked) clue else "••••••••••••"}",
                                            color = if (isUnlocked) BrandPrimary else TextSecondary,
                                            fontFamily = ModernSansFont,
                                            fontSize = 13.sp,
                                            fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal
                                        )

                                        if (!isUnlocked) {
                                            IconButton(
                                                onClick = {
                                                    haptics.performTick(composeHaptics)
                                                    clueStep = idx + 1
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Text("🔓", fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryPartyButton(
                        text = if (timerRunning) "Pause ⏱️" else "Start ⏱️",
                        onClick = { timerRunning = !timerRunning },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryPartyButton(
                        text = "Submit Guess 🎯",
                        accentColor = BrandPrimary,
                        onClick = { gameFinished = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Game Finished & Scoring Result
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "Did Friends Guess Correctly?",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Secret Profession: ${currentCareer.title}",
                        color = BrandPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(130.dp, 56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SuccessContainer)
                                .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable {
                                    haptics.performPop()
                                    groupGuessedRight = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Yes! 🎉", color = SuccessGreen, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Box(
                            modifier = Modifier
                                .size(130.dp, 56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AlertContainer)
                                .border(1.dp, AlertRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable {
                                    haptics.performHeavyBurst()
                                    groupGuessedRight = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No 😅", color = AlertRed, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    groupGuessedRight?.let { right ->
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (right) "Awesome! 1 Point awarded to Guessers!" else "Tricky! 1 Point awarded to Active Player!",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (isRevealed) {
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryPartyButton(
                    text = "Next Player Turn ▶",
                    accentColor = BrandPrimary,
                    onClick = {
                        currentCareer = sampleCareers.random()
                        isRevealed = false
                        holdProgress = 0f
                        clueStep = 1
                        timerSeconds = 30
                        timerRunning = false
                        gameFinished = false
                        groupGuessedRight = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

