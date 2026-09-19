package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Default Brand Constants - Luxury Fintech Baseline
val SolidPrimary = Color(0xFF059669)         // Rich Emerald Wealth
val SolidPrimaryDark = Color(0xFF10B981)
val SolidPrimaryContainer = Color(0xFFE6F7F0)
val SolidOnPrimaryContainer = Color(0xFF064E3B)

// Financial Semantic Indicators (Default)
val SolidIncome = Color(0xFF059669)          // Rich Emerald
val SolidIncomeDark = Color(0xFF10B981)
val SolidIncomeContainer = Color(0xFFE6F7F0)
val SolidOnIncomeContainer = Color(0xFF064E3B)

val SolidExpense = Color(0xFFE11D48)         // Luxury Rose Crimson
val SolidExpenseDark = Color(0xFFFB7185)
val SolidExpenseContainer = Color(0xFFFFE4E6)
val SolidOnExpenseContainer = Color(0xFF881337)

val SolidTransfer = Color(0xFF0284C7)        // Pure Sky Azure
val SolidTransferContainer = Color(0xFFE0F2FE)

val SolidEquity = Color(0xFF7C3AED)          // Imperial Royal Violet
val SolidEquityContainer = Color(0xFFEDE9FE)
val SolidAmber = Color(0xFFD97706)           // Warm Amber Gold

// Neutrals: Light (High-End Solid Canvas)
val SolidLightBg = Color(0xFFF8FAFC)
val SolidLightSurface = Color(0xFFFFFFFF)
val SolidLightSurfaceVariant = Color(0xFFF1F5F9)
val SolidLightTextPrimary = Color(0xFF0F172A)
val SolidLightTextSecondary = Color(0xFF475569)
val SolidLightBorder = Color(0xFFE2E8F0)

// Neutrals: Dark (Obsidian Luxury - Slate 900 canvas, soothing & balanced)
val SolidDarkBg = Color(0xFF0A0D14)
val SolidDarkSurface = Color(0xFF121722)
val SolidDarkSurfaceVariant = Color(0xFF1B2232)
val SolidDarkTextPrimary = Color(0xFFF8FAFC)
val SolidDarkTextSecondary = Color(0xFF94A3B8)
val SolidDarkBorder = Color(0xFF263246)

// Neutrals: AMOLED Night (Pure Pitch)
val SolidAmoledBg = Color(0xFF000000)
val SolidAmoledSurface = Color(0xFF0D0E12)
val SolidAmoledSurfaceVariant = Color(0xFF161820)
val SolidAmoledBorder = Color(0xFF22242C)

/**
 * Color scheme builder for the 5 standard themes (3 Day, 2 Night)
 */
