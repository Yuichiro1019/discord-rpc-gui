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

    // ── Handcrafted Presets (from Stitch V3 design) ──

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
        secondary = Color(0xFF5BA8F5),       // Complementary: Cool blue to contrast amber
        secondaryContainer = Color(0xFF1A3A5C),
        tertiary = Color(0xFFFF8A80),         // Analogous: Coral/rose warmth
        tertiaryContainer = Color(0xFF5C2020),
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
        secondary = Color(0xFF34D399),         // Complementary: Emerald green
        secondaryContainer = Color(0xFF065F46),
        tertiary = Color(0xFFF472B6),          // Analogous: Pink
        tertiaryContainer = Color(0xFF5C1A3A),
        textPrimary = Color(0xFFFAFAFA),
        textSecondary = Color(0xFFA1A1AA),
        textMuted = Color(0xFF71717A)
    )

    val DiscordBlurple = ThemeColors(
        background = Color(0xFF0C0C14),
        surfaceContainerLowest = Color(0xFF08080F),
        surfaceContainerLow = Color(0xFF101018),
        surfaceContainer = Color(0xFF14141E),
        surfaceContainerHigh = Color(0xFF1A1A26),
        surfaceContainerHighest = Color(0xFF22222E),
        outline = Color(0xFF4A4A6A),
        outlineVariant = Color(0xFF2A2A3E),
        primary = Color(0xFF5865F2),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF3B44B0),
        onPrimaryContainer = Color(0xFFCDD0FF),
        secondary = Color(0xFFF2A858),         // Complementary: Warm orange
        secondaryContainer = Color(0xFF5C3A10),
        tertiary = Color(0xFF58F2C8),          // Analogous: Teal
        tertiaryContainer = Color(0xFF105C44),
        textPrimary = Color(0xFFE8E8F0),
        textSecondary = Color(0xFFA0A0B8),
        textMuted = Color(0xFF686880)
    )

    val NeonCyber = ThemeColors(
        background = Color(0xFF050A0C),
        surfaceContainerLowest = Color(0xFF030608),
        surfaceContainerLow = Color(0xFF0A0F12),
        surfaceContainer = Color(0xFF0E1418),
        surfaceContainerHigh = Color(0xFF141C22),
        surfaceContainerHighest = Color(0xFF1A242C),
        outline = Color(0xFF3A5A6A),
        outlineVariant = Color(0xFF1A2A34),
        primary = Color(0xFF00E5FF),
        onPrimary = Color(0xFF001A1F),
        primaryContainer = Color(0xFF00889A),
        onPrimaryContainer = Color(0xFFB0F4FF),
        secondary = Color(0xFFFF6090),         // Complementary: Hot pink
        secondaryContainer = Color(0xFF5C1A2A),
        tertiary = Color(0xFFB0FF60),          // Analogous: Lime green
        tertiaryContainer = Color(0xFF2A5C10),
        textPrimary = Color(0xFFE0F4F8),
        textSecondary = Color(0xFF90C0D0),
        textMuted = Color(0xFF507080)
    )

    val CrimsonForge = ThemeColors(
        background = Color(0xFF120808),
        surfaceContainerLowest = Color(0xFF0E0505),
        surfaceContainerLow = Color(0xFF180C0C),
        surfaceContainer = Color(0xFF1E1010),
        surfaceContainerHigh = Color(0xFF261616),
        surfaceContainerHighest = Color(0xFF301E1E),
        outline = Color(0xFF7A4A4A),
        outlineVariant = Color(0xFF442A2A),
        primary = Color(0xFFDC2626),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF991B1B),
        onPrimaryContainer = Color(0xFFFFDADA),
        secondary = Color(0xFF26DCC8),         // Complementary: Teal
        secondaryContainer = Color(0xFF105C52),
        tertiary = Color(0xFFDC8C26),          // Analogous: Orange/gold
        tertiaryContainer = Color(0xFF5C3A10),
        textPrimary = Color(0xFFF0E0E0),
        textSecondary = Color(0xFFC0A0A0),
        textMuted = Color(0xFF806060)
    )

    // ── Dynamic Generation (for Custom Hex) ──

    fun generateTheme(seed: Color, isCyber: Boolean = false): ThemeColors {
        val (h, s, l) = rgbToHsl(seed.red, seed.green, seed.blue)
        
        val bgL = if (isCyber) 0.02f else 0.05f
        val bgS = min(s, 0.15f)

        // Split-Complementary Color Theory
        val hComp = (h + 180f) % 360f    // Opposite on color wheel
        val hAnalog = (h + 45f) % 360f   // 45° shift for analogous warmth

        return ThemeColors(
            background = hslToColor(h, bgS, bgL),
            surfaceContainerLowest = hslToColor(h, bgS, max(0.01f, bgL - 0.02f)),
            surfaceContainerLow = hslToColor(h, bgS, bgL + 0.02f),
            surfaceContainer = hslToColor(h, bgS, bgL + 0.04f),
            surfaceContainerHigh = hslToColor(h, bgS, bgL + 0.07f),
            surfaceContainerHighest = hslToColor(h, bgS, bgL + 0.10f),

            outline = hslToColor(h, min(s, 0.3f), 0.4f),
            outlineVariant = hslToColor(h, min(s, 0.2f), 0.2f),

            primary = seed,
            onPrimary = if (l > 0.5f) hslToColor(h, s, 0.1f) else Color.White,
            primaryContainer = hslToColor(h, s, max(0.1f, l - 0.2f)),
            onPrimaryContainer = hslToColor(h, s, min(0.9f, l + 0.3f)),

            // Secondary: Complementary hue (180° shift) — max contrast pop
            secondary = hslToColor(hComp, min(s * 0.9f, 0.8f), min(l + 0.1f, 0.7f)),
            secondaryContainer = hslToColor(hComp, min(s * 0.7f, 0.6f), 0.15f),

            // Tertiary: Analogous hue (45° shift) — related but distinct
            tertiary = hslToColor(hAnalog, min(s * 0.85f, 0.8f), min(l + 0.1f, 0.7f)),
            tertiaryContainer = hslToColor(hAnalog, min(s * 0.6f, 0.5f), 0.15f),

            textPrimary = hslToColor(h, min(s, 0.15f), 0.93f),
            textSecondary = hslToColor(h, min(s, 0.15f), 0.72f),
            textMuted = hslToColor(h, min(s, 0.1f), 0.5f)
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
        val clampedL = l.coerceIn(0f, 1f)
        val clampedS = s.coerceIn(0f, 1f)
        val hue = h % 360f
        val q = if (clampedL < 0.5f) clampedL * (1f + clampedS) else clampedL + clampedS - clampedL * clampedS
        val p = 2f * clampedL - q

        val r = hueToRgb(p, q, hue / 360f + 1f / 3f)
        val g = hueToRgb(p, q, hue / 360f)
        val b = hueToRgb(p, q, hue / 360f - 1f / 3f)

        return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), 1f)
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
