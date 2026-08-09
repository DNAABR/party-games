package com.leminno.partygames.ui.games.write_funny

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*

val funnyPrompts = listOf(
    "The worst thing to say right after a job interview handshake",
    "A strange title for a self-help book written by a cat",
    "An unconvincing excuse for being 3 hours late to a wedding",
    "The secret warning label hidden on cheap sunglasses",
    "What aliens really think about human reality TV shows"
)

@Composable
fun WriteFunnyScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    var currentPrompt by remember { mutableStateOf(funnyPrompts.random()) }
    var gamePhase by remember { mutableStateOf("PLAYER1_INPUT") } // PLAYER1_INPUT, PLAYER2_INPUT, GROUP_VOTE, REVEAL
    var answer1 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }
    var selectedWinner by remember { mutableIntStateOf(0) }

    GameScaffold(
        title = "WRITE FUNNY (QUIPLASH) ⚡",
        titleColor = Color(0xFFFFD166),
        gameId = "write_funny",
        onExitGame = onExitGame
    ) {
        if (gamePhase != "REVEAL") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Prompt Box
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
                        Text("ABSURD PROMPT", color = Color(0xFFFFD166), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentPrompt,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (gamePhase == "PLAYER1_INPUT" || gamePhase == "PLAYER2_INPUT") {
                    val isPlayer1 = gamePhase == "PLAYER1_INPUT"
                    val activeTitle = if (isPlayer1) "PLAYER 1 RESPONSE" else "PLAYER 2 RESPONSE"
                    val activeValue = if (isPlayer1) answer1 else answer2

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeTitle,
                            color = Color(0xFF00F2FE),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = activeValue,
                            onValueChange = { if (isPlayer1) answer1 = it else answer2 = it },
                            label = { Text("Write your funny punchline") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00F2FE), unfocusedBorderColor = BorderGlassDefault)
                        )
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            if (isPlayer1) {
                                gamePhase = "PLAYER2_INPUT"
                            } else {
                                gamePhase = "GROUP_VOTE"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = activeValue.isNotBlank()
                    ) {
                        Text(
                            text = if (isPlayer1) "SUBMIT & PASS TO PLAYER 2 ▶" else "LOCK ANSWERS & START VOTE 🗳️",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (gamePhase == "GROUP_VOTE") {
                    // Anonymous Side-by-Side Voting Cards
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "WHICH PUNCHLINE IS FUNNIER?",
                            color = Color(0xFFFFD166),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Answer Option 1
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedWinner == 1) Color(0x3300F2FE) else SurfaceGlassDark)
                                .border(1.5.dp, if (selectedWinner == 1) Color(0xFF00F2FE) else BorderGlassDefault, RoundedCornerShape(16.dp))
                                .clickable {
                                    haptics.performPop()
                                    selectedWinner = 1
                                }
                                .padding(20.dp)
                        ) {
                            Text(answer1, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("⚔️ VS ⚔️", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Black)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Answer Option 2
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedWinner == 2) Color(0x33FF007F) else SurfaceGlassDark)
                                .border(1.5.dp, if (selectedWinner == 2) Color(0xFFFF007F) else BorderGlassDefault, RoundedCornerShape(16.dp))
                                .clickable {
                                    haptics.performPop()
                                    selectedWinner = 2
                                }
                                .padding(20.dp)
                        ) {
                            Text(answer2, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedWinner != 0) {
                                haptics.performHeavyBurst()
                                gamePhase = "REVEAL"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = selectedWinner != 0
                    ) {
                        Text("CROWN WINNER 👑", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            val winningAnswer = if (selectedWinner == 1) answer1 else answer2
            val winnerName = if (selectedWinner == 1) "PLAYER 1" else "PLAYER 2"

            VictoryCeremonyOverlay(
                winnerTitle = "VICTORY TO $winnerName! 👑",
                subtitle = "\"$winningAnswer\"",
                onPlayAgain = {
                    currentPrompt = funnyPrompts.random()
                    answer1 = ""
                    answer2 = ""
                    selectedWinner = 0
                    gamePhase = "PLAYER1_INPUT"
                },
                onBackToHub = onExitGame
            )
        }
    }
}
