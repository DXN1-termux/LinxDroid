package com.linxdroid.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linxdroid.app.model.AppState
import com.linxdroid.app.ui.theme.*
import java.text.DecimalFormat

@Composable
fun ProgressScreen(state: AppState) {
    val isDownloading = state is AppState.Downloading
    val isExtracting  = state is AppState.Extracting

    val progress = when (state) {
        is AppState.Downloading -> state.progress
        is AppState.Extracting  -> state.progress
        else -> 0f
    }

    val distroName = when (state) {
        is AppState.Downloading -> state.distro.name
        is AppState.Extracting  -> state.distro.name
        else -> ""
    }

    val animatedProgress by animateFloatAsState(
        targetValue  = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isDownloading) "📥" else "📦",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = if (isDownloading) "Downloading $distroName" else "Extracting $distroName",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (isDownloading) "Please wait while the rootfs is downloaded…"
                       else "Unpacking files to your device…",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = GreenPrimary,
                trackColor = SurfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold
                )

                if (state is AppState.Downloading && state.totalBytes > 0) {
                    Text(
                        text = "${formatBytes(state.bytesReceived)} / ${formatBytes(state.totalBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            StepIndicator(
                steps = listOf("Download", "Extract", "Ready"),
                currentStep = if (isDownloading) 0 else 1
            )
        }
    }
}

@Composable
private fun StepIndicator(steps: List<String>, currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isActive   = index == currentStep
            val isDone     = index < currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when {
                                isDone   -> GreenPrimary
                                isActive -> GreenContainer
                                else     -> SurfaceVariant
                            },
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDone) "✓" else "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isDone   -> BackgroundDark
                            isActive -> GreenPrimary
                            else     -> TextDisabled
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isDone   -> GreenPrimary
                        isActive -> TextPrimary
                        else     -> TextDisabled
                    }
                )
            }

            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(40.dp)
                        .padding(bottom = 24.dp),
                    color = if (isDone) GreenPrimary else CardBorder,
                    thickness = 1.dp
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val df = DecimalFormat("0.0")
    return when {
        bytes >= 1_000_000 -> "${df.format(bytes / 1_000_000.0)} MB"
        bytes >= 1_000     -> "${df.format(bytes / 1_000.0)} KB"
        else               -> "$bytes B"
    }
}
