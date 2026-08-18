package com.leminno.partygames.ui.hub.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.model.GameCategory
import com.leminno.partygames.ui.model.GameItem
import com.leminno.partygames.ui.theme.*

@Composable
fun GameCard(
    game: GameItem,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY = if (isPressed) 3.dp else 0.dp
    val borderColor = if (isPressed) PixelMagentaHot else PixelOutlineBlack

    val categoryIcon: ImageVector = when (game.category) {
        GameCategory.TRIVIA -> PixelIcons.Lightbulb
        GameCategory.ACTION -> PixelIcons.Zap
        GameCategory.MYSTERY -> PixelIcons.Eye
        GameCategory.BOARD -> PixelIcons.Dice
    }

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .border(width = 3.dp, color = borderColor, shape = RoundedCornerShape(2.dp))
            .background(
                brush = pixelBandedVertical(
                    listOf(
                        PixelVioletElevated,
                        PixelVioletBase,
                        PixelVioletDark
                    )
                ),
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Category Badge & Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Stepped Pixel Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.5.dp, PixelOutlineBlack, RoundedCornerShape(0.dp))
                        .background(
                            brush = pixelBandedVertical(
                                listOf(PixelMagentaHighlight, PixelMagentaHot, PixelMagentaShadow)
                            )
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = PixelOutlineBlack,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = game.category.title.split(" ").first().uppercase(),
                        color = PixelOutlineBlack,
                        fontFamily = PressStart2PFont,
                        fontSize = 8.sp
                    )
                }

                if (onToggleFavorite != null) {
                    Box(
                        modifier = Modifier
                            .size(48.dp) // Minimum 48dp touch target
                            .clickable(onClick = onToggleFavorite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) PixelIcons.Heart else PixelIcons.HeartBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) PixelAlertRed else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Game Title (Arcade Pixel Marquee)
            Text(
                text = game.title.uppercase(),
                color = PixelCrtCyan,
                fontFamily = PressStart2PFont,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            Text(
                text = game.tagLine,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Info CRT Strip (Banded Scanline Background)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, PixelOutlineBlack)
                    .background(PixelCrtDarkCanvas)
                    .crtScanlines()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PixelIcons.Users,
                        contentDescription = "Player count",
                        tint = PixelAmberGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${game.minPlayers}-${game.maxPlayers}P",
                        color = PixelAmberGold,
                        fontFamily = PressStart2PFont,
                        fontSize = 8.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PixelIcons.Clock,
                        contentDescription = "Duration",
                        tint = PixelAmberGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${game.estTimeMinutes}M",
                        color = PixelAmberGold,
                        fontFamily = PressStart2PFont,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
