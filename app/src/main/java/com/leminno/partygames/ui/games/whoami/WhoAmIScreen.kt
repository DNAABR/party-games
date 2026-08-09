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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.theme.*

@Composable
fun WhoAmIScreen(
    timerSec: Int = 60,
    viewModel: WhoAmIViewModel = viewModel(),
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(timerSec) {
        viewModel.initGame(timerSec)
    }

    // Accelerometer Sensor Listener
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                viewModel.onSensorTilt(event.values[2])
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (uiState.cardFeedbackState) {
            "CORRECT" -> SuccessGreen.copy(alpha = 0.85f)
            "SKIP" -> AlertRed.copy(alpha = 0.85f)
            else -> BackgroundNavySlate
        },
        animationSpec = tween(300),
        label = "bgColor"
    )

    GameScaffold(
        title = "Who Am I?",
        titleColor = Color(0xFF9D4EDD),
        onExitGame = onExitGame
    ) {
        if (!uiState.isGameOver) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Score & Timer Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⏱️ ${uiState.timeRemaining}s",
                            color = if (uiState.timeRemaining <= 10) AlertRed else TextPrimary,
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
                            text = "Score: ${uiState.score}",
                            color = SuccessGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Center Word Display Card
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
                            text = uiState.wordList.getOrElse(uiState.currentIndex) { "Finished!" },
                            color = TextPrimary,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val feedbackText = when (uiState.cardFeedbackState) {
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

                // Bottom Hint Controls (Manual fallbacks)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            haptics.performWarningThud()
                            viewModel.onManualSkip()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.3f))
                    ) {
                        Text("SKIP ⬆️", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            viewModel.onManualGotIt()
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
                        Text("${uiState.score}", color = SuccessGreen, fontSize = 64.sp, fontWeight = FontWeight.Black)
                        Text("Skips: ${uiState.skips}", color = TextSecondary, fontSize = 14.sp)
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
