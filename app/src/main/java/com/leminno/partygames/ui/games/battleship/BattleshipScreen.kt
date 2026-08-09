package com.leminno.partygames.ui.games.battleship

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
import com.leminno.partygames.ui.components.GameScaffold
import com.leminno.partygames.ui.components.VictoryCeremonyOverlay
import com.leminno.partygames.ui.theme.*

enum class BattleCellState {
    EMPTY, SHIP, HIT, MISS
}

@Composable
fun BattleshipScreen(
    onExitGame: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { HapticFeedbackManager(context) }

    val gridSize = 6 // 6x6 grid for fast mobile party play
    var player1Board by remember { mutableStateOf(List(gridSize * gridSize) { BattleCellState.EMPTY }) }
    var player2Board by remember { mutableStateOf(List(gridSize * gridSize) { BattleCellState.EMPTY }) }

    var gamePhase by remember { mutableStateOf("P1_PLACEMENT") } // P1_PLACEMENT, PASS_PRIVACY, P2_PLACEMENT, BATTLE_P1, P2_BATTLE, GAME_OVER
    var winnerText by remember { mutableStateOf<String?>(null) }
    var actionLog by remember { mutableStateOf("Place 4 ships on your grid!") }

    fun placeShip(board: List<BattleCellState>, index: Int): List<BattleCellState> {
        val currentShipCount = board.count { it == BattleCellState.SHIP }
        if (currentShipCount >= 4) return board

        val mutable = board.toMutableList()
        mutable[index] = if (mutable[index] == BattleCellState.SHIP) BattleCellState.EMPTY else BattleCellState.SHIP
        return mutable
    }

    fun fireSalvo(targetBoard: List<BattleCellState>, index: Int): Pair<List<BattleCellState>, Boolean> {
        val mutable = targetBoard.toMutableList()
        val cell = mutable[index]
        if (cell == BattleCellState.HIT || cell == BattleCellState.MISS) return Pair(targetBoard, false)

        val isHit = cell == BattleCellState.SHIP
        mutable[index] = if (isHit) BattleCellState.HIT else BattleCellState.MISS
        return Pair(mutable, isHit)
    }

    GameScaffold(
        title = "BATTLESHIP ⚓",
        titleColor = Color(0xFF00F2FE),
        gameId = "battleship",
        onExitGame = onExitGame
    ) {
        if (gamePhase != "GAME_OVER") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (gamePhase == "PASS_PRIVACY") {
                    // Full Screen Privacy Cover
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔒 PASS PHONE TO NEXT PLAYER", color = Color(0xFF00F2FE), fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Screen obscured to protect secret fleet positions!", color = TextMuted, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            gamePhase = if (player2Board.count { it == BattleCellState.SHIP } < 4) "P2_PLACEMENT" else "BATTLE_P1"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("READY! UNLOCK SCREEN ▶", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "P1_PLACEMENT" || gamePhase == "P2_PLACEMENT") {
                    val isP1 = gamePhase == "P1_PLACEMENT"
                    val activeBoard = if (isP1) player1Board else player2Board
                    val shipCount = activeBoard.count { it == BattleCellState.SHIP }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isP1) "PLAYER 1: DEPLOY FLEET" else "PLAYER 2: DEPLOY FLEET",
                            color = Color(0xFF00F2FE),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap grid cells to place 4 ships ($shipCount/4 placed)", color = TextMuted, fontSize = 12.sp)
                    }

                    // 6x6 Placement Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until gridSize) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (col in 0 until gridSize) {
                                    val idx = row * gridSize + col
                                    val cell = activeBoard[idx]

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (cell == BattleCellState.SHIP) Color(0xFF00F2FE) else SurfaceGlassDark)
                                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(8.dp))
                                            .clickable {
                                                haptics.performTick()
                                                if (isP1) {
                                                    player1Board = placeShip(player1Board, idx)
                                                } else {
                                                    player2Board = placeShip(player2Board, idx)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cell == BattleCellState.SHIP) {
                                            Text("🚢", fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            gamePhase = "PASS_PRIVACY"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = shipCount == 4
                    ) {
                        Text("LOCK FLEET & PASS DEVICE ▶", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else if (gamePhase == "BATTLE_P1" || gamePhase == "P2_BATTLE") {
                    val isP1Turn = gamePhase == "BATTLE_P1"
                    val targetBoard = if (isP1Turn) player2Board else player1Board

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isP1Turn) "PLAYER 1: CALL SALVO STRIKE 💥" else "PLAYER 2: CALL SALVO STRIKE 💥",
                            color = Color(0xFFFF0055),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(actionLog, color = TextPrimary, fontSize = 13.sp)
                    }

                    // 6x6 Strike Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until gridSize) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (col in 0 until gridSize) {
                                    val idx = row * gridSize + col
                                    val cell = targetBoard[idx]

                                    val (bg, icon) = when (cell) {
                                        BattleCellState.HIT -> Pair(Color(0xFFFF0055), "💥")
                                        BattleCellState.MISS -> Pair(Color(0xFF8D99AE).copy(alpha = 0.3f), "💧")
                                        else -> Pair(SurfaceGlassDark, "")
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (cell == BattleCellState.EMPTY || cell == BattleCellState.SHIP) {
                                                    val (updated, isHit) = fireSalvo(targetBoard, idx)
                                                    if (isP1Turn) player2Board = updated else player1Board = updated

                                                    if (isHit) {
                                                        haptics.performHeavyBurst()
                                                        actionLog = "DIRECT HIT! 💥 Fire again or end turn."
                                                    } else {
                                                        haptics.performWarningThud()
                                                        actionLog = "SPLASH! MISS! 💧 Turn passes."
                                                    }

                                                    val remainingHitsNeeded = updated.count { it == BattleCellState.SHIP }
                                                    if (remainingHitsNeeded == 0) {
                                                        winnerText = if (isP1Turn) "PLAYER 1 SUNK THE FLEET! VICTORY! 🏆" else "PLAYER 2 SUNK THE FLEET! VICTORY! 🏆"
                                                        gamePhase = "GAME_OVER"
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(icon, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            haptics.performPop()
                            gamePhase = if (isP1Turn) "P2_BATTLE" else "BATTLE_P1"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("END TURN & PASS PHONE 🔒", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            VictoryCeremonyOverlay(
                winnerTitle = winnerText ?: "VICTORY! 🏆",
                subtitle = "Supreme Naval Commander!",
                onPlayAgain = {
                    player1Board = List(gridSize * gridSize) { BattleCellState.EMPTY }
                    player2Board = List(gridSize * gridSize) { BattleCellState.EMPTY }
                    gamePhase = "P1_PLACEMENT"
                    winnerText = null
                    actionLog = "Place 4 ships on your grid!"
                },
                onBackToHub = onExitGame
            )
        }
    }
}
