package com.leminno.partygames.ui.games.hand_cricket

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class HandCricketGameMode(val title: String, val subtitle: String, val icon: String) {
    SPLIT_SCREEN("1v1 Split Screen", "Same device facing opposite directions", "📲"),
    TEAM_ROOM("Team Match / Online Room", "Multi-Device & >2 Players in Teams", "🌐")
}

@Composable
fun HandCricketScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var selectedGameMode by remember { mutableStateOf<HandCricketGameMode?>(null) }

    // Multi-Device Online Room State
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }
    var roomJoined by remember { mutableStateOf(false) }

    // Game state
    var isInnings1 by remember { mutableStateOf(true) }
    var team1Name by remember { mutableStateOf("Team A") }
    var team2Name by remember { mutableStateOf("Team B") }

    var batterScore by remember { mutableIntStateOf(0) }
    var wicketsLost by remember { mutableIntStateOf(0) }
    val maxWickets = 3

    var innings1Target by remember { mutableIntStateOf(0) }

    var p1Choice by remember { mutableStateOf<Int?>(null) } // Batter
    var p2Choice by remember { mutableStateOf<Int?>(null) } // Bowler
    var roundResultText by remember { mutableStateOf<String?>(null) }
    var matchGameOver by remember { mutableStateOf(false) }
    var winnerName by remember { mutableStateOf("") }

    fun generateRoomCode(): String {
        return (100000..999999).random().toString()
    }

    fun evaluateRound() {
        if (p1Choice != null && p2Choice != null) {
            val bat = p1Choice!!
            val bowl = p2Choice!!

            if (bat == bowl) {
                // OUT!
                haptics.performHeavyClick(composeHaptics)
                wicketsLost++
                roundResultText = "WICKET! OUT! 💥 Both picked $bat!"

                if (wicketsLost >= maxWickets) {
                    if (isInnings1) {
                        // Switch Innings
                        innings1Target = batterScore + 1
                        batterScore = 0
                        wicketsLost = 0
                        isInnings1 = false
                        roundResultText = "INNINGS 1 OVER! Target: $innings1Target Runs!"
                    } else {
                        // Match Over
                        matchGameOver = true
                        winnerName = if (batterScore >= innings1Target) team2Name else team1Name
                    }
                }
            } else {
                // RUNS ADDED!
                haptics.performSuccess()
                batterScore += bat
                roundResultText = "+$bat Runs! (Bat: $bat, Bowl: $bowl)"

                if (!isInnings1 && batterScore >= innings1Target) {
                    matchGameOver = true
                    winnerName = team2Name
                }
            }

            p1Choice = null
            p2Choice = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F141D))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        if (selectedGameMode == null) {
            // Mode Selector
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onExitGame) {
                        Text("✕", color = TextSecondary, fontSize = 22.sp)
                    }
                    Text(
                        text = "HAND CRICKET 🏏",
                        color = Color(0xFFFFD166),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SELECT PLAY MODE",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        HandCricketGameMode.entries.forEach { mode ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(SurfaceGlassDark)
                                    .border(1.5.dp, Color(0xFFFFD166), RoundedCornerShape(20.dp))
                                    .clickable {
                                        haptics.performTick(composeHaptics)
                                        selectedGameMode = mode
                                        if (mode == HandCricketGameMode.TEAM_ROOM) {
                                            roomCode = generateRoomCode()
                                        }
                                    }
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = mode.icon, fontSize = 36.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = mode.title,
                                            color = Color(0xFFFFD166),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = mode.subtitle,
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                TextButton(onClick = onExitGame) {
                    Text("Back to Hub", color = TextMuted)
                }
            }
        } else if (selectedGameMode == HandCricketGameMode.TEAM_ROOM && !roomJoined) {
            // Online Multi-Device Room Setup View
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedGameMode = null }) {
                        Text("←", color = TextSecondary, fontSize = 22.sp)
                    }
                    Text(
                        text = "ONLINE TEAM ROOM 🌐",
                        color = Color(0xFF00F2FE),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceGlassDark)
                            .border(2.dp, Color(0xFF00F2FE), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "MATCH ROOM CODE", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = roomCode,
                                color = Color(0xFF00F2FE),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Share room code with teammates on other phones!",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "TEAM ASSIGNMENTS",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = team1Name,
                            onValueChange = { team1Name = it },
                            label = { Text("Team 1 Name") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00F2FE), unfocusedBorderColor = BorderGlassDefault)
                        )

                        OutlinedTextField(
                            value = team2Name,
                            onValueChange = { team2Name = it },
                            label = { Text("Team 2 Name") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF007F), unfocusedBorderColor = BorderGlassDefault)
                        )
                    }
                }

                Button(
                    onClick = {
                        haptics.performSuccess()
                        roomJoined = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("JOIN ROOM & START MATCH ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        } else if (!matchGameOver) {
            // Live Hand Cricket Game Board
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Scoreboard Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceGlassDark)
                        .border(1.dp, BorderGlassDefault, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isInnings1) "$team1Name (Batting)" else "$team2Name (Chasing Target $innings1Target)",
                                color = Color(0xFFFFD166),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$batterScore Runs  •  $wicketsLost/$maxWickets Wickets",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        IconButton(onClick = onExitGame) {
                            Text("✕", color = TextSecondary, fontSize = 20.sp)
                        }
                    }
                }

                roundResultText?.let { res ->
                    Text(
                        text = res,
                        color = if (res.contains("WICKET")) Color(0xFFFF0055) else Color(0xFF00E676),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // 180° Rotated Top Zone for Opposite Player in Split-Screen mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color(0xFFFF007F).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.rotate(if (selectedGameMode == HandCricketGameMode.SPLIT_SCREEN) 180f else 0f)
                    ) {
                        Text(
                            text = "BOWLER (1 - 6)",
                            color = Color(0xFFFF007F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..6).forEach { num ->
                                val isSel = p2Choice == num
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSel) Color(0xFFFF007F) else SurfaceGlassDark)
                                        .border(1.dp, Color(0xFFFF007F), CircleShape)
                                        .clickable {
                                            haptics.performTick(composeHaptics)
                                            p2Choice = num
                                            evaluateRound()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$num",
                                        color = if (isSel) Color.White else TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Zone for Batter
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color(0xFF00F2FE).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BATTER (1 - 6)",
                            color = Color(0xFF00F2FE),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..6).forEach { num ->
                                val isSel = p1Choice == num
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSel) Color(0xFF00F2FE) else SurfaceGlassDark)
                                        .border(1.dp, Color(0xFF00F2FE), CircleShape)
                                        .clickable {
                                            haptics.performTick(composeHaptics)
                                            p1Choice = num
                                            evaluateRound()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$num",
                                        color = if (isSel) Color.Black else TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Match Result & Victory Ceremony
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "MATCH COMPLETED! 🏆",
                    color = Color(0xFFFFD166),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WINNER: $winnerName 🎉",
                    color = Color(0xFF00E676),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        matchGameOver = false
                        isInnings1 = true
                        batterScore = 0
                        wicketsLost = 0
                        p1Choice = null
                        p2Choice = null
                        roundResultText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("PLAY REMATCH 🔄", color = Color.Black, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onExitGame) {
                    Text("Back to Hub", color = TextMuted)
                }
            }
        }
    }
}
