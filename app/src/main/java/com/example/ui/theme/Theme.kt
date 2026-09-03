package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
    themeConfig: AppThemeConfig = AppThemeConfig(),
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDarkMode = when (themeConfig.mode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED_NIGHT -> true
    }

    val isAmoled = themeConfig.mode == ThemeMode.AMOLED_NIGHT

    val colorScheme = when {
        themeConfig.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> buildThemeColorScheme(
            palette = themeConfig.palette,
            isDark = isDarkMode,
            isAmoled = isAmoled,
            intensity = themeConfig.colorIntensity
        )
    }

    val typography = createAppTypography(themeConfig.fontPreset.fontFamily)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
