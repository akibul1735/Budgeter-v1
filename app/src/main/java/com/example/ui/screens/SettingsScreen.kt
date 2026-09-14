package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.CalendarDisplayMode
import com.example.util.DateFormatOption
import com.example.util.ItemDisplayFormat
import com.example.util.LanguageHelper

enum class SettingsCategory(
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector
) {
    LOCALIZATION("Localization", "ভাষা ও এলাকা", Icons.Default.Language),
    APPEARANCE("Appearance & Display", "রূপরেখা ও প্রদর্শন", Icons.Default.Palette),
    TRANSACTIONS("Transactions & Features", "লেনদেন ও ফিচার", Icons.Default.ReceiptLong),
    SECURITY("Security & Data", "নিরাপত্তা ও ডাটা", Icons.Default.Security),
    ABOUT("About & Support", "সম্পর্কে ও সহায়তা", Icons.Default.Info)
}

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

    // Tree Expansion State: All 5 categories are expanded by default
    var expandedBranches by remember {
        mutableStateOf(SettingsCategory.entries.toSet())
    }

    // Horizontal category selection filter (null means "All Categories")
    var selectedCategoryFilter by remember { mutableStateOf<SettingsCategory?>(null) }

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

    fun handleSecurityAccess() {
        if (securityConfig.isAppLockEnabled || securityConfig.hasPin || securityConfig.isBiometricEnabled) {
            showSecurityAuthDialog = true
        } else {
            currentSubPage = SettingsSubPage.SECURITY
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
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.TRANSACTIONS,
                    currentSubPage = SettingsSubPage.PAYMENT_SOURCES,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                com.example.ui.screens.settings.PaymentSourcesSettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.LANGUAGE -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.LOCALIZATION,
                    currentSubPage = SettingsSubPage.LANGUAGE,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                LanguageSettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.CURRENCY -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.LOCALIZATION,
                    currentSubPage = SettingsSubPage.CURRENCY,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                CurrencySettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.AMOUNT_FORMAT -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.LOCALIZATION,
                    currentSubPage = SettingsSubPage.AMOUNT_FORMAT,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                com.example.ui.screens.settings.AmountFormatSettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.DATE_TIME -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.LOCALIZATION,
                    currentSubPage = SettingsSubPage.DATE_TIME,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                DateSettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.THEMES -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.APPEARANCE,
                    currentSubPage = SettingsSubPage.THEMES,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                AppearanceSettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.NAVIGATION_TABS -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.APPEARANCE,
                    currentSubPage = SettingsSubPage.NAVIGATION_TABS,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                NavigationTabsSettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.TRANSACTION_SETUP -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.TRANSACTIONS,
                    currentSubPage = SettingsSubPage.TRANSACTION_SETUP,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                TransactionSetupSettingsPage(
                    viewModel = viewModel,
                    accounts = accounts,
                    categories = categories,
                    languageMode = languageMode,
                    onNavigateToAutofill = { currentSubPage = SettingsSubPage.SMART_AUTOFILL },
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.SMART_AUTOFILL -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.TRANSACTIONS,
                    currentSubPage = SettingsSubPage.SMART_AUTOFILL,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                SmartAutofillSettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.TRANSACTION_SETUP }
                )
            }
        }
        SettingsSubPage.CALENDAR -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.APPEARANCE,
                    currentSubPage = SettingsSubPage.CALENDAR,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                CalendarSettingsPage(
                    viewModel = viewModel,
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.NOTIFICATIONS -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.TRANSACTIONS,
                    currentSubPage = SettingsSubPage.NOTIFICATIONS,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                NotificationSettingsPage(
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
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
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.ABOUT,
                    currentSubPage = SettingsSubPage.ABOUT,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                AboutSettingsPage(
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.FAQ -> {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsBreadcrumbBar(
                    category = SettingsCategory.ABOUT,
                    currentSubPage = SettingsSubPage.FAQ,
                    languageMode = languageMode,
                    onNavigateToRoot = { currentSubPage = SettingsSubPage.ROOT },
                    onSelectSubPage = { currentSubPage = it }
                )
                FaqSettingsPage(
                    languageMode = languageMode,
                    onBack = { currentSubPage = SettingsSubPage.ROOT }
                )
            }
        }
        SettingsSubPage.ROOT -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .testTag("settings_screen")
            ) {
                // Header with Menu Button, Icon, and title "Settings"
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
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "সব ক্যাটাগরি প্রসারিত করুন" else "Expand All Categories") },
                                    leadingIcon = { Icon(Icons.Default.UnfoldMore, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        expandedBranches = SettingsCategory.entries.toSet()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "সব ক্যাটাগরি বন্ধ করুন" else "Collapse All Categories") },
                                    leadingIcon = { Icon(Icons.Default.UnfoldLess, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        expandedBranches = emptySet()
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
                                        handleSecurityAccess()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (languageMode == LanguageMode.BANGLA) "রিসেট ও ডিলিট" else "Reset & Wipe Data") },
                                    leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                                    onClick = {
                                        showSettingsMenu = false
                                        onNavigateToReset()
                                    }
                                )
                            }
                        }
                    }
                )

                // Root Header Card with Overview & Expand/Collapse Toggle (No ASCII symbols)
                ModernSettingsOverviewCard(
                    languageMode = languageMode,
                    allExpanded = expandedBranches.size == SettingsCategory.entries.size,
                    onToggleExpandAll = {
                        expandedBranches = if (expandedBranches.size == SettingsCategory.entries.size) {
                            emptySet()
                        } else {
                            SettingsCategory.entries.toSet()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Left-Right Horizontal Category Swiping / Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সকল সেটিংস" else "All Settings",
                                fontSize = 12.sp,
                                fontWeight = if (selectedCategoryFilter == null) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    SettingsCategory.entries.forEach { category ->
                        val isSelected = selectedCategoryFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryFilter = if (isSelected) null else category
                                if (!isSelected) {
                                    expandedBranches = expandedBranches + category
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) category.titleBn else category.titleEn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hierarchical Settings List
                LazyColumn(
                    state = rootListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // =========================================================================
                    // 1. CATEGORY: Localization (ভাষা ও এলাকা)
                    // =========================================================================
                    if (selectedCategoryFilter == null || selectedCategoryFilter == SettingsCategory.LOCALIZATION) {
                        item {
                            val isExpanded = expandedBranches.contains(SettingsCategory.LOCALIZATION)

                            ModernSettingsCategoryCard(
                                category = SettingsCategory.LOCALIZATION,
                                subtitle = if (languageMode == LanguageMode.BANGLA) "ভাষা, মুদ্রা, কমা সেপারেটর ও ক্যালেন্ডার" else "Language, Currency, Amount format & Dates",
                                itemCount = 4,
                                isExpanded = isExpanded,
                                languageMode = languageMode,
                                onToggle = {
                                    expandedBranches = if (isExpanded) {
                                        expandedBranches - SettingsCategory.LOCALIZATION
                                    } else {
                                        expandedBranches + SettingsCategory.LOCALIZATION
                                    }
                                }
                            ) {
                                // Child 1: Language Preference
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "ভাষা পছন্দ" else "Language Preference",
                                    subtitle = when (languageMode) {
                                        LanguageMode.ENGLISH -> "English (United States)"
                                        LanguageMode.BANGLA -> "বাংলা (বাংলাদেশ ও পশ্চিমবঙ্গ)"
                                    },
                                    icon = Icons.Default.Translate,
                                    badgeText = if (languageMode == LanguageMode.BANGLA) "বাংলা" else "EN",
                                    onClick = { currentSubPage = SettingsSubPage.LANGUAGE }
                                )

                                // Child 2: Currency Setup
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা কনফিগারেশন" else "Currency Setup",
                                    subtitle = "${currencyConfig.activeCode} (${currencyConfig.activeSymbol}) • ${if (languageMode == LanguageMode.BANGLA) currencyConfig.displayMode.titleBn else currencyConfig.displayMode.titleEn}",
                                    icon = Icons.Default.CurrencyExchange,
                                    badgeText = "${currencyConfig.activeSymbol} ${currencyConfig.activeCode}",
                                    onClick = { currentSubPage = SettingsSubPage.CURRENCY }
                                )

                                // Child 3: Amount Format
                                val presetTitle = if (languageMode == LanguageMode.BANGLA) amountFormatConfig.preset.titleBn else amountFormatConfig.preset.titleEn
                                val sampleText = LanguageHelper.formatCurrency(1234567.89, languageMode)
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "টাকার কমা ও সেপারেটর" else "Amount Comma Separator",
                                    subtitle = "$presetTitle • $sampleText",
                                    icon = Icons.Default.Numbers,
                                    badgeText = "12,34,567",
                                    onClick = { currentSubPage = SettingsSubPage.AMOUNT_FORMAT }
                                )

                                // Child 4: Date Settings
                                val currentFormatOption = DateFormatOption.fromPattern(displayFormatConfig.dateFormatPattern)
                                val formatDisplay = if (languageMode == LanguageMode.BANGLA) currentFormatOption.titleBn else currentFormatOption.titleEn
                                val firstDayName = when (displayFormatConfig.firstDayOfWeek) {
                                    java.util.Calendar.MONDAY -> if (languageMode == LanguageMode.BANGLA) "সোমবার" else "Monday"
                                    java.util.Calendar.SATURDAY -> if (languageMode == LanguageMode.BANGLA) "শনিবার" else "Saturday"
                                    else -> if (languageMode == LanguageMode.BANGLA) "রবিবার" else "Sunday"
                                }
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "তারিখ ও সময় সেটিংস" else "Date Settings",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "ফরম্যাট: $formatDisplay • প্রথম দিন: $firstDayName" else "Format: $formatDisplay • First day: $firstDayName",
                                    icon = Icons.Default.CalendarToday,
                                    badgeText = formatDisplay,
                                    onClick = { currentSubPage = SettingsSubPage.DATE_TIME }
                                )
                            }
                        }
                    }

                    // =========================================================================
                    // 2. CATEGORY: Appearance & Display (রূপরেখা ও প্রদর্শন)
                    // =========================================================================
                    if (selectedCategoryFilter == null || selectedCategoryFilter == SettingsCategory.APPEARANCE) {
                        item {
                            val isExpanded = expandedBranches.contains(SettingsCategory.APPEARANCE)

                            ModernSettingsCategoryCard(
                                category = SettingsCategory.APPEARANCE,
                                subtitle = if (languageMode == LanguageMode.BANGLA) "থিম, কালার, নেভিগেশন ও ক্যালেন্ডার" else "Themes, Colors, Navigation Tabs & Calendar",
                                itemCount = 3,
                                isExpanded = isExpanded,
                                languageMode = languageMode,
                                onToggle = {
                                    expandedBranches = if (isExpanded) {
                                        expandedBranches - SettingsCategory.APPEARANCE
                                    } else {
                                        expandedBranches + SettingsCategory.APPEARANCE
                                    }
                                }
                            ) {
                                // Child 1: Themes & Styling
                                val fontTitle = if (languageMode == LanguageMode.BANGLA) themeConfig.fontPreset.titleBn else themeConfig.fontPreset.titleEn
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "থিম ও রূপরেখা কাস্টমাইজেশন" else "Themes & Appearance",
                                    subtitle = "${themeConfig.mode.name.lowercase().replaceFirstChar { it.uppercase() }} • ${themeConfig.activeThemeDisplayName} • $fontTitle",
                                    icon = Icons.Default.Palette,
                                    badgeText = themeConfig.activeThemeDisplayName,
                                    onClick = { currentSubPage = SettingsSubPage.THEMES }
                                )

                                // Child 2: Navigation Tabs
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ট্যাব কাস্টমাইজেশন" else "Navigation Tabs",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "ট্যাব বারের অবস্থান, প্রদর্শন ও ক্রম পরিবর্তন" else "Tab position, visibility & custom ordering",
                                    icon = Icons.Default.ViewCarousel,
                                    badgeText = null,
                                    onClick = { currentSubPage = SettingsSubPage.NAVIGATION_TABS }
                                )

                                // Child 3: Calendar View
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "ক্যালেন্ডার ও পরিসংখ্যান" else "Calendar View & Display",
                                    subtitle = if (dashboardConfig.calendarDisplayMode == CalendarDisplayMode.DOTS)
                                        "Indicator: Dots • Show Income & Expenses"
                                    else
                                        "Indicator: Amount Badges • Show Income & Expenses",
                                    icon = Icons.Default.DateRange,
                                    badgeText = if (dashboardConfig.calendarDisplayMode == CalendarDisplayMode.DOTS) "Dots" else "Badges",
                                    onClick = { currentSubPage = SettingsSubPage.CALENDAR }
                                )
                            }
                        }
                    }

                    // =========================================================================
                    // 3. CATEGORY: Transactions & Features (লেনদেন ও ফিচার)
                    // =========================================================================
                    if (selectedCategoryFilter == null || selectedCategoryFilter == SettingsCategory.TRANSACTIONS) {
                        item {
                            val isExpanded = expandedBranches.contains(SettingsCategory.TRANSACTIONS)

                            ModernSettingsCategoryCard(
                                category = SettingsCategory.TRANSACTIONS,
                                subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেন সেটআপ, স্মার্ট অটোফিল ও রিমাইন্ডার" else "Input forms, Autofill, Payment sources & Alerts",
                                itemCount = 4,
                                isExpanded = isExpanded,
                                languageMode = languageMode,
                                onToggle = {
                                    expandedBranches = if (isExpanded) {
                                        expandedBranches - SettingsCategory.TRANSACTIONS
                                    } else {
                                        expandedBranches + SettingsCategory.TRANSACTIONS
                                    }
                                }
                            ) {
                                // Child 1: Transaction Setup
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "লেনদেন সেটআপ" else "Transaction Setup",
                                    subtitle = if (displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.TWO_LINES)
                                        if (languageMode == LanguageMode.BANGLA) "দ্বি-লাইন প্রদর্শন • কাস্টম কনফিগারেশন" else "Double-Line Display • Custom Settings"
                                    else
                                        if (languageMode == LanguageMode.BANGLA) "একক-লাইন প্রদর্শন • কাস্টম কনফিগারেশন" else "Single-Line Display • Custom Settings",
                                    icon = Icons.Default.AddCircleOutline,
                                    badgeText = if (displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.TWO_LINES) "2 Lines" else "1 Line",
                                    onClick = { currentSubPage = SettingsSubPage.TRANSACTION_SETUP }
                                )

                                // Child 2: Smart Autofill
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "স্মার্ট অটোফিল ও পরামর্শ" else "Smart Autofill & Suggestions",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় অ্যাকাউন্ট ও ক্যাটাগরি পরামর্শ" else "Smart category & account suggestions",
                                    icon = Icons.Default.AutoAwesome,
                                    badgeText = "AI Suggest",
                                    onClick = { currentSubPage = SettingsSubPage.SMART_AUTOFILL }
                                )

                                // Child 3: Payment Sources
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স ও মাধ্যম" else "Payment Sources & Accounts",
                                    subtitle = "${accounts.size} active sources (Cash, Bank, Wallets, Cards)",
                                    icon = Icons.Default.MonetizationOn,
                                    badgeText = "${accounts.size} Sources",
                                    onClick = { currentSubPage = SettingsSubPage.PAYMENT_SOURCES }
                                )

                                // Child 4: Phone Notifications
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "ফোন নোটিফিকেশন" else "Phone Notification",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "দৈনিক হিসাব রিমাইন্ডার ও বিল সতর্কতা" else "Daily expense reminder & bill due alerts",
                                    icon = Icons.Default.NotificationsNone,
                                    badgeText = null,
                                    onClick = { currentSubPage = SettingsSubPage.NOTIFICATIONS }
                                )
                            }
                        }
                    }

                    // =========================================================================
                    // 4. CATEGORY: Security & Data (নিরাপত্তা ও ডাটা)
                    // =========================================================================
                    if (selectedCategoryFilter == null || selectedCategoryFilter == SettingsCategory.SECURITY) {
                        item {
                            val isExpanded = expandedBranches.contains(SettingsCategory.SECURITY)

                            ModernSettingsCategoryCard(
                                category = SettingsCategory.SECURITY,
                                subtitle = if (languageMode == LanguageMode.BANGLA) "অ্যাপ সুরক্ষা, ব্যাকআপ সিঙ্ক ও ডাটা রিসেট" else "Password, Cloud Sync, Local Backup & Data Wipe",
                                itemCount = 3,
                                isExpanded = isExpanded,
                                languageMode = languageMode,
                                onToggle = {
                                    expandedBranches = if (isExpanded) {
                                        expandedBranches - SettingsCategory.SECURITY
                                    } else {
                                        expandedBranches + SettingsCategory.SECURITY
                                    }
                                }
                            ) {
                                // Child 1: Password & Fingerprint
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ও বায়োমেট্রিক" else "Password and Fingerprint",
                                    subtitle = if (securityConfig.isAppLockEnabled)
                                        if (languageMode == LanguageMode.BANGLA) "অ্যাপ সুরক্ষা সক্রিয় • পিন ও বায়োমেট্রিক" else "App Lock Active • Protected with PIN & Biometrics"
                                    else
                                        if (languageMode == LanguageMode.BANGLA) "সুরক্ষা বন্ধ রয়েছে" else "App Lock Disabled",
                                    icon = Icons.Default.Fingerprint,
                                    badgeText = if (securityConfig.isAppLockEnabled) "Locked" else "Off",
                                    onClick = { handleSecurityAccess() }
                                )

                                // Child 2: Data Management & Sync
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "ডাটা ব্যবস্থাপনা ও ব্যাকআপ" else "Data Management & Sync",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "অনলাইন ড্রাইভ, লোকাল ব্যাকআপ, এক্সপোর্ট ও ইমপোর্ট" else "Google Drive, Local Storage, CSV & JSON",
                                    icon = Icons.Default.Storage,
                                    badgeText = "Cloud & Local",
                                    onClick = onNavigateToBackupSync
                                )

                                // Child 3: Reset & Wipe
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "রিসেট ও ডিলিট" else "Reset & Wipe Data",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "ডাটা, সেটিংস, অ্যাকাউন্ট বা ক্যাটাগরি রিসেট করুন" else "Reset data, settings, accounts, categories or wipe all",
                                    icon = Icons.Default.RestartAlt,
                                    badgeText = "Reset",
                                    onClick = onNavigateToReset
                                )
                            }
                        }
                    }

                    // =========================================================================
                    // 5. CATEGORY: About & Support (সম্পর্কে ও সহায়তা)
                    // =========================================================================
                    if (selectedCategoryFilter == null || selectedCategoryFilter == SettingsCategory.ABOUT) {
                        item {
                            val isExpanded = expandedBranches.contains(SettingsCategory.ABOUT)

                            ModernSettingsCategoryCard(
                                category = SettingsCategory.ABOUT,
                                subtitle = if (languageMode == LanguageMode.BANGLA) "অ্যাপ তথ্য, প্রাইভেসি, ইউজার গাইড ও সহায়তা" else "Version info, Offline privacy, FAQ & Documentation",
                                itemCount = 2,
                                isExpanded = isExpanded,
                                languageMode = languageMode,
                                onToggle = {
                                    expandedBranches = if (isExpanded) {
                                        expandedBranches - SettingsCategory.ABOUT
                                    } else {
                                        expandedBranches + SettingsCategory.ABOUT
                                    }
                                }
                            ) {
                                // Child 1: About Budgeter
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "বাজেটার সম্পর্কে" else "About Budgeter",
                                    subtitle = "Version 3.6 • Offline-First Personal Finance",
                                    icon = Icons.Default.Info,
                                    badgeText = "v3.6",
                                    onClick = { currentSubPage = SettingsSubPage.ABOUT }
                                )

                                // Child 2: FAQ & Support
                                ModernSettingsChildRow(
                                    title = if (languageMode == LanguageMode.BANGLA) "প্রশ্নোত্তর ও সহায়তা" else "FAQ & Support Guide",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "ইউজার গাইড, প্রশ্নোত্তর ও ডাবল-এন্ট্রি নির্দেশিকা" else "User guide, FAQ & double-entry documentation",
                                    icon = Icons.Default.HelpOutline,
                                    badgeText = "Guide",
                                    onClick = { currentSubPage = SettingsSubPage.FAQ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern Root Header Card (without ASCII tree symbols)
 */
@Composable
private fun ModernSettingsOverviewCard(
    languageMode: LanguageMode,
    allExpanded: Boolean,
    onToggleExpandAll: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সেটিংস কনফিগারেশন" else "Settings Configuration",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "৫টি প্রধান বিভাগ • ১৬টি কনফিগারেশন" else "5 Main Sections • 16 Configurable Options",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.clickable { onToggleExpandAll() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (allExpanded) {
                            if (languageMode == LanguageMode.BANGLA) "সব বন্ধ" else "Collapse All"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "সব খুলুন" else "Expand All"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Modern Category Card with smooth Drop-Down accordion expansion (No ASCII symbols)
 */
@Composable
private fun ModernSettingsCategoryCard(
    category: SettingsCategory,
    subtitle: String,
    itemCount: Int,
    isExpanded: Boolean,
    languageMode: LanguageMode,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val chevronRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "chevron")

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) category.titleBn else category.titleEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = "$itemCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Drop-down Chevron Indicator
                IconButton(
                    onClick = { onToggle() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(chevronRotation)
                    )
                }
            }

            // Expanded Subcategory Items
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp, top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    content()
                }
            }
        }
    }
}

