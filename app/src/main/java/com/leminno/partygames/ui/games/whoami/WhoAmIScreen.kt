package com.leminno.partygames.ui.games.whoami

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Phonelink
import androidx.compose.material.icons.rounded.Public
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
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*

@Composable
fun WhoAmIScreen(
    timerSec: Int = 60,
    viewModel: WhoAmIViewModel = viewModel(),
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var selectedMode by remember { mutableStateOf<String?>(null) } // null, LOCAL, REMOTE
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }

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
            else -> SurfaceGlassDark
        },
        animationSpec = tween(300),
        label = "bgColor"
    )

    GameScaffold(
        title = "WHO AM I?",
        titleColor = AccentViolet,
        gameId = "who_am_i",
        onExitGame = onExitGame
    ) {
        if (selectedMode == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SELECT PLAY MODE",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, AccentViolet, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedMode = "LOCAL"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AccentViolet.copy(alpha = 0.2f))
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Phonelink,
                                    contentDescription = null,
                                    tint = AccentViolet,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Forehead Pass (Same Phone)",
                                    color = AccentViolet,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Hold phone on forehead with tilt detection",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, AccentCyan, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedMode = "REMOTE"
                                showRemoteSheet = true
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AccentCyan.copy(alpha = 0.2f))
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Public,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Remote Play (Multi-Device)",
                                    color = AccentCyan,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Card shown on remote screens while guesser asks questions",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        } else if (!uiState.isGameOver) {
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
                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⏱️ ${uiState.timeRemaining}s",
                            color = if (uiState.timeRemaining <= 10) AlertRed else TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark)
                            .border(1.dp, AccentViolet.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SCORE: ${uiState.score}",
                            color = AccentViolet,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Forehead Card Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(backgroundColor)
                        .border(1.5.dp, AccentViolet.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val currentWord = uiState.wordList.getOrNull(uiState.currentIndex)
                        Text(
                            text = currentWord ?: "CARD",
                            color = TextPrimary,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Manual Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrimaryPartyButton(
                        text = "SKIP",
                        icon = Icons.Rounded.Close,
                        accentColor = AlertRed,
                        onClick = { viewModel.onManualSkip() },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryPartyButton(
                        text = "CORRECT",
                        icon = Icons.Rounded.Check,
                        accentColor = SuccessGreen,
                        onClick = { viewModel.onManualGotIt() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "FINAL SCORE: ${uiState.score} 🎉",
                subtitle = "Who Am I Champions!",
                onPlayAgain = {
                    viewModel.initGame(timerSec)
                    selectedMode = null
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "who_am_i",
                gameName = "Who Am I? 🎭",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) selectedMode = null
                },
                onRoomJoined = { code, _, _ ->
                    roomCode = code
                    selectedMode = "REMOTE"
                    showRemoteSheet = false
                }
            )
        }
    }
}
