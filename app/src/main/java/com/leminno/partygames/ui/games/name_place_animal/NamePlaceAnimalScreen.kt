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
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay

val alphabetList = ('A'..'Z').filter { it !in listOf('Q', 'X', 'Z') }

@Composable
fun NamePlaceAnimalScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    var currentLetter by remember { mutableStateOf(alphabetList.random()) }
    var nameInput by remember { mutableStateOf("") }
    var placeInput by remember { mutableStateOf("") }
    var animalInput by remember { mutableStateOf("") }
    var thingInput by remember { mutableStateOf("") }

    var timerRemaining by remember { mutableIntStateOf(30) }
    var isTimerActive by remember { mutableStateOf(false) }
    var isRoundComplete by remember { mutableStateOf(false) }
    var calculatedScore by remember { mutableIntStateOf(0) }

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (timerRemaining > 0 && isTimerActive) {
                delay(1000)
                timerRemaining--
            }
            if (timerRemaining <= 0) {
                isTimerActive = false
                isRoundComplete = true
                haptics.performHeavyBurst()

                // Calculate score
                var pts = 0
                if (nameInput.trim().startsWith(currentLetter, ignoreCase = true)) pts += 10
                if (placeInput.trim().startsWith(currentLetter, ignoreCase = true)) pts += 10
                if (animalInput.trim().startsWith(currentLetter, ignoreCase = true)) pts += 10
                if (thingInput.trim().startsWith(currentLetter, ignoreCase = true)) pts += 10
                calculatedScore = pts
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07111E))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitGame) {
                    Text("✕", color = TextSecondary, fontSize = 22.sp)
                }
                Text(
                    text = "NAME PLACE ANIMAL 🔤",
                    color = Color(0xFF00E676),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Target Letter Badge & Timer
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
                        color = if (timerRemaining <= 5) Color(0xFFFF0055) else Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (!isRoundComplete) {
                // 4-Field Speed Entry Form
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name starting with '$currentLetter'") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = BorderGlassDefault),
                        enabled = isTimerActive
                    )

                    OutlinedTextField(
                        value = placeInput,
                        onValueChange = { placeInput = it },
                        label = { Text("Place starting with '$currentLetter'") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = BorderGlassDefault),
                        enabled = isTimerActive
                    )

                    OutlinedTextField(
                        value = animalInput,
                        onValueChange = { animalInput = it },
                        label = { Text("Animal starting with '$currentLetter'") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = BorderGlassDefault),
                        enabled = isTimerActive
                    )

                    OutlinedTextField(
                        value = thingInput,
                        onValueChange = { thingInput = it },
                        label = { Text("Thing starting with '$currentLetter'") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = BorderGlassDefault),
                        enabled = isTimerActive
                    )
                }

                Button(
                    onClick = {
                        haptics.performPop()
                        if (!isTimerActive) {
                            isTimerActive = true
                        } else {
                            timerRemaining = 0
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (!isTimerActive) "START 30S TIMER ▶" else "STOP & SUBMIT EARLY 🛑",
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                // Score Evaluation & Dispute Appeals
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ROUND COMPLETE!",
                        color = Color(0xFF00E676),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "TOTAL SCORE: $calculatedScore POINTS 🏆",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceGlassDark)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Name: $nameInput", color = TextPrimary, fontSize = 14.sp)
                            Text("Place: $placeInput", color = TextPrimary, fontSize = 14.sp)
                            Text("Animal: $animalInput", color = TextPrimary, fontSize = 14.sp)
                            Text("Thing: $thingInput", color = TextPrimary, fontSize = 14.sp)
                        }
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
                        isTimerActive = false
                        isRoundComplete = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("NEXT LETTER RACE ▶", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
