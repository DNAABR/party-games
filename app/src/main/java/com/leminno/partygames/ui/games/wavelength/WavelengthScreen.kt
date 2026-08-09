package com.leminno.partygames.ui.games.wavelength

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*
import kotlin.math.abs
import kotlin.math.atan2

val spectrumPairs = listOf(
    Pair("Hot 🥵", "Cold 🥶"),
    Pair("Cheap 💵", "Expensive 💎"),
    Pair("Useless 🗑️", "Super Useful 🛠️"),
    Pair("Cute 🐰", "Terrifying 🦖"),
    Pair("Worst Food 🤢", "Best Delicacy 😋"),
    Pair("Guilty Pleasure 🙈", "Proud Achievement 🏆")
)

@Composable
fun WavelengthScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    var currentSpectrum by remember { mutableStateOf(spectrumPairs.random()) }
    var psychicClue by remember { mutableStateOf("") }
    var targetAngle by remember { mutableFloatStateOf((30..150).random().toFloat()) }

    var gamePhase by remember { mutableStateOf("PSYCHIC_PEEK") } // PSYCHIC_PEEK, TEAM_DIAL, REVEAL
    var dialAngle by remember { mutableFloatStateOf(90f) }
    var scoreEarned by remember { mutableIntStateOf(0) }

    fun calculateScore(): Int {
        val diff = abs(dialAngle - targetAngle)
        return when {
            diff <= 5f -> 4 // Bulls-eye
            diff <= 12f -> 3
            diff <= 20f -> 2
            else -> 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F121C))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
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
                    text = "WAVELENGTH 🔮",
                    color = Color(0xFFFFD166),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Spectrum Pair Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceGlassDark)
                    .border(1.dp, BorderGlassDefault, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(currentSpectrum.first, color = Color(0xFF00F2FE), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("◄ ── Spectrum ── ►", color = TextMuted, fontSize = 11.sp)
                    Text(currentSpectrum.second, color = Color(0xFFFF007F), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Dial Arc Wheel Canvas Widget
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .pointerInput(gamePhase) {
                        if (gamePhase == "TEAM_DIAL") {
                            detectDragGestures { change, _ ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchPos = change.position
                                val angleRad = atan2((center.y - touchPos.y).toDouble(), (touchPos.x - center.x).toDouble())
                                var angleDeg = Math.toDegrees(angleRad).toFloat()
                                if (angleDeg < 0) angleDeg += 360f
                                if (angleDeg in 0f..180f) {
                                    dialAngle = angleDeg
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Outer Arc Track
                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Target Slice (Only visible during Psychic Peek or Reveal)
                    if (gamePhase == "PSYCHIC_PEEK" || gamePhase == "REVEAL") {
                        drawArc(
                            color = Color(0xFFFFD166),
                            startAngle = 360f - targetAngle - 10f,
                            sweepAngle = 20f,
                            useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF00E676),
                            startAngle = 360f - targetAngle - 4f,
                            sweepAngle = 8f,
                            useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Guesser Dial Needle
                    if (gamePhase != "PSYCHIC_PEEK") {
                        val rad = Math.toRadians((360 - dialAngle).toDouble())
                        val needleLength = canvasWidth / 2f - 20.dp.toPx()
                        val endX = canvasWidth / 2f + (needleLength * Math.cos(rad)).toFloat()
                        val endY = canvasHeight / 2f + (needleLength * Math.sin(rad)).toFloat()

                        drawLine(
                            color = Color(0xFFFF007F),
                            start = Offset(canvasWidth / 2f, canvasHeight / 2f),
                            end = Offset(endX, endY),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            if (gamePhase == "PSYCHIC_PEEK") {
                // Psychic Clue Entry
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PSYCHIC SECRET VIEW 🔮",
                        color = Color(0xFFFFD166),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = psychicClue,
                        onValueChange = { psychicClue = it },
                        label = { Text("Give a one-phrase clue for this target location") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD166), unfocusedBorderColor = BorderGlassDefault)
                    )
                }

                Button(
                    onClick = {
                        if (psychicClue.isNotBlank()) {
                            haptics.performPop()
                            gamePhase = "TEAM_DIAL"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("CLOSE COVER & PASS TO TEAM ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else if (gamePhase == "TEAM_DIAL") {
                // Team Dialing Phase
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PSYCHIC CLUE:", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(psychicClue, color = Color(0xFFFFD166), fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Drag across dial arc wheel to set your guess!", color = TextSecondary, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        haptics.performHeavyBurst()
                        scoreEarned = calculateScore()
                        gamePhase = "REVEAL"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("OPEN TARGET COVER 🎯", color = Color.White, fontWeight = FontWeight.Black)
                }
            } else {
                // Reveal & Scoring Phase
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (scoreEarned > 0) "+$scoreEarned POINTS EARNED! 🎉" else "MISSED TARGET ZONE 😅",
                        color = if (scoreEarned > 0) Color(0xFF00E676) else Color(0xFFFF0055),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Dial offset: ${abs(dialAngle - targetAngle).toInt()}° from target center",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = {
                        currentSpectrum = spectrumPairs.random()
                        psychicClue = ""
                        targetAngle = (30..150).random().toFloat()
                        dialAngle = 90f
                        scoreEarned = 0
                        gamePhase = "PSYCHIC_PEEK"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("NEXT PSYCHIC TURN ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
