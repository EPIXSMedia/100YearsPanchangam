package com.panchangam100.live.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410001),
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0C2),
    onSecondaryContainer = Color(0xFF221B00),
    tertiary = SacredOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCE),
    onTertiaryContainer = Color(0xFF3A0900),
    error = InauspiciousRed,
    onError = Color.White,
    background = ParchmentWhite,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5E6D0),
    onSurfaceVariant = TextSecondary,
    outline = CardBorderLight,
    outlineVariant = Color(0xFFD4C4A0)
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldLight,
    onPrimary = Color(0xFF3A0000),
    primaryContainer = CrimsonDark,
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = GoldLight,
    onSecondary = Color(0xFF221B00),
    secondaryContainer = GoldDark,
    onSecondaryContainer = Color(0xFFFFF0C2),
    tertiary = SacredOrangeLight,
    onTertiary = Color(0xFF3A0900),
    tertiaryContainer = SacredOrangeDark,
    onTertiaryContainer = Color(0xFFFFDBCE),
    error = InauspiciousRedLight,
    onError = Color(0xFF690005),
    background = TempleBg,
    onBackground = TextOnDark,
    surface = SurfaceDark,
    onSurface = TextOnDark,
    surfaceVariant = TempleCardBg,
    onSurfaceVariant = TextOnDarkSecondary,
    outline = CardBorderDark,
    outlineVariant = Color(0xFF3D2800)
)

@Composable
fun PanchangamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
