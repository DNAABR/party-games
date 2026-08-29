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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    playerCount: Int = 4,
    timerSec: Int = 60,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    val players = remember(playerCount) {
        com.leminno.partygames.data.repository.UserPreferencesRepository.getActiveRoster(playerCount)
    }

    var actorIndex by remember { mutableIntStateOf(0) }
    val currentActorName = players.getOrElse(actorIndex % players.size) { "Player 1" }
    var showScoreboard by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf(CharadesCategory.MOVIES) }
    var gameStarted by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(timerSec) }

    var wordList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentWordIndex by remember { mutableIntStateOf(0) }
    var scoredWords by remember { mutableStateOf<List<ScoredWord>>(emptyList()) }

    var flashState by remember { mutableStateOf<Color?>(null) } // Green for correct, Amber for skip

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

                val y = event.values[1] // Y-axis tilt
                val z = event.values[2] // Z-axis tilt

                // Tilt down (screen facing ground) = Correct
                if (z < -6.5f || y > 7.5f) {
                    lastTriggerTime = System.currentTimeMillis()
                    haptics.performPop()
                    flashState = Color(0xFF00E676)
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
                // Tilt up (screen facing ceiling) = Skip
                else if (z > 6.5f || y < -7.5f) {
                    lastTriggerTime = System.currentTimeMillis()
                    haptics.performPop()
                    flashState = Color(0xFFFFB300)
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
            delay(400)
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
        title = "CHARADES 🎭",
        titleColor = Color(0xFFFFD166),
        gameId = "charades",
        onExitGame = onExitGame
    ) {
        if (!gameStarted) {
            // Category Selection Pre-Game View
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    com.leminno.partygames.ui.components.InGamePlayerHeader(
                        currentPlayerName = currentActorName,
                        playerIndex = actorIndex % players.size,
                        totalPlayers = players.size,
                        onOpenScoreboard = { showScoreboard = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CHOOSE YOUR DECK",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pass phone to $currentActorName to place on forehead!",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CharadesCategory.entries.forEach { category ->
                            val isSel = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSel) Color(0x33FFD166) else SurfaceGlassDark)
                                    .border(1.5.dp, if (isSel) Color(0xFFFFD166) else BorderGlassDefault, RoundedCornerShape(16.dp))
                                    .clickable {
                                        haptics.performTick(composeHaptics)
                                        selectedCategory = category
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = category.icon, fontSize = 26.sp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = category.displayName,
                                            color = if (isSel) Color(0xFFFFD166) else TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${category.words.size} Words",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        haptics.performHeavyBurst()
                        wordList = selectedCategory.words.shuffled()
                        currentWordIndex = 0
                        scoredWords = emptyList()
                        gameStarted = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "START $currentActorName'S TURN ▶",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else if (!gameOver) {
            // Live Forehead Gameplay Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header (Timer & Score Count)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currentActorName: ${scoredWords.count { it.isCorrect }} pts",
                        color = Color(0xFF00E676),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (remainingSeconds <= 10) Color(0x33FF0055) else Color(0x33FFD166))
                            .border(1.dp, if (remainingSeconds <= 10) Color(0xFFFF0055) else Color(0xFFFFD166), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⏱ ${remainingSeconds}s",
                            color = if (remainingSeconds <= 10) Color(0xFFFF0055) else Color(0xFFFFD166),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Central Active Word Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(flashState ?: SurfaceGlassDark)
                        .border(2.dp, Color(0xFFFFD166), RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val activeWord = wordList.getOrNull(currentWordIndex) ?: "Ready!"
                    Text(
                        text = activeWord,
                        color = if (flashState != null) Color.Black else Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // Manual Touch Action Buttons Fallback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            haptics.performPop()
                            flashState = Color(0xFFFFB300)
                            val word = wordList.getOrNull(currentWordIndex) ?: ""
                            if (word.isNotEmpty()) scoredWords = scoredWords + ScoredWord(word, false)
                            if (currentWordIndex + 1 < wordList.size) currentWordIndex++ else gameOver = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("SKIP 🟡", color = Color.Black, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            flashState = Color(0xFF00E676)
                            val word = wordList.getOrNull(currentWordIndex) ?: ""
                            if (word.isNotEmpty()) scoredWords = scoredWords + ScoredWord(word, true)
                            if (currentWordIndex + 1 < wordList.size) currentWordIndex++ else gameOver = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("CORRECT 🟢", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            val correctCount = scoredWords.count { it.isCorrect }
            LaunchedEffect(Unit) {
                com.leminno.partygames.data.repository.UserPreferencesRepository.updatePlayerScore(currentActorName, correctCount)
            }

            VictoryCeremonyOverlay(
                winnerTitle = "$currentActorName SCORED $correctCount / ${scoredWords.size}!",
                subtitle = "Great Charades Acting! +$correctCount PTS awarded",
                onPlayAgain = {
                    actorIndex++
                    gameStarted = false
                    gameOver = false
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
