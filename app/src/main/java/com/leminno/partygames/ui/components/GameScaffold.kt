package com.leminno.partygames.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*

/**
 * Reusable layout scaffold for all in-game screens.
 * Includes standardized top header bar, edge-to-edge padding, and system BackHandler exit confirmation.
 */
@Composable
fun GameScaffold(
    title: String,
    titleColor: Color = TextPrimary,
    onExitGame: () -> Unit,
    showExitConfirmation: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

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
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { handleExitAttempt() }) {
                    Text("✕", color = TextSecondary, fontSize = 22.sp)
                }
                Text(
                    text = title.uppercase(),
                    color = titleColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Game Content
            content()
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
