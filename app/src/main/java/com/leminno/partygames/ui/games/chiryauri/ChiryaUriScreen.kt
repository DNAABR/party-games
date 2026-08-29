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
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
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
    var statusText by remember { mutableStateOf("All players: Hold your corner touch target!") }

    // Multi-touch target tracking for 4 corner zones
    val touchStates = remember { mutableStateListOf(false, false, false, false) }

    // Round Loop
    LaunchedEffect(isRoundActive) {
        if (isRoundActive) {
            delay(1500L)
            currentPrompt = flyPrompts.random()
            statusText = "${currentPrompt.emoji} ${currentPrompt.name} Flies!"
            haptics.performPop()
            delay(2000L)
            statusText = "Get ready for next round..."
            isRoundActive = false
        }
    }

    GameScaffold(
        title = "Chirya Uri",
        titleColor = TextPrimary,
        gameId = "chirya_uri",
        onExitGame = onExitGame
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Center Prompt HUD
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleCardShadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                PrimaryPartyButton(
                    text = "Next Prompt",
                    accentColor = BrandPrimary,
                    onClick = {
                        isRoundActive = true
                        haptics.performPop()
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
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
                        .padding(4.dp)
                        .size(110.dp)
                        .subtleCardShadow(elevation = if (isTouching) 3.dp else 1.dp, shape = RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isTouching) SuccessContainer else SurfaceLight)
                        .border(
                            1.5.dp,
                            if (isTouching) SuccessGreen.copy(alpha = 0.5f) else BorderSubtle,
                            RoundedCornerShape(22.dp)
                        )
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
                        Text("P${index + 1}", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            if (isTouching) "Holding 👇" else "Touch Here",
                            color = if (isTouching) SuccessGreen else TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

