package com.leminno.partygames.ui.games.wouldyourather

import androidx.compose.animation.core.animateFloatAsState
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
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.theme.*

data class WouldYouRatherScenario(val optionA: String, val optionB: String, val percentA: Int)

@Composable
fun WouldYouRatherScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    val scenarios = remember {
        listOf(
            WouldYouRatherScenario("Be able to fly at 10mph", "Teleport once per day anywhere", 62),
            WouldYouRatherScenario("Always speak your mind out loud", "Never be able to speak again", 81),
            WouldYouRatherScenario("Live in a world without music", "Live in a world without movies", 74),
            WouldYouRatherScenario("Have infinite free coffee for life", "Have infinite free pizza for life", 58),
            WouldYouRatherScenario("Explore deep ocean trenches", "Explore outer space planets", 69)
        ).shuffled()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) } // "A" or "B"

    val currentScenario = scenarios[currentIndex % scenarios.size]

    val animatedPercentA by animateFloatAsState(
        targetValue = if (selectedOption != null) currentScenario.percentA.toFloat() else 0f,
        label = "percentA"
    )

    GameScaffold(
        title = "Would You Rather ⚖️",
        titleColor = Color(0xFF00F2FE),
        gameId = "would_you_rather",
        onExitGame = onExitGame
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Dilemma Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceGlassDark)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Dilemma #${currentIndex + 1}", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option A Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selectedOption == "A") Color(0x4400F2FE) else SurfaceGlassDark)
                    .border(2.dp, if (selectedOption == "A") Color(0xFF00F2FE) else BorderGlassDefault, RoundedCornerShape(24.dp))
                    .clickable {
                        selectedOption = "A"
                        haptics.performPop()
                    }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("OPTION A 🅰️", color = Color(0xFF00F2FE), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentScenario.optionA,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (selectedOption != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${animatedPercentA.toInt()}% Chose This",
                            color = Color(0xFF00F2FE),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option B Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selectedOption == "B") Color(0x44FF007F) else SurfaceGlassDark)
                    .border(2.dp, if (selectedOption == "B") Color(0xFFFF007F) else BorderGlassDefault, RoundedCornerShape(24.dp))
                    .clickable {
                        selectedOption = "B"
                        haptics.performPop()
                    }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("OPTION B 🅱️", color = Color(0xFFFF007F), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentScenario.optionB,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (selectedOption != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${100 - animatedPercentA.toInt()}% Chose This",
                            color = Color(0xFFFF007F),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Scenario Button
            Button(
                onClick = {
                    selectedOption = null
                    currentIndex++
                    haptics.performPop()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
            ) {
                Text("NEXT DILEMMA ▶", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
