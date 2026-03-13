package dev.anaes.qrh.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = CardBgLight,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = CardBgLight,
    secondary = AccentLight,
    onSecondary = CardBgLight,
    background = WindowBackgroundLight,
    onBackground = BlackTxtLight,
    surface = CardBgLight,
    onSurface = BlackTxtLight,
    surfaceVariant = WindowBackgroundLight,
    onSurfaceVariant = BlackTxtLight,
    error = RedTxtLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = CardBgLight,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = CardBgLight,
    secondary = AccentDark,
    onSecondary = CardBgDark,
    background = WindowBackgroundDark,
    onBackground = BlackTxtDark,
    surface = CardBgDark,
    onSurface = BlackTxtDark,
    surfaceVariant = WindowBackgroundDark,
    onSurfaceVariant = BlackTxtDark,
    error = RedTxtDark,
)

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun QrhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QrhTypography,
            content = content
        )
    }
}
