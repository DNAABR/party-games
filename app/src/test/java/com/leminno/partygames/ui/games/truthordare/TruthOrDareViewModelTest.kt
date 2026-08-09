package com.leminno.partygames.ui.games.truthordare

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TruthOrDareViewModelTest {

    private lateinit var viewModel: TruthOrDareViewModel

    @Before
    fun setUp() {
        viewModel = TruthOrDareViewModel()
    }

    @Test
    fun `initGame loads prompts for selected deck`() {
        viewModel.initGame(4, "Extreme")

        val state = viewModel.uiState.value
        assertEquals("Extreme", state.selectedDeck)
        assertTrue(state.truthPrompts.isNotEmpty())
        assertTrue(state.darePrompts.isNotEmpty())
        assertNull(state.activePrompt)
    }

    @Test
    fun `drawTruth updates active prompt and increments index`() {
        viewModel.initGame(4, "Clean")
        viewModel.drawTruth()

        val state = viewModel.uiState.value
        assertEquals("TRUTH", state.activePromptType)
        assertNotNull(state.activePrompt)
        assertEquals(1, state.truthIndex)
    }

    @Test
    fun `selectDeck changes deck and resets draw indices`() {
        viewModel.initGame(4, "Clean")
        viewModel.drawTruth()

        viewModel.selectDeck("Party")

        val state = viewModel.uiState.value
        assertEquals("Party", state.selectedDeck)
        assertEquals(0, state.truthIndex)
        assertNull(state.activePrompt)
    }
}
