package com.leminno.partygames.ui.games.connectfour

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*

@Composable
fun ConnectFourScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    // 7 Columns x 6 Rows Board state (0 = Empty, 1 = Red P1, 2 = Yellow P2)
    val grid = remember { mutableStateListOf(*Array(6) { IntArray(7) }) }
    var isRedTurn by remember { mutableStateOf(true) }
    var winnerPlayer by remember { mutableStateOf<Int?>(null) } // 1, 2, or null

    fun dropDisc(col: Int) {
        if (winnerPlayer != null) return
        for (row in 5 downTo 0) {
            if (grid[row][col] == 0) {
                grid[row][col] = if (isRedTurn) 1 else 2
                haptics.performPop()

                // Check Win
                val targetPlayer = if (isRedTurn) 1 else 2
                if (checkWin(grid, row, col, targetPlayer)) {
                    winnerPlayer = targetPlayer
                    haptics.performHeavyBurst()
                } else {
                    isRedTurn = !isRedTurn
                }
                break
            }
        }
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
                    text = "CONNECT FOUR 🟡",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                val turnColor = if (isRedTurn) Color(0xFFFF4D4D) else Color(0xFFFFD700)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(turnColor.copy(alpha = 0.2f))
                        .border(1.dp, turnColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (winnerPlayer != null) "PLAYER $winnerPlayer WINS! 🏆" else if (isRedTurn) "RED TURN 🔴" else "YELLOW TURN 🟡",
                        color = turnColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 7x6 Game Board Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(7f / 6f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B))
                    .border(2.dp, BorderGlassDefault, RoundedCornerShape(20.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    for (row in 0 until 6) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0 until 7) {
                                val cellVal = grid[row][col]
                                val cellColor by animateColorAsState(
                                    targetValue = when (cellVal) {
                                        1 -> Color(0xFFFF4D4D)
                                        2 -> Color(0xFFFFD700)
                                        else -> Color(0xFF0F172A)
                                    },
                                    label = "cellColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(cellColor)
                                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                                        .clickable {
                                            dropDisc(col)
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Reset Game Button
            Button(
                onClick = {
                    for (r in 0 until 6) {
                        for (c in 0 until 7) {
                            grid[r][c] = 0
                        }
                    }
                    winnerPlayer = null
                    isRedTurn = true
                    haptics.performPop()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE))
            ) {
                Text("RESTART MATCH 🔄", color = BackgroundObsidian, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

private fun checkWin(board: List<IntArray>, row: Int, col: Int, player: Int): Boolean {
    val directions = listOf(
        Pair(0, 1),  // Horizontal
        Pair(1, 0),  // Vertical
        Pair(1, 1),  // Diagonal Down-Right
        Pair(1, -1)  // Diagonal Down-Left
    )

    for ((dr, dc) in directions) {
        var count = 1
        // Positive direction
        var r = row + dr
        var c = col + dc
        while (r in 0 until 6 && c in 0 until 7 && board[r][c] == player) {
            count++
            r += dr
            c += dc
        }
        // Negative direction
        r = row - dr
        c = col - dc
        while (r in 0 until 6 && c in 0 until 7 && board[r][c] == player) {
            count++
            r -= dr
            c -= dc
        }

        if (count >= 4) return true
    }
    return false
}
