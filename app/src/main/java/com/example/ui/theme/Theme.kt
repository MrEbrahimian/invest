package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

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
    val context = LocalContext.current

    val safeFontFamily = remember(context) {
        try {
            // Attempt to load the fonts natively using Android's ResourcesCompat
            androidx.core.content.res.ResourcesCompat.getFont(context, com.example.R.font.estedad_regular)
            androidx.core.content.res.ResourcesCompat.getFont(context, com.example.R.font.estedad_bold)
            // If they load fine without throwing exceptions, we can safely use the custom font family
            EstedadFontFamily
        } catch (t: Throwable) {
            // If loading fails (e.g. Samsung system font bug, compression issue), fallback to standard system sans-serif
            FontFamily.SansSerif
        }
    }

    val typography = remember(safeFontFamily) {
        getTypography(safeFontFamily)
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}
