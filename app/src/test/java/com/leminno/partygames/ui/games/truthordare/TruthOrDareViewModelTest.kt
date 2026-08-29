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
    fun `initGame loads prompts for selected deck and sets timer duration`() {
        viewModel.initGame(playerCount = 3, deck = "Extreme", timerSec = 90)

        val state = viewModel.uiState.value
        assertEquals(3, state.playerCount)
        assertEquals("Extreme", state.selectedDeck)
        assertEquals(90, state.timerDurationSec)
        assertEquals(90, state.timeRemaining)
        assertEquals(1, state.currentPlayerTurn)
        assertFalse(state.isTimerRunning)
        assertFalse(state.isTimesUp)
        assertTrue(state.truthPrompts.isNotEmpty())
        assertTrue(state.darePrompts.isNotEmpty())
        assertNull(state.activePrompt)
    }

    @Test
    fun `drawTruth updates active prompt, starts timer and increments index`() {
        viewModel.initGame(playerCount = 4, deck = "Clean", timerSec = 60)
        viewModel.drawTruth()

        val state = viewModel.uiState.value
        assertEquals("TRUTH", state.activePromptType)
        assertNotNull(state.activePrompt)
        assertEquals(1, state.truthIndex)
        assertTrue(state.isTimerRunning)
        assertFalse(state.isPaused)
        assertEquals(60, state.timeRemaining)
    }

    @Test
    fun `drawDare updates active prompt, starts timer and increments index`() {
        viewModel.initGame(playerCount = 4, deck = "Clean", timerSec = 30)
        viewModel.drawDare()

        val state = viewModel.uiState.value
        assertEquals("DARE", state.activePromptType)
        assertNotNull(state.activePrompt)
        assertEquals(1, state.dareIndex)
        assertTrue(state.isTimerRunning)
        assertFalse(state.isPaused)
        assertEquals(30, state.timeRemaining)
    }

    @Test
    fun `togglePauseTimer toggles pause state`() {
        viewModel.initGame(playerCount = 4, deck = "Clean", timerSec = 60)
        viewModel.drawTruth()
        assertFalse(viewModel.uiState.value.isPaused)

        viewModel.togglePauseTimer()
        assertTrue(viewModel.uiState.value.isPaused)

        viewModel.togglePauseTimer()
        assertFalse(viewModel.uiState.value.isPaused)
    }

    @Test
    fun `completeFate clears active prompt and advances turn to next player`() {
        viewModel.initGame(playerCount = 2, deck = "Clean", timerSec = 60)
        viewModel.drawTruth()
        assertEquals(1, viewModel.uiState.value.currentPlayerTurn)

        viewModel.completeFate()
        val state = viewModel.uiState.value
        assertNull(state.activePrompt)
        assertNull(state.activePromptType)
        assertFalse(state.isTimerRunning)
        assertEquals(2, state.currentPlayerTurn)

        // Turn wraps around back to Player 1
        viewModel.drawDare()
        viewModel.completeFate()
        assertEquals(1, viewModel.uiState.value.currentPlayerTurn)
    }

    @Test
    fun `nextTurn advances turn and resets times up state`() {
        viewModel.initGame(playerCount = 3, deck = "Clean", timerSec = 60)
        viewModel.nextTurn()
        assertEquals(2, viewModel.uiState.value.currentPlayerTurn)

        viewModel.nextTurn()
        assertEquals(3, viewModel.uiState.value.currentPlayerTurn)

        viewModel.nextTurn()
        assertEquals(1, viewModel.uiState.value.currentPlayerTurn)
    }

    @Test
    fun `selectDeck changes deck and resets draw indices`() {
        viewModel.initGame(playerCount = 4, deck = "Clean", timerSec = 60)
        viewModel.drawTruth()

        viewModel.selectDeck("Party")

        val state = viewModel.uiState.value
        assertEquals("Party", state.selectedDeck)
        assertEquals(0, state.truthIndex)
        assertNull(state.activePrompt)
        assertFalse(state.isTimerRunning)
    }
}
