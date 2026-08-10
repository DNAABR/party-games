package com.leminno.partygames.ui.games.wavelength

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, PSYCHIC_PEEK, TEAM_DIAL, REVEAL
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var currentSpectrum by remember { mutableStateOf(spectrumPairs.random()) }
    var psychicClue by remember { mutableStateOf("") }
    var targetAngle by remember { mutableFloatStateOf((30..150).random().toFloat()) }
    var dialAngle by remember { mutableFloatStateOf(90f) }
    var scoreEarned by remember { mutableIntStateOf(0) }

    // Observe Remote Wavelength State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteTarget = (room.gameState["targetAngle"] as? Number)?.toFloat()
                    val remoteDial = (room.gameState["dialAngle"] as? Number)?.toFloat()
                    val remoteClue = room.gameState["clue"] as? String
                    val remotePhase = room.gameState["phase"] as? String

                    if (remoteTarget != null) targetAngle = remoteTarget
                    if (remoteDial != null) dialAngle = remoteDial
                    if (!remoteClue.isNullOrBlank()) psychicClue = remoteClue
                    if (remotePhase != null && remotePhase != gamePhase) {
                        gamePhase = remotePhase
                    }
                }
            }
        }
    }

    fun syncRemote(target: Float, dial: Float, clue: String, phase: String) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("targetAngle" to target, "dialAngle" to dial, "clue" to clue, "phase" to phase)
                )
            }
        }
    }

    fun calculateScore(): Int {
        val diff = abs(dialAngle - targetAngle)
        return when {
            diff <= 5f -> 4
            diff <= 12f -> 3
            diff <= 20f -> 2
            else -> 0
        }
    }

    GameScaffold(
        title = "Wavelength 🔮",
        titleColor = Color(0xFFFFD166),
        gameId = "wavelength",
        onExitGame = onExitGame
    ) {
        if (gamePhase == "MODE_SELECT") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SELECT PLAY MODE", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFFFFD166), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = false
                                gamePhase = "PSYCHIC_PEEK"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFFFFD166), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Psychic peeks target angle on single phone", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = true
                                showRemoteSheet = true
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Remote Play (Multi-Device)", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Psychic sees secret target angle; team turns dial remotely", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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
                                    val touch = change.position
                                    val rad = atan2(center.y - touch.y, touch.x - center.x)
                                    var deg = Math.toDegrees(rad.toDouble()).toFloat()
                                    if (deg < 0) deg += 360f
                                    val clamped = deg.coerceIn(20f, 160f)
                                    dialAngle = clamped
                                    syncRemote(targetAngle, dialAngle, psychicClue, gamePhase)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Outer Arc Track
                        drawArc(
                            color = SurfaceGlassDark,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(20f, 20f),
                            size = Size(w - 40f, h - 40f),
                            style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Target Zone Arc (Visible during PSYCHIC_PEEK or REVEAL)
                        if (gamePhase == "PSYCHIC_PEEK" || gamePhase == "REVEAL") {
                            drawArc(
                                color = Color(0xFFFFD166),
                                startAngle = 180f + (180f - targetAngle - 10f),
                                sweepAngle = 20f,
                                useCenter = false,
                                topLeft = Offset(20f, 20f),
                                size = Size(w - 40f, h - 40f),
                                style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Team Needle Dial Pointer Line
                        val needleRad = Math.toRadians((180.0 - dialAngle)).toFloat()
                        val needleLen = (w / 2f) - 30.dp.toPx()
                        val endX = (w / 2f) + needleLen * kotlin.math.cos(needleRad)
                        val endY = (h / 2f) - needleLen * kotlin.math.sin(needleRad)

                        drawLine(
                            color = Color(0xFFFF007F),
                            start = Offset(w / 2f, h / 2f),
                            end = Offset(endX, endY),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                if (gamePhase == "PSYCHIC_PEEK") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PSYCHIC ROLE: GIVE A CLUE", color = Color(0xFFFFD166), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = psychicClue,
                            onValueChange = { psychicClue = it },
                            label = { Text("Enter your verbal spectrum clue") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD166), unfocusedBorderColor = BorderGlassDefault)
                        )
                    }

                    Button(
                        onClick = {
                            if (psychicClue.isNotBlank()) {
                                haptics.performPop()
                                gamePhase = "TEAM_DIAL"
                                syncRemote(targetAngle, dialAngle, psychicClue, "TEAM_DIAL")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("LOCK CLUE & HAND DIAL TO TEAM ▶", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "TEAM_DIAL") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PSYCHIC CLUE: \"$psychicClue\"", color = Color(0xFF00F2FE), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Drag needle along spectrum arc to target spot!", color = TextMuted, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            haptics.performHeavyBurst()
                            scoreEarned = calculateScore()
                            gamePhase = "REVEAL"
                            syncRemote(targetAngle, dialAngle, psychicClue, "REVEAL")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("OPEN SHUTTER & REVEAL SCORE 🎯", color = Color.White, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "REVEAL") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (scoreEarned > 0) "+$scoreEarned POINTS! 🎯" else "MISSED TARGET! 0 PTS",
                            color = if (scoreEarned > 0) Color(0xFF00E676) else Color(0xFFFF0055),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = {
                            currentSpectrum = spectrumPairs.random()
                            psychicClue = ""
                            targetAngle = (30..150).random().toFloat()
                            dialAngle = 90f
                            gamePhase = "MODE_SELECT"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("NEXT SPECTRUM ROUND 🔮", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "wavelength",
                gameName = "Wavelength 🔮",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "PSYCHIC_PEEK"
                    showRemoteSheet = false
                    if (hostFlag) {
                        syncRemote(targetAngle, dialAngle, psychicClue, "PSYCHIC_PEEK")
                    }
                }
            )
        }
    }
}
