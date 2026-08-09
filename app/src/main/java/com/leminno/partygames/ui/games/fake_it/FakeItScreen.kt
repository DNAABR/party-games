package com.leminno.partygames.ui.games.fake_it

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.theme.*

data class FakeItQuestion(
    val prompt: String,
    val realAnswer: String
)

val fakeItQuestions = listOf(
    FakeItQuestion("In 1923, what was uniquely used as currency in Germany during hyperinflation?", "Stamps"),
    FakeItQuestion("What is the official state toy of Mississippi?", "Teddy Bear"),
    FakeItQuestion("What unexpected ingredient was originally in 19th century ketchup?", "Fish Gut Extract"),
    FakeItQuestion("What animal holds the record for the longest sleep cycle (3 years)?", "Desert Snail")
)

data class AnswerCard(
    val text: String,
    val isReal: Boolean,
    val author: String
)

@Composable
fun FakeItScreen(
    playerCount: Int = 3,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    var currentQuestion by remember { mutableStateOf(fakeItQuestions.random()) }
    var gamePhase by remember { mutableStateOf("BLUFF_INPUT") } // BLUFF_INPUT, VOTING, REVEAL
    var currentPlayerIdx by remember { mutableIntStateOf(0) }
    var bluffInputText by remember { mutableStateOf("") }
    var playerBluffs by remember { mutableStateOf<List<AnswerCard>>(emptyList()) }
    var selectedVoteCard by remember { mutableStateOf<AnswerCard?>(null) }

    GameScaffold(
        title = "FAKE IT (BLUFF) 🤥",
        titleColor = Color(0xFFFF007F),
        gameId = "fake_it",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Question Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceGlassDark)
                    .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("OBSCURE TRIVIA PROMPT", color = Color(0xFFFF007F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.prompt,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (gamePhase == "BLUFF_INPUT") {
                val activePlayer = "Player ${currentPlayerIdx + 1}"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PASS PHONE TO $activePlayer",
                        color = Color(0xFFFFD166),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = bluffInputText,
                        onValueChange = { bluffInputText = it },
                        label = { Text("Type a believable fake answer") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF007F), unfocusedBorderColor = BorderGlassDefault)
                    )
                }

                Button(
                    onClick = {
                        if (bluffInputText.isNotBlank()) {
                            haptics.performPop()
                            playerBluffs = playerBluffs + AnswerCard(bluffInputText, false, activePlayer)
                            bluffInputText = ""
                            if (currentPlayerIdx + 1 < playerCount) {
                                currentPlayerIdx++
                            } else {
                                // Add real answer and shuffle
                                val allCards = (playerBluffs + AnswerCard(currentQuestion.realAnswer, true, "TRUTH")).shuffled()
                                playerBluffs = allCards
                                gamePhase = "VOTING"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = bluffInputText.isNotBlank()
                ) {
                    Text(
                        text = if (currentPlayerIdx + 1 < playerCount) "SUBMIT FAKE ANSWER & PASS ▶" else "SHUFFLE & START GROUP VOTE 🗳️",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (gamePhase == "VOTING") {
                // Shuffled Answer Selection Card Matrix
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SELECT THE GENUINE TRUTH!",
                        color = Color(0xFFFFD166),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(playerBluffs) { card ->
                            val isSelected = selectedVoteCard == card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0x33FF007F) else SurfaceGlassDark)
                                    .border(1.5.dp, if (isSelected) Color(0xFFFF007F) else BorderGlassDefault, RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptics.performPop()
                                        selectedVoteCard = card
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(card.text, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedVoteCard != null) {
                            haptics.performHeavyBurst()
                            gamePhase = "REVEAL"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedVoteCard != null
                ) {
                    Text("REVEAL TRUTH 🏆", color = Color.White, fontWeight = FontWeight.Black)
                }
            } else {
                // Reveal Phase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GENUINE TRUTH: ${currentQuestion.realAnswer}",
                        color = Color(0xFF00E676),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedVoteCard?.isReal == true) {
                        Text("YOU SPOTTED THE TRUTH! +2 POINTS 🟢", color = Color(0xFF00E676), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("YOU GOT FOOLED BY ${selectedVoteCard?.author}'s BLUFF! 🔴", color = Color(0xFFFF0055), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        currentQuestion = fakeItQuestions.random()
                        currentPlayerIdx = 0
                        bluffInputText = ""
                        playerBluffs = emptyList()
                        selectedVoteCard = null
                        gamePhase = "BLUFF_INPUT"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("NEXT TRIVIA PROMPT ▶", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
