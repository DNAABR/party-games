package com.leminno.partygames.ui.games.hangman

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.RemoteRoomSetupSheet
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

val presetHangmanWords = listOf("PARTY", "ANDROID", "KOTLIN", "CIRCUS", "ROCKET", "GUITAR", "PIRATE", "DRAGON")

@Composable
fun HangmanScreen(
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

    var secretWord by remember { mutableStateOf(presetHangmanWords.random()) }
    var inputCustomWord by remember { mutableStateOf("") }
    var guessedLetters by remember { mutableStateOf<Set<Char>>(emptySet()) }
    var wrongGuessCount by remember { mutableIntStateOf(0) }
    var isVictory by remember { mutableStateOf(false) }

    val qwertyRows = listOf(
        listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
        listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
        listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')
    )

    // Observe Remote Hangman State
    LaunchedEffect(roomCode, isRemoteMode) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(roomCode).collect { room ->
                if (room != null && room.gameState.isNotEmpty()) {
                    val remoteWord = room.gameState["secretWord"] as? String
                    val remoteGuessed = (room.gameState["guessedLetters"] as? List<*>)?.mapNotNull { it?.toString()?.getOrNull(0) }?.toSet()
                    val remotePhase = room.gameState["phase"] as? String

                    if (!remoteWord.isNullOrBlank()) secretWord = remoteWord
                    if (remoteGuessed != null) {
                        guessedLetters = remoteGuessed
                        wrongGuessCount = remoteGuessed.count { !secretWord.contains(it, ignoreCase = true) }
                    }
                    if (remotePhase != null && remotePhase != gamePhase) {
                        gamePhase = remotePhase
                    }
                }
            }
        }
    }

    fun syncRemoteState(newWord: String, newGuessed: Set<Char>, newPhase: String) {
        if (isRemoteMode && roomCode.isNotBlank()) {
            scope.launch {
                RemoteRoomRepository.updateGameState(
                    roomCode,
                    mapOf(
                        "secretWord" to newWord,
                        "guessedLetters" to newGuessed.map { it.toString() },
                        "phase" to newPhase
                    )
                )
            }
        }
    }

    fun handleLetterGuess(letter: Char) {
        if (guessedLetters.contains(letter) || isVictory || wrongGuessCount >= 6) return

        val updatedGuessed = guessedLetters + letter
        guessedLetters = updatedGuessed

        if (secretWord.contains(letter, ignoreCase = true)) {
            haptics.performPop()
            val unrevealed = secretWord.uppercase().filter { it.isLetter() }.any { !updatedGuessed.contains(it) }
            if (!unrevealed) {
                isVictory = true
                gamePhase = "GAME_OVER"
                syncRemoteState(secretWord, updatedGuessed, "GAME_OVER")
                return
            }
        } else {
            haptics.performWarningThud()
            wrongGuessCount++
            if (wrongGuessCount >= 6) {
                haptics.performHeavyBurst()
                isVictory = false
                gamePhase = "GAME_OVER"
                syncRemoteState(secretWord, updatedGuessed, "GAME_OVER")
                return
            }
        }
        syncRemoteState(secretWord, updatedGuessed, gamePhase)
    }

    GameScaffold(
        title = "Hangman",
        titleColor = TextPrimary,
        gameId = "hangman",
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
                Text("Choose single phone pass or remote room guessing", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)

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
                            gamePhase = "WORD_SETUP"
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BoardContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Pass & Play (Same Phone)", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Hide word setup before passing phone", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
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
                            Text("Host sets word, friends guess from devices", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else if (gamePhase != "GAME_OVER") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (gamePhase == "WORD_SETUP") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Set Secret Hangman Word",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose a word for other players to guess letter by letter",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = inputCustomWord,
                            onValueChange = { inputCustomWord = it.uppercase() },
                            placeholder = { Text("Type secret word...", color = TextSecondary, fontFamily = ModernSansFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight,
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = {
                            haptics.performTick()
                            inputCustomWord = presetHangmanWords.random()
                        }) {
                            Text("🎲 Suggest Random Word", color = BrandPrimary, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    PrimaryPartyButton(
                        text = if (isRemoteMode) "Start Remote Guessing 🚀" else "Hide Word & Pass to Guessers ▶",
                        accentColor = BrandPrimary,
                        onClick = {
                            val word = inputCustomWord.ifBlank { presetHangmanWords.random() }.trim().uppercase()
                            haptics.performPop()
                            secretWord = word
                            guessedLetters = emptySet()
                            wrongGuessCount = 0
                            gamePhase = "GUESSING"
                            syncRemoteState(word, emptySet(), "GUESSING")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (gamePhase == "GUESSING") {
                    // Hangman Drawing Canvas
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .subtleCardShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceLight)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val gallowsCol = TextMuted
                            val bodyCol = AlertRed

                            drawLine(gallowsCol, Offset(20f, h - 14f), Offset(w - 20f, h - 14f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(gallowsCol, Offset(45f, h - 14f), Offset(45f, 16f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(gallowsCol, Offset(45f, 16f), Offset(w / 2f, 16f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(gallowsCol, Offset(w / 2f, 16f), Offset(w / 2f, 40f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)

                            if (wrongGuessCount >= 1) drawCircle(bodyCol, radius = 16.dp.toPx(), center = Offset(w / 2f, 40f + 16.dp.toPx()), style = Stroke(width = 3.dp.toPx()))
                            if (wrongGuessCount >= 2) drawLine(bodyCol, Offset(w / 2f, 40f + 32.dp.toPx()), Offset(w / 2f, 40f + 72.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            if (wrongGuessCount >= 3) drawLine(bodyCol, Offset(w / 2f, 40f + 42.dp.toPx()), Offset(w / 2f - 22.dp.toPx(), 40f + 58.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            if (wrongGuessCount >= 4) drawLine(bodyCol, Offset(w / 2f, 40f + 42.dp.toPx()), Offset(w / 2f + 22.dp.toPx(), 40f + 58.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            if (wrongGuessCount >= 5) drawLine(bodyCol, Offset(w / 2f, 40f + 72.dp.toPx()), Offset(w / 2f - 22.dp.toPx(), 40f + 100.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            if (wrongGuessCount >= 6) drawLine(bodyCol, Offset(w / 2f, 40f + 72.dp.toPx()), Offset(w / 2f + 22.dp.toPx(), 40f + 100.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }

                    // Word blanks
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        secretWord.forEach { char ->
                            val isRevealed = guessedLetters.contains(char.uppercaseChar()) || !char.isLetter()
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(44.dp)
                                    .subtleCardShadow(elevation = 1.dp, shape = RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isRevealed) BrandPrimaryContainer else SurfaceLight)
                                    .border(1.dp, if (isRevealed) BrandPrimary.copy(alpha = 0.4f) else BorderSubtle, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isRevealed) "$char" else "_",
                                    color = if (isRevealed) BrandPrimary else TextSecondary,
                                    fontFamily = ModernSansFont,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Keyboard
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        qwertyRows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.forEach { char ->
                                    val isUsed = guessedLetters.contains(char)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isUsed) SurfaceSubtle else SurfaceLight)
                                            .border(1.dp, if (isUsed) Color.Transparent else BorderSubtle, RoundedCornerShape(8.dp))
                                            .clickable(enabled = !isUsed) { handleLetterGuess(char) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$char",
                                            color = if (isUsed) TextMuted else TextPrimary,
                                            fontFamily = ModernSansFont,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = if (isVictory) "Word Uncovered! 🎉" else "Execution Complete! ☠️",
                subtitle = "Secret Word: $secretWord",
                onPlayAgain = {
                    inputCustomWord = ""
                    gamePhase = "MODE_SELECT"
                },
                onBackToHub = onExitGame
            )
        }

        if (showRemoteSheet) {
            RemoteRoomSetupSheet(
                gameId = "hangman",
                gameName = "Hangman",
                onDismiss = {
                    showRemoteSheet = false
                    if (roomCode.isBlank()) gamePhase = "MODE_SELECT"
                },
                onRoomJoined = { code, hostFlag, _ ->
                    roomCode = code
                    isHost = hostFlag
                    isRemoteMode = true
                    gamePhase = if (hostFlag) "WORD_SETUP" else "GUESSING"
                    showRemoteSheet = false
                }
            )
        }
    }
}

