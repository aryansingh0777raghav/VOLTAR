package com.voltar.battery.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.voltar.battery.core.datastore.ThemeMode

data class VoltarCustomColors(
    val background: Color,
    val surface: Color,
    val surfaceTranslucent: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val accent: Color,
    val cardBackground: Color,
    val isDark: Boolean
)

val LocalVoltarColors = staticCompositionLocalOf {
    VoltarCustomColors(
        background = LightBackground,
        surface = LightSurface,
        surfaceTranslucent = LightSurfaceTranslucent,
        surfaceVariant = Color(0xFFEBECEE),
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        border = LightBorder,
        accent = EmeraldDark,
        cardBackground = LightCardBackground,
        isDark = false
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1B3D2B),
    onPrimaryContainer = EmeraldPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF1E1E1E),
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = EmeraldDark,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = Color(0xFFEBECEE),
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    outline = LightBorder
)

@Composable
fun VoltarTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val customColors = if (darkTheme) {
        VoltarCustomColors(
            background = DarkBackground,
            surface = DarkSurface,
            surfaceTranslucent = DarkSurfaceTranslucent,
            surfaceVariant = Color(0xFF1E1E1E),
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            border = DarkBorder,
            accent = EmeraldPrimary,
            cardBackground = DarkCardBackground,
            isDark = true
        )
    } else {
        VoltarCustomColors(
            background = LightBackground,
            surface = LightSurface,
            surfaceTranslucent = LightSurfaceTranslucent,
            surfaceVariant = Color(0xFFEBECEE),
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            border = LightBorder,
            accent = EmeraldDark,
            cardBackground = LightCardBackground,
            isDark = false
        )
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            } catch (_: Throwable) {}
        }
    }

    CompositionLocalProvider(LocalVoltarColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object VoltarTheme {
    val colors: VoltarCustomColors
        @Composable
        get() = LocalVoltarColors.current

    val typography: androidx.compose.material3.Typography
        @Composable
        get() = MaterialTheme.typography

    val shapes: androidx.compose.material3.Shapes
        @Composable
        get() = MaterialTheme.shapes
}
