package com.linxdroid.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linxdroid.app.model.Distribution
import com.linxdroid.app.model.TerminalLine
import com.linxdroid.app.ui.components.TerminalView
import com.linxdroid.app.ui.theme.*

@Composable
fun TerminalScreen(
    distribution: Distribution,
    lines: List<TerminalLine>,
    onCommand: (String) -> Unit,
    onStopSession: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        TerminalTopBar(
            distribution = distribution,
            onStop = onStopSession,
            onClear = onClear
        )

        HorizontalDivider(color = CardBorder, thickness = 1.dp)

        TerminalView(
            lines = lines,
            onCommand = onCommand,
            sessionActive = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TerminalTopBar(
    distribution: Distribution,
    onStop: () -> Unit,
    onClear: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(GreenPrimary, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = distribution.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "terminal",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        actions = {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
            }
            IconButton(onClick = onStop) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = ErrorRed)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceDark
        )
    )
}
