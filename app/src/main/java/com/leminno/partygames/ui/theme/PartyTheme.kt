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
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// LEMINNO AIR - THEME CONFIGURATION (LIGHT MODE FIRST)
// ============================================================================

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandPrimaryText,
    secondary = BrandSecondary,
    onSecondary = TextOnPrimary,
    secondaryContainer = BrandSecondaryContainer,
    onSecondaryContainer = BrandSecondaryText,
    tertiary = TriviaPrimary,
    onTertiary = TextOnPrimary,
    background = CanvasLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtleLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF082F49),
    tertiary = Color(0xFFA78BFA),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

@Composable
fun PartyGamesTheme(
    darkTheme: Boolean = false, // Light mode first as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PartyTypography,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
            ) {
                content()
            }
        }
    )
}

/**
 * Modifier helper for clean, modern cards featuring continuous curve radiuses and subtle hairline borders.
 */
fun Modifier.modernCard(
    cornerRadius: Dp = 20.dp,
    borderColor: Color = BorderSubtle,
    backgroundColor: Color = SurfaceLight,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))

/**
 * Backward-compatible helper for legacy retroCard calls, redirecting to the modern aesthetic.
 */
fun Modifier.retroCard(
    cornerRadius: Dp = 18.dp,
    borderColor: Color = BorderSubtle,
    backgroundColor: Color = SurfaceLight,
    borderWidth: Dp = 1.dp
): Modifier = modernCard(
    cornerRadius = cornerRadius,
    borderColor = borderColor,
    backgroundColor = backgroundColor,
    borderWidth = borderWidth
)

/**
 * Clickable helper without ripple for custom tactile press handling.
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

