package com.leminno.partygames.data.model

/**
 * Domain model representing core game categories across the Party Games catalog.
 */
enum class GameCategory(
    val title: String,
    val description: String,
    val iconSymbol: String
) {
    TRIVIA("Trivia & Word", "Cerebral, fast-paced prompts", "🎭"),
    ACTION("Action & Physical", "Fast, high-velocity motion & quick reactions", "⚡"),
    MYSTERY("Mystery & Deduction", "Secret roles, quiet reveals & psychological bluffs", "🕵️"),
    BOARD("Board & Strategy", "Tactical grid choices & physical turn placement", "🎲")
}
