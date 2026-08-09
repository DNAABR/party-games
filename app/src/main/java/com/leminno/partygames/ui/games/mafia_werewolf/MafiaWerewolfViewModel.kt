package com.leminno.partygames.ui.games.mafia_werewolf

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MafiaRole(val title: String, val team: String, val icon: String, val desc: String) {
    MAFIA("Mafia", "Mafia", "🕶️", "Wake up at night and choose a victim to eliminate."),
    DOCTOR("Doctor", "Civilians", "🩺", "Wake up at night and save 1 player from elimination."),
    DETECTIVE("Detective", "Civilians", "🕵️", "Wake up at night to inspect 1 suspect's team."),
    CIVILIAN("Civilian", "Civilians", "🧑", "Participate in day discussions and vote out suspected Mafia.")
}

data class PlayerRoleState(
    val name: String,
    val role: MafiaRole,
    val isAlive: Boolean = true
)

data class MafiaWerewolfUiState(
    val playerCount: Int = 6,
    val gamePhase: String = "ROLE_ASSIGNMENT", // ROLE_ASSIGNMENT, NIGHT, DAY_DISCUSSION, GAME_OVER
    val currentPlayerIndex: Int = 0,
    val isRoleRevealed: Boolean = false,
    val playersState: List<PlayerRoleState> = emptyList(),
    val nightStep: Int = 1, // 1: Mafia, 2: Doctor, 3: Detective
    val nightEliminatedTarget: PlayerRoleState? = null,
    val nightSavedTarget: PlayerRoleState? = null,
    val nightLogMessage: String = "",
    val audioMaskingActive: Boolean = true,
    val winnerTeam: String = "",
    val daySelectedVoteTarget: PlayerRoleState? = null
)

class MafiaWerewolfViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MafiaWerewolfUiState())
    val uiState: StateFlow<MafiaWerewolfUiState> = _uiState.asStateFlow()

    fun initGame(playerCount: Int) {
        val rolesList = generateBalancedRoles(playerCount)
        val players = rolesList.mapIndexed { idx, role ->
            PlayerRoleState("Player ${idx + 1}", role)
        }

        _uiState.update {
            MafiaWerewolfUiState(
                playerCount = playerCount,
                gamePhase = "ROLE_ASSIGNMENT",
                playersState = players
            )
        }
    }

    private fun generateBalancedRoles(playerCount: Int): List<MafiaRole> {
        val mafiaCount = (playerCount / 4).coerceAtLeast(1)
        val roles = mutableListOf<MafiaRole>()

        repeat(mafiaCount) { roles.add(MafiaRole.MAFIA) }
        roles.add(MafiaRole.DOCTOR)
        roles.add(MafiaRole.DETECTIVE)

        while (roles.size < playerCount) {
            roles.add(MafiaRole.CIVILIAN)
        }
        return roles.shuffled()
    }

    fun toggleRoleRevealed() {
        _uiState.update { it.copy(isRoleRevealed = !it.isRoleRevealed) }
    }

    fun nextRoleAssignment() {
        val state = _uiState.value
        if (state.currentPlayerIndex + 1 < state.playersState.size) {
            _uiState.update {
                it.copy(
                    currentPlayerIndex = it.currentPlayerIndex + 1,
                    isRoleRevealed = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    gamePhase = "NIGHT",
                    nightStep = 1,
                    isRoleRevealed = false
                )
            }
        }
    }

    fun toggleAudioMasking() {
        _uiState.update { it.copy(audioMaskingActive = !it.audioMaskingActive) }
    }

    fun selectNightTarget(target: PlayerRoleState) {
        val state = _uiState.value
        when (state.nightStep) {
            1 -> _uiState.update { it.copy(nightEliminatedTarget = target) }
            2 -> _uiState.update { it.copy(nightSavedTarget = target) }
            3 -> _uiState.update {
                it.copy(nightLogMessage = "${target.name} belongs to team ${target.role.team}!")
            }
        }
    }

    fun nextNightStep() {
        val state = _uiState.value
        if (state.nightStep < 3) {
            _uiState.update { it.copy(nightStep = it.nightStep + 1) }
        } else {
            // Resolve Night Actions
            val eliminated = state.nightEliminatedTarget
            val saved = state.nightSavedTarget

            var updatedPlayers = state.playersState
            val logMsg: String

            if (eliminated != null && eliminated.name != saved?.name) {
                updatedPlayers = state.playersState.map {
                    if (it.name == eliminated.name) it.copy(isAlive = false) else it
                }
                logMsg = "Night Over! ${eliminated.name} was eliminated during the night!"
            } else {
                logMsg = "Night Over! The Doctor saved the target! No one died tonight!"
            }

            val winTeam = checkWinCondition(updatedPlayers)

            if (winTeam != null) {
                _uiState.update {
                    it.copy(
                        playersState = updatedPlayers,
                        gamePhase = "GAME_OVER",
                        winnerTeam = winTeam,
                        nightLogMessage = logMsg
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        playersState = updatedPlayers,
                        gamePhase = "DAY_DISCUSSION",
                        nightLogMessage = logMsg,
                        daySelectedVoteTarget = null
                    )
                }
            }
        }
    }

    fun selectDayVoteTarget(target: PlayerRoleState) {
        _uiState.update { it.copy(daySelectedVoteTarget = target) }
    }

    fun confirmDayElimination() {
        val state = _uiState.value
        val target = state.daySelectedVoteTarget ?: return

        val updatedPlayers = state.playersState.map {
            if (it.name == target.name) it.copy(isAlive = false) else it
        }

        val winTeam = checkWinCondition(updatedPlayers)

        if (winTeam != null) {
            _uiState.update {
                it.copy(
                    playersState = updatedPlayers,
                    gamePhase = "GAME_OVER",
                    winnerTeam = winTeam
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    playersState = updatedPlayers,
                    gamePhase = "NIGHT",
                    nightStep = 1,
                    nightEliminatedTarget = null,
                    nightSavedTarget = null,
                    daySelectedVoteTarget = null
                )
            }
        }
    }

    private fun checkWinCondition(players: List<PlayerRoleState>): String? {
        val mafiaAlive = players.count { it.isAlive && it.role == MafiaRole.MAFIA }
        val townAlive = players.count { it.isAlive && it.role != MafiaRole.MAFIA }

        return when {
            mafiaAlive == 0 -> "CIVILIANS WIN 👑"
            mafiaAlive >= townAlive -> "MAFIA WINS 🕶️"
            else -> null
        }
    }

    fun startNewMatch() {
        initGame(_uiState.value.playerCount)
    }
}
