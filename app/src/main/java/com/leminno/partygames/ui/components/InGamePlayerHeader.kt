package com.leminno.partygames.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun InGamePlayerHeader(
    currentPlayerName: String,
    playerIndex: Int,
    totalPlayers: Int,
    onOpenScoreboard: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sessionScores by UserPreferencesRepository.sessionScores.collectAsState()
    val playerScore = sessionScores.getOrDefault(currentPlayerName, 0)
    val avatarColor = PlayerAvatarColors.getOrElse(playerIndex % PlayerAvatarColors.size) { PixelCrtCyan }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
            .background(
                brush = pixelBandedVertical(
                    listOf(PixelVioletElevated, PixelVioletBase)
                ),
                shape = RoundedCornerShape(2.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Avatar & Turn Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(1.5.dp, PixelOutlineBlack)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentPlayerName.take(1).uppercase(),
                        color = PixelOutlineBlack,
                        fontFamily = PressStart2PFont,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ ${currentPlayerName.uppercase()}",
                            color = PixelCrtCyan,
                            fontFamily = PressStart2PFont,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "PLAYER ${playerIndex + 1} OF $totalPlayers • $playerScore PTS",
                        color = PixelAmberGold,
                        fontFamily = PressStart2PFont,
                        fontSize = 7.sp
                    )
                }
            }

            // Trophy / Scoreboard Icon Trigger
            if (onOpenScoreboard != null) {
                Box(
                    modifier = Modifier
                        .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                        .background(PixelVioletDark)
                        .clickable { onOpenScoreboard() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = PixelIcons.Trophy,
                            contentDescription = "Scoreboard",
                            tint = PixelAmberGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "SCORE",
                            color = PixelAmberGold,
                            fontFamily = PressStart2PFont,
                            fontSize = 7.sp
                        )
                    }
                }
            }
        }
    }
}
