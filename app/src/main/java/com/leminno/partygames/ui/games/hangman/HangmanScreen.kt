package com.leminno.partygames.ui.games.hangman

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*

val presetHangmanWords = listOf("PARTY", "ANDROID", "KOTLIN", "CIRCUS", "ROCKET", "GUITAR", "PIRATE", "DRAGON")

@Composable
fun HangmanScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    var gamePhase by remember { mutableStateOf("WORD_SETUP") } // WORD_SETUP, GUESSING, GAME_OVER
    var secretWord by remember { mutableStateOf(presetHangmanWords.random()) }
    var inputCustomWord by remember { mutableStateOf("") }
    var guessedLetters by remember { mutableStateOf<Set<Char>>(emptySet()) }
    var wrongGuessCount by remember { mutableIntStateOf(0) }
    var isVictory by remember { mutableStateOf(false) }

    val qwertyRows = listOf(
        listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
        listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
        listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')
    )

    fun handleLetterGuess(letter: Char) {
        if (guessedLetters.contains(letter) || isVictory || wrongGuessCount >= 6) return

        guessedLetters = guessedLetters + letter
        if (secretWord.contains(letter, ignoreCase = true)) {
            haptics.performPop()
            val unrevealed = secretWord.uppercase().filter { it.isLetter() }.any { !guessedLetters.contains(it) }
            if (!unrevealed) {
                isVictory = true
                gamePhase = "GAME_OVER"
            }
        } else {
            haptics.performWarningThud()
            wrongGuessCount++
            if (wrongGuessCount >= 6) {
                haptics.performHeavyBurst()
                isVictory = false
                gamePhase = "GAME_OVER"
            }
        }
    }

    GameScaffold(
        title = "HANGMAN 😵",
        titleColor = Color(0xFFFFD166),
        gameId = "hangman",
        onExitGame = onExitGame
    ) {
        if (gamePhase != "GAME_OVER") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (gamePhase == "WORD_SETUP") {
                    // Custom Word Entry
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SET SECRET HANGMAN WORD",
                            color = Color(0xFFFFD166),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = inputCustomWord,
                            onValueChange = { inputCustomWord = it.uppercase() },
                            label = { Text("Type custom word for group to guess") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD166), unfocusedBorderColor = BorderGlassDefault)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = {
                            haptics.performTick()
                            inputCustomWord = presetHangmanWords.random()
                        }) {
                            Text("🎲 Random Prompt Suggestion", color = Color(0xFF00F2FE), fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (inputCustomWord.isNotBlank()) {
                                haptics.performPop()
                                secretWord = inputCustomWord.trim().uppercase()
                                guessedLetters = emptySet()
                                wrongGuessCount = 0
                                gamePhase = "GUESSING"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("HIDE WORD & PASS TO GUESSERS ▶", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "GUESSING") {
                    // Graphic Hangman Stage Canvas
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceGlassDark)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Gallows Frame
                            drawLine(Color(0xFF8D99AE), Offset(20f, h - 20f), Offset(w - 20f, h - 20f), strokeWidth = 4.dp.toPx())
                            drawLine(Color(0xFF8D99AE), Offset(50f, h - 20f), Offset(50f, 20f), strokeWidth = 4.dp.toPx())
                            drawLine(Color(0xFF8D99AE), Offset(50f, 20f), Offset(w / 2f, 20f), strokeWidth = 4.dp.toPx())
                            drawLine(Color(0xFF8D99AE), Offset(w / 2f, 20f), Offset(w / 2f, 50f), strokeWidth = 2.dp.toPx())

                            // 1. Head
                            if (wrongGuessCount >= 1) {
                                drawCircle(Color(0xFFFF0055), radius = 18.dp.toPx(), center = Offset(w / 2f, 50f + 18.dp.toPx()), style = Stroke(width = 3.dp.toPx()))
                            }
                            // 2. Body
                            if (wrongGuessCount >= 2) {
                                drawLine(Color(0xFFFF0055), Offset(w / 2f, 50f + 36.dp.toPx()), Offset(w / 2f, 50f + 80.dp.toPx()), strokeWidth = 4.dp.toPx())
                            }
                            // 3. Left Arm
                            if (wrongGuessCount >= 3) {
                                drawLine(Color(0xFFFF0055), Offset(w / 2f, 50f + 45.dp.toPx()), Offset(w / 2f - 25.dp.toPx(), 50f + 65.dp.toPx()), strokeWidth = 3.dp.toPx())
                            }
                            // 4. Right Arm
                            if (wrongGuessCount >= 4) {
                                drawLine(Color(0xFFFF0055), Offset(w / 2f, 50f + 45.dp.toPx()), Offset(w / 2f + 25.dp.toPx(), 50f + 65.dp.toPx()), strokeWidth = 3.dp.toPx())
                            }
                            // 5. Left Leg
                            if (wrongGuessCount >= 5) {
                                drawLine(Color(0xFFFF0055), Offset(w / 2f, 50f + 80.dp.toPx()), Offset(w / 2f - 25.dp.toPx(), 50f + 110.dp.toPx()), strokeWidth = 3.dp.toPx())
                            }
                            // 6. Right Leg (Execution complete)
                            if (wrongGuessCount >= 6) {
                                drawLine(Color(0xFFFF0055), Offset(w / 2f, 50f + 80.dp.toPx()), Offset(w / 2f + 25.dp.toPx(), 50f + 110.dp.toPx()), strokeWidth = 3.dp.toPx())
                            }
                        }
                    }

                    // Uncovered Word Slots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        secretWord.forEach { char ->
                            val isRevealed = guessedLetters.contains(char.uppercaseChar()) || !char.isLetter()
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceGlassDark)
                                    .border(1.5.dp, if (isRevealed) Color(0xFFFFD166) else BorderGlassDefault, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isRevealed) "$char" else "_",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Virtual QWERTY Keyboard
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        qwertyRows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.forEach { char ->
                                    val isUsed = guessedLetters.contains(char)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isUsed) Color.White.copy(alpha = 0.1f) else Color(0xFFFFD166))
                                            .clickable(enabled = !isUsed) { handleLetterGuess(char) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$char",
                                            color = if (isUsed) TextMuted else Color.Black,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = if (isVictory) "WORD UNCOVERED! VICTORY! 🎉" else "EXECUTION COMPLETE! ☠️",
                subtitle = "Secret Word Was: $secretWord",
                onPlayAgain = {
                    inputCustomWord = ""
                    gamePhase = "WORD_SETUP"
                },
                onBackToHub = onExitGame
            )
        }
    }
}
