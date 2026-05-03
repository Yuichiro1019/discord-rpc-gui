package com.discordrpc.gui.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Premium dark color palette — inspired by Discord's dark mode
 * with carefully chosen accent colors and high-contrast text.
 */
object AppColors {
    // ── Base surfaces ──
    val background = Color(0xFF0B0B14)       // very deep navy-black
    val surface = Color(0xFF12122A)           // dark indigo
    val surfaceVariant = Color(0xFF1A1A3E)    // medium indigo
    val surfaceElevated = Color(0xFF20204A)   // elevated cards

    // ── Glass effects ──
    val card = Color(0x1AFFFFFF)              // 10% white
    val cardHover = Color(0x26FFFFFF)         // 15% white
    val cardBorder = Color(0x1AFFFFFF)        // subtle border
    val cardBorderFocused = Color(0x40FFFFFF) // focused border

    // ── Brand colors ──
    val primary = Color(0xFF5865F2)           // Discord Blurple
    val primaryLight = Color(0xFF7983F5)      // lighter blurple
    val primaryDim = Color(0xFF4752C4)        // pressed blurple
    val secondary = Color(0xFF57F287)         // Discord Green (brighter)
    val secondaryDim = Color(0xFF3BA55C)      // muted green
    val error = Color(0xFFED4245)             // Discord Red
    val errorDim = Color(0xFFD83C3E)
    val warning = Color(0xFFFEE75C)           // Discord Yellow
    val warningDim = Color(0xFFF0B232)

    // ── Accent gradient endpoints ──
    val accent = Color(0xFF00D2FF)            // cyan
    val accentGradientEnd = Color(0xFF7A5CFA) // purple
    val accentPink = Color(0xFFEB459E)        // Discord Fuchsia
    val accentGold = Color(0xFFF5C542)

    // ── Text hierarchy ──
    val textPrimary = Color(0xFFF2F3F5)       // near-white
    val textSecondary = Color(0xFFB5BAC1)     // gray-blue
    val textMuted = Color(0xFF6D6F78)         // dim gray
    val textLink = Color(0xFF00AFF4)          // link blue

    // ── Status ──
    val online = Color(0xFF23A55A)
    val idle = Color(0xFFF0B232)
    val dnd = Color(0xFFED4245)
    val offline = Color(0xFF80848E)
}

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.primary,
    secondary = AppColors.secondary,
    error = AppColors.error,
    background = AppColors.background,
    surface = AppColors.surface,
    surfaceVariant = AppColors.surfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color(0xFF003300),
    onBackground = AppColors.textPrimary,
    onSurface = AppColors.textPrimary,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.cardBorder,
    outlineVariant = AppColors.cardBorderFocused,
    primaryContainer = AppColors.primaryDim.copy(alpha = 0.25f),
    onPrimaryContainer = AppColors.textPrimary,
    secondaryContainer = AppColors.secondaryDim.copy(alpha = 0.2f),
    onSecondaryContainer = AppColors.textPrimary,
    tertiaryContainer = AppColors.warningDim.copy(alpha = 0.15f),
    onTertiaryContainer = AppColors.textPrimary,
    errorContainer = AppColors.error.copy(alpha = 0.2f),
    onErrorContainer = AppColors.textPrimary,
    inverseSurface = AppColors.textPrimary,
    inverseOnSurface = AppColors.background
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, color = AppColors.textPrimary),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = AppColors.textPrimary),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = AppColors.textPrimary),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.textPrimary),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = AppColors.textPrimary),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, color = AppColors.textPrimary),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, color = AppColors.textSecondary),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp, color = AppColors.textMuted),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppColors.textPrimary),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, color = AppColors.textSecondary),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp, color = AppColors.textMuted)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
