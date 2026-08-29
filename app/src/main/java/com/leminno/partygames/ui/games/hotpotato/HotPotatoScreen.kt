package com.leminno.partygames.ui.games.hotpotato

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun HotPotatoScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    val categories = remember {
        listOf(
            "Name a country starting with 'A'",
            "Name a movie starring Tom Cruise",
            "Name a pizza topping",
            "Name a brand of shoes",
            "Name a Marvel superhero",
            "Name a fast food restaurant",
            "Name an ocean animal",
            "Name a musical instrument"
        ).shuffled()
    }

    var currentCategoryIndex by remember { mutableIntStateOf(0) }
    var isExploded by remember { mutableStateOf(false) }
    var randomDurationSec by remember { mutableIntStateOf(Random.nextInt(15, 35)) }
    var elapsedSec by remember { mutableIntStateOf(0) }

    val pulseScale by animateFloatAsState(
        targetValue = if (isExploded) 1.3f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "pulseScale"
    )

    // Countdown & Explosion effect loop
    LaunchedEffect(isExploded, randomDurationSec) {
        if (!isExploded) {
            while (elapsedSec < randomDurationSec) {
                delay(1000L)
                elapsedSec++
                haptics.performTick()
            }
            isExploded = true
            haptics.performHeavyBurst()

            // Flash Camera LED explosion effect if hardware available
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                if (cameraId != null) {
                    val chars = cameraManager.getCameraCharacteristics(cameraId)
                    val hasFlash = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    if (hasFlash) {
                        try {
                            cameraManager.setTorchMode(cameraId, true)
                            delay(300L)
                        } finally {
                            try { cameraManager.setTorchMode(cameraId, false) } catch (_: Exception) {}
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    GameScaffold(
        title = "Hot Potato",
        titleColor = TextPrimary,
        gameId = "hot_potato",
        onExitGame = onExitGame
    ) {
        if (!isExploded) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Pass Warning Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(AlertContainer)
                        .border(1.dp, AlertRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Pass Fast! 🔄", color = AlertRed, fontFamily = ModernSansFont, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Bomb Visualizer Pulse Node
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(pulseScale)
                        .subtleCardShadow(elevation = 6.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(BoardContainer)
                        .border(2.dp, BoardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💣", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PASS!",
                            color = BoardText,
                            fontFamily = ModernSansFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Category Prompt Box
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
                        Text("SHOUT ANSWER OUT LOUD:", color = TextSecondary, fontFamily = ModernSansFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = categories[currentCategoryIndex % categories.size],
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Next Prompt Button
                PrimaryPartyButton(
                    text = "Next Prompt ▶",
                    accentColor = BrandPrimary,
                    onClick = {
                        currentCategoryIndex++
                        haptics.performPop()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = "💥 Boom! Bomb Exploded!",
                subtitle = "Whoever holds the phone is OUT!",
                onPlayAgain = {
                    isExploded = false
                    elapsedSec = 0
                    randomDurationSec = Random.nextInt(15, 35)
                    currentCategoryIndex++
                },
                onBackToHub = onExitGame
            )
        }
    }
}

