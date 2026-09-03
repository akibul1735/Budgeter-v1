package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Default Brand Constants
val SolidPrimary = Color(0xFF2563EB)
val SolidPrimaryDark = Color(0xFF1D4ED8)
val SolidPrimaryContainer = Color(0xFFDBEAFE)
val SolidOnPrimaryContainer = Color(0xFF1E40AF)

// Financial Semantic Indicators
val SolidIncome = Color(0xFF10B981)         // Solid Emerald Green
val SolidIncomeDark = Color(0xFF047857)
val SolidIncomeContainer = Color(0xFFD1FAE5)
val SolidOnIncomeContainer = Color(0xFF065F46)

val SolidExpense = Color(0xFFEF4444)        // Solid Crimson Red
val SolidExpenseDark = Color(0xFFB91C1C)
val SolidExpenseContainer = Color(0xFFFEE2E2)
val SolidOnExpenseContainer = Color(0xFF991B1B)

val SolidTransfer = Color(0xFF0284C7)       // Solid Sky Blue
val SolidTransferContainer = Color(0xFFE0F2FE)

val SolidEquity = Color(0xFF8B5CF6)         // Solid Violet
val SolidEquityContainer = Color(0xFFEDE9FE)
val SolidAmber = Color(0xFFF59E0B)

// Neutrals: Light
val SolidLightBg = Color(0xFFF8FAFC)
val SolidLightSurface = Color(0xFFFFFFFF)
val SolidLightSurfaceVariant = Color(0xFFF1F5F9)
val SolidLightTextPrimary = Color(0xFF0F172A)
val SolidLightTextSecondary = Color(0xFF64748B)
val SolidLightBorder = Color(0xFFE2E8F0)

// Neutrals: Dark
val SolidDarkBg = Color(0xFF0F172A)
val SolidDarkSurface = Color(0xFF1E293B)
val SolidDarkSurfaceVariant = Color(0xFF334155)
val SolidDarkTextPrimary = Color(0xFFF8FAFC)
val SolidDarkTextSecondary = Color(0xFF94A3B8)
val SolidDarkBorder = Color(0xFF475569)

// Neutrals: AMOLED Night
val SolidAmoledBg = Color(0xFF000000)
val SolidAmoledSurface = Color(0xFF0D0D0D)
val SolidAmoledSurfaceVariant = Color(0xFF1A1A1A)
val SolidAmoledBorder = Color(0xFF2B2B2B)

