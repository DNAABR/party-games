package com.leminno.partygames.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
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
    accentColor: Color = AccentCyan,
    cornerRadius: Dp = 16.dp
) {
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    val haptics = remember { HapticFeedbackManager(context) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnScale"
    )

    val backgroundBrush = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(
                accentColor,
                accentColor.copy(alpha = 0.85f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                SurfaceElevated,
                SurfaceElevated
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundBrush)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.performTick(composeHaptics)
                    onClick()
                }
            )
            .padding(horizontal = 24.dp),
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
                    tint = if (enabled) TextOnAccent else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text.uppercase(),
                color = if (enabled) TextOnAccent else TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
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
    borderColor: Color = BorderGlassDefault,
    cornerRadius: Dp = 16.dp
) {
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    val haptics = remember { HapticFeedbackManager(context) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "secBtnScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(50.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (isPressed) SurfaceGlassLight else SurfaceGlassDark)
            .border(1.dp, if (isPressed) BorderGlassActive else borderColor, RoundedCornerShape(cornerRadius))
            .clickable(
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
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
