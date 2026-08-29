package com.leminno.partygames.ui.hub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.leminno.partygames.ui.components.PrimaryPartyButton
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
        containerColor = SurfaceLight,
        scrimColor = ScrimModal,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrandPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚙️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settings",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Preferences & Feedback",
                            color = TextSecondary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                ) {
                    Icon(
                        imageVector = PixelIcons.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Keep Screen Awake Option
            SettingSwitchRow(
                title = "Keep Screen Awake",
                subtitle = "Prevents screen from dimming during active games",
                icon = "💡",
                checked = keepAwake,
                onCheckedChange = { UserPreferencesRepository.setKeepScreenAwake(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Haptics Option
            SettingSwitchRow(
                title = "Vibration Haptics",
                subtitle = "Physical vibration feedback on buttons & actions",
                icon = "📳",
                checked = hapticsEnabled,
                onCheckedChange = { UserPreferencesRepository.setHapticsEnabled(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Audio FX Option
            SettingSwitchRow(
                title = "Sound Effects",
                subtitle = "In-game sound cues & timer alerts",
                icon = "🔊",
                checked = audioEnabled,
                onCheckedChange = { UserPreferencesRepository.setAudioEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryPartyButton(
                text = "Done",
                onClick = onDismissRequest,
                accentColor = BrandPrimary,
                modifier = Modifier.fillMaxWidth()
            )

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
            .background(SurfaceSubtle)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
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
                Text(text = icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontFamily = ModernSansFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontFamily = ModernSansFont,
                        fontSize = 12.sp
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextOnPrimary,
                    checkedTrackColor = BrandPrimary,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = Color(0xFFE2E8F0),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

