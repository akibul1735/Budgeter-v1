package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Default Brand Constants - Luxury Fintech Baseline
val SolidPrimary = Color(0xFF059669)         // Rich Emerald Wealth
val SolidPrimaryDark = Color(0xFF10B981)
val SolidPrimaryContainer = Color(0xFFDCFCE7)
val SolidOnPrimaryContainer = Color(0xFF064E3B)

// Financial Semantic Indicators (Default)
val SolidIncome = Color(0xFF059669)          // Rich Emerald
val SolidIncomeDark = Color(0xFF10B981)
val SolidIncomeContainer = Color(0xFFDCFCE7)
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
val SolidLightBg = Color(0xFFF8F9FA)
val SolidLightSurface = Color(0xFFFFFFFF)
val SolidLightSurfaceVariant = Color(0xFFF1F3F6)
val SolidLightTextPrimary = Color(0xFF0A0F1D)
val SolidLightTextSecondary = Color(0xFF475569)
val SolidLightBorder = Color(0xFFE2E8F0)

// Neutrals: Dark (Obsidian Luxury)
val SolidDarkBg = Color(0xFF0A0D14)
val SolidDarkSurface = Color(0xFF131824)
val SolidDarkSurfaceVariant = Color(0xFF1C2333)
val SolidDarkTextPrimary = Color(0xFFF8FAFC)
val SolidDarkTextSecondary = Color(0xFF94A3B8)
val SolidDarkBorder = Color(0xFF252D3D)

// Neutrals: AMOLED Night (Pure Pitch)
val SolidAmoledBg = Color(0xFF000000)
val SolidAmoledSurface = Color(0xFF0D0E12)
val SolidAmoledSurfaceVariant = Color(0xFF161820)
val SolidAmoledBorder = Color(0xFF22242C)

