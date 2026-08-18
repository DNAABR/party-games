package com.leminno.partygames.ui.theme

import androidx.compose.ui.graphics.Color

// --- True Pixel-Art Retro Arcade Cabinet Base Palette ---
val PixelVioletBase       = Color(0xFF2B0A3D) // Deep violet cabinet body
val PixelVioletDark       = Color(0xFF1B0626) // Deepest background cavity
val PixelVioletElevated   = Color(0xFF3D1054) // Structural panel background
val PixelVioletLight      = Color(0xFF551973) // High-level cabinet panel

// --- Hot Magenta / Pink Structural Panel Tokens ---
val PixelMagentaHot       = Color(0xFFFF2E9C) // Primary structural marquee & card borders
val PixelMagentaHighlight = Color(0xFFFF69BC) // Top bevel highlight band
val PixelMagentaShadow    = Color(0xFFC4006B) // Bottom bevel shadow band
val PixelMagentaDark      = Color(0xFF6B003A) // Deep structural inset

// --- Cyan CRT Screen & Scanline Palette ---
val PixelCrtCyan          = Color(0xFF00E5FF) // Active CRT screen surface & primary text
val PixelCrtCyanHighlight = Color(0xFF80F3FF) // Screen highlight pixel band
val PixelCrtCyanShadow    = Color(0xFF0099B8) // Screen shadow pixel band
val PixelCrtDarkCanvas    = Color(0xFF0C1826) // Off/Dark CRT screen background
val PixelCrtScanlineBand  = Color(0xFF162C42) // Discrete scanline pixel stripe

// --- Accent Tokens ---
val PixelAmberGold        = Color(0xFFFFB703) // Arcade coin/marquee accent
val PixelAmberShadow      = Color(0xFFC68800) // Stepped shadow for gold
val PixelEmeraldGreen     = Color(0xFF00E676) // Success/Ready action accent
val PixelEmeraldShadow    = Color(0xFF009E52) // Stepped shadow for green
val PixelAlertRed         = Color(0xFFFF2A55) // Danger/Dare accent
val PixelAlertShadow      = Color(0xFFB3002D) // Stepped shadow for red

// --- Sprite Outlines & Borders ---
val PixelOutlineBlack     = Color(0xFF08020A) // 2-4dp chunky sprite border outline
val PixelInnerBevel       = Color(0xFF4A1568) // Inner 1dp pixel bevel

// --- Legacy Mappings for Compatibility ---
val BackgroundObsidian    = PixelVioletDark
val BackgroundNavySlate   = PixelVioletBase
val SurfaceGlassDark      = PixelVioletElevated
val SurfaceGlassLight     = PixelVioletLight
val SurfaceElevated       = PixelVioletElevated
val BorderGlassDefault    = PixelOutlineBlack
val BorderGlassAccent     = PixelMagentaHot

val TextPrimary           = Color(0xFFF8FAFC)
val TextSecondary         = Color(0xFFD0C0E8)
val TextMuted             = Color(0xFF8A72A8)
val TextOnAccent          = PixelOutlineBlack

val AccentCyan            = PixelCrtCyan
val AccentViolet          = Color(0xFF9D00FF)
val AccentMagenta         = PixelMagentaHot
val AccentAmber           = PixelAmberGold

val WinGold               = PixelAmberGold
val AlertRed              = PixelAlertRed
val SuccessGreen          = PixelEmeraldGreen
