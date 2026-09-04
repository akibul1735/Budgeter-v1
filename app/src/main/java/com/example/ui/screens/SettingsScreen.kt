package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewCarousel
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.theme.ColorIntensity
import com.example.ui.theme.FontPreset
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.example.ui.viewmodel.BudgetViewModel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.util.CurrencyConfig
import com.example.util.CurrencyDisplayMode
import com.example.util.CurrencyItem
import com.example.util.CurrencyPreferences
import com.example.util.LanguageHelper

@Composable
fun SettingsScreen(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onNavigateToBackupSync: () -> Unit,
    onOpenTabCustomizer: () -> Unit,
    onOpenThemeFontSettings: () -> Unit,
    onOpenAutofillSettings: () -> Unit
) {
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    var customCodeInput by remember(currencyConfig) { mutableStateOf(currencyConfig.customCode) }
    var customSymbolInput by remember(currencyConfig) { mutableStateOf(currencyConfig.customSymbol) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SolidPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SolidPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও পছন্দসমূহ" else "Settings & Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "থিম, নেভিগেশন, ব্যাকআপ ও সাধারণ কনফিগারেশন" else "Themes, navigation tabs, cloud sync & preferences",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Section 1: Appearance & Theme
        item {
            SettingsCategoryCard(
                title = if (languageMode == LanguageMode.BANGLA) "থিম ও রূপরেখা" else "Appearance & Theme",
                icon = Icons.Default.Palette
            ) {
                // Theme Mode Selector (System, Light, Dark)
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ডিসপ্লে মোড" else "Display Mode",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themeConfig.mode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        label = { Text("System", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeConfig.mode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        label = { Text("Light", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeConfig.mode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        label = { Text("Dark", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Theme Palette Quick Picker
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "কালার প্যালেট" else "Color Palette",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemePalette.values().take(4).forEach { palette ->
                        FilterChip(
                            selected = themeConfig.palette == palette,
                            onClick = { viewModel.setThemePalette(palette) },
                            label = { Text(palette.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Full Theme & Typography Dialog Button
                OutlinedButton(
                    onClick = onOpenThemeFontSettings,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সম্পূর্ণ থিম ও ফন্ট সেটিংস খুলুন" else "Custom Palettes & Typography Settings",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Section 2: Navigation Tabs
        item {
            SettingsCategoryCard(
                title = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন বার সেটিংস" else "Navigation Tabs",
                icon = Icons.Default.ViewCarousel
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ট্যাব অবস্থান ও সক্রিয় ট্যাব কাস্টমাইজ করুন (অ্যাকাউন্টস এবং ক্যাটাগরি মেনু থেকে অ্যাক্সেসযোগ্য)" else "Customize tab bar position and enabled tabs (Accounts and Categories are accessible via the Menu)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenTabCustomizer,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ট্যাব কাস্টমাইজার" else "Customize Navigation Tabs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section 3: Data, Backup & Cloud Sync
        item {
            SettingsCategoryCard(
                title = if (languageMode == LanguageMode.BANGLA) "ডাটা ব্যাকআপ ও ক্লাউড সিঙ্ক" else "Data, Backup & Cloud Sync",
                icon = Icons.Default.CloudSync
            ) {
                SettingsActionTile(
                    title = if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভ ও ব্যাকআপ কেন্দ্র" else "Google Drive, Excel & Backup Center",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "অটো সিঙ্ক, এক্সেল/সিএসভি রপ্তানি ও ডাটাবেজ ব্যাকআপ" else "Google Drive Auto-Sync, Excel/CSV Export & DB Restore",
                    icon = Icons.Default.Backup,
                    onClick = onNavigateToBackupSync
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SettingsActionTile(
                    title = if (languageMode == LanguageMode.BANGLA) "তাত্ক্ষণিক ড্রাইভ সিঙ্ক" else "Instant Cloud JSON Sync",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "ড্রাইভে রিয়েলটাইম ডাটা আপডেট করুন" else "Trigger manual sync to Google Drive cloud",
                    icon = Icons.Default.Sync,
                    onClick = { viewModel.triggerQuickSync() }
                )
            }
        }

        // Section 4: Smart Suggestions & Autofill
        item {
            SettingsCategoryCard(
                title = if (languageMode == LanguageMode.BANGLA) "স্মার্ট অটোফিল ও প্রেডিকশন" else "Smart Autofill & Predictions",
                icon = Icons.Default.AutoAwesome
            ) {
                SettingsActionTile(
                    title = if (languageMode == LanguageMode.BANGLA) "অটোফিল পছন্দসমূহ" else "Autofill & Smart Categorization",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেনের জন্য স্মার্ট ক্যাটাগরি ও পেয়ী পরামর্শ" else "Smart category prediction and payee suggestions",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onOpenAutofillSettings
                )
            }
        }

        // Section 5: Language & Region
        item {
            SettingsCategoryCard(
                title = if (languageMode == LanguageMode.BANGLA) "ভাষা ও আঞ্চলিক সেটিংস" else "Language & Region",
                icon = Icons.Default.Translate
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "অ্যাপের ভাষা নির্বাচন করুন" else "Select Application Language",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = languageMode == LanguageMode.ENGLISH,
                        onClick = { viewModel.setLanguageMode(LanguageMode.ENGLISH) },
                        label = { Text("English", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = languageMode == LanguageMode.BANGLA,
                        onClick = { viewModel.setLanguageMode(LanguageMode.BANGLA) },
                        label = { Text("বাংলা", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = languageMode == LanguageMode.BILINGUAL,
                        onClick = { viewModel.setLanguageMode(LanguageMode.BILINGUAL) },
                        label = { Text("Both", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 6: Currency & Symbol Preferences
        item {
            SettingsCategoryCard(
                title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা ও প্রতীক সেটিংস" else "Currency & Symbol Settings",
                icon = Icons.Default.Payments
            ) {
                // Live preview banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বর্তমান ফরম্যাট প্রিভিউ" else "Current Display Preview",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(45000.00, languageMode),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "${currencyConfig.activeCode} (${currencyConfig.activeSymbol})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Display Mode selection (Symbol only, Code only, Both, Hide)
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "মুদ্রা প্রদর্শনের নিয়ম" else "Display Mode (Show / Hide / Symbol)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CurrencyDisplayMode.values().forEach { mode ->
                        FilterChip(
                            selected = currencyConfig.displayMode == mode,
                            onClick = { viewModel.setCurrencyDisplayMode(mode) },
                            label = {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) mode.titleBn else mode.titleEn,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (currencyConfig.displayMode == mode) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Popular Currencies
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "জনপ্রিয় মুদ্রা নির্বাচন করুন" else "Popular Currencies & Symbols",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CurrencyPreferences.POPULAR_CURRENCIES) { item ->
                        val isSelected = currencyConfig.selectedCode == item.code && currencyConfig.customSymbol.isBlank()
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCurrency(item) },
                            label = {
                                Text(
                                    text = "${item.symbol} ${item.code}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Symbol & Code Fields
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "কাস্টম কোড বা প্রতীক দিন" else "Custom Currency Code / Symbol",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customCodeInput,
                        onValueChange = {
                            customCodeInput = it
                            viewModel.setCustomCurrency(it, customSymbolInput)
                        },
                        label = { Text("Code (e.g. BDT)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customSymbolInput,
                        onValueChange = {
                            customSymbolInput = it
                            viewModel.setCustomCurrency(customCodeInput, it)
                        },
                        label = { Text("Symbol (e.g. ৳)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 7: Demo Mode Environment
        item {
            SettingsCategoryCard(
                title = if (languageMode == LanguageMode.BANGLA) "ডেমো স্যান্ডবক্স ও টেস্ট ডাটা" else "Demo Sandbox & Sample Data",
                icon = Icons.Default.Storage
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ডেমো মোড" else "Demo Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDemoMode) "Active (Real database is isolated)" else "Inactive (Using real live database)",
                            fontSize = 11.sp,
                            color = if (isDemoMode) SolidIncome else MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = { viewModel.setDemoMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SolidIncome
                        )
                    )
                }

                if (isDemoMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetDemoData() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নমুনা ডেমো ডাটা পুনরায় লোড করুন" else "Reset Sample Demo Data",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section 7: App Version info
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Budgeter v1.0 • Offline-First Personal Finance",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SolidPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SolidPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SolidPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
