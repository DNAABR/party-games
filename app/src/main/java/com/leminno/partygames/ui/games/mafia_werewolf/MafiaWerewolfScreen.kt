package com.leminno.partygames.ui.games.mafia_werewolf

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
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

    var selectedMode by remember { mutableStateOf<String?>(null) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }

    GameScaffold(
        title = "Mafia / Werewolf",
        titleColor = TextPrimary,
        gameId = "mafia_werewolf",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedMode == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Select Play Mode", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Choose single phone pass or multi-device roles", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedMode = "LOCAL"
                            }
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MysteryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📱", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("Pass single phone around for secret role cards", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedMode = "REMOTE"
                                showRemoteSheet = true
                            }
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ActionContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🌐", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Remote Play (Multi-Device)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("Secret role cards delivered to each player's phone", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandPrimaryContainer)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Pass Phone to ${currentPlayer.name}",
                                color = BrandPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceLight)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                                .clickable {
                                    haptics.performPop()
                                    viewModel.toggleRoleRevealed()
                                }
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!uiState.isRoleRevealed) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(MysteryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🔒", fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Tap Card to View Role",
                                        color = TextPrimary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Ensure other players are not looking!",
                                        color = TextSecondary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(currentPlayer.role.icon, fontSize = 52.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentPlayer.role.title,
                                        color = TextPrimary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MysteryContainer)
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Team: ${currentPlayer.role.team}",
                                            color = MysteryText,
                                            fontFamily = ModernSansFont,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = currentPlayer.role.desc,
                                        color = TextSecondary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    PrimaryPartyButton(
                        text = if (uiState.currentPlayerIndex + 1 < uiState.playersState.size) "Next Player ▶" else "Start Night Phase 🌙",
                        accentColor = BrandPrimary,
                        onClick = {
                            haptics.performPop()
                            viewModel.nextRoleAssignment()
                        },
                        enabled = uiState.isRoleRevealed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (uiState.gamePhase == "NIGHT") {
                // Automated Narrator Night Phase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "Night Phase 🌙",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "All players close eyes! App narrator is active.",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Masking Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 1.dp, shape = RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔊 Tap Sound Masking", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = uiState.audioMaskingActive,
                                onCheckedChange = { viewModel.toggleAudioMasking() },
                                colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary, checkedTrackColor = BrandPrimaryContainer)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when (uiState.nightStep) {
                                1 -> {
                                    Text("🕶️ Mafia Wake Up", color = AlertRed, fontFamily = ModernSansFont, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Select a player to eliminate secretly.", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp)
                                }
                                2 -> {
                                    Text("🩺 Doctor Wake Up", color = SuccessGreen, fontFamily = ModernSansFont, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Select 1 player to save from attack.", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp)
                                }
                                3 -> {
                                    Text("🕵️ Detective Wake Up", color = ActionText, fontFamily = ModernSansFont, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Select 1 player to inspect team alignment.", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) BrandPrimaryContainer else SurfaceLight)
                                    .border(1.dp, if (isSelected) BrandPrimary else BorderSubtle, RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptics.performPop()
                                        viewModel.selectNightTarget(player)
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(player.name, color = TextPrimary, fontFamily = ModernSansFont, fontWeight = FontWeight.SemiBold)
                                    if (isSelected) {
                                        Text("Selected ✓", color = BrandPrimary, fontFamily = ModernSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                PrimaryPartyButton(
                    text = if (uiState.nightStep < 3) "Next Narrator Step ▶" else "Awaken Town (Day) ☀️",
                    accentColor = BrandPrimary,
                    onClick = {
                        haptics.performPop()
                        viewModel.nextNightStep()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (uiState.gamePhase == "DAY_DISCUSSION") {
                // Day Discussion & Elimination Voting
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "Day Phase ☀️",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BoardContainer)
                            .border(1.dp, BoardBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.nightLogMessage,
                            color = BoardText,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Vote Suspect To Eliminate", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.height(190.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.playersState) { player ->
                            val isSelectedForVote = uiState.daySelectedVoteTarget?.name == player.name

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            !player.isAlive -> SurfaceSubtle
                                            isSelectedForVote -> AlertContainer
                                            else -> SurfaceLight
                                        }
                                    )
                                    .border(1.dp, if (isSelectedForVote) AlertRed else BorderSubtle, RoundedCornerShape(14.dp))
                                    .clickable(enabled = player.isAlive) {
                                        haptics.performPop()
                                        viewModel.selectDayVoteTarget(player)
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(player.name, color = if (player.isAlive) TextPrimary else TextMuted, fontFamily = ModernSansFont, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = if (player.isAlive) (if (isSelectedForVote) "Vote Target 🎯" else "Alive 💚") else "Eliminated ☠️",
                                        color = if (player.isAlive) (if (isSelectedForVote) AlertRed else SuccessGreen) else TextMuted,
                                        fontFamily = ModernSansFont,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                PrimaryPartyButton(
                    text = "Confirm Day Elimination ☠️",
                    accentColor = AlertRed,
                    onClick = {
                        haptics.performHeavyBurst()
                        viewModel.confirmDayElimination()
                    },
                    enabled = uiState.daySelectedVoteTarget != null,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                VictoryCeremonyOverlay(
                    winnerTitle = uiState.winnerTeam,
                    subtitle = "Mafia / Werewolf Match Complete",
                    onPlayAgain = { viewModel.startNewMatch() },
                    onBackToHub = onExitGame
                )
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "mafia_werewolf",
                gameName = "Mafia / Werewolf",
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

