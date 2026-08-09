package com.leminno.partygames.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.leminno.partygames.data.model.GameCategory

enum class MotionPersonality {
    SPRING_BOUNCY,     // Trivia: playful curved curves & bouncy springs
    HIGH_VELOCITY,     // Action: fast diagonal cut animations
    SLOW_AMBIENT,      // Mystery: smooth fade glows & veiled reveals
    DELIBERATE_SLIDE   // Board: crisp, structured mechanical sliding
}

enum class HapticProfile {
    PLAYFUL_POPS,     // Short, light double ticks
    REACTIVE_PULSE,   // High-velocity sharp taps & rumbles
    MYSTERIOUS_RUMBLE,// Low frequency subtle rumble
    TACTILE_CLICK     // Firm mechanical click
}

data class CategoryThemeToken(
    val category: GameCategory,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val surfaceBorder: Color,
    val backgroundGlow: Color,
    val cornerRadius: Dp,
    val motionPersonality: MotionPersonality,
    val hapticProfile: HapticProfile
) {
    companion object {
        val TriviaToken = CategoryThemeToken(
            category = GameCategory.TRIVIA,
            primaryAccent = Color(0xFF9D4EDD), // Violet
            secondaryAccent = Color(0xFFFF007F), // Magenta
            surfaceBorder = Color(0x669D4EDD),
            backgroundGlow = Color(0x229D4EDD),
            cornerRadius = 24.dp, // Playful curved corners
            motionPersonality = MotionPersonality.SPRING_BOUNCY,
            hapticProfile = HapticProfile.PLAYFUL_POPS
        )

        val ActionToken = CategoryThemeToken(
            category = GameCategory.ACTION,
            primaryAccent = Color(0xFF00F2FE), // Cyber Turquoise
            secondaryAccent = Color(0xFFFFB300), // Amber Gold
            surfaceBorder = Color(0x6600F2FE),
            backgroundGlow = Color(0x2200F2FE),
            cornerRadius = 14.dp, // Sharp reactive cut feel
            motionPersonality = MotionPersonality.HIGH_VELOCITY,
            hapticProfile = HapticProfile.REACTIVE_PULSE
        )

        val MysteryToken = CategoryThemeToken(
            category = GameCategory.MYSTERY,
            primaryAccent = Color(0xFF00E676), // Mint Emerald
            secondaryAccent = Color(0xFF1A237E), // Deep Indigo
            surfaceBorder = Color(0x6600E676),
            backgroundGlow = Color(0x1A00E676),
            cornerRadius = 18.dp, // Veiled subtle curve
            motionPersonality = MotionPersonality.SLOW_AMBIENT,
            hapticProfile = HapticProfile.MYSTERIOUS_RUMBLE
        )

        val BoardToken = CategoryThemeToken(
            category = GameCategory.BOARD,
            primaryAccent = Color(0xFFFF6B6B), // Coral Sunrise
            secondaryAccent = Color(0xFFF7AEF8), // Rose Gold
            surfaceBorder = Color(0x66FF6B6B),
            backgroundGlow = Color(0x22FF6B6B),
            cornerRadius = 16.dp, // Structured grid card feel
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
