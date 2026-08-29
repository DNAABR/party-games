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
import com.leminno.partygames.ui.components.PrimaryPartyButton
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
        title = "Twit Trivia",
        titleColor = TextPrimary,
        gameId = "twit",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Question Prompt Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceLight)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                    .padding(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(TriviaContainer)
                            .border(1.dp, TriviaBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("NUMERICAL TRIVIA", color = TriviaText, fontFamily = ModernSansFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentQuestion.prompt,
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (gamePhase == "GUESS_INPUT") {
                // Input secret guess per player
                val activePlayerName = "Player ${currentPlayerIdx + 1}"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandPrimaryContainer)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Pass Phone to $activePlayerName",
                            color = BrandPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = inputGuessText,
                        onValueChange = { inputGuessText = it },
                        placeholder = { Text("Enter your numeric guess", color = TextSecondary, fontFamily = ModernSansFont) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    text = if (currentPlayerIdx + 1 < playerCount) "Submit & Pass Phone ▶" else "Lock Guesses & Bet 🎰",
                    accentColor = BrandPrimary,
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
                    enabled = inputGuessText.toIntOrNull() != null,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (gamePhase == "BETTING") {
                // Spectrum layout betting phase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Bet on closest without going over",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(guessesList) { item ->
                            val isSelected = selectedBetGuess == item
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .subtleCardShadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) BrandPrimaryContainer else SurfaceLight)
                                    .border(1.dp, if (isSelected) BrandPrimary else BorderSubtle, RoundedCornerShape(16.dp))
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
                                    Text("Guess #${guessesList.indexOf(item) + 1}", color = TextPrimary, fontFamily = ModernSansFont, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${item.guessValue}",
                                        color = if (isSelected) BrandPrimary else TextPrimary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                PrimaryPartyButton(
                    text = "Reveal Truth & Payout 💰",
                    accentColor = BrandPrimary,
                    onClick = {
                        if (selectedBetGuess != null) {
                            haptics.performHeavyBurst()
                            gamePhase = "REVEAL"
                        }
                    },
                    enabled = selectedBetGuess != null,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Reveal & Payout
                val validGuesses = guessesList.filter { it.guessValue <= currentQuestion.actualAnswer }
                val winningGuess = validGuesses.maxByOrNull { it.guessValue }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Actual Answer", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${currentQuestion.actualAnswer}",
                                color = SuccessGreen,
                                fontFamily = ModernSansFont,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (winningGuess != null) SuccessContainer else AlertContainer)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (winningGuess != null) "Closest: ${winningGuess.playerName} (${winningGuess.guessValue}) 🎉" else "All guesses went over! 💥",
                                    color = if (winningGuess != null) SuccessGreen else AlertRed,
                                    fontFamily = ModernSansFont,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                PrimaryPartyButton(
                    text = "Next Question ▶",
                    accentColor = BrandPrimary,
                    onClick = {
                        currentQuestion = twitQuestions.random()
                        currentPlayerIdx = 0
                        inputGuessText = ""
                        guessesList = emptyList()
                        selectedBetGuess = null
                        gamePhase = "GUESS_INPUT"
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

