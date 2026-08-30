package com.leminno.partygames.ui.games.hotpotato

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.AiGateway
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.PrimaryPartyButton
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val DEFAULT_HOT_POTATO_CATEGORIES = listOf(
    "Name a country starting with 'A'",
    "Name a movie starring Tom Cruise",
    "Name a pizza topping",
    "Name a brand of shoes",
    "Name a Marvel superhero",
    "Name a fast food restaurant",
    "Name an ocean animal",
    "Name a musical instrument",
    "Name a song by Taylor Swift",
    "Name a video game console",
    "Name a type of cheese",
    "Name a car manufacturer",
    "Name a Disney character",
    "Name a fruit that is red",
    "Name a cereal brand",
    "Name a sport played with a ball",
    "Name a famous landmark",
    "Name a board game",
    "Name a dog breed",
    "Name a breakfast food",
    "Name an app on your phone",
    "Name a Netflix TV show",
    "Name something you bring to the beach",
    "Name a flavor of ice cream",
    "Name a superhero with a cape",
    "Name a tool found in a toolbox",
    "Name a country starting with 'B'",
    "Name a movie starring Dwayne 'The Rock' Johnson",
    "Name a soft drink brand",
    "Name a pasta shape or type",
    "Name a chocolate bar",
    "Name a coffee drink",
    "Name a tropical fruit",
    "Name a bakery item",
    "Name a spice or seasoning",
    "Name a DC superhero",
    "Name a Harry Potter character",
    "Name a famous cartoon character",
    "Name a Star Wars character",
    "Name an anime character",
    "Name a iconic video game hero",
    "Name a Pokemon",
    "Name a Pixar movie",
    "Name a 90s hit song",
    "Name a rock band",
    "Name a pop star",
    "Name a musical genre",
    "Name a clothing brand",
    "Name a tech company",
    "Name a streaming service",
    "Name a luxury fashion brand",
    "Name a jungle animal",
    "Name a species of bird",
    "Name an insect",
    "Name a farm animal",
    "Name a prehistoric or extinct animal",
    "Name a card game",
    "Name an Olympic sport",
    "Name an extreme sport",
    "Name something in a kitchen cabinet",
    "Name something in a bathroom medicine cabinet",
    "Name something found under a bed",
    "Name something in a student backpack",
    "Name something in a woman's purse",
    "Name something brought to a campfire",
    "Name something seen at an airport",
    "Name something found in a gym",
    "Name something inside a wallet",
    "Name a superpower you wish you had",
    "Name a high-paying job or profession",
    "Name a spoken language",
    "Name a US State",
    "Name a world capital city",
    "Name a chemical element",
    "Name a planet or celestial body",
    "Name a mythological god or figure",
    "Name a Halloween costume idea",
    "Name something at a birthday party",
    "Name a country starting with 'S'",
    "Name a movie starring Leonardo DiCaprio",
    "Name a vegetable that is green",
    "Name a potato chip flavor",
    "Name a candy brand",
    "Name an instrument played with keys",
    "Name a song from the 2000s",
    "Name a famous YouTuber or streamer",
    "Name a shoe style (e.g. sneakers, boots)",
    "Name a web browser",
    "Name a nocturnal animal",
    "Name a desert animal",
    "Name a winter sport",
    "Name something you wear on your head",
    "Name something you wear on your feet",
    "Name something red in a grocery store",
    "Name something yellow in nature",
    "Name a reason someone might be late",
    "Name a sound made by an animal",
    "Name a excuse for missing homework",
    "Name a topping for a hamburger",
    "Name a flavor of donut",
    "Name something you press or turn on",
    "Name a country starting with 'M'",
    "Name a movie starring Zendaya",
    "Name a breakfast cereal mascot",
    "Name a famous historical figure",
    "Name something that generates heat"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotPotatoScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }
    val scope = rememberCoroutineScope()

    var categories by remember { mutableStateOf(DEFAULT_HOT_POTATO_CATEGORIES.shuffled()) }
    var currentCategoryIndex by remember { mutableIntStateOf(0) }
    var isExploded by remember { mutableStateOf(false) }
    var randomDurationSec by remember { mutableIntStateOf(Random.nextInt(15, 35)) }
    var elapsedSec by remember { mutableFloatStateOf(0f) }

    var isLoadingAi by remember { mutableStateOf(false) }
    var isAiGenerated by remember { mutableStateOf(false) }
    var customTopicInput by remember { mutableStateOf("") }
    var showCustomTopicSheet by remember { mutableStateOf(false) }

    // Synthesized Sound Effect Generator for ticks and bomb explosion
    val toneGenerator = remember {
        try { ToneGenerator(AudioManager.STREAM_MUSIC, 90) } catch (_: Exception) { null }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { toneGenerator?.release() } catch (_: Exception) {}
        }
    }

    // Helper to fetch dynamic AI categories
    fun fetchAiCategories(customTopic: String? = null) {
        isLoadingAi = true
        scope.launch {
            val topicPrompt = if (!customTopic.isNullOrBlank()) " themed around '$customTopic'" else ""
            val prompt = "Generate 12 fun, distinct, fast-paced Hot Potato game prompt categories$topicPrompt (e.g. 'Name a movie starting with...', 'Name something found in a...'). Keep each prompt under 10 words. Output ONLY a line-separated list of the prompts without numbers or bullets."
            val result = AiGateway.askAiSuspend(prompt)
            isLoadingAi = false
            result.onSuccess { response ->
                val lines = response.text.lines()
                    .map { it.trim().removePrefix("- ").removePrefix("* ").removePrefix("• ") }
                    .filter { it.isNotBlank() && it.length > 3 }
                if (lines.isNotEmpty()) {
                    categories = lines.shuffled()
                    currentCategoryIndex = 0
                    isAiGenerated = true
                }
            }.onFailure {
                // Keep existing shuffled categories if network fails
            }
        }
    }

    // Fetch fresh AI categories on initial load
    LaunchedEffect(Unit) {
        fetchAiCategories()
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (isExploded) 1.3f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "pulseScale"
    )

    // Countdown & Explosion effect loop with dynamic ticking & audio/vibration feedback
    LaunchedEffect(isExploded, randomDurationSec) {
        if (!isExploded) {
            while (elapsedSec < randomDurationSec) {
                val remainingSec = randomDurationSec - elapsedSec
                val delayMs = if (remainingSec <= 5) 400L else 1000L
                delay(delayMs)
                elapsedSec += if (remainingSec <= 5) 0.4f else 1.0f

                haptics.performTick()
                try {
                    val toneType = if (remainingSec <= 5) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_BEEP
                    toneGenerator?.startTone(toneType, 50)
                } catch (_: Exception) {}
            }
            isExploded = true
            haptics.performHeavyBurst()
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 700)
            } catch (_: Exception) {}

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
                // Header Pass Warning Badge & AI Status Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(AlertContainer)
                            .border(1.dp, AlertRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Pass Fast! 🔄", color = AlertRed, fontFamily = ModernSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // AI Refresh / Custom Topic Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isAiGenerated) BrandPrimaryContainer else SurfaceSubtle)
                            .border(1.dp, if (isAiGenerated) BrandPrimary.copy(alpha = 0.4f) else BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable { showCustomTopicSheet = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLoadingAi) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = BrandPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Loading...", color = BrandPrimary, fontFamily = ModernSansFont, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            } else {
                                Text("✨", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAiGenerated) "AI Topics" else "AI Pack",
                                    color = if (isAiGenerated) BrandPrimary else TextSecondary,
                                    fontFamily = ModernSansFont,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Bomb Visualizer Pulse Node
                Box(
                    modifier = Modifier
                        .size(200.dp)
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
                        Text(
                            text = if (isAiGenerated) "✨ AI GENERATED PROMPT:" else "SHOUT ANSWER OUT LOUD:",
                            color = if (isAiGenerated) BrandPrimary else TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                    elapsedSec = 0f
                    randomDurationSec = Random.nextInt(15, 35)
                    currentCategoryIndex++
                    // Fetch fresh AI prompts for the next game round
                    fetchAiCategories()
                },
                onBackToHub = onExitGame
            )
        }
    }

    // Modal Sheet for Custom AI Topic Generation
    if (showCustomTopicSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomTopicSheet = false },
            containerColor = SurfaceLight,
            scrimColor = ScrimModal,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Generate AI Hot Potato Pack",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter any topic or theme to get 12 brand new AI categories (e.g. '90s Cartoons', 'Hollywood', 'Video Games'):",
                    color = TextSecondary,
                    fontFamily = ModernSansFont,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customTopicInput,
                    onValueChange = { customTopicInput = it },
                    placeholder = { Text("e.g. Marvel Movies, Foods, Pop Songs...", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showCustomTopicSheet = false
                            fetchAiCategories(null)
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Random AI Pack", color = TextPrimary, fontFamily = ModernSansFont, fontSize = 13.sp)
                    }

                    PrimaryPartyButton(
                        text = "Generate Pack",
                        accentColor = BrandPrimary,
                        onClick = {
                            showCustomTopicSheet = false
                            fetchAiCategories(customTopicInput.ifBlank { null })
                        },
                        modifier = Modifier.weight(1f).height(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


