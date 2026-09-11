package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.dialogs.SecurityAuthDialog
import com.example.ui.screens.settings.AboutSettingsPage
import com.example.ui.screens.settings.AppearanceSettingsPage
import com.example.ui.screens.settings.CalendarSettingsPage
import com.example.ui.screens.settings.CurrencySettingsPage
import com.example.ui.screens.settings.DateSettingsPage
import com.example.ui.screens.settings.FaqSettingsPage
import com.example.ui.screens.settings.LanguageSettingsPage
import com.example.ui.screens.settings.NavigationTabsSettingsPage
import com.example.ui.screens.settings.NotificationSettingsPage
import com.example.ui.screens.settings.SecuritySettingsPage
import com.example.ui.screens.settings.SmartAutofillSettingsPage
import com.example.ui.screens.settings.TransactionSetupSettingsPage
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.CalendarDisplayMode
import com.example.util.DateFormatOption
import com.example.util.ItemDisplayFormat
import com.example.util.LanguageHelper

private val SectionHeaderColor = Color(0xFF4C7B5D)

enum class SettingsSubPage {
    ROOT,
    PAYMENT_SOURCES,
    LANGUAGE,
    CURRENCY,
    AMOUNT_FORMAT,
    DATE_TIME,
    THEMES,
    NAVIGATION_TABS,
    TRANSACTION_SETUP,
    SMART_AUTOFILL,
    CALENDAR,
    NOTIFICATIONS,
    SECURITY,
    ABOUT,
    FAQ
}

