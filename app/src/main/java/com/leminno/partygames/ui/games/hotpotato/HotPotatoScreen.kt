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
        targetValue = if (isExploded) 1.4f else 1.0f,
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
                    cameraManager.setTorchMode(cameraId, true)
                    delay(300L)
                    cameraManager.setTorchMode(cameraId, false)
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isExploded) AlertRed.copy(alpha = 0.9f) else BackgroundNavySlate)
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
                    text = "HOT POTATO 💥",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFB300))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("PASS FAST! 🔄", color = Color(0xFFFFB300), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bomb Visualizer Pulse Node
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(if (isExploded) AlertRed else Color(0x33FFB300))
                    .border(2.dp, if (isExploded) WinGold else Color(0xFFFFB300), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isExploded) "💥 BOOM!" else "💣 PASS!",
                    fontSize = if (isExploded) 48.sp else 54.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Category Prompt Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceGlassDark)
                    .border(1.dp, BorderGlassDefault, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SHOUT ANSWER OUT LOUD:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = categories[currentCategoryIndex % categories.size],
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Controls
            if (!isExploded) {
                Button(
                    onClick = {
                        currentCategoryIndex++
                        haptics.performPop()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                ) {
                    Text("NEXT PROMPT ▶", color = BackgroundObsidian, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Button(
                    onClick = {
                        isExploded = false
                        elapsedSec = 0
                        randomDurationSec = Random.nextInt(15, 35)
                        currentCategoryIndex++
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("PLAY AGAIN 💣", color = BackgroundObsidian, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
