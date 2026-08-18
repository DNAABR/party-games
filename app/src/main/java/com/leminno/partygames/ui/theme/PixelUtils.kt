package com.leminno.partygames.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Creates discrete stepped flat color bands for background panels and screen surfaces,
 * completely avoiding soft/blurred linear gradient transitions.
 */
fun pixelBandedVertical(colors: List<Color>): Brush {
    if (colors.size <= 1) return Brush.verticalGradient(colors)
    val stops = mutableListOf<Pair<Float, Color>>()
    val stepSize = 1.0f / colors.size
    colors.forEachIndexed { index, color ->
        stops.add(index * stepSize to color)
        stops.add((index + 1) * stepSize to color)
    }
    return Brush.verticalGradient(colorStops = stops.toTypedArray())
}

/**
 * Creates discrete stepped horizontal color bands.
 */
fun pixelBandedHorizontal(colors: List<Color>): Brush {
    if (colors.size <= 1) return Brush.horizontalGradient(colors)
    val stops = mutableListOf<Pair<Float, Color>>()
    val stepSize = 1.0f / colors.size
    colors.forEachIndexed { index, color ->
        stops.add(index * stepSize to color)
        stops.add((index + 1) * stepSize to color)
    }
    return Brush.horizontalGradient(colorStops = stops.toTypedArray())
}

/**
 * Layered pixel halo outline modifier that replaces gaussian blur / alpha glow
 * with crisp, hard-edged pixel borders.
 */
fun Modifier.pixelHaloBorder(
    haloColor: Color = PixelMagentaHot,
    borderColor: Color = PixelOutlineBlack,
    borderWidth: Dp = 3.dp,
    shape: Shape = RectangleShape
): Modifier = this
    .border(width = borderWidth + 2.dp, color = haloColor, shape = shape)
    .border(width = borderWidth, color = borderColor, shape = shape)

/**
 * Draws discrete horizontal CRT scanline pixel bands across a surface.
 * Ensures anti-aliasing is disabled so lines stay pixel-crisp.
 */
fun Modifier.crtScanlines(
    scanlineColor: Color = PixelCrtScanlineBand.copy(alpha = 0.4f),
    lineHeightPx: Float = 4f
): Modifier = this.drawWithContent {
    drawContent()
    val paint = Paint().apply {
        color = scanlineColor
        style = PaintingStyle.Fill
        isAntiAlias = false
    }
    var y = 0f
    val canvasHeight = size.height
    val canvasWidth = size.width
    while (y < canvasHeight) {
        drawContext.canvas.drawRect(
            left = 0f,
            top = y,
            right = canvasWidth,
            bottom = (y + lineHeightPx / 2f).coerceAtMost(canvasHeight),
            paint = paint
        )
        y += lineHeightPx
    }
}
