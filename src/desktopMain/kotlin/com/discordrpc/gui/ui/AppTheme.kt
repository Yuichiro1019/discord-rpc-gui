package com.discordrpc.gui.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.discordrpc.gui.theme.ThemeColors
import com.discordrpc.gui.theme.ThemeEngine

// Provide colors down the tree
val LocalAppColors = staticCompositionLocalOf { ThemeEngine.PlushAmber }

object AppTheme {
    val colors: ThemeColors
        @Composable
        get() = LocalAppColors.current
}

@Composable
fun AppTheme(
    colors: ThemeColors = ThemeEngine.PlushAmber,
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryContainer,
        onPrimaryContainer = colors.onPrimaryContainer,
        secondary = colors.secondary,
        secondaryContainer = colors.secondaryContainer,
        error = colors.error,
        background = colors.background,
        surface = colors.surfaceContainer,
        onBackground = colors.textPrimary,
        onSurface = colors.textPrimary,
        onSurfaceVariant = colors.textSecondary,
        outline = colors.outline
    )

    val appTypography = Typography(
        displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 48.sp, color = colors.textPrimary, letterSpacing = (-0.96).sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colors.textPrimary),
        titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.textPrimary),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colors.textPrimary),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, color = colors.textPrimary),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, color = colors.textSecondary),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, color = colors.textSecondary),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.textPrimary),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = colors.textPrimary, letterSpacing = 0.65.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, color = colors.textMuted)
    )

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = darkColorScheme,
            typography = appTypography,
            content = content
        )
    }
}
