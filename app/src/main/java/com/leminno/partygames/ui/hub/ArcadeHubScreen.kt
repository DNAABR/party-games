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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
    var selectedPlayerFilter by remember { mutableStateOf<String>("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    var selectedGameForSheet by remember { mutableStateOf<GameItem?>(null) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showPartyCompanion by remember { mutableStateOf(false) }
    var showRemoteRoomSheet by remember { mutableStateOf(initialRoomCode != null) }

    val recentGames = remember(recentGameIds) {
        recentGameIds.mapNotNull { id -> GameCatalogRepository.allGames.find { it.id == id } }
    }

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
        modifier = Modifier
            .fillMaxSize()
            .background(PixelVioletDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Arcade Header Marquee & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ARCADE HUB",
                            color = PixelCrtCyan,
                            fontFamily = PressStart2PFont,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(0.dp))
                                .background(PixelMagentaHot)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "8-BIT",
                                color = PixelOutlineBlack,
                                fontFamily = PressStart2PFont,
                                fontSize = 7.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CHOOSE YOUR GAME",
                        color = PixelMagentaHot,
                        fontFamily = PressStart2PFont,
                        fontSize = 8.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Remote Room Code Button
                    IconButton(
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showRemoteRoomSheet = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(PixelVioletElevated)
                    ) {
                        Icon(
                            imageVector = PixelIcons.Users,
                            contentDescription = "Remote Room",
                            tint = PixelCrtCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Scorekeeper Button
                    IconButton(
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showPartyCompanion = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(PixelVioletElevated)
                    ) {
                        Icon(
                            imageVector = PixelIcons.Trophy,
                            contentDescription = "Scorekeeper",
                            tint = PixelAmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showQuickSettings = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(PixelVioletElevated)
                    ) {
                        Icon(
                            imageVector = PixelIcons.Sliders,
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Play Hero Arcade Push Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                    .background(
                        brush = pixelBandedHorizontal(
                            listOf(
                                PixelMagentaHighlight,
                                PixelMagentaHot,
                                PixelMagentaShadow
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .clickable {
                        haptics.performPop()
                        val mvpGames = GameCatalogRepository.allGames.filter { it.isMvp }
                        selectedGameForSheet = mvpGames.random()
                    }
                    .padding(horizontal = 14.dp),
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
                                .border(1.5.dp, PixelOutlineBlack)
                                .background(PixelAmberGold)
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = PixelIcons.Zap,
                                contentDescription = null,
                                tint = PixelOutlineBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "QUICK PLAY",
                                color = PixelOutlineBlack,
                                fontFamily = PressStart2PFont,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "RANDOM 5-MIN PARTY",
                                color = PixelOutlineBlack.copy(alpha = 0.8f),
                                fontFamily = PressStart2PFont,
                                fontSize = 7.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(PixelCrtCyan)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "INSERT COIN",
                            color = PixelOutlineBlack,
                            fontFamily = PressStart2PFont,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            // Recently Played Carousel (if available)
            if (recentGames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "RECENT CABINETS",
                    color = TextMuted,
                    fontFamily = PressStart2PFont,
                    fontSize = 8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recentGames, key = { "recent_${it.id}" }) { game ->
                        val catIcon = when (game.category) {
                            GameCategory.TRIVIA -> PixelIcons.Lightbulb
                            GameCategory.ACTION -> PixelIcons.Zap
                            GameCategory.MYSTERY -> PixelIcons.Eye
                            GameCategory.BOARD -> PixelIcons.Dice
                        }
                        Box(
                            modifier = Modifier
                                .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                                .background(
                                    brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                                )
                                .clickable {
                                    haptics.performTick(composeHaptics)
                                    selectedGameForSheet = game
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = null,
                                    tint = PixelCrtCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = game.title.uppercase(),
                                    color = TextPrimary,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pixel Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("SEARCH GAMES...", color = TextMuted, fontFamily = PressStart2PFont, fontSize = 8.sp) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = PixelIcons.Close,
                                contentDescription = "Clear search",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                    .background(PixelVioletDark),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixelCrtCyan,
                    unfocusedBorderColor = PixelOutlineBlack,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category & Favorites Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        label = "ALL",
                        icon = PixelIcons.ArcadeJoystick,
                        isSelected = selectedCategory == null && !showOnlyFavorites,
                        accentColor = PixelCrtCyan,
                        onClick = {
                            haptics.performTick(composeHaptics)
                            selectedCategory = null
                            showOnlyFavorites = false
                        }
                    )
                }

                item {
                    CategoryChip(
                        label = "FAVS",
                        icon = PixelIcons.Heart,
                        isSelected = showOnlyFavorites,
                        accentColor = PixelAlertRed,
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showOnlyFavorites = !showOnlyFavorites
                            if (showOnlyFavorites) selectedCategory = null
                        }
                    )
                }

                items(GameCategory.entries.toTypedArray()) { category ->
                    val catIcon = when (category) {
                        GameCategory.TRIVIA -> PixelIcons.Lightbulb
                        GameCategory.ACTION -> PixelIcons.Zap
                        GameCategory.MYSTERY -> PixelIcons.Eye
                        GameCategory.BOARD -> PixelIcons.Dice
                    }
                    CategoryChip(
                        label = category.title.split(" ").first().uppercase(),
                        icon = catIcon,
                        isSelected = selectedCategory == category && !showOnlyFavorites,
                        accentColor = PixelMagentaHot,
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
                    listOf("ALL" to "ANY", "2P" to "2 PLAYERS", "3-6P" to "3-6 GROUP", "8P+" to "8+ PARTY")
                ) { (code, label) ->
                    val isSel = selectedPlayerFilter == code
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(if (isSel) PixelCrtCyan else PixelVioletElevated)
                            .clickable {
                                haptics.performTick(composeHaptics)
                                selectedPlayerFilter = code
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) PixelOutlineBlack else TextSecondary,
                            fontFamily = PressStart2PFont,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        Text(
                            text = "NO GAMES FOUND",
                            color = PixelAlertRed,
                            fontFamily = PressStart2PFont,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                                .background(PixelCrtCyan)
                                .clickable {
                                    searchQuery = ""
                                    selectedCategory = null
                                    showOnlyFavorites = false
                                    selectedPlayerFilter = "ALL"
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "RESET FILTERS",
                                color = PixelOutlineBlack,
                                fontFamily = PressStart2PFont,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                gameName = "Remote Multiplayer Room",
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
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(38.dp)
            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
            .background(if (isSelected) accentColor else PixelVioletElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PixelOutlineBlack else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) PixelOutlineBlack else TextSecondary,
                fontFamily = PressStart2PFont,
                fontSize = 8.sp
            )
        }
    }
}
