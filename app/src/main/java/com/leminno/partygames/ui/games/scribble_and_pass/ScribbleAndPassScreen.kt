package com.leminno.partygames.ui.games.scribble_and_pass

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

data class DrawPathState(
    val path: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

val samplePrompts = listOf(
    "A cat wearing a tuxedo and top hat",
    "A rocket ship landing on a pizza moon",
    "A giant panda surfing a tidal wave",
    "A detective hamster solving a crime",
    "An alien eating a hot dog on Earth"
)

@Composable
fun ScribbleAndPassScreen(
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

    var secretPrompt by remember { mutableStateOf(samplePrompts.random()) }
    var guessInput by remember { mutableStateOf("") }

    // Canvas drawing state
    var paths by remember { mutableStateOf<List<DrawPathState>>(emptyList()) }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color(0xFF1E293B)) }
    var strokeWidth by remember { mutableFloatStateOf(8f) }

    val colorPalette = listOf(
        Color(0xFF1E293B), Color(0xFF6366F1), Color(0xFFEC4899),
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF06B6D4)
    )

    // Observe Remote Game State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remotePrompt = room.gameState["prompt"] as? String
                    val remoteGuess = room.gameState["guess"] as? String
                    val remotePhase = room.gameState["phase"] as? String

                    if (!remotePrompt.isNullOrBlank()) secretPrompt = remotePrompt
                    if (!remoteGuess.isNullOrBlank()) guessInput = remoteGuess
                    if (remotePhase != null && remotePhase != gamePhase) {
                        gamePhase = remotePhase
                    }
                }
            }
        }
    }

    fun syncRemote(prompt: String, guess: String, phase: String) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("prompt" to prompt, "guess" to guess, "phase" to phase)
                )
            }
        }
    }

    GameScaffold(
        title = "Scribble & Pass",
        titleColor = TextPrimary,
        gameId = "scribble_and_pass",
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
                Text("Choose single phone pass or online room sync", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                            gamePhase = "PROMPT_ENTRY"
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
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Draw & pass single phone around group", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                                .background(MysteryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Remote Play (Multi-Device)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Draw on phone, pass across rooms via code", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                if (gamePhase == "PROMPT_ENTRY") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = "Secret Drawing Prompt",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = secretPrompt,
                            onValueChange = { secretPrompt = it },
                            placeholder = { Text("What should the artist draw?", color = TextSecondary, fontFamily = ModernSansFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight,
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = {
                            haptics.performTick()
                            secretPrompt = samplePrompts.random()
                        }) {
                            Text("🎲 Random Prompt Suggestion", color = BrandPrimary, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    PrimaryPartyButton(
                        text = "Start Drawing Canvas ▶",
                        accentColor = BrandPrimary,
                        onClick = {
                            if (secretPrompt.isNotBlank()) {
                                haptics.performPop()
                                gamePhase = "DRAWING"
                                syncRemote(secretPrompt, guessInput, "DRAWING")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "DRAWING") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ActionContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Draw: $secretPrompt",
                                color = ActionText,
                                fontFamily = ModernSansFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceLight)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset -> currentPath = listOf(offset) },
                                        onDragEnd = {
                                            if (currentPath.isNotEmpty()) {
                                                paths = paths + DrawPathState(currentPath, selectedColor, strokeWidth)
                                                currentPath = emptyList()
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            currentPath = currentPath + change.position
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                paths.forEach { drawPath ->
                                    for (i in 0 until drawPath.path.size - 1) {
                                        drawLine(
                                            color = drawPath.color,
                                            start = drawPath.path[i],
                                            end = drawPath.path[i + 1],
                                            strokeWidth = drawPath.strokeWidth,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                                for (i in 0 until currentPath.size - 1) {
                                    drawLine(
                                        color = selectedColor,
                                        start = currentPath[i],
                                        end = currentPath[i + 1],
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                colorPalette.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(if (selectedColor == color) 2.5.dp else 0.dp, BrandPrimary, CircleShape)
                                            .clickable { selectedColor = color }
                                    )
                                }
                            }

                            TextButton(onClick = { paths = emptyList() }) {
                                Text("🗑️ Clear", color = AlertRed, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    PrimaryPartyButton(
                        text = "Submit & Pass To Guesser ▶",
                        accentColor = BrandPrimary,
                        onClick = {
                            haptics.performPop()
                            gamePhase = "GUESSING"
                            syncRemote(secretPrompt, guessInput, "GUESSING")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "GUESSING") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text("Guess What Was Drawn!", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceLight)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                paths.forEach { drawPath ->
                                    for (i in 0 until drawPath.path.size - 1) {
                                        drawLine(
                                            color = drawPath.color,
                                            start = drawPath.path[i],
                                            end = drawPath.path[i + 1],
                                            strokeWidth = drawPath.strokeWidth,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = guessInput,
                            onValueChange = { guessInput = it },
                            placeholder = { Text("Enter your guess here...", color = TextSecondary, fontFamily = ModernSansFont) },
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
                        text = "Reveal Album Comparison 🎉",
                        accentColor = BrandPrimary,
                        onClick = {
                            if (guessInput.isNotBlank()) {
                                haptics.performPop()
                                gamePhase = "ALBUM_REVEAL"
                                syncRemote(secretPrompt, guessInput, "ALBUM_REVEAL")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "ALBUM_REVEAL") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text("Album Reveal 🎉", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ActionContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Original: $secretPrompt", color = ActionText, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MysteryContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Guess: $guessInput", color = MysteryText, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceLight)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                paths.forEach { drawPath ->
                                    for (i in 0 until drawPath.path.size - 1) {
                                        drawLine(
                                            color = drawPath.color,
                                            start = drawPath.path[i],
                                            end = drawPath.path[i + 1],
                                            strokeWidth = drawPath.strokeWidth,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PrimaryPartyButton(
                        text = "Play Another Round 🔄",
                        accentColor = BrandPrimary,
                        onClick = {
                            paths = emptyList()
                            secretPrompt = samplePrompts.random()
                            guessInput = ""
                            gamePhase = "MODE_SELECT"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "scribble_and_pass",
                gameName = "Scribble & Pass",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "PROMPT_ENTRY"
                    showRemoteSheet = false
                }
            )
        }
    }
}

