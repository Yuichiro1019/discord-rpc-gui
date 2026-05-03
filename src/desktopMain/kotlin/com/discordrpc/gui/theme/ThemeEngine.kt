package com.discordrpc.gui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * A data class holding our dynamic palette.
 */
data class ThemeColors(
    val background: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    
    val outline: Color,
    val outlineVariant: Color,

    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,

    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,

    val online: Color = Color(0xFF34D399),
    val idle: Color = Color(0xFFFBBF24),
    val error: Color = Color(0xFFFFB4AB),
    val errorDim: Color = Color(0xFF93000A),
    val offline: Color = Color(0xFF686C78),
    val warning: Color = Color(0xFFFBBF24),
    val warningDim: Color = Color(0xFFD97706)
)

object ThemeEngine {
    // ── Built-in Presets ──

    val PlushAmber = ThemeColors(
        background = Color(0xFF161311),
        surfaceContainerLowest = Color(0xFF100E0C),
        surfaceContainerLow = Color(0xFF1E1B19),
        surfaceContainer = Color(0xFF221F1D),
        surfaceContainerHigh = Color(0xFF2D2927),
        surfaceContainerHighest = Color(0xFF383432),
        outline = Color(0xFFA08E7A),
        outlineVariant = Color(0xFF534434),
        primary = Color(0xFFFFC174),
        onPrimary = Color(0xFF472A00),
        primaryContainer = Color(0xFFF59E0B),
        onPrimaryContainer = Color(0xFF613B00),
        secondary = Color(0xFFFFB693),
        secondaryContainer = Color(0xFF76330D),
        tertiary = Color(0xFFFFBEA1),
        tertiaryContainer = Color(0xFFE9A07E),
        textPrimary = Color(0xFFE9E1DD),
        textSecondary = Color(0xFFD8C3AD),
        textMuted = Color(0xFFA08E7A)
    )

    val ObsidianViolet = ThemeColors(
        background = Color(0xFF09090B),
        surfaceContainerLowest = Color(0xFF09090B),
        surfaceContainerLow = Color(0xFF0F0F12),
        surfaceContainer = Color(0xFF121215),
        surfaceContainerHigh = Color(0xFF18181B),
        surfaceContainerHighest = Color(0xFF1E1E22),
        outline = Color(0xFF52525B),
        outlineVariant = Color(0xFF27272A),
        primary = Color(0xFFA78BFA),
        onPrimary = Color(0xFF0A0012),
        primaryContainer = Color(0xFF7C3AED),
        onPrimaryContainer = Color(0xFFEDE9FE),
        secondary = Color(0xFF71717A),
        secondaryContainer = Color(0xFF27272A),
        tertiary = Color(0xFF34D399),
        tertiaryContainer = Color(0xFF065F46),
        textPrimary = Color(0xFFFAFAFA),
        textSecondary = Color(0xFFA1A1AA),
        textMuted = Color(0xFF71717A)
    )

    val DiscordBlurple = generateTheme(Color(0xFF5865F2))
    val NeonCyber = generateTheme(Color(0xFF00E5FF), isCyber = true)
    val CrimsonForge = generateTheme(Color(0xFFDC2626))

    // ── Dynamic Generation ──

    fun generateTheme(seed: Color, isCyber: Boolean = false): ThemeColors {
        val (h, s, l) = rgbToHsl(seed.red, seed.green, seed.blue)
        
        // Backgrounds: Tinted very dark with the seed hue. 
        // If cyber, we might use darker pure blacks.
        val bgL = if (isCyber) 0.02f else 0.05f
        val bgS = min(s, 0.15f)

        return ThemeColors(
            background = hslToColor(h, bgS, bgL),
            surfaceContainerLowest = hslToColor(h, bgS, bgL - 0.02f),
            surfaceContainerLow = hslToColor(h, bgS, bgL + 0.02f),
            surfaceContainer = hslToColor(h, bgS, bgL + 0.04f),
            surfaceContainerHigh = hslToColor(h, bgS, bgL + 0.07f),
            surfaceContainerHighest = hslToColor(h, bgS, bgL + 0.10f),

            outline = hslToColor(h, min(s, 0.3f), 0.4f),
            outlineVariant = hslToColor(h, min(s, 0.2f), 0.2f),

            primary = seed,
            onPrimary = hslToColor(h, s, 0.1f),
            primaryContainer = hslToColor(h, s, max(0.1f, l - 0.2f)),
            onPrimaryContainer = hslToColor(h, s, min(0.9f, l + 0.3f)),

            // Secondary: Shift hue by 30 degrees, slightly desaturated
            secondary = hslToColor((h + 30f) % 360f, s * 0.8f, l),
            secondaryContainer = hslToColor((h + 30f) % 360f, s * 0.8f, max(0.1f, l - 0.3f)),

            // Tertiary: Shift hue backwards by 30 degrees
            tertiary = hslToColor((h + 330f) % 360f, s * 0.8f, l),
            tertiaryContainer = hslToColor((h + 330f) % 360f, s * 0.8f, max(0.1f, l - 0.3f)),

            textPrimary = hslToColor(h, min(s, 0.2f), 0.95f),
            textSecondary = hslToColor(h, min(s, 0.2f), 0.75f),
            textMuted = hslToColor(h, min(s, 0.15f), 0.55f)
        )
    }

    private fun rgbToHsl(r: Float, g: Float, b: Float): FloatArray {
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        var h = 0f
        var s = 0f
        val l = (max + min) / 2f

        if (max != min) {
            val d = max - min
            s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
            h = when (max) {
                r -> (g - b) / d + (if (g < b) 6f else 0f)
                g -> (b - r) / d + 2f
                b -> (r - g) / d + 4f
                else -> 0f
            }
            h /= 6f
        }
        return floatArrayOf(h * 360f, s, l)
    }

    private fun hslToColor(h: Float, s: Float, l: Float): Color {
        val hue = h % 360f
        val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
        val p = 2f * l - q

        val r = hueToRgb(p, q, hue / 360f + 1f / 3f)
        val g = hueToRgb(p, q, hue / 360f)
        val b = hueToRgb(p, q, hue / 360f - 1f / 3f)

        return Color(r, g, b, 1f)
    }

    private fun hueToRgb(p: Float, q: Float, t: Float): Float {
        var tAdj = t
        if (tAdj < 0f) tAdj += 1f
        if (tAdj > 1f) tAdj -= 1f
        if (tAdj < 1f / 6f) return p + (q - p) * 6f * tAdj
        if (tAdj < 1f / 2f) return q
        if (tAdj < 2f / 3f) return p + (q - p) * (2f / 3f - tAdj) * 6f
        return p
    }
}
