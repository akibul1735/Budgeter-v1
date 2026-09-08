package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DisplayFormatConfig
import com.example.util.DisplayFormatPreferences
import com.example.util.ItemDisplayFormat
import com.example.util.LabelsDisplayMode
import com.example.util.NotesDisplayMode
import com.example.util.TransactionConfig
import com.example.util.TransactionPreferences
import com.example.util.UnnamedPayeeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionSetupDialog(
    accounts: List<Account>,
    categories: List<Category>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onOpenAutofillSettings: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val txPrefs = remember { TransactionPreferences.getInstance(context) }
    val txConfig by txPrefs.config.collectAsStateWithLifecycle()

    val displayFormatPrefs = remember { DisplayFormatPreferences.getInstance(context) }
    val displayFormatConfig by displayFormatPrefs.config.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    val incomeCategories = remember(categories) {
        categories.filter { it.type == CategoryType.INCOME && it.parentId == null }
    }
    val expenseCategories = remember(categories) {
        categories.filter { it.type == CategoryType.EXPENSE && it.parentId == null }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SolidPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isBangla) "লেনদেন সেটিংস" else "Transaction Setup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "ডিফল্ট মান, প্রদর্শন রীতি ও +১ আচরণ" else "Defaults, input rules & (+1) behavior",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 3-Tab Segmented Controls
            val tabs = listOf(
                if (isBangla) "ইনপুট ও ডেট" else "Input & Date",
                if (isBangla) "ডিফল্ট মান" else "Defaults & Names",
                if (isBangla) "+১ এবং প্রদর্শন" else "+1 & Display"
            )

            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SolidPrimary
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> InputAndDateTab(
                        txConfig = txConfig,
                        isBangla = isBangla,
                        onUpdate = { txPrefs.updateConfig(it) },
                        onOpenAutofill = {
                            onDismiss()
                            onOpenAutofillSettings()
                        }
                    )
                    1 -> DefaultsAndNamesTab(
                        txConfig = txConfig,
                        accounts = accounts,
                        incomeCategories = incomeCategories,
                        expenseCategories = expenseCategories,
                        isBangla = isBangla,
                        onUpdate = { txPrefs.updateConfig(it) }
                    )
                    2 -> PlusOneAndDisplayTab(
                        txConfig = txConfig,
                        displayFormatConfig = displayFormatConfig,
                        isBangla = isBangla,
                        onUpdateTxConfig = { txPrefs.updateConfig(it) },
                        onSetItemDisplayFormat = { format ->
                            displayFormatPrefs.setItemDisplayFormat(format)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Tab 0: Input Form & Quick Date Pickers
 */
@Composable
private fun InputAndDateTab(
    txConfig: TransactionConfig,
    isBangla: Boolean,
    onUpdate: ((TransactionConfig) -> TransactionConfig) -> Unit,
    onOpenAutofill: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Date & Time Controls
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Quick Date Picker Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = if (isBangla) "দ্রুত তারিখ নির্বাচন (কুইক চিপস)" else "Quick Date Picker Chips",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBangla) "আজ, গতকাল, গত পরশু দ্রুত বাছাইয়ের অপশন" else "Show Today, Yesterday, Day Before quick chips",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = txConfig.enableQuickDatePicker,
                        onCheckedChange = { onUpdate { cfg -> cfg.copy(enableQuickDatePicker = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Show Time Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = if (isBangla) "সময় নির্বাচন দেখান" else "Show Transaction Time",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBangla) "লেনদেনের নির্দিষ্ট সময় (ঘণ্টা:মিনিট) নির্ধারণ" else "Enable selecting exact time with transactions",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = txConfig.showTimePicker,
                        onCheckedChange = { onUpdate { cfg -> cfg.copy(showTimePicker = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Show Keyboard Immediately Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = if (isBangla) "কিবোর্ড অবিলম্বে দেখান" else "Show Keyboard Immediately",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBangla) "লেনদেন যোগ করার সময় নাম ফিল্ডে সরাসরি কিবোর্ড খুলবে" else "Open keyboard immediately in Name field when adding transaction",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = txConfig.showKeyboardImmediately,
                        onCheckedChange = { onUpdate { cfg -> cfg.copy(showKeyboardImmediately = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }
            }
        }

        // Input Enhancements Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Auto Focus Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = if (isBangla) "স্বয়ংক্রিয় টাকার পরিমাণ ফোকাস" else "Auto-Focus Amount Field",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBangla) "নতুন এন্ট্রিতে টাকার ঘর স্বয়ংক্রিয়ভাবে সক্রিয় হবে" else "Directly focus amount field when opening entry sheet",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = txConfig.autoFocusAmount,
                        onCheckedChange = { onUpdate { cfg -> cfg.copy(autoFocusAmount = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Quick Amount Presets (+10, +50, +100, +500)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = if (isBangla) "টাকার কুইক প্রি-সেট চিপস (+৫০, +১০০)" else "Quick Amount Presets (+50, +100)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBangla) "১-ট্যাপে পরিমাণ যোগ করার বোতাম দেখান" else "Show quick addition amount chips on keypad",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = txConfig.enableQuickAmountPresets,
                        onCheckedChange = { onUpdate { cfg -> cfg.copy(enableQuickAmountPresets = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }
            }
        }

        // Shortcut to Smart Autofill & Categorization
        OutlinedButton(
            onClick = onOpenAutofill,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBangla) "স্মার্ট অটোফিল ও ক্যাটাগরাইজেশন কনফিগার" else "Configure Smart Autofill & Categorization",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SolidPrimary
            )
        }
    }
}

/**
 * Tab 1: Default Accounts, Categories & Unnamed Payee Strategy
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DefaultsAndNamesTab(
    txConfig: TransactionConfig,
    accounts: List<Account>,
    incomeCategories: List<Category>,
    expenseCategories: List<Category>,
    isBangla: Boolean,
    onUpdate: ((TransactionConfig) -> TransactionConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section A: Default Account & Category Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isBangla) "পূর্বনির্ধারিত ডিফল্ট অ্যাকাউন্ট ও ক্যাটাগরি" else "Default Account & Categories",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // 1. Default Account
                SelectorRow(
                    icon = Icons.Default.AccountBalance,
                    title = if (isBangla) "ডিফল্ট অ্যাকাউন্ট" else "Default Account",
                    selectedName = accounts.firstOrNull { it.id == txConfig.defaultAccountId }?.localizedName(if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH)
                        ?: (if (isBangla) "কোনোটি নয় (সর্বশেষ ব্যবহৃত)" else "None (Last Used)"),
                    options = listOf(null to (if (isBangla) "কোনোটি নয় (স্বয়ংক্রিয়)" else "None (Auto)")) +
                            accounts.map { it.id to it.localizedName(if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH) },
                    onSelect = { id -> onUpdate { it.copy(defaultAccountId = id) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // 2. Default Expense Category
                SelectorRow(
                    icon = Icons.Default.Category,
                    title = if (isBangla) "ডিফল্ট খরচ ক্যাটাগরি" else "Default Expense Category",
                    selectedName = expenseCategories.firstOrNull { it.id == txConfig.defaultExpenseCategoryId }?.localizedName(if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH)
                        ?: (if (isBangla) "কোনোটি নয়" else "None"),
                    options = listOf(null to (if (isBangla) "কোনোটি নয়" else "None")) +
                            expenseCategories.map { it.id to it.localizedName(if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH) },
                    onSelect = { id -> onUpdate { it.copy(defaultExpenseCategoryId = id) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // 3. Default Income Category
                SelectorRow(
                    icon = Icons.Default.Category,
                    title = if (isBangla) "ডিফল্ট আয় ক্যাটাগরি" else "Default Income Category",
                    selectedName = incomeCategories.firstOrNull { it.id == txConfig.defaultIncomeCategoryId }?.localizedName(if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH)
                        ?: (if (isBangla) "কোনোটি নয়" else "None"),
                    options = listOf(null to (if (isBangla) "কোনোটি নয়" else "None")) +
                            incomeCategories.map { it.id to it.localizedName(if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH) },
                    onSelect = { id -> onUpdate { it.copy(defaultIncomeCategoryId = id) } }
                )
            }
        }

        // Section B: Unnamed / Blank Payee Strategy
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBangla) "নামহীন লেনদেনের ডিফল্ট নাম / প্রাপক" else "Payee for Unnamed Transactions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isBangla)
                        "লেনদেনের বিবরণ বা প্রাপকের নাম খালি থাকলে স্বয়ংক্রিয়ভাবে কোন নামটি যুক্ত হবে:"
                    else
                        "Select default value when payee / title field is left blank:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                val payeeModes = listOf(
                    UnnamedPayeeMode.DEFAULT_OTHERS to (if (isBangla) "Others / অন্যান্য" else "Default (Others)"),
                    UnnamedPayeeMode.CATEGORY_NAME to (if (isBangla) "ক্যাটাগরির নাম" else "Category Name"),
                    UnnamedPayeeMode.ACCOUNT_NAME to (if (isBangla) "অ্যাকাউন্টের নাম" else "Account Name"),
                    UnnamedPayeeMode.CUSTOM_NAME to (if (isBangla) "নিজস্ব নাম" else "Custom Text")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    payeeModes.forEach { (mode, label) ->
                        val isSelected = txConfig.unnamedPayeeMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdate { it.copy(unnamedPayeeMode = mode) } },
                            label = { Text(label, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = SolidPrimary
                            )
                        )
                    }
                }

                if (txConfig.unnamedPayeeMode == UnnamedPayeeMode.CUSTOM_NAME) {
                    OutlinedTextField(
                        value = txConfig.customDefaultPayee,
                        onValueChange = { str -> onUpdate { it.copy(customDefaultPayee = str) } },
                        label = { Text(if (isBangla) "ডিফল্ট নাম লিখুন" else "Custom Default Payee") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Tab 2: Save & Add Another (+1) Behavior & Transaction Display Formats
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlusOneAndDisplayTab(
    txConfig: TransactionConfig,
    displayFormatConfig: DisplayFormatConfig,
    isBangla: Boolean,
    onUpdateTxConfig: ((TransactionConfig) -> TransactionConfig) -> Unit,
    onSetItemDisplayFormat: (ItemDisplayFormat) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section 1: Save & Add Another (+1) Parameter Clearing Rules
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PlusOne, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(22.dp))
                    Column {
                        Text(
                            text = if (isBangla) "সংরক্ষণ ও নতুন যোগ (+১) নিয়ম" else "Save & Add Another (+1) Rules",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isBangla) "পরবর্তী এন্ট্রির আগে কোন মানগুলো মুছে ফেলা বা রাখা হবে" else "Choose parameters to clear or keep for the next entry",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Clear Amount
                PlusOneToggleRow(
                    title = if (isBangla) "টাকার পরিমাণ মুছে ফেলুন" else "Clear Amount Field",
                    subtitle = if (isBangla) "নতুন এন্ট্রিতে টাকার ঘর শূন্য হবে" else "Reset amount to 0 for next entry",
                    checked = txConfig.plusOneClearAmount,
                    onCheckedChange = { onUpdateTxConfig { cfg -> cfg.copy(plusOneClearAmount = it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Clear Note
                PlusOneToggleRow(
                    title = if (isBangla) "নোট / মন্তব্য মুছে ফেলুন" else "Clear Note Field",
                    subtitle = if (isBangla) "পরবর্তী এন্ট্রিতে নোট ফিল্ড খালি হবে" else "Clear custom transaction note",
                    checked = txConfig.plusOneClearNote,
                    onCheckedChange = { onUpdateTxConfig { cfg -> cfg.copy(plusOneClearNote = it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Clear Payee / Title
                PlusOneToggleRow(
                    title = if (isBangla) "প্রাপক / বিবরণ মুছে ফেলুন" else "Clear Payee / Title Field",
                    subtitle = if (isBangla) "নতুন এন্ট্রিতে নামের ঘর খালি হবে" else "Clear payee / transaction title",
                    checked = txConfig.plusOneClearPayee,
                    onCheckedChange = { onUpdateTxConfig { cfg -> cfg.copy(plusOneClearPayee = it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Keep Category
                PlusOneToggleRow(
                    title = if (isBangla) "বর্তমান ক্যাটাগরি বহাল রাখুন" else "Keep Selected Category",
                    subtitle = if (isBangla) "পরের এন্ট্রিতেও একই ক্যাটাগরি নির্বাচিত থাকবে" else "Preserve currently chosen category",
                    checked = txConfig.plusOneKeepCategory,
                    onCheckedChange = { onUpdateTxConfig { cfg -> cfg.copy(plusOneKeepCategory = it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Keep Account
                PlusOneToggleRow(
                    title = if (isBangla) "বর্তমান অ্যাকাউন্ট বহাল রাখুন" else "Keep Selected Account",
                    subtitle = if (isBangla) "পরের এন্ট্রিতেও একই পেমেন্ট সোর্স নির্বাচিত থাকবে" else "Preserve source / destination account",
                    checked = txConfig.plusOneKeepAccount,
                    onCheckedChange = { onUpdateTxConfig { cfg -> cfg.copy(plusOneKeepAccount = it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Keep Date
                PlusOneToggleRow(
                    title = if (isBangla) "নির্বাচিত তারিখ বহাল রাখুন" else "Keep Selected Date",
                    subtitle = if (isBangla) "আগের এন্ট্রির নির্বাচিত তারিখ অপরিবর্তিত থাকবে" else "Keep currently selected date",
                    checked = txConfig.plusOneKeepDate,
                    onCheckedChange = { onUpdateTxConfig { cfg -> cfg.copy(plusOneKeepDate = it) } }
                )
            }
        }

        // Section 2: List Display Icons, Notes & Labels
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBangla) "লেনদেন তালিকায় নোট ও লেবেল প্রদর্শন" else "List Display: Notes & Labels",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Notes display mode
                Text(
                    text = if (isBangla) "নোট প্রদর্শন রীতি" else "Notes Display Style",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline
                )

                val noteModes = listOf(
                    NotesDisplayMode.FULL_TEXT to (if (isBangla) "পূর্ণ টেক্সট" else "Full Text"),
                    NotesDisplayMode.ICON_ONLY to (if (isBangla) "আইকন ব্যাজ" else "Icon Badge"),
                    NotesDisplayMode.HIDDEN to (if (isBangla) "লুকান" else "Hidden")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    noteModes.forEach { (mode, label) ->
                        val isSelected = txConfig.notesDisplayMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateTxConfig { it.copy(notesDisplayMode = mode) } },
                            label = { Text(label, fontSize = 11.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Labels display mode
                Text(
                    text = if (isBangla) "লেবেল / ট্যাগ প্রদর্শন রীতি" else "Labels / Tags Display Style",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline
                )

                val labelModes = listOf(
                    LabelsDisplayMode.CHIP_BADGE to (if (isBangla) "#ট্যাগ চিপ" else "#Tag Chip"),
                    LabelsDisplayMode.ICON_ONLY to (if (isBangla) "আইকন ব্যাজ" else "Tag Icon"),
                    LabelsDisplayMode.HIDDEN to (if (isBangla) "লুকান" else "Hidden")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    labelModes.forEach { (mode, label) ->
                        val isSelected = txConfig.labelsDisplayMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateTxConfig { it.copy(labelsDisplayMode = mode) } },
                            label = { Text(label, fontSize = 11.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            } else null
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Icons toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "ক্যাটাগরি ও অ্যাকাউন্ট আইকন সক্ষম" else "Enable Category & Account Icons",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isBangla) "তালিকায় রঙিন প্রতীকী আইকন প্রদর্শন করুন" else "Show graphic icons in transaction list rows",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = txConfig.enableCategoryIcons,
                        onCheckedChange = { onUpdateTxConfig { cfg -> cfg.copy(enableCategoryIcons = it, enableAccountIcons = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Line Display format
                Text(
                    text = if (isBangla) "আইটেম ডিসপ্লে ফরম্যাট" else "Item Display Format",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.TWO_LINES,
                        onClick = { onSetItemDisplayFormat(ItemDisplayFormat.TWO_LINES) },
                        label = { Text(if (isBangla) "দ্বি-সারি (Double-Line)" else "Double-Line", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.SINGLE_LINE,
                        onClick = { onSetItemDisplayFormat(ItemDisplayFormat.SINGLE_LINE) },
                        label = { Text(if (isBangla) "একক সারি (Single-Line)" else "Single-Line", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlusOneToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
        )
    }
}

@Composable
private fun SelectorRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    selectedName: String,
    options: List<Pair<Long?, String>>,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(text = selectedName, fontSize = 12.sp, color = SolidPrimary, fontWeight = FontWeight.Medium)
            }
        }

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Select", fontSize = 12.sp)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name, fontSize = 13.sp) },
                        onClick = {
                            onSelect(id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
