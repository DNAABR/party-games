package com.leminno.partygames.ui.hub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.repository.UserPreferencesRepository
import com.leminno.partygames.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsSheet(
    onDismissRequest: () -> Unit
) {
    val keepAwake by UserPreferencesRepository.keepScreenAwake.collectAsState()
    val hapticsEnabled by UserPreferencesRepository.hapticsEnabled.collectAsState()
    val audioEnabled by UserPreferencesRepository.audioEnabled.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = SurfaceGlassDark,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚙️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "QUICK SETTINGS",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "AMBIENT & FEEDBACK CONTROLS",
                            color = Color(0xFF00F2FE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Text("✕", color = TextSecondary, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Keep Screen Awake Option
            SettingSwitchRow(
                title = "Keep Screen Awake",
                subtitle = "Prevents screen from dimming/sleeping during active games",
                icon = "💡",
                checked = keepAwake,
                onCheckedChange = { UserPreferencesRepository.setKeepScreenAwake(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Haptics Option
            SettingSwitchRow(
                title = "Vibration Haptics",
                subtitle = "Physical vibration feedback on buttons & spin wheels",
                icon = "📳",
                checked = hapticsEnabled,
                onCheckedChange = { UserPreferencesRepository.setHapticsEnabled(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Audio FX Option
            SettingSwitchRow(
                title = "Sound Effects & Audio",
                subtitle = "In-game sound triggers & countdown tones",
                icon = "🔊",
                checked = audioEnabled,
                onCheckedChange = { UserPreferencesRepository.setAudioEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("DONE", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGlassDark)
            .border(1.dp, BorderGlassDefault, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Color(0xFF00F2FE),
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceGlassDark
                )
            )
        }
    }
}
