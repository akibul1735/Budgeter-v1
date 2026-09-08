package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemePalette(val displayNameEn: String, val displayNameBn: String, val primaryColor: Color) {
    ELEGANT_BLUE("Royal Sapphire", "এলিগ্যান্ট ব্লু", Color(0xFF1D4ED8)),
    PREMIUM_GREEN("Emerald Wealth", "প্রিমিয়াম গ্রিন", Color(0xFF15803D)),
    SOFT_PASTEL("Lavender Mist", "সফট ল্যাভেন্ডার", Color(0xFF7E57C2)),
    WARM_NEUTRAL("Warm Amber Gold", "ওয়ার্ম গোল্ডেন", Color(0xFFB45309)),
    MODERN_INDIGO("Modern Indigo", "মডার্ন ইন্ডিগো", Color(0xFF4F46E5)),
    CALM_SAGE("Calm Sage", "কাম সেইজ", Color(0xFF3B7A57)),
    CRIMSON_ROYAL("Royal Crimson", "রয়্যাল ক্রিমসন", Color(0xFFBE123C)),
    CYAN_BREEZE("Ocean Breeze", "ওশেন সায়ান", Color(0xFF0284C7)),
    DEEP_PURPLE("Electric Violet", "ইলেক্ট্রিক ভায়োলেট", Color(0xFF9333EA)),
    SUNSET_ORANGE("Sunset Flame", "সানসেট ফ্লেম", Color(0xFFEA580C)),
    SOPHISTICATED_DARK("Slate Graphite", "সোফিস্টিকেটেড স্লেট", Color(0xFF27272A)),
    MINIMAL_MONO("Minimal Monochrome", "মিনিমাল মনোক্রোম", Color(0xFF343A40))
}

enum class ThemeMode(val titleEn: String, val titleBn: String) {
    SYSTEM("Follow System", "সিস্টেম অনুযায়ী"),
    LIGHT("Light Mode", "লাইট মোড"),
    DARK("Dark Mode", "ডার্ক মোড"),
    AMOLED_NIGHT("Pure AMOLED Night", "পিওর ওলেড ব্ল্যাক")
}

enum class FontPreset(val titleEn: String, val titleBn: String, val fontFamily: FontFamily) {
    DEFAULT("Default System", "সিস্টেম ডিফল্ট", FontFamily.Default),
    SANS_SERIF("Modern Sans", "মডার্ন স্যান্স", FontFamily.SansSerif),
    SERIF("Classic Serif", "ক্লাসিক সেরিফ", FontFamily.Serif),
    MONOSPACE("Tech Monospace", "টেক মনোস্পেস", FontFamily.Monospace),
    CURSIVE("Friendly Script", "হাতে লেখা ফন্ট", FontFamily.Cursive)
}

enum class ColorIntensity(val titleEn: String, val titleBn: String) {
    PASTEL_SOFT("Soft Pastel", "নরম ও হালকা"),
    STANDARD("Standard", "স্বাভাবিক"),
    VIVID("Vivid (Rich)", "উজ্জ্বল গাঢ়"),
    DEEP_CONTRAST("Deep Contrast", "উচ্চ স্পষ্টতা")
}

enum class AppCornerRadius(val titleEn: String, val titleBn: String, val cornerDp: Int) {
    COMPACT("Compact (4dp)", "কম্প্যাক্ট (৪dp)", 4),
    STANDARD("Standard (12dp)", "স্ট্যান্ডার্ড (১২dp)", 12),
    ROUNDED("Smooth (18dp)", "রাউন্ডেড (১৮dp)", 18),
    PILL("Pill (26dp)", "পিল (২৬dp)", 26)
}

enum class AppFontScale(val titleEn: String, val titleBn: String, val scaleFactor: Float) {
    COMPACT("Compact (90%)", "কম্প্যাক্ট (৯০%)", 0.90f),
    DEFAULT("Standard (100%)", "স্ট্যান্ডার্ড (১০০%)", 1.00f),
    LARGE("Large (110%)", "বড় (১১০%)", 1.10f),
    EXTRA_LARGE("Extra Large (120%)", "খুব বড় (১২০%)", 1.20f)
}

