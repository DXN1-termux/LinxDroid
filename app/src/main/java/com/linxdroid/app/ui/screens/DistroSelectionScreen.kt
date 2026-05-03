package com.linxdroid.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.linxdroid.app.model.Distribution
import com.linxdroid.app.model.Distributions
import com.linxdroid.app.ui.theme.*

@Composable
fun DistroSelectionScreen(onInstall: (Distribution) -> Unit) {
    var selected by remember { mutableStateOf<Distribution?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Choose a Distribution",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Select the Linux distribution to install. Alpine is the fastest to download.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Distributions.all) { distro ->
                DistroCard(
                    distribution = distro,
                    isSelected = selected?.id == distro.id,
                    onClick = { selected = distro }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { selected?.let(onInstall) },
            enabled = selected != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor    = GreenPrimary,
                contentColor      = BackgroundDark,
                disabledContainerColor = SurfaceVariant,
                disabledContentColor   = TextDisabled
            )
        ) {
            Text(
                text = if (selected != null) "Install ${selected!!.name}" else "Select a distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DistroCard(
    distribution: Distribution,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) GreenPrimary else CardBorder
    val bgColor = if (isSelected) GreenContainer else SurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = distribution.iconEmoji, fontSize = 28.sp)
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = distribution.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) GreenOnContainer else TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = distribution.version,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) GreenPrimary else TextSecondary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = distribution.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) GreenOnContainer.copy(alpha = 0.8f) else TextSecondary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "~${distribution.estimatedSizeMb} MB",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) GreenPrimary else TerminalSystem
            )
        }

        if (isSelected) {
            Spacer(Modifier.width(8.dp))
            RadioButton(
                selected = true,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
            )
        }
    }
}
