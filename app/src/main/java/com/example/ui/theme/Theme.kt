package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightHeritageColorScheme = lightColorScheme(
    primary = TerracottaMaroon,
    onPrimary = ParchmentSurface,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = HeritageGold,
    onSecondary = CharcoalInk,
    secondaryContainer = HeritageGoldContainer,
    onSecondaryContainer = OnHeritageGoldContainer,
    tertiary = SageAccent,
    onTertiary = ParchmentSurface,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = Color(0xFF102A1C),
    background = ParchmentBackground,
    onBackground = CharcoalInk,
    surface = ParchmentSurface,
    onSurface = CharcoalInk,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = MutedInk,
    surfaceContainerLowest = ParchmentSurface,
    surfaceContainerLow = ParchmentBackground,
    surfaceContainer = ParchmentSurfaceContainer,
    surfaceContainerHigh = ParchmentSurfaceContainerHigh,
    outline = WarmOutline,
    outlineVariant = WarmOutlineSubtle,
    error = WarmError,
    onError = ParchmentSurface,
    errorContainer = WarmErrorContainer,
    onErrorContainer = Color(0xFF410E0B)
)

private val DarkHeritageColorScheme = darkColorScheme(
    primary = DarkTerracottaPrimary,
    onPrimary = DarkOnTerracottaPrimary,
    primaryContainer = DarkTerracottaContainer,
    onPrimaryContainer = TerracottaContainer,
    secondary = DarkGoldSecondary,
    onSecondary = DarkOnGoldSecondary,
    secondaryContainer = Color(0xFF543F05),
    onSecondaryContainer = HeritageGoldContainer,
    tertiary = Color(0xFF8CC9A7),
    onTertiary = Color(0xFF0A321F),
    tertiaryContainer = Color(0xFF1E3A2B),
    onTertiaryContainer = Color(0xFFDCEEE2),
    background = DarkParchmentBg,
    onBackground = DarkInkText,
    surface = DarkParchmentSurface,
    onSurface = DarkInkText,
    surfaceVariant = DarkParchmentSurfaceVariant,
    onSurfaceVariant = DarkMutedText,
    surfaceContainerLowest = Color(0xFF15110F),
    surfaceContainerLow = Color(0xFF1F1916),
    surfaceContainer = DarkParchmentSurface,
    surfaceContainerHigh = DarkParchmentSurfaceVariant,
    outline = DarkWarmOutline,
    outlineVariant = Color(0xFF3A312B)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ThirukkuralTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always preserve the requested warm parchment & terracotta heritage identity by default
    val colorScheme = if (darkTheme) DarkHeritageColorScheme else LightHeritageColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
