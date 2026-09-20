package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemePalette(
    val displayNameEn: String,
    val displayNameBn: String,
    val primaryColor: Color,
    val isNightTheme: Boolean = false
) {
    // 3 Day Themes
    EMERALD_WEALTH("Emerald Mint", "এমারেল্ড মিন্ট", Color(0xFF059669), false),
    ROYAL_SAPPHIRE("Ocean Sapphire", "ওশেন স্যাফায়ার", Color(0xFF1D4ED8), false),
    WARM_AMBER("Warm Amber", "ওয়ার্ম অ্যাম্বার", Color(0xFFB45309), false),

    // 2 Night Themes
    MIDNIGHT_SLATE("Midnight Slate", "মিডনাইট স্লেট", Color(0xFF38BDF8), true),
    OBSIDIAN_NIGHT("Obsidian OLED", "অবসিডিয়ান ওলেড", Color(0xFF10B981), true)
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
        "Obsidian Slate (Default)",
        "অবসিডিয়ান স্লেট (ডিফল্ট)",
        Color(0xFF0A0D14),
        Color(0xFF131824),
        Color(0xFF1C2333)
    ),
    MIDNIGHT_INDIGO(
        "Midnight Cobalt Deep",
        "মিডনাইট কোবাল্ট",
        Color(0xFF07090F),
        Color(0xFF0E1320),
        Color(0xFF161E30)
    ),
    WARM_CHARCOAL(
        "Titanium Charcoal Luxe",
        "টাইটানিয়াম চারকোল",
        Color(0xFF0D0D10),
        Color(0xFF16161B),
        Color(0xFF22222A)
    ),
    AMOLED_PITCH(
        "Pure OLED Pitch Black",
        "পিওর ওলেড ব্ল্যাক",
        Color(0xFF000000),
        Color(0xFF0D0E12),
        Color(0xFF161820)
    )
}

data class CustomTheme(
    val id: String,
    val name: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long? = null,
    val surfaceColorHex: Long? = null,
    val headerColorHex: Long? = null,
    val incomeColorHex: Long? = null,
    val expenseColorHex: Long? = null,
    val transferColorHex: Long? = null,
    val shadeIntensity: Int = 100,
    val isDarkOptimized: Boolean = true
) {
    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = secondaryColorHex?.let { Color(it) } ?: primaryColor
    val customSurfaceColor: Color? get() = surfaceColorHex?.let { Color(it) }
    val customHeaderColor: Color? get() = headerColorHex?.let { Color(it) }
    val customIncomeColor: Color? get() = incomeColorHex?.let { Color(it) }
    val customExpenseColor: Color? get() = expenseColorHex?.let { Color(it) }
    val customTransferColor: Color? get() = transferColorHex?.let { Color(it) }
}

