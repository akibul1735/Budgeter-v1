package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemePalette(val displayNameEn: String, val displayNameBn: String, val primaryColor: Color) {
    SOFT_PASTEL("Soft Pastel", "সফট প্যাস্টেল", Color(0xFF7E57C2)),
    PREMIUM_GREEN("Premium Green", "প্রিমিয়াম গ্রিন", Color(0xFF15803D)),
    ELEGANT_BLUE("Elegant Blue", "এলিগ্যান্ট ব্লু", Color(0xFF1D4ED8)),
    WARM_NEUTRAL("Warm Neutral", "ওয়ার্ম নিউট্রাল", Color(0xFFB45309)),
    MODERN_INDIGO("Modern Indigo", "মডার্ন ইন্ডিগো", Color(0xFF4F46E5)),
    CALM_SAGE("Calm Sage", "কাম সেইজ", Color(0xFF3B7A57)),
    SOPHISTICATED_DARK("Sophisticated Dark", "সোফিস্টিকেটেড ডার্ক", Color(0xFF27272A)),
    MINIMAL_MONO("Minimal Monochrome", "মিনিমাল মনোক্রোম", Color(0xFF343A40))
}

enum class ThemeMode(val titleEn: String, val titleBn: String) {
    SYSTEM("Follow System", "সিস্টেম অনুযায়ী"),
    LIGHT("Light Mode", "লাইট মোড"),
    DARK("Dark Mode", "ডার্ক মোড"),
    AMOLED_NIGHT("Pure Night (AMOLED)", "নাইট মোড (AMOLED)")
}

enum class FontPreset(val titleEn: String, val titleBn: String, val fontFamily: FontFamily) {
    DEFAULT("Default System", "সিস্টেম ডিফল্ট", FontFamily.Default),
    SANS_SERIF("Modern Sans", "মডার্ন স্যান্স", FontFamily.SansSerif),
    SERIF("Classic Serif", "ক্লাসিক সেরিফ", FontFamily.Serif),
    MONOSPACE("Tech Monospace", "টেক মনোস্পেস", FontFamily.Monospace),
    CURSIVE("Friendly Script", "হাতে লেখা ফন্ট", FontFamily.Cursive)
}

enum class ColorIntensity(val titleEn: String, val titleBn: String) {
    STANDARD("Standard", "স্বাভাবিক"),
    VIVID("Vivid (High)", "উজ্জ্বল গাঢ়"),
    DEEP_CONTRAST("Deep Contrast", "উচ্চ স্পষ্টতা")
}

data class CustomTheme(
    val id: String,
    val name: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long? = null,
    val isDarkOptimized: Boolean = true
) {
    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = secondaryColorHex?.let { Color(it) } ?: primaryColor
}

data class AppThemeConfig(
    val palette: ThemePalette = ThemePalette.ELEGANT_BLUE,
    val customThemeId: String? = null,
    val customThemes: List<CustomTheme> = emptyList(),
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val colorIntensity: ColorIntensity = ColorIntensity.VIVID,
    val dynamicColor: Boolean = false,
    val fontPreset: FontPreset = FontPreset.DEFAULT
) {
    val activeCustomTheme: CustomTheme?
        get() = customThemes.find { it.id == customThemeId }

    val activeThemeDisplayName: String
        get() = activeCustomTheme?.name ?: palette.displayNameEn
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

        val palette = when (paletteName) {
            "EMERALD" -> ThemePalette.PREMIUM_GREEN
            "SAPPHIRE" -> ThemePalette.ELEGANT_BLUE
            "AMETHYST" -> ThemePalette.SOFT_PASTEL
            "GOLDEN" -> ThemePalette.WARM_NEUTRAL
            "CRIMSON" -> ThemePalette.SOPHISTICATED_DARK
            "TEAL" -> ThemePalette.CALM_SAGE
            "SUNSET" -> ThemePalette.WARM_NEUTRAL
            else -> try { ThemePalette.valueOf(paletteName) } catch (_: Exception) { ThemePalette.ELEGANT_BLUE }
        }
        val mode = try { ThemeMode.valueOf(modeName) } catch (_: Exception) { ThemeMode.SYSTEM }
        val intensity = try { ColorIntensity.valueOf(intensityName) } catch (_: Exception) { ColorIntensity.VIVID }
        val font = try { FontPreset.valueOf(fontName) } catch (_: Exception) { FontPreset.DEFAULT }

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
            fontPreset = font
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

    fun addCustomTheme(name: String, primaryColorHex: Long, secondaryColorHex: Long? = null): CustomTheme {
        val current = _themeConfig.value
        val newTheme = CustomTheme(
            id = "theme_${System.currentTimeMillis()}",
            name = name.trim().ifBlank { "Custom Theme" },
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex
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
                list.add(CustomTheme(id = id, name = name, primaryColorHex = primary, secondaryColorHex = secondary))
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

        @Volatile
        private var instance: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return instance ?: synchronized(this) {
                instance ?: ThemePreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
