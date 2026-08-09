package com.leminno.partygames.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

object RemoteRoomRepository {

    private const val TAG = "RemoteRoomRepository"
    private const val ROOMS_NODE = "rooms"

    private fun getDatabase(): FirebaseDatabase? {
        return try {
            FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Realtime DB not initialized or missing google-services.json", e)
            null
        }
    }

    /**
     * Generates a clean 6-character uppercase alphanumeric room code.
     */
    fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    /**
     * Generates a unique device/player ID stored in memory/prefs.
     */
    fun getOrCreatePlayerId(context: Context): String {
        val prefs = context.getSharedPreferences("party_games_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("local_player_id", null)
        if (id.isNullOrEmpty()) {
            id = UUID.randomUUID().toString().take(8)
            prefs.edit().putString("local_player_id", id).apply()
        }
        return id
    }

    /**
     * Creates a shareable invite link for a given room code.
     */
    fun getShareableInviteLink(roomCode: String): String {
        return "https://partygames.leminno.com/join/$roomCode"
    }

    /**
     * Triggers the Android System Share Sheet to send the room code & link.
     */
    fun shareRoomInvite(context: Context, gameName: String, roomCode: String) {
        val link = getShareableInviteLink(roomCode)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join $gameName on Party Games!")
            putExtra(
                Intent.EXTRA_TEXT,
                "🎮 Join my $gameName room on Party Games!\n\n" +
                        "🔑 Room Code: $roomCode\n" +
                        "🔗 Click to join: $link"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Invite friends to play"))
    }

    /**
     * Creates a new remote game room in Firebase Realtime DB.
     */
    suspend fun createRoom(
        gameId: String,
        hostPlayerId: String,
        hostPlayerName: String
    ): Result<RemoteRoom> {
        val db = getDatabase() ?: return Result.failure(Exception("Firebase DB unavailable"))
        val roomCode = generateRoomCode()
        val roomRef = db.getReference(ROOMS_NODE).child(roomCode)

        val hostPlayer = RemotePlayer(
            id = hostPlayerId,
            name = hostPlayerName.ifBlank { "Host" },
            isHost = true,
            connected = true
        )

        val room = RemoteRoom(
            roomCode = roomCode,
            gameId = gameId,
            hostId = hostPlayerId,
            status = RoomStatus.LOBBY.name,
            createdAt = System.currentTimeMillis(),
            players = mapOf(hostPlayerId to hostPlayer),
            gameState = emptyMap()
        )

        return try {
            roomRef.setValue(room).await()

            // Setup presence on disconnect
            roomRef.child("players").child(hostPlayerId).child("connected")
                .onDisconnect().setValue(false)

            Result.success(room)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create room: $roomCode", e)
            Result.failure(e)
        }
    }

    /**
     * Joins an existing room with a 6-character room code.
     */
    suspend fun joinRoom(
        roomCode: String,
        playerId: String,
        playerName: String
    ): Result<RemoteRoom> {
        val db = getDatabase() ?: return Result.failure(Exception("Firebase DB unavailable"))
        val cleanCode = roomCode.trim().uppercase()
        val roomRef = db.getReference(ROOMS_NODE).child(cleanCode)

        return try {
            val snapshot = roomRef.get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("Room $cleanCode not found"))
            }

            val room = snapshot.getValue(RemoteRoom::class.java)
                ?: return Result.failure(Exception("Invalid room data format"))

            val newPlayer = RemotePlayer(
                id = playerId,
                name = playerName.ifBlank { "Player ${room.players.size + 1}" },
                isHost = false,
                connected = true
            )

            val updatedPlayers = room.players.toMutableMap()
            updatedPlayers[playerId] = newPlayer

            roomRef.child("players").child(playerId).setValue(newPlayer).await()

            // Setup presence on disconnect
            roomRef.child("players").child(playerId).child("connected")
                .onDisconnect().setValue(false)

            Result.success(room.copy(players = updatedPlayers))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to join room: $cleanCode", e)
            Result.failure(e)
        }
    }

    /**
     * Observes real-time updates for a given room code.
     */
    fun observeRoom(roomCode: String): Flow<RemoteRoom?> = callbackFlow {
        val db = getDatabase()
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val cleanCode = roomCode.trim().uppercase()
        val roomRef = db.getReference(ROOMS_NODE).child(cleanCode)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val room = snapshot.getValue(RemoteRoom::class.java)
                    trySend(room)
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Room observation cancelled: ${error.message}")
                close(error.toException())
            }
        }

        roomRef.addValueEventListener(listener)

        awaitClose {
            roomRef.removeEventListener(listener)
        }
    }

    /**
     * Updates the status of a room (e.g. LOBBY -> PLAYING -> FINISHED).
     */
    suspend fun updateRoomStatus(roomCode: String, status: RoomStatus): Boolean {
        val db = getDatabase() ?: return false
        val roomRef = db.getReference(ROOMS_NODE).child(roomCode.uppercase())
        return try {
            roomRef.child("status").setValue(status.name).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update room status", e)
            false
        }
    }

    /**
     * Updates the game state payload dictionary.
     */
    suspend fun updateGameState(roomCode: String, gameState: Map<String, Any?>): Boolean {
        val db = getDatabase() ?: return false
        val roomRef = db.getReference(ROOMS_NODE).child(roomCode.uppercase())
        return try {
            roomRef.child("gameState").setValue(gameState).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update game state", e)
            false
        }
    }

    /**
     * Leaves a room and updates presence.
     */
    suspend fun leaveRoom(roomCode: String, playerId: String) {
        val db = getDatabase() ?: return
        val roomRef = db.getReference(ROOMS_NODE).child(roomCode.uppercase())
        try {
            roomRef.child("players").child(playerId).child("connected").setValue(false).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving room", e)
        }
    }
}
