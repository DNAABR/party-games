package com.leminno.partygames.ui.games.name_place_animal

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

    var gamePhase by remember { mutableStateOf("MODE_SELECT") } // MODE_SELECT, PLAYING, RESULT
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
        title = "NAME PLACE ANIMAL 🔤",
        titleColor = Color(0xFF00E676),
        gameId = "name_place_animal",
        onExitGame = onExitGame
    ) {
        if (gamePhase == "MODE_SELECT") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SELECT PLAY MODE", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = false
                                gamePhase = "PLAYING"
                                isTimerActive = true
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Solo / Same Phone", color = Color(0xFF00E676), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Local speed round against the 30-second timer", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                            .clickable {
                                isRemoteMode = true
                                showRemoteSheet = true
                            }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Remote Play (Multi-Device)", color = Color(0xFF00F2FE), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Synced target letter & speed race across phones", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (gamePhase == "PLAYING") {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                            .border(2.dp, Color(0xFF00E676), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$currentLetter",
                            color = Color(0xFF00E676),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("SPEED TIMER", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${timerRemaining}s",
                            color = if (timerRemaining <= 10) Color(0xFFFF0055) else Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name starting with '$currentLetter'") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = BorderGlassDefault)
                    )

                    OutlinedTextField(
                        value = placeInput,
                        onValueChange = { placeInput = it },
                        label = { Text("Place starting with '$currentLetter'") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = BorderGlassDefault)
                    )

                    OutlinedTextField(
                        value = animalInput,
                        onValueChange = { animalInput = it },
                        label = { Text("Animal starting with '$currentLetter'") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = BorderGlassDefault)
                    )

                    OutlinedTextField(
                        value = thingInput,
                        onValueChange = { thingInput = it },
                        label = { Text("Thing starting with '$currentLetter'") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = BorderGlassDefault)
                    )
                }

                Button(
                    onClick = {
                        isTimerActive = false
                        timerRemaining = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("STOP TIMER & SCORE ROUND 🛑", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        } else {
            // Result View
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ROUND COMPLETE! 🏆", color = Color(0xFF00E676), fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "$calculatedScore / 40 PTS",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("👤 Name: $nameInput", color = TextPrimary, fontSize = 14.sp)
                        Text("📍 Place: $placeInput", color = TextPrimary, fontSize = 14.sp)
                        Text("🐾 Animal: $animalInput", color = TextPrimary, fontSize = 14.sp)
                        Text("📦 Thing: $thingInput", color = TextPrimary, fontSize = 14.sp)
                    }
                }

                Button(
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("NEXT LETTER ROUND 🎲", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "name_place_animal",
                gameName = "Name Place Animal 🔤",
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