data class AppThemeConfig(
    val palette: ThemePalette = ThemePalette.EMERALD_WEALTH,
    val dayPalette: ThemePalette = ThemePalette.EMERALD_WEALTH,
    val nightPalette: ThemePalette = ThemePalette.MIDNIGHT_SLATE,
    val customThemeId: String? = null,
    val customThemes: List<CustomTheme> = emptyList(),
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val colorIntensity: ColorIntensity = ColorIntensity.STANDARD,
    val dynamicColor: Boolean = false,
    val fontPreset: FontPreset = FontPreset.DEFAULT,
    val cornerRadius: AppCornerRadius = AppCornerRadius.STANDARD,
    val fontScale: AppFontScale = AppFontScale.DEFAULT,
    val semanticPalette: FinancialSemanticPalette = FinancialSemanticPalette.CLASSIC,
    val darkSurfaceTone: DarkSurfaceTone = DarkSurfaceTone.SLATE_BLUE
) {
    val activeCustomTheme: CustomTheme?
        get() = customThemes.find { it.id == customThemeId }

    fun effectivePalette(isDarkMode: Boolean): ThemePalette {
        return when (mode) {
            ThemeMode.SYSTEM -> if (isDarkMode) nightPalette else dayPalette
            ThemeMode.LIGHT -> if (palette.isNightTheme) dayPalette else palette
            ThemeMode.DARK, ThemeMode.AMOLED_NIGHT -> if (!palette.isNightTheme) nightPalette else palette
        }
    }

    fun effectiveThemeDisplayName(isDarkMode: Boolean, isBangla: Boolean = false): String {
        val custom = activeCustomTheme
        if (custom != null) return custom.name
        val pal = effectivePalette(isDarkMode)
        return if (isBangla) pal.displayNameBn else pal.displayNameEn
    }

    val activeThemeDisplayName: String
        get() = activeCustomTheme?.name ?: palette.displayNameEn

    val activeIncomeColor: Color
        get() = activeCustomTheme?.customIncomeColor ?: semanticPalette.incomeColor

    val activeExpenseColor: Color
        get() = activeCustomTheme?.customExpenseColor ?: semanticPalette.expenseColor

    val activeTransferColor: Color
        get() = activeCustomTheme?.customTransferColor ?: semanticPalette.transferColor
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_theme_prefs", Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<AppThemeConfig> = _themeConfig.asStateFlow()

    private fun loadConfig(): AppThemeConfig {
        val paletteName = prefs.getString(KEY_PALETTE, ThemePalette.EMERALD_WEALTH.name) ?: ThemePalette.EMERALD_WEALTH.name
        val selectedCustomThemeId = prefs.getString(KEY_SELECTED_CUSTOM_THEME_ID, null)
        val customThemesJson = prefs.getString(KEY_CUSTOM_THEMES, null)
        val customThemes = deserializeCustomThemes(customThemesJson)

        val modeName = prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val intensityName = prefs.getString(KEY_INTENSITY, ColorIntensity.STANDARD.name) ?: ColorIntensity.STANDARD.name
        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        val fontName = prefs.getString(KEY_FONT, FontPreset.DEFAULT.name) ?: FontPreset.DEFAULT.name
        val cornerRadiusName = prefs.getString(KEY_CORNER_RADIUS, AppCornerRadius.STANDARD.name) ?: AppCornerRadius.STANDARD.name
        val fontScaleName = prefs.getString(KEY_FONT_SCALE, AppFontScale.DEFAULT.name) ?: AppFontScale.DEFAULT.name
        val semanticPaletteName = prefs.getString(KEY_SEMANTIC_PALETTE, FinancialSemanticPalette.CLASSIC.name) ?: FinancialSemanticPalette.CLASSIC.name
        val darkSurfaceToneName = prefs.getString(KEY_DARK_SURFACE_TONE, DarkSurfaceTone.SLATE_BLUE.name) ?: DarkSurfaceTone.SLATE_BLUE.name

        val palette = when (paletteName) {
            "EMERALD_WEALTH", "PREMIUM_GREEN", "CALM_SAGE", "EMERALD", "TEAL" -> ThemePalette.EMERALD_WEALTH
            "ROYAL_SAPPHIRE", "ELEGANT_BLUE", "MODERN_INDIGO", "CYAN_BREEZE", "SAPPHIRE" -> ThemePalette.ROYAL_SAPPHIRE
            "WARM_AMBER", "WARM_NEUTRAL", "SUNSET_ORANGE", "SOFT_PASTEL", "CRIMSON_ROYAL", "DEEP_PURPLE", "GOLDEN", "CRIMSON", "AMETHYST", "SUNSET" -> ThemePalette.WARM_AMBER
            "MIDNIGHT_SLATE", "SOPHISTICATED_DARK" -> ThemePalette.MIDNIGHT_SLATE
            "OBSIDIAN_NIGHT", "MINIMAL_MONO" -> ThemePalette.OBSIDIAN_NIGHT
            else -> try { ThemePalette.valueOf(paletteName) } catch (_: Exception) { ThemePalette.EMERALD_WEALTH }
        }

        val dayPaletteName = prefs.getString(KEY_DAY_PALETTE, null)
        val nightPaletteName = prefs.getString(KEY_NIGHT_PALETTE, null)
        val dayPalette = dayPaletteName?.let {
            try {
                val p = ThemePalette.valueOf(it)
                if (!p.isNightTheme) p else null
            } catch (_: Exception) { null }
        } ?: if (!palette.isNightTheme) palette else ThemePalette.EMERALD_WEALTH

        val nightPalette = nightPaletteName?.let {
            try {
                val p = ThemePalette.valueOf(it)
                if (p.isNightTheme) p else null
            } catch (_: Exception) { null }
        } ?: if (palette.isNightTheme) palette else ThemePalette.MIDNIGHT_SLATE

        val mode = try { ThemeMode.valueOf(modeName) } catch (_: Exception) { ThemeMode.SYSTEM }
        val intensity = try { ColorIntensity.valueOf(intensityName) } catch (_: Exception) { ColorIntensity.STANDARD }
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
            dayPalette = dayPalette,
            nightPalette = nightPalette,
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
        val isNight = palette.isNightTheme
        val editor = prefs.edit()
            .putString(KEY_PALETTE, palette.name)
            .remove(KEY_SELECTED_CUSTOM_THEME_ID)
        if (isNight) {
            editor.putString(KEY_NIGHT_PALETTE, palette.name)
        } else {
            editor.putString(KEY_DAY_PALETTE, palette.name)
        }
        editor.apply()
        val current = _themeConfig.value
        _themeConfig.value = current.copy(
            palette = palette,
            dayPalette = if (!isNight) palette else current.dayPalette,
            nightPalette = if (isNight) palette else current.nightPalette,
            customThemeId = null
        )
    }

    fun setDayPalette(palette: ThemePalette) {
        prefs.edit().putString(KEY_DAY_PALETTE, palette.name).apply()
        val current = _themeConfig.value
        _themeConfig.value = current.copy(
            dayPalette = palette,
            palette = if (current.mode == ThemeMode.LIGHT || (current.mode == ThemeMode.SYSTEM && !current.palette.isNightTheme)) palette else current.palette
        )
    }

    fun setNightPalette(palette: ThemePalette) {
        prefs.edit().putString(KEY_NIGHT_PALETTE, palette.name).apply()
        val current = _themeConfig.value
        _themeConfig.value = current.copy(
            nightPalette = palette,
            palette = if (current.mode == ThemeMode.DARK || current.mode == ThemeMode.AMOLED_NIGHT || (current.mode == ThemeMode.SYSTEM && current.palette.isNightTheme)) palette else current.palette
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
        surfaceColorHex: Long? = null,
        headerColorHex: Long? = null,
        incomeColorHex: Long? = null,
        expenseColorHex: Long? = null,
        transferColorHex: Long? = null,
        shadeIntensity: Int = 100
    ): CustomTheme {
        val current = _themeConfig.value
        val newTheme = CustomTheme(
            id = "theme_${System.currentTimeMillis()}",
            name = name.trim().ifBlank { "Custom Theme" },
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            surfaceColorHex = surfaceColorHex,
            headerColorHex = headerColorHex,
            incomeColorHex = incomeColorHex,
            expenseColorHex = expenseColorHex,
            transferColorHex = transferColorHex,
            shadeIntensity = shadeIntensity
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
            .putString(KEY_DAY_PALETTE, config.dayPalette.name)
            .putString(KEY_NIGHT_PALETTE, config.nightPalette.name)
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
            if (theme.surfaceColorHex != null) {
                obj.put("surface", theme.surfaceColorHex)
            }
            if (theme.headerColorHex != null) {
                obj.put("header", theme.headerColorHex)
            }
            if (theme.incomeColorHex != null) {
                obj.put("income", theme.incomeColorHex)
            }
            if (theme.expenseColorHex != null) {
                obj.put("expense", theme.expenseColorHex)
            }
            if (theme.transferColorHex != null) {
                obj.put("transfer", theme.transferColorHex)
            }
            obj.put("shade", theme.shadeIntensity)
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
                val surface = if (obj.has("surface")) obj.getLong("surface") else null
                val header = if (obj.has("header")) obj.getLong("header") else null
                val income = if (obj.has("income")) obj.getLong("income") else null
                val expense = if (obj.has("expense")) obj.getLong("expense") else null
                val transfer = if (obj.has("transfer")) obj.getLong("transfer") else null
                val shade = if (obj.has("shade")) obj.getInt("shade") else 100
                list.add(
                    CustomTheme(
                        id = id,
                        name = name,
                        primaryColorHex = primary,
                        secondaryColorHex = secondary,
                        surfaceColorHex = surface,
                        headerColorHex = header,
                        incomeColorHex = income,
                        expenseColorHex = expense,
                        transferColorHex = transfer,
                        shadeIntensity = shade
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    companion object {
        private const val KEY_PALETTE = "theme_palette"
        private const val KEY_DAY_PALETTE = "day_theme_palette"
        private const val KEY_NIGHT_PALETTE = "night_theme_palette"
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
