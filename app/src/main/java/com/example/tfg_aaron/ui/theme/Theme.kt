package com.example.tfg_aaron.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = NavyDark,
    primaryContainer = Color(0xFF1A2E00),
    onPrimaryContainer = NeonGreen,
    secondary = OrangeBase,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF7C2D00),
    onSecondaryContainer = Color(0xFFFFDBCC),
    tertiary = TealAccent,
    onTertiary = NavyDark,
    tertiaryContainer = Color(0xFF003050),
    onTertiaryContainer = Color(0xFFB9EAFF),
    error = RedError,
    onError = Color.White,
    errorContainer = RedSurface,
    onErrorContainer = Color(0xFFFFDAD6),
    background = NavyDark,
    onBackground = TextPrimary,
    surface = NavyCard,
    onSurface = TextPrimary,
    surfaceVariant = NavyElevated,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = Color(0xFF253047),
    inverseSurface = Color(0xFFEFF1F6),
    inverseOnSurface = Color(0xFF1A1A2E),
    inversePrimary = NeonGreenDim,
    surfaceTint = NeonGreen,
    scrim = Color(0x80000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3AAA00),         // Vivid green — acento principal
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFF85),
    onPrimaryContainer = Color(0xFF0A2000),
    secondary = Color(0xFF2E8800),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDFFB0),
    onSecondaryContainer = Color(0xFF062000),
    tertiary = Color(0xFF0055CC),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6E4FF),
    onTertiaryContainer = Color(0xFF001A42),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFFFF),      // Fondo blanco puro
    onBackground = Color(0xFF0B1326),
    surface = Color(0xFFFFFFFF),         // Tarjetas blancas
    onSurface = Color(0xFF0B1326),
    surfaceVariant = Color(0xFFF4F6FA),
    onSurfaceVariant = Color(0xFF3D4F6E),
    outline = Color(0xFFADB8CC),
    outlineVariant = Color(0xFFDDE3EE),
    inverseSurface = Color(0xFF0B1326),
    inverseOnSurface = Color(0xFFF5F5F0),
    inversePrimary = Color(0xFFADFF2F),
    surfaceTint = Color(0xFF3AAA00),
    scrim = Color(0x80000000)
)

@Composable
fun TFGAaronTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    // Sync global reactive color state — triggers recomposition of all screens
    // that use any NavyDark / NavyCard / NeonGreen etc. color getter.
    isLightTheme.value = !darkTheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bgColor = if (darkTheme) Color(0xFF0B1326).toArgb() else Color(0xFFFFFFFF).toArgb()
            window.statusBarColor = bgColor
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
