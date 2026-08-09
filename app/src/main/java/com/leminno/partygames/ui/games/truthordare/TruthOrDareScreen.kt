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

    var truthIndex by remember { mutableIntStateOf(0) }
    var dareIndex by remember { mutableIntStateOf(0) }

    val truthPrompts = remember(selectedDeck) {
        when (selectedDeck) {
            "Clean" -> listOf(
                "What is your biggest fear?",
                "What is your most embarrassing school memory?",
                "What is one secret talent you have?",
                "What is the nicest thing someone has done for you?",
                "What is your most embarrassing habit?",
                "What is the weirdest food you secretly enjoy?",
                "What is your most irrational fear?",
                "What childhood toy do you still miss?",
                "What is the silliest thing you believed as a kid?",
                "What is the longest you have gone without showering?"
            )
            "Extreme" -> listOf(
                "What is the spiciest text you ever sent?",
                "Have you ever cheated on a test?",
                "What is your biggest regret in a relationship?",
                "What is the most embarrassing thing in your camera roll?",
                "Have you ever pretended to be sick to skip plans?",
                "What lie do you tell most often?",
                "What is the worst date you have ever been on?",
                "Have you ever stalked someone on social media?",
                "What is the most reckless thing you have ever done?",
                "What secret would ruin your reputation if it got out?"
            )
            else -> listOf(
                "What is the weirdest dream you ever had?",
                "Who in this room would you survive a zombie apocalypse with?",
                "What is your guilty pleasure song?",
                "What fictional world would you live in?",
                "Who was your most embarrassing celebrity crush?",
                "What is the most childish thing you still do?",
                "What is a movie that always makes you cry?",
                "If you could swap lives with someone here for a day, who?",
                "What is the worst gift you have ever received?",
                "What is something you are terrible at but love doing?"
            )
        }.shuffled()
    }

    val darePrompts = remember(selectedDeck) {
        when (selectedDeck) {
            "Clean" -> listOf(
                "Do 10 jumping jacks while singing!",
                "Do your best impression of a chicken!",
                "Speak in a funny accent for 2 rounds.",
                "Do your best robot dance for 15 seconds.",
                "Talk in slow motion for the next minute.",
                "Sing the alphabet backwards.",
                "Do your best celebrity impression.",
                "Let someone draw on your hand with a pen.",
                "Speak only in questions for the next 2 rounds.",
                "Do a dramatic reading of the last text you sent."
            )
            "Extreme" -> listOf(
                "Let someone send a random emoji to your recent contact!",
                "Do 20 pushups right now!",
                "Eat a spoonful of hot sauce or mustard!",
                "Let the group pick a new profile photo for you.",
                "Call a random contact and compliment them.",
                "Show the group your most embarrassing photo.",
                "Do a plank until your next turn.",
                "Let someone post a story on your social media.",
                "Speak in a baby voice for the next 3 rounds.",
                "Dance with no music for 30 seconds."
            )
            else -> listOf(
                "Do an impression of someone in this room!",
                "Sing the chorus of your favorite song loudly!",
                "Let the group design a funny hair style for you.",
                "Do your best catwalk across the room.",
                "Hold an ice cube until it melts.",
                "Talk without closing your mouth for 1 minute.",
                "Act out a scene from your favorite movie.",
                "Let someone tickle you for 10 seconds.",
                "Do a dramatic slow-motion replay of tripping.",
                "Swap an item of clothing with someone for 3 rounds."
            )
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
                        activePrompt = truthPrompts[truthIndex % truthPrompts.size]
                        truthIndex++
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
                        activePrompt = darePrompts[dareIndex % darePrompts.size]
                        dareIndex++
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
