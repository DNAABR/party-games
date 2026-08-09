package com.leminno.partygames.data

import com.leminno.partygames.data.model.GameCategory
import org.junit.Assert.*
import org.junit.Test

class GameCatalogRepositoryTest {

    @Test
    fun `allGames returns valid game catalog list`() {
        val games = GameCatalogRepository.allGames
        assertTrue("Catalog should contain games", games.isNotEmpty())
        assertEquals(27, games.size)
    }

    @Test
    fun `mvpGames filtering returns only games marked isMvp true`() {
        val mvpGames = GameCatalogRepository.allGames.filter { it.isMvp }
        assertTrue("MVP games list should not be empty", mvpGames.isNotEmpty())
        assertTrue("All filtered MVP games must have isMvp == true", mvpGames.all { it.isMvp })
        assertEquals(8, mvpGames.size)
    }

    @Test
    fun `game categories are populated correctly`() {
        val categories = GameCatalogRepository.allGames.map { it.category }.toSet()
        assertTrue(categories.contains(GameCategory.TRIVIA))
        assertTrue(categories.contains(GameCategory.ACTION))
        assertTrue(categories.contains(GameCategory.MYSTERY))
        assertTrue(categories.contains(GameCategory.BOARD))
    }
}
