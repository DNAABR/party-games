package com.leminno.partygames.ui.games.wouldyourather

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Phonelink
import androidx.compose.material.icons.rounded.Public
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
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

data class WouldYouRatherScenario(val optionA: String, val optionB: String, val percentA: Int)

@Composable
fun WouldYouRatherScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    val players = remember(playerCount) {
        com.leminno.partygames.data.repository.UserPreferencesRepository.getActiveRoster(playerCount)
    }

    var showScoreboard by remember { mutableStateOf(false) }

    val scenarios = remember {
        listOf(
            WouldYouRatherScenario("Be able to fly at 10mph", "Teleport once per day anywhere", 62),
            WouldYouRatherScenario("Always speak your mind out loud", "Never be able to speak again", 81),
            WouldYouRatherScenario("Live in a world without music", "Live in a world without movies", 74),
            WouldYouRatherScenario("Have infinite free coffee for life", "Have infinite free pizza for life", 58),
            WouldYouRatherScenario("Explore deep ocean trenches", "Explore outer space planets", 69),
            WouldYouRatherScenario("Have the ability to speak to animals", "Speak every human foreign language fluently", 85),
            WouldYouRatherScenario("Live in a treehouse mansion", "Live in an underwater dome palace", 64),
            WouldYouRatherScenario("Never have to sleep again", "Never have to work a job again", 78)
        ).shuffled()
    }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, PLAYING
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var currentIndex by remember { mutableIntStateOf(0) }
    val activePlayerIndex = currentIndex % players.size
    val activePlayerName = players.getOrElse(activePlayerIndex) { "Player 1" }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    val currentScenario = scenarios[currentIndex % scenarios.size]

    val animatedPercentA by animateFloatAsState(
        targetValue = if (selectedOption != null) currentScenario.percentA.toFloat() else 0f,
        label = "percentA"
    )

    // Observe Remote State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteIdx = (room.gameState["index"] as? Number)?.toInt()
                    val remoteOpt = room.gameState["selectedOption"] as? String

                    if (remoteIdx != null) currentIndex = remoteIdx
                    if (remoteOpt != null) selectedOption = remoteOpt
                }
            }
        }
    }

    fun syncRemote(idx: Int, opt: String?) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("index" to idx, "selectedOption" to opt)
                )
            }
        }
    }

    GameScaffold(
        title = "WOULD YOU RATHER",
        titleColor = AccentCyan,
        gameId = "would_you_rather",
        onExitGame = onExitGame
    ) {
        if (gamePhase == "MODE_SELECT") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SELECT PLAY MODE",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, AccentCyan, RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = false
                                gamePhase = "PLAYING"
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AccentCyan.copy(alpha = 0.2f))
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Phonelink,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Solo / Same Phone",
                                    color = AccentCyan,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap option to reveal global choice percentage",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, AccentMagenta, RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = true
                                showRemoteSheet = true
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AccentMagenta.copy(alpha = 0.2f))
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Public,
                                    contentDescription = null,
                                    tint = AccentMagenta,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Remote Play (Multi-Device)",
                                    color = AccentMagenta,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Synchronized dilemmas & live polling across screens",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                com.leminno.partygames.ui.components.InGamePlayerHeader(
                    currentPlayerName = activePlayerName,
                    playerIndex = activePlayerIndex,
                    totalPlayers = players.size,
                    onOpenScoreboard = { showScoreboard = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Option A Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedOption == "A") AccentCyan.copy(alpha = 0.25f) else SurfaceGlassDark)
                        .border(1.5.dp, if (selectedOption == "A") AccentCyan else BorderGlassDefault, RoundedCornerShape(24.dp))
                        .clickable {
                            selectedOption = "A"
                            haptics.performPop()
                            syncRemote(currentIndex, "A")
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OPTION A",
                            color = AccentCyan,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentScenario.optionA,
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (selectedOption != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${animatedPercentA.toInt()}% Voted This",
                                color = AccentCyan,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option B Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedOption == "B") AccentMagenta.copy(alpha = 0.25f) else SurfaceGlassDark)
                        .border(1.5.dp, if (selectedOption == "B") AccentMagenta else BorderGlassDefault, RoundedCornerShape(24.dp))
                        .clickable {
                            selectedOption = "B"
                            haptics.performPop()
                            syncRemote(currentIndex, "B")
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OPTION B",
                            color = AccentMagenta,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentScenario.optionB,
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (selectedOption != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${100 - animatedPercentA.toInt()}% Voted This",
                                color = AccentMagenta,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryPartyButton(
                    text = "NEXT DILEMMA",
                    icon = Icons.Rounded.ArrowForward,
                    accentColor = AccentCyan,
                    enabled = selectedOption != null,
                    onClick = {
                        selectedOption = null
                        currentIndex++
                        syncRemote(currentIndex, null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "would_you_rather",
                gameName = "Would You Rather ⚖️",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "PLAYING"
                    showRemoteSheet = false
                    if (hostFlag) {
                        syncRemote(currentIndex, selectedOption)
                    }
                }
            )
        if (showScoreboard) {
            com.leminno.partygames.ui.components.InGameScoreboardModal(
                players = players,
                onDismissRequest = { showScoreboard = false }
            )
        }
    }
}
