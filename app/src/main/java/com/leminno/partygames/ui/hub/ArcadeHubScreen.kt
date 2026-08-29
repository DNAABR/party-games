package com.leminno.partygames.ui.hub

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.GameCatalogRepository
import com.leminno.partygames.data.model.GameCategory
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.components.PrimaryPartyButton
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
            .background(CanvasLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header Bar: App Brand & Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Party Games",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Play together with friends offline",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 13.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Remote Room Code Button
                    IconButton(
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showRemoteRoomSheet = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .subtleCardShadow(elevation = 2.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, CircleShape)
                    ) {
                        Icon(
                            imageVector = PixelIcons.Users,
                            contentDescription = "Remote Room",
                            tint = TextPrimary,
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
                            .subtleCardShadow(elevation = 2.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, CircleShape)
                    ) {
                        Icon(
                            imageVector = PixelIcons.Trophy,
                            contentDescription = "Scorekeeper",
                            tint = WarningAmber,
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
                            .subtleCardShadow(elevation = 2.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, CircleShape)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Play Hero Card
            val quickPlayInteractionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrandPrimary)
                    .springPressScale(quickPlayInteractionSource, pressedScale = 0.98f)
                    .clickable(
                        interactionSource = quickPlayInteractionSource,
                        indication = null
                    ) {
                        haptics.performPop()
                        val mvpGames = GameCatalogRepository.allGames.filter { it.isMvp }
                        selectedGameForSheet = mvpGames.random()
                    }
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎲", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Quick Play",
                                color = TextOnPrimary,
                                fontFamily = ModernSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Start a random 5-min game",
                                color = TextOnPrimary.copy(alpha = 0.85f),
                                fontFamily = ModernSansFont,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Play Now →",
                            color = TextOnPrimary,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search games by name or description...",
                        color = TextMuted,
                        fontFamily = ModernSansFont,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = PixelIcons.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = PixelIcons.Close,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleCardShadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Category & Favorites Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ModernFilterChip(
                        label = "All Games",
                        icon = PixelIcons.ArcadeJoystick,
                        isSelected = selectedCategory == null && !showOnlyFavorites,
                        containerColor = BrandPrimaryContainer,
                        accentColor = BrandPrimary,
                        onClick = {
                            haptics.performTick(composeHaptics)
                            selectedCategory = null
                            showOnlyFavorites = false
                        }
                    )
                }

                item {
                    ModernFilterChip(
                        label = "Favorites",
                        icon = PixelIcons.Heart,
                        isSelected = showOnlyFavorites,
                        containerColor = AlertContainer,
                        accentColor = AlertRed,
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showOnlyFavorites = !showOnlyFavorites
                            if (showOnlyFavorites) selectedCategory = null
                        }
                    )
                }

                items(GameCategory.entries.toTypedArray()) { category ->
                    val catToken = CategoryThemeToken.forCategory(category)
                    val catIcon = when (category) {
                        GameCategory.TRIVIA -> PixelIcons.Lightbulb
                        GameCategory.ACTION -> PixelIcons.Zap
                        GameCategory.MYSTERY -> PixelIcons.Eye
                        GameCategory.BOARD -> PixelIcons.Dice
                    }
                    ModernFilterChip(
                        label = category.title.split(" ").first(),
                        icon = catIcon,
                        isSelected = selectedCategory == category && !showOnlyFavorites,
                        containerColor = catToken.containerColor,
                        accentColor = catToken.textColor,
                        onClick = {
                            haptics.performTick(composeHaptics)
                            showOnlyFavorites = false
                            selectedCategory = if (selectedCategory == category) null else category
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) TextPrimary else SurfaceSubtle)
                            .clickable {
                                haptics.performTick(composeHaptics)
                                selectedPlayerFilter = code
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) TextOnPrimary else TextSecondary,
                            fontFamily = ModernSansFont,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Games Grid or Empty State
            if (filteredGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Games Found",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search or filters",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryPartyButton(
                            text = "Reset Filters",
                            onClick = {
                                searchQuery = ""
                                selectedCategory = null
                                showOnlyFavorites = false
                                selectedPlayerFilter = "ALL"
                            },
                            accentColor = BrandPrimary
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp),
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
                onRoomJoined = { _, _, _ ->
                    showRemoteRoomSheet = false
                    onLaunchGame("hand_cricket", 2, 60)
                }
            )
        }
    }
}

@Composable
private fun ModernFilterChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    containerColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) containerColor else SurfaceLight)
            .border(
                1.dp,
                if (isSelected) accentColor.copy(alpha = 0.3f) else BorderSubtle,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) accentColor else TextSecondary,
                fontFamily = ModernSansFont,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

