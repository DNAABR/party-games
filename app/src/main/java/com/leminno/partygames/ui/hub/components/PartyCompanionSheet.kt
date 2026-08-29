package com.leminno.partygames.ui.hub.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.model.PartySessionManager
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.SecondaryPartyButton
import com.leminno.partygames.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyCompanionSheet(
    onDismissRequest: () -> Unit
) {
    val players by PartySessionManager.players.collectAsState()
    val savedRoster by UserPreferencesRepository.savedRoster.collectAsState()

    var newPlayerName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Leaderboard, 1 = Rosters

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = SurfaceLight,
        scrimColor = Color(0x660F172A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WarningContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏆", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Party Companion",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Scorekeeper & Rosters",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                ) {
                    Icon(
                        imageVector = PixelIcons.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Buttons: Scoreboard vs Rosters (Segmented Control Pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceSubtle)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 0) BrandPrimary else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Scoreboard",
                        color = if (selectedTab == 0) TextOnPrimary else TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) BrandPrimary else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Saved Rosters",
                        color = if (selectedTab == 1) TextOnPrimary else TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Add New Player Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        placeholder = { Text("Enter player name...", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceLight,
                            unfocusedContainerColor = SurfaceLight
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    PrimaryPartyButton(
                        text = "+ Add",
                        onClick = {
                            if (newPlayerName.isNotBlank()) {
                                val currentNames = players.map { it.name }.toMutableList()
                                currentNames.add(newPlayerName.trim())
                                PartySessionManager.updatePlayers(currentNames)
                                newPlayerName = ""
                            }
                        },
                        accentColor = BrandPrimary,
                        modifier = Modifier.height(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scoreboard List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                ) {
                    items(players) { player ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceSubtle)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = player.name,
                                    color = TextPrimary,
                                    fontFamily = ModernSansFont,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { PartySessionManager.decrementScore(player.name) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceLight)
                                    ) {
                                        Text("-", color = AlertRed, fontFamily = ModernSansFont, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = "${player.score} pts",
                                        color = BrandPrimary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    IconButton(
                                        onClick = { PartySessionManager.incrementScore(player.name) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceLight)
                                    ) {
                                        Text("+", color = SuccessGreen, fontFamily = ModernSansFont, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { PartySessionManager.resetScores() }) {
                        Text("Reset Scores 🔄", color = TextSecondary, fontFamily = ModernSansFont, fontWeight = FontWeight.Medium)
                    }

                    TextButton(onClick = {
                        UserPreferencesRepository.saveRoster(players.map { it.name })
                    }) {
                        Text("Save Roster 💾", color = BrandPrimary, fontFamily = ModernSansFont, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // Saved Rosters Tab
                if (savedRoster.isEmpty()) {
                    Text(
                        text = "No saved rosters yet. Save current active player names from the Scoreboard tab!",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Saved Player Roster",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceSubtle)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = savedRoster.joinToString(", "),
                                    color = TextPrimary,
                                    fontFamily = ModernSansFont,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                PrimaryPartyButton(
                                    text = "Load Roster to Scoreboard",
                                    onClick = {
                                        PartySessionManager.updatePlayers(savedRoster)
                                        selectedTab = 0
                                    },
                                    accentColor = BrandPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

