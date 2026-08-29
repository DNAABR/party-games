package com.leminno.partygames.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leminno.partygames.data.model.GameCategory

enum class MotionPersonality {
    SPRING_BOUNCY,     // Trivia: playful curves & smooth spring response
    HIGH_VELOCITY,     // Action: snappy tactile transitions
    SLOW_AMBIENT,      // Mystery: smooth fade & subtle reveals
    DELIBERATE_SLIDE   // Board: structured, clean sliding
}

enum class HapticProfile {
    PLAYFUL_POPS,      // Short, light double ticks
    REACTIVE_PULSE,    // High-velocity sharp taps
    MYSTERIOUS_RUMBLE, // Low frequency subtle rumble
    TACTILE_CLICK      // Firm mechanical click
}

data class CategoryThemeToken(
    val category: GameCategory,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val containerColor: Color,
    val textColor: Color,
    val surfaceBorder: Color,
    val backgroundGlow: Color,
    val cornerRadius: Dp,
    val motionPersonality: MotionPersonality,
    val hapticProfile: HapticProfile
) {
    companion object {
        val TriviaToken = CategoryThemeToken(
            category = GameCategory.TRIVIA,
            primaryAccent = TriviaPrimary,
            secondaryAccent = Color(0xFFA78BFA),
            containerColor = TriviaContainer,
            textColor = TriviaText,
            surfaceBorder = TriviaBorder,
            backgroundGlow = Color(0x1A8B5CF6),
            cornerRadius = 20.dp,
            motionPersonality = MotionPersonality.SPRING_BOUNCY,
            hapticProfile = HapticProfile.PLAYFUL_POPS
        )

        val ActionToken = CategoryThemeToken(
            category = GameCategory.ACTION,
            primaryAccent = ActionPrimary,
            secondaryAccent = Color(0xFF38BDF8),
            containerColor = ActionContainer,
            textColor = ActionText,
            surfaceBorder = ActionBorder,
            backgroundGlow = Color(0x1A0284C7),
            cornerRadius = 18.dp,
            motionPersonality = MotionPersonality.HIGH_VELOCITY,
            hapticProfile = HapticProfile.REACTIVE_PULSE
        )

        val MysteryToken = CategoryThemeToken(
            category = GameCategory.MYSTERY,
            primaryAccent = MysteryPrimary,
            secondaryAccent = Color(0xFF34D399),
            containerColor = MysteryContainer,
            textColor = MysteryText,
            surfaceBorder = MysteryBorder,
            backgroundGlow = Color(0x1A059669),
            cornerRadius = 20.dp,
            motionPersonality = MotionPersonality.SLOW_AMBIENT,
            hapticProfile = HapticProfile.MYSTERIOUS_RUMBLE
        )

        val BoardToken = CategoryThemeToken(
            category = GameCategory.BOARD,
            primaryAccent = BoardPrimary,
            secondaryAccent = Color(0xFFFB923C),
            containerColor = BoardContainer,
            textColor = BoardText,
            surfaceBorder = BoardBorder,
            backgroundGlow = Color(0x1AEA580C),
            cornerRadius = 18.dp,
            motionPersonality = MotionPersonality.DELIBERATE_SLIDE,
            hapticProfile = HapticProfile.TACTILE_CLICK
        )

        fun forCategory(category: GameCategory): CategoryThemeToken {
            return when (category) {
                GameCategory.TRIVIA -> TriviaToken
                GameCategory.ACTION -> ActionToken
                GameCategory.MYSTERY -> MysteryToken
                GameCategory.BOARD -> BoardToken
            }
        }
    }
}

