package com.leminno.partygames.ui.games.two_truths_and_a_lie

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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*

data class StatementItem(
    val text: String,
    val isLie: Boolean,
    val originalIndex: Int
)

val presetDecks = listOf(
    Triple("I have jumped out of an airplane", "I have met a celebrity", "I have lived in 5 different countries"),
    Triple("I can play 3 musical instruments", "I have never broken a bone", "I won a national chess tournament"),
    Triple("I have eaten fried grasshoppers", "I can speak 4 languages fluently", "I ran a full marathon"),
    Triple("I have been on national TV", "I can juggle 4 balls", "I owned a pet monkey as a child")
)

@Composable
fun TwoTruthsAndALieScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var truth1 by remember { mutableStateOf("") }
    var truth2 by remember { mutableStateOf("") }
    var lieInput by remember { mutableStateOf("") }

    var gamePhase by remember { mutableStateOf("INPUT") } // INPUT, VOTING, REVEAL
    var shuffledStatements by remember { mutableStateOf<List<StatementItem>>(emptyList()) }
    var selectedVoteIndex by remember { mutableStateOf<Int?>(null) }

    fun loadPreset() {
        val preset = presetDecks.random()
        truth1 = preset.first
        truth2 = preset.second
        lieInput = preset.third
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitGame) {
                    Text("✕", color = TextSecondary, fontSize = 22.sp)
                }
                Text(
                    text = "TWO TRUTHS & A LIE 🎭",
                    color = Color(0xFFFF007F),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            if (gamePhase == "INPUT") {
                // Input Form Phase
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACTIVE PLAYER ENTRY",
                        color = Color(0xFFFF007F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Enter 2 genuine truths and 1 believable lie below:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = truth1,
                        onValueChange = { truth1 = it },
                        label = { Text("Truth #1") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = BorderGlassDefault,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = truth2,
                        onValueChange = { truth2 = it },
                        label = { Text("Truth #2") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = BorderGlassDefault,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = lieInput,
                        onValueChange = { lieInput = it },
                        label = { Text("The Lie 🤫") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF007F),
                            unfocusedBorderColor = BorderGlassDefault,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = {
                        haptics.performTick(composeHaptics)
                        loadPreset()
                    }) {
                        Text("🎲 Auto-Fill Preset Statements", color = Color(0xFF00F2FE), fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = {
                        if (truth1.isNotBlank() && truth2.isNotBlank() && lieInput.isNotBlank()) {
                            haptics.performPop()
                            val items = listOf(
                                StatementItem(truth1, false, 0),
                                StatementItem(truth2, false, 1),
                                StatementItem(lieInput, true, 2)
                            ).shuffled()
                            shuffledStatements = items
                            gamePhase = "VOTING"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = truth1.isNotBlank() && truth2.isNotBlank() && lieInput.isNotBlank()
                ) {
                    Text("SHUFFLE & PASS PHONE ▶", color = Color.White, fontWeight = FontWeight.Black)
                }
            } else if (gamePhase == "VOTING") {
                // Group Voting Phase
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WHICH ONE IS THE LIE?",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Group reads statements and taps the suspected lie!",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        shuffledStatements.forEachIndexed { index, item ->
                            val isSelected = selectedVoteIndex == index
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0x33FF007F) else SurfaceGlassDark)
                                    .border(1.5.dp, if (isSelected) Color(0xFFFF007F) else BorderGlassDefault, RoundedCornerShape(16.dp))
                                    .clickable {
                                        haptics.performTick(composeHaptics)
                                        selectedVoteIndex = index
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${index + 1}.",
                                        color = Color(0xFFFF007F),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.text,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedVoteIndex != null) {
                            haptics.performHeavyBurst()
                            gamePhase = "REVEAL"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedVoteIndex != null
                ) {
                    Text("REVEAL TRUTH 🔓", color = Color.White, fontWeight = FontWeight.Black)
                }
            } else {
                // Reveal & Scoring Phase
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE REVEAL! 🎭",
                        color = Color(0xFFFF007F),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        shuffledStatements.forEachIndexed { index, item ->
                            val isVotedLie = selectedVoteIndex == index
                            val accent = if (item.isLie) Color(0xFFFF007F) else Color(0xFF00E676)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(accent.copy(alpha = 0.15f))
                                    .border(2.dp, accent, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.text,
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (item.isLie) "THE LIE 🤥" else "TRUTH ✓",
                                            color = accent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    if (isVotedLie) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("VOTED", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val votedCorrectly = selectedVoteIndex != null && shuffledStatements[selectedVoteIndex!!].isLie
                    Text(
                        text = if (votedCorrectly) "🎉 Group Spotted the Lie! +1 Point to Guessers!" else "😈 Active Player Tricked Everyone! +2 Points to Active Player!",
                        color = if (votedCorrectly) Color(0xFF00E676) else Color(0xFFFF007F),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        truth1 = ""
                        truth2 = ""
                        lieInput = ""
                        selectedVoteIndex = null
                        gamePhase = "INPUT"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("PLAY NEXT ROUND ▶", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