// Color scheme builders for all palettes and custom configurations
fun buildThemeColorScheme(
    palette: ThemePalette,
    isDark: Boolean,
    isAmoled: Boolean = false,
    intensity: ColorIntensity = ColorIntensity.VIVID,
    semanticPalette: FinancialSemanticPalette = FinancialSemanticPalette.CLASSIC,
    darkSurfaceTone: DarkSurfaceTone = DarkSurfaceTone.SLATE_BLUE
): ColorScheme {
    val incomeColor = semanticPalette.incomeColor
    val expenseColor = semanticPalette.expenseColor

    return if (isDark) {
        // Dark Mode Primary - Luminous, punchy, and solid
        val darkPrimary = when (palette) {
            ThemePalette.ELEGANT_BLUE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF93C5FD)
                ColorIntensity.STANDARD -> Color(0xFF60A5FA)
                ColorIntensity.VIVID -> Color(0xFF3B82F6)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF60A5FA)
            }
            ThemePalette.PREMIUM_GREEN -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFA7F3D0)
                ColorIntensity.STANDARD -> Color(0xFF34D399)
                ColorIntensity.VIVID -> Color(0xFF10B981)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF059669)
            }
            ThemePalette.SOFT_PASTEL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFDDD6FE)
                ColorIntensity.STANDARD -> Color(0xFFC084FC)
                ColorIntensity.VIVID -> Color(0xFFA855F7)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFC084FC)
            }
            ThemePalette.WARM_NEUTRAL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFFDE68A)
                ColorIntensity.STANDARD -> Color(0xFFFBBF24)
                ColorIntensity.VIVID -> Color(0xFFF59E0B)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFFBBF24)
            }
            ThemePalette.MODERN_INDIGO -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFC7D2FE)
                ColorIntensity.STANDARD -> Color(0xFF818CF8)
                ColorIntensity.VIVID -> Color(0xFF6366F1)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF818CF8)
            }
            ThemePalette.CALM_SAGE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFA7F3D0)
                ColorIntensity.STANDARD -> Color(0xFF6EE7B7)
                ColorIntensity.VIVID -> Color(0xFF10B981)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF34D399)
            }
            ThemePalette.CRIMSON_ROYAL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFFECDD3)
                ColorIntensity.STANDARD -> Color(0xFFFDA4AF)
                ColorIntensity.VIVID -> Color(0xFFFB7185)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFF43F5E)
            }
            ThemePalette.CYAN_BREEZE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFBAE6FD)
                ColorIntensity.STANDARD -> Color(0xFF38BDF8)
                ColorIntensity.VIVID -> Color(0xFF0EA5E9)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF38BDF8)
            }
            ThemePalette.DEEP_PURPLE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFE9D5FF)
                ColorIntensity.STANDARD -> Color(0xFFC084FC)
                ColorIntensity.VIVID -> Color(0xFFA855F7)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFD8B4FE)
            }
            ThemePalette.SUNSET_ORANGE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFFFEDD5)
                ColorIntensity.STANDARD -> Color(0xFFFB923C)
                ColorIntensity.VIVID -> Color(0xFFF97316)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFFB923C)
            }
            ThemePalette.SOPHISTICATED_DARK -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFE4E4E7)
                ColorIntensity.STANDARD -> Color(0xFFD4D4D8)
                ColorIntensity.VIVID -> Color(0xFFF4F4F5)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFFFFFFF)
            }
            ThemePalette.MINIMAL_MONO -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFE9ECEF)
                ColorIntensity.STANDARD -> Color(0xFFDEE2E6)
                ColorIntensity.VIVID -> Color(0xFFF8F9FA)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFFFFFFF)
            }
        }

        val darkPrimaryContainer = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFF1E3A8A)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF064E3B)
            ThemePalette.SOFT_PASTEL -> Color(0xFF4C1D95)
            ThemePalette.WARM_NEUTRAL -> Color(0xFF78350F)
            ThemePalette.MODERN_INDIGO -> Color(0xFF312E81)
            ThemePalette.CALM_SAGE -> Color(0xFF143D28)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFF881337)
            ThemePalette.CYAN_BREEZE -> Color(0xFF0C4A6E)
            ThemePalette.DEEP_PURPLE -> Color(0xFF581C87)
            ThemePalette.SUNSET_ORANGE -> Color(0xFF7C2D12)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFF27272A)
            ThemePalette.MINIMAL_MONO -> Color(0xFF343A40)
        }

        val darkOnPrimaryContainer = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFFDBEAFE)
            ThemePalette.PREMIUM_GREEN -> Color(0xFFDCFCE7)
            ThemePalette.SOFT_PASTEL -> Color(0xFFEDE9FE)
            ThemePalette.WARM_NEUTRAL -> Color(0xFFFEF3C7)
            ThemePalette.MODERN_INDIGO -> Color(0xFFE0E7FF)
            ThemePalette.CALM_SAGE -> Color(0xFFDDF0E6)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFFFFE4E6)
            ThemePalette.CYAN_BREEZE -> Color(0xFFE0F2FE)
            ThemePalette.DEEP_PURPLE -> Color(0xFFF3E8FF)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFFFEDD5)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFFF4F4F5)
            ThemePalette.MINIMAL_MONO -> Color(0xFFF8F9FA)
        }

        val darkSecondary = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFF38BDF8)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF2DD4BF)
            ThemePalette.SOFT_PASTEL -> Color(0xFFDDD6FE)
            ThemePalette.WARM_NEUTRAL -> Color(0xFFFB923C)
            ThemePalette.MODERN_INDIGO -> Color(0xFFA5B4FC)
            ThemePalette.CALM_SAGE -> Color(0xFF95D5B2)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFFFDA4AF)
            ThemePalette.CYAN_BREEZE -> Color(0xFF7DD3FC)
            ThemePalette.DEEP_PURPLE -> Color(0xFFE9D5FF)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFFDBA74)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFFD4D4D8)
            ThemePalette.MINIMAL_MONO -> Color(0xFFDEE2E6)
        }

        val darkSecondaryContainer = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFF0369A1)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF115E59)
            ThemePalette.SOFT_PASTEL -> Color(0xFF2E1065)
            ThemePalette.WARM_NEUTRAL -> Color(0xFF7C2D12)
            ThemePalette.MODERN_INDIGO -> Color(0xFF3730A3)
            ThemePalette.CALM_SAGE -> Color(0xFF1B3D2B)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFF4C0519)
            ThemePalette.CYAN_BREEZE -> Color(0xFF075985)
            ThemePalette.DEEP_PURPLE -> Color(0xFF3B0764)
            ThemePalette.SUNSET_ORANGE -> Color(0xFF431407)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFF18181B)
            ThemePalette.MINIMAL_MONO -> Color(0xFF212529)
        }

        val darkBg = if (isAmoled) SolidAmoledBg else darkSurfaceTone.darkBg
        val darkSurface = if (isAmoled) SolidAmoledSurface else darkSurfaceTone.darkSurface
        val darkSurfaceVariant = if (isAmoled) SolidAmoledSurfaceVariant else darkSurfaceTone.darkSurfaceVariant

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
            outline = Color(0xFF4A5873),
            outlineVariant = Color(0xFF212A3B),
            error = expenseColor,
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFF7F1D1D),
            onErrorContainer = Color(0xFFFFE4E6)
        )
    } else {
        // Light Mode Colors - Deep, rich, luxurious, non-faded
        val lightPrimary = when (palette) {
            ThemePalette.ELEGANT_BLUE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF2563EB)
                ColorIntensity.STANDARD -> Color(0xFF1D4ED8)
                ColorIntensity.VIVID -> Color(0xFF1E40AF)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF172554)
            }
            ThemePalette.PREMIUM_GREEN -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF16A34A)
                ColorIntensity.STANDARD -> Color(0xFF059669)
                ColorIntensity.VIVID -> Color(0xFF047857)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF064E3B)
            }
            ThemePalette.SOFT_PASTEL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF8B5CF6)
                ColorIntensity.STANDARD -> Color(0xFF7C3AED)
                ColorIntensity.VIVID -> Color(0xFF6D28D9)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF4C1D95)
            }
            ThemePalette.WARM_NEUTRAL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFD97706)
                ColorIntensity.STANDARD -> Color(0xFFB45309)
                ColorIntensity.VIVID -> Color(0xFF92400E)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF78350F)
            }
            ThemePalette.MODERN_INDIGO -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF4F46E5)
                ColorIntensity.STANDARD -> Color(0xFF4338CA)
                ColorIntensity.VIVID -> Color(0xFF3730A3)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF312E81)
            }
            ThemePalette.CALM_SAGE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF3B7A57)
                ColorIntensity.STANDARD -> Color(0xFF2E6546)
                ColorIntensity.VIVID -> Color(0xFF1E4B33)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF143D28)
            }
            ThemePalette.CRIMSON_ROYAL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFE11D48)
                ColorIntensity.STANDARD -> Color(0xFFBE123C)
                ColorIntensity.VIVID -> Color(0xFF9F1239)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF881337)
            }
            ThemePalette.CYAN_BREEZE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF0284C7)
                ColorIntensity.STANDARD -> Color(0xFF0369A1)
                ColorIntensity.VIVID -> Color(0xFF075985)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF0C4A6E)
            }
            ThemePalette.DEEP_PURPLE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF9333EA)
                ColorIntensity.STANDARD -> Color(0xFF7E22CE)
                ColorIntensity.VIVID -> Color(0xFF6B21A8)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF581C87)
            }
            ThemePalette.SUNSET_ORANGE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFEA580C)
                ColorIntensity.STANDARD -> Color(0xFFC2410C)
                ColorIntensity.VIVID -> Color(0xFF9A3412)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF7C2D12)
            }
            ThemePalette.SOPHISTICATED_DARK -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF27272A)
                ColorIntensity.STANDARD -> Color(0xFF18181B)
                ColorIntensity.VIVID -> Color(0xFF09090B)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF000000)
            }
            ThemePalette.MINIMAL_MONO -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF343A40)
                ColorIntensity.STANDARD -> Color(0xFF212529)
                ColorIntensity.VIVID -> Color(0xFF121416)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF000000)
            }
        }

        val lightPrimaryContainer = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFFDBEAFE)
            ThemePalette.PREMIUM_GREEN -> Color(0xFFDCFCE7)
            ThemePalette.SOFT_PASTEL -> Color(0xFFEDE9FE)
            ThemePalette.WARM_NEUTRAL -> Color(0xFFFEF3C7)
            ThemePalette.MODERN_INDIGO -> Color(0xFFE0E7FF)
            ThemePalette.CALM_SAGE -> Color(0xFFDDF0E6)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFFFFE4E6)
            ThemePalette.CYAN_BREEZE -> Color(0xFFE0F2FE)
            ThemePalette.DEEP_PURPLE -> Color(0xFFF3E8FF)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFFFEDD5)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFFE4E4E7)
            ThemePalette.MINIMAL_MONO -> Color(0xFFE9ECEF)
        }

        val lightOnPrimaryContainer = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFF1E3A8A)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF064E3B)
            ThemePalette.SOFT_PASTEL -> Color(0xFF4C1D95)
            ThemePalette.WARM_NEUTRAL -> Color(0xFF78350F)
            ThemePalette.MODERN_INDIGO -> Color(0xFF312E81)
            ThemePalette.CALM_SAGE -> Color(0xFF143D28)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFF881337)
            ThemePalette.CYAN_BREEZE -> Color(0xFF0C4A6E)
            ThemePalette.DEEP_PURPLE -> Color(0xFF581C87)
            ThemePalette.SUNSET_ORANGE -> Color(0xFF7C2D12)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFF09090B)
            ThemePalette.MINIMAL_MONO -> Color(0xFF141619)
        }

        val lightSecondary = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFF0284C7)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF0D9488)
            ThemePalette.SOFT_PASTEL -> Color(0xFF8B5CF6)
            ThemePalette.WARM_NEUTRAL -> Color(0xFFC2410C)
            ThemePalette.MODERN_INDIGO -> Color(0xFF6366F1)
            ThemePalette.CALM_SAGE -> Color(0xFF476E5B)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFFBE123C)
            ThemePalette.CYAN_BREEZE -> Color(0xFF0EA5E9)
            ThemePalette.DEEP_PURPLE -> Color(0xFF7E22CE)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFEA580C)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFF52525B)
            ThemePalette.MINIMAL_MONO -> Color(0xFF495057)
        }

        val lightSecondaryContainer = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFFE0F2FE)
            ThemePalette.PREMIUM_GREEN -> Color(0xFFCCFBF1)
            ThemePalette.SOFT_PASTEL -> Color(0xFFF3E8FF)
            ThemePalette.WARM_NEUTRAL -> Color(0xFFFFEDD5)
            ThemePalette.MODERN_INDIGO -> Color(0xFFEEF2FF)
            ThemePalette.CALM_SAGE -> Color(0xFFE6F2EC)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFFFFE4E6)
            ThemePalette.CYAN_BREEZE -> Color(0xFFE0F2FE)
            ThemePalette.DEEP_PURPLE -> Color(0xFFF3E8FF)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFFFEDD5)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFFF4F4F5)
            ThemePalette.MINIMAL_MONO -> Color(0xFFF1F3F5)
        }

        val lightOnSecondaryContainer = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFF075985)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF115E59)
            ThemePalette.SOFT_PASTEL -> Color(0xFF581C87)
            ThemePalette.WARM_NEUTRAL -> Color(0xFF7C2D12)
            ThemePalette.MODERN_INDIGO -> Color(0xFF3730A3)
            ThemePalette.CALM_SAGE -> Color(0xFF1E3F30)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFF4C0519)
            ThemePalette.CYAN_BREEZE -> Color(0xFF0369A1)
            ThemePalette.DEEP_PURPLE -> Color(0xFF3B0764)
            ThemePalette.SUNSET_ORANGE -> Color(0xFF431407)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFF18181B)
            ThemePalette.MINIMAL_MONO -> Color(0xFF212529)
        }

        // Clean, crisp solid background canvas
        val lightBg = Color(0xFFF8F9FA)
        val lightSurface = Color(0xFFFFFFFF)
        val lightSurfaceVariant = Color(0xFFF1F3F6)
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
            onBackground = Color(0xFF0A0F1D),
            surface = lightSurface,
            onSurface = Color(0xFF0A0F1D),
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

