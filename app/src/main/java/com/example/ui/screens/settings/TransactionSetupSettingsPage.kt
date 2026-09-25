package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DisplayFormatPreferences
import com.example.util.IconHelper
import com.example.util.ItemDisplayFormat
import com.example.util.LabelsDisplayMode
import com.example.util.NotesDisplayMode
import com.example.util.TransactionPreferences
import com.example.util.UnnamedPayeeMode

private enum class TxPickerType {
    NONE,
    DEFAULT_ACCOUNT,
    DEFAULT_INCOME_CATEGORY,
    DEFAULT_EXPENSE_CATEGORY
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionSetupSettingsPage(
    viewModel: BudgetViewModel,
    accounts: List<Account>,
    categories: List<Category>,
    languageMode: LanguageMode,
    onNavigateToAutofill: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA

    val txPrefs = remember { TransactionPreferences.getInstance(context) }
    val txConfig by txPrefs.config.collectAsStateWithLifecycle()

    val displayFormatPrefs = remember { DisplayFormatPreferences.getInstance(context) }
    val displayFormatConfig by displayFormatPrefs.config.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var activePicker by remember { mutableStateOf(TxPickerType.NONE) }
    var customPayeeDraft by remember(txConfig.customDefaultPayee) { mutableStateOf(txConfig.customDefaultPayee) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transaction_setup_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "লেনদেন সেটআপ" else "Transaction Setup",
            tabIcon = Icons.Default.Tune,
            onBack = onBack,
            autoHideOnScroll = false
        )

        val tabs = listOf(
            if (isBangla) "ইনপুট ও ক্যালেন্ডার" else "Input & Calendar",
            if (isBangla) "ডিফল্ট ও প্রদর্শন" else "Defaults & Display",
            if (isBangla) "+১ ধারাবাহিক এন্ট্রি" else "+1 Repeat Entry"
        )

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SolidPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.5.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            when (selectedTab) {
                0 -> {
                    // =========================================================
                    // TAB 0: Input & Calendar
                    // =========================================================

                    // 1. Date & Calendar Settings Card
                    SettingsSectionCard(
                        title = if (isBangla) "ক্যালেন্ডার ও তারিখ নির্বাচন" else "Calendar & Date Picker",
                        subtitle = if (isBangla) "দ্রুত তারিখ চয়ন ও ১-ট্যাপ নির্বাচন সুবিধা" else "Fast date presets and 1-tap auto-selection",
                        icon = Icons.Default.CalendarMonth,
                        iconTint = MaterialTheme.colorScheme.primary
                    ) {
                        SettingsSwitchRow(
                            title = if (isBangla) "দ্রুত ডেট চিপস প্রদর্শন" else "Show Quick Date Chips",
                            subtitle = if (isBangla) "আজ, গতকাল ইত্যাদি দ্রুত বাটন" else "Today, Yesterday quick presets",
                            checked = txConfig.enableQuickDatePicker,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(enableQuickDatePicker = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "১-ট্যাপ অটো সিলেক্ট মোড" else "1-Tap Quick Calendar Select",
                            subtitle = if (isBangla) "ক্যালেন্ডারে যেকোনো দিনে ট্যাপ করলে সরাসরি নির্বাচন হবে" else "Tapping any day immediately selects and closes calendar",
                            checked = txConfig.autoSelectDateOnDayClick,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(autoSelectDateOnDayClick = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "টাইম পিকার প্রদর্শন" else "Show Time Picker",
                            subtitle = if (isBangla) "নতুন এন্ট্রিতে নির্দিষ্ট সময় নির্ধারণের সুবিধা" else "Allow editing exact time for entries",
                            checked = txConfig.showTimePicker,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(showTimePicker = it) } }
                        )
                    }

                    // 2. Keyboard & Input Behavior Card
                    SettingsSectionCard(
                        title = if (isBangla) "কিবোর্ড ও ইনপুট সহায়তা" else "Keyboard & Input Behavior",
                        subtitle = if (isBangla) "লেনদেন এন্ট্রিতে দ্রুত লেখার নিয়ম" else "Smart focus and quick input chips",
                        icon = Icons.Default.Keyboard,
                        iconTint = Color(0xFF0284C7)
                    ) {
                        SettingsSwitchRow(
                            title = if (isBangla) "স্বয়ংক্রিয় কিবোর্ড পপআপ" else "Auto-Open Keyboard",
                            subtitle = if (isBangla) "নতুন লেনদেন খোলার সাথে সাথে কিবোর্ড খুলুন" else "Open soft keyboard automatically on entry screen",
                            checked = txConfig.showKeyboardImmediately,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(showKeyboardImmediately = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "টাকার ঘরে সরাসরি ফোকাস" else "Auto-Focus Amount Field",
                            subtitle = if (isBangla) "নামের পরিবর্তে আগে টাকার ইনপুটে ফোকাস করুন" else "Focus the amount field first instead of payee name",
                            checked = txConfig.autoFocusAmount,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(autoFocusAmount = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "টাকার কুইক প্রি-সেট চিপস" else "Quick Amount Presets",
                            subtitle = if (isBangla) "ক্যালকুলেটরে +৫০, +১০০, +৫০০ কুইক বাটন" else "Show +50, +100, +500 amount presets",
                            checked = txConfig.enableQuickAmountPresets,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(enableQuickAmountPresets = it) } }
                        )
                    }

                    // 3. Smart Autofill Navigation Card
                    OutlinedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToAutofill)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isBangla) "স্মার্ট অটোফিল সেটিংস" else "Smart Autofill Settings",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isBangla) "SMS ও নোটিফিকেশন থেকে স্বয়ংক্রিয় ট্র্যাকিং" else "Automatic tracking from SMS & banking alerts",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                1 -> {
                    // =========================================================
                    // TAB 1: Defaults & Display
                    // =========================================================
                    val currentAccount = accounts.firstOrNull { it.id == txConfig.defaultAccountId }
                    val currentIncomeCat = categories.firstOrNull { it.id == txConfig.defaultIncomeCategoryId }
                    val currentExpenseCat = categories.firstOrNull { it.id == txConfig.defaultExpenseCategoryId }

                    // 1. Defaults Card
                    SettingsSectionCard(
                        title = if (isBangla) "ডিফল্ট অ্যাকাউন্ট ও ক্যাটাগরি" else "Default Account & Category",
                        subtitle = if (isBangla) "নতুন লেনদেন তৈরির সময় স্বয়ংক্রিয়ভাবে বসবে" else "Pre-selected defaults for new transaction entries",
                        icon = Icons.Default.AccountBalance,
                        iconTint = SolidPrimary
                    ) {
                        SettingsPickerRow(
                            title = if (isBangla) "ডিফল্ট অ্যাকাউন্ট" else "Default Account",
                            subtitle = currentAccount?.localizedName(languageMode) ?: (if (isBangla) "কোনো ডিফল্ট নেই (ম্যানুয়াল)" else "None (Select manually)"),
                            icon = Icons.Default.AccountBalance,
                            iconTint = SolidPrimary,
                            onClick = { activePicker = TxPickerType.DEFAULT_ACCOUNT }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsPickerRow(
                            title = if (isBangla) "ডিফল্ট আয় ক্যাটাগরি" else "Default Income Category",
                            subtitle = currentIncomeCat?.localizedName(languageMode) ?: (if (isBangla) "কোনো ডিফল্ট নেই (ম্যানুয়াল)" else "None (Select manually)"),
                            icon = Icons.Default.Category,
                            iconTint = SolidIncome,
                            onClick = { activePicker = TxPickerType.DEFAULT_INCOME_CATEGORY }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsPickerRow(
                            title = if (isBangla) "ডিফল্ট খরচ ক্যাটাগরি" else "Default Expense Category",
                            subtitle = currentExpenseCat?.localizedName(languageMode) ?: (if (isBangla) "কোনো ডিফল্ট নেই (ম্যানুয়াল)" else "None (Select manually)"),
                            icon = Icons.Default.Category,
                            iconTint = SolidExpense,
                            onClick = { activePicker = TxPickerType.DEFAULT_EXPENSE_CATEGORY }
                        )
                    }

                    // 2. Unnamed Payee / Title Name Mode Card
                    SettingsSectionCard(
                        title = if (isBangla) "নামহীন লেনদেনের ডিফল্ট নাম" else "Unnamed Payee / Title Name",
                        subtitle = if (isBangla) "নাম না দিলে যা ডিফল্ট শিরোনাম হিসেবে বসবে" else "Fallback title when payee/name is left blank",
                        icon = Icons.Default.EditNote,
                        iconTint = Color(0xFFD97706)
                    ) {
                        val modes = listOf(
                            UnnamedPayeeMode.DEFAULT_OTHERS to (if (isBangla) "অন্যান্য" else "Others"),
                            UnnamedPayeeMode.CATEGORY_NAME to (if (isBangla) "ক্যাটাগরি" else "Category"),
                            UnnamedPayeeMode.ACCOUNT_NAME to (if (isBangla) "অ্যাকাউন্ট" else "Account"),
                            UnnamedPayeeMode.CUSTOM_NAME to (if (isBangla) "কাস্টম" else "Custom")
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            modes.forEach { (mode, label) ->
                                val isSelected = txConfig.unnamedPayeeMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { txPrefs.updateConfig { it.copy(unnamedPayeeMode = mode) } },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = txConfig.unnamedPayeeMode == UnnamedPayeeMode.CUSTOM_NAME,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedTextField(
                                    value = customPayeeDraft,
                                    onValueChange = {
                                        customPayeeDraft = it
                                        txPrefs.updateConfig { cfg -> cfg.copy(customDefaultPayee = it) }
                                    },
                                    label = { Text(if (isBangla) "কাস্টম ডিফল্ট নাম" else "Custom Default Payee Name", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // 3. List Display & Formatting Card
                    SettingsSectionCard(
                        title = if (isBangla) "তালিকা প্রদর্শন ও স্টাইল" else "List Display & Formatting",
                        subtitle = if (isBangla) "লেনদেন তালিকার ঘনত্ব ও আইকন ব্যাজ" else "Item density and visual badge options",
                        icon = Icons.Default.ViewList,
                        iconTint = SolidPrimary
                    ) {
                        Text(
                            text = if (isBangla) "আইটেম ঘনত্ব (Item Density):" else "List Item Density:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isSingle = displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.SINGLE_LINE
                            FilterChip(
                                selected = isSingle,
                                onClick = { displayFormatPrefs.setItemDisplayFormat(ItemDisplayFormat.SINGLE_LINE) },
                                label = {
                                    Text(
                                        text = if (isBangla) "এক লাইন (কম্প্যাক্ট)" else "Single Line",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !isSingle,
                                onClick = { displayFormatPrefs.setItemDisplayFormat(ItemDisplayFormat.TWO_LINES) },
                                label = {
                                    Text(
                                        text = if (isBangla) "দুই লাইন (পূর্ণাঙ্গ)" else "Double Line",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "ক্যাটাগরি আইকন প্রদর্শন" else "Show Category Icons",
                            subtitle = if (isBangla) "তালিকায় ক্যাটাগরির রঙিন আইকন ব্যাজ দেখান" else "Show category icons in transaction lists",
                            checked = txConfig.enableCategoryIcons,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(enableCategoryIcons = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "অ্যাকাউন্ট আইকন ও লেবেল" else "Show Account Badges",
                            subtitle = if (isBangla) "পেমেন্ট অ্যাকাউন্টের আইকন ব্যাজ প্রদর্শন" else "Show account & payment source badges",
                            checked = txConfig.enableAccountIcons,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(enableAccountIcons = it) } }
                        )
                    }

                    // 4. Notes & Labels Display Mode Card
                    SettingsSectionCard(
                        title = if (isBangla) "নোট ও লেবেল ভিউ স্টাইল" else "Notes & Labels Style",
                        subtitle = if (isBangla) "লেনদেন তালিকায় বিবরণ ও ট্যাগ যেভাবে দেখাবে" else "How notes and tags appear in transaction rows",
                        icon = Icons.Default.Label,
                        iconTint = Color(0xFF6366F1)
                    ) {
                        Text(
                            text = if (isBangla) "নোটের ভিউ স্টাইল:" else "Notes display mode:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                NotesDisplayMode.FULL_TEXT to (if (isBangla) "সম্পূর্ণ" else "Full"),
                                NotesDisplayMode.ICON_ONLY to (if (isBangla) "আইকন" else "Icon"),
                                NotesDisplayMode.HIDDEN to (if (isBangla) "লুকান" else "Hidden")
                            ).forEach { (mode, label) ->
                                val isSelected = txConfig.notesDisplayMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { txPrefs.updateConfig { it.copy(notesDisplayMode = mode) } },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        Text(
                            text = if (isBangla) "লেবেলের ভিউ স্টাইল:" else "Labels display mode:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                LabelsDisplayMode.CHIP_BADGE to (if (isBangla) "ট্যাগ" else "Chips"),
                                LabelsDisplayMode.ICON_ONLY to (if (isBangla) "আইকন" else "Icon"),
                                LabelsDisplayMode.HIDDEN to (if (isBangla) "লুকান" else "Hidden")
                            ).forEach { (mode, label) ->
                                val isSelected = txConfig.labelsDisplayMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { txPrefs.updateConfig { it.copy(labelsDisplayMode = mode) } },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // =========================================================
                    // TAB 2: Save & Add Another (+1) / Consecutive Repeat Entry
                    // =========================================================

                    // Info Header Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+1",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = if (isBangla) {
                                    "সংরক্ষণ ও নতুন যোগ (+১) বোতামে চাপ দিলে বর্তমান লেনদেনটি সংরক্ষিত হবে এবং পরবর্তী লেনদেন টাইপ করতে ফর্মটি স্বয়ংক্রিয়ভাবে প্রস্তুত থাকবে।"
                                } else {
                                    "Tapping the (+1) button saves the current transaction and keeps the form open ready for fast consecutive entries."
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Card 1: Retain Parameters on Next Entry
                    SettingsSectionCard(
                        title = if (isBangla) "যা যা মনে রাখবে (Retain)" else "Retain for Next Entry",
                        subtitle = if (isBangla) "পরবর্তী এন্ট্রিতে কোন কোন তথ্য ধরে রাখবে" else "Values preserved when adding consecutive transactions",
                        icon = Icons.Default.Check,
                        iconTint = SolidIncome
                    ) {
                        SettingsSwitchRow(
                            title = if (isBangla) "লেনদেনের ধরন মনে রাখুন" else "Keep Transaction Type",
                            subtitle = if (isBangla) "আয় / ব্যয় / স্থানান্তর অপরিবর্তিত থাকবে" else "Keep Income, Expense, or Transfer",
                            checked = txConfig.plusOneKeepType,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepType = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "ক্যাটাগরি মনে রাখুন" else "Keep Category",
                            subtitle = if (isBangla) "নির্বাচিত ক্যাটাগরি ও সাব-ক্যাটাগরি বহাল থাকবে" else "Preserve selected category & subcategory",
                            checked = txConfig.plusOneKeepCategory,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepCategory = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "অ্যাকাউন্ট মনে রাখুন" else "Keep Account",
                            subtitle = if (isBangla) "নির্বাচিত পেমেন্ট অ্যাকাউন্ট বজায় থাকবে" else "Preserve selected payment account",
                            checked = txConfig.plusOneKeepAccount,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepAccount = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "স্থানান্তর গন্তব্য অ্যাকাউন্ট মনে রাখুন" else "Keep Transfer Destination",
                            subtitle = if (isBangla) "স্থানান্তরের To অ্যাকাউন্ট ধরে রাখবে" else "Preserve destination account for transfers",
                            checked = txConfig.plusOneKeepTransferAccount,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepTransferAccount = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "তারিখ মনে রাখুন" else "Keep Selected Date",
                            subtitle = if (isBangla) "নির্বাচিত তারিখ অপরিবর্তিত থাকবে" else "Preserve selected date for next entry",
                            checked = txConfig.plusOneKeepDate,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepDate = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "সময় মনে রাখুন" else "Keep Selected Time",
                            subtitle = if (isBangla) "সময় রিফ্রেশ না করে আগের সময় রাখবে" else "Keep exact time instead of refreshing",
                            checked = txConfig.plusOneKeepTime,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepTime = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "পেমেন্ট মেথড মনে রাখুন" else "Keep Payment Method",
                            subtitle = if (isBangla) "নির্বাচিত পেমেন্ট মোড বহাল রাখবে" else "Preserve selected payment mode",
                            checked = txConfig.plusOneKeepPaymentMethod,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepPaymentMethod = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "ট্যাগ ও লেবেল মনে রাখুন" else "Keep Tags & Labels",
                            subtitle = if (isBangla) "রেফারেন্স ও লেবেল ট্যাগ ধরে রাখবে" else "Preserve reference and label tags",
                            checked = txConfig.plusOneKeepLabels,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepLabels = it) } }
                        )
                    }

                    // Card 2: Reset Parameters on Next Entry
                    SettingsSectionCard(
                        title = if (isBangla) "যা যা রিসেট করবে (Reset)" else "Reset for Next Entry",
                        subtitle = if (isBangla) "পরবর্তী এন্ট্রির জন্য কোন কোন ঘর খালি করবে" else "Fields cleared for the next transaction",
                        icon = Icons.Default.RestartAlt,
                        iconTint = Color(0xFFEF4444)
                    ) {
                        SettingsSwitchRow(
                            title = if (isBangla) "টাকার পরিমাণ রিসেট করুন" else "Clear Amount",
                            subtitle = if (isBangla) "পরবর্তী এন্ট্রির জন্য টাকার ঘর খালি করবে" else "Reset amount input to 0 for next entry",
                            checked = txConfig.plusOneClearAmount,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneClearAmount = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "প্রাপক / পেয়ি নাম রিসেট করুন" else "Clear Payee / Payer",
                            subtitle = if (isBangla) "পরবর্তী এন্ট্রির জন্য প্রাপকের ঘর খালি করবে" else "Clear payee field for next entry",
                            checked = txConfig.plusOneClearPayee,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneClearPayee = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "নোট / বিবরণ রিসেট করুন" else "Clear Note / Description",
                            subtitle = if (isBangla) "নোটের ঘর মুছে নতুন টাইপের সুযোগ দেবে" else "Clear note field for next entry",
                            checked = txConfig.plusOneClearNote,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneClearNote = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "সংযুক্ত ছবি ও ফাইল রিসেট করুন" else "Clear Photos & Attachments",
                            subtitle = if (isBangla) "ছবি বা রসিদ সংযুক্তি মুছে ফেলবে" else "Remove photo and file attachments",
                            checked = txConfig.plusOneClearAttachment,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneClearAttachment = it) } }
                        )
                    }

                    // Card 3: Sequence & Notification Feedback
                    SettingsSectionCard(
                        title = if (isBangla) "ক্রমিক নম্বর ও ফিডব্যাক" else "Sequence & Notification",
                        subtitle = if (isBangla) "ধারাবাহিক এন্ট্রির ট্র্যাকিং সুবিধা" else "Note sequence counter and save confirmation",
                        icon = Icons.Default.Pin,
                        iconTint = SolidPrimary
                    ) {
                        SettingsSwitchRow(
                            title = if (isBangla) "নোটে অটো ক্রমিক সংখ্যা যোগ (#১, #২)" else "Auto-Increment Sequence in Note",
                            subtitle = if (isBangla) "ধারাবাহিক লেনদেনে #১, #২ কাউন্টার যোগ করবে" else "Appends sequential counter (#1, #2) to notes",
                            checked = txConfig.plusOneAutoIncrementNote,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneAutoIncrementNote = it) } }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        SettingsSwitchRow(
                            title = if (isBangla) "+১ সেভের পর কনফার্মেশন মেসেজ" else "Show Confirmation Toast on (+1)",
                            subtitle = if (isBangla) "নিচে দ্রুত পপআপ বার্তা দেখাবে" else "Quick toast notification when transaction is stored",
                            checked = txConfig.plusOneShowConfirmationToast,
                            onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneShowConfirmationToast = it) } }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // =========================================================================
    // SEARCHABLE MODAL BOTTOM SHEETS FOR PICKING DEFAULTS
    // =========================================================================

    if (activePicker == TxPickerType.DEFAULT_ACCOUNT) {
        AccountPickerBottomSheet(
            title = if (isBangla) "ডিফল্ট অ্যাকাউন্ট নির্বাচন" else "Select Default Account",
            accounts = accounts,
            selectedAccountId = txConfig.defaultAccountId,
            languageMode = languageMode,
            onSelectAccount = { pickedId ->
                txPrefs.updateConfig { it.copy(defaultAccountId = pickedId) }
                activePicker = TxPickerType.NONE
            },
            onDismiss = { activePicker = TxPickerType.NONE }
        )
    }

    if (activePicker == TxPickerType.DEFAULT_INCOME_CATEGORY) {
        CategoryPickerBottomSheet(
            title = if (isBangla) "ডিফল্ট আয় ক্যাটাগরি" else "Select Default Income Category",
            categories = categories.filter { it.type == CategoryType.INCOME },
            selectedCategoryId = txConfig.defaultIncomeCategoryId,
            languageMode = languageMode,
            themeColor = SolidIncome,
            onSelectCategory = { pickedId ->
                txPrefs.updateConfig { it.copy(defaultIncomeCategoryId = pickedId) }
                activePicker = TxPickerType.NONE
            },
            onDismiss = { activePicker = TxPickerType.NONE }
        )
    }

    if (activePicker == TxPickerType.DEFAULT_EXPENSE_CATEGORY) {
        CategoryPickerBottomSheet(
            title = if (isBangla) "ডিফল্ট খরচ ক্যাটাগরি" else "Select Default Expense Category",
            categories = categories.filter { it.type == CategoryType.EXPENSE },
            selectedCategoryId = txConfig.defaultExpenseCategoryId,
            languageMode = languageMode,
            themeColor = SolidExpense,
            onSelectCategory = { pickedId ->
                txPrefs.updateConfig { it.copy(defaultExpenseCategoryId = pickedId) }
                activePicker = TxPickerType.NONE
            },
            onDismiss = { activePicker = TxPickerType.NONE }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 14.5.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun SettingsPickerRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = iconTint,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPickerBottomSheet(
    title: String,
    accounts: List<Account>,
    selectedAccountId: Long?,
    languageMode: LanguageMode,
    onSelectAccount: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val leafAccounts = remember(accounts) {
        val parentIds = accounts.mapNotNull { it.parentId }.toSet()
        accounts.filter { it.isActive && it.id !in parentIds }
    }

    val filtered = remember(leafAccounts, searchQuery) {
        if (searchQuery.isBlank()) leafAccounts
        else leafAccounts.filter {
            it.nameEn.contains(searchQuery, ignoreCase = true) ||
            it.nameBn.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBangla) "অ্যাকাউন্ট অনুসন্ধান..." else "Search accounts...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "None" option
                item {
                    val isNoneSelected = selectedAccountId == null
                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isNoneSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isNoneSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAccount(null) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBangla) "কোনো ডিফল্ট নেই (ম্যানুয়াল নির্বাচন)" else "None (Select manually every time)",
                                fontSize = 12.5.sp,
                                fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNoneSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isNoneSelected) {
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

                items(filtered, key = { it.id }) { acc ->
                    val isSelected = selectedAccountId == acc.id
                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAccount(acc.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(acc.iconName),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = acc.localizedName(languageMode),
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerBottomSheet(
    title: String,
    categories: List<Category>,
    selectedCategoryId: Long?,
    languageMode: LanguageMode,
    themeColor: Color,
    onSelectCategory: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val leafCategories = remember(categories) {
        val parentIds = categories.mapNotNull { it.parentId }.toSet()
        categories.filter { it.isActive && it.id !in parentIds }
    }

    val filtered = remember(leafCategories, searchQuery) {
        if (searchQuery.isBlank()) leafCategories
        else leafCategories.filter {
            it.nameEn.contains(searchQuery, ignoreCase = true) ||
            it.nameBn.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColor)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBangla) "ক্যাটাগরি অনুসন্ধান..." else "Search categories...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "None" option
                item {
                    val isNoneSelected = selectedCategoryId == null
                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isNoneSelected) themeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isNoneSelected) themeColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCategory(null) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBangla) "কোনো ডিফল্ট নেই (ম্যানুয়াল নির্বাচন)" else "None (Select manually every time)",
                                fontSize = 12.5.sp,
                                fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNoneSelected) themeColor else MaterialTheme.colorScheme.onSurface
                            )
                            if (isNoneSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = themeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                items(filtered, key = { it.id }) { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    val parentCat = cat.parentId?.let { pId -> categories.firstOrNull { it.id == pId } }

                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) themeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) themeColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCategory(cat.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(cat.iconName),
                                    contentDescription = null,
                                    tint = if (isSelected) themeColor else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = cat.localizedName(languageMode),
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (parentCat != null) {
                                        Text(
                                            text = parentCat.localizedName(languageMode),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = themeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
