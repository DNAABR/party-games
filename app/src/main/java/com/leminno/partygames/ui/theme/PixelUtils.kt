package com.leminno.partygames.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// LEMINNO AIR - MODERN DESIGN UTILITIES & MODIFIERS
// ============================================================================

/**
 * Creates smooth modern vertical gradients for subtle surface card fills.
 */
fun smoothGradientVertical(colors: List<Color>): Brush {
    return if (colors.size <= 1) Brush.verticalGradient(colors) else Brush.verticalGradient(colors)
}

/**
 * Backward compatibility alias for pixelBandedVertical, rendering smooth gradients.
 */
fun pixelBandedVertical(colors: List<Color>): Brush {
    return Brush.verticalGradient(colors)
}

/**
 * Backward compatibility alias for pixelBandedHorizontal.
 */
fun pixelBandedHorizontal(colors: List<Color>): Brush {
    return Brush.horizontalGradient(colors)
}

/**
 * Modern tactile scale bounce animation on press.
 */
fun Modifier.springPressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "pressScale"
    )
    this.scale(scale)
}

/**
 * Subtle ambient elevation shadow helper with clean modern corner radius.
 */
fun Modifier.subtleCardShadow(
    elevation: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    ambientColor: Color = Color(0x0F0F172A),
    spotColor: Color = Color(0x0A0F172A)
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = ambientColor,
    spotColor = spotColor
)

/**
 * Backward-compatible helper for legacy pixelHaloBorder, rendering clean hairline outline.
 */
fun Modifier.pixelHaloBorder(
    haloColor: Color = BorderSubtle,
    borderColor: Color = BorderSubtle,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = this
    .border(width = borderWidth, color = borderColor, shape = shape)

/**
 * Backward-compatible helper for legacy crtScanlines (now a clean subtle container with no scanlines).
 */
fun Modifier.crtScanlines(
    scanlineColor: Color = Color.Transparent,
    lineHeightPx: Float = 0f
): Modifier = this