fun buildCustomThemeColorScheme(
    customTheme: CustomTheme,
    isDark: Boolean,
    isAmoled: Boolean = false,
    intensity: ColorIntensity = ColorIntensity.VIVID,
    semanticPalette: FinancialSemanticPalette = FinancialSemanticPalette.CLASSIC,
    darkSurfaceTone: DarkSurfaceTone = DarkSurfaceTone.SLATE_BLUE
): ColorScheme {
    val basePrimary = customTheme.primaryColor
    val baseSecondary = customTheme.secondaryColor
    val incomeColor = customTheme.customIncomeColor ?: semanticPalette.incomeColor
    val expenseColor = customTheme.customExpenseColor ?: semanticPalette.expenseColor

    return if (isDark) {
        val darkPrimary = when (intensity) {
            ColorIntensity.PASTEL_SOFT -> basePrimary.lighten(0.40f)
            ColorIntensity.STANDARD -> basePrimary.lighten(0.25f)
            ColorIntensity.VIVID -> basePrimary.lighten(0.35f)
            ColorIntensity.DEEP_CONTRAST -> basePrimary.lighten(0.50f)
        }
        val darkPrimaryContainer = basePrimary.darken(0.60f)
        val darkOnPrimaryContainer = basePrimary.lighten(0.85f)

        val darkSecondary = baseSecondary.lighten(0.35f)
        val darkSecondaryContainer = baseSecondary.darken(0.60f)

        val darkBg = if (isAmoled) SolidAmoledBg else darkSurfaceTone.darkBg
        val darkSurface = if (isAmoled) SolidAmoledSurface else darkSurfaceTone.darkSurface
        val darkSurfaceVariant = if (isAmoled) SolidAmoledSurfaceVariant else darkSurfaceTone.darkSurfaceVariant

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
            outline = Color(0xFF4A5873),
            outlineVariant = Color(0xFF212A3B),
            error = expenseColor,
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFF7F1D1D),
            onErrorContainer = Color(0xFFFFE4E6)
        )
    } else {
        val lightPrimary = when (intensity) {
            ColorIntensity.PASTEL_SOFT -> basePrimary.lighten(0.10f)
            ColorIntensity.STANDARD -> basePrimary
            ColorIntensity.VIVID -> basePrimary.darken(0.10f)
            ColorIntensity.DEEP_CONTRAST -> basePrimary.darken(0.25f)
        }
        val lightPrimaryContainer = basePrimary.lighten(0.85f)
        val lightOnPrimaryContainer = basePrimary.darken(0.65f)

        val lightSecondary = baseSecondary
        val lightSecondaryContainer = baseSecondary.lighten(0.88f)
        val lightOnSecondaryContainer = baseSecondary.darken(0.65f)

        val lightBg = Color(0xFFF8F9FA)
        val lightSurface = Color.White
        val lightSurfaceVariant = Color(0xFFF1F3F6)

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
            onBackground = Color(0xFF0A0F1D),
            surface = lightSurface,
            onSurface = Color(0xFF0A0F1D),
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