enum class FinancialSemanticPalette(
    val titleEn: String,
    val titleBn: String,
    val incomeColor: Color,
    val expenseColor: Color,
    val transferColor: Color
) {
    CLASSIC(
        "Classic Emerald & Crimson",
        "ক্লাসিক সবুজ ও লাল",
        Color(0xFF10B981),
        Color(0xFFEF4444),
        Color(0xFF0284C7)
    ),
    MINT_CORAL(
        "Fresh Mint & Coral Rose",
        "মিন্ট ও কোরাল",
        Color(0xFF0D9488),
        Color(0xFFF43F5E),
        Color(0xFF6366F1)
    ),
    OCEAN_AMBER(
        "Ocean Cyan & Warm Amber",
        "সায়ান ও অ্যাম্বার",
        Color(0xFF0284C7),
        Color(0xFFF59E0B),
        Color(0xFF8B5CF6)
    ),
    INDIGO_ROSE(
        "Royal Indigo & Ruby Rose",
        "ইন্ডিগো ও রুবি",
        Color(0xFF4F46E5),
        Color(0xFFE11D48),
        Color(0xFF059669)
    ),
    FOREST_RUBY(
        "Forest Green & Deep Ruby",
        "ফরেস্ট ও রুবি",
        Color(0xFF15803D),
        Color(0xFFB91C1C),
        Color(0xFF0284C7)
    )
}

enum class DarkSurfaceTone(
    val titleEn: String,
    val titleBn: String,
    val darkBg: Color,
    val darkSurface: Color,
    val darkSurfaceVariant: Color
) {
    SLATE_BLUE(
        "Slate Dark",
        "স্লেট ব্লু ডার্ক",
        Color(0xFF0F172A),
        Color(0xFF1E293B),
        Color(0xFF334155)
    ),
    AMOLED_PITCH(
        "Pure OLED Pitch Black",
        "পিওর ওলেড ব্ল্যাক",
        Color(0xFF000000),
        Color(0xFF0D0D0D),
        Color(0xFF1A1A1A)
    ),
    WARM_CHARCOAL(
        "Warm Espresso Charcoal",
        "ওয়ার্ম চারকোল",
        Color(0xFF18181B),
        Color(0xFF27272A),
        Color(0xFF3F3F46)
    ),
    MIDNIGHT_INDIGO(
        "Midnight Indigo Deep",
        "মিডনাইট ইন্ডিগো",
        Color(0xFF090B14),
        Color(0xFF121626),
        Color(0xFF1E233D)
    )
}

data class CustomTheme(
    val id: String,
    val name: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long? = null,
    val incomeColorHex: Long? = null,
    val expenseColorHex: Long? = null,
    val isDarkOptimized: Boolean = true
) {
    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = secondaryColorHex?.let { Color(it) } ?: primaryColor
    val customIncomeColor: Color? get() = incomeColorHex?.let { Color(it) }
    val customExpenseColor: Color? get() = expenseColorHex?.let { Color(it) }
}

