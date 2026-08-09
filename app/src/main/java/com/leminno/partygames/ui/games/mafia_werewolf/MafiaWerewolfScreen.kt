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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.theme.*

@Composable
fun MafiaWerewolfScreen(
    playerCount: Int = 6,
    viewModel: MafiaWerewolfViewModel = viewModel(),
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    var selectedMode by remember { mutableStateOf<String?>(null) } // null, LOCAL, REMOTE
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }

    GameScaffold(
        title = "MAFIA / WEREWOLF 🌙",
        titleColor = Color(0xFF9D4EDD),
        gameId = "mafia_werewolf",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedMode == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SELECT PLAY MODE", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF9D4EDD), RoundedCornerShape(20.dp))
                            .clickable {
                                selectedMode = "LOCAL"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = Color(0xFF9D4EDD), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Pass single phone around for secret role cards", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                            .clickable {
                                selectedMode = "REMOTE"
                                showRemoteSheet = true
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Remote Play (Multi-Device)", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Secret role cards delivered directly to separate phone screens", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else if (uiState.gamePhase == "ROLE_ASSIGNMENT") {
                // Role Assignment Phase
                val currentPlayer = uiState.playersState.getOrNull(uiState.currentPlayerIndex)
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
                                    viewModel.toggleRoleRevealed()
                                }
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!uiState.isRoleRevealed) {
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
                            viewModel.nextRoleAssignment()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = uiState.isRoleRevealed
                    ) {
                        Text(
                            text = if (uiState.currentPlayerIndex + 1 < uiState.playersState.size) "NEXT PLAYER ▶" else "START NIGHT PHASE 🌙",
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            } else if (uiState.gamePhase == "NIGHT") {
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audio Masking Bar
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
                                checked = uiState.audioMaskingActive,
                                onCheckedChange = { viewModel.toggleAudioMasking() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF9D4EDD))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                            when (uiState.nightStep) {
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

                    // Player Selection Matrix
                    LazyColumn(
                        modifier = Modifier.height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.playersState.filter { it.isAlive }) { player ->
                            val isSelected = (uiState.nightStep == 1 && uiState.nightEliminatedTarget?.name == player.name) ||
                                    (uiState.nightStep == 2 && uiState.nightSavedTarget?.name == player.name)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0x449D4EDD) else SurfaceGlassDark)
                                    .border(1.dp, if (isSelected) Color(0xFF9D4EDD) else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptics.performPop()
                                        viewModel.selectNightTarget(player)
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(player.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    if (isSelected) {
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
                        viewModel.nextNightStep()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (uiState.nightStep < 3) "NEXT NARRATOR STEP ▶" else "AWAKEN TOWN (DAY) ☀️",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (uiState.gamePhase == "DAY_DISCUSSION") {
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
                        text = uiState.nightLogMessage,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("VOTE SUSPECT TO ELIMINATE", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.playersState) { player ->
                            val isSelectedForVote = uiState.daySelectedVoteTarget?.name == player.name

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            !player.isAlive -> Color.Red.copy(alpha = 0.15f)
                                            isSelectedForVote -> Color(0x55FF0055)
                                            else -> SurfaceGlassDark
                                        }
                                    )
                                    .border(1.dp, if (isSelectedForVote) Color(0xFFFF0055) else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable(enabled = player.isAlive) {
                                        haptics.performPop()
                                        viewModel.selectDayVoteTarget(player)
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(player.name, color = if (player.isAlive) TextPrimary else TextMuted, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (player.isAlive) (if (isSelectedForVote) "VOTE TARGET 🎯" else "ALIVE 💚") else "ELIMINATED ☠️",
                                        color = if (player.isAlive) (if (isSelectedForVote) Color(0xFFFF0055) else Color(0xFF00E676)) else Color(0xFFFF0055),
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
                        viewModel.confirmDayElimination()
                    },
                    enabled = uiState.daySelectedVoteTarget != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("CONFIRM DAY ELIMINATION ☠️", color = Color.White, fontWeight = FontWeight.Black)
                }
            } else {
                // Game Over View
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
                        text = uiState.winnerTeam,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        viewModel.startNewMatch()
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
        }

        if (showRemoteSheet) {
            com.leminno.partygames.ui.components.RemoteRoomSetupSheet(
                gameId = "mafia_werewolf",
                gameName = "Mafia / Werewolf 🌙",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) selectedMode = null
                },
                onRoomJoined = { code, _, _ ->
                    roomCode = code
                    selectedMode = "REMOTE"
                    showRemoteSheet = false
                }
            )
        }
    }
}
