package com.hoandesign.standby.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.hoandesign.standby.model.NightModeState

/**
 * Composition local providing the active Night Mode status to child composables.
 */
val LocalNightMode = compositionLocalOf { false }

/**
 * Composition local providing the active accent color to child composables.
 */
val LocalAccentColor = compositionLocalOf { AccentOrange }

/**
 * Base Material 3 dark color scheme tuned for pitch-black OLED displays.
 */
private val StandbyBaseDarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = Color.Black,
    primaryContainer = StandbyCardBgSecondary,
    onPrimaryContainer = TextPrimary,
    secondary = AccentAmber,
    onSecondary = Color.Black,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = OledBlack,
    onSurface = TextPrimary,
    surfaceVariant = StandbyCardBg,
    onSurfaceVariant = TextSecondary,
    outline = StandbyBorder,
    outlineVariant = StandbyBorderSubtle
)

/**
 * Root theme for StandBy Android.
 *
 * Configures pitch-black OLED surfaces, deep red night mode palette switching,
 * custom typography scale, and StandBy-specific CompositionLocal providers.
 */
@Composable
fun StandByTheme(
    isNightMode: Boolean = false,
    accentColor: Color = AccentOrange,
    content: @Composable () -> Unit
) {
    val activeAccent = if (isNightMode) NightRed else accentColor
    val colorScheme = StandbyBaseDarkColorScheme.copy(
        primary = activeAccent,
        secondary = if (isNightMode) NightRedDim else AccentAmber,
        outline = if (isNightMode) NightRedDim else StandbyBorder
    )

    CompositionLocalProvider(
        LocalNightMode provides isNightMode,
        LocalAccentColor provides activeAccent,
        LocalStandbyTypography provides StandbyTypography()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StandbyMaterialTypography,
            content = content
        )
    }
}

/**
 * Overload for [StandByTheme] taking a [NightModeState] instance directly.
 */
@Composable
fun StandByTheme(
    nightModeState: NightModeState,
    accentColor: Color = AccentOrange,
    content: @Composable () -> Unit
) {
    StandByTheme(
        isNightMode = nightModeState.isNightModeActive,
        accentColor = accentColor,
        content = content
    )
}

/**
 * Direct accessor object for StandBy theme properties.
 */
object StandbyTheme {
    val isNightMode: Boolean
        @Composable
        get() = LocalNightMode.current

    val accentColor: Color
        @Composable
        get() = LocalAccentColor.current

    val typography: StandbyTypography
        @Composable
        get() = LocalStandbyTypography.current
}
