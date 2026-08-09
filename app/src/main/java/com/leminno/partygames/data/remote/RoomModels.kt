package com.leminno.partygames.data.remote

import androidx.annotation.Keep

@Keep
enum class RoomStatus {
    LOBBY,
    PLAYING,
    FINISHED
}

@Keep
data class RemotePlayer(
    val id: String = "",
    val name: String = "",
    val isHost: Boolean = false,
    val connected: Boolean = true,
    val team: Int = 1,
    val joinedAt: Long = System.currentTimeMillis()
)

@Keep
data class RemoteRoom(
    val roomCode: String = "",
    val gameId: String = "",
    val hostId: String = "",
    val status: String = RoomStatus.LOBBY.name,
    val createdAt: Long = System.currentTimeMillis(),
    val players: Map<String, RemotePlayer> = emptyMap(),
    val gameState: Map<String, Any?> = emptyMap()
)
