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
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.theme.*

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
    val haptics = remember { HapticFeedbackManager(context) }

    var gamePhase by remember { mutableStateOf("PROMPT_ENTRY") } // PROMPT_ENTRY, DRAWING, GUESSING, ALBUM_REVEAL
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

    GameScaffold(
        title = "SCRIBBLE & PASS 🎨",
        titleColor = Color(0xFF00F2FE),
        gameId = "scribble_and_pass",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (gamePhase == "PROMPT_ENTRY") {
                // Secret Prompt Entry
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
                            paths = emptyList()
                            gamePhase = "DRAWING"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("PASS TO ARTIST ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else if (gamePhase == "DRAWING") {
                // Vector Canvas Drawing Phase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DRAW THIS PROMPT: $secretPrompt",
                        color = Color(0xFFFFD166),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compose Vector Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF141A29))
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPath = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        val newPoints = currentPath + change.position
                                        currentPath = newPoints
                                    },
                                    onDragEnd = {
                                        if (currentPath.isNotEmpty()) {
                                            paths = paths + DrawPathState(currentPath, selectedColor, strokeWidth)
                                            currentPath = emptyList()
                                        }
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
                            if (currentPath.size > 1) {
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
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Palette Picker & Clear Canvas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            colorPalette.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (selectedColor == color) 2.5.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            haptics.performTick()
                                            selectedColor = color
                                        }
                                )
                            }
                        }

                        IconButton(onClick = {
                            haptics.performPop()
                            paths = emptyList()
                            currentPath = emptyList()
                        }) {
                            Text("🧹 Clear", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                Button(
                    onClick = {
                        haptics.performPop()
                        gamePhase = "GUESSING"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("DONE DRAWING ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else if (gamePhase == "GUESSING") {
                // Guesser Phase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GUESS THE DRAWING!",
                        color = Color(0xFF00F2FE),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rendered Artwork Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF141A29))
                            .border(1.5.dp, BorderGlassDefault, RoundedCornerShape(20.dp))
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

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = guessInput,
                        onValueChange = { guessInput = it },
                        label = { Text("What did they draw?") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00F2FE), unfocusedBorderColor = BorderGlassDefault)
                    )
                }

                Button(
                    onClick = {
                        haptics.performHeavyBurst()
                        gamePhase = "ALBUM_REVEAL"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("REVEAL ALBUM 🖼️", color = Color.White, fontWeight = FontWeight.Black)
                }
            } else {
                // Album Reveal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CHAIN REACTION ALBUM! 🖼️",
                        color = Color(0xFFFFD166),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFFFFD166), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ORIGINAL PROMPT:", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(secretPrompt, color = Color(0xFF00F2FE), fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("FINAL GUESS:", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(if (guessInput.isNotBlank()) guessInput else "No Guess", color = Color(0xFFFF007F), fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        }
                    }
                }

                Button(
                    onClick = {
                        secretPrompt = samplePrompts.random()
                        guessInput = ""
                        paths = emptyList()
                        gamePhase = "PROMPT_ENTRY"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("START NEW DRAWING CHAIN ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
