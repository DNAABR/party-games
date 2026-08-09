package com.leminno.partygames.ui.games.i_want_to_be

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
        title = "I Want To Be... 💼",
        titleColor = Color(0xFF00F2FE),
        gameId = "i_want_to_be",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!isRevealed) {
                // Anti-Cheat Hold-To-Reveal Card
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SECRET ROLE CARD",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(260.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceGlassDark)
                            .border(2.dp, Color(0xFF00F2FE), RoundedCornerShape(24.dp))
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
                            Text(text = "🔒", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "HOLD SCREEN TO REVEAL",
                                color = Color(0xFF00F2FE),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Anti-cheat protection: Prevents nearby players from peeking while passing phone.",
                                color = TextMuted,
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
                                    color = Color(0xFF00F2FE),
                                    trackColor = Color.White.copy(alpha = 0.1f)
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "YOUR SECRET PROFESSION", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentCareer.title,
                                color = Color(0xFF00F2FE),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Category: ${currentCareer.category}",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Timer & Clues Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⏱️ ${timerSeconds}s",
                                color = if (timerSeconds <= 5) Color(0xFFFF0055) else Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Give 3 subtle clues to your friends out loud!",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

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
                                            .background(if (isUnlocked) Color(0x2200F2FE) else Color.White.copy(alpha = 0.05f))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Clue #${idx + 1}: ${if (isUnlocked) clue else "••••••••••••"}",
                                            color = if (isUnlocked) TextPrimary else TextMuted,
                                            fontSize = 14.sp,
                                            fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal
                                        )

                                        if (!isUnlocked) {
                                            IconButton(
                                                onClick = {
                                                    haptics.performTick(composeHaptics)
                                                    clueStep = idx + 1
                                                }
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!timerRunning) timerRunning = true else timerRunning = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (timerRunning) Color(0xFFFFB300) else Color(0xFF00F2FE)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (timerRunning) "PAUSE TIMER ⏱️" else "START TIMER ▶",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = {
                            gameFinished = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("SUBMIT GUESS 🎯", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                // Game Finished & Scoring Result
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DID FRIENDS GUESS CORRECTLY?",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Secret Profession: ${currentCareer.title}",
                        color = Color(0xFF00F2FE),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                haptics.performPop()
                                groupGuessedRight = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(130.dp, 60.dp)
                        ) {
                            Text("YES! 🎉", color = Color.Black, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                haptics.performHeavyBurst()
                                groupGuessedRight = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(130.dp, 60.dp)
                        ) {
                            Text("NO 😅", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }

                    groupGuessedRight?.let { right ->
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (right) "Awesome! 1 Point awarded to Guessers!" else "Tricky! 1 Point awarded to Active Player!",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Footer Button
            if (isRevealed) {
                Button(
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("NEXT PLAYER TURN ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
