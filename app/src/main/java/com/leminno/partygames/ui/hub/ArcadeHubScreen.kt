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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import com.leminno.partygames.data.model.GameCategory
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.hub.components.GameCard
import com.leminno.partygames.ui.hub.components.PartyCompanionSheet
import com.leminno.partygames.ui.hub.components.PreGameGuideSheet
import com.leminno.partygames.ui.hub.components.QuickSettingsSheet
import com.leminno.partygames.ui.model.GameItem
import com.leminno.partygames.ui.theme.*

@Composable
fun ArcadeHubScreen(
    initialRoomCode: String? = null,
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
    var showRemoteRoomSheet by remember { mutableStateOf(initialRoomCode != null) }

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

            // Header Bar & QoL Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PARTY GAMES",
                            color = TextPrimary,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                color = AccentCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "CHOOSE YOUR CHAOS",
                        color = AccentCyan,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Remote Room Code Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark)
                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(12.dp))
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performTick(composeHaptics)
                                showRemoteRoomSheet = true
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = "Remote Room",
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Party Scorekeeper Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark)
                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(12.dp))
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performTick(composeHaptics)
                                showPartyCompanion = true
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = "Scorekeeper",
                                tint = WinGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark)
                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(12.dp))
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performTick(composeHaptics)
                                showQuickSettings = true
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Play Hero Banner Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AccentCyan,
                                AccentViolet,
                                AccentMagenta
                            )
                        )
                    )
                    .clickable {
                        haptics.performPop()
                        val mvpGames = GameCatalogRepository.allGames.filter { it.isMvp }
                        selectedGameForSheet = mvpGames.random()
                    }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "QUICK PLAY",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Random 5-Min Challenge",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PLAY NOW",
                                color = TextOnAccent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = TextOnAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Recently Played Carousel (if available)
            if (recentGames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "RECENTLY PLAYED",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
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
                                .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
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
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search games...", color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceGlassDark),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = BorderGlassDefault,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category & Favorites Scrollable Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        label = "All",
                        icon = "🌟",
                        isSelected = selectedCategory == null && !showOnlyFavorites,
                        accentColor = AccentCyan,
                        onClick = {
                            haptics.performTick(composeHaptics)
                            selectedCategory = null
                            showOnlyFavorites = false
                        }
                    )
                }

                item {
                    CategoryChip(
                        label = "Favorites",
                        icon = "❤️",
                        isSelected = showOnlyFavorites,
                        accentColor = Color(0xFFFF2A6D),
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showOnlyFavorites = !showOnlyFavorites
                            if (showOnlyFavorites) selectedCategory = null
                        }
                    )
                }

                items(GameCategory.entries.toTypedArray()) { category ->
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

            Spacer(modifier = Modifier.height(8.dp))

            // Player Count Pill Selector Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    listOf("ALL" to "Any Size", "2P" to "2 Players", "3-6P" to "3-6 Group", "8P+" to "8+ Party")
                ) { (code, label) ->
                    val isSel = selectedPlayerFilter == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) AccentCyan.copy(alpha = 0.2f) else SurfaceGlassDark)
                            .border(1.dp, if (isSel) AccentCyan else BorderGlassDefault, RoundedCornerShape(10.dp))
                            .clickable {
                                haptics.performTick(composeHaptics)
                                selectedPlayerFilter = code
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) AccentCyan else TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Games Grid or Empty State
            if (filteredGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No games found",
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search or category filter",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceGlassDark)
                                .border(1.dp, BorderGlassDefault, RoundedCornerShape(12.dp))
                                .clickable {
                                    searchQuery = ""
                                    selectedCategory = null
                                    showOnlyFavorites = false
                                    selectedPlayerFilter = "ALL"
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "RESET FILTERS",
                                color = AccentCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
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

        // Remote Room Join Sheet
        if (showRemoteRoomSheet) {
            com.leminno.partygames.ui.components.RemoteRoomSetupSheet(
                gameId = "hand_cricket",
                gameName = "Remote Multiplayer Room 🌐",
                initialRoomCode = initialRoomCode,
                onDismiss = { showRemoteRoomSheet = false },
                onRoomJoined = { roomCode, isHost, _ ->
                    showRemoteRoomSheet = false
                    onLaunchGame("hand_cricket", 2, 60)
                }
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
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else SurfaceGlassDark)
            .border(1.dp, if (isSelected) accentColor else BorderGlassDefault, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) accentColor else TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
