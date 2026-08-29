package com.leminno.partygames.ui.games.wavelength

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.leminno.partygames.ui.components.PrimaryPartyButton
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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
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
        title = "Wavelength",
        titleColor = TextPrimary,
        gameId = "wavelength",
        onExitGame = onExitGame
    ) {
        if (gamePhase == "MODE_SELECT") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Select Play Mode", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Choose single phone pass or remote dial sync", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            isRemoteMode = false
                            gamePhase = "PSYCHIC_PEEK"
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MysteryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Psychic peeks target angle on single phone", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            isRemoteMode = true
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
                            Text("🌐", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Remote Play (Multi-Device)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Psychic sees secret target; team turns dial remotely", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Spectrum Pair Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentSpectrum.first, color = BrandPrimary, fontFamily = ModernSansFont, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("◄ ── Spectrum ── ►", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(currentSpectrum.second, color = AlertRed, fontFamily = ModernSansFont, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Dial Arc Wheel Canvas Widget
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
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
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val w = size.width
                        val h = size.height

                        // Outer Arc Track
                        drawArc(
                            color = SurfaceSubtle,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(20f, 20f),
                            size = Size(w - 40f, h - 40f),
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Target Zone Arc (Visible during PSYCHIC_PEEK or REVEAL)
                        if (gamePhase == "PSYCHIC_PEEK" || gamePhase == "REVEAL") {
                            drawArc(
                                color = BrandPrimary,
                                startAngle = 180f + (180f - targetAngle - 10f),
                                sweepAngle = 20f,
                                useCenter = false,
                                topLeft = Offset(20f, 20f),
                                size = Size(w - 40f, h - 40f),
                                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Team Needle Dial Pointer Line
                        val needleRad = Math.toRadians((180.0 - dialAngle)).toFloat()
                        val needleLen = (w / 2f) - 24.dp.toPx()
                        val endX = (w / 2f) + needleLen * kotlin.math.cos(needleRad)
                        val endY = (h / 2f) - needleLen * kotlin.math.sin(needleRad)

                        drawLine(
                            color = AlertRed,
                            start = Offset(w / 2f, h / 2f),
                            end = Offset(endX, endY),
                            strokeWidth = 5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                if (gamePhase == "PSYCHIC_PEEK") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text("Psychic Role: Give A Clue", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = psychicClue,
                            onValueChange = { psychicClue = it },
                            placeholder = { Text("Enter your verbal spectrum clue...", color = TextSecondary, fontFamily = ModernSansFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight,
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )
                    }

                    PrimaryPartyButton(
                        text = "Lock Clue & Hand Dial to Team ▶",
                        accentColor = BrandPrimary,
                        onClick = {
                            if (psychicClue.isNotBlank()) {
                                haptics.performPop()
                                gamePhase = "TEAM_DIAL"
                                syncRemote(targetAngle, dialAngle, psychicClue, "TEAM_DIAL")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "TEAM_DIAL") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandPrimaryContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Psychic Clue: \"$psychicClue\"", color = BrandPrimary, fontFamily = ModernSansFont, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Drag needle along spectrum arc to target spot!", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp)
                    }

                    PrimaryPartyButton(
                        text = "Open Shutter & Reveal Score 🎯",
                        accentColor = BrandPrimary,
                        onClick = {
                            haptics.performHeavyBurst()
                            scoreEarned = calculateScore()
                            gamePhase = "REVEAL"
                            syncRemote(targetAngle, dialAngle, psychicClue, "REVEAL")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "REVEAL") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (scoreEarned > 0) SuccessContainer else AlertContainer)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (scoreEarned > 0) "+$scoreEarned Points! 🎯" else "Missed Target! 0 Pts",
                                color = if (scoreEarned > 0) SuccessGreen else AlertRed,
                                fontFamily = ModernSansFont,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    PrimaryPartyButton(
                        text = "Next Spectrum Round 🔮",
                        accentColor = BrandPrimary,
                        onClick = {
                            currentSpectrum = spectrumPairs.random()
                            psychicClue = ""
                            targetAngle = (30..150).random().toFloat()
                            dialAngle = 90f
                            gamePhase = "MODE_SELECT"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "wavelength",
                gameName = "Wavelength",
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

