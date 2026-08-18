package com.leminno.partygames.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentViolet,
    tertiary = AccentMagenta,
    background = BackgroundObsidian,
    surface = SurfaceGlassDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun PartyGamesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = PartyTypography,
        content = {
            // Obsidian background canvas with retro ambient arcade radial glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF14142B),
                                BackgroundNavySlate,
                                BackgroundObsidian
                            ),
                            radius = 1800f
                        )
                    )
            ) {
                content()
            }
        }
    )
}

/**
 * Modifier helper for retro arcade cards featuring blocky corner radiuses and glowing stroke borders.
 */
fun Modifier.retroCard(
    cornerRadius: Dp = 14.dp,
    borderColor: Color = BorderGlassDefault,
    backgroundColor: Color = SurfaceGlassDark,
    borderWidth: Dp = 2.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))

/**
 * No-ripple clickable helper for crisp retro touch interactions.
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