@Composable
fun SettingsScreen(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateToBackupSync: () -> Unit,
    onNavigateToReset: () -> Unit = {},
    onOpenTabCustomizer: () -> Unit = {},
    onOpenThemeFontSettings: () -> Unit = {},
    onOpenAutofillSettings: () -> Unit = {}
) {
    var currentSubPage by remember { mutableStateOf(SettingsSubPage.ROOT) }
    var showSecurityAuthDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    val rootListState = rememberLazyListState()

    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    val amountFormatConfig by viewModel.amountFormatConfig.collectAsStateWithLifecycle()
    val displayFormatConfig by viewModel.displayFormatConfig.collectAsStateWithLifecycle()
    val dashboardConfig by viewModel.dashboardConfig.collectAsStateWithLifecycle()
    val securityConfig by viewModel.securityConfig.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()

    // Handle back presses when inside sub-pages
    if (currentSubPage != SettingsSubPage.ROOT) {
        BackHandler(enabled = true) {
            currentSubPage = SettingsSubPage.ROOT
        }
    }

    if (showSecurityAuthDialog) {
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "নিরাপত্তা নিশ্চিতকরণ" else "Security Authentication",
            message = if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ও ফিঙ্গারপ্রিন্ট সেটিংসে প্রবেশ করতে আপনার পিন বা বায়োমেট্রিক দিন।" else "Please authenticate with your PIN or biometric to access Security settings.",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "প্রবেশ করুন" else "Authenticate",
            requiresAuth = true,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { pin -> viewModel.verifySecurityPin(pin) },
            onConfirm = {
                showSecurityAuthDialog = false
                currentSubPage = SettingsSubPage.SECURITY
            },
            onDismiss = {
                showSecurityAuthDialog = false
            }
        )
    }

    when (currentSubPage) {
        SettingsSubPage.PAYMENT_SOURCES -> {
            com.example.ui.screens.settings.PaymentSourcesSettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.LANGUAGE -> {
            LanguageSettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.CURRENCY -> {
            CurrencySettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.AMOUNT_FORMAT -> {
            com.example.ui.screens.settings.AmountFormatSettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.DATE_TIME -> {
            DateSettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.THEMES -> {
            AppearanceSettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.NAVIGATION_TABS -> {
            NavigationTabsSettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.TRANSACTION_SETUP -> {
            TransactionSetupSettingsPage(
                viewModel = viewModel,
                accounts = accounts,
                categories = categories,
                languageMode = languageMode,
                onNavigateToAutofill = { currentSubPage = SettingsSubPage.SMART_AUTOFILL },
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.SMART_AUTOFILL -> {
            SmartAutofillSettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.TRANSACTION_SETUP }
            )
        }
        SettingsSubPage.CALENDAR -> {
            CalendarSettingsPage(
                viewModel = viewModel,
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.NOTIFICATIONS -> {
            NotificationSettingsPage(
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.SECURITY -> {
            SecuritySettingsPage(
                securityConfig = securityConfig,
                languageMode = languageMode,
                onSetAppLockEnabled = { viewModel.setAppLockEnabled(it) },
                onSetPin = { viewModel.setSecurityPin(it) },
                onVerifyPin = { viewModel.verifySecurityPin(it) },
                onSetBiometricEnabled = { viewModel.setBiometricEnabled(it) },
                onSetRequireAuthForGroupDeletion = { viewModel.setRequireAuthForGroupDeletion(it) },
                onSetRequireAuthForMultiSelect = { viewModel.setRequireAuthForMultiSelect(it) },
                onSetRequireAuthForTrashClear = { viewModel.setRequireAuthForTrashClear(it) },
                onSetRequireAuthForBackupRestore = { viewModel.setRequireAuthForBackupRestore(it) },
                onSetRequireAuthForBackupDeletion = { viewModel.setRequireAuthForBackupDeletion(it) },
                onSetLockTimeoutSeconds = { viewModel.setLockTimeoutSeconds(it) },
                onSetSecurityRecovery = { q, a -> viewModel.setSecurityRecovery(q, a) },
                onVerifySecurityAnswer = { viewModel.verifySecurityAnswer(it) },
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.ABOUT -> {
            AboutSettingsPage(
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.FAQ -> {
            FaqSettingsPage(
                languageMode = languageMode,
                onBack = { currentSubPage = SettingsSubPage.ROOT }
            )
        }
        SettingsSubPage.ROOT -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .testTag("settings_screen")
            ) {
                // Header with Menu Button, Icon, and text "Settings"
                AppTabHeader(
                    title = LanguageHelper.getString("settings", languageMode).ifEmpty { "Settings" },
                    tabIcon = Icons.Default.Settings,
                    showCoinIcon = false,
                    onOpenDrawer = onOpenDrawer,
                    onBack = null,
                    actions = {
                        IconButton(
                            onClick = { currentSubPage = SettingsSubPage.FAQ },
                            modifier = Modifier.testTag("settings_faq_button")
                        ) {
                            Icon(
                                Icons.Default.HelpOutline,
                                contentDescription = "Help",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { showSettingsMenu = true },
                                modifier = Modifier.testTag("settings_menu_button")
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Settings Menu",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showSettingsMenu,
                                onDismissRequest = { showSettingsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "প্রধান মেনু খুলুন" else "Open Navigation Menu") },
                                    leadingIcon = { Icon(Icons.Default.Menu, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        onOpenDrawer()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "ডাটা ব্যবস্থাপনা ও ব্যাকআপ" else "Data Management & Sync") },
                                    leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        onNavigateToBackupSync()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ও ফিঙ্গারপ্রিন্ট" else "Security & App Lock") },
                                    leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        if (securityConfig.isAppLockEnabled || securityConfig.hasPin || securityConfig.isBiometricEnabled) {
                                            showSecurityAuthDialog = true
                                        } else {
                                            currentSubPage = SettingsSubPage.SECURITY
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "থিম ও রূপরেখা" else "Themes & Appearance") },
                                    leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        currentSubPage = SettingsSubPage.THEMES
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "মুদ্রা কনফিগারেশন" else "Currency Setup") },
                                    leadingIcon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        currentSubPage = SettingsSubPage.CURRENCY
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "ভাষা পছন্দ" else "Language Preference") },
                                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        currentSubPage = SettingsSubPage.LANGUAGE
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "রিসেট ও ডিলিট" else "Reset & Wipe") },
                                    leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        onNavigateToReset()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "প্রশ্নোত্তর ও সহায়তা" else "FAQ & Help") },
                                    leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        currentSubPage = SettingsSubPage.FAQ
                                    }
                                )
                            }
                        }
                    }
                )

                LazyColumn(
                    state = rootListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                ) {
                    // Group 1: Localization
                    item {
                        SettingsSectionHeader(title = if (languageMode == LanguageMode.BANGLA) "ভাষা ও এলাকা" else "Localization")
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "ভাষা পছন্দ" else "Language Preference",
                            subtitle = when (languageMode) {
                                LanguageMode.ENGLISH -> "English (United States)"
                                LanguageMode.BANGLA -> "বাংলা (বাংলাদেশ ও পশ্চিমবঙ্গ)"
                            },
                            icon = Icons.Default.Language,
                            onClick = { currentSubPage = SettingsSubPage.LANGUAGE }
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা কনফিগারেশন" else "Currency Setup",
                            subtitle = "${currencyConfig.activeCode} (${currencyConfig.activeSymbol}) • ${if (languageMode == LanguageMode.BANGLA) currencyConfig.displayMode.titleBn else currencyConfig.displayMode.titleEn}",
                            icon = Icons.Default.CurrencyExchange,
                            onClick = { currentSubPage = SettingsSubPage.CURRENCY }
                        )
                    }

                    item {
                        val presetTitle = if (languageMode == LanguageMode.BANGLA) {
                            amountFormatConfig.preset.titleBn
                        } else {
                            amountFormatConfig.preset.titleEn
                        }
                        val sampleText = LanguageHelper.formatCurrency(1234567.89, languageMode)
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "টাকার কমা ও সেপারেটর" else "Amount Comma Separator",
                            subtitle = "$presetTitle • $sampleText",
                            icon = Icons.Default.Numbers,
                            onClick = { currentSubPage = SettingsSubPage.AMOUNT_FORMAT }
                        )
                    }

                    item {
                        val currentFormatOption = DateFormatOption.fromPattern(displayFormatConfig.dateFormatPattern)
                        val formatDisplay = if (languageMode == LanguageMode.BANGLA) currentFormatOption.titleBn else currentFormatOption.titleEn
                        val firstDayName = when (displayFormatConfig.firstDayOfWeek) {
                            java.util.Calendar.MONDAY -> if (languageMode == LanguageMode.BANGLA) "সোমবার" else "Monday"
                            java.util.Calendar.SATURDAY -> if (languageMode == LanguageMode.BANGLA) "শনিবার" else "Saturday"
                            else -> if (languageMode == LanguageMode.BANGLA) "রবিবার" else "Sunday"
                        }
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "তারিখ ও ক্যালেন্ডার সেটিংস" else "Date Settings",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "ফরম্যাট: $formatDisplay • প্রথম দিন: $firstDayName" else "Format: $formatDisplay • First day: $firstDayName",
                            icon = Icons.Default.CalendarToday,
                            onClick = { currentSubPage = SettingsSubPage.DATE_TIME }
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
                            title = if (languageMode == LanguageMode.BANGLA) "থিম ও রূপরেখা কাস্টমাইজেশন" else "Themes & Appearance",
                            subtitle = "${themeConfig.mode.name.lowercase().replaceFirstChar { it.uppercase() }} mode • ${themeConfig.activeThemeDisplayName} • ${if (languageMode == LanguageMode.BANGLA) themeConfig.fontPreset.titleBn else themeConfig.fontPreset.titleEn}",
                            icon = Icons.Default.Palette,
                            onClick = { currentSubPage = SettingsSubPage.THEMES }
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ট্যাব কাস্টমাইজেশন" else "Navigation Tabs",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "ট্যাব বারের অবস্থান, প্রদর্শন ও ক্রম পরিবর্তন" else "Tab position, visibility & custom ordering",
                            icon = Icons.Default.ViewCarousel,
                            onClick = { currentSubPage = SettingsSubPage.NAVIGATION_TABS }
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "ডাটা ব্যবস্থাপনা ও ব্যাকআপ" else "Data Management",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "অনলাইন সিঙ্ক, লোকাল ব্যাকআপ ও এক্সপোর্ট/ইমপোর্ট" else "Online Sync, Local Backup & Export or Import",
                            icon = Icons.Default.Storage,
                            onClick = onNavigateToBackupSync
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "লেনদেন সেটআপ" else "Transaction Setup",
                            subtitle = if (displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.TWO_LINES)
                                if (languageMode == LanguageMode.BANGLA) "দ্বি-লাইন প্রদর্শন • কাস্টম কনফিগারেশন" else "Double-Line Display • Custom Settings"
                            else
                                if (languageMode == LanguageMode.BANGLA) "একক-লাইন প্রদর্শন • কাস্টম কনফিগারেশন" else "Single-Line Display • Custom Settings",
                            icon = Icons.Default.AddCircleOutline,
                            onClick = { currentSubPage = SettingsSubPage.TRANSACTION_SETUP }
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "ক্যালেন্ডার ও পরিসংখ্যান" else "Calendar",
                            subtitle = if (dashboardConfig.calendarDisplayMode == CalendarDisplayMode.DOTS)
                                "Indicator: Dots • Show Income & Expenses"
                            else
                                "Indicator: Amount Badges • Show Income & Expenses",
                            icon = Icons.Default.DateRange,
                            onClick = { currentSubPage = SettingsSubPage.CALENDAR }
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "ফোন নোটিফিকেশন" else "Phone Notification",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "দৈনিক হিসাব রিমাইন্ডার ও বিল সতর্কতা" else "Daily expense reminder & bill due alerts",
                            icon = Icons.Default.NotificationsNone,
                            onClick = { currentSubPage = SettingsSubPage.NOTIFICATIONS }
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ও বায়োমেট্রিক" else "Password and Fingerprint",
                            subtitle = if (securityConfig.isAppLockEnabled)
                                if (languageMode == LanguageMode.BANGLA) "অ্যাপ সুরক্ষা সক্রিয় • পিন ও বায়োমেট্রিক" else "App Lock Active • Protected with PIN & Biometrics"
                            else
                                if (languageMode == LanguageMode.BANGLA) "সুরক্ষা বন্ধ রয়েছে" else "App Lock Disabled",
                            icon = Icons.Default.Fingerprint,
                            onClick = {
                                if (securityConfig.isAppLockEnabled || securityConfig.hasPin || securityConfig.isBiometricEnabled) {
                                    showSecurityAuthDialog = true
                                } else {
                                    currentSubPage = SettingsSubPage.SECURITY
                                }
                            }
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "রিসেট ও ডিলিট" else "Reset & Wipe",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "ডাটা, সেটিংস, অ্যাকাউন্ট বা ক্যাটাগরি রিসেট করুন" else "Reset data, settings, accounts, categories or wipe all",
                            icon = Icons.Default.RestartAlt,
                            onClick = onNavigateToReset
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
                            onClick = { currentSubPage = SettingsSubPage.ABOUT }
                        )
                    }

                    item {
                        SettingsListItem(
                            title = if (languageMode == LanguageMode.BANGLA) "প্রশ্নোত্তর ও সহায়তা" else "FAQ & Support",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "ইউজার গাইড, প্রশ্নোত্তর ও সহায়তা" else "User guide, FAQ & double-entry documentation",
                            icon = Icons.Default.HelpOutline,
                            onClick = { currentSubPage = SettingsSubPage.FAQ }
                        )
                    }
                }
            }
        }
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
    leading: (@Composable () -> Unit)? = null,
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
            if (leading != null) {
                leading()
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.size(24.dp)
                )
            }
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
