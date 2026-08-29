package com.leminno.partygames.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leminno.partygames.data.remote.AiGateway
import com.leminno.partygames.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPromptGeneratorSheet(
    gameTitle: String,
    onPromptsGenerated: (List<String>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var themeTopic by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var generatedPrompts by remember { mutableStateOf<List<String>>(emptyList()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = SurfaceLight,
        scrimColor = Color(0x660F172A),
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
                        Text(text = "✨", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Prompt Generator",
                            color = TextPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Custom pack for $gameTitle",
                            color = BrandPrimary,
                            fontFamily = ModernSansFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter a custom theme or topic (e.g. '90s Nostalgia', 'Office Humor', 'College Dorm'):",
                color = TextSecondary,
                fontFamily = ModernSansFont,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = themeTopic,
                    onValueChange = { themeTopic = it },
                    placeholder = { Text("e.g. Marvel Movies...", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(10.dp))

                PrimaryPartyButton(
                    text = if (isLoading) "..." else "Generate",
                    onClick = {
                        if (themeTopic.isNotBlank() && !isLoading) {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val prompt = "Generate 10 funny, short, creative party game prompts/words for the game '$gameTitle' themed around '${themeTopic.trim()}'. Output ONLY a line-separated list of the 10 prompts without numbering or extra text."
                                val result = AiGateway.askAiSuspend(prompt)
                                isLoading = false
                                result.onSuccess { response ->
                                    val lines = response.text.lines().map { it.trim().removePrefix("- ").removePrefix("* ") }.filter { it.isNotBlank() }
                                    if (lines.isNotEmpty()) {
                                        generatedPrompts = lines
                                    } else {
                                        errorMessage = "Failed to parse generated prompts."
                                    }
                                }.onFailure { err ->
                                    errorMessage = err.localizedMessage ?: "AI Gateway connection failed."
                                }
                            }
                        }
                    },
                    accentColor = BrandPrimary,
                    modifier = Modifier.height(52.dp)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage!!, color = AlertRed, fontFamily = ModernSansFont, fontSize = 12.sp)
            }

            if (generatedPrompts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Generated Pack (${generatedPrompts.size} Prompts):",
                    color = BrandPrimary,
                    fontFamily = ModernSansFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    items(generatedPrompts) { p ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSubtle)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(text = p, color = TextPrimary, fontFamily = ModernSansFont, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryPartyButton(
                    text = "Use This Prompt Pack",
                    onClick = {
                        onPromptsGenerated(generatedPrompts)
                        onDismissRequest()
                    },
                    accentColor = SuccessGreen,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

