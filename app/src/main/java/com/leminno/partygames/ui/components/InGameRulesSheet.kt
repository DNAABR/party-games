package com.leminno.partygames.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.model.GameItem
import com.leminno.partygames.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InGameRulesSheet(
    game: GameItem,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = PixelVioletElevated,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(2.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(0.dp))
                            .background(
                                brush = pixelBandedVertical(
                                    listOf(PixelMagentaHighlight, PixelMagentaHot)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PixelIcons.Lightbulb,
                            contentDescription = null,
                            tint = PixelOutlineBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = game.title.uppercase(),
                            color = PixelCrtCyan,
                            fontFamily = PressStart2PFont,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "RULES & HOW TO PLAY",
                            color = PixelMagentaHot,
                            fontFamily = PressStart2PFont,
                            fontSize = 8.sp
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = PixelIcons.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rules Steps List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(game.rules) { step ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                            .background(
                                brush = pixelBandedVertical(
                                    listOf(PixelVioletBase, PixelVioletDark)
                                ),
                                shape = RoundedCornerShape(2.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.5.dp, PixelOutlineBlack)
                                    .background(PixelAmberGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = step.stepNumber.toString(),
                                    color = PixelOutlineBlack,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = step.title,
                                    color = TextPrimary,
                                    fontFamily = PressStart2PFont,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = step.description,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                game.antiCheatNotice?.let { notice ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, PixelAlertRed, RoundedCornerShape(2.dp))
                                .background(PixelVioletDark)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = PixelIcons.Shield,
                                    contentDescription = "Notice",
                                    tint = PixelAlertRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = notice,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryPartyButton(
                text = "RESUME GAME",
                onClick = onDismissRequest,
                accentColor = PixelCrtCyan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
