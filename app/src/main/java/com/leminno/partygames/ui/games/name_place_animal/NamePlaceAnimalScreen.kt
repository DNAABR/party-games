package com.leminno.partygames.ui.games.name_place_animal

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val alphabetList = ('A'..'Z').filter { it !in listOf('Q', 'X', 'Z') }

@Composable
fun NamePlaceAnimalScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember { HapticFeedbackManager(context) }

    var gamePhase by remember { mutableStateOf("MODE_SELECT") }
    var isRemoteMode by remember { mutableStateOf(false) }
    var showRemoteSheet by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    var currentLetter by remember { mutableStateOf(alphabetList.random()) }
    var nameInput by remember { mutableStateOf("") }
    var placeInput by remember { mutableStateOf("") }
    var animalInput by remember { mutableStateOf("") }
    var thingInput by remember { mutableStateOf("") }

    var timerRemaining by remember { mutableIntStateOf(30) }
    var isTimerActive by remember { mutableStateOf(false) }
    var calculatedScore by remember { mutableIntStateOf(0) }

    // Observe Remote State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteLetterStr = room.gameState["letter"] as? String
                    if (!remoteLetterStr.isNullOrBlank()) {
                        currentLetter = remoteLetterStr.first()
                    }
                }
            }
        }
    }

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (timerRemaining > 0 && isTimerActive) {
                delay(1000)
                timerRemaining--
            }
            if (timerRemaining <= 0) {
                isTimerActive = false
                gamePhase = "RESULT"
                haptics.performHeavyBurst()

                var pts = 0
                if (nameInput.trim().startsWith(currentLetter, ignoreCase = true)) pts += 10
                if (placeInput.trim().startsWith(currentLetter, ignoreCase = true)) pts += 10
                if (animalInput.trim().startsWith(currentLetter, ignoreCase = true)) pts += 10
                if (thingInput.trim().startsWith(currentLetter, ignoreCase = true)) pts += 10
                calculatedScore = pts

                if (isRemoteMode && roomCode.isNotBlank()) {
                    scope.launch {
                        RemoteRoomRepository.updateGameState(
                            roomCode,
                            mapOf("letter" to currentLetter.toString(), "score" to calculatedScore)
                        )
                    }
                }
            }
        }
    }

    GameScaffold(
        title = "Name Place Animal",
        titleColor = TextPrimary,
        gameId = "name_place_animal",
        onExitGame = onExitGame
    ) {
        if (gamePhase == "MODE_SELECT") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Select Play Mode", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Choose single phone challenge or synced online race", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            isRemoteMode = false
                            gamePhase = "PLAYING"
                            isTimerActive = true
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SuccessContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Solo / Same Phone", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Local speed round against the 30-second timer", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                            isRemoteMode = true
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
                            Text("Synced target letter & speed race across phones", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else if (gamePhase == "PLAYING") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(ActionContainer)
                            .border(1.5.dp, ActionBorder, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$currentLetter",
                            color = ActionText,
                            fontFamily = ModernSansFont,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Speed Timer", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (timerRemaining <= 10) AlertContainer else SurfaceSubtle)
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${timerRemaining}s",
                                color = if (timerRemaining <= 10) AlertRed else TextPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("Name starting with '$currentLetter'", color = TextSecondary, fontFamily = ModernSansFont) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceLight,
                            unfocusedContainerColor = SurfaceLight,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = placeInput,
                        onValueChange = { placeInput = it },
                        placeholder = { Text("Place starting with '$currentLetter'", color = TextSecondary, fontFamily = ModernSansFont) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceLight,
                            unfocusedContainerColor = SurfaceLight,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = animalInput,
                        onValueChange = { animalInput = it },
                        placeholder = { Text("Animal starting with '$currentLetter'", color = TextSecondary, fontFamily = ModernSansFont) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceLight,
                            unfocusedContainerColor = SurfaceLight,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = thingInput,
                        onValueChange = { thingInput = it },
                        placeholder = { Text("Thing starting with '$currentLetter'", color = TextSecondary, fontFamily = ModernSansFont) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceLight,
                            unfocusedContainerColor = SurfaceLight,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }

                PrimaryPartyButton(
                    text = "Stop Timer & Score Round 🛑",
                    accentColor = BrandPrimary,
                    onClick = {
                        isTimerActive = false
                        timerRemaining = 0
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Result View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text("Round Complete! 🏆", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$calculatedScore / 40 PTS",
                                color = BrandPrimary,
                                fontFamily = ModernSansFont,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("👤 Name: ${nameInput.ifBlank { "—" }}", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 14.sp)
                                Text("📍 Place: ${placeInput.ifBlank { "—" }}", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 14.sp)
                                Text("🐾 Animal: ${animalInput.ifBlank { "—" }}", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 14.sp)
                                Text("📦 Thing: ${thingInput.ifBlank { "—" }}", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 14.sp)
                            }
                        }
                    }
                }

                PrimaryPartyButton(
                    text = "Next Letter Round 🎲",
                    accentColor = BrandPrimary,
                    onClick = {
                        currentLetter = alphabetList.random()
                        nameInput = ""
                        placeInput = ""
                        animalInput = ""
                        thingInput = ""
                        timerRemaining = 30
                        gamePhase = "PLAYING"
                        isTimerActive = true
                        if (isRemoteMode && isHost) {
                            scope.launch {
                                RemoteRoomRepository.updateGameState(
                                    roomCode,
                                    mapOf("letter" to currentLetter.toString())
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "name_place_animal",
                gameName = "Name Place Animal",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = "PLAYING"
                    isTimerActive = true
                    showRemoteSheet = false
                    if (hostFlag) {
                        scope.launch {
                            RemoteRoomRepository.updateGameState(
                                roomCode,
                                mapOf("letter" to currentLetter.toString())
                            )
                        }
                    }
                }
            )
        }
    }
}

