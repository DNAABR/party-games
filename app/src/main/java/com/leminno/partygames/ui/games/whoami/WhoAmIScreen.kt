package com.leminno.partygames.ui.games.whoami

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    var selectedMode by remember { mutableStateOf<String?>(null) }
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

    val cardBgColor = when (uiState.cardFeedbackState) {
        "CORRECT" -> PixelEmeraldGreen
        "SKIP" -> PixelAlertRed
        else -> PixelCrtDarkCanvas
    }

    GameScaffold(
        title = "WHO AM I?",
        titleColor = PixelMagentaHot,
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
                        color = PixelCrtCyan,
                        fontFamily = PressStart2PFont,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Forehead Pass Mode Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(
                                brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                            )
                            .clickable { selectedMode = "LOCAL" }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.5.dp, PixelOutlineBlack)
                                    .background(PixelMagentaHot),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PixelIcons.ArcadeJoystick,
                                    contentDescription = null,
                                    tint = PixelOutlineBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "FOREHEAD PASS",
                                    color = PixelCrtCyan,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Hold phone on forehead with tilt detection",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Remote Play Mode Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(
                                brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                            )
                            .clickable {
                                selectedMode = "REMOTE"
                                showRemoteSheet = true
                            }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.5.dp, PixelOutlineBlack)
                                    .background(PixelCrtCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PixelIcons.Users,
                                    contentDescription = null,
                                    tint = PixelOutlineBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "REMOTE PLAY",
                                    color = PixelMagentaHot,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Multi-device room code sync",
                                    color = TextSecondary,
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
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(PixelVioletElevated)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PixelIcons.Clock,
                                contentDescription = null,
                                tint = if (uiState.timeRemaining <= 10) PixelAlertRed else PixelCrtCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${uiState.timeRemaining}S",
                                color = if (uiState.timeRemaining <= 10) PixelAlertRed else PixelCrtCyan,
                                fontFamily = PressStart2PFont,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(PixelVioletElevated)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PixelIcons.Trophy,
                                contentDescription = null,
                                tint = PixelAmberGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SCORE:${uiState.score}",
                                color = PixelAmberGold,
                                fontFamily = PressStart2PFont,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // CRT Forehead Screen Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 14.dp)
                        .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                        .background(cardBgColor)
                        .crtScanlines()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val currentWord = uiState.wordList.getOrNull(uiState.currentIndex)
                        Text(
                            text = (currentWord ?: "CARD").uppercase(),
                            color = if (uiState.cardFeedbackState == "NORMAL") PixelCrtCyan else PixelOutlineBlack,
                            fontFamily = PressStart2PFont,
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Manual Pixel Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryPartyButton(
                        text = "SKIP",
                        icon = PixelIcons.Close,
                        accentColor = PixelAlertRed,
                        onClick = { viewModel.onManualSkip() },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryPartyButton(
                        text = "CORRECT",
                        icon = PixelIcons.Zap,
                        accentColor = PixelEmeraldGreen,
                        onClick = { viewModel.onManualGotIt() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "FINAL SCORE: ${uiState.score}",
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
                gameName = "Who Am I?",
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
