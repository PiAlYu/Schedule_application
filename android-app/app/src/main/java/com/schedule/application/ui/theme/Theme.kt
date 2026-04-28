package com.schedule.application.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.schedule.application.data.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    secondary = OrangeSecondary,
    tertiary = MintTertiary,
)

private val DarkColors = darkColorScheme(
    primary = DarkOrangePrimary,
    secondary = DarkOrangeSecondary,
    tertiary = DarkMintTertiary,
)

@Composable
fun ScheduleAppTheme(
    mode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content,
    )
}
