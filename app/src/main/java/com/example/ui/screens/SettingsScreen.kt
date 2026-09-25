package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.screens.settings.AppPermissionsSettingsPage
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
import com.example.util.IconHelper
import com.example.util.ItemDisplayFormat
import com.example.util.LanguageHelper
import androidx.compose.material.icons.filled.Widgets
import kotlinx.coroutines.launch

enum class SettingsSubPage {
    ROOT,
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
    PERMISSIONS,
    SECURITY,
    WIDGETS,
    ICON_IMAGE_CACHE,
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
    onNavigateToArchivePrune: () -> Unit = {},
    onNavigateToReset: () -> Unit = {},
    onOpenTabCustomizer: () -> Unit = {},
    onOpenThemeFontSettings: () -> Unit = {},
    onOpenAutofillSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var currentSubPage by remember { mutableStateOf(SettingsSubPage.ROOT) }
    var showSecurityAuthDialog by remember { mutableStateOf(false) }
    var showArchiveAuthDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showIconStorageDialog by remember { mutableStateOf(false) }
    var iconCacheStats by remember { mutableStateOf(IconHelper.IconCacheStats()) }
    var isCleaningIconCache by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    val rootListState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.settingsScrollIndex,
        initialFirstVisibleItemScrollOffset = viewModel.settingsScrollOffset
    )

    LaunchedEffect(rootListState) {
        snapshotFlow { rootListState.firstVisibleItemIndex to rootListState.firstVisibleItemScrollOffset }
            .collect { (idx, offset) ->
                viewModel.settingsScrollIndex = idx
                viewModel.settingsScrollOffset = offset
            }
    }

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

    fun handleArchivePruneAccess() {
        if (securityConfig.isAppLockEnabled || securityConfig.hasPin || securityConfig.isBiometricEnabled) {
            showArchiveAuthDialog = true
        } else {
            onNavigateToArchivePrune()
        }
    }

    if (showArchiveAuthDialog) {
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও প্রুনিং নিরাপত্তা" else "Archive & Prune Authentication",
            message = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও প্রুনিং বিভাগে প্রবেশ করতে আপনার পিন বা ফিঙ্গারপ্রিন্ট দিন।" else "Please authenticate with your PIN or biometric to access Archive & Prune.",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "প্রবেশ করুন" else "Authenticate",
            requiresAuth = true,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { pin -> viewModel.verifySecurityPin(pin) },
            onConfirm = {
                showArchiveAuthDialog = false
                onNavigateToArchivePrune()
            },
            onDismiss = {
                showArchiveAuthDialog = false
            }
        )
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

    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "মতামত ও পরামর্শ" else "Feedback & Bug Report",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "বাজেটার অ্যাপটি উন্নত করতে আপনার মতামত বা সমস্যার কথা লিখুন:"
                        else
                            "Tell us what you'd like improved or describe any issue you encountered:"
                    )
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        placeholder = {
                            Text(
                                if (languageMode == LanguageMode.BANGLA) "আপনার মন্তব্য এখানে লিখুন..." else "Enter your feedback here..."
                            )
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFeedbackDialog = false
                        feedbackText = ""
                        Toast.makeText(
                            context,
                            if (languageMode == LanguageMode.BANGLA) "আপনার মতামতের জন্য ধন্যবাদ!" else "Thank you for your feedback!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "পাঠান" else "Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "গোপনীয়তা নীতি" else "Privacy Policy",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "১. বাজেটার সম্পূর্ণ অফলাইন-ফার্স্ট অ্যাপ্লিকেশন। আপনার সমস্ত আয়, ব্যয় ও অ্যাকাউন্টের ডাটা আপনার নিজের ফোনেই সংরক্ষিত থাকে।\n\n২. আমরা কোনো ট্র্যাকিং, থার্ড-পার্টি অ্যানালিটিক্স বা ক্লাউড সার্ভারে ডাটা পাঠাই না।\n\n৩. আপনি ব্যাকআপ নিলে তা শুধুমাত্র আপনার নিজস্ব লোকাল ফাইল বা আপনার নিজস্ব গুগল ড্রাইভ অ্যাকাউন্টে জমা হয়।"
                        else
                            "1. Budgeter is a 100% offline-first application. All financial entries, transactions, and account details remain securely on your device.\n\n2. We do not collect, track, or transmit your personal financial information to any third-party servers or analytics providers.\n\n3. Cloud backups are saved strictly into your own private Google Drive storage."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "Got it")
                }
            }
        )
    }

    if (showIconStorageDialog) {
        val totalKb = (iconCacheStats.totalBytes / 1024).coerceAtLeast(if (iconCacheStats.totalCount > 0) 1 else 0)
        val activeKb = (iconCacheStats.activeBytes / 1024).coerceAtLeast(if (iconCacheStats.activeCount > 0) 1 else 0)
        val unusedKb = (iconCacheStats.unusedBytes / 1024).coerceAtLeast(if (iconCacheStats.unusedCount > 0) 1 else 0)

        AlertDialog(
            onDismissRequest = { if (!isCleaningIconCache) showIconStorageDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "আইটেম আইকন" else "Items Icon",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "অ্যাপে সংরক্ষিত কাস্টম ও ডাউনলোডকৃত আইকন এবং ছবির মেমোরি বিবরণ:"
                        else
                            "Storage occupied by downloaded and custom cropped icons and images:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedCard(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "মোট কাস্টম আইকন:" else "Total Custom Icons:",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${iconCacheStats.totalCount} items (~$totalKb KB)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ব্যবহৃত (সংরক্ষিত):" else "In Use (Protected):",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${iconCacheStats.activeCount} items (~$activeKb KB)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "অব্যবহৃত ক্যাশ:" else "Unused Cache (Safe to clean):",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${iconCacheStats.unusedCount} items (~$unusedKb KB)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (iconCacheStats.unusedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (iconCacheStats.unusedCount == 0) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "সবগুলো আইকন বর্তমানে ক্যাটাগরি, অ্যাকাউন্ট বা লক্ষ্যে সক্রিয় রয়েছে। ক্যাশ সম্পূর্ণ পরিষ্কার।"
                            else
                                "All icons are currently assigned to active categories, accounts, or goals. Cache is clean.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "ক্যাশ পরিষ্কার করলে শুধুমাত্র অব্যবহৃত আইকন মুছে যাবে, কিন্তু আপনার ব্যবহৃত কোনো আইকন নষ্ট হবে না।"
                            else
                                "Clearing cache only removes unused orphan icons without affecting any of your active icons.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            confirmButton = {
                if (iconCacheStats.unusedCount > 0) {
                    Button(
                        onClick = {
                            isCleaningIconCache = true
                            coroutineScope.launch {
                                val (cleanedCount, freedBytes) = viewModel.clearUnusedIconCache()
                                iconCacheStats = viewModel.getIconCacheStats()
                                isCleaningIconCache = false
                                val freedKb = (freedBytes / 1024).coerceAtLeast(1)
                                Toast.makeText(
                                    context,
                                    if (languageMode == LanguageMode.BANGLA)
                                        "$cleanedCount টি অব্যবহৃত আইকন মুছে $freedKb KB মেমোরি খালি করা হয়েছে!"
                                    else
                                        "Cleaned $cleanedCount unused icons! Freed $freedKb KB",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = !isCleaningIconCache,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        if (isCleaningIconCache) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(if (languageMode == LanguageMode.BANGLA) "ক্যাশ মুছুন" else "Clear Cache")
                    }
                } else {
                    Button(onClick = { showIconStorageDialog = false }) {
                        Text(if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "Done")
                    }
                }
            },
            dismissButton = {
                if (iconCacheStats.unusedCount > 0) {
                    TextButton(onClick = { showIconStorageDialog = false }, enabled = !isCleaningIconCache) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close")
                    }
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main ROOT Settings List - ALWAYS kept in composition so LazyListState and scroll position are never destroyed
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_screen")
        ) {
            // Top App Header
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
                }
            )

            // Clean flat settings list categorized by sections
            LazyColumn(
                state = rootListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // =========================================================================
                // 1. SECTION: APPEARANCE & INTERFACE
                // =========================================================================
                item(key = "section_appearance") {
                    SettingsSectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "রূপরেখা ও ইন্টারফেস" else "APPEARANCE & INTERFACE"
                    )
                }

                item(key = "item_themes") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "থিম ও রূপরেখা" else "Customize Appearance",
                        subtitle = "${themeConfig.activeThemeDisplayName} • ${themeConfig.mode.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        icon = Icons.Default.Palette,
                        onClick = { currentSubPage = SettingsSubPage.THEMES }
                    )
                }

                item(key = "item_navigation_tabs") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ট্যাব" else "Navigation Tabs",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "নিচের ট্যাব বারের প্রদর্শন ও অবস্থান" else "Customize visible bottom tabs & order",
                        icon = Icons.Default.ViewCarousel,
                        onClick = { currentSubPage = SettingsSubPage.NAVIGATION_TABS }
                    )
                }

                item(key = "item_items_icon") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "আইটেম আইকন ও স্টুডিও" else "Items Icon & Studio",
                        subtitle = if (languageMode == LanguageMode.BANGLA)
                            "অনলাইন আইকন, ইমেজ ক্রপ স্টুডিও ও কাস্টম আইকন"
                        else
                            "Online icons, photo cropper & custom item icons",
                        icon = Icons.Default.Category,
                        onClick = { currentSubPage = SettingsSubPage.ICON_IMAGE_CACHE }
                    )
                }

                item(key = "item_widgets") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "হোম স্ক্রিন উইজেট" else "Home Screen Widgets",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "প্রিমিয়াম কুইক অ্যাকশন ও বাজেট উইজেট পিন করুন" else "Quick actions, live budget meter & live feeds",
                        icon = Icons.Default.Widgets,
                        onClick = { currentSubPage = SettingsSubPage.WIDGETS }
                    )
                }

                item(key = "item_calendar") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "ক্যালেন্ডার প্রদর্শন" else "Calendar Display",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "আয় ও ব্যয়ের ইনডিকেটর প্রদর্শন" else "Visual indicators & fiscal calendar",
                        icon = Icons.Default.CalendarMonth,
                        onClick = { currentSubPage = SettingsSubPage.CALENDAR }
                    )
                }

                item(key = "divider_1") {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // =========================================================================
                // 2. SECTION: REGIONAL & FORMATTING
                // =========================================================================
                item(key = "section_localization") {
                    SettingsSectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "লোকালাইজেশন ও ফরম্যাটিং" else "REGIONAL & FORMATTING"
                    )
                }

                item(key = "item_language") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "ভাষা পছন্দ" else "Language Preference",
                        subtitle = when (languageMode) {
                            LanguageMode.ENGLISH -> "English"
                            LanguageMode.BANGLA -> "বাংলা"
                        },
                        icon = Icons.Default.Language,
                        onClick = { currentSubPage = SettingsSubPage.LANGUAGE }
                    )
                }

                item(key = "item_currency") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা কনফিগারেশন" else "Currency Setup",
                        subtitle = "${currencyConfig.activeCode} (${currencyConfig.activeSymbol})",
                        icon = Icons.Default.CurrencyExchange,
                        onClick = { currentSubPage = SettingsSubPage.CURRENCY }
                    )
                }

                item(key = "item_amount_format") {
                    val presetTitle = if (languageMode == LanguageMode.BANGLA) amountFormatConfig.preset.titleBn else amountFormatConfig.preset.titleEn
                    val sampleText = LanguageHelper.formatCurrency(1234567.89, languageMode)
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "টাকার কমা ও সেপারেটর" else "Amount Format",
                        subtitle = "$presetTitle • $sampleText",
                        icon = Icons.Default.Numbers,
                        onClick = { currentSubPage = SettingsSubPage.AMOUNT_FORMAT }
                    )
                }

                item(key = "item_date_settings") {
                    val currentFormatOption = DateFormatOption.fromPattern(displayFormatConfig.dateFormatPattern)
                    val formatDisplay = if (languageMode == LanguageMode.BANGLA) currentFormatOption.titleBn else currentFormatOption.titleEn
                    val firstDayName = when (displayFormatConfig.firstDayOfWeek) {
                        java.util.Calendar.SATURDAY -> if (languageMode == LanguageMode.BANGLA) "শনিবার" else "Saturday"
                        java.util.Calendar.SUNDAY -> if (languageMode == LanguageMode.BANGLA) "রবিবার" else "Sunday"
                        java.util.Calendar.MONDAY -> if (languageMode == LanguageMode.BANGLA) "সোমবার" else "Monday"
                        java.util.Calendar.TUESDAY -> if (languageMode == LanguageMode.BANGLA) "মঙ্গলবার" else "Tuesday"
                        java.util.Calendar.WEDNESDAY -> if (languageMode == LanguageMode.BANGLA) "বুধবার" else "Wednesday"
                        java.util.Calendar.THURSDAY -> if (languageMode == LanguageMode.BANGLA) "বৃহস্পতিবার" else "Thursday"
                        java.util.Calendar.FRIDAY -> if (languageMode == LanguageMode.BANGLA) "শুক্রবার" else "Friday"
                        else -> if (languageMode == LanguageMode.BANGLA) "রবিবার" else "Sunday"
                    }
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "তারিখ ও সময় সেটিংস" else "Date Settings",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "$formatDisplay • প্রথম দিন $firstDayName" else "$formatDisplay • First day $firstDayName",
                        icon = Icons.Default.CalendarToday,
                        onClick = { currentSubPage = SettingsSubPage.DATE_TIME }
                    )
                }

                item(key = "divider_2") {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // =========================================================================
                // 3. SECTION: TRANSACTIONS & WORKFLOW
                // =========================================================================
                item(key = "section_transactions") {
                    SettingsSectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "লেনদেন ও কার্যপ্রণালী" else "TRANSACTIONS & WORKFLOW"
                    )
                }

                item(key = "item_transaction_setup") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "লেনদেন সেটআপ" else "Transaction Setup",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট মাধ্যম, দ্রুত লেনদেন ও প্রদর্শন" else "Default accounts, layout & autofill",
                        icon = Icons.Default.AddCircleOutline,
                        onClick = { currentSubPage = SettingsSubPage.TRANSACTION_SETUP }
                    )
                }

                item(key = "item_phone_notifications") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "ফোন নোটিফিকেশন" else "Phone Notification",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "দৈনিক রিমাইন্ডার ও বিল অ্যালার্ট" else "Daily expense alarms & reminders",
                        icon = Icons.Default.NotificationsNone,
                        onClick = { currentSubPage = SettingsSubPage.NOTIFICATIONS }
                    )
                }

                item(key = "divider_3") {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // =========================================================================
                // 4. SECTION: SECURITY & PERMISSIONS
                // =========================================================================
                item(key = "section_security") {
                    SettingsSectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "নিরাপত্তা ও অনুমতি" else "SECURITY & PERMISSIONS"
                    )
                }

                item(key = "item_security") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ও ফিঙ্গারপ্রিন্ট" else "Password and Fingerprint",
                        subtitle = if (securityConfig.isAppLockEnabled)
                            (if (languageMode == LanguageMode.BANGLA) "অ্যাপ লক সক্রিয় রয়েছে" else "App lock enabled")
                        else
                            (if (languageMode == LanguageMode.BANGLA) "বায়োমেট্রিক ও পিন সুরক্ষা" else "Biometric authentication & PIN lock"),
                        icon = Icons.Default.Fingerprint,
                        onClick = { handleSecurityAccess() }
                    )
                }

                item(key = "item_app_permissions") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "অ্যাপের অনুমতিসমূহ" else "App Permissions",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "ক্যামেরা, নোটিফিকেশন ও সিস্টেম অনুমতি নিয়ন্ত্রণ" else "Manage camera, notifications & system access",
                        icon = Icons.Default.Security,
                        onClick = { currentSubPage = SettingsSubPage.PERMISSIONS }
                    )
                }

                item(key = "divider_4") {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // =========================================================================
                // 5. SECTION: DATA & STORAGE
                // =========================================================================
                item(key = "section_data_storage") {
                    SettingsSectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "ডাটা ও স্টোরেজ" else "DATA & STORAGE"
                    )
                }

                item(key = "item_backup_sync") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "অনলাইন সিঙ্ক ও ব্যাকআপ" else "Backup & Cloud Sync",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভ, ড্রপবক্স ও লোকাল ব্যাকআপ" else "Google Drive, Dropbox & Local Storage",
                        icon = Icons.Default.CloudSync,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToBackupSync
                    )
                }

                item(key = "item_archive_prune") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও ডাটা প্রুনিং" else "Archive & Prune",
                        subtitle = if (languageMode == LanguageMode.BANGLA)
                            "ব্যালেন্স অপরিবর্তিত রেখে পুরাতন ডাটা এনক্রিপ্ট ও প্রুন করুন"
                        else
                            "Smart fiscal year roll-forward, shrink DB & 100% balance match",
                        icon = Icons.Default.Archive,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { handleArchivePruneAccess() }
                    )
                }

                item(key = "item_reset_wipe") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "রিসেট ও ডিলিট" else "Reset & Wipe",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেন ডিলিট, সেটিংস রিসেট বা ফ্যাক্টরি ক্লিন স্টেট" else "Clear transactions or factory reset app",
                        icon = Icons.Default.DeleteSweep,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = onNavigateToReset
                    )
                }

                item(key = "divider_5") {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // =========================================================================
                // 6. SECTION: SUPPORT & ABOUT
                // =========================================================================
                item(key = "section_about") {
                    SettingsSectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "সহায়তা ও তথ্য" else "SUPPORT & ABOUT"
                    )
                }

                item(key = "item_faq") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "সহায়তা ও প্রশ্নোত্তর" else "Support & FAQ",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "ইউজার গাইড ও প্রশ্নোত্তর" else "User guide & frequently asked questions",
                        icon = Icons.Default.HelpOutline,
                        onClick = { currentSubPage = SettingsSubPage.FAQ }
                    )
                }

                item(key = "item_feedback") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "মতামত ও পরামর্শ" else "Feedback & Bug Report",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "ডেভেলপারদের পরামর্শ ও মতামত জানান" else "Send suggestions to developers",
                        icon = Icons.Default.Feedback,
                        onClick = { showFeedbackDialog = true }
                    )
                }

                item(key = "item_privacy") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "গোপনীয়তা নীতি" else "Privacy Policy",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "১০০% অফলাইন, আপনার ডাটা ফোনেই নিরাপদ" else "100% offline-first, your data stays on device",
                        icon = Icons.Default.Security,
                        onClick = { showPrivacyDialog = true }
                    )
                }

                item(key = "item_app_version") {
                    ModernSettingsItemRow(
                        title = if (languageMode == LanguageMode.BANGLA) "অ্যাপ সংস্করণ" else "App Version",
                        subtitle = "v1.0.0 (Budgeter Release)",
                        icon = Icons.Default.Info,
                        onClick = { currentSubPage = SettingsSubPage.ABOUT }
                    )
                }
            }
        }

        // Sub-pages rendered on top in a full-screen Surface
        if (currentSubPage != SettingsSubPage.ROOT) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentSubPage) {
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
                    SettingsSubPage.PERMISSIONS -> {
                        AppPermissionsSettingsPage(
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
                            onSetRequireAuthForCloudDisconnect = { viewModel.setRequireAuthForCloudDisconnect(it) },
                            onSetLockTimeoutSeconds = { viewModel.setLockTimeoutSeconds(it) },
                            onSetSecurityRecovery = { q, a -> viewModel.setSecurityRecovery(q, a) },
                            onVerifySecurityAnswer = { viewModel.verifySecurityAnswer(it) },
                            onBack = { currentSubPage = SettingsSubPage.ROOT }
                        )
                    }
                    SettingsSubPage.WIDGETS -> {
                        com.example.ui.screens.settings.WidgetsSettingsPage(
                            viewModel = viewModel,
                            languageMode = languageMode,
                            onBack = { currentSubPage = SettingsSubPage.ROOT }
                        )
                    }
                    SettingsSubPage.ICON_IMAGE_CACHE -> {
                        com.example.ui.screens.settings.IconImageCacheSettingsPage(
                            viewModel = viewModel,
                            languageMode = languageMode,
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
                    SettingsSubPage.ROOT -> {}
                }
            }
        }
    }
}

/**
 * Modern Clean Settings Item Row (Flat layout matching reference)
 */
@Composable
private fun ModernSettingsItemRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Section Header (Uppercase, colored, compact spacing)
 */
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}
