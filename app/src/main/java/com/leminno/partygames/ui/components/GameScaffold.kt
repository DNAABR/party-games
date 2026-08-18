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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.GameCatalogRepository
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.theme.*

/**
 * Reusable layout scaffold for all in-game screens.
 * Includes standardized pixel top header bar, edge-to-edge padding, system BackHandler exit confirmation,
 * Keep Screen Awake window flag management, and mid-game rules peek sheet.
 */
@Composable
fun GameScaffold(
    title: String,
    titleColor: Color = PixelCrtCyan,
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
            .background(PixelVioletDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Pixel Arcade Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                    .background(
                        brush = pixelBandedVertical(
                            listOf(PixelVioletElevated, PixelVioletBase)
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit Button
                IconButton(
                    onClick = { handleExitAttempt() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = PixelIcons.Close,
                        contentDescription = "Exit",
                        tint = PixelMagentaHot,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Arcade Marquee Title
                Text(
                    text = title.uppercase(),
                    color = titleColor,
                    fontFamily = PressStart2PFont,
                    fontSize = 11.sp,
                    maxLines = 1
                )

                if (matchedGameItem != null) {
                    IconButton(
                        onClick = { showRulesSheet = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = PixelIcons.Lightbulb,
                            contentDescription = "Rules",
                            tint = PixelCrtCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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

        // Exit Confirmation Dialog Modal (8-bit Pixel Styled)
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        text = "EXIT GAME?",
                        color = PixelMagentaHot,
                        fontFamily = PressStart2PFont,
                        fontSize = 13.sp
                    )
                },
                text = {
                    Text(
                        text = "Active game progress will be lost. Return to Arcade Hub?",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    PrimaryPartyButton(
                        text = "EXIT",
                        onClick = {
                            showConfirmDialog = false
                            onExitGame()
                        },
                        accentColor = PixelAlertRed
                    )
                },
                dismissButton = {
                    SecondaryPartyButton(
                        text = "CANCEL",
                        onClick = { showConfirmDialog = false }
                    )
                },
                containerColor = PixelVioletElevated,
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
            )
        }
    }
}
