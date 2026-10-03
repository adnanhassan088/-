package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = CharcoalBackground,
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = GoldLight,
    onSecondary = CharcoalBackground,
    secondaryContainer = CharcoalSurfaceVariant,
    onSecondaryContainer = GoldLight,
    tertiary = TextGold,
    onTertiary = CharcoalBackground,
    background = CharcoalBackground,
    onBackground = TextWhite,
    surface = CharcoalSurface,
    onSurface = TextWhite,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = CharcoalCardBorder,
    outlineVariant = CharcoalCardBorder.copy(alpha = 0.6f)
)

private val LightColorScheme = DarkColorScheme // Elegant dark-first luxury aesthetic for Alfares brand

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CharcoalBackground.toArgb()
            window.navigationBarColor = CharcoalBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
