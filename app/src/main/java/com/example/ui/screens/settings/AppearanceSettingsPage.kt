package com.example.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.AppCoinEmblem
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.AppCornerRadius
import com.example.ui.theme.ColorIntensity
import com.example.ui.theme.DarkSurfaceTone
import com.example.ui.theme.FontPreset
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.AvailableAppIcons
import com.example.util.CustomAppIcon

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onNavigateToCustomIcon: () -> Unit = {},
    onBack: () -> Unit
) {
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val currentAppIcon by viewModel.currentAppIcon.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("appearance_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "থিম ও রূপরেখা" else "Themes & Appearance",
            tabIcon = Icons.Default.Palette,
            onBack = onBack,
            autoHideOnScroll = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Display Mode Section
            Text(
                text = if (isBangla) "ডিসপ্লে মোড" else "Display Mode",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    Triple(ThemeMode.SYSTEM, "Auto", if (isBangla) "অটো" else "Auto"),
                    Triple(ThemeMode.LIGHT, "Light", if (isBangla) "লাইট" else "Light"),
                    Triple(ThemeMode.DARK, "Dark", if (isBangla) "ডার্ক" else "Dark"),
                    Triple(ThemeMode.AMOLED_NIGHT, "AMOLED", if (isBangla) "অ্যামোলেড" else "AMOLED")
                )
                modes.forEach { (mode, _, title) ->
                    val isSelected = themeConfig.mode == mode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setThemeMode(mode) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.AMOLED_NIGHT -> Icons.Default.Nightlight
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Color Palette
            Text(
                text = if (isBangla) "কালার প্যালেট ও ব্র্যান্ডিং" else "Primary Color Palette",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemePalette.values().forEach { palette ->
                    val isSelected = themeConfig.palette == palette
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setThemePalette(palette) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(palette.primaryColor, CircleShape)
                            )
                        },
                        label = {
                            Text(
                                text = palette.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Dark Surface Tone
            if (themeConfig.mode == ThemeMode.DARK || themeConfig.mode == ThemeMode.AMOLED_NIGHT) {
                Text(
                    text = if (isBangla) "ডার্ক ব্যাকগ্রাউন্ড টোন" else "Dark Background Tone",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DarkSurfaceTone.values().forEach { tone ->
                        val isSelected = themeConfig.darkSurfaceTone == tone
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDarkSurfaceTone(tone) },
                            label = {
                                Text(
                                    text = if (isBangla) tone.titleBn else tone.titleEn,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Color Intensity
            Text(
                text = if (isBangla) "রঙের উজ্জ্বলতা ও তীব্রতা" else "Color Intensity",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorIntensity.values().forEach { intensity ->
                    val isSelected = themeConfig.colorIntensity == intensity
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setColorIntensity(intensity) },
                        label = {
                            Text(
                                text = if (isBangla) intensity.titleBn else intensity.titleEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Typography & Font Preset
            Text(
                text = if (isBangla) "টাইপোগ্রাফি ও ফন্ট" else "Typography & Font Preset",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FontPreset.values().forEach { preset ->
                    val isSelected = themeConfig.fontPreset == preset
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setFontPreset(preset) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) preset.titleBn else preset.titleEn,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "৳ 54,000.00 • Double-Entry Accounting",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Corner Radius
            Text(
                text = if (isBangla) "কার্ডের প্রান্তের গোলাকার মাত্রা" else "Corner Radius Style",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppCornerRadius.values().forEach { radius ->
                    val isSelected = themeConfig.cornerRadius == radius
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setThemeCornerRadius(radius) },
                        label = {
                            Text(
                                text = if (isBangla) radius.titleBn else radius.titleEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(radius.cornerDp.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Dynamic Material You (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "ম্যাটেরিয়াল ইউ ডায়নামিক রঙ" else "Material You Dynamic Color",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBangla) "ওয়ালপেপার থেকে স্বয়ংক্রিয়ভাবে রঙ নির্বাচন" else "Use wallpaper-derived dynamic colors",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = themeConfig.dynamicColor,
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }
                }
            }

            // Custom App Icon Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isBangla) "অ্যাপ লঞ্চার আইকন" else "App Launcher Icon",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (isBangla) "সব আইকন দেখুন →" else "View all →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onNavigateToCustomIcon() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = if (isBangla) "হোম স্ক্রিনের জন্য বাংলাদেশি টাকা (৳) কয়েন আইকন নির্বাচন করুন:" else "Choose your home screen Bangladesh Taka (৳) coin emblem:",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    items(AvailableAppIcons.allIcons, key = { it.id }) { icon ->
                        val isSelected = icon.id == currentAppIcon.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = if (isSelected)
                                androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            else null,
                            modifier = Modifier
                                .width(94.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setCustomAppIcon(icon) }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AppCoinEmblem(
                                        icon = icon,
                                        size = 38.dp,
                                        elevation = if (isSelected) 3.dp else 1.dp
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(16.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isBangla) icon.titleBn.substringBefore(" ") else icon.titleEn.substringBefore(" ৳"),
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Reset Defaults Button
            OutlinedButton(
                onClick = { viewModel.resetThemePreferences() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBangla) "ডিফল্ট থিম ফিরিয়ে আনুন" else "Reset Theme to Defaults", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