/**
 * Modern Redesigned Child / Subcategory Item
 * Clean card layout with left indentation, icon, bold title, descriptive subtitle, value badge and chevron arrow.
 */
@Composable
private fun ModernSettingsChildRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String?,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in rounded surface
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Value Badge if present
            if (!badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * Breadcrumb & Sibling Navigation Bar displayed at the top of settings sub-pages
 * Enables left-right horizontal switching between sibling items in the same branch
 */
@Composable
private fun SettingsBreadcrumbBar(
    category: SettingsCategory,
    currentSubPage: SettingsSubPage,
    languageMode: LanguageMode,
    onNavigateToRoot: () -> Unit,
    onSelectSubPage: (SettingsSubPage) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            // Hierarchical Breadcrumb line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToRoot() }
                )
                Text(
                    text = "  ›  ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) category.titleBn else category.titleEn,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Horizontal Sibling Chips for Left-Right horizontal switching
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (category) {
                    SettingsCategory.LOCALIZATION -> {
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "ভাষা" else "Language",
                            icon = Icons.Default.Translate,
                            isSelected = currentSubPage == SettingsSubPage.LANGUAGE,
                            onClick = { onSelectSubPage(SettingsSubPage.LANGUAGE) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "মুদ্রা" else "Currency",
                            icon = Icons.Default.CurrencyExchange,
                            isSelected = currentSubPage == SettingsSubPage.CURRENCY,
                            onClick = { onSelectSubPage(SettingsSubPage.CURRENCY) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "কমা সেপারেটর" else "Amount Format",
                            icon = Icons.Default.Numbers,
                            isSelected = currentSubPage == SettingsSubPage.AMOUNT_FORMAT,
                            onClick = { onSelectSubPage(SettingsSubPage.AMOUNT_FORMAT) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "তারিখ" else "Date Settings",
                            icon = Icons.Default.CalendarToday,
                            isSelected = currentSubPage == SettingsSubPage.DATE_TIME,
                            onClick = { onSelectSubPage(SettingsSubPage.DATE_TIME) }
                        )
                    }
                    SettingsCategory.APPEARANCE -> {
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "থিম" else "Themes",
                            icon = Icons.Default.Palette,
                            isSelected = currentSubPage == SettingsSubPage.THEMES,
                            onClick = { onSelectSubPage(SettingsSubPage.THEMES) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "ট্যাব বার" else "Tabs",
                            icon = Icons.Default.ViewCarousel,
                            isSelected = currentSubPage == SettingsSubPage.NAVIGATION_TABS,
                            onClick = { onSelectSubPage(SettingsSubPage.NAVIGATION_TABS) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "ক্যালেন্ডার" else "Calendar",
                            icon = Icons.Default.DateRange,
                            isSelected = currentSubPage == SettingsSubPage.CALENDAR,
                            onClick = { onSelectSubPage(SettingsSubPage.CALENDAR) }
                        )
                    }
                    SettingsCategory.TRANSACTIONS -> {
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "লেনদেন সেটআপ" else "Transaction Setup",
                            icon = Icons.Default.AddCircleOutline,
                            isSelected = currentSubPage == SettingsSubPage.TRANSACTION_SETUP,
                            onClick = { onSelectSubPage(SettingsSubPage.TRANSACTION_SETUP) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "অটোফিল" else "Autofill",
                            icon = Icons.Default.AutoAwesome,
                            isSelected = currentSubPage == SettingsSubPage.SMART_AUTOFILL,
                            onClick = { onSelectSubPage(SettingsSubPage.SMART_AUTOFILL) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স" else "Payment Sources",
                            icon = Icons.Default.MonetizationOn,
                            isSelected = currentSubPage == SettingsSubPage.PAYMENT_SOURCES,
                            onClick = { onSelectSubPage(SettingsSubPage.PAYMENT_SOURCES) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "নোটিফিকেশন" else "Notifications",
                            icon = Icons.Default.NotificationsNone,
                            isSelected = currentSubPage == SettingsSubPage.NOTIFICATIONS,
                            onClick = { onSelectSubPage(SettingsSubPage.NOTIFICATIONS) }
                        )
                    }
                    SettingsCategory.ABOUT -> {
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "বাজেটার তথ্য" else "About",
                            icon = Icons.Default.Info,
                            isSelected = currentSubPage == SettingsSubPage.ABOUT,
                            onClick = { onSelectSubPage(SettingsSubPage.ABOUT) }
                        )
                        SiblingChip(
                            label = if (languageMode == LanguageMode.BANGLA) "প্রশ্নোত্তর" else "FAQ",
                            icon = Icons.Default.HelpOutline,
                            isSelected = currentSubPage == SettingsSubPage.FAQ,
                            onClick = { onSelectSubPage(SettingsSubPage.FAQ) }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun SiblingChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
