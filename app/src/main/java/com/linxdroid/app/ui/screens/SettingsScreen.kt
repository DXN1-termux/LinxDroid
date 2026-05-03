package com.linxdroid.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linxdroid.app.BuildConfig
import com.linxdroid.app.model.Distribution
import com.linxdroid.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    distribution: Distribution?,
    installedSizeMb: Long,
    vncPort: Int,
    vncDisplay: Int,
    customArgs: String,
    onBack: () -> Unit,
    onUninstall: () -> Unit,
    onSaveVnc: (port: Int, display: Int) -> Unit,
    onSaveCustomArgs: (String) -> Unit
) {
    var showUninstallDialog by remember { mutableStateOf(false) }
    var portInput    by remember { mutableStateOf(vncPort.toString()) }
    var displayInput by remember { mutableStateOf(vncDisplay.toString()) }
    var argsInput    by remember { mutableStateOf(customArgs) }

    if (showUninstallDialog) {
        AlertDialog(
            onDismissRequest = { showUninstallDialog = false },
            title = { Text("Uninstall Distribution") },
            text = {
                Text(
                    "This will permanently delete the installed rootfs ($installedSizeMb MB). Are you sure?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showUninstallDialog = false; onUninstall() },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) { Text("Yes, Uninstall") }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallDialog = false }) { Text("Cancel") }
            },
            containerColor = SurfaceVariant
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader("Distribution")

            if (distribution != null) {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                distribution.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                "v${distribution.version} · $installedSizeMb MB installed",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            distribution.iconEmoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showUninstallDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        border = BorderStroke(1.dp, ErrorRed)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Uninstall Distribution")
                    }
                }
            } else {
                SettingsCard {
                    Text(
                        "No distribution installed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("VNC")

            SettingsCard {
                Text(
                    "VNC Port",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = portInput,
                    onValueChange = { portInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = settingsTextFieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "VNC Display Number",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = displayInput,
                    onValueChange = { displayInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = settingsTextFieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSaveVnc(
                            portInput.toIntOrNull() ?: 5900,
                            displayInput.toIntOrNull() ?: 0
                        )
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor   = BackgroundDark
                    )
                ) { Text("Save") }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Advanced")

            SettingsCard {
                Text(
                    "Custom PRoot Arguments",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Additional flags passed to proot on session start",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = argsInput,
                    onValueChange = { argsInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = settingsTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onSaveCustomArgs(argsInput) },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor   = BackgroundDark
                    )
                ) { Text("Save") }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("About")

            SettingsCard {
                InfoRow("App", "LinxDroid")
                InfoRow("Version", BuildConfig.VERSION_NAME)
                InfoRow("Package", BuildConfig.APPLICATION_ID)
                InfoRow("Build Type", BuildConfig.BUILD_TYPE)
                InfoRow("Min SDK", "Android 8.0 (API 26)")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = GreenPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@Composable
private fun settingsTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = GreenPrimary,
    unfocusedBorderColor    = CardBorder,
    cursorColor             = GreenPrimary,
    focusedTextColor        = TextPrimary,
    unfocusedTextColor      = TextPrimary,
    focusedContainerColor   = SurfaceBright,
    unfocusedContainerColor = SurfaceBright
)
