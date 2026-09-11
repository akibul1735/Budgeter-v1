package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

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

    val context = LocalContext.current
    val view = LocalView.current
    if (!view.isInEditMode) {
        // Status bar background in this app is surface/background.
        // If the surface or background is light (high luminance), icons must be dark (isAppearanceLightStatusBars = true).
        val isLightSurface = colorScheme.surface.luminance() > 0.45f || !isDarkMode
        SideEffect {
            val window = context.findActivity()?.window ?: (view.context.findActivity()?.window)
            if (window != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isStatusBarContrastEnforced = false
                    window.isNavigationBarContrastEnforced = false
                }
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = isLightSurface
                insetsController.isAppearanceLightNavigationBars = isLightSurface
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
