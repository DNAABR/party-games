package com.leminno.partygames.ui.games.chiryauri

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay

data class FlyPrompt(val name: String, val canFly: Boolean, val emoji: String)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChiryaUriScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    val flyPrompts = remember {
        listOf(
            FlyPrompt("Sparrow", true, "🐦"),
            FlyPrompt("Eagle", true, "🦅"),
            FlyPrompt("Cow", false, "🐄"),
            FlyPrompt("Airplane", true, "✈️"),
            FlyPrompt("Elephant", false, "🐘"),
            FlyPrompt("Parrot", true, "🦜"),
            FlyPrompt("Dog", false, "🐕"),
            FlyPrompt("Helicopter", true, "🚁"),
            FlyPrompt("Car", false, "🚗")
        )
    }

    var currentPrompt by remember { mutableStateOf(flyPrompts.random()) }
    var isRoundActive by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("ALL PLAYERS: HOLD YOUR CORNER TOUCH TARGETS!") }

    // Multi-touch target tracking for 4 corner zones
    val touchStates = remember { mutableStateListOf(false, false, false, false) }
    val scores = remember { mutableStateListOf(0, 0, 0, 0) }

    // Round Loop
    LaunchedEffect(isRoundActive) {
        if (isRoundActive) {
            delay(1500L)
            currentPrompt = flyPrompts.random()
            statusText = "${currentPrompt.emoji} ${currentPrompt.name.uppercase()} FLIES!"
            haptics.performPop()
            delay(2000L)
            statusText = "GET READY FOR NEXT ROUND..."
            isRoundActive = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundObsidian)
    ) {
        // Center Prompt HUD
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("CHIRYA URI 🦅", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceGlassDark)
                    .border(2.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusText,
                    color = Color(0xFF00F2FE),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isRoundActive = true
                    haptics.performPop()
                },
                enabled = !isRoundActive,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F))
            ) {
                Text("NEXT PROMPT ▶", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // 4 Corner Multi-Touch Target Zones
        val corners = listOf(
            Alignment.TopStart to 0,
            Alignment.TopEnd to 1,
            Alignment.BottomStart to 2,
            Alignment.BottomEnd to 3
        )

        corners.forEach { (alignment, index) ->
            val isTouching = touchStates[index]
            Box(
                modifier = Modifier
                    .align(alignment)
                    .padding(16.dp)
                    .size(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isTouching) Color(0xFF00E676) else Color(0x33FFFFFF))
                    .border(2.dp, if (isTouching) Color(0xFF00E676) else BorderGlassDefault, RoundedCornerShape(24.dp))
                    .pointerInteropFilter { event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                touchStates[index] = true
                                haptics.performTick()
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                                touchStates[index] = false
                                true
                            }
                            else -> false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("P${index + 1}", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(if (isTouching) "HOLDING 👇" else "TOUCH HERE", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Top Exit Button
        IconButton(
            onClick = onExitGame,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            Text("❌", fontSize = 20.sp)
        }
    }
}
