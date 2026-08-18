package com.leminno.partygames.data.model

/**
 * Domain model representing core game categories across the Party Games catalog.
 */
enum class GameCategory(
    val title: String,
    val description: String,
    val iconKey: String
) {
    TRIVIA("Trivia & Word", "Cerebral, fast-paced prompts", "psychology"),
    ACTION("Action & Physical", "Fast, high-velocity motion & quick reactions", "bolt"),
    MYSTERY("Mystery & Deduction", "Secret roles, quiet reveals & psychological bluffs", "visibility_off"),
    BOARD("Board & Strategy", "Tactical grid choices & physical turn placement", "grid_view")
}

