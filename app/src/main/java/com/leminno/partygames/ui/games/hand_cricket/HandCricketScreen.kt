package com.leminno.partygames.ui.games.hand_cricket

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

enum class HandCricketGameMode(val title: String, val subtitle: String, val icon: String) {
    SPLIT_SCREEN("1v1 Split Screen", "Same device facing opposite directions", "📲"),
    TEAM_ROOM("Online Multi-Device", "Multiplayer room via code or link", "🌐")
}

@Composable
fun HandCricketScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }
    val composeHaptics = LocalHapticFeedback.current

    var selectedGameMode by remember { mutableStateOf<HandCricketGameMode?>(null) }
    var showRemoteSheet by remember { mutableStateOf(false) }

    // Multi-Device Online Room State
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }
    var roomJoined by remember { mutableStateOf(false) }
    var localPlayerId by remember { mutableStateOf("") }

    // Game state
    var isInnings1 by remember { mutableStateOf(true) }
    var team1Name by remember { mutableStateOf("Team A") }
    var team2Name by remember { mutableStateOf("Team B") }

    var batterScore by remember { mutableIntStateOf(0) }
    var wicketsLost by remember { mutableIntStateOf(0) }
    val maxWickets = 3

    var innings1Target by remember { mutableIntStateOf(0) }

    var p1Choice by remember { mutableStateOf<Int?>(null) } // Batter
    var p2Choice by remember { mutableStateOf<Int?>(null) } // Bowler
    var roundResultText by remember { mutableStateOf<String?>(null) }
    var matchGameOver by remember { mutableStateOf(false) }
    var winnerName by remember { mutableStateOf("") }

    // Remote State Listener
    LaunchedEffect(roomCode, roomJoined) {
        if (roomJoined && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteP1 = (room.gameState["p1Choice"] as? Number)?.toInt()
                    val remoteP2 = (room.gameState["p2Choice"] as? Number)?.toInt()

                    if (remoteP1 != null) p1Choice = remoteP1
                    if (remoteP2 != null) p2Choice = remoteP2

                    if (remoteP1 != null && remoteP2 != null) {
                        val bat = remoteP1
                        val bowl = remoteP2

                        if (bat == bowl) {
                            haptics.performHeavyBurst()
                            wicketsLost++
                            roundResultText = "WICKET! OUT! 💥 Both picked $bat!"
                            if (wicketsLost >= maxWickets) {
                                if (isInnings1) {
                                    innings1Target = batterScore + 1
                                    batterScore = 0
                                    wicketsLost = 0
                                    isInnings1 = false
                                    roundResultText = "Innings 1 Over! Target: $innings1Target Runs!"
                                } else {
                                    matchGameOver = true
                                    winnerName = if (batterScore >= innings1Target) team2Name else team1Name
                                }
                            }
                        } else {
                            haptics.performPop()
                            batterScore += bat
                            roundResultText = "+$bat Runs! (Bat: $bat, Bowl: $bowl)"
                            if (!isInnings1 && batterScore >= innings1Target) {
                                matchGameOver = true
                                winnerName = team2Name
                            }
                        }

                        if (isHost) {
                            scope.launch {
                                kotlinx.coroutines.delay(1800)
                                RemoteRoomRepository.updateGameState(
                                    roomCode,
                                    mapOf("p1Choice" to null, "p2Choice" to null)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun evaluateLocalRound() {
        if (p1Choice != null && p2Choice != null) {
            val bat = p1Choice!!
            val bowl = p2Choice!!

            if (bat == bowl) {
                haptics.performHeavyBurst()
                wicketsLost++
                roundResultText = "WICKET! OUT! 💥 Both picked $bat!"

                if (wicketsLost >= maxWickets) {
                    if (isInnings1) {
                        innings1Target = batterScore + 1
                        batterScore = 0
                        wicketsLost = 0
                        isInnings1 = false
                        roundResultText = "Innings 1 Over! Target: $innings1Target Runs!"
                    } else {
                        matchGameOver = true
                        winnerName = if (batterScore >= innings1Target) team2Name else team1Name
                    }
                }
            } else {
                haptics.performPop()
                batterScore += bat
                roundResultText = "+$bat Runs! (Bat: $bat, Bowl: $bowl)"

                if (!isInnings1 && batterScore >= innings1Target) {
                    matchGameOver = true
                    winnerName = team2Name
                }
            }

            p1Choice = null
            p2Choice = null
        }
    }

    fun handleGestureSelection(choice: Int, isBatter: Boolean) {
        if (roomJoined) {
            val currentP1 = if (isBatter) choice else p1Choice
            val currentP2 = if (!isBatter) choice else p2Choice
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf("p1Choice" to currentP1, "p2Choice" to currentP2)
                )
            }
        } else {
            if (isBatter) p1Choice = choice else p2Choice = choice
            evaluateLocalRound()
        }
    }

    GameScaffold(
        title = "Hand Cricket",
        titleColor = TextPrimary,
        gameId = "hand_cricket",
        onExitGame = onExitGame
    ) {
        if (selectedGameMode == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Select Play Mode",
                    color = TextPrimary,
                    fontFamily = ModernSansFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose 1v1 split screen or online room",
                    color = TextSecondary,
                    fontFamily = ModernSansFont,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    HandCricketGameMode.entries.forEach { mode ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceLight)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                                .clickable {
                                    haptics.performTick(composeHaptics)
                                    selectedGameMode = mode
                                    if (mode == HandCricketGameMode.TEAM_ROOM) {
                                        showRemoteSheet = true
                                    }
                                }
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (mode == HandCricketGameMode.SPLIT_SCREEN) BoardContainer else ActionContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = mode.icon, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = mode.title,
                                        color = TextPrimary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = mode.subtitle,
                                        color = TextSecondary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (!matchGameOver) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Scoreboard Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isInnings1) "$team1Name (Batting)" else "$team2Name (Chasing Target: $innings1Target)",
                                color = BrandPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (roomJoined) {
                                Text(
                                    text = "🌐 Room: $roomCode",
                                    color = TextSecondary,
                                    fontFamily = ModernSansFont,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$batterScore Runs • $wicketsLost/$maxWickets Wickets",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                roundResultText?.let { res ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (res.contains("WICKET")) AlertContainer else SuccessContainer)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = res,
                            color = if (res.contains("WICKET")) AlertRed else SuccessGreen,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Bowler Zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.rotate(if (selectedGameMode == HandCricketGameMode.SPLIT_SCREEN) 180f else 0f)
                    ) {
                        Text(
                            text = "Bowler (1 - 6)",
                            color = AlertRed,
                            fontFamily = ModernSansFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..6).forEach { num ->
                                val isSel = p2Choice == num
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSel) AlertContainer else SurfaceSubtle)
                                        .border(1.dp, if (isSel) AlertRed else BorderSubtle, CircleShape)
                                        .clickable {
                                            haptics.performTick(composeHaptics)
                                            handleGestureSelection(num, isBatter = false)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$num",
                                        color = if (isSel) AlertRed else TextPrimary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Batter Zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Batter (1 - 6)",
                            color = ActionText,
                            fontFamily = ModernSansFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..6).forEach { num ->
                                val isSel = p1Choice == num
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSel) ActionContainer else SurfaceSubtle)
                                        .border(1.dp, if (isSel) ActionPrimary else BorderSubtle, CircleShape)
                                        .clickable {
                                            haptics.performTick(composeHaptics)
                                            handleGestureSelection(num, isBatter = true)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$num",
                                        color = if (isSel) ActionText else TextPrimary,
                                        fontFamily = ModernSansFont,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "Winner: $winnerName 🎉",
                subtitle = "Hand Cricket Champions!",
                onPlayAgain = {
                    matchGameOver = false
                    isInnings1 = true
                    batterScore = 0
                    wicketsLost = 0
                    p1Choice = null
                    p2Choice = null
                    roundResultText = null
                },
                onBackToHub = onExitGame
            )
        }

        // Remote Room Setup Sheet
        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "hand_cricket",
                gameName = "Hand Cricket",
                onDismiss = {
                    showRemoteSheet = false
                    if (!roomJoined) selectedGameMode = null
                },
                onRoomJoined = { code, hostFlag, pid ->
                    roomCode = code
                    isHost = hostFlag
                    localPlayerId = pid
                    roomJoined = true
                    showRemoteSheet = false
                }
            )
        }
    }
}

