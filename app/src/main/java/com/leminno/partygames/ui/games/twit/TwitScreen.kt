package com.leminno.partygames.ui.games.twit

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.theme.*

data class NumericQuestion(
    val prompt: String,
    val actualAnswer: Int
)

val twitQuestions = listOf(
    NumericQuestion("How many total bones are in an adult cat's body?", 230),
    NumericQuestion("How many total stairs are in the Eiffel Tower?", 1665),
    NumericQuestion("How many element tiles are on a standard Scrabble board?", 100),
    NumericQuestion("How many total keys are on a full-size piano keyboard?", 88),
    NumericQuestion("How many days does it take for the Moon to orbit Earth?", 27)
)

data class PlayerGuess(
    val playerName: String,
    val guessValue: Int
)

@Composable
fun TwitScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    var currentQuestion by remember { mutableStateOf(twitQuestions.random()) }
    var gamePhase by remember { mutableStateOf("GUESS_INPUT") } // GUESS_INPUT, BETTING, REVEAL
    var currentPlayerIdx by remember { mutableIntStateOf(0) }
    var inputGuessText by remember { mutableStateOf("") }
    var guessesList by remember { mutableStateOf<List<PlayerGuess>>(emptyList()) }
    var selectedBetGuess by remember { mutableStateOf<PlayerGuess?>(null) }

    GameScaffold(
        title = "TWIT (NUMERIC BETTING) 🎰",
        titleColor = Color(0xFFFFD166),
        gameId = "twit",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Question Prompt Card
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
                    Text("NUMERICAL TRIVIA QUESTION", color = Color(0xFFFFD166), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.prompt,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (gamePhase == "GUESS_INPUT") {
                // Input secret guess per player
                val activePlayerName = "Player ${currentPlayerIdx + 1}"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PASS PHONE TO $activePlayerName",
                        color = Color(0xFF00F2FE),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = inputGuessText,
                        onValueChange = { inputGuessText = it },
                        label = { Text("Enter your numeric guess") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00F2FE), unfocusedBorderColor = BorderGlassDefault)
                    )
                }

                Button(
                    onClick = {
                        val num = inputGuessText.toIntOrNull()
                        if (num != null) {
                            haptics.performPop()
                            guessesList = guessesList + PlayerGuess(activePlayerName, num)
                            inputGuessText = ""
                            if (currentPlayerIdx + 1 < playerCount) {
                                currentPlayerIdx++
                            } else {
                                guessesList = guessesList.sortedBy { it.guessValue }
                                gamePhase = "BETTING"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = inputGuessText.toIntOrNull() != null
                ) {
                    Text(
                        text = if (currentPlayerIdx + 1 < playerCount) "SUBMIT & PASS PHONE ▶" else "LOCK GUESSES & START BETTING 🎰",
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (gamePhase == "BETTING") {
                // Spectrum layout betting phase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "BET ON CLOSEST WITHOUT GOING OVER",
                        color = Color(0xFFFFD166),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(guessesList) { item ->
                            val isSelected = selectedBetGuess == item
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0x33FFD166) else SurfaceGlassDark)
                                    .border(1.5.dp, if (isSelected) Color(0xFFFFD166) else BorderGlassDefault, RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptics.performPop()
                                        selectedBetGuess = item
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Guess #${guessesList.indexOf(item) + 1}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${item.guessValue}",
                                        color = Color(0xFFFFD166),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedBetGuess != null) {
                            haptics.performHeavyBurst()
                            gamePhase = "REVEAL"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedBetGuess != null
                ) {
                    Text("REVEAL TRUTH & PAYOUT 💰", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else {
                // Reveal & Payout
                val validGuesses = guessesList.filter { it.guessValue <= currentQuestion.actualAnswer }
                val winningGuess = validGuesses.maxByOrNull { it.guessValue }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ACTUAL ANSWER: ${currentQuestion.actualAnswer}",
                        color = Color(0xFF00E676),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (winningGuess != null) "CLOSEST GUESS: ${winningGuess.playerName} (${winningGuess.guessValue}) 🎉" else "ALL GUESSES WENT OVER! 💥",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        currentQuestion = twitQuestions.random()
                        currentPlayerIdx = 0
                        inputGuessText = ""
                        guessesList = emptyList()
                        selectedBetGuess = null
                        gamePhase = "GUESS_INPUT"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("NEXT QUESTION ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
