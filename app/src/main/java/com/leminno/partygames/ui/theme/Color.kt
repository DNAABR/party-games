package com.leminno.partygames.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// LEMINNO AIR - PREMIUM MINIMALIST DESIGN SYSTEM PALETTE
// Inspired by Apple Human Interface Guidelines & Google Material 3
// ============================================================================

// --- Core Neutral Light Canvas & Surfaces ---
val CanvasLight           = Color(0xFFF8F9FA) // Soft warm off-white canvas
val SurfaceLight          = Color(0xFFFFFFFF) // Crisp elevated white card surface
val SurfaceSubtle         = Color(0xFFF1F5F9) // Secondary container & chip fill
val SurfaceElevatedLight  = Color(0xFFFFFFFF) // Modal & sheet background
val BorderSubtle          = Color(0xFFE2E8F0) // Clean hairline border (slate-200)
val BorderSubtleLight     = Color(0xFFF1F5F9) // Extra soft border for nested elements
val BorderFocus           = Color(0xFFCBD5E1) // Active/Hover stroke border
val ScrimModal            = Color(0x660F172A) // 40% translucent slate backdrop
val ScrimOverlay          = Color(0x800F172A) // 50% translucent slate overlay

// --- Typography Hierarchy ---
val TextPrimary           = Color(0xFF0F172A) // Deep charcoal slate (slate-900)
val TextSecondary         = Color(0xFF475569) // Neutral slate (slate-600)
val TextMuted             = Color(0xFF94A3B8) // Light slate (slate-400)
val TextOnPrimary         = Color(0xFFFFFFFF) // White on solid primary fills
val TextOnAccent          = Color(0xFFFFFFFF)

// --- Primary Brand Accents ---
val BrandPrimary          = Color(0xFF6366F1) // Modern Indigo / Iris
val BrandPrimaryDark      = Color(0xFF4F46E5) // Indigo-600 pressed state
val BrandPrimaryContainer = Color(0xFFEEF2FF) // Soft indigo background
val BrandPrimaryText      = Color(0xFF4338CA) // Indigo text on container

val BrandSecondary        = Color(0xFF0EA5E9) // Sky Blue
val BrandSecondaryContainer = Color(0xFFE0F2FE)
val BrandSecondaryText    = Color(0xFF0369A1)

// --- Category Pastels (Harmonious & Tactile) ---
// 1. Trivia & Brain (Purple / Lilac)
val TriviaPrimary         = Color(0xFF8B5CF6)
val TriviaContainer       = Color(0xFFF5F3FF)
val TriviaBorder          = Color(0xFFDDD6FE)
val TriviaText            = Color(0xFF6D28D9)

// 2. Action & Movement (Sky / Cerulean)
val ActionPrimary         = Color(0xFF0284C7)
val ActionContainer       = Color(0xFFF0F9FF)
val ActionBorder          = Color(0xFFBAE6FD)
val ActionText            = Color(0xFF0369A1)

// 3. Mystery & Deduction (Emerald / Mint Sage)
val MysteryPrimary        = Color(0xFF059669)
val MysteryContainer      = Color(0xFFECFDF5)
val MysteryBorder         = Color(0xFFA7F3D0)
val MysteryText           = Color(0xFF047857)

// 4. Board & Classic (Coral / Terracotta Peach)
val BoardPrimary          = Color(0xFFEA580C)
val BoardContainer        = Color(0xFFFFF7ED)
val BoardBorder           = Color(0xFFFED7AA)
val BoardText             = Color(0xFFC2410C)

// --- Semantic Feedback Tokens ---
val SuccessGreen          = Color(0xFF10B981) // Emerald
val SuccessContainer      = Color(0xFFD1FAE5)
val AlertRed              = Color(0xFFEF4444) // Clean Coral Red
val AlertContainer        = Color(0xFFFEE2E2)
val WarningAmber          = Color(0xFFF59E0B) // Amber
val WarningContainer      = Color(0xFFFEF3C7)
val WinGold               = WarningAmber

// --- Legacy & Aliased Mappings for Seamless Compatibility ---
val BackgroundObsidian    = CanvasLight
val BackgroundNavySlate   = SurfaceSubtle
val SurfaceGlassDark      = SurfaceLight
val SurfaceGlassLight     = SurfaceSubtle
val SurfaceElevated       = SurfaceLight
val BorderGlassDefault    = BorderSubtle
val BorderGlassAccent     = BrandPrimary

val AccentCyan            = BrandSecondary
val AccentViolet          = TriviaPrimary
val AccentMagenta         = BrandPrimary
val AccentAmber           = WarningAmber

// Pixel / Arcade Tokens Redirected to Clean Minimalist Palette
val PixelVioletBase       = CanvasLight
val PixelVioletDark       = CanvasLight
val PixelVioletElevated   = SurfaceLight
val PixelVioletLight      = SurfaceSubtle

val PixelMagentaHot       = BrandPrimary
val PixelMagentaHighlight = BrandPrimaryContainer
val PixelMagentaShadow    = BrandPrimaryDark
val PixelMagentaDark      = BrandPrimaryText

val PixelCrtCyan          = BrandSecondary
val PixelCrtCyanHighlight = BrandSecondaryContainer
val PixelCrtCyanShadow    = BrandSecondaryText
val PixelCrtDarkCanvas    = SurfaceSubtle
val PixelCrtScanlineBand  = Color.Transparent

val PixelAmberGold        = WarningAmber
val PixelAmberShadow      = Color(0xFFD97706)
val PixelEmeraldGreen     = SuccessGreen
val PixelEmeraldShadow    = Color(0xFF059669)
val PixelAlertRed         = AlertRed
val PixelAlertShadow      = Color(0xFFDC2626)

val PixelOutlineBlack     = BorderSubtle
val PixelInnerBevel       = BorderSubtleLight
