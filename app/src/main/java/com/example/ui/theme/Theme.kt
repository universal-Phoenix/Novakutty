package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CosmicCyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    secondary = CyberGreen,
    tertiary = CyberAmber,
    background = CyberBlack,
    surface = CyberCard,
    onPrimary = CyberBlack,
    onSecondary = CyberBlack,
    onTertiary = CyberBlack,
    onBackground = CyberWhite,
    onSurface = CyberWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicCyberColorScheme,
        typography = Typography,
        content = content
    )
}
