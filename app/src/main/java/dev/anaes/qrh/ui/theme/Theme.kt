package dev.anaes.qrh.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = CardBgLight,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = CardBgLight,
    secondary = AccentLight,
    onSecondary = CardBgLight,
    secondaryContainer = SelectedBgLight,
    onSecondaryContainer = BlackTxtLight,
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
    secondaryContainer = SelectedBgDark,
    onSecondaryContainer = BlackTxtDark,
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

/**
 * Colours shared by every screen's top app bar: red in the light theme and dark grey in the dark
 * theme, matching the pre-Compose app. `primary` stays red in both themes for accents.
 */
@ExperimentalMaterial3Api
@Composable
fun qrhTopAppBarColors(): TopAppBarColors {
    val container = if (LocalIsDarkTheme.current) PrimaryDark else PrimaryLight
    return TopAppBarDefaults.topAppBarColors(
        containerColor = container,
        scrolledContainerColor = container,
        navigationIconContentColor = Color.White,
        titleContentColor = Color.White,
        actionIconContentColor = Color.White,
    )
}