data class AppThemeConfig(
    val palette: ThemePalette = ThemePalette.ELEGANT_BLUE,
    val customThemeId: String? = null,
    val customThemes: List<CustomTheme> = emptyList(),
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val colorIntensity: ColorIntensity = ColorIntensity.VIVID,
    val dynamicColor: Boolean = false,
    val fontPreset: FontPreset = FontPreset.DEFAULT,
    val cornerRadius: AppCornerRadius = AppCornerRadius.STANDARD,
    val fontScale: AppFontScale = AppFontScale.DEFAULT,
    val semanticPalette: FinancialSemanticPalette = FinancialSemanticPalette.CLASSIC,
    val darkSurfaceTone: DarkSurfaceTone = DarkSurfaceTone.SLATE_BLUE
) {
    val activeCustomTheme: CustomTheme?
        get() = customThemes.find { it.id == customThemeId }

    val activeThemeDisplayName: String
        get() = activeCustomTheme?.name ?: palette.displayNameEn

    val activeIncomeColor: Color
        get() = activeCustomTheme?.customIncomeColor ?: semanticPalette.incomeColor

    val activeExpenseColor: Color
        get() = activeCustomTheme?.customExpenseColor ?: semanticPalette.expenseColor

    val activeTransferColor: Color
        get() = semanticPalette.transferColor
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_theme_prefs", Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<AppThemeConfig> = _themeConfig.asStateFlow()

    private fun loadConfig(): AppThemeConfig {
        val paletteName = prefs.getString(KEY_PALETTE, ThemePalette.ELEGANT_BLUE.name) ?: ThemePalette.ELEGANT_BLUE.name
        val selectedCustomThemeId = prefs.getString(KEY_SELECTED_CUSTOM_THEME_ID, null)
        val customThemesJson = prefs.getString(KEY_CUSTOM_THEMES, null)
        val customThemes = deserializeCustomThemes(customThemesJson)

        val modeName = prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val intensityName = prefs.getString(KEY_INTENSITY, ColorIntensity.VIVID.name) ?: ColorIntensity.VIVID.name
        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        val fontName = prefs.getString(KEY_FONT, FontPreset.DEFAULT.name) ?: FontPreset.DEFAULT.name
        val cornerRadiusName = prefs.getString(KEY_CORNER_RADIUS, AppCornerRadius.STANDARD.name) ?: AppCornerRadius.STANDARD.name
        val fontScaleName = prefs.getString(KEY_FONT_SCALE, AppFontScale.DEFAULT.name) ?: AppFontScale.DEFAULT.name
        val semanticPaletteName = prefs.getString(KEY_SEMANTIC_PALETTE, FinancialSemanticPalette.CLASSIC.name) ?: FinancialSemanticPalette.CLASSIC.name
        val darkSurfaceToneName = prefs.getString(KEY_DARK_SURFACE_TONE, DarkSurfaceTone.SLATE_BLUE.name) ?: DarkSurfaceTone.SLATE_BLUE.name

        val palette = when (paletteName) {
            "EMERALD" -> ThemePalette.PREMIUM_GREEN
            "SAPPHIRE" -> ThemePalette.ELEGANT_BLUE
            "AMETHYST" -> ThemePalette.SOFT_PASTEL
            "GOLDEN" -> ThemePalette.WARM_NEUTRAL
            "CRIMSON" -> ThemePalette.CRIMSON_ROYAL
            "TEAL" -> ThemePalette.CALM_SAGE
            "SUNSET" -> ThemePalette.SUNSET_ORANGE
            else -> try { ThemePalette.valueOf(paletteName) } catch (_: Exception) { ThemePalette.ELEGANT_BLUE }
        }
        val mode = try { ThemeMode.valueOf(modeName) } catch (_: Exception) { ThemeMode.SYSTEM }
        val intensity = try { ColorIntensity.valueOf(intensityName) } catch (_: Exception) { ColorIntensity.VIVID }
        val font = try { FontPreset.valueOf(fontName) } catch (_: Exception) { FontPreset.DEFAULT }
        val cornerRadius = try { AppCornerRadius.valueOf(cornerRadiusName) } catch (_: Exception) { AppCornerRadius.STANDARD }
        val fontScale = try { AppFontScale.valueOf(fontScaleName) } catch (_: Exception) { AppFontScale.DEFAULT }
        val semanticPalette = try { FinancialSemanticPalette.valueOf(semanticPaletteName) } catch (_: Exception) { FinancialSemanticPalette.CLASSIC }
        val darkSurfaceTone = try { DarkSurfaceTone.valueOf(darkSurfaceToneName) } catch (_: Exception) { DarkSurfaceTone.SLATE_BLUE }

        val validCustomThemeId = if (selectedCustomThemeId != null && customThemes.any { it.id == selectedCustomThemeId }) {
            selectedCustomThemeId
        } else null

        return AppThemeConfig(
            palette = palette,
            customThemeId = validCustomThemeId,
            customThemes = customThemes,
            mode = mode,
            colorIntensity = intensity,
            dynamicColor = dynamicColor,
            fontPreset = font,
            cornerRadius = cornerRadius,
            fontScale = fontScale,
            semanticPalette = semanticPalette,
            darkSurfaceTone = darkSurfaceTone
        )
    }

    fun setPalette(palette: ThemePalette) {
        prefs.edit()
            .putString(KEY_PALETTE, palette.name)
            .remove(KEY_SELECTED_CUSTOM_THEME_ID)
            .apply()
        _themeConfig.value = _themeConfig.value.copy(
            palette = palette,
            customThemeId = null
        )
    }

    fun selectCustomTheme(themeId: String) {
        val current = _themeConfig.value
        if (current.customThemes.any { it.id == themeId }) {
            prefs.edit().putString(KEY_SELECTED_CUSTOM_THEME_ID, themeId).apply()
            _themeConfig.value = current.copy(customThemeId = themeId)
        }
    }

    fun addCustomTheme(
        name: String,
        primaryColorHex: Long,
        secondaryColorHex: Long? = null,
        incomeColorHex: Long? = null,
        expenseColorHex: Long? = null
    ): CustomTheme {
        val current = _themeConfig.value
        val newTheme = CustomTheme(
            id = "theme_${System.currentTimeMillis()}",
            name = name.trim().ifBlank { "Custom Theme" },
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            incomeColorHex = incomeColorHex,
            expenseColorHex = expenseColorHex
        )
        val updatedList = current.customThemes + newTheme
        prefs.edit()
            .putString(KEY_CUSTOM_THEMES, serializeCustomThemes(updatedList))
            .putString(KEY_SELECTED_CUSTOM_THEME_ID, newTheme.id)
            .apply()
        _themeConfig.value = current.copy(
            customThemes = updatedList,
            customThemeId = newTheme.id
        )
        return newTheme
    }

    fun updateCustomTheme(theme: CustomTheme) {
        val current = _themeConfig.value
        val updatedList = current.customThemes.map {
            if (it.id == theme.id) theme else it
        }
        prefs.edit()
            .putString(KEY_CUSTOM_THEMES, serializeCustomThemes(updatedList))
            .apply()
        _themeConfig.value = current.copy(customThemes = updatedList)
    }

    fun deleteCustomTheme(themeId: String) {
        val current = _themeConfig.value
        val updatedList = current.customThemes.filterNot { it.id == themeId }
        val newSelectedId = if (current.customThemeId == themeId) null else current.customThemeId
        val editor = prefs.edit().putString(KEY_CUSTOM_THEMES, serializeCustomThemes(updatedList))
        if (newSelectedId == null) {
            editor.remove(KEY_SELECTED_CUSTOM_THEME_ID)
        } else {
            editor.putString(KEY_SELECTED_CUSTOM_THEME_ID, newSelectedId)
        }
        editor.apply()
        _themeConfig.value = current.copy(
            customThemes = updatedList,
            customThemeId = newSelectedId
        )
    }

    fun updateConfig(config: AppThemeConfig) {
        prefs.edit()
            .putString(KEY_PALETTE, config.palette.name)
            .putString(KEY_SELECTED_CUSTOM_THEME_ID, config.customThemeId)
            .putString(KEY_CUSTOM_THEMES, serializeCustomThemes(config.customThemes))
            .putString(KEY_MODE, config.mode.name)
            .putString(KEY_INTENSITY, config.colorIntensity.name)
            .putBoolean(KEY_DYNAMIC_COLOR, config.dynamicColor)
            .putString(KEY_FONT, config.fontPreset.name)
            .putString(KEY_CORNER_RADIUS, config.cornerRadius.name)
            .putString(KEY_FONT_SCALE, config.fontScale.name)
            .putString(KEY_SEMANTIC_PALETTE, config.semanticPalette.name)
            .putString(KEY_DARK_SURFACE_TONE, config.darkSurfaceTone.name)
            .apply()
        _themeConfig.value = config
    }

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _themeConfig.value = _themeConfig.value.copy(mode = mode)
    }

    fun setColorIntensity(intensity: ColorIntensity) {
        prefs.edit().putString(KEY_INTENSITY, intensity.name).apply()
        _themeConfig.value = _themeConfig.value.copy(colorIntensity = intensity)
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _themeConfig.value = _themeConfig.value.copy(dynamicColor = enabled)
    }

    fun setFontPreset(fontPreset: FontPreset) {
        prefs.edit().putString(KEY_FONT, fontPreset.name).apply()
        _themeConfig.value = _themeConfig.value.copy(fontPreset = fontPreset)
    }

    fun setCornerRadius(cornerRadius: AppCornerRadius) {
        prefs.edit().putString(KEY_CORNER_RADIUS, cornerRadius.name).apply()
        _themeConfig.value = _themeConfig.value.copy(cornerRadius = cornerRadius)
    }

    fun setFontScale(fontScale: AppFontScale) {
        prefs.edit().putString(KEY_FONT_SCALE, fontScale.name).apply()
        _themeConfig.value = _themeConfig.value.copy(fontScale = fontScale)
    }

    fun setSemanticPalette(semanticPalette: FinancialSemanticPalette) {
        prefs.edit().putString(KEY_SEMANTIC_PALETTE, semanticPalette.name).apply()
        _themeConfig.value = _themeConfig.value.copy(semanticPalette = semanticPalette)
    }

    fun setDarkSurfaceTone(darkSurfaceTone: DarkSurfaceTone) {
        prefs.edit().putString(KEY_DARK_SURFACE_TONE, darkSurfaceTone.name).apply()
        _themeConfig.value = _themeConfig.value.copy(darkSurfaceTone = darkSurfaceTone)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _themeConfig.value = loadConfig()
    }

    private fun serializeCustomThemes(themes: List<CustomTheme>): String {
        val array = org.json.JSONArray()
        themes.forEach { theme ->
            val obj = org.json.JSONObject()
            obj.put("id", theme.id)
            obj.put("name", theme.name)
            obj.put("primary", theme.primaryColorHex)
            if (theme.secondaryColorHex != null) {
                obj.put("secondary", theme.secondaryColorHex)
            }
            if (theme.incomeColorHex != null) {
                obj.put("income", theme.incomeColorHex)
            }
            if (theme.expenseColorHex != null) {
                obj.put("expense", theme.expenseColorHex)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeCustomThemes(json: String?): List<CustomTheme> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<CustomTheme>()
        try {
            val array = org.json.JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val primary = obj.getLong("primary")
                val secondary = if (obj.has("secondary")) obj.getLong("secondary") else null
                val income = if (obj.has("income")) obj.getLong("income") else null
                val expense = if (obj.has("expense")) obj.getLong("expense") else null
                list.add(
                    CustomTheme(
                        id = id,
                        name = name,
                        primaryColorHex = primary,
                        secondaryColorHex = secondary,
                        incomeColorHex = income,
                        expenseColorHex = expense
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    companion object {
        private const val KEY_PALETTE = "theme_palette"
        private const val KEY_SELECTED_CUSTOM_THEME_ID = "selected_custom_theme_id"
        private const val KEY_CUSTOM_THEMES = "custom_themes_list_json"
        private const val KEY_MODE = "theme_mode"
        private const val KEY_INTENSITY = "color_intensity"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color_enabled"
        private const val KEY_FONT = "font_preset"
        private const val KEY_CORNER_RADIUS = "corner_radius"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_SEMANTIC_PALETTE = "semantic_palette"
        private const val KEY_DARK_SURFACE_TONE = "dark_surface_tone"

        @Volatile
        private var instance: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return instance ?: synchronized(this) {
                instance ?: ThemePreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
