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
                    // Fallback simulated wave generator when mic permission is withheld or bufferSize invalid
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
        title = "DECIBEL SCREAM 🎙️",
        titleColor = Color(0xFF00E676),
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!uiState.isListening && !uiState.challengeComplete) {
                // Mode Selector Screen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SELECT SOUND CHALLENGE",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DecibelChallengeMode.entries.forEach { mode ->
                            val isSel = uiState.selectedMode == mode
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSel) Color(0x3300E676) else SurfaceGlassDark)
                                    .border(1.5.dp, if (isSel) Color(0xFF00E676) else BorderGlassDefault, RoundedCornerShape(16.dp))
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
                                    Column {
                                        Text(
                                            text = mode.title,
                                            color = if (isSel) Color(0xFF00E676) else TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = mode.desc,
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(mode.targetGoal, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        haptics.performHeavyBurst()
                        viewModel.startChallenge(
                            onChallengeComplete = { haptics.performPop() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("START 5-SEC CHALLENGE 🎙️", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else if (uiState.isListening) {
                // Live Sound Meter & Gauge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TIMING: ${uiState.timerRemaining}s",
                        color = Color(0xFFFF0055),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Circular Gauge Meter Display
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(SurfaceGlassDark)
                            .border(4.dp, Color(0xFF00E676), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.currentDb.toInt()}",
                                color = Color.White,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "DECIBELS (dB)",
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PEAK: ${uiState.peakDb.toInt()} dB",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Make your noise into the microphone!",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Results Screen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CHALLENGE COMPLETE! 🏆",
                        color = Color(0xFF00E676),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.selectedMode.title,
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${uiState.peakDb.toInt()} dB",
                                color = Color(0xFF00E676),
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "Peak Sound Level Recorded",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.resetChallenge()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("TRY AGAIN OR NEXT PLAYER ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
