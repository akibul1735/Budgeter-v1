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

// Financial Semantic Indicators (Default)
val SolidIncome = Color(0xFF10B981)         // Emerald Green
val SolidIncomeDark = Color(0xFF047857)
val SolidIncomeContainer = Color(0xFFD1FAE5)
val SolidOnIncomeContainer = Color(0xFF065F46)

val SolidExpense = Color(0xFFEF4444)        // Crimson Red
val SolidExpenseDark = Color(0xFFB91C1C)
val SolidExpenseContainer = Color(0xFFFEE2E2)
val SolidOnExpenseContainer = Color(0xFF991B1B)

val SolidTransfer = Color(0xFF0284C7)       // Sky Blue
val SolidTransferContainer = Color(0xFFE0F2FE)

val SolidEquity = Color(0xFF8B5CF6)         // Violet
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
        // Dark Mode Primary
        val darkPrimary = when (palette) {
            ThemePalette.ELEGANT_BLUE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF93C5FD)
                ColorIntensity.STANDARD -> Color(0xFF60A5FA)
                ColorIntensity.VIVID -> Color(0xFF3B82F6)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF60A5FA)
            }
            ThemePalette.PREMIUM_GREEN -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFA7F3D0)
                ColorIntensity.STANDARD -> Color(0xFF4ADE80)
                ColorIntensity.VIVID -> Color(0xFF22C55E)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF4ADE80)
            }
            ThemePalette.SOFT_PASTEL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFDDD6FE)
                ColorIntensity.STANDARD -> Color(0xFFB39DDB)
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
                ColorIntensity.STANDARD -> Color(0xFF68B88E)
                ColorIntensity.VIVID -> Color(0xFF10B981)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF34D399)
            }
            ThemePalette.CRIMSON_ROYAL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFFECDD3)
                ColorIntensity.STANDARD -> Color(0xFFFB7185)
                ColorIntensity.VIVID -> Color(0xFFF43F5E)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFFDA4AF)
            }
            ThemePalette.CYAN_BREEZE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFBAE6FD)
                ColorIntensity.STANDARD -> Color(0xFF38BDF8)
                ColorIntensity.VIVID -> Color(0xFF0EA5E9)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF7DD3FC)
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
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFFDBA74)
            }
            ThemePalette.SOPHISTICATED_DARK -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFE4E4E7)
                ColorIntensity.STANDARD -> Color(0xFFD4D4D8)
                ColorIntensity.VIVID -> Color(0xFFF4F4F5)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFFFFFFF)
            }
            ThemePalette.MINIMAL_MONO -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFE9ECEF)
                ColorIntensity.STANDARD -> Color(0xFFCED4DA)
                ColorIntensity.VIVID -> Color(0xFFF8F9FA)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFFFFFFFF)
            }
        }

        val darkPrimaryContainer = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFF1E3A8A)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF14532D)
            ThemePalette.SOFT_PASTEL -> Color(0xFF3B1C6E)
            ThemePalette.WARM_NEUTRAL -> Color(0xFF78350F)
            ThemePalette.MODERN_INDIGO -> Color(0xFF312E81)
            ThemePalette.CALM_SAGE -> Color(0xFF143D28)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFF881337)
            ThemePalette.CYAN_BREEZE -> Color(0xFF0C4A6E)
            ThemePalette.DEEP_PURPLE -> Color(0xFF581C87)
            ThemePalette.SUNSET_ORANGE -> Color(0xFF7C2D12)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFF27272A)
            ThemePalette.MINIMAL_MONO -> Color(0xFF2B3035)
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
            onPrimary = Color(0xFF09090B),
            primaryContainer = darkPrimaryContainer,
            onPrimaryContainer = darkOnPrimaryContainer,
            secondary = darkSecondary,
            onSecondary = Color(0xFF09090B),
            secondaryContainer = darkSecondaryContainer,
            onSecondaryContainer = darkOnPrimaryContainer,
            tertiary = incomeColor,
            onTertiary = Color(0xFF052E16),
            background = darkBg,
            onBackground = Color(0xFFF8FAFC),
            surface = darkSurface,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = darkSurfaceVariant,
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF8EA3C0),
            outlineVariant = Color(0xFF334155),
            error = expenseColor,
            onError = Color(0xFF450A0A),
            errorContainer = Color(0xFF7F1D1D),
            onErrorContainer = Color(0xFFFEE2E2)
        )
    } else {
        // Light Mode Colors
        val lightPrimary = when (palette) {
            ThemePalette.ELEGANT_BLUE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF3B82F6)
                ColorIntensity.STANDARD -> Color(0xFF2563EB)
                ColorIntensity.VIVID -> Color(0xFF1D4ED8)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF1E40AF)
            }
            ThemePalette.PREMIUM_GREEN -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF22C55E)
                ColorIntensity.STANDARD -> Color(0xFF16A34A)
                ColorIntensity.VIVID -> Color(0xFF15803D)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF166534)
            }
            ThemePalette.SOFT_PASTEL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF8B5CF6)
                ColorIntensity.STANDARD -> Color(0xFF7C3AED)
                ColorIntensity.VIVID -> Color(0xFF6D28D9)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF5B21B6)
            }
            ThemePalette.WARM_NEUTRAL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFF59E0B)
                ColorIntensity.STANDARD -> Color(0xFFD97706)
                ColorIntensity.VIVID -> Color(0xFFB45309)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF92400E)
            }
            ThemePalette.MODERN_INDIGO -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF6366F1)
                ColorIntensity.STANDARD -> Color(0xFF4F46E5)
                ColorIntensity.VIVID -> Color(0xFF4338CA)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF3730A3)
            }
            ThemePalette.CALM_SAGE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF10B981)
                ColorIntensity.STANDARD -> Color(0xFF3B7A57)
                ColorIntensity.VIVID -> Color(0xFF2E6546)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF1E4B33)
            }
            ThemePalette.CRIMSON_ROYAL -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFF43F5E)
                ColorIntensity.STANDARD -> Color(0xFFE11D48)
                ColorIntensity.VIVID -> Color(0xFFBE123C)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF9F1239)
            }
            ThemePalette.CYAN_BREEZE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF0EA5E9)
                ColorIntensity.STANDARD -> Color(0xFF0284C7)
                ColorIntensity.VIVID -> Color(0xFF0369A1)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF075985)
            }
            ThemePalette.DEEP_PURPLE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFA855F7)
                ColorIntensity.STANDARD -> Color(0xFF9333EA)
                ColorIntensity.VIVID -> Color(0xFF7E22CE)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF6B21A8)
            }
            ThemePalette.SUNSET_ORANGE -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFFF97316)
                ColorIntensity.STANDARD -> Color(0xFFEA580C)
                ColorIntensity.VIVID -> Color(0xFFC2410C)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF9A3412)
            }
            ThemePalette.SOPHISTICATED_DARK -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF3F3F46)
                ColorIntensity.STANDARD -> Color(0xFF27272A)
                ColorIntensity.VIVID -> Color(0xFF18181B)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF09090B)
            }
            ThemePalette.MINIMAL_MONO -> when (intensity) {
                ColorIntensity.PASTEL_SOFT -> Color(0xFF495057)
                ColorIntensity.STANDARD -> Color(0xFF343A40)
                ColorIntensity.VIVID -> Color(0xFF212529)
                ColorIntensity.DEEP_CONTRAST -> Color(0xFF121416)
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
            ThemePalette.ELEGANT_BLUE -> Color(0xFF1E40AF)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF14532D)
            ThemePalette.SOFT_PASTEL -> Color(0xFF4C1D95)
            ThemePalette.WARM_NEUTRAL -> Color(0xFF78350F)
            ThemePalette.MODERN_INDIGO -> Color(0xFF312E81)
            ThemePalette.CALM_SAGE -> Color(0xFF143D28)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFF881337)
            ThemePalette.CYAN_BREEZE -> Color(0xFF0369A1)
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

        val lightBg = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFFF6F9FD)
            ThemePalette.PREMIUM_GREEN -> Color(0xFFF5FAF7)
            ThemePalette.SOFT_PASTEL -> Color(0xFFFAF8FC)
            ThemePalette.WARM_NEUTRAL -> Color(0xFFFAF7F2)
            ThemePalette.MODERN_INDIGO -> Color(0xFFF7F8FC)
            ThemePalette.CALM_SAGE -> Color(0xFFF5F8F6)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFFFFF7F8)
            ThemePalette.CYAN_BREEZE -> Color(0xFFF0F9FF)
            ThemePalette.DEEP_PURPLE -> Color(0xFFFAF5FF)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFFFF7ED)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFFF4F5F7)
            ThemePalette.MINIMAL_MONO -> Color(0xFFF8F9FA)
        }

        val lightSurface = Color(0xFFFFFFFF)

        val lightSurfaceVariant = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFFEDF3FA)
            ThemePalette.PREMIUM_GREEN -> Color(0xFFEAF4EE)
            ThemePalette.SOFT_PASTEL -> Color(0xFFF3EEF9)
            ThemePalette.WARM_NEUTRAL -> Color(0xFFF3ECE2)
            ThemePalette.MODERN_INDIGO -> Color(0xFFEFF1F9)
            ThemePalette.CALM_SAGE -> Color(0xFFEAF2EE)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFFFFECEF)
            ThemePalette.CYAN_BREEZE -> Color(0xFFE0F2FE)
            ThemePalette.DEEP_PURPLE -> Color(0xFFF3E8FF)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFFFEDD5)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFFE9EBEF)
            ThemePalette.MINIMAL_MONO -> Color(0xFFF1F3F5)
        }

        val lightOutline = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFF64748B)
            ThemePalette.PREMIUM_GREEN -> Color(0xFF52745E)
            ThemePalette.SOFT_PASTEL -> Color(0xFF6D5E7A)
            ThemePalette.WARM_NEUTRAL -> Color(0xFF786654)
            ThemePalette.MODERN_INDIGO -> Color(0xFF6B7280)
            ThemePalette.CALM_SAGE -> Color(0xFF5C7468)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFF9F1239)
            ThemePalette.CYAN_BREEZE -> Color(0xFF0284C7)
            ThemePalette.DEEP_PURPLE -> Color(0xFF7E22CE)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFC2410C)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFF71717A)
            ThemePalette.MINIMAL_MONO -> Color(0xFF6C757D)
        }

        val lightOutlineVariant = when (palette) {
            ThemePalette.ELEGANT_BLUE -> Color(0xFFCBD5E1)
            ThemePalette.PREMIUM_GREEN -> Color(0xFFC8E2D1)
            ThemePalette.SOFT_PASTEL -> Color(0xFFE4D8F0)
            ThemePalette.WARM_NEUTRAL -> Color(0xFFDFCFC0)
            ThemePalette.MODERN_INDIGO -> Color(0xFFD1D5DB)
            ThemePalette.CALM_SAGE -> Color(0xFFC8DDD2)
            ThemePalette.CRIMSON_ROYAL -> Color(0xFFFECDD3)
            ThemePalette.CYAN_BREEZE -> Color(0xFFBAE6FD)
            ThemePalette.DEEP_PURPLE -> Color(0xFFE9D5FF)
            ThemePalette.SUNSET_ORANGE -> Color(0xFFFED7AA)
            ThemePalette.SOPHISTICATED_DARK -> Color(0xFFD4D4D8)
            ThemePalette.MINIMAL_MONO -> Color(0xFFCED4DA)
        }

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
            onSurfaceVariant = Color(0xFF334155),
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
            ColorIntensity.PASTEL_SOFT -> basePrimary.lighten(0.55f)
            ColorIntensity.STANDARD -> basePrimary.lighten(0.35f)
            ColorIntensity.VIVID -> basePrimary.lighten(0.48f)
            ColorIntensity.DEEP_CONTRAST -> basePrimary.lighten(0.65f)
        }
        val darkPrimaryContainer = basePrimary.darken(0.55f)
        val darkOnPrimaryContainer = basePrimary.lighten(0.85f)

        val darkSecondary = baseSecondary.lighten(0.45f)
        val darkSecondaryContainer = baseSecondary.darken(0.55f)

        val darkBg = if (isAmoled) SolidAmoledBg else darkSurfaceTone.darkBg
        val darkSurface = if (isAmoled) SolidAmoledSurface else darkSurfaceTone.darkSurface
        val darkSurfaceVariant = if (isAmoled) SolidAmoledSurfaceVariant else darkSurfaceTone.darkSurfaceVariant

        darkColorScheme(
            primary = darkPrimary,
            onPrimary = Color(0xFF09090B),
            primaryContainer = darkPrimaryContainer,
            onPrimaryContainer = darkOnPrimaryContainer,
            secondary = darkSecondary,
            onSecondary = Color(0xFF09090B),
            secondaryContainer = darkSecondaryContainer,
            onSecondaryContainer = darkOnPrimaryContainer,
            tertiary = incomeColor,
            onTertiary = Color(0xFF052E16),
            background = darkBg,
            onBackground = Color(0xFFF8FAFC),
            surface = darkSurface,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = darkSurfaceVariant,
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF94A3B8),
            outlineVariant = Color(0xFF334155),
            error = expenseColor,
            onError = Color(0xFF450A0A),
            errorContainer = Color(0xFF7F1D1D),
            onErrorContainer = Color(0xFFFEE2E2)
        )
    } else {
        val lightPrimary = when (intensity) {
            ColorIntensity.PASTEL_SOFT -> basePrimary.lighten(0.15f)
            ColorIntensity.STANDARD -> basePrimary
            ColorIntensity.VIVID -> basePrimary.darken(0.08f)
            ColorIntensity.DEEP_CONTRAST -> basePrimary.darken(0.22f)
        }
        val lightPrimaryContainer = basePrimary.lighten(0.85f)
        val lightOnPrimaryContainer = basePrimary.darken(0.60f)

        val lightSecondary = baseSecondary
        val lightSecondaryContainer = baseSecondary.lighten(0.88f)
        val lightOnSecondaryContainer = baseSecondary.darken(0.60f)

        val lightBg = basePrimary.lighten(0.97f)
        val lightSurface = Color.White
        val lightSurfaceVariant = basePrimary.lighten(0.93f)

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
            onSurfaceVariant = Color(0xFF334155),
            outline = Color(0xFF64748B),
            outlineVariant = basePrimary.lighten(0.86f),
            error = expenseColor,
            onError = Color.White,
            errorContainer = SolidExpenseContainer,
            onErrorContainer = SolidOnExpenseContainer
        )
    }
}
