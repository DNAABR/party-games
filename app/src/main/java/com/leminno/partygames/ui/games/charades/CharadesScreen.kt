package com.leminno.partygames.ui.games.charades

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay

enum class CharadesCategory(val displayName: String, val icon: String, val words: List<String>) {
    MOVIES("Blockbuster Movies", "🎬", listOf("Titanic", "Avatar", "Jurassic Park", "Spider-Man", "The Matrix", "Inception", "The Lion King", "Harry Potter", "Star Wars", "Frozen", "The Avengers", "Jaws")),
    ANIMALS("Wild Animals", "🦁", listOf("Gorilla", "Flamingo", "Cheetah", "Penguin", "Kangaroo", "Octopus", "Sloth", "Chameleon", "Peacock", "Dolphin", "Grizzly Bear", "Elephant")),
    ACTIONS("Absurd Actions", "🕺", listOf("Baking a Cake", "Walking on Ice", "Flying a Kite", "Milking a Cow", "Riding a Unicycle", "Defusing a Bomb", "Conducting an Orchestra", "Sumo Wrestling", "Surfing a Big Wave")),
    POP_CULTURE("Pop Culture", "⭐", listOf("Beyoncé", "TikTok Dance", "Taylor Swift", "Cristiano Ronaldo", "Elon Musk", "Super Mario", "Barbie", "Wednesday Addams", "Iron Man"))
}

data class ScoredWord(val word: String, val isCorrect: Boolean)

@Composable
fun CharadesScreen(
    timerSec: Int = 60,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var selectedCategory by remember { mutableStateOf(CharadesCategory.MOVIES) }
    var gameStarted by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(timerSec) }

    var wordList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentWordIndex by remember { mutableIntStateOf(0) }
    var scoredWords by remember { mutableStateOf<List<ScoredWord>>(emptyList()) }

    var flashState by remember { mutableStateOf<Color?>(null) }

    // Tilt sensor logic
    DisposableEffect(gameStarted, gameOver) {
        if (!gameStarted || gameOver) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            return@DisposableEffect onDispose {}
        }

        var lastTriggerTime = System.currentTimeMillis()

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || System.currentTimeMillis() - lastTriggerTime < 1000) return

                val y = event.values[1]
                val z = event.values[2]

                // Tilt down = Correct
                if (z < -6.5f || y > 7.5f) {
                    lastTriggerTime = System.currentTimeMillis()
                    haptics.performPop()
                    flashState = SuccessContainer
                    val word = wordList.getOrNull(currentWordIndex) ?: ""
                    if (word.isNotEmpty()) {
                        scoredWords = scoredWords + ScoredWord(word, true)
                    }
                    if (currentWordIndex + 1 < wordList.size) {
                        currentWordIndex++
                    } else {
                        gameOver = true
                    }
                }
                // Tilt up = Skip
                else if (z > 6.5f || y < -7.5f) {
                    lastTriggerTime = System.currentTimeMillis()
                    haptics.performPop()
                    flashState = AlertContainer
                    val word = wordList.getOrNull(currentWordIndex) ?: ""
                    if (word.isNotEmpty()) {
                        scoredWords = scoredWords + ScoredWord(word, false)
                    }
                    if (currentWordIndex + 1 < wordList.size) {
                        currentWordIndex++
                    } else {
                        gameOver = true
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Flash reset effect
    LaunchedEffect(flashState) {
        if (flashState != null) {
            delay(350)
            flashState = null
        }
    }

    // Timer countdown
    LaunchedEffect(gameStarted, gameOver) {
        if (gameStarted && !gameOver) {
            remainingSeconds = timerSec
            while (remainingSeconds > 0 && !gameOver) {
                delay(1000)
                remainingSeconds--
            }
            if (remainingSeconds <= 0) {
                haptics.performHeavyBurst()
                gameOver = true
            }
        }
    }

    GameScaffold(
        title = "Charades",
        titleColor = TextPrimary,
        gameId = "charades",
        onExitGame = onExitGame
    ) {
        if (!gameStarted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Choose Your Deck",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Place phone on forehead facing your friends!",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CharadesCategory.entries.forEach { category ->
                            val isSel = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .subtleCardShadow(elevation = if (isSel) 3.dp else 1.dp, shape = RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isSel) BrandPrimaryContainer else SurfaceLight)
                                    .border(
                                        1.5.dp,
                                        if (isSel) BrandPrimary.copy(alpha = 0.5f) else BorderSubtle,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable {
                                        haptics.performTick(composeHaptics)
                                        selectedCategory = category
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isSel) SurfaceLight else SurfaceSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = category.icon, fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = category.displayName,
                                            color = TextPrimary,
                                            fontFamily = ModernSansFont,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${category.words.size} Words",
                                            color = TextSecondary,
                                            fontFamily = ModernSansFont,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                PrimaryPartyButton(
                    text = "Start Game",
                    accentColor = BrandPrimary,
                    onClick = {
                        haptics.performHeavyBurst()
                        wordList = selectedCategory.words.shuffled()
                        currentWordIndex = 0
                        scoredWords = emptyList()
                        gameStarted = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if (!gameOver) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header (Timer & Score Count)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (remainingSeconds <= 10) AlertContainer else SurfaceSubtle)
                            .border(1.dp, if (remainingSeconds <= 10) AlertRed.copy(alpha = 0.3f) else BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⏱️ ${remainingSeconds}s",
                            color = if (remainingSeconds <= 10) AlertRed else TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SuccessContainer)
                            .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Score: ${scoredWords.count { it.isCorrect }}",
                            color = SuccessGreen,
                            fontFamily = ModernSansFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = { gameOver = true }) {
                        Text("End", color = TextSecondary, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold)
                    }
                }

                // Main Secret Word Prompt Card
                val cardBg = flashState ?: SurfaceLight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .subtleCardShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = wordList.getOrNull(currentWordIndex) ?: "Finished!",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 42.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSubtle)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Tilt down = Correct 🟢 | Tilt up = Skip 🟡",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Action Buttons Fallback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Button(
                        onClick = {
                            haptics.performPop()
                            flashState = AlertContainer
                            val word = wordList.getOrNull(currentWordIndex) ?: ""
                            if (word.isNotEmpty()) scoredWords = scoredWords + ScoredWord(word, false)
                            if (currentWordIndex + 1 < wordList.size) currentWordIndex++ else gameOver = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertContainer),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Skip ⏩", color = AlertRed, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            flashState = SuccessContainer
                            val word = wordList.getOrNull(currentWordIndex) ?: ""
                            if (word.isNotEmpty()) scoredWords = scoredWords + ScoredWord(word, true)
                            if (currentWordIndex + 1 < wordList.size) currentWordIndex++ else gameOver = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessContainer),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Correct ✓", color = SuccessGreen, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "Score: ${scoredWords.count { it.isCorrect }} / ${scoredWords.size}",
                subtitle = "Great Charades Acting!",
                onPlayAgain = {
                    gameStarted = false
                    gameOver = false
                },
                onBackToHub = onExitGame
            )
        }
    }
}

