package com.leminno.partygames.ui.games.scrabble_league

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

data class TileCell(
    val letter: Char? = null,
    val points: Int = 0
)

val tileLettersBag = listOf(
    'A', 'A', 'B', 'C', 'D', 'E', 'E', 'F', 'G', 'H', 'I', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
)

@Composable
fun ScrabbleLeagueScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    val boardSize = 5 // 5x5 board grid for fast mobile word placement
    var boardGrid by remember { mutableStateOf(List(boardSize * boardSize) { TileCell() }) }
    var player1Score by remember { mutableIntStateOf(0) }
    var player2Score by remember { mutableIntStateOf(0) }
    var isPlayer1Turn by remember { mutableStateOf(true) }

    var isRackOverlayVisible by remember { mutableStateOf(false) }
    var player1Rack by remember { mutableStateOf((1..7).map { tileLettersBag.random() }) }
    var player2Rack by remember { mutableStateOf((1..7).map { tileLettersBag.random() }) }
    var selectedRackTile by remember { mutableStateOf<Char?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A121A))
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
                    text = "LETTER LEAGUE 🔠",
                    color = Color(0xFFFFD166),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Scoreboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00F2FE).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF00F2FE), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("P1 SCORE: $player1Score", color = Color(0xFF00F2FE), fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                Text(
                    text = if (isPlayer1Turn) "TURN: PLAYER 1 🔵" else "TURN: PLAYER 2 🔴",
                    color = if (isPlayer1Turn) Color(0xFF00F2FE) else Color(0xFFFF0055),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF0055).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("P2 SCORE: $player2Score", color = Color(0xFFFF0055), fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }

            // 5x5 Scrabble Board Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until boardSize) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (col in 0 until boardSize) {
                            val idx = row * boardSize + col
                            val cell = boardGrid[idx]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (cell.letter != null) Color(0xFFFFD166) else SurfaceGlassDark)
                                    .border(1.dp, BorderGlassDefault, RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (selectedRackTile != null && cell.letter == null) {
                                            haptics.performPop()
                                            val updated = boardGrid.toMutableList()
                                            updated[idx] = TileCell(selectedRackTile, 2)
                                            boardGrid = updated

                                            // Remove from current active rack
                                            if (isPlayer1Turn) {
                                                val rackMut = player1Rack.toMutableList()
                                                rackMut.remove(selectedRackTile)
                                                rackMut.add(tileLettersBag.random())
                                                player1Rack = rackMut
                                                player1Score += 2
                                            } else {
                                                val rackMut = player2Rack.toMutableList()
                                                rackMut.remove(selectedRackTile)
                                                rackMut.add(tileLettersBag.random())
                                                player2Rack = rackMut
                                                player2Score += 2
                                            }

                                            selectedRackTile = null
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (cell.letter != null) {
                                    Text(
                                        text = "${cell.letter}",
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Anti-Cheat Secret Tile Rack Overlay & Turn Controls
            if (isRackOverlayVisible) {
                val currentRack = if (isPlayer1Turn) player1Rack else player2Rack

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceGlassDark)
                        .border(1.5.dp, Color(0xFFFFD166), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SECRET TILE RACK", color = Color(0xFFFFD166), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentRack.forEach { char ->
                                val isSelected = selectedRackTile == char
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF00E676) else Color(0xFFFFD166))
                                        .clickable {
                                            haptics.performTick()
                                            selectedRackTile = char
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$char", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        haptics.performPop()
                        isRackOverlayVisible = !isRackOverlayVisible
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isRackOverlayVisible) "HIDE TILE RACK 🔒" else "VIEW MY TILES 🔠",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        haptics.performPop()
                        isPlayer1Turn = !isPlayer1Turn
                        isRackOverlayVisible = false
                        selectedRackTile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("PASS TURN ⏭️", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }

            TextButton(onClick = onExitGame) {
                Text("Back to Hub", color = TextMuted)
            }
        }
    }
}
