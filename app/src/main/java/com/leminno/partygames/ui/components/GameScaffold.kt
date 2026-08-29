package com.leminno.partygames.ui.components

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.GameCatalogRepository
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.theme.*

/**
 * Reusable layout scaffold for all in-game screens.
 * Clean, chill light-mode navigation bar, edge-to-edge layout, Keep Screen Awake management,
 * and mid-game rules peek sheet.
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
            .background(CanvasLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Modern Clean Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceLight)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit / Back Button
                IconButton(
                    onClick = { handleExitAttempt() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                ) {
                    Icon(
                        imageVector = PixelIcons.Close,
                        contentDescription = "Exit",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Centered Clean Title
                Text(
                    text = title,
                    color = TextPrimary,
                    fontFamily = ModernSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                if (matchedGameItem != null) {
                    IconButton(
                        onClick = { showRulesSheet = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceSubtle)
                    ) {
                        Icon(
                            imageVector = PixelIcons.Lightbulb,
                            contentDescription = "Rules",
                            tint = BrandPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(38.dp))
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

        // Exit Confirmation Dialog Modal (Modern Clean Dialog)
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        text = "Exit Game?",
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Active game progress will be lost. Return to the games hub?",
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    PrimaryPartyButton(
                        text = "Exit",
                        onClick = {
                            showConfirmDialog = false
                            onExitGame()
                        },
                        accentColor = AlertRed,
                        modifier = Modifier.height(44.dp)
                    )
                },
                dismissButton = {
                    SecondaryPartyButton(
                        text = "Stay",
                        onClick = { showConfirmDialog = false },
                        modifier = Modifier.height(44.dp)
                    )
                },
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
            )
        }
    }
}

