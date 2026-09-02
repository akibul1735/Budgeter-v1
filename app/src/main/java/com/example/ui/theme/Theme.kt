package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SolidPrimary,
    onPrimary = Color.White,
    primaryContainer = SolidPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = SolidTransfer,
    onSecondary = Color.White,
    secondaryContainer = SolidDarkSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = SolidIncome,
    background = SolidDarkBg,
    onBackground = SolidDarkTextPrimary,
    surface = SolidDarkSurface,
    onSurface = SolidDarkTextPrimary,
    surfaceVariant = SolidDarkSurfaceVariant,
    onSurfaceVariant = SolidDarkTextSecondary,
    outline = SolidDarkBorder,
    error = SolidExpense,
    onError = Color.White,
    errorContainer = SolidExpenseContainer,
    onErrorContainer = SolidOnExpenseContainer
)

private val LightColorScheme = lightColorScheme(
    primary = SolidPrimary,
    onPrimary = Color.White,
    primaryContainer = SolidPrimaryContainer,
    onPrimaryContainer = SolidOnPrimaryContainer,
    secondary = SolidTransfer,
    onSecondary = Color.White,
    secondaryContainer = SolidTransferContainer,
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = SolidIncome,
    background = SolidLightBg,
    onBackground = SolidLightTextPrimary,
    surface = SolidLightSurface,
    onSurface = SolidLightTextPrimary,
    surfaceVariant = SolidLightSurfaceVariant,
    onSurfaceVariant = SolidLightTextSecondary,
    outline = SolidLightBorder,
    error = SolidExpense,
    onError = Color.White,
    errorContainer = SolidExpenseContainer,
    onErrorContainer = SolidOnExpenseContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
