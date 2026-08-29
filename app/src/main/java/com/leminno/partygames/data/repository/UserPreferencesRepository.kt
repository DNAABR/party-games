package com.leminno.partygames.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserPreferencesRepository {
    private const val PREFS_NAME = "party_games_preferences"
    private const val KEY_KEEP_SCREEN_AWAKE = "keep_screen_awake"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    private const val KEY_AUDIO_ENABLED = "audio_enabled"
    private const val KEY_FAVORITES = "favorite_game_ids"
    private const val KEY_RECENTS = "recent_game_ids"
    private const val KEY_SAVED_ROSTER = "saved_party_roster"

    private var prefs: SharedPreferences? = null

    private val _keepScreenAwake = MutableStateFlow(true)
    val keepScreenAwake: StateFlow<Boolean> = _keepScreenAwake.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _audioEnabled = MutableStateFlow(true)
    val audioEnabled: StateFlow<Boolean> = _audioEnabled.asStateFlow()

    private val _favoriteGameIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteGameIds: StateFlow<Set<String>> = _favoriteGameIds.asStateFlow()

    private val _recentGameIds = MutableStateFlow<List<String>>(emptyList())
    val recentGameIds: StateFlow<List<String>> = _recentGameIds.asStateFlow()

    private val _savedRoster = MutableStateFlow<List<String>>(emptyList())
    val savedRoster: StateFlow<List<String>> = _savedRoster.asStateFlow()

    private val _sessionScores = MutableStateFlow<Map<String, Int>>(emptyMap())
    val sessionScores: StateFlow<Map<String, Int>> = _sessionScores.asStateFlow()

    val funPartyNicknames = listOf(
        "Neon Ninja", "Pixel King", "Chaos Queen", "Glitch Master",
        "Disco Diva", "Laser Boss", "Retro Rebel", "Vapor Wave",
        "Arcade Hero", "Cyber Punk", "Ghost Rider", "Sonic Star",
        "Turbo Champ", "Mega Pixel", "Space Ace", "Wild Card"
    )

    fun init(context: Context) {
        if (prefs == null) {
            val applicationContext = context.applicationContext
            prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            _keepScreenAwake.value = prefs?.getBoolean(KEY_KEEP_SCREEN_AWAKE, true) ?: true
            _hapticsEnabled.value = prefs?.getBoolean(KEY_HAPTICS_ENABLED, true) ?: true
            _audioEnabled.value = prefs?.getBoolean(KEY_AUDIO_ENABLED, true) ?: true

            val favs = prefs?.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
            _favoriteGameIds.value = favs

            val recentsCsv = prefs?.getString(KEY_RECENTS, "") ?: ""
            _recentGameIds.value = if (recentsCsv.isNotBlank()) recentsCsv.split(",").filter { it.isNotBlank() } else emptyList()

            val rosterCsv = prefs?.getString(KEY_SAVED_ROSTER, "") ?: ""
            _savedRoster.value = if (rosterCsv.isNotBlank()) rosterCsv.split(",").filter { it.isNotBlank() } else emptyList()
        }
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        _keepScreenAwake.value = enabled
        prefs?.edit()?.putBoolean(KEY_KEEP_SCREEN_AWAKE, enabled)?.apply()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_HAPTICS_ENABLED, enabled)?.apply()
    }

    fun setAudioEnabled(enabled: Boolean) {
        _audioEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_AUDIO_ENABLED, enabled)?.apply()
    }

    fun toggleFavorite(gameId: String) {
        val current = _favoriteGameIds.value.toMutableSet()
        if (current.contains(gameId)) {
            current.remove(gameId)
        } else {
            current.add(gameId)
        }
        _favoriteGameIds.value = current
        prefs?.edit()?.putStringSet(KEY_FAVORITES, current)?.apply()
    }

    fun isFavorite(gameId: String): Boolean {
        return _favoriteGameIds.value.contains(gameId)
    }

    fun addRecentlyPlayed(gameId: String) {
        val current = _recentGameIds.value.toMutableList()
        current.remove(gameId)
        current.add(0, gameId)
        val trimmed = current.take(5)
        _recentGameIds.value = trimmed
        prefs?.edit()?.putString(KEY_RECENTS, trimmed.joinToString(","))?.apply()
    }

    fun saveRoster(players: List<String>) {
        val cleaned = players.map { it.trim() }.filter { it.isNotBlank() }
        _savedRoster.value = cleaned
        prefs?.edit()?.putString(KEY_SAVED_ROSTER, cleaned.joinToString(","))?.apply()
    }

    fun getActiveRoster(targetCount: Int): List<String> {
        val current = _savedRoster.value.toMutableList()
        val result = mutableListOf<String>()
        for (i in 0 until targetCount) {
            if (i < current.size && current[i].isNotBlank()) {
                result.add(current[i])
            } else {
                result.add("Player ${i + 1}")
            }
        }
        return result
    }

    fun getRandomNickname(index: Int): String {
        return funPartyNicknames.getOrNull(index % funPartyNicknames.size) ?: "Player ${index + 1}"
    }

    fun updatePlayerScore(playerName: String, delta: Int) {
        val current = _sessionScores.value.toMutableMap()
        val currentScore = current.getOrDefault(playerName, 0)
        val newScore = (currentScore + delta).coerceAtLeast(0)
        current[playerName] = newScore
        _sessionScores.value = current
    }

    fun resetPlayerScores() {
        _sessionScores.value = emptyMap()
    }
}

