package com.leminno.partygames.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.theme.*

val PlayerAvatarColors = listOf(
    PixelCrtCyan,
    PixelMagentaHot,
    PixelAmberGold,
    PixelEmeraldGreen,
    Color(0xFFFF7043),
    Color(0xFFAB47BC),
    Color(0xFF26A69A),
    Color(0xFFFFCA28)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InGameScoreboardModal(
    players: List<String>,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sessionScores by UserPreferencesRepository.sessionScores.collectAsState()

    // Find highest score for winner crown
    val highestScore = players.maxOfOrNull { sessionScores.getOrDefault(it, 0) } ?: 0

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = PixelVioletElevated,
        scrimColor = Color(0xCC000000),
        shape = RoundedCornerShape(2.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PixelIcons.Trophy,
                        contentDescription = null,
                        tint = PixelAmberGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PARTY SCOREBOARD",
                        color = PixelAmberGold,
                        fontFamily = PressStart2PFont,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PixelIcons.Close,
                        contentDescription = "Close",
                        tint = PixelMagentaHot,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player List with Score Controls
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(players) { index, player ->
                    val color = PlayerAvatarColors[index % PlayerAvatarColors.size]
                    val score = sessionScores.getOrDefault(player, 0)
                    val isLeader = score > 0 && score == highestScore

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, if (isLeader) PixelAmberGold else PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(
                                brush = pixelBandedVertical(
                                    listOf(PixelVioletBase, PixelVioletDark)
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Player Color / Initial Box
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .border(1.5.dp, PixelOutlineBlack)
                                        .background(color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = player.take(1).uppercase(),
                                        color = PixelOutlineBlack,
                                        fontFamily = PressStart2PFont,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = player.uppercase(),
                                            color = TextPrimary,
                                            fontFamily = PressStart2PFont,
                                            fontSize = 9.sp,
                                            maxLines = 1
                                        )
                                        if (isLeader) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "👑",
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Score: $score pts",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            // -1 and +1 Buttons
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                                        .background(PixelAlertRed)
                                ) {
                                    IconButton(
                                        onClick = {
                                            UserPreferencesRepository.updatePlayerScore(player, -1)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "-",
                                            color = Color.White,
                                            fontFamily = PressStart2PFont,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                                        .background(PixelEmeraldGreen)
                                ) {
                                    IconButton(
                                        onClick = {
                                            UserPreferencesRepository.updatePlayerScore(player, 1)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "+",
                                            color = PixelOutlineBlack,
                                            fontFamily = PressStart2PFont,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecondaryPartyButton(
                    text = "RESET",
                    onClick = {
                        UserPreferencesRepository.resetPlayerScores()
                    },
                    modifier = Modifier.weight(0.35f)
                )

                PrimaryPartyButton(
                    text = "RESUME",
                    accentColor = PixelCrtCyan,
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(0.65f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