fun buildThemeColorScheme(
    palette: ThemePalette,
    isDark: Boolean,
    isAmoled: Boolean = false,
    intensity: ColorIntensity = ColorIntensity.STANDARD,
    semanticPalette: FinancialSemanticPalette = FinancialSemanticPalette.CLASSIC,
    darkSurfaceTone: DarkSurfaceTone = DarkSurfaceTone.SLATE_BLUE
): ColorScheme {
    val incomeColor = semanticPalette.incomeColor
    val expenseColor = semanticPalette.expenseColor

    return if (isDark) {
        // Dark Mode Primary - Clean, luminous, soothing without glaring neon saturation
        val darkPrimary = when (palette) {
            ThemePalette.EMERALD_WEALTH -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFA7F3D0)
                ColorIntensity.STANDARD -> Color(0xFF34D399)
                ColorIntensity.VIVID -> Color(0xFF10B981)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF059669)
            }
            ThemePalette.ROYAL_SAPPHIRE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF93C5FD)
                ColorIntensity.STANDARD -> Color(0xFF60A5FA)
                ColorIntensity.VIVID -> Color(0xFF3B82F6)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF2563EB)
            }
            ThemePalette.WARM_AMBER -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFFDE68A)
                ColorIntensity.STANDARD -> Color(0xFFFBBF24)
                ColorIntensity.VIVID -> Color(0xFFF59E0B)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFD97706)
            }
            ThemePalette.MIDNIGHT_SLATE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFBAE6FD)
                ColorIntensity.STANDARD -> Color(0xFF38BDF8)
                ColorIntensity.VIVID -> Color(0xFF0EA5E9)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF38BDF8)
            }
            ThemePalette.OBSIDIAN_NIGHT -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFA7F3D0)
                ColorIntensity.STANDARD -> Color(0xFF34D399)
                ColorIntensity.VIVID -> Color(0xFF10B981)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF34D399)
            }
        }

        // Dark Containers - Subdued, soothing dark tones to prevent eye strain or over-vibrancy
        val darkPrimaryContainer = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFF0D281E)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFF0E2248)
            ThemePalette.WARM_AMBER -> Color(0xFF2E1C0A)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFF10283B)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFF0D1F17)
        }

        val darkOnPrimaryContainer = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFFA7F3D0)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFFBFDBFE)
            ThemePalette.WARM_AMBER -> Color(0xFFFDE68A)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFFBAE6FD)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFFA7F3D0)
        }

        val darkSecondary = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFF2DD4BF)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFF38BDF8)
            ThemePalette.WARM_AMBER -> Color(0xFFF97316)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFF818CF8)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFF2DD4BF)
        }

        val darkSecondaryContainer = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFF0F2D29)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFF0B2B3F)
            ThemePalette.WARM_AMBER -> Color(0xFF2B160B)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFF1E1E3F)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFF112620)
        }

        val darkBg = if (isAmoled || palette == ThemePalette.OBSIDIAN_NIGHT) SolidAmoledBg else darkSurfaceTone.darkBg
        val darkSurface = if (isAmoled || palette == ThemePalette.OBSIDIAN_NIGHT) SolidAmoledSurface else darkSurfaceTone.darkSurface
        val darkSurfaceVariant = if (isAmoled || palette == ThemePalette.OBSIDIAN_NIGHT) SolidAmoledSurfaceVariant else darkSurfaceTone.darkSurfaceVariant

        darkColorScheme(
            primary = darkPrimary,
            onPrimary = Color(0xFF000000),
            primaryContainer = darkPrimaryContainer,
            onPrimaryContainer = darkOnPrimaryContainer,
            secondary = darkSecondary,
            onSecondary = Color(0xFF000000),
            secondaryContainer = darkSecondaryContainer,
            onSecondaryContainer = darkOnPrimaryContainer,
            tertiary = incomeColor,
            onTertiary = Color(0xFF052E16),
            background = darkBg,
            onBackground = Color(0xFFF8FAFC),
            surface = darkSurface,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = darkSurfaceVariant,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF3B4861),
            outlineVariant = Color(0xFF1F2937),
            error = expenseColor,
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFF4C0519),
            onErrorContainer = Color(0xFFFFE4E6)
        )
    } else {
        // Light Mode Colors - Deep, rich, luxurious, crystal clear
        val lightPrimary = when (palette) {
            ThemePalette.EMERALD_WEALTH -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF16A34A)
                ColorIntensity.STANDARD -> Color(0xFF059669)
                ColorIntensity.VIVID -> Color(0xFF047857)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF064E3B)
            }
            ThemePalette.ROYAL_SAPPHIRE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF2563EB)
                ColorIntensity.STANDARD -> Color(0xFF1D4ED8)
                ColorIntensity.VIVID -> Color(0xFF1E40AF)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF172554)
            }
            ThemePalette.WARM_AMBER -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFD97706)
                ColorIntensity.STANDARD -> Color(0xFFB45309)
                ColorIntensity.VIVID -> Color(0xFF92400E)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF78350F)
            }
            ThemePalette.MIDNIGHT_SLATE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF0284C7)
                ColorIntensity.STANDARD -> Color(0xFF0369A1)
                ColorIntensity.VIVID -> Color(0xFF075985)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF0C4A6E)
            }
            ThemePalette.OBSIDIAN_NIGHT -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF16A34A)
                ColorIntensity.STANDARD -> Color(0xFF059669)
                ColorIntensity.VIVID -> Color(0xFF047857)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF064E3B)
            }
        }

        val lightPrimaryContainer = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFFE6F7F0)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFFEFF6FF)
            ThemePalette.WARM_AMBER -> Color(0xFFFFFBEB)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFFF0F9FF)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFFE6F7F0)
        }

        val lightOnPrimaryContainer = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFF064E3B)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFF1E3A8A)
            ThemePalette.WARM_AMBER -> Color(0xFF78350F)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFF0C4A6E)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFF064E3B)
        }

        val lightSecondary = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFF0D9488)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFF0284C7)
            ThemePalette.WARM_AMBER -> Color(0xFFD97706)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFF4F46E5)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFF0D9488)
        }

        val lightSecondaryContainer = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFFCCFBF1)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFFE0F2FE)
            ThemePalette.WARM_AMBER -> Color(0xFFFEF3C7)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFFEEF2FF)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFFCCFBF1)
        }

        val lightOnSecondaryContainer = when (palette) {
            ThemePalette.EMERALD_WEALTH -> Color(0xFF115E59)
            ThemePalette.ROYAL_SAPPHIRE -> Color(0xFF075985)
            ThemePalette.WARM_AMBER -> Color(0xFF7C2D12)
            ThemePalette.MIDNIGHT_SLATE -> Color(0xFF3730A3)
            ThemePalette.OBSIDIAN_NIGHT -> Color(0xFF115E59)
        }

        val lightBg = Color(0xFFF8FAFC)
        val lightSurface = Color(0xFFFFFFFF)
        val lightSurfaceVariant = Color(0xFFF1F5F9)
        val lightOutline = Color(0xFF64748B)
        val lightOutlineVariant = Color(0xFFE2E8F0)

        lightColorScheme(
            primary = lightPrimary,
            onPrimary = Color.White,
            primaryContainer = lightPrimaryContainer,
            onPrimaryContainer = lightOnPrimaryContainer,
            secondary = lightSecondary,
            onSecondary = Color.White,
            secondaryContainer = lightSecondaryContainer,
            onSecondaryContainer = lightOnSecondaryContainer,
            tertiary = incomeColor,
            onTertiary = Color.White,
            background = lightBg,
            onBackground = Color(0xFF0F172A),
            surface = lightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = lightSurfaceVariant,
            onSurfaceVariant = Color(0xFF475569),
            outline = lightOutline,
            outlineVariant = lightOutlineVariant,
            error = expenseColor,
            onError = Color.White,
            errorContainer = SolidExpenseContainer,
            onErrorContainer = SolidOnExpenseContainer
        )
    }
}

fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Builds custom theme color scheme with support for area customization (primary, secondary,
 * surface, header, income, expense, transfer) and shade intensity adjustments.
 */
fun buildCustomThemeColorScheme(
    customTheme: CustomTheme,
    isDark: Boolean,
    isAmoled: Boolean = false,
    intensity: ColorIntensity = ColorIntensity.STANDARD,
    semanticPalette: FinancialSemanticPalette = FinancialSemanticPalette.CLASSIC,
    darkSurfaceTone: DarkSurfaceTone = DarkSurfaceTone.SLATE_BLUE
): ColorScheme {
    val basePrimary = customTheme.primaryColor
    val baseSecondary = customTheme.secondaryColor
    val incomeColor = customTheme.customIncomeColor ?: semanticPalette.incomeColor
    val expenseColor = customTheme.customExpenseColor ?: semanticPalette.expenseColor
    val customSurface = customTheme.customSurfaceColor

    val shadeFactor = (customTheme.shadeIntensity / 100f).coerceIn(0.5f, 1.5f)

    return if (isDark) {
        val darkPrimary = when (intensity) {
            ColorIntensity.PASTEL_SOFT -> basePrimary.lighten((0.35f * shadeFactor).coerceIn(0.1f, 0.7f))
            ColorIntensity.STANDARD -> basePrimary.lighten((0.20f * shadeFactor).coerceIn(0.05f, 0.5f))
            ColorIntensity.VIVID -> basePrimary.lighten((0.30f * shadeFactor).coerceIn(0.1f, 0.6f))
            ColorIntensity.DEEP_CONTRAST -> basePrimary.lighten((0.45f * shadeFactor).coerceIn(0.2f, 0.8f))
        }
        val darkPrimaryContainer = basePrimary.darken(0.70f)
        val darkOnPrimaryContainer = basePrimary.lighten(0.80f)

        val darkSecondary = baseSecondary.lighten(0.25f)
        val darkSecondaryContainer = baseSecondary.darken(0.70f)

        val darkBg = when {
            isAmoled -> SolidAmoledBg
            customSurface != null -> customSurface.darken(0.25f)
            else -> darkSurfaceTone.darkBg
        }
        val darkSurface = when {
            isAmoled -> SolidAmoledSurface
            customSurface != null -> customSurface
            else -> darkSurfaceTone.darkSurface
        }
        val darkSurfaceVariant = when {
            isAmoled -> SolidAmoledSurfaceVariant
            customSurface != null -> customSurface.lighten(0.10f)
            else -> darkSurfaceTone.darkSurfaceVariant
        }

        darkColorScheme(
            primary = darkPrimary,
            onPrimary = Color(0xFF000000),
            primaryContainer = darkPrimaryContainer,
            onPrimaryContainer = darkOnPrimaryContainer,
            secondary = darkSecondary,
            onSecondary = Color(0xFF000000),
            secondaryContainer = darkSecondaryContainer,
            onSecondaryContainer = darkOnPrimaryContainer,
            tertiary = incomeColor,
            onTertiary = Color(0xFF052E16),
            background = darkBg,
            onBackground = Color(0xFFF8FAFC),
            surface = darkSurface,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = darkSurfaceVariant,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF3B4861),
            outlineVariant = Color(0xFF1F2937),
            error = expenseColor,
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFF4C0519),
            onErrorContainer = Color(0xFFFFE4E6)
        )
    } else {
        val lightPrimary = when (intensity) {
            ColorIntensity.PASTEL_SOFT -> basePrimary.lighten((0.15f * (2f - shadeFactor)).coerceIn(0.05f, 0.5f))
            ColorIntensity.STANDARD -> basePrimary
            ColorIntensity.VIVID -> basePrimary.darken((0.10f * shadeFactor).coerceIn(0.05f, 0.4f))
            ColorIntensity.DEEP_CONTRAST -> basePrimary.darken((0.25f * shadeFactor).coerceIn(0.1f, 0.6f))
        }
        val lightPrimaryContainer = basePrimary.lighten(0.88f)
        val lightOnPrimaryContainer = basePrimary.darken(0.65f)

        val lightSecondary = baseSecondary
        val lightSecondaryContainer = baseSecondary.lighten(0.90f)
        val lightOnSecondaryContainer = baseSecondary.darken(0.65f)

        val lightBg = if (customSurface != null) customSurface.lighten(0.05f) else Color(0xFFF8FAFC)
        val lightSurface = customSurface ?: Color.White
        val lightSurfaceVariant = if (customSurface != null) customSurface.darken(0.05f) else Color(0xFFF1F5F9)

        lightColorScheme(
            primary = lightPrimary,
            onPrimary = Color.White,
            primaryContainer = lightPrimaryContainer,
            onPrimaryContainer = lightOnPrimaryContainer,
            secondary = lightSecondary,
            onSecondary = Color.White,
            secondaryContainer = lightSecondaryContainer,
            onSecondaryContainer = lightOnSecondaryContainer,
            tertiary = incomeColor,
            onTertiary = Color.White,
            background = lightBg,
            onBackground = Color(0xFF0F172A),
            surface = lightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = lightSurfaceVariant,
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFF64748B),
            outlineVariant = Color(0xFFE2E8F0),
            error = expenseColor,
            onError = Color.White,
            errorContainer = SolidExpenseContainer,
            onErrorContainer = SolidOnExpenseContainer
        )
    }
}
