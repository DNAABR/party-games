package com.leminno.partygames.ui.model

import com.leminno.partygames.data.model.GameCategory

enum class SetupType(val label: String, val badgeIconKey: String) {
    FOREHEAD_SENSOR("Forehead Sensor", "phone_android"),
    PASS_AND_PLAY("Pass & Play", "sync"),
    MULTI_TOUCH("Multi-Touch Screen", "touch_app"),
    PHYSICAL_PASS("Physical Pass", "bolt"),
    SPLIT_SCREEN("Split Screen", "splitscreen"),
    DUAL_DEVICE("Dual Device", "link")
}

data class GameRuleStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val iconKey: String
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

