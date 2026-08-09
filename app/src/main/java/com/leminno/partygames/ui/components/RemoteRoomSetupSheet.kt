package com.leminno.partygames.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.RemotePlayer
import com.leminno.partygames.data.remote.RemoteRoom
import com.leminno.partygames.data.remote.RemoteRoomRepository
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

val AccentCyanColor = Color(0xFF00F2FE)
val AccentPurpleColor = Color(0xFF9D4EDD)
val GradientPurpleCyanBrush = Brush.horizontalGradient(listOf(AccentPurpleColor, AccentCyanColor))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteRoomSetupSheet(
    gameId: String,
    gameName: String,
    initialRoomCode: String? = null,
    onDismiss: () -> Unit,
    onRoomJoined: (roomCode: String, isHost: Boolean, playerId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localPlayerId = remember { RemoteRoomRepository.getOrCreatePlayerId(context) }

    var selectedTab by remember { mutableIntStateOf(if (initialRoomCode != null) 1 else 0) } // 0 = Host, 1 = Join
    var playerName by remember { mutableStateOf("Player ${localPlayerId.take(4)}") }

    // Host State
    var createdRoomCode by remember { mutableStateOf("") }
    var isCreatingRoom by remember { mutableStateOf(false) }
    var activeRoom by remember { mutableStateOf<RemoteRoom?>(null) }

    // Join State
    var inputCode by remember { mutableStateOf(initialRoomCode ?: "") }
    var isJoiningRoom by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observe created room updates if host
    LaunchedEffect(createdRoomCode) {
        if (createdRoomCode.isNotBlank()) {
            RemoteRoomRepository.observeRoom(createdRoomCode).collect { room ->
                activeRoom = room
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundObsidian,
        scrimColor = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "🌐 REMOTE MULTIPLAYER",
                color = AccentCyanColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = gameName,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Player Name Input
            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Your Display Name", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyanColor,
                    unfocusedBorderColor = BorderGlassDefault,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector Tabs (Host vs Join)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceGlassDark)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) GradientPurpleCyanBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👑 Host Room",
                        color = if (selectedTab == 0) Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) GradientPurpleCyanBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔑 Join with Code",
                        color = if (selectedTab == 1) Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TAB 0: HOST ROOM
            if (selectedTab == 0) {
                if (createdRoomCode.isEmpty()) {
                    Button(
                        onClick = {
                            isCreatingRoom = true
                            errorMessage = null
                            scope.launch {
                                val result = RemoteRoomRepository.createRoom(
                                    gameId = gameId,
                                    hostPlayerId = localPlayerId,
                                    hostPlayerName = playerName
                                )
                                isCreatingRoom = false
                                result.onSuccess { room ->
                                    createdRoomCode = room.roomCode
                                }.onFailure { err ->
                                    errorMessage = err.localizedMessage ?: "Failed to create room"
                                }
                            }
                        },
                        enabled = !isCreatingRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyanColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isCreatingRoom) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text("CREATE ONLINE ROOM", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    // Room Created View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceGlassDark)
                            .border(2.dp, AccentCyanColor, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ROOM CODE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = createdRoomCode,
                                color = AccentCyanColor,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Room Code", createdRoomCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("📋 Copy Code", color = TextPrimary)
                                }

                                Button(
                                    onClick = {
                                        RemoteRoomRepository.shareRoomInvite(context, gameName, createdRoomCode)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurpleColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🔗 Share Link", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player List
                    val playersList = activeRoom?.players?.values?.toList() ?: emptyList()
                    Text(
                        text = "PLAYERS JOINED (${playersList.size})",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(playersList) { player ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceGlassDark)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = player.name + if (player.isHost) " 👑 (Host)" else "",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (player.connected) Color(0xFF00FF87) else Color.Gray)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onRoomJoined(createdRoomCode, true, localPlayerId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyanColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("START GAME FOR ALL 🚀", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                // TAB 1: JOIN ROOM
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { if (it.length <= 6) inputCode = it.uppercase() },
                    label = { Text("Enter 6-Character Code", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyanColor,
                        unfocusedBorderColor = BorderGlassDefault,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (inputCode.length < 4) {
                            errorMessage = "Please enter a valid room code"
                            return@Button
                        }
                        isJoiningRoom = true
                        errorMessage = null
                        scope.launch {
                            val result = RemoteRoomRepository.joinRoom(
                                roomCode = inputCode,
                                playerId = localPlayerId,
                                playerName = playerName
                            )
                            isJoiningRoom = false
                            result.onSuccess { room ->
                                onRoomJoined(room.roomCode, false, localPlayerId)
                            }.onFailure { err ->
                                errorMessage = err.localizedMessage ?: "Failed to join room"
                            }
                        }
                    },
                    enabled = inputCode.isNotBlank() && !isJoiningRoom,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyanColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isJoiningRoom) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text("JOIN ROOM NOW 🔑", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Error Display
            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
