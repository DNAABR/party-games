package com.leminno.partygames.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.ui.theme.*

@Composable
fun VictoryCeremonyOverlay(
    winnerTitle: String,
    subtitle: String = "GREAT GAME, ARCADE LEGENDS!",
    onPlayAgain: () -> Unit,
    onBackToHub: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, PixelOutlineBlack, RoundedCornerShape(2.dp))
                .background(
                    brush = pixelBandedVertical(listOf(PixelVioletElevated, PixelVioletBase)),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(2.dp, PixelOutlineBlack)
                    .background(PixelAmberGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PixelIcons.Trophy,
                    contentDescription = "Victory Trophy",
                    tint = PixelOutlineBlack,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "GAME OVER",
                color = PixelMagentaHot,
                fontFamily = PressStart2PFont,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = winnerTitle.uppercase(),
                color = PixelCrtCyan,
                fontFamily = PressStart2PFont,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle.uppercase(),
                color = TextSecondary,
                fontFamily = PressStart2PFont,
                fontSize = 8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Play Again Button
            PrimaryPartyButton(
                text = "PLAY AGAIN",
                icon = PixelIcons.Zap,
                onClick = onPlayAgain,
                accentColor = PixelCrtCyan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Back to Hub Button
            SecondaryPartyButton(
                text = "ARCADE HUB",
                onClick = onBackToHub,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
