package com.leminno.partygames.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*

@Composable
fun PrimaryPartyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    accentColor: Color = PixelCrtCyan,
    cornerRadius: Dp = 2.dp
) {
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    val haptics = remember { HapticFeedbackManager(context) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY = if (isPressed && enabled) 3.dp else 0.dp

    val backgroundBrush = if (enabled) {
        if (accentColor == PixelCrtCyan) {
            pixelBandedVertical(listOf(PixelCrtCyanHighlight, PixelCrtCyan, PixelCrtCyanShadow))
        } else if (accentColor == PixelMagentaHot) {
            pixelBandedVertical(listOf(PixelMagentaHighlight, PixelMagentaHot, PixelMagentaShadow))
        } else if (accentColor == PixelEmeraldGreen) {
            pixelBandedVertical(listOf(Color(0xFF80FFC2), PixelEmeraldGreen, PixelEmeraldShadow))
        } else {
            pixelBandedVertical(listOf(accentColor, accentColor, accentColor))
        }
    } else {
        pixelBandedVertical(listOf(Color(0xFF555566), Color(0xFF333344)))
    }

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .height(50.dp)
            .border(3.dp, PixelOutlineBlack, RoundedCornerShape(cornerRadius))
            .background(backgroundBrush, shape = RoundedCornerShape(cornerRadius))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.performTick(composeHaptics)
                    onClick()
                }
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) PixelOutlineBlack else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text.uppercase(),
                color = if (enabled) PixelOutlineBlack else TextMuted,
                fontFamily = PressStart2PFont,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SecondaryPartyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    borderColor: Color = PixelOutlineBlack,
    cornerRadius: Dp = 2.dp
) {
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    val haptics = remember { HapticFeedbackManager(context) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY = if (isPressed) 3.dp else 0.dp

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .height(48.dp)
            .border(2.5.dp, borderColor, RoundedCornerShape(cornerRadius))
            .background(
                brush = if (isPressed) {
                    pixelBandedVertical(listOf(PixelVioletBase, PixelVioletDark))
                } else {
                    pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase))
                },
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.performTick(composeHaptics)
                    onClick()
                }
            )
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text.uppercase(),
                color = TextPrimary,
                fontFamily = PressStart2PFont,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
