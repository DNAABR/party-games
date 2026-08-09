package com.leminno.partygames.ui.hub.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.model.PartySessionManager
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyCompanionSheet(
    onDismissRequest: () -> Unit
) {
    val players by PartySessionManager.players.collectAsState()
    val savedRoster by UserPreferencesRepository.savedRoster.collectAsState()

    var newPlayerName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Leaderboard, 1 = Rosters

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = SurfaceGlassDark,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PARTY COMPANION",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "GLOBAL SESSION SCOREKEEPER",
                            color = Color(0xFF00F2FE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Text("✕", color = TextSecondary, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Buttons: Scoreboard vs Rosters
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF00F2FE)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("SCOREBOARD", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("SAVED ROSTERS", fontWeight = FontWeight.Bold) }
                )
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
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F2FE),
                            unfocusedBorderColor = BorderGlassDefault,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newPlayerName.isNotBlank()) {
                                val currentNames = players.map { it.name }.toMutableList()
                                currentNames.add(newPlayerName.trim())
                                PartySessionManager.updatePlayers(currentNames)
                                newPlayerName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("+ ADD", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scoreboard List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(players) { player ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceGlassDark)
                                .border(1.dp, BorderGlassDefault, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = player.name,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { PartySessionManager.decrementScore(player.name) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("-", color = Color(0xFFFF0055), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    }

                                    Text(
                                        text = "${player.score} pts",
                                        color = Color(0xFF00F2FE),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = { PartySessionManager.incrementScore(player.name) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("+", color = Color(0xFF00E676), fontSize = 20.sp, fontWeight = FontWeight.Black)
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
                        Text("Reset Scores 🔄", color = TextSecondary)
                    }

                    TextButton(onClick = {
                        UserPreferencesRepository.saveRoster(players.map { it.name })
                    }) {
                        Text("Save Roster 💾", color = Color(0xFF00F2FE))
                    }
                }
            } else {
                // Saved Rosters Tab
                if (savedRoster.isEmpty()) {
                    Text(
                        text = "No saved rosters yet. Save current active player names from the Scoreboard tab!",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SAVED PLAYER ROSTER",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceGlassDark)
                                .border(1.dp, BorderGlassDefault, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = savedRoster.joinToString(", "),
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        PartySessionManager.updatePlayers(savedRoster)
                                        selectedTab = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("LOAD ROSTER TO SCOREBOARD", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
