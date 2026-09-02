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
    isAmoled: Boolean = false
): ColorScheme {
    val (primary, primaryDark, primaryContainer, onPrimaryContainer) = when (palette) {
        ThemePalette.EMERALD -> listOf(Color(0xFF059669), Color(0xFF047857), Color(0xFFD1FAE5), Color(0xFF065F46))
        ThemePalette.SAPPHIRE -> listOf(Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFFDBEAFE), Color(0xFF1E40AF))
        ThemePalette.AMETHYST -> listOf(Color(0xFF7C3AED), Color(0xFF6D28D9), Color(0xFFEDE9FE), Color(0xFF5B21B6))
        ThemePalette.GOLDEN -> listOf(Color(0xFFD97706), Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFF92400E))
        ThemePalette.CRIMSON -> listOf(Color(0xFFDC2626), Color(0xFFB91C1C), Color(0xFFFEE2E2), Color(0xFF991B1B))
        ThemePalette.TEAL -> listOf(Color(0xFF0D9488), Color(0xFF0F766E), Color(0xFFCCFBF1), Color(0xFF115E59))
        ThemePalette.SUNSET -> listOf(Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFFFFEDD5), Color(0xFF9A3412))
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
                onSurfaceVariant = Color(0xFFA1A1AA),
                outline = SolidAmoledBorder,
                error = SolidExpense,
                onError = Color.White,
                errorContainer = Color(0xFF450A0A),
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
                onBackground = SolidDarkTextPrimary,
                surface = SolidDarkSurface,
                onSurface = SolidDarkTextPrimary,
                surfaceVariant = SolidDarkSurfaceVariant,
                onSurfaceVariant = SolidDarkTextSecondary,
                outline = SolidDarkBorder,
                error = SolidExpense,
                onError = Color.White,
                errorContainer = SolidExpenseContainer,
                onErrorContainer = SolidOnExpenseContainer
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
            onSurfaceVariant = SolidLightTextSecondary,
            outline = SolidLightBorder,
            error = SolidExpense,
            onError = Color.White,
            errorContainer = SolidExpenseContainer,
            onErrorContainer = SolidOnExpenseContainer
        )
    }
}
