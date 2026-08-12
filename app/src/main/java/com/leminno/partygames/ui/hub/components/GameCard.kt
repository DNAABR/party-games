package com.leminno.partygames.ui.hub.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.model.GameItem
import com.leminno.partygames.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

@Composable
fun GameCard(
    game: GameItem,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryToken = CategoryThemeToken.forCategory(game.category)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile physical scale-down on press with spring physics
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cardScale"
    )

    // Border brightens on interaction
    val borderAlpha = if (isPressed) 0.9f else 0.4f
    val borderColor = categoryToken.primaryAccent.copy(alpha = borderAlpha)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(categoryToken.cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceGlassDark,
                        categoryToken.backgroundGlow
                    )
                )
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(categoryToken.cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Category Icon, Setup Type & Favorite Heart Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon Symbol + Title Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryToken.primaryAccent.copy(alpha = 0.2f))
                        .border(1.dp, categoryToken.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${game.category.iconSymbol} ${game.category.title.split(" ").first()}",
                        color = categoryToken.primaryAccent,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Setup Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceGlassLight)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${game.setupType.badgeIcon}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (onToggleFavorite != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onToggleFavorite)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFFF2A6D) else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Game Title
            Text(
                text = game.title,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tagline / Short description
            Text(
                text = game.tagLine,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Info Bar: Players & Estimated Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 ${game.minPlayers}-${game.maxPlayers} Players",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "⏱️ ~${game.estTimeMinutes}m",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

