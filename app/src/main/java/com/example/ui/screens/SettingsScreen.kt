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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.util.CalendarDisplayMode
import com.example.util.CurrencyConfig
import com.example.util.CurrencyDisplayMode
import com.example.util.CurrencyPreferences
import com.example.util.DisplayFormatConfig
import com.example.util.ItemDisplayFormat
import com.example.util.LanguageHelper

private val SectionHeaderColor = Color(0xFF4C7B5D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit = {},
    onNavigateToBackupSync: () -> Unit,
    onOpenTabCustomizer: () -> Unit,
    onOpenThemeFontSettings: () -> Unit,
    onOpenAutofillSettings: () -> Unit
) {
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    val displayFormatConfig by viewModel.displayFormatConfig.collectAsStateWithLifecycle()
    val dashboardConfig by viewModel.dashboardConfig.collectAsStateWithLifecycle()

    // Dialog States
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDateSettingsDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showDataManagementDialog by remember { mutableStateOf(false) }
    var showTransactionSetupDialog by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD700),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "৳",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4A3800)
                                )
                            }
                        }
                        Text(
                            text = LanguageHelper.getString("settings", languageMode).ifEmpty { "Settings" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFaqDialog = true }) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Group 1: Localization
            item {
                SettingsSectionHeader(title = if (languageMode == LanguageMode.BANGLA) "ভাষা ও এলাকা" else "Localization")
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "ভাষা পছন্দ" else "Language Preference",
                    subtitle = when (languageMode) {
                        LanguageMode.ENGLISH -> "English"
                        LanguageMode.BANGLA -> "বাংলা (Bangla)"
                        LanguageMode.BILINGUAL -> "Bilingual (English + বাংলা)"
                    },
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা কনফিগারেশন" else "Currency Setup",
                    subtitle = "${currencyConfig.activeCode} (${currencyConfig.activeSymbol}) • ${if (languageMode == LanguageMode.BANGLA) currencyConfig.displayMode.titleBn else currencyConfig.displayMode.titleEn}",
                    icon = Icons.Default.CurrencyExchange,
                    onClick = { showCurrencyDialog = true }
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "তারিখ ও ক্যালেন্ডার সেটিংস" else "Date Settings",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "ফরম্যাট: dd MMM, yyyy • সপ্তাহের প্রথম দিন: রবিবার" else "Format: dd MMM, yyyy • First day: Sunday",
                    icon = Icons.Default.CalendarToday,
                    onClick = { showDateSettingsDialog = true }
                )
            }

            // Divider
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Group 2: General
            item {
                SettingsSectionHeader(title = if (languageMode == LanguageMode.BANGLA) "সাধারণ সেটিংস" else "General")
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "রূপরেখা কাস্টমাইজেশন" else "Customize Appearance",
                    subtitle = "${themeConfig.mode.name.lowercase().replaceFirstChar { it.uppercase() }} mode • ${themeConfig.palette.name.lowercase().replaceFirstChar { it.uppercase() }} palette • ${if (languageMode == LanguageMode.BANGLA) themeConfig.fontPreset.titleBn else themeConfig.fontPreset.titleEn}",
                    icon = Icons.Default.Palette,
                    onClick = { showAppearanceDialog = true }
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "ডাটা ব্যবস্থাপনা ও ব্যাকআপ" else "Data Management",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "এক্সেল/সিএসভি রপ্তানি, স্থানীয় ব্যাকআপ ও ডেমো মোড" else "Excel/CSV Export, Local DB Backup & Demo Mode",
                    icon = Icons.Default.Storage,
                    onClick = { showDataManagementDialog = true }
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "অনলাইন সিঙ্ক ও ক্লাউড" else "Online Sync",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভ অটোমেটেড ক্লাউড ব্যাকআপ" else "Google Drive Automated Cloud Backup & Sync",
                    icon = Icons.Default.CloudSync,
                    onClick = onNavigateToBackupSync
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "লেনদেন সেটআপ" else "Transaction Setup",
                    subtitle = if (displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.TWO_LINES)
                        "Double-Line Display • Smart Autofill Enabled"
                    else
                        "Single-Line Display • Smart Autofill Enabled",
                    icon = Icons.Default.AddCircleOutline,
                    onClick = { showTransactionSetupDialog = true }
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "ক্যালেন্ডার ও পরিসংখ্যান" else "Calendar",
                    subtitle = if (dashboardConfig.calendarDisplayMode == CalendarDisplayMode.DOTS)
                        "Indicator: Dots • Show Income & Expenses"
                    else
                        "Indicator: Numbers • Show Income & Expenses",
                    icon = Icons.Default.DateRange,
                    onClick = { showCalendarDialog = true }
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "ফোন নোটিফিকেশন" else "Phone Notification",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "দৈনিক হিসাব রিমাইন্ডার ও বিল সতর্কতা" else "Daily expense reminder & bill due alerts",
                    icon = Icons.Default.NotificationsNone,
                    onClick = { showNotificationDialog = true }
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ও বায়োমেট্রিক" else "Password and Fingerprint",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "অ্যাপ সিকিউরিটি লক ও বায়োমেট্রিক সুরক্ষা" else "App lock, PIN protection & biometric security",
                    icon = Icons.Default.Fingerprint,
                    onClick = { showSecurityDialog = true }
                )
            }

            // Divider
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Group 3: About
            item {
                SettingsSectionHeader(title = if (languageMode == LanguageMode.BANGLA) "সম্পর্কে" else "About")
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "বাজেটার সম্পর্কে" else "About Budgeter",
                    subtitle = "Version 3.6 • Offline-First Personal Finance",
                    icon = Icons.Default.Info,
                    onClick = { showAboutDialog = true }
                )
            }

            item {
                SettingsListItem(
                    title = if (languageMode == LanguageMode.BANGLA) "প্রশ্নোত্তর ও সহায়তা" else "FAQ & Support",
                    subtitle = if (languageMode == LanguageMode.BANGLA) "ইউজার গাইড, প্রশ্নোত্তর ও সহায়তা" else "User guide, FAQ & developer feedback",
                    icon = Icons.Default.HelpOutline,
                    onClick = { showFaqDialog = true }
                )
            }
        }
    }

    // 1. Language Preference Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ভাষা নির্বাচন করুন" else "Select Language",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageOptionRow(
                        title = "English",
                        subtitle = "Standard English interface",
                        isSelected = languageMode == LanguageMode.ENGLISH,
                        onClick = {
                            viewModel.setLanguageMode(LanguageMode.ENGLISH)
                            showLanguageDialog = false
                        }
                    )
                    LanguageOptionRow(
                        title = "বাংলা",
                        subtitle = "সম্পূর্ণ বাংলা ইন্টারফেস",
                        isSelected = languageMode == LanguageMode.BANGLA,
                        onClick = {
                            viewModel.setLanguageMode(LanguageMode.BANGLA)
                            showLanguageDialog = false
                        }
                    )
                    LanguageOptionRow(
                        title = "Bilingual (English + বাংলা)",
                        subtitle = "ইংরেজি ও বাংলা যৌথভাবে",
                        isSelected = languageMode == LanguageMode.BILINGUAL,
                        onClick = {
                            viewModel.setLanguageMode(LanguageMode.BILINGUAL)
                            showLanguageDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 2. Currency Setup Dialog
    if (showCurrencyDialog) {
        var customCodeInput by remember(currencyConfig) { mutableStateOf(currencyConfig.customCode) }
        var customSymbolInput by remember(currencyConfig) { mutableStateOf(currencyConfig.customSymbol) }

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "মুদ্রা ও প্রতীক নির্বাচন" else "Currency Setup",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Preview
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Preview", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    text = LanguageHelper.formatCurrency(54000.0, languageMode),
                                    fontSize = 17.sp,
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

                    // Display Mode
                    Text("Display Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                        fontSize = 10.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Popular
                    Text("Popular Currencies", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CurrencyPreferences.POPULAR_CURRENCIES) { item ->
                            val isSelected = currencyConfig.selectedCode == item.code && currencyConfig.customSymbol.isBlank()
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCurrency(item) },
                                label = { Text("${item.symbol} ${item.code}", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Custom Code & Symbol
                    Text("Custom Code & Symbol", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customCodeInput,
                            onValueChange = {
                                customCodeInput = it
                                viewModel.setCustomCurrency(it, customSymbolInput)
                            },
                            label = { Text("Code (BDT)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customSymbolInput,
                            onValueChange = {
                                customSymbolInput = it
                                viewModel.setCustomCurrency(customCodeInput, it)
                            },
                            label = { Text("Symbol (৳)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCurrencyDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // 3. Date Settings Dialog
    if (showDateSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showDateSettingsDialog = false },
            title = { Text("Date & Calendar Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Date Format", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("dd MMM, yyyy (e.g. 05 Sep, 2026)", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("First Day of the Week", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text("Sunday", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text("Monday", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text("Saturday", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDateSettingsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // 4. Customize Appearance Dialog
    if (showAppearanceDialog) {
        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = { Text("Customize Appearance", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Display Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeConfig.mode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                            label = { Text("System", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeConfig.mode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                            label = { Text("Light", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeConfig.mode == ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                            label = { Text("Dark", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Color Palette", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(ThemePalette.values()) { palette ->
                            FilterChip(
                                selected = themeConfig.palette == palette,
                                onClick = { viewModel.setThemePalette(palette) },
                                label = { Text(palette.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedButton(
                        onClick = {
                            showAppearanceDialog = false
                            onOpenThemeFontSettings()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fonts & Color Intensities", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            showAppearanceDialog = false
                            onOpenTabCustomizer()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Customize Navigation Tabs", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAppearanceDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // 5. Data Management Dialog
    if (showDataManagementDialog) {
        AlertDialog(
            onDismissRequest = { showDataManagementDialog = false },
            title = { Text("Data Management", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Demo Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Demo Sandbox Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (isDemoMode) "Sample data active (Isolated)" else "Real database in use",
                                fontSize = 11.sp,
                                color = if (isDemoMode) SolidIncome else MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = isDemoMode,
                            onCheckedChange = { viewModel.setDemoMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SolidIncome)
                        )
                    }

                    if (isDemoMode) {
                        OutlinedButton(
                            onClick = { viewModel.resetDemoData() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Sample Demo Data", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Button(
                        onClick = {
                            showDataManagementDialog = false
                            onNavigateToBackupSync()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Drive & Backup Center", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataManagementDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 6. Transaction Setup Dialog
    if (showTransactionSetupDialog) {
        AlertDialog(
            onDismissRequest = { showTransactionSetupDialog = false },
            title = { Text("Transaction Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Item & Category Display Format", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.TWO_LINES,
                            onClick = { viewModel.setItemDisplayFormat(ItemDisplayFormat.TWO_LINES) },
                            label = { Text("Double-Line", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.SINGLE_LINE,
                            onClick = { viewModel.setItemDisplayFormat(ItemDisplayFormat.SINGLE_LINE) },
                            label = { Text("Single-Line", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedButton(
                        onClick = {
                            showTransactionSetupDialog = false
                            onOpenAutofillSettings()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Smart Autofill & Categorization", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showTransactionSetupDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // 7. Calendar Dialog
    if (showCalendarDialog) {
        AlertDialog(
            onDismissRequest = { showCalendarDialog = false },
            title = { Text("Calendar Display Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Display Mode", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = dashboardConfig.calendarDisplayMode == CalendarDisplayMode.DOTS,
                            onClick = { viewModel.setCalendarSettings(CalendarDisplayMode.DOTS, dashboardConfig.calendarShowIncome, dashboardConfig.calendarShowExpense) },
                            label = { Text("Dots Indicator", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = dashboardConfig.calendarDisplayMode == CalendarDisplayMode.AMOUNTS,
                            onClick = { viewModel.setCalendarSettings(CalendarDisplayMode.AMOUNTS, dashboardConfig.calendarShowIncome, dashboardConfig.calendarShowExpense) },
                            label = { Text("Amount Badges", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCalendarDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // 8. Notification Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Phone Notifications", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily Expense Reminder (9:00 PM)", fontSize = 13.sp)
                        Switch(checked = true, onCheckedChange = {})
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bill Due Reminders", fontSize = 13.sp)
                        Switch(checked = true, onCheckedChange = {})
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // 9. Security Dialog
    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = { Text("Password & Fingerprint", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable App Lock (PIN / Biometrics)", fontSize = 13.sp)
                        Switch(checked = false, onCheckedChange = {})
                    }
                    Text(
                        text = "Secure your financial data with device screen lock or fingerprint authentication.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showSecurityDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // 10. About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("৳", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A3800))
                        }
                    }
                    Text("Budgeter v3.6", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Budgeter is a high-performance, offline-first personal financial manager built with double-entry accounting precision.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Double-entry transaction architecture\n• Real-time account balance tracking & transfer legs\n• Live interactive calculator with quick percentages\n• Google Drive sync & automated backups\n• Zero third-party ad tracking",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 11. FAQ & Support Dialog
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text("FAQ & Support", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("How does double-entry work?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Every transaction records a debit and credit leg across accounts and categories, ensuring zero discrepancies.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)

                    Text("Where is my data stored?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("All data is stored offline on your device and can be backed up to your personal Google Drive account.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            },
            confirmButton = {
                Button(onClick = { showFaqDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = SectionHeaderColor,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsListItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (isSelected) SolidPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) SolidPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SolidPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
