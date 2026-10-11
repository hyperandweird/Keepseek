package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Slate950,
    primaryContainer = Slate800,
    onPrimaryContainer = CyanGlow,
    secondary = GoldAccent,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = GoldGlow,
    tertiary = EmeraldNeon,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    error = RoseNeon,
    onError = Color.White
)

private val ObsidianMidnightColorScheme = darkColorScheme(
    primary = IndigoNeon,
    onPrimary = PureBlack,
    primaryContainer = DarkCard,
    onPrimaryContainer = Color.White,
    secondary = CyanNeon,
    onSecondary = PureBlack,
    secondaryContainer = DarkCard,
    onSecondaryContainer = CyanGlow,
    tertiary = EmeraldNeon,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = Slate100,
    surface = DarkSurface,
    onSurface = Slate100,
    surfaceVariant = DarkCard,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    error = RoseNeon,
    onError = Color.White
)

private val EmeraldVaultColorScheme = darkColorScheme(
    primary = EmeraldGlow,
    onPrimary = DeepEmeraldBg,
    primaryContainer = DeepEmeraldCard,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    onSecondary = DeepEmeraldBg,
    secondaryContainer = DeepEmeraldCard,
    onSecondaryContainer = GoldGlow,
    tertiary = CyanNeon,
    onTertiary = DeepEmeraldBg,
    background = DeepEmeraldBg,
    onBackground = Slate100,
    surface = DeepEmeraldCard,
    onSurface = Slate100,
    surfaceVariant = DeepEmeraldBorder,
    onSurfaceVariant = Slate400,
    outline = DeepEmeraldBorder,
    error = RoseNeon,
    onError = Color.White
)

private val CleanLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightCard,
    onPrimaryContainer = LightPrimary,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = LightCard,
    onSecondaryContainer = Slate800,
    tertiary = EmeraldNeon,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightSubtext,
    outline = LightBorder,
    error = RoseDark,
    onError = Color.White
)

@Composable
fun KeepseekTheme(
    themeMode: AppThemeMode = AppThemeMode.CYBER_DARK,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.CYBER_DARK -> CyberDarkColorScheme
        AppThemeMode.OBSIDIAN_MIDNIGHT -> ObsidianMidnightColorScheme
        AppThemeMode.EMERALD_VAULT -> EmeraldVaultColorScheme
        AppThemeMode.CLEAN_LIGHT -> CleanLightColorScheme
        AppThemeMode.SYSTEM -> if (isSystemDark) CyberDarkColorScheme else CleanLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
