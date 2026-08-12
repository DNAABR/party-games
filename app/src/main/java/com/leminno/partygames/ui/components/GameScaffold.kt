package com.leminno.partygames.ui.components

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.GameCatalogRepository
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info

/**
 * Reusable layout scaffold for all in-game screens.
 * Includes standardized top header bar, edge-to-edge padding, system BackHandler exit confirmation,
 * Keep Screen Awake window flag management, and mid-game rules peek sheet.
 */
@Composable
fun GameScaffold(
    title: String,
    titleColor: Color = TextPrimary,
    gameId: String? = null,
    onExitGame: () -> Unit,
    showExitConfirmation: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showRulesSheet by remember { mutableStateOf(false) }

    val isKeepScreenAwake by UserPreferencesRepository.keepScreenAwake.collectAsState()

    // Screen Awake Window Flag Manager
    DisposableEffect(isKeepScreenAwake) {
        val window = (context as? Activity)?.window
        if (isKeepScreenAwake && window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    val matchedGameItem = remember(gameId) {
        gameId?.let { id -> GameCatalogRepository.allGames.find { it.id == id } }
    }

    fun handleExitAttempt() {
        if (showExitConfirmation) {
            showConfirmDialog = true
        } else {
            onExitGame()
        }
    }

    BackHandler(enabled = true) {
        handleExitAttempt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundNavySlate)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGlassDark)
                        .border(1.dp, BorderGlassDefault, RoundedCornerShape(12.dp))
                ) {
                    IconButton(
                        onClick = { handleExitAttempt() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Exit",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = title.uppercase(),
                    color = titleColor,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    maxLines = 1
                )

                if (matchedGameItem != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark)
                            .border(1.dp, BorderGlassDefault, RoundedCornerShape(12.dp))
                    ) {
                        IconButton(
                            onClick = { showRulesSheet = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Rules",
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            // Game Content
            content()
        }


        // Mid-Game Rules Peek Sheet Modal
        if (showRulesSheet && matchedGameItem != null) {
            InGameRulesSheet(
                game = matchedGameItem,
                onDismissRequest = { showRulesSheet = false }
            )
        }

        // Exit Confirmation Dialog Modal
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        text = "EXIT GAME?",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text(
                        text = "Active game progress will be lost. Are you sure you want to leave?",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            onExitGame()
                        }
                    ) {
                        Text("EXIT", color = Color(0xFFFF0055), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("CANCEL", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SurfaceGlassDark,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.border(1.dp, BorderGlassDefault, RoundedCornerShape(20.dp))
            )
        }
    }
}
