package com.leminno.partygames.ui.games.codenames

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

enum class CodenameCardType(val title: String, val color: Color) {
    RED("Red Team", Color(0xFFFF0055)),
    BLUE("Blue Team", Color(0xFF00F2FE)),
    CIVILIAN("Civilian", Color(0xFF8D99AE)),
    ASSASSIN("Assassin ☠️", Color.Black)
}

data class CodenameCard(
    val word: String,
    val type: CodenameCardType,
    val isRevealed: Boolean = false
)

val codenameWordPool = listOf(
    "APPLE", "AGENT", "BANK", "BEACH", "BERLIN", "BOTTLE", "BRIDGE", "CAMERA",
    "CASTLE", "DRAGON", "EAGLE", "ENGINE", "FLIGHT", "FOREST", "HAWK", "HOTEL",
    "ICE", "KNIGHT", "LASER", "MATCH", "MOON", "NIGHT", "OCEAN", "PIPES", "QUEEN"
)

@Composable
fun CodenamesScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    var isSpymasterView by remember { mutableStateOf(false) }
    var redScore by remember { mutableIntStateOf(0) }
    var blueScore by remember { mutableIntStateOf(0) }
    var isRedTurn by remember { mutableStateOf(true) }
    var gameOverWinner by remember { mutableStateOf<String?>(null) }

    var gridCards by remember {
        val shuffledWords = codenameWordPool.shuffled()
        val types = (List(9) { CodenameCardType.RED } +
                List(8) { CodenameCardType.BLUE } +
                List(7) { CodenameCardType.CIVILIAN } +
                listOf(CodenameCardType.ASSASSIN)).shuffled()

        mutableStateOf(shuffledWords.mapIndexed { idx, word ->
            CodenameCard(word, types[idx])
        })
    }

    fun handleCardClick(index: Int) {
        if (gameOverWinner != null || gridCards[index].isRevealed) return

        haptics.performPop()
        val card = gridCards[index]
        val updated = gridCards.toMutableList()
        updated[index] = card.copy(isRevealed = true)
        gridCards = updated

        when (card.type) {
            CodenameCardType.RED -> {
                redScore++
                if (redScore >= 9) gameOverWinner = "RED TEAM WINS! 🎉"
                if (!isRedTurn) isRedTurn = true
            }
            CodenameCardType.BLUE -> {
                blueScore++
                if (blueScore >= 8) gameOverWinner = "BLUE TEAM WINS! 🎉"
                if (isRedTurn) isRedTurn = false
            }
            CodenameCardType.CIVILIAN -> {
                isRedTurn = !isRedTurn
            }
            CodenameCardType.ASSASSIN -> {
                haptics.performHeavyBurst()
                gameOverWinner = if (isRedTurn) "BLUE TEAM WINS! (Red hit Assassin ☠️)" else "RED TEAM WINS! (Blue hit Assassin ☠️)"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C15))
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
                    text = "CODENAMES 🕵️",
                    color = Color(0xFF00F2FE),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Status Scoreboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF0055).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("RED: $redScore/9", color = Color(0xFFFF0055), fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                Text(
                    text = if (gameOverWinner != null) gameOverWinner!! else if (isRedTurn) "TURN: RED TEAM 🔴" else "TURN: BLUE TEAM 🔵",
                    color = if (isRedTurn) Color(0xFFFF0055) else Color(0xFF00F2FE),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00F2FE).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF00F2FE), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("BLUE: $blueScore/8", color = Color(0xFF00F2FE), fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }

            // 5x5 Grid Canvas Cards
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0..4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (col in 0..4) {
                            val index = row * 5 + col
                            val card = gridCards[index]
                            val isShown = card.isRevealed || isSpymasterView

                            val bgColor = if (isShown) card.type.color.copy(alpha = 0.85f) else SurfaceGlassDark
                            val borderColor = if (isShown) card.type.color else BorderGlassDefault

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                    .clickable { handleCardClick(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = card.word,
                                    color = if (isShown && card.type == CodenameCardType.ASSASSIN) Color.White else TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Anti-Cheat Spymaster View Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        haptics.performPop()
                        isSpymasterView = !isSpymasterView
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpymasterView) Color(0xFFFFD166) else Color.White.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isSpymasterView) "CLOSE SPYMASTER KEY 🔒" else "SPYMASTER KEY 🔑",
                        color = if (isSpymasterView) Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        haptics.performPop()
                        isRedTurn = !isRedTurn
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("END TURN ⏭️", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
