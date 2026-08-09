package com.leminno.partygames.ui.games.mafia_werewolf

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
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay

enum class MafiaRole(val title: String, val team: String, val icon: String, val desc: String) {
    MAFIA("Mafia", "Mafia", "🕶️", "Wake up at night and choose a victim to eliminate."),
    DOCTOR("Doctor", "Civilians", "🩺", "Wake up at night and save 1 player from elimination."),
    DETECTIVE("Detective", "Civilians", "🕵️", "Wake up at night to inspect 1 suspect's team."),
    CIVILIAN("Civilian", "Civilians", "🧑", "Participate in day discussions and vote out suspected Mafia.")
}

data class PlayerRoleState(
    val name: String,
    val role: MafiaRole,
    val isAlive: Boolean = true
)

@Composable
fun MafiaWerewolfScreen(
    playerCount: Int = 6,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    var gamePhase by remember { mutableStateOf("ROLE_ASSIGNMENT") } // ROLE_ASSIGNMENT, NIGHT, DAY_DISCUSSION, GAME_OVER
    var currentPlayerIndex by remember { mutableIntStateOf(0) }
    var isRoleRevealed by remember { mutableStateOf(false) }

    val rolesList = remember(playerCount) {
        val baseRoles = mutableListOf(MafiaRole.MAFIA, MafiaRole.DOCTOR, MafiaRole.DETECTIVE)
        while (baseRoles.size < playerCount) {
            baseRoles.add(MafiaRole.CIVILIAN)
        }
        baseRoles.shuffled()
    }

    var playersState by remember(rolesList) {
        mutableStateOf(rolesList.mapIndexed { idx, role -> PlayerRoleState("Player ${idx + 1}", role) })
    }

    var nightStep by remember { mutableIntStateOf(1) } // 1: Mafia, 2: Doctor, 3: Detective
    var nightEliminatedTarget by remember { mutableStateOf<PlayerRoleState?>(null) }
    var nightSavedTarget by remember { mutableStateOf<PlayerRoleState?>(null) }
    var nightLogMessage by remember { mutableStateOf("") }
    var audioMaskingActive by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C15))
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
                    text = "MAFIA / WEREWOLF 🌙",
                    color = Color(0xFF9D4EDD),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            if (gamePhase == "ROLE_ASSIGNMENT") {
                // Role Assignment Phase
                val currentPlayer = playersState.getOrNull(currentPlayerIndex)
                if (currentPlayer != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PASS PHONE TO ${currentPlayer.name.uppercase()}",
                            color = Color(0xFF9D4EDD),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceGlassDark)
                                .border(2.dp, Color(0xFF9D4EDD), RoundedCornerShape(24.dp))
                                .clickable {
                                    haptics.performPop()
                                    isRoleRevealed = !isRoleRevealed
                                }
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isRoleRevealed) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔒", fontSize = 52.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "TAP CARD TO VIEW ROLE",
                                        color = Color(0xFF9D4EDD),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ensure other players are not looking!",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(currentPlayer.role.icon, fontSize = 56.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentPlayer.role.title,
                                        color = Color(0xFF9D4EDD),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Team: ${currentPlayer.role.team}",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = currentPlayer.role.desc,
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            isRoleRevealed = false
                            if (currentPlayerIndex + 1 < playersState.size) {
                                currentPlayerIndex++
                            } else {
                                gamePhase = "NIGHT"
                                nightStep = 1
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isRoleRevealed
                    ) {
                        Text(
                            text = if (currentPlayerIndex + 1 < playersState.size) "NEXT PLAYER ▶" else "START NIGHT PHASE 🌙",
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            } else if (gamePhase == "NIGHT") {
                // Automated Narrator Night Phase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "NIGHT PHASE 🌙",
                        color = Color(0xFF9D4EDD),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "All players close eyes! App narrator is active.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Night Audio Masking Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x339D4EDD))
                            .border(1.dp, Color(0xFF9D4EDD), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔊 Audio Tap-Sound Masking Active", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = audioMaskingActive,
                                onCheckedChange = { audioMaskingActive = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF9D4EDD))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Step Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when (nightStep) {
                                1 -> {
                                    Text("🕶️ MAFIA WAKE UP", color = Color(0xFFFF0055), fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Select a player to eliminate secretly.", color = TextMuted, fontSize = 12.sp)
                                }
                                2 -> {
                                    Text("🩺 DOCTOR WAKE UP", color = Color(0xFF00E676), fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Select 1 player to save from attack.", color = TextMuted, fontSize = 12.sp)
                                }
                                3 -> {
                                    Text("🕵️ DETECTIVE WAKE UP", color = Color(0xFF00F2FE), fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Select 1 player to inspect their team alignment.", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player Selection Matrix for Night Action
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playersState.filter { it.isAlive }) { player ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceGlassDark)
                                    .clickable {
                                        haptics.performPop()
                                        when (nightStep) {
                                            1 -> nightEliminatedTarget = player
                                            2 -> nightSavedTarget = player
                                            3 -> nightLogMessage = "${player.name} is ${player.role.team}!"
                                        }
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(player.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    if ((nightStep == 1 && nightEliminatedTarget == player) ||
                                        (nightStep == 2 && nightSavedTarget == player)
                                    ) {
                                        Text("SELECTED ✓", color = Color(0xFF9D4EDD), fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        haptics.performPop()
                        if (nightStep < 3) {
                            nightStep++
                        } else {
                            // Resolve Night Results
                            val eliminated = nightEliminatedTarget
                            val saved = nightSavedTarget

                            if (eliminated != null && eliminated != saved) {
                                playersState = playersState.map {
                                    if (it.name == eliminated.name) it.copy(isAlive = false) else it
                                }
                                nightLogMessage = "Night Over! ${eliminated.name} was eliminated!"
                            } else {
                                nightLogMessage = "Night Over! The Doctor saved the victim! No one died!"
                            }

                            // Check Win Condition
                            val mafiaAlive = playersState.count { it.isAlive && it.role == MafiaRole.MAFIA }
                            val townAlive = playersState.count { it.isAlive && it.role != MafiaRole.MAFIA }

                            if (mafiaAlive == 0 || mafiaAlive >= townAlive) {
                                gamePhase = "GAME_OVER"
                            } else {
                                gamePhase = "DAY_DISCUSSION"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (nightStep < 3) "NEXT NARRATOR STEP ▶" else "AWAKEN TOWN (DAY) ☀️",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (gamePhase == "DAY_DISCUSSION") {
                // Day Discussion & Elimination Voting
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DAY PHASE ☀️",
                        color = Color(0xFFFFD166),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = nightLogMessage,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("ALIVE PLAYERS", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playersState) { player ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (player.isAlive) SurfaceGlassDark else Color.Red.copy(alpha = 0.15f))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(player.name, color = if (player.isAlive) TextPrimary else TextMuted, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (player.isAlive) "ALIVE 💚" else "ELIMINATED ☠️",
                                        color = if (player.isAlive) Color(0xFF00E676) else Color(0xFFFF0055),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        haptics.performHeavyBurst()
                        nightStep = 1
                        nightEliminatedTarget = null
                        nightSavedTarget = null
                        gamePhase = "NIGHT"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("BEGIN NEXT NIGHT PHASE 🌙", color = Color.White, fontWeight = FontWeight.Black)
                }
            } else {
                // Game Over Screen
                val mafiaAlive = playersState.count { it.isAlive && it.role == MafiaRole.MAFIA }
                val winnerTeam = if (mafiaAlive > 0) "MAFIA WINS 🕶️" else "CIVILIANS WIN 👑"

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GAME OVER!",
                        color = Color(0xFF9D4EDD),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = winnerTeam,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        gamePhase = "ROLE_ASSIGNMENT"
                        currentPlayerIndex = 0
                        isRoleRevealed = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("PLAY NEW MATCH ▶", color = Color.White, fontWeight = FontWeight.Black)
                }
            }

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
