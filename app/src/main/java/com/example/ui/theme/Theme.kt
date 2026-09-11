package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

fun createAppShapes(cornerRadius: AppCornerRadius = AppCornerRadius.STANDARD): Shapes {
    val r = cornerRadius.cornerDp.toFloat()
    return Shapes(
        extraSmall = RoundedCornerShape((r * 0.4f).coerceAtLeast(2f).dp),
        small = RoundedCornerShape((r * 0.65f).coerceAtLeast(3f).dp),
        medium = RoundedCornerShape(r.dp),
        large = RoundedCornerShape((r * 1.35f).dp),
        extraLarge = RoundedCornerShape((r * 1.75f).dp)
    )
}

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
        themeConfig.activeCustomTheme != null -> buildCustomThemeColorScheme(
            customTheme = themeConfig.activeCustomTheme!!,
            isDark = isDarkMode,
            isAmoled = isAmoled,
            intensity = themeConfig.colorIntensity,
            semanticPalette = themeConfig.semanticPalette,
            darkSurfaceTone = themeConfig.darkSurfaceTone
        )
        else -> buildThemeColorScheme(
            palette = themeConfig.palette,
            isDark = isDarkMode,
            isAmoled = isAmoled,
            intensity = themeConfig.colorIntensity,
            semanticPalette = themeConfig.semanticPalette,
            darkSurfaceTone = themeConfig.darkSurfaceTone
        )
    }

    val typography = createAppTypography(
        fontFamily = themeConfig.fontPreset.fontFamily,
        scale = themeConfig.fontScale.scaleFactor
    )

    val shapes = createAppShapes(themeConfig.cornerRadius)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // In light mode (!isDarkMode), status bar and nav bar icons must be dark (isAppearanceLightStatusBars = true)
                insetsController.isAppearanceLightStatusBars = !isDarkMode
                insetsController.isAppearanceLightNavigationBars = !isDarkMode
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
