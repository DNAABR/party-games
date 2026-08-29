package com.leminno.partygames.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Handcrafted 24x24 8-bit/16-bit Pixel Icons system for Party Games arcade UI.
 * Built with sharp pixel-grid path definitions.
 */
object PixelIcons {

    val Heart: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelHeart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Heart Pixel Grid (Filled)
                moveTo(5f, 4f); lineTo(9f, 4f); lineTo(9f, 6f); lineTo(11f, 6f); lineTo(11f, 8f); lineTo(13f, 8f); lineTo(13f, 6f); lineTo(15f, 6f); lineTo(15f, 4f); lineTo(19f, 4f); lineTo(19f, 6f); lineTo(21f, 6f); lineTo(21f, 12f); lineTo(19f, 12f); lineTo(19f, 14f); lineTo(17f, 14f); lineTo(17f, 16f); lineTo(15f, 16f); lineTo(15f, 18f); lineTo(13f, 18f); lineTo(13f, 20f); lineTo(11f, 20f); lineTo(11f, 18f); lineTo(9f, 18f); lineTo(9f, 16f); lineTo(7f, 16f); lineTo(7f, 14f); lineTo(5f, 14f); lineTo(5f, 12f); lineTo(3f, 12f); lineTo(3f, 6f); lineTo(5f, 6f); close()
            }
        }.build()
    }

    val HeartBorder: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelHeartBorder",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(5f, 4f); lineTo(9f, 4f); lineTo(9f, 6f); lineTo(15f, 6f); lineTo(15f, 4f); lineTo(19f, 4f); lineTo(19f, 6f); lineTo(21f, 6f); lineTo(21f, 12f); lineTo(19f, 14f); lineTo(17f, 16f); lineTo(15f, 18f); lineTo(13f, 20f); lineTo(11f, 20f); lineTo(9f, 18f); lineTo(7f, 16f); lineTo(5f, 14f); lineTo(3f, 12f); lineTo(3f, 6f); close()
                // Inner cutout
                moveTo(5f, 6f); lineTo(5f, 12f); lineTo(7f, 14f); lineTo(9f, 16f); lineTo(11f, 18f); lineTo(13f, 18f); lineTo(15f, 16f); lineTo(17f, 14f); lineTo(19f, 12f); lineTo(19f, 6f); lineTo(16f, 6f); lineTo(16f, 8f); lineTo(14f, 8f); lineTo(14f, 10f); lineTo(10f, 10f); lineTo(10f, 8f); lineTo(8f, 8f); lineTo(8f, 6f); close()
            }
        }.build()
    }

    val Users: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelUsers",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Left Head
                moveTo(6f, 3f); lineTo(10f, 3f); lineTo(10f, 7f); lineTo(6f, 7f); close()
                // Left Body
                moveTo(4f, 9f); lineTo(12f, 9f); lineTo(12f, 13f); lineTo(4f, 13f); close()
                // Right Head
                moveTo(14f, 5f); lineTo(18f, 5f); lineTo(18f, 9f); lineTo(14f, 9f); close()
                // Right Body
                moveTo(12f, 11f); lineTo(20f, 11f); lineTo(20f, 15f); lineTo(12f, 15f); close()
                // Base Stand
                moveTo(2f, 17f); lineTo(22f, 17f); lineTo(22f, 19f); lineTo(2f, 19f); close()
            }
        }.build()
    }

    val Clock: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelClock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Circle Frame
                moveTo(8f, 2f); lineTo(16f, 2f); lineTo(16f, 4f); lineTo(20f, 4f); lineTo(20f, 8f); lineTo(22f, 8f); lineTo(22f, 16f); lineTo(20f, 16f); lineTo(20f, 20f); lineTo(16f, 20f); lineTo(16f, 22f); lineTo(8f, 22f); lineTo(8f, 20f); lineTo(4f, 20f); lineTo(4f, 16f); lineTo(2f, 16f); lineTo(2f, 8f); lineTo(4f, 8f); lineTo(4f, 4f); lineTo(8f, 4f); close()
                // Clock Hands Cutout
                moveTo(11f, 6f); lineTo(13f, 6f); lineTo(13f, 11f); lineTo(17f, 11f); lineTo(17f, 13f); lineTo(11f, 13f); close()
            }
        }.build()
    }

    val Zap: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelZap",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(11f, 2f); lineTo(17f, 2f); lineTo(15f, 8f); lineTo(20f, 8f); lineTo(9f, 22f); lineTo(11f, 12f); lineTo(6f, 12f); close()
            }
        }.build()
    }

    val Trophy: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelTrophy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Cup Rim & Body
                moveTo(4f, 3f); lineTo(20f, 3f); lineTo(20f, 9f); lineTo(18f, 9f); lineTo(18f, 12f); lineTo(14f, 14f); lineTo(14f, 17f); lineTo(17f, 17f); lineTo(17f, 21f); lineTo(7f, 21f); lineTo(7f, 17f); lineTo(10f, 17f); lineTo(10f, 14f); lineTo(6f, 12f); lineTo(6f, 9f); lineTo(4f, 9f); close()
                // Handles
                moveTo(2f, 5f); lineTo(4f, 5f); lineTo(4f, 9f); lineTo(2f, 9f); close()
                moveTo(20f, 5f); lineTo(22f, 5f); lineTo(22f, 9f); lineTo(20f, 9f); close()
            }
        }.build()
    }

    val Sliders: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelSliders",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Track 1
                moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 7f); lineTo(3f, 7f); close()
                moveTo(7f, 3f); lineTo(11f, 3f); lineTo(11f, 9f); lineTo(7f, 9f); close()
                // Track 2
                moveTo(3f, 11f); lineTo(21f, 11f); lineTo(21f, 13f); lineTo(3f, 13f); close()
                moveTo(13f, 9f); lineTo(17f, 9f); lineTo(17f, 15f); lineTo(13f, 15f); close()
                // Track 3
                moveTo(3f, 17f); lineTo(21f, 17f); lineTo(21f, 19f); lineTo(3f, 19f); close()
                moveTo(5f, 15f); lineTo(9f, 15f); lineTo(9f, 21f); lineTo(5f, 21f); close()
            }
        }.build()
    }

    val Volume: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelVolume",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Speaker Base
                moveTo(3f, 9f); lineTo(7f, 9f); lineTo(11f, 5f); lineTo(11f, 19f); lineTo(7f, 15f); lineTo(3f, 15f); close()
                // Waves
                moveTo(15f, 8f); lineTo(17f, 8f); lineTo(17f, 16f); lineTo(15f, 16f); close()
                moveTo(19f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(19f, 19f); close()
            }
        }.build()
    }

    val VolumeX: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelVolumeX",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Speaker Base
                moveTo(3f, 9f); lineTo(7f, 9f); lineTo(11f, 5f); lineTo(11f, 19f); lineTo(7f, 15f); lineTo(3f, 15f); close()
                // X mark
                moveTo(15f, 9f); lineTo(17f, 9f); lineTo(17f, 11f); lineTo(19f, 11f); lineTo(19f, 9f); lineTo(21f, 9f); lineTo(21f, 11f); lineTo(19f, 13f); lineTo(21f, 15f); lineTo(21f, 17f); lineTo(19f, 17f); lineTo(17f, 15f); lineTo(15f, 17f); lineTo(15f, 15f); lineTo(17f, 13f); lineTo(15f, 11f); close()
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelClose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(5f, 5f); lineTo(9f, 5f); lineTo(9f, 7f); lineTo(11f, 7f); lineTo(11f, 9f); lineTo(13f, 9f); lineTo(13f, 7f); lineTo(15f, 7f); lineTo(15f, 5f); lineTo(19f, 5f); lineTo(19f, 9f); lineTo(17f, 9f); lineTo(17f, 11f); lineTo(15f, 11f); lineTo(15f, 13f); lineTo(17f, 13f); lineTo(17f, 15f); lineTo(19f, 15f); lineTo(19f, 19f); lineTo(15f, 19f); lineTo(15f, 17f); lineTo(13f, 17f); lineTo(13f, 15f); lineTo(11f, 15f); lineTo(11f, 17f); lineTo(9f, 17f); lineTo(9f, 19f); lineTo(5f, 19f); lineTo(5f, 15f); lineTo(7f, 15f); lineTo(7f, 13f); lineTo(9f, 13f); lineTo(9f, 11f); lineTo(7f, 11f); lineTo(7f, 9f); lineTo(5f, 9f); close()
            }
        }.build()
    }

    val ChevronLeft: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelChevronLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(14f, 4f); lineTo(18f, 4f); lineTo(18f, 8f); lineTo(14f, 8f); lineTo(14f, 10f); lineTo(10f, 10f); lineTo(10f, 14f); lineTo(14f, 14f); lineTo(14f, 16f); lineTo(18f, 16f); lineTo(18f, 20f); lineTo(14f, 20f); lineTo(14f, 18f); lineTo(10f, 18f); lineTo(10f, 16f); lineTo(6f, 16f); lineTo(6f, 8f); lineTo(10f, 8f); lineTo(10f, 6f); lineTo(14f, 6f); close()
            }
        }.build()
    }

    val ArcadeJoystick: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelArcadeJoystick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Ball Top
                moveTo(9f, 2f); lineTo(15f, 2f); lineTo(15f, 8f); lineTo(9f, 8f); close()
                // Stick
                moveTo(11f, 8f); lineTo(13f, 8f); lineTo(13f, 15f); lineTo(11f, 15f); close()
                // Console Box
                moveTo(3f, 15f); lineTo(21f, 15f); lineTo(21f, 22f); lineTo(3f, 22f); close()
            }
        }.build()
    }

    val Lightbulb: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelLightbulb",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(7f, 3f); lineTo(17f, 3f); lineTo(17f, 5f); lineTo(19f, 5f); lineTo(19f, 11f); lineTo(17f, 11f); lineTo(17f, 13f); lineTo(15f, 13f); lineTo(15f, 17f); lineTo(9f, 17f); lineTo(9f, 13f); lineTo(7f, 13f); lineTo(7f, 11f); lineTo(5f, 11f); lineTo(5f, 5f); lineTo(7f, 5f); close()
                // Screw base
                moveTo(9f, 19f); lineTo(15f, 19f); lineTo(15f, 21f); lineTo(9f, 21f); close()
            }
        }.build()
    }

    val Eye: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelEye",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Eye Frame
                moveTo(6f, 6f); lineTo(18f, 6f); lineTo(18f, 8f); lineTo(22f, 8f); lineTo(22f, 16f); lineTo(18f, 16f); lineTo(18f, 18f); lineTo(6f, 18f); lineTo(6f, 16f); lineTo(2f, 16f); lineTo(2f, 8f); lineTo(6f, 8f); close()
                // Pupil
                moveTo(10f, 10f); lineTo(14f, 10f); lineTo(14f, 14f); lineTo(10f, 14f); close()
            }
        }.build()
    }

    val Refresh: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelRefresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 3f); lineTo(20f, 3f); lineTo(20f, 9f); lineTo(18f, 9f); lineTo(18f, 5f); lineTo(12f, 5f); lineTo(8f, 5f); lineTo(8f, 9f); lineTo(6f, 9f); lineTo(6f, 5f); close()
                moveTo(12f, 21f); lineTo(4f, 21f); lineTo(4f, 15f); lineTo(6f, 15f); lineTo(6f, 19f); lineTo(12f, 19f); lineTo(16f, 19f); lineTo(16f, 15f); lineTo(18f, 15f); lineTo(18f, 19f); close()
            }
        }.build()
    }

    val Shield: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelShield",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(4f, 3f); lineTo(20f, 3f); lineTo(20f, 11f); lineTo(18f, 11f); lineTo(18f, 15f); lineTo(14f, 15f); lineTo(14f, 19f); lineTo(12f, 19f); lineTo(12f, 21f); lineTo(10f, 19f); lineTo(10f, 15f); lineTo(6f, 15f); lineTo(6f, 11f); lineTo(4f, 11f); close()
            }
        }.build()
    }

    val Dice: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelDice",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Cube Box
                moveTo(3f, 3f); lineTo(21f, 3f); lineTo(21f, 21f); lineTo(3f, 21f); close()
                // Dots cutout
                moveTo(6f, 6f); lineTo(8f, 6f); lineTo(8f, 8f); lineTo(6f, 8f); close()
                moveTo(16f, 6f); lineTo(18f, 6f); lineTo(18f, 8f); lineTo(16f, 8f); close()
                moveTo(11f, 11f); lineTo(13f, 11f); lineTo(13f, 13f); lineTo(11f, 13f); close()
                moveTo(6f, 16f); lineTo(8f, 16f); lineTo(8f, 18f); lineTo(6f, 18f); close()
                moveTo(16f, 16f); lineTo(18f, 16f); lineTo(18f, 18f); lineTo(16f, 18f); close()
            }
        }.build()
    }

    val Spy: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelSpy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Fedora Hat Crown
                moveTo(8f, 2f); lineTo(16f, 2f); lineTo(16f, 7f); lineTo(8f, 7f); close()
                // Fedora Brim
                moveTo(3f, 7f); lineTo(21f, 7f); lineTo(21f, 10f); lineTo(3f, 10f); close()
                // Sunglasses Left Lens
                moveTo(4f, 12f); lineTo(10f, 12f); lineTo(10f, 16f); lineTo(4f, 16f); close()
                // Sunglasses Right Lens
                moveTo(14f, 12f); lineTo(20f, 12f); lineTo(20f, 16f); lineTo(14f, 16f); close()
                // Glasses Bridge
                moveTo(10f, 13f); lineTo(14f, 13f); lineTo(14f, 15f); lineTo(10f, 15f); close()
                // Trench Coat Collar
                moveTo(6f, 18f); lineTo(18f, 18f); lineTo(18f, 22f); lineTo(6f, 22f); close()
            }
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "PixelSearch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Magnifying glass lens rim
                moveTo(5f, 3f); lineTo(15f, 3f); lineTo(15f, 5f); lineTo(17f, 5f); lineTo(17f, 13f); lineTo(15f, 13f); lineTo(15f, 15f); lineTo(13f, 15f); lineTo(13f, 17f); lineTo(5f, 17f); lineTo(5f, 15f); lineTo(3f, 15f); lineTo(3f, 5f); lineTo(5f, 5f); close()
                // Inner cutout
                moveTo(7f, 6f); lineTo(13f, 6f); lineTo(13f, 12f); lineTo(7f, 12f); close()
                // Handle
                moveTo(14f, 14f); lineTo(17f, 14f); lineTo(21f, 18f); lineTo(21f, 21f); lineTo(18f, 21f); lineTo(14f, 17f); close()
            }
        }.build()
    }
}


