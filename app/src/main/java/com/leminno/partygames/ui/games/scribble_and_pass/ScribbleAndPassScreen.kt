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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, PROMPT_ENTRY, DRAWING, GUESSING, ALBUM_REVEAL
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var secretPrompt by remember { mutableStateOf(samplePrompts.random()) }
    var guessInput by remember { mutableStateOf("") }

    // Canvas drawing state
    var paths by remember { mutableStateOf<List<DrawPathState>>(emptyList()) }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color(0xFF00F2FE)) }
    var strokeWidth by remember { mutableFloatStateOf(10f) }

    val colorPalette = listOf(
        Color(0xFF00F2FE), Color(0xFFFF007F), Color(0xFFFFD166),
        Color(0xFF00E676), Color(0xFF9D4EDD), Color.White
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
        title = "SCRIBBLE & PASS 🎨",
        titleColor = Color(0xFF00F2FE),
        gameId = "scribble_and_pass",
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
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = false
                                gamePhase = "PROMPT_ENTRY"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Draw & pass single phone around group", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(20.dp))
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
                                Text("Remote Play (Multi-Device)", color = Color(0xFFFF007F), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Draw on your phone, pass across rooms via Room Code", color = TextMuted, fontSize = 12.sp)
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
                if (gamePhase == "PROMPT_ENTRY") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SECRET DRAWING PROMPT",
                            color = Color(0xFF00F2FE),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = secretPrompt,
                            onValueChange = { secretPrompt = it },
                            label = { Text("What should the artist draw?") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00F2FE),
                                unfocusedBorderColor = BorderGlassDefault,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = {
                            haptics.performTick()
                            secretPrompt = samplePrompts.random()
                        }) {
                            Text("🎲 Random Prompt Suggestion", color = Color(0xFFFFD166), fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (secretPrompt.isNotBlank()) {
                                haptics.performPop()
                                gamePhase = "DRAWING"
                                syncRemote(secretPrompt, guessInput, "DRAWING")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("START DRAWING CANVAS ▶", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "DRAWING") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DRAW THIS PROMPT:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = secretPrompt,
                            color = Color(0xFFFFD166),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            colorPalette.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(if (selectedColor == color) 2.dp else 0.dp, Color.White, CircleShape)
                                        .clickable { selectedColor = color }
                                )
                            }
                        }

                        TextButton(onClick = { paths = emptyList() }) {
                            Text("🗑️ Clear", color = Color(0xFFFF007F))
                        }
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            gamePhase = "GUESSING"
                            syncRemote(secretPrompt, guessInput, "GUESSING")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("SUBMIT DRAWING & PASS TO GUESSER ▶", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "GUESSING") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GUESS WHAT WAS DRAWN!", color = Color(0xFFFF007F), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceGlassDark)
                                .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(20.dp))
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

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = guessInput,
                            onValueChange = { guessInput = it },
                            label = { Text("Enter your guess here...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF007F),
                                unfocusedBorderColor = BorderGlassDefault,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Button(
                        onClick = {
                            if (guessInput.isNotBlank()) {
                                haptics.performPop()
                                gamePhase = "ALBUM_REVEAL"
                                syncRemote(secretPrompt, guessInput, "ALBUM_REVEAL")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("REVEAL ALBUM & PROMPT COMPARISON 🎉", color = Color.White, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "ALBUM_REVEAL") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ALBUM REVEAL 🎉", color = Color(0xFFFFD166), fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("ORIGINAL PROMPT: $secretPrompt", color = Color(0xFF00F2FE), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("FINAL GUESS: $guessInput", color = Color(0xFFFF007F), fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceGlassDark)
                                .border(1.5.dp, Color(0xFFFFD166), RoundedCornerShape(20.dp))
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

                    Button(
                        onClick = {
                            paths = emptyList()
                            secretPrompt = samplePrompts.random()
                            guessInput = ""
                            gamePhase = "MODE_SELECT"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("PLAY ANOTHER ROUND 🔄", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "scribble_and_pass",
                gameName = "Scribble & Pass 🎨",
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
