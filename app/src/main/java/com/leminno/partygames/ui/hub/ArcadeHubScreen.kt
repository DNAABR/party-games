package com.leminno.partygames.ui.hub

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.GameCatalogRepository
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.hub.components.GameCard
import com.leminno.partygames.ui.hub.components.PartyCompanionSheet
import com.leminno.partygames.ui.hub.components.PreGameGuideSheet
import com.leminno.partygames.ui.hub.components.QuickSettingsSheet
import com.leminno.partygames.ui.model.GameItem
import com.leminno.partygames.data.model.GameCategory
import com.leminno.partygames.ui.theme.*

@Composable
fun ArcadeHubScreen(
    onLaunchGame: (gameId: String, playerCount: Int, timerSec: Int) -> Unit
) {
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    val haptics = remember { HapticFeedbackManager(context) }

    LaunchedEffect(Unit) {
        UserPreferencesRepository.init(context)
    }

    val favoriteGameIds by UserPreferencesRepository.favoriteGameIds.collectAsState()
    val recentGameIds by UserPreferencesRepository.recentGameIds.collectAsState()

    var selectedCategory by remember { mutableStateOf<GameCategory?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var selectedPlayerFilter by remember { mutableStateOf<String>("ALL") } // ALL, 2P, 3-6P, 8P+
    var searchQuery by remember { mutableStateOf("") }

    var selectedGameForSheet by remember { mutableStateOf<GameItem?>(null) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showPartyCompanion by remember { mutableStateOf(false) }

    val recentGames = remember(recentGameIds) {
        recentGameIds.mapNotNull { id -> GameCatalogRepository.allGames.find { it.id == id } }
    }

    // Filter games list
    val filteredGames = remember(selectedCategory, showOnlyFavorites, favoriteGameIds, selectedPlayerFilter, searchQuery) {
        GameCatalogRepository.allGames.filter { game ->
            val matchesCategory = selectedCategory == null || game.category == selectedCategory
            val matchesFavorites = !showOnlyFavorites || favoriteGameIds.contains(game.id)
            val matchesSearch = searchQuery.isEmpty() ||
                    game.title.contains(searchQuery, ignoreCase = true) ||
                    game.description.contains(searchQuery, ignoreCase = true)
            val matchesPlayers = when (selectedPlayerFilter) {
                "2P" -> game.minPlayers <= 2 && game.maxPlayers >= 2
                "3-6P" -> game.minPlayers <= 6 && game.maxPlayers >= 3
                "8P+" -> game.maxPlayers >= 8
                else -> true
            }
            matchesCategory && matchesFavorites && matchesSearch && matchesPlayers
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Console Title Header & Arcade Emblem + QoL Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PARTY GAMES 🕹️",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CHOOSE YOUR CHAOS",
                        color = Color(0xFF00F2FE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Party Scorekeeper Button
                    IconButton(onClick = {
                        haptics.performTick(composeHaptics)
                        showPartyCompanion = true
                    }) {
                        Text("🏆", fontSize = 20.sp)
                    }

                    // Settings Button
                    IconButton(onClick = {
                        haptics.performTick(composeHaptics)
                        showQuickSettings = true
                    }) {
                        Text("⚙️", fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Play Chaos Hero Banner Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00F2FE),
                                Color(0xFF9D4EDD),
                                Color(0xFFFF007F)
                            )
                        )
                    )
                    .clickable {
                        haptics.performPop()
                        val mvpGames = GameCatalogRepository.allGames.filter { it.isMvp }
                        selectedGameForSheet = mvpGames.random()
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "QUICK PLAY",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Random 5-Min Challenge",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "PLAY NOW ▶",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Recently Played Carousel (if available)
            if (recentGames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "🕒 RECENTLY PLAYED",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recentGames, key = { "recent_${it.id}" }) { game ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceGlassDark)
                                .border(1.dp, Color(0xFF00F2FE).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    haptics.performTick(composeHaptics)
                                    selectedGameForSheet = game
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = game.category.iconSymbol, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = game.title,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search games...", color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceGlassDark),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F2FE),
                    unfocusedBorderColor = BorderGlassDefault,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category & Favorites Filter Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ALL Chip
                CategoryChip(
                    label = "All",
                    icon = "🌟",
                    isSelected = selectedCategory == null && !showOnlyFavorites,
                    accentColor = Color(0xFF00F2FE),
                    onClick = {
                        haptics.performTick(composeHaptics)
                        selectedCategory = null
                        showOnlyFavorites = false
                    }
                )

                // Favorites Chip
                CategoryChip(
                    label = "Favs",
                    icon = "❤️",
                    isSelected = showOnlyFavorites,
                    accentColor = Color(0xFFFF0055),
                    onClick = {
                        haptics.performTick(composeHaptics)
                        showOnlyFavorites = !showOnlyFavorites
                        if (showOnlyFavorites) selectedCategory = null
                    }
                )

                GameCategory.entries.forEach { category ->
                    val token = CategoryThemeToken.forCategory(category)
                    CategoryChip(
                        label = category.title.split(" ").first(),
                        icon = category.iconSymbol,
                        isSelected = selectedCategory == category && !showOnlyFavorites,
                        accentColor = token.primaryAccent,
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showOnlyFavorites = false
                            selectedCategory = if (selectedCategory == category) null else category
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Count Pill Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL" to "Any Size", "2P" to "2 Players", "3-6P" to "3-6 Group", "8P+" to "8+ Party").forEach { (code, label) ->
                    val isSel = selectedPlayerFilter == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) Color(0x3300F2FE) else SurfaceGlassDark)
                            .border(1.dp, if (isSel) Color(0xFF00F2FE) else BorderGlassDefault, RoundedCornerShape(10.dp))
                            .clickable {
                                haptics.performTick(composeHaptics)
                                selectedPlayerFilter = code
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color(0xFF00F2FE) else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Games Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredGames, key = { it.id }) { game ->
                    val isFav = favoriteGameIds.contains(game.id)
                    GameCard(
                        game = game,
                        isFavorite = isFav,
                        onToggleFavorite = {
                            haptics.performTick(composeHaptics)
                            UserPreferencesRepository.toggleFavorite(game.id)
                        },
                        onClick = {
                            haptics.performTick(composeHaptics)
                            selectedGameForSheet = game
                        }
                    )
                }
            }
        }

        // Pre-Game Sheet Modal
        selectedGameForSheet?.let { game ->
            PreGameGuideSheet(
                game = game,
                onDismissRequest = { selectedGameForSheet = null },
                onStartGame = { playerCount, roundTimerSec, _ ->
                    val gameToLaunch = game.id
                    selectedGameForSheet = null
                    UserPreferencesRepository.addRecentlyPlayed(gameToLaunch)
                    onLaunchGame(gameToLaunch, playerCount, roundTimerSec)
                }
            )
        }

        // Quick Settings Modal
        if (showQuickSettings) {
            QuickSettingsSheet(
                onDismissRequest = { showQuickSettings = false }
            )
        }

        // Party Companion Modal
        if (showPartyCompanion) {
            PartyCompanionSheet(
                onDismissRequest = { showPartyCompanion = false }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    icon: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.25f) else SurfaceGlassDark)
            .border(1.dp, if (isSelected) accentColor else BorderGlassDefault, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) accentColor else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
