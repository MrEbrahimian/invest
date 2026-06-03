package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,           // Noble Gold
    secondary = GoldSecondary,        // Bronze Gold
    tertiary = StockGreen,           // Soft Green
    background = DarkBackground,     // Midnight space comfort obsidian
    surface = DarkSurface,           // Dark navy sleek card
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onBackground = Color(0xFFF5F3EF), // Comfort off-white cream
    onSurface = Color(0xFFF5F3EF),
    onSurfaceVariant = SlateTextSecondary,
    error = StockRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB39256),     // Noble darker gold
    secondary = Color(0xFFC5A872),   // Matte Gold
    tertiary = Color(0xFF2E7D32),    // Clean standard Green
    background = Color(0xFFFAF8F5),  // Soft cream/sand canvas background (just like user's screens)
    surface = Color(0xFFFFFFFF),     // Pure white surface card
    surfaceVariant = Color(0xFFF3EFE9), // Light warm beige/sand variant
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1F1B18), // Deep charcoal brown-slate for magnificent warm contrast
    onSurface = Color(0xFF1F1B18),    // Elegant readability
    onSurfaceVariant = Color(0xFF5C544E), // Muted slate warm text
    error = Color(0xFFC62828)        // Standard crimson red
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
