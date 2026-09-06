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

// Color scheme builders for the 8 distinct premium themes
fun buildThemeColorScheme(
    palette: ThemePalette,
    isDark: Boolean,
    isAmoled: Boolean = false,
    intensity: ColorIntensity = ColorIntensity.VIVID
): ColorScheme {
    // 1. Primary Colors & Containers per Theme & Intensity
    val (primary, primaryDark, primaryContainer, onPrimaryContainer) = when (palette) {
        ThemePalette.SOFT_PASTEL -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF8E67CE), Color(0xFF6A45AA), Color(0xFFF1E9FA), Color(0xFF432085))
            ColorIntensity.VIVID -> listOf(Color(0xFF7E57C2), Color(0xFF5E35B1), Color(0xFFEDE7F6), Color(0xFF311B92))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF5E35B1), Color(0xFF311B92), Color(0xFFD1C4E9), Color(0xFF1E0A60))
        }
        ThemePalette.PREMIUM_GREEN -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF16A34A), Color(0xFF15803D), Color(0xFFDCFCE7), Color(0xFF14532D))
            ColorIntensity.VIVID -> listOf(Color(0xFF15803D), Color(0xFF166534), Color(0xFFBBF7D0), Color(0xFF052E16))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF166534), Color(0xFF052E16), Color(0xFF86EFAC), Color(0xFF021C0D))
        }
        ThemePalette.ELEGANT_BLUE -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFFDBEAFE), Color(0xFF1E40AF))
            ColorIntensity.VIVID -> listOf(Color(0xFF1D4ED8), Color(0xFF1E40AF), Color(0xFFBFDBFE), Color(0xFF172554))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF1E40AF), Color(0xFF172554), Color(0xFF93C5FD), Color(0xFF0F172A))
        }
        ThemePalette.WARM_NEUTRAL -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFFD97706), Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFF78350F))
            ColorIntensity.VIVID -> listOf(Color(0xFFB45309), Color(0xFF92400E), Color(0xFFFDE68A), Color(0xFF451A03))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF92400E), Color(0xFF451A03), Color(0xFFFCD34D), Color(0xFF261001))
        }
        ThemePalette.MODERN_INDIGO -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFFE0E7FF), Color(0xFF312E81))
            ColorIntensity.VIVID -> listOf(Color(0xFF4F46E5), Color(0xFF4338CA), Color(0xFFC7D2FE), Color(0xFF1E1B4B))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF4338CA), Color(0xFF1E1B4B), Color(0xFFA5B4FC), Color(0xFF0F0E2A))
        }
        ThemePalette.CALM_SAGE -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF4B8B67), Color(0xFF3B7A57), Color(0xFFDDF0E6), Color(0xFF1B4D33))
            ColorIntensity.VIVID -> listOf(Color(0xFF3B7A57), Color(0xFF2E6546), Color(0xFFC3E6D2), Color(0xFF123B25))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF2E6546), Color(0xFF123B25), Color(0xFFA0D8B7), Color(0xFF071F13))
        }
        ThemePalette.SOPHISTICATED_DARK -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF3F3F46), Color(0xFF27272A), Color(0xFFE4E4E7), Color(0xFF18181B))
            ColorIntensity.VIVID -> listOf(Color(0xFF27272A), Color(0xFF18181B), Color(0xFFD4D4D8), Color(0xFF09090B))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF18181B), Color(0xFF09090B), Color(0xFFA1A1AA), Color(0xFF000000))
        }
        ThemePalette.MINIMAL_MONO -> when (intensity) {
            ColorIntensity.STANDARD -> listOf(Color(0xFF495057), Color(0xFF343A40), Color(0xFFE9ECEF), Color(0xFF212529))
            ColorIntensity.VIVID -> listOf(Color(0xFF343A40), Color(0xFF212529), Color(0xFFDEE2E6), Color(0xFF141619))
            ColorIntensity.DEEP_CONTRAST -> listOf(Color(0xFF212529), Color(0xFF000000), Color(0xFFCED4DA), Color(0xFF000000))
        }
    }

    // 2. Secondary & Accent Tones tailored per theme
    val (lightSecondary, lightSecondaryContainer, lightOnSecondaryContainer) = when (palette) {
        ThemePalette.SOFT_PASTEL -> listOf(Color(0xFF9575CD), Color(0xFFF3E5F5), Color(0xFF4A148C))
        ThemePalette.PREMIUM_GREEN -> listOf(Color(0xFF0D9488), Color(0xFFCCFBF1), Color(0xFF115E59))
        ThemePalette.ELEGANT_BLUE -> listOf(Color(0xFF0284C7), Color(0xFFE0F2FE), Color(0xFF0369A1))
        ThemePalette.WARM_NEUTRAL -> listOf(Color(0xFFC2410C), Color(0xFFFFEDD5), Color(0xFF9A3412))
        ThemePalette.MODERN_INDIGO -> listOf(Color(0xFF7C3AED), Color(0xFFEDE9FE), Color(0xFF5B21B6))
        ThemePalette.CALM_SAGE -> listOf(Color(0xFF52796F), Color(0xFFE0ECE7), Color(0xFF2F4F4F))
        ThemePalette.SOPHISTICATED_DARK -> listOf(Color(0xFF71717A), Color(0xFFF4F4F5), Color(0xFF27272A))
        ThemePalette.MINIMAL_MONO -> listOf(Color(0xFF6C757D), Color(0xFFF1F3F5), Color(0xFF343A40))
    }

    // 3. Theme-Specific Light Backgrounds & Surfaces
    val (lightBg, lightSurface, lightSurfaceVariant, lightOutline) = when (palette) {
        ThemePalette.SOFT_PASTEL -> listOf(Color(0xFFFBF9FE), Color(0xFFFFFFFF), Color(0xFFF4EFF9), Color(0xFFE0D4EC))
        ThemePalette.PREMIUM_GREEN -> listOf(Color(0xFFF5FAF7), Color(0xFFFFFFFF), Color(0xFFEBF4EE), Color(0xFFCDE5D6))
        ThemePalette.ELEGANT_BLUE -> listOf(Color(0xFFF6F9FD), Color(0xFFFFFFFF), Color(0xFFEDF3FA), Color(0xFFCBDFF4))
        ThemePalette.WARM_NEUTRAL -> listOf(Color(0xFFFAF7F2), Color(0xFFFFFFFF), Color(0xFFF3ECE2), Color(0xFFE5D8C5))
        ThemePalette.MODERN_INDIGO -> listOf(Color(0xFFF7F8FC), Color(0xFFFFFFFF), Color(0xFFEFF1F9), Color(0xFFD2D9EE))
        ThemePalette.CALM_SAGE -> listOf(Color(0xFFF5F8F6), Color(0xFFFFFFFF), Color(0xFFEBF2EE), Color(0xFFCADCD2))
        ThemePalette.SOPHISTICATED_DARK -> listOf(Color(0xFFF4F5F7), Color(0xFFFFFFFF), Color(0xFFE9EBEF), Color(0xFFD4D4D8))
        ThemePalette.MINIMAL_MONO -> listOf(Color(0xFFF8F9FA), Color(0xFFFFFFFF), Color(0xFFF1F3F5), Color(0xFFCED4DA))
    }

    // 4. Theme-Specific Dark Backgrounds & Surfaces
    val (darkBg, darkSurface, darkSurfaceVariant, darkOutline) = when (palette) {
        ThemePalette.SOFT_PASTEL -> listOf(Color(0xFF14101D), Color(0xFF1D1728), Color(0xFF282136), Color(0xFF423854))
        ThemePalette.PREMIUM_GREEN -> listOf(Color(0xFF0B1610), Color(0xFF13261D), Color(0xFF1C362A), Color(0xFF2E5241))
        ThemePalette.ELEGANT_BLUE -> listOf(Color(0xFF0A1124), Color(0xFF121E3B), Color(0xFF1A2C54), Color(0xFF2C467E))
        ThemePalette.WARM_NEUTRAL -> listOf(Color(0xFF17130F), Color(0xFF241E18), Color(0xFF332A22), Color(0xFF524335))
        ThemePalette.MODERN_INDIGO -> listOf(Color(0xFF0C1021), Color(0xFF161C36), Color(0xFF20294D), Color(0xFF354275))
        ThemePalette.CALM_SAGE -> listOf(Color(0xFF0E1713), Color(0xFF182721), Color(0xFF243830), Color(0xFF355347))
        ThemePalette.SOPHISTICATED_DARK -> listOf(Color(0xFF090A0D), Color(0xFF13151A), Color(0xFF1E222A), Color(0xFF323846))
        ThemePalette.MINIMAL_MONO -> listOf(Color(0xFF111215), Color(0xFF1A1D21), Color(0xFF262A30), Color(0xFF3D434D))
    }

    return if (isDark) {
        if (isAmoled) {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primaryDark,
                onPrimaryContainer = Color.White,
                secondary = lightSecondary,
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
                secondary = lightSecondary,
                onSecondary = Color.White,
                secondaryContainer = darkSurfaceVariant,
                onSecondaryContainer = Color.White,
                tertiary = SolidIncome,
                background = darkBg,
                onBackground = Color(0xFFF8FAFC),
                surface = darkSurface,
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = darkSurfaceVariant,
                onSurfaceVariant = Color(0xFFE2E8F0),
                outline = darkOutline,
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
            secondary = lightSecondary,
            onSecondary = Color.White,
            secondaryContainer = lightSecondaryContainer,
            onSecondaryContainer = lightOnSecondaryContainer,
            tertiary = SolidIncome,
            background = lightBg,
            onBackground = SolidLightTextPrimary,
            surface = lightSurface,
            onSurface = SolidLightTextPrimary,
            surfaceVariant = lightSurfaceVariant,
            onSurfaceVariant = Color(0xFF334155),
            outline = lightOutline,
            error = SolidExpense,
            onError = Color.White,
            errorContainer = SolidExpenseContainer,
            onErrorContainer = SolidOnExpenseContainer
        )
    }
}