// Color scheme builders for the 7 themes
fun buildThemeColorScheme(
    palette: ThemePalette,
    isDark: Boolean,
    isAmoled: Boolean = false,
    intensity: ColorIntensity = ColorIntensity.VIVID
): ColorScheme {
    val (primary, primaryDark, primaryContainer, onPrimaryContainer) = when (palette) {
        ThemePalette.EMERALD -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF059669), Color(0xFF047857), Color(0xFFD1FAE5), Color(0xFF065F46))
            ColorIntensity.VIVID -> listOf(Color(0xFF047857), Color(0xFF065F46), Color(0xFFA7F3D0), Color(0xFF022C22))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF065F46), Color(0xFF022C22), Color(0xFF6EE7B7), Color(0xFF022C22))
        }
        ThemePalette.SAPPHIRE -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFFDBEAFE), Color(0xFF1E40AF))
            ColorIntensity.VIVID -> listOf(Color(0xFF1D4ED8), Color(0xFF1E40AF), Color(0xFFBFDBFE), Color(0xFF172554))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF1E40AF), Color(0xFF172554), Color(0xFF93C5FD), Color(0xFF0F172A))
        }
        ThemePalette.AMETHYST -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF7C3AED), Color(0xFF6D28D9), Color(0xFFEDE9FE), Color(0xFF5B21B6))
            ColorIntensity.VIVID -> listOf(Color(0xFF6D28D9), Color(0xFF5B21B6), Color(0xFFDDD6FE), Color(0xFF2E1065))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF5B21B6), Color(0xFF2E1065), Color(0xFFC4B5FD), Color(0xFF1E1B4B))
        }
        ThemePalette.GOLDEN -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFFD97706), Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFF92400E))
            ColorIntensity.VIVID -> listOf(Color(0xFFB45309), Color(0xFF92400E), Color(0xFFFDE68A), Color(0xFF451A03))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF92400E), Color(0xFF451A03), Color(0xFFFCD34D), Color(0xFF261001))
        }
        ThemePalette.CRIMSON -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFFDC2626), Color(0xFFB91C1C), Color(0xFFFEE2E2), Color(0xFF991B1B))
            ColorIntensity.VIVID -> listOf(Color(0xFFB91C1C), Color(0xFF991B1B), Color(0xFFFECACA), Color(0xFF450A0A))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF991B1B), Color(0xFF450A0A), Color(0xFFFCA5A5), Color(0xFF2A0404))
        }
        ThemePalette.TEAL -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF0D9488), Color(0xFF0F766E), Color(0xFFCCFBF1), Color(0xFF115E59))
            ColorIntensity.VIVID -> listOf(Color(0xFF0F766E), Color(0xFF115E59), Color(0xFF99F6E4), Color(0xFF042F2E))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF115E59), Color(0xFF042F2E), Color(0xFF5EEAD4), Color(0xFF021C1C))
        }
        ThemePalette.SUNSET -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFFFFEDD5), Color(0xFF9A3412))
            ColorIntensity.VIVID -> listOf(Color(0xFFC2410C), Color(0xFF9A3412), Color(0xFFFED7AA), Color(0xFF431407))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF9A3412), Color(0xFF431407), Color(0xFFFDBA74), Color(0xFF270903))
        }
    }

    return if (isDark) {
        if (isAmoled) {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primaryDark,
                onPrimaryContainer = Color.White,
                secondary = SolidTransfer,
                onSecondary = Color.White,
                secondaryContainer = SolidAmoledSurfaceVariant,
                onSecondaryContainer = Color.White,
                tertiary = SolidIncome,
                background = SolidAmoledBg,
                onBackground = Color.White,
                surface = SolidAmoledSurface,
                onSurface = Color.White,
                surfaceVariant = SolidAmoledSurfaceVariant,
                onSurfaceVariant = Color(0xFFE4E4E7),
                outline = Color(0xFF52525B),
                error = SolidExpense,
                onError = Color.White,
                errorContainer = Color(0xFF7F1D1D),
                onErrorContainer = Color(0xFFFCA5A5)
            )
        } else {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primaryDark,
                onPrimaryContainer = Color.White,
                secondary = SolidTransfer,
                onSecondary = Color.White,
                secondaryContainer = SolidDarkSurfaceVariant,
                onSecondaryContainer = Color.White,
                tertiary = SolidIncome,
                background = SolidDarkBg,
                onBackground = Color.White,
                surface = SolidDarkSurface,
                onSurface = Color.White,
                surfaceVariant = SolidDarkSurfaceVariant,
                onSurfaceVariant = Color(0xFFE2E8F0),
                outline = Color(0xFF64748B),
                error = SolidExpense,
                onError = Color.White,
                errorContainer = Color(0xFF7F1D1D),
                onErrorContainer = Color(0xFFFCA5A5)
            )
        }
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = SolidTransfer,
            onSecondary = Color.White,
            secondaryContainer = SolidTransferContainer,
            onSecondaryContainer = Color(0xFF0369A1),
            tertiary = SolidIncome,
            background = SolidLightBg,
            onBackground = SolidLightTextPrimary,
            surface = SolidLightSurface,
            onSurface = SolidLightTextPrimary,
            surfaceVariant = SolidLightSurfaceVariant,
            onSurfaceVariant = Color(0xFF334155),
            outline = Color(0xFFCBD5E1),
            error = SolidExpense,
            onError = Color.White,
            errorContainer = SolidExpenseContainer,
            onErrorContainer = SolidOnExpenseContainer
        )
    }
}
