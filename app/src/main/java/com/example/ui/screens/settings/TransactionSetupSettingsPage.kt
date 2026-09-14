package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
            if (isBangla) "ইনপুট ও ডেট" else "Input & Date",
            if (isBangla) "ডিফল্ট মান" else "Defaults & Names",
            if (isBangla) "+১ এবং প্রদর্শন" else "+1 & Display"
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
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: Input & Date
                    // 1. Quick Date Chips
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isBangla) "কুইক ডেট চিপস প্রদর্শন" else "Quick Date Picker",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isBangla) "লেনদেন ইনপুটে দ্রুত তারিখ চয়ন বাটন" else "Fast date selection chips on transaction entry",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "তারিখ চিপস সক্রিয় রাখুন" else "Show Quick Date Picker",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.enableQuickDatePicker,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(enableQuickDatePicker = it) } },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // 2. Default Transaction Time
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isBangla) "লেনদেনের সময় নির্বাচন" else "Show Time Picker",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isBangla) "নতুন এন্ট্রিতে সময় নির্ধারণের সুবিধা" else "Allow editing time for new entries",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "টাইম পিকার প্রদর্শন" else "Show Time Picker",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.showTimePicker,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(showTimePicker = it) } },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // 3. Immediate Keyboard Focus
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isBangla) "স্বয়ংক্রিয় কিবোর্ড পপআপ" else "Immediate Keyboard Focus",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isBangla) "নতুন লেনদেন খোলার সাথে সাথে কিবোর্ড খুলুন" else "Open keyboard instantly on entry screen",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "কিবোর্ড অবিলম্বে চালু" else "Auto-Focus Keyboard",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.showKeyboardImmediately,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(showKeyboardImmediately = it) } },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // 4. Smart Autofill Shortcut Card
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToAutofill)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isBangla) "স্মার্ট অটোফিল সেটিংস" else "Smart Autofill Settings",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isBangla) "SMS ও নোটিফিকেশন থেকে স্বয়ংক্রিয় ট্র্যাকিং" else "Automatic tracking from SMS & banking alerts",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                1 -> {
                    // TAB 1: Defaults & Names
                    val currentAccount = accounts.firstOrNull { it.id == txConfig.defaultAccountId }
                    val currentIncomeCat = categories.firstOrNull { it.id == txConfig.defaultIncomeCategoryId }
                    val currentExpenseCat = categories.firstOrNull { it.id == txConfig.defaultExpenseCategoryId }

                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = if (isBangla) "ডিফল্ট নির্বাচন (নতুন লেনদেন)" else "Default Selections (New Transaction)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // 1. Account Picker Row
                            SettingsPickerRow(
                                title = if (isBangla) "ডিফল্ট অ্যাকাউন্ট" else "Default Account",
                                subtitle = currentAccount?.localizedName(languageMode) ?: (if (isBangla) "কোনো ডিফল্ট নেই (ম্যানুয়াল)" else "None (Select manually)"),
                                icon = Icons.Default.AccountBalance,
                                iconTint = MaterialTheme.colorScheme.primary,
                                onClick = { activePicker = TxPickerType.DEFAULT_ACCOUNT }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 2. Income Category Picker Row
                            SettingsPickerRow(
                                title = if (isBangla) "ডিফল্ট আয়ের ক্যাটাগরি" else "Default Income Category",
                                subtitle = currentIncomeCat?.localizedName(languageMode) ?: (if (isBangla) "কোনো ডিফল্ট নেই (ম্যানুয়াল)" else "None (Select manually)"),
                                icon = Icons.Default.Category,
                                iconTint = SolidIncome,
                                onClick = { activePicker = TxPickerType.DEFAULT_INCOME_CATEGORY }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 3. Expense Category Picker Row
                            SettingsPickerRow(
                                title = if (isBangla) "ডিফল্ট খরচের ক্যাটাগরি" else "Default Expense Category",
                                subtitle = currentExpenseCat?.localizedName(languageMode) ?: (if (isBangla) "কোনো ডিফল্ট নেই (ম্যানুয়াল)" else "None (Select manually)"),
                                icon = Icons.Default.Category,
                                iconTint = SolidExpense,
                                onClick = { activePicker = TxPickerType.DEFAULT_EXPENSE_CATEGORY }
                            )
                        }
                    }

                    // Display Modes Card (Notes, Labels, Unnamed Payee)
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (isBangla) "নোট ও লেবেল প্রদর্শন" else "Notes & Labels Display",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Notes Mode
                            Text(
                                text = if (isBangla) "নোটের ভিউ স্টাইল:" else "Notes display mode:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = txConfig.notesDisplayMode == NotesDisplayMode.FULL_TEXT,
                                    onClick = { txPrefs.updateConfig { it.copy(notesDisplayMode = NotesDisplayMode.FULL_TEXT) } },
                                    label = { Text(if (isBangla) "সম্পূর্ণ নোট" else "Full Text", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = txConfig.notesDisplayMode == NotesDisplayMode.ICON_ONLY,
                                    onClick = { txPrefs.updateConfig { it.copy(notesDisplayMode = NotesDisplayMode.ICON_ONLY) } },
                                    label = { Text(if (isBangla) "আইকন মাত্র" else "Icon Only", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = txConfig.notesDisplayMode == NotesDisplayMode.HIDDEN,
                                    onClick = { txPrefs.updateConfig { it.copy(notesDisplayMode = NotesDisplayMode.HIDDEN) } },
                                    label = { Text(if (isBangla) "লুকান" else "Hidden", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Labels Mode
                            Text(
                                text = if (isBangla) "লেবেলের ভিউ স্টাইল:" else "Labels display mode:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = txConfig.labelsDisplayMode == LabelsDisplayMode.CHIP_BADGE,
                                    onClick = { txPrefs.updateConfig { it.copy(labelsDisplayMode = LabelsDisplayMode.CHIP_BADGE) } },
                                    label = { Text(if (isBangla) "ট্যাগ চিপস" else "Chips", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = txConfig.labelsDisplayMode == LabelsDisplayMode.ICON_ONLY,
                                    onClick = { txPrefs.updateConfig { it.copy(labelsDisplayMode = LabelsDisplayMode.ICON_ONLY) } },
                                    label = { Text(if (isBangla) "আইকন" else "Icon", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = txConfig.labelsDisplayMode == LabelsDisplayMode.HIDDEN,
                                    onClick = { txPrefs.updateConfig { it.copy(labelsDisplayMode = LabelsDisplayMode.HIDDEN) } },
                                    label = { Text(if (isBangla) "লুকান" else "Hidden", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: +1 & Display Settings
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (isBangla) "লেনদেন তালিকা ভিউ ফরম্যাট" else "Transaction List Item Density",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SolidPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isSingle = displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.SINGLE_LINE
                                FilterChip(
                                    selected = isSingle,
                                    onClick = { displayFormatPrefs.setItemDisplayFormat(ItemDisplayFormat.SINGLE_LINE) },
                                    label = { Text(if (isBangla) "এক লাইন (কম্প্যাক্ট)" else "Single Line (Compact)", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = !isSingle,
                                    onClick = { displayFormatPrefs.setItemDisplayFormat(ItemDisplayFormat.TWO_LINES) },
                                    label = { Text(if (isBangla) "দুই লাইন (পূর্ণাঙ্গ)" else "Double Line (Default)", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Text(
                                text = if (isBangla) "+১ (Save and Add Another) বোতামের আচরণ" else "(+1) Save & Add Another Behavior",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "ক্যাটাগরি মনে রাখুন" else "Keep selected Category",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.plusOneKeepCategory,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepCategory = it) } },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "অ্যাকাউন্ট মনে রাখুন" else "Keep selected Account",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.plusOneKeepAccount,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepAccount = it) } },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "তারিখ মনে রাখুন" else "Keep selected Date",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.plusOneKeepDate,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepDate = it) } },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, fontSize = 12.sp, color = iconTint, fontWeight = FontWeight.Medium)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)

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
                                fontSize = 13.sp,
                                fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNoneSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isNoneSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
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
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColor)

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
                                fontSize = 13.sp,
                                fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNoneSelected) themeColor else MaterialTheme.colorScheme.onSurface
                            )
                            if (isNoneSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = themeColor, modifier = Modifier.size(18.dp))
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
                                        fontSize = 13.sp,
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
                                Icon(Icons.Default.Check, contentDescription = null, tint = themeColor, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
