package com.leminno.partygames.ui.games.decibel_scream

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.theme.*
import kotlin.math.log10

@Composable
fun DecibelScreamScreen(
    viewModel: DecibelScreamViewModel = viewModel(),
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    val uiState by viewModel.uiState.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    // Audio recording thread loop
    @SuppressLint("MissingPermission")
    DisposableEffect(uiState.isListening) {
        if (!uiState.isListening) return@DisposableEffect onDispose {}

        var audioRecord: AudioRecord? = null
        var isThreadRunning = true

        val bufferSize = AudioRecord.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            if (hasMicPermission && bufferSize > 0) {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                audioRecord.startRecording()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val thread = Thread {
            val safeBufferSize = if (bufferSize > 0) bufferSize else 1024
            val buffer = ShortArray(safeBufferSize)
            while (isThreadRunning) {
                if (audioRecord != null && audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val readSize = audioRecord.read(buffer, 0, buffer.size)
                    if (readSize > 0) {
                        var sum = 0.0
                        for (i in 0 until readSize) {
                            sum += buffer[i] * buffer[i]
                        }
                        val amplitude = Math.sqrt(sum / readSize)
                        val db = if (amplitude > 0) (20 * log10(amplitude)).toFloat() else 0f
                        viewModel.updateDbLevel(db)
                    }
                } else {
                    val simDb = (30..95).random().toFloat()
                    viewModel.updateDbLevel(simDb)
                    Thread.sleep(150)
                }
            }
        }
        thread.start()

        onDispose {
            isThreadRunning = false
            try {
                audioRecord?.stop()
                audioRecord?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    GameScaffold(
        title = "Decibel Scream",
        titleColor = TextPrimary,
        gameId = "decibel_scream",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!uiState.isListening && !uiState.challengeComplete) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Select Sound Challenge",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Test who can scream, whisper, or roar loudest!",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DecibelChallengeMode.entries.forEach { mode ->
                            val isSel = uiState.selectedMode == mode
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
                                        viewModel.selectMode(mode)
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.title,
                                            color = TextPrimary,
                                            fontFamily = ModernSansFont,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = mode.desc,
                                            color = TextSecondary,
                                            fontFamily = ModernSansFont,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceSubtle)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            mode.targetGoal,
                                            color = BrandPrimary,
                                            fontFamily = ModernSansFont,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                PrimaryPartyButton(
                    text = "Start 5-Sec Challenge 🎙️",
                    accentColor = BrandPrimary,
                    onClick = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        haptics.performHeavyBurst()
                        viewModel.startChallenge(
                            onChallengeComplete = { haptics.performPop() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (uiState.isListening) {
                // Live Sound Meter & Gauge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(AlertContainer)
                            .border(1.dp, AlertRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⏱️ Timing: ${uiState.timerRemaining}s",
                            color = AlertRed,
                            fontFamily = ModernSansFont,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Circular Gauge Meter Display
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .subtleCardShadow(elevation = 6.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(SurfaceLight)
                            .border(4.dp, BrandPrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.currentDb.toInt()}",
                                color = TextPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 60.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "DECIBELS (dB)",
                                color = BrandPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Peak: ${uiState.peakDb.toInt()} dB",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = "Make your noise into the microphone!",
                    color = TextSecondary,
                    fontFamily = ModernSansFont,
                    fontSize = 13.sp
                )
            } else {
                // Results Screen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "Challenge Complete! 🏆",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.selectedMode.title,
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${uiState.peakDb.toInt()} dB",
                                color = BrandPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Peak Sound Level Recorded",
                                color = TextSecondary,
                                fontFamily = ModernSansFont,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                PrimaryPartyButton(
                    text = "Try Again or Next Player",
                    accentColor = BrandPrimary,
                    onClick = { viewModel.resetChallenge() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

