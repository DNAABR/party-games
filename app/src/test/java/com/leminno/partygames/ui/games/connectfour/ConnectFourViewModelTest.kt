package com.leminno.partygames.ui.games.connectfour

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConnectFourViewModelTest {

    private lateinit var viewModel: ConnectFourViewModel

    @Before
    fun setUp() {
        viewModel = ConnectFourViewModel()
    }

    @Test
    fun `initial state is empty 6x7 board with Red turn`() {
        val state = viewModel.uiState.value
        assertTrue(state.isRedTurn)
        assertNull(state.winnerPlayer)
        assertFalse(state.isDraw)
        assertEquals(6, state.grid.size)
        assertEquals(7, state.grid[0].size)
    }

    @Test
    fun `dropDisc places disc at bottom row and toggles turn`() {
        viewModel.dropDisc(0)

        val state = viewModel.uiState.value
        assertEquals(1, state.grid[5][0]) // Red placed disc at row 5, col 0
        assertFalse(state.isRedTurn)     // Turn toggled to Yellow
    }

    @Test
    fun `connect four vertical win condition detected`() {
        // Red drops in col 0, Yellow drops in col 1, 4 times
        for (i in 1..3) {
            viewModel.dropDisc(0) // Red
            viewModel.dropDisc(1) // Yellow
        }
        viewModel.dropDisc(0) // Red 4th vertical disc

        val state = viewModel.uiState.value
        assertEquals(1, state.winnerPlayer) // Player 1 (Red) wins
    }

    @Test
    fun `resetGame restores clean state`() {
        viewModel.dropDisc(0)
        viewModel.resetGame()

        val state = viewModel.uiState.value
        assertTrue(state.isRedTurn)
        assertNull(state.winnerPlayer)
        assertFalse(state.isDraw)
        assertEquals(0, state.grid[5][0])
    }
}
