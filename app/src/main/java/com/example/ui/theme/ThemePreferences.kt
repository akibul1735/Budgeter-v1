package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemePalette(val displayNameEn: String, val displayNameBn: String, val primaryColor: Color) {
    EMERALD("Emerald Forest", "পান্না সবুজ", Color(0xFF059669)),
    SAPPHIRE("Sapphire Ocean", "নীলকান্তমণি", Color(0xFF2563EB)),
    AMETHYST("Royal Amethyst", "রাজকীয় বেগুনী", Color(0xFF7C3AED)),
    GOLDEN("Golden Amber", "সোনালী অম্বর", Color(0xFFD97706)),
    CRIMSON("Crimson Ruby", "রক্তিম চুনী", Color(0xFFDC2626)),
    TEAL("Teal Nordic", "নর্ডিক টিল", Color(0xFF0D9488)),
    SUNSET("Sunset Coral", "সূর্যাস্ত কোরাল", Color(0xFFEA580C))
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

data class AppThemeConfig(
    val palette: ThemePalette = ThemePalette.SAPPHIRE,
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val colorIntensity: ColorIntensity = ColorIntensity.VIVID,
    val dynamicColor: Boolean = false,
    val fontPreset: FontPreset = FontPreset.DEFAULT
)

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_theme_prefs", Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<AppThemeConfig> = _themeConfig.asStateFlow()

    private fun loadConfig(): AppThemeConfig {
        val paletteName = prefs.getString(KEY_PALETTE, ThemePalette.SAPPHIRE.name) ?: ThemePalette.SAPPHIRE.name
        val modeName = prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val intensityName = prefs.getString(KEY_INTENSITY, ColorIntensity.VIVID.name) ?: ColorIntensity.VIVID.name
        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        val fontName = prefs.getString(KEY_FONT, FontPreset.DEFAULT.name) ?: FontPreset.DEFAULT.name

        val palette = try { ThemePalette.valueOf(paletteName) } catch (_: Exception) { ThemePalette.SAPPHIRE }
        val mode = try { ThemeMode.valueOf(modeName) } catch (_: Exception) { ThemeMode.SYSTEM }
        val intensity = try { ColorIntensity.valueOf(intensityName) } catch (_: Exception) { ColorIntensity.VIVID }
        val font = try { FontPreset.valueOf(fontName) } catch (_: Exception) { FontPreset.DEFAULT }

        return AppThemeConfig(
            palette = palette,
            mode = mode,
            colorIntensity = intensity,
            dynamicColor = dynamicColor,
            fontPreset = font
        )
    }

    fun setPalette(palette: ThemePalette) {
        prefs.edit().putString(KEY_PALETTE, palette.name).apply()
        _themeConfig.value = _themeConfig.value.copy(palette = palette)
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

    companion object {
        private const val KEY_PALETTE = "theme_palette"
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
