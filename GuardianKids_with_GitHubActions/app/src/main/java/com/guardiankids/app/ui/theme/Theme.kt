package com.guardiankids.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    secondary = SunnyYellow,
    onSecondary = DeepBlue,
    tertiary = LeafGreen,
    background = SoftBackground,
    onBackground = DeepBlue,
    surface = Color.White,
    onSurface = DeepBlue
)

@Composable
fun GKTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content
    )
}
