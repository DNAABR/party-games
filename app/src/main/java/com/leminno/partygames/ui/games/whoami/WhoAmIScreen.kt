package com.leminno.partygames.ui.games.whoami

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

@Composable
fun WhoAmIScreen(
    timerSec: Int = 60,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    val wordList = remember {
        listOf(
            "Harry Potter", "Spider-Man", "Albert Einstein", "Taylor Swift",
            "Mickey Mouse", "Batman", "Sherlock Holmes", "Barack Obama",
            "SpongeBob", "Elon Musk", "Cristiano Ronaldo", "Pikachu",
            "Leonardo DiCaprio", "Serena Williams", "Katy Perry", "Mario",
            "Beyoncé", "Darth Vader", "Dwayne Johnson", "Wonder Woman",
            "Michael Jordan", "Elsa", "Tom Cruise", "Oprah Winfrey",
            "Iron Man", "Lionel Messi", "Lady Gaga", "Shrek",
            "Drake", "Hermione Granger", "Will Smith", "Cleopatra",
            "Homer Simpson", "Naruto", "Rihanna", "Indiana Jones",
            "Gordon Ramsay", "Bugs Bunny", "Adele", "James Bond",
            "Goku", "Ed Sheeran", "Cinderella", "The Rock",
            "Ariana Grande", "Scooby-Doo", "Freddie Mercury", "Groot"
        ).shuffled()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var skips by remember { mutableIntStateOf(0) }
    var timeRemaining by remember { mutableIntStateOf(timerSec) }
    var isGameOver by remember { mutableStateOf(false) }

    var cardFeedbackState by remember { mutableStateOf<String?>(null) } // "CORRECT", "SKIP", null
    var isWaitingForUpright by remember { mutableStateOf(false) }

    // Accelerometer Sensor Listener
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || isGameOver) return
                val z = event.values[2] // Z-axis tilt when held on forehead

                if (!isWaitingForUpright) {
                    if (z > 7.5f) {
                        // Tilted DOWN = Correct
                        score++
                        cardFeedbackState = "CORRECT"
                        isWaitingForUpright = true
                        haptics.performPop()
                    } else if (z < -7.5f) {
                        // Tilted UP = Skip
                        skips++
                        cardFeedbackState = "SKIP"
                        isWaitingForUpright = true
                        haptics.performWarningThud()
                    }
                } else {
                    // Reset when returned to upright forehead position (~0 to +5)
                    if (z in 2.0f..6.5f) {
                        isWaitingForUpright = false
                        cardFeedbackState = null
                        if (currentIndex + 1 < wordList.size) {
                            currentIndex++
                        } else {
                            isGameOver = true
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Countdown Timer Loop
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (timeRemaining > 0) {
                delay(1000L)
                if (!isWaitingForUpright) {
                    timeRemaining--
                }
            }
            isGameOver = true
            haptics.performHeavyBurst()
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (cardFeedbackState) {
            "CORRECT" -> SuccessGreen.copy(alpha = 0.85f)
            "SKIP" -> AlertRed.copy(alpha = 0.85f)
            else -> BackgroundNavySlate
        },
        animationSpec = tween(300),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp)
    ) {
        if (!isGameOver) {
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

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⏱️ ${timeRemaining}s",
                            color = if (timeRemaining <= 10) AlertRed else TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x3300E676))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Score: $score",
                            color = SuccessGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Center Word Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceGlassDark)
                        .border(2.dp, Color(0x669D4EDD), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = wordList.getOrElse(currentIndex) { "Finished!" },
                            color = TextPrimary,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val feedbackText = when (cardFeedbackState) {
                            "CORRECT" -> "CORRECT! 🟢 (Return upright)"
                            "SKIP" -> "SKIPPED 🟡 (Return upright)"
                            else -> "Hold to forehead! Tilt DOWN = Pass, UP = Skip"
                        }

                        Text(
                            text = feedbackText,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Bottom Hint Controls (Manual fallbacks for tap clicks)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            skips++
                            cardFeedbackState = "SKIP"
                            haptics.performWarningThud()
                            if (currentIndex + 1 < wordList.size) currentIndex++ else isGameOver = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.3f))
                    ) {
                        Text("SKIP ⬆️", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            score++
                            cardFeedbackState = "CORRECT"
                            haptics.performPop()
                            if (currentIndex + 1 < wordList.size) currentIndex++ else isGameOver = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen.copy(alpha = 0.3f))
                    ) {
                        Text("GOT IT! ⬇️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Victory / Game Summary View
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉 TIME'S UP!", color = WinGold, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceGlassDark)
                        .border(1.dp, BorderGlassDefault, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FINAL SCORE", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$score", color = SuccessGreen, fontSize = 64.sp, fontWeight = FontWeight.Black)
                        Text("Skips: $skips", color = TextSecondary, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onExitGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
                ) {
                    Text("BACK TO ARCADE 🎮", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
