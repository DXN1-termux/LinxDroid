package com.linxdroid.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LinxDroidColorScheme = darkColorScheme(
    primary            = GreenPrimary,
    onPrimary          = BackgroundDark,
    primaryContainer   = GreenContainer,
    onPrimaryContainer = GreenOnContainer,
    secondary          = GreenDark,
    onSecondary        = TextPrimary,
    background         = BackgroundDark,
    onBackground       = TextPrimary,
    surface            = SurfaceDark,
    onSurface          = TextPrimary,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = TextSecondary,
    error              = ErrorRed,
    onError            = TextPrimary,
    outline            = CardBorder
)

@Composable
fun LinxDroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LinxDroidColorScheme,
        typography  = Typography,
        content     = content
    )
}
