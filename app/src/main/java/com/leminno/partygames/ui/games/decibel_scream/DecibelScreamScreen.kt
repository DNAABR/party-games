package com.leminno.partygames.ui.games.decibel_scream

import android.annotation.SuppressLint
import android.Manifest
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
import androidx.compose.runtime.snapshots.Snapshot
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
import androidx.core.content.ContextCompat
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.log10

enum class DecibelChallengeMode(val title: String, val desc: String, val targetGoal: String) {
    WHISPER("Quiet Whisper", "Stay under 20dB while whispering your secret!", "< 20 dB"),
    MAX_SCREAM("Max Volume Spike", "Scream as loud as possible in 3 seconds!", "Peak dB"),
    STEADY_HUM("Steady Hum", "Hold a consistent hum tone for 5 seconds!", "Consistent dB")
}

@Composable
fun DecibelScreamScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var selectedMode by remember { mutableStateOf(DecibelChallengeMode.MAX_SCREAM) }
    var isListening by remember { mutableStateOf(false) }
    var currentDb by remember { mutableFloatStateOf(0f) }
    var peakDb by remember { mutableFloatStateOf(0f) }
    var timerRemaining by remember { mutableIntStateOf(5) }
    var challengeComplete by remember { mutableStateOf(false) }

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
    DisposableEffect(isListening) {
        if (!isListening) return@DisposableEffect onDispose {}

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
                        val calculatedDb = db.coerceIn(0f, 120f)
                        Snapshot.withMutableSnapshot {
                            currentDb = calculatedDb
                            if (calculatedDb > peakDb) peakDb = calculatedDb
                        }
                    }
                } else {
                    // Fallback simulated wave generator when mic permission is withheld or bufferSize invalid
                    val simDb = (30..95).random().toFloat()
                    Snapshot.withMutableSnapshot {
                        currentDb = simDb
                        if (simDb > peakDb) peakDb = simDb
                    }
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

    // Challenge Timer Loop
    LaunchedEffect(isListening) {
        if (isListening) {
            timerRemaining = 5
            while (timerRemaining > 0 && isListening) {
                delay(1000)
                timerRemaining--
            }
            if (timerRemaining <= 0) {
                isListening = false
                challengeComplete = true
                haptics.performPop()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07101E))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitGame) {
                    Text("✕", color = TextSecondary, fontSize = 22.sp)
                }
                Text(
                    text = "DECIBEL SCREAM 🎙️",
                    color = Color(0xFF00E676),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            if (!isListening && !challengeComplete) {
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
                            val isSel = selectedMode == mode
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSel) Color(0x3300E676) else SurfaceGlassDark)
                                    .border(1.5.dp, if (isSel) Color(0xFF00E676) else BorderGlassDefault, RoundedCornerShape(16.dp))
                                    .clickable {
                                        haptics.performTick(composeHaptics)
                                        selectedMode = mode
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
                        peakDb = 0f
                        currentDb = 0f
                        isListening = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("START 5-SEC CHALLENGE 🎙️", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else if (isListening) {
                // Live Sound Meter & Gauge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TIMING: ${timerRemaining}s",
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
                                text = "${currentDb.toInt()}",
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
                                text = "PEAK: ${peakDb.toInt()} dB",
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
                                text = selectedMode.title,
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${peakDb.toInt()} dB",
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
                        challengeComplete = false
                        isListening = false
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

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
