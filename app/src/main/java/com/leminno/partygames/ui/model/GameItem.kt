package com.leminno.partygames.ui.model

import com.leminno.partygames.data.model.GameCategory

enum class SetupType(val label: String, val badgeIcon: String) {
    FOREHEAD_SENSOR("Forehead Sensor", "📱"),
    PASS_AND_PLAY("Pass & Play", "🔄"),
    MULTI_TOUCH("Multi-Touch Screen", "👇"),
    PHYSICAL_PASS("Physical Pass", "💥"),
    SPLIT_SCREEN("Split Screen", "📲"),
    DUAL_DEVICE("Dual Device", "🔗")
}

data class GameRuleStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val iconSymbol: String
)

data class GameItem(
    val id: String,
    val title: String,
    val tagLine: String,
    val description: String,
    val category: GameCategory,
    val setupType: SetupType,
    val minPlayers: Int,
    val maxPlayers: Int,
    val estTimeMinutes: Int,
    val isMvp: Boolean = false,
    val antiCheatNotice: String? = null,
    val rules: List<GameRuleStep>
)
