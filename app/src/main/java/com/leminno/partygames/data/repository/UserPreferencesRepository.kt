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
}
