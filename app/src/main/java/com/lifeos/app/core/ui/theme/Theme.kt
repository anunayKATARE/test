package com.lifeos.app.core.ui.theme

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

private val LightColors = lightColorScheme(
    primary = LifeOSGreen40,
    onPrimary = NeutralGrey99,
    primaryContainer = LifeOSGreen90,
    onPrimaryContainer = LifeOSGreen10,
    secondary = LifeOSBlue40,
    tertiary = LifeOSAmber40,
    error = LifeOSRed40,
    background = NeutralGrey99,
    onBackground = NeutralGrey10,
    surface = NeutralGrey99,
    onSurface = NeutralGrey10,
    surfaceVariant = NeutralGrey95,
)

private val DarkColors = darkColorScheme(
    primary = LifeOSGreen80,
    onPrimary = LifeOSGreen20,
    primaryContainer = LifeOSGreen30,
    onPrimaryContainer = LifeOSGreen90,
    secondary = LifeOSBlue80,
    tertiary = LifeOSAmber80,
    error = LifeOSRed80,
    background = NeutralGrey10,
    onBackground = NeutralGrey90,
    surface = NeutralGrey10,
    onSurface = NeutralGrey90,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF44483E),
)

@Composable
fun LifeOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            activity?.window?.let { window ->
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LifeOSTypography,
        content = content,
    )
}
