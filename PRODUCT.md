# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Primary users are friends and family gathered together in social party or casual game-night settings. They seek fast, frictionless, entertaining party games on Android devices—playing in-person via Pass & Play, forehead tilt mechanics, multi-touch, or remote room connections.

## Product Purpose

Party Games is a modern, tactile, all-in-one Android party game suite. It provides a curated catalog of easy-to-learn, fun party games with zero setup hassle. The primary goal is delivering a beautiful, cohesive, and premium UI/UX experience across the priority game catalog.

## Positioning

Unlike generic or clunky party game apps, Party Games offers a highly visual, fluid, motion-rich native Android experience. It features local-first offline play with zero forced connectivity, combined with optional AI prompt generation via the Leminno Protected AI Gateway.

## Operating Context

Played during in-person social gatherings, parties, and family events. Operating context requires:
- High contrast, legible UI designed for group viewing and fast turns.
- Tactile haptic feedback and smooth gesture/sensor interactions (e.g. forehead tilt for Who Am I?, hold-to-reveal anti-cheat for Undercover Spy).
- Seamless transition between games in portrait phone orientation.

## Capabilities and Constraints

- **Platform & Target:** Native Android (Min SDK 26 / Android 8.0+), Kotlin, Jetpack Compose, Gradle Kotlin DSL. Phone-first, portrait orientation primary.
- **Priority Launch Catalog:** 4 core MVP games (*Who Am I?*, *Never Have I Ever*, *Truth or Dare*, *Undercover Spy*). ~23 future concepts deferred.
- **Connectivity & Offline Play:** 100% offline-first for pass-and-play and local games. Internet is strictly optional for AI prompt expansion (Leminno AI Gateway at `https://ai.leminno.com/api/chat`) and deep-link room code invites (`partygames://join/...`).
- **Hardware Fallback:** Graceful fallback when sensor hardware (accelerometer, vibration motor) is absent—games must automatically use touch/button inputs or hide missing features without crashing.

## Brand Commitments

- **App Name:** Party Games
- **Package Name:** `com.leminno.partygames`
- **Organization:** Leminno
- **Links:** LinkedIn (`https://www.linkedin.com/company/leminno/`), Discord (`https://discord.gg/uTmQnkMVkA`)

## Evidence on Hand

- Core project rules in `AGENTS.md` and `techstack.md`.
- Game concept catalog in `plan.md`.
- Established `AiGateway.kt` utility connecting to `https://ai.leminno.com/api/chat`.
- Deep link intent filter configuration in `AndroidManifest.xml` (`partygames://join/...`).

## Product Principles

1. **Craft & Visual Polish First:** Focus heavily on modern, fluid Jetpack Compose UI/UX, micro-animations, and Material 3 theming over raw feature count.
2. **Zero-Friction Pass & Play:** Gameplay flows must be immediate, readable from a distance, and protected by intuitive anti-cheat affordances.
3. **Robust Hardware Resilience:** Never block or fail on missing device sensors; adapt gracefully between tactile motion controls and touch fallbacks.
4. **Local-First Independence:** Core game modes are completely self-contained offline, with AI gateway features serving as optional enhancements.

## Accessibility & Inclusion

- Material 3 color roles supporting dynamic light/dark themes.
- Touch targets adhering to 48×48 dp minimum.
- Font sizing adhering to Android `sp` units to respect system scale settings.
