package com.leminno.partygames.ui.games.undercover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*

@Composable
fun UndercoverSpyScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    val locations = remember {
        listOf("Submarine", "Airplane", "Space Station", "Movie Studio", "Hospital", "Casino", "Supermarket", "Circus", "Polar Station")
    }

    val secretLocation = remember { locations.random() }
    val spyIndex = remember { (0 until playerCount).random() }

    var currentPlayerTurn by remember { mutableIntStateOf(0) }
    var isRevealingRole by remember { mutableStateOf(false) }
    var isSetupPhase by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundNavySlate)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitGame) {
                    Text("❌", fontSize = 20.sp)
                }

                Text(
                    text = "UNDERCOVER SPY 🕵️",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3300E676))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Player ${currentPlayerTurn + 1} / $playerCount", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (isSetupPhase) {
                // Secret Role Reveal Card with Hold-to-Reveal Fingerprint Scanner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceGlassDark)
                        .border(2.dp, if (isRevealingRole) Color(0xFF00E676) else BorderGlassDefault, RoundedCornerShape(28.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isRevealingRole = true
                                    haptics.performPop()
                                    tryAwaitRelease()
                                    isRevealingRole = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRevealingRole) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val isSpy = currentPlayerTurn == spyIndex
                            Text(
                                text = if (isSpy) "YOU ARE THE SPY! 🕵️" else "LOCATION: $secretLocation",
                                color = if (isSpy) Color(0xFFFF007F) else Color(0xFF00E676),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isSpy) "Try to blend in and guess the location!" else "Ask subtle questions to spot the imposter!",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x2200E676))
                                    .border(1.dp, Color(0xFF00E676), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔒", fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "HOLD FINGERPRINT TO REVEAL",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ensure other players cannot see screen!",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Next Player / Start Discussion Button
                Button(
                    onClick = {
                        if (currentPlayerTurn + 1 < playerCount) {
                            currentPlayerTurn++
                            haptics.performPop()
                        } else {
                            isSetupPhase = false
                            haptics.performHeavyBurst()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text(
                        text = if (currentPlayerTurn + 1 < playerCount) "PASS TO PLAYER ${currentPlayerTurn + 2} 🔄" else "START DISCUSSION 💬",
                        color = BackgroundObsidian,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                // Game Phase Discussion View
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("💬 DISCUSSION PHASE!", color = Color(0xFF00E676), fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Take turns asking each other questions! The spy must guess the location without being identified.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onExitGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("REVEAL SPY & FINISH 🕵️", color = BackgroundObsidian, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
