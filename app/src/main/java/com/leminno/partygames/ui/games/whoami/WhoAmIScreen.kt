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
import androidx.compose.foundation.shape.CircleShape
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

    val cardBgColor by animateColorAsState(
        targetValue = when (uiState.cardFeedbackState) {
            "CORRECT" -> SuccessContainer
            "SKIP" -> AlertContainer
            else -> SurfaceLight
        },
        animationSpec = tween(150),
        label = "cardBgColor"
    )

    val cardTextColor = when (uiState.cardFeedbackState) {
        "CORRECT" -> SuccessGreen
        "SKIP" -> AlertRed
        else -> TextPrimary
    }

    GameScaffold(
        title = "Who Am I?",
        titleColor = TextPrimary,
        gameId = "who_am_i",
        onExitGame = onExitGame
    ) {
        if (selectedMode == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Select Play Mode",
                    color = TextPrimary,
                    fontFamily = ModernSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose how your group will play this round",
                    color = TextSecondary,
                    fontFamily = ModernSansFont,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Forehead Pass Mode Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable { selectedMode = "LOCAL" }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(TriviaContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Forehead Pass",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Hold phone on forehead with tilt detection",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Remote Play Mode Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            selectedMode = "REMOTE"
                            showRemoteSheet = true
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ActionContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🌐", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Remote Multiplayer",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Sync across multiple devices with room code",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else if (!uiState.isGameOver) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (uiState.timeRemaining <= 10) AlertContainer else SurfaceLight)
                            .border(1.dp, if (uiState.timeRemaining <= 10) AlertRed.copy(alpha = 0.3f) else BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PixelIcons.Clock,
                                contentDescription = null,
                                tint = if (uiState.timeRemaining <= 10) AlertRed else BrandPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${uiState.timeRemaining}s",
                                color = if (uiState.timeRemaining <= 10) AlertRed else TextPrimary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarningContainer)
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PixelIcons.Trophy,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Score: ${uiState.score}",
                                color = Color(0xFF92400E),
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Forehead Screen Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .subtleCardShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBgColor)
                        .border(
                            1.dp,
                            when (uiState.cardFeedbackState) {
                                "CORRECT" -> SuccessGreen.copy(alpha = 0.4f)
                                "SKIP" -> AlertRed.copy(alpha = 0.4f)
                                else -> BorderSubtle
                            },
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val currentWord = uiState.wordList.getOrNull(uiState.currentIndex)
                        Text(
                            text = currentWord ?: "Ready?",
                            color = cardTextColor,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            lineHeight = 40.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Tilt down for Correct • Tilt up for Skip",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 12.sp
                        )
                    }
                }

                // Manual Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrimaryPartyButton(
                        text = "Skip",
                        icon = PixelIcons.Close,
                        accentColor = AlertRed,
                        onClick = { viewModel.onManualSkip() },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryPartyButton(
                        text = "Correct!",
                        icon = PixelIcons.Zap,
                        accentColor = SuccessGreen,
                        onClick = { viewModel.onManualGotIt() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "Final Score: ${uiState.score}",
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

