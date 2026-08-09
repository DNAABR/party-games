package com.leminno.partygames.ui.games.truthordare

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun TruthOrDareScreen(
    playerCount: Int = 4,
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val scope = rememberCoroutineScope()

    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }

    var selectedDeck by remember { mutableStateOf("Party") } // Clean, Party, Extreme
    var activePrompt by remember { mutableStateOf<String?>(null) }
    var activePromptType by remember { mutableStateOf<String?>(null) } // "TRUTH" or "DARE"

    val truthPrompts = remember(selectedDeck) {
        when (selectedDeck) {
            "Clean" -> listOf("What is your biggest fear?", "What is your most embarrassing school memory?", "What is one secret talent you have?")
            "Extreme" -> listOf("What is the spiciest text you ever sent?", "Have you ever cheated on a test?", "What is your biggest regret in a relationship?")
            else -> listOf("What is the weirdest dream you ever had?", "Who in this room would you survive a zombie apocalypse with?", "What is your guilty pleasure song?")
        }.shuffled()
    }

    val darePrompts = remember(selectedDeck) {
        when (selectedDeck) {
            "Clean" -> listOf("Do 10 jumping jacks while singing!", "Do your best impression of a chicken!", "Speak in a funny accent for 2 rounds.")
            "Extreme" -> listOf("Let someone send a random emoji to your recent contact!", "Do 20 pushups right now!", "Eat a spoonful of hot sauce or mustard!")
            else -> listOf("Do an impression of someone in this room!", "Sing the chorus of your favorite song loudly!", "Let the group design a funny hair style for you.")
        }.shuffled()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundNavySlate)
            .padding(20.dp)
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
                    text = "TRUTH OR DARE 🍾",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                // Deck selector chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGlassDark)
                        .border(1.dp, BorderGlassDefault, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Deck: $selectedDeck", color = Color(0xFF00F2FE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Interactive Physics Bottle Spinner Canvas
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .pointerInput(isSpinning) {
                        if (!isSpinning) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val dragMagnitude = dragAmount.x + dragAmount.y
                                if (dragMagnitude > 30f || dragMagnitude < -30f) {
                                    isSpinning = true
                                    val targetRotation = rotation.value + 1440f + Random.nextFloat() * 720f
                                    scope.launch {
                                        haptics.performPop()
                                        rotation.animateTo(
                                            targetValue = targetRotation,
                                            animationSpec = tween(durationMillis = 3000)
                                        )
                                        isSpinning = false
                                        haptics.performHeavyBurst()
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)

                    // Draw outer player ring circle
                    drawCircle(
                        color = Color(0x3300F2FE),
                        radius = size.width / 2.2f,
                        center = center
                    )

                    // Draw Bottle Arrow Shape
                    rotate(degrees = rotation.value, pivot = center) {
                        val path = Path().apply {
                            moveTo(center.x, center.y - 110f) // Tip of bottle
                            lineTo(center.x + 25f, center.y - 40f)
                            lineTo(center.x + 20f, center.y + 90f)
                            lineTo(center.x - 20f, center.y + 90f)
                            lineTo(center.x - 25f, center.y - 40f)
                            close()
                        }

                        drawPath(path, color = Color(0xFF00F2FE))
                        drawCircle(color = Color(0xFFFF007F), radius = 24f, center = center)
                    }
                }
            }

            Text(
                text = if (isSpinning) "Spinning... 🍾" else "SWIPE BOTTLE TO SPIN!",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Prompt Card Reveal Display
            if (activePrompt != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceGlassDark)
                        .border(1.dp, if (activePromptType == "TRUTH") Color(0xFF9D4EDD) else Color(0xFFFF007F), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activePromptType ?: "CHALLENGE",
                            color = if (activePromptType == "TRUTH") Color(0xFF9D4EDD) else Color(0xFFFF007F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activePrompt ?: "",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Choice Buttons (Truth vs Dare)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        activePromptType = "TRUTH"
                        activePrompt = truthPrompts.random()
                        haptics.performPop()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
                ) {
                    Text("TRUTH 🔮", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        activePromptType = "DARE"
                        activePrompt = darePrompts.random()
                        haptics.performPop()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F))
                ) {
                    Text("DARE 🔥", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
