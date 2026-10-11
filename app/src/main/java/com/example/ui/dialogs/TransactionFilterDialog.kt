package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.components.DropdownItem
import com.example.ui.components.UnifiedFilterDialogContainer
import com.example.ui.components.UnifiedFilterFooter
import com.example.ui.components.UnifiedFilterGroup
import com.example.ui.components.UnifiedFilterHeader
import com.example.ui.components.UnifiedFilterItem
import com.example.ui.components.UnifiedFilterSection
import com.example.ui.components.UnifiedFilterShortcutBar
import com.example.ui.components.UnifiedGroupedMultiSelectDropdown
import com.example.ui.components.UnifiedMultiSelectDropdown
import com.example.ui.components.UnifiedShortcutItem
import com.example.ui.components.UnifiedSingleSelectDropdown
import com.example.ui.screens.LedgerDatePreset
import com.example.ui.screens.LedgerRowStyle
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import org.json.JSONArray
import org.json.JSONObject

/**
 * Filter Popup for Transactions (Ledger) tab.
 * Built using the EXACT Budget tab filter layout design (UnifiedFilterDialogContainer,
 * UnifiedFilterHeader, UnifiedFilterShortcutBar, UnifiedFilterSection, Dropdowns, and UnifiedFilterFooter).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionFilterDialog(
    currentFilter: TransactionTabFilter,
    categories: List<Category>,
    accounts: List<Account>,
    allLabels: List<String> = emptyList(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApplyFilter: (TransactionTabFilter) -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA
    var tempFilter by remember { mutableStateOf(currentFilter) }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showOpenPresetDialog by remember { mutableStateOf(false) }

    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }

    val savedPresets = remember {
        mutableStateListOf<SavedTransactionFilterPreset>().apply {
            addAll(TransactionFilterPresetsStorage.loadPresets(context))
        }
    }

    // -------------------------------------------------------------------------
    // Categories Grouped for UnifiedGroupedMultiSelectDropdown
    // -------------------------------------------------------------------------
    val categoryFilterGroups = remember(categories, languageMode) {
        val activeCats = categories.filter { it.isActive }
        val parentCats = activeCats.filter { it.parentId == null }
        val childCats = activeCats.filter { it.parentId != null }
        val childrenByParent = childCats.groupBy { it.parentId }

        val groupList = mutableListOf<UnifiedFilterGroup<Long>>()

        parentCats.forEach { parent ->
            val children = childrenByParent[parent.id] ?: emptyList()
            val parentName = if (isBangla) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn
            val parentColor = try {
                if (parent.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(parent.colorHex)) else null
            } catch (_: Exception) { null }

            if (children.isNotEmpty()) {
                val items = children.map { child ->
                    val childName = if (isBangla) child.nameBn.ifBlank { child.nameEn } else child.nameEn
                    val childColor = try {
                        if (child.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(child.colorHex)) else parentColor
                    } catch (_: Exception) { parentColor }

                    UnifiedFilterItem(
                        id = child.id,
                        title = childName,
                        subtitle = if (child.type == CategoryType.EXPENSE) (if (isBangla) "ব্যয়" else "Expense") else (if (isBangla) "আয়" else "Income"),
                        color = childColor
                    )
                }
                groupList.add(
                    UnifiedFilterGroup(
                        groupKey = "cat_parent_${parent.id}",
                        groupTitle = parentName,
                        groupSubtitle = if (parent.type == CategoryType.EXPENSE) (if (isBangla) "ব্যয় গ্রুপ" else "Expense Group") else (if (isBangla) "আয় গ্রুপ" else "Income Group"),
                        color = parentColor,
                        items = items
                    )
                )
            } else {
                val singleItem = UnifiedFilterItem(
                    id = parent.id,
                    title = parentName,
                    subtitle = if (parent.type == CategoryType.EXPENSE) (if (isBangla) "ব্যয়" else "Expense") else (if (isBangla) "আয়" else "Income"),
                    color = parentColor
                )
                groupList.add(
                    UnifiedFilterGroup(
                        groupKey = "cat_parent_${parent.id}",
                        groupTitle = parentName,
                        color = parentColor,
                        items = listOf(singleItem)
                    )
                )
            }
        }

        val orphaned = childCats.filter { child -> parentCats.none { it.id == child.parentId } }
        if (orphaned.isNotEmpty()) {
            val items = orphaned.map { child ->
                val childName = if (isBangla) child.nameBn.ifBlank { child.nameEn } else child.nameEn
                UnifiedFilterItem(
                    id = child.id,
                    title = childName,
                    subtitle = if (child.type == CategoryType.EXPENSE) (if (isBangla) "ব্যয়" else "Expense") else (if (isBangla) "আয়" else "Income")
                )
            }
            groupList.add(
                UnifiedFilterGroup(
                    groupKey = "cat_orphaned",
                    groupTitle = if (isBangla) "অন্যান্য ক্যাটাগরি" else "Other Categories",
                    items = items
                )
            )
        }

        groupList
    }

    // -------------------------------------------------------------------------
    // Accounts Grouped for UnifiedGroupedMultiSelectDropdown
    // -------------------------------------------------------------------------
    val accountFilterGroups = remember(accounts, languageMode) {
        val activeAccs = accounts.filter { it.isActive }
        val parentAccs = activeAccs.filter { it.parentId == null }
        val childAccs = activeAccs.filter { it.parentId != null }
        val childrenByParent = childAccs.groupBy { it.parentId }

        val groupList = mutableListOf<UnifiedFilterGroup<Long>>()

        if (childAccs.isNotEmpty()) {
            parentAccs.forEach { parent ->
                val children = childrenByParent[parent.id] ?: emptyList()
                val parentName = if (isBangla) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn
                val parentColor = try {
                    if (parent.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(parent.colorHex)) else null
                } catch (_: Exception) { null }

                if (children.isNotEmpty()) {
                    val items = children.map { child ->
                        val childName = if (isBangla) child.nameBn.ifBlank { child.nameEn } else child.nameEn
                        val childColor = try {
                            if (child.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(child.colorHex)) else parentColor
                        } catch (_: Exception) { parentColor }

                        UnifiedFilterItem(
                            id = child.id,
                            title = childName,
                            subtitle = child.accountNumber.ifBlank { child.type.name },
                            color = childColor
                        )
                    }
                    groupList.add(
                        UnifiedFilterGroup(
                            groupKey = "acc_parent_${parent.id}",
                            groupTitle = parentName,
                            groupSubtitle = parent.type.name,
                            color = parentColor,
                            items = items
                        )
                    )
                } else {
                    val singleItem = UnifiedFilterItem(
                        id = parent.id,
                        title = parentName,
                        subtitle = parent.accountNumber.ifBlank { parent.type.name },
                        color = parentColor
                    )
                    groupList.add(
                        UnifiedFilterGroup(
                            groupKey = "acc_parent_${parent.id}",
                            groupTitle = parentName,
                            color = parentColor,
                            items = listOf(singleItem)
                        )
                    )
                }
            }
        } else {
            // Group by Account Type
            val byType = activeAccs.groupBy { it.type }
            AccountType.values().forEach { type ->
                val accList = byType[type] ?: emptyList()
                if (accList.isNotEmpty()) {
                    val groupTitle = when (type) {
                        AccountType.ASSET -> if (isBangla) "সম্পদ (Assets)" else "Assets"
                        AccountType.LIABILITY -> if (isBangla) "দায় (Liabilities)" else "Liabilities"
                        AccountType.EQUITY -> if (isBangla) "মালিকানাস্বত্ব (Equity)" else "Equity"
                        AccountType.INCOME -> if (isBangla) "আয় (Income)" else "Income"
                        AccountType.EXPENSE -> if (isBangla) "ব্যয় (Expense)" else "Expense"
                    }
                    val groupColor = when (type) {
                        AccountType.ASSET -> Color(0xFF10B981)
                        AccountType.LIABILITY -> Color(0xFFEF4444)
                        AccountType.EQUITY -> Color(0xFF6366F1)
                        AccountType.INCOME -> Color(0xFF10B981)
                        AccountType.EXPENSE -> Color(0xFFEF4444)
                    }
                    val items = accList.map { acc ->
                        val accName = if (isBangla) acc.nameBn.ifBlank { acc.nameEn } else acc.nameEn
                        UnifiedFilterItem(
                            id = acc.id,
                            title = accName,
                            subtitle = acc.accountNumber.ifBlank { acc.type.name },
                            color = try {
                                if (acc.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(acc.colorHex)) else groupColor
                            } catch (_: Exception) { groupColor }
                        )
                    }
                    groupList.add(
                        UnifiedFilterGroup(
                            groupKey = "acc_type_${type.name}",
                            groupTitle = groupTitle,
                            color = groupColor,
                            items = items
                        )
                    )
                }
            }
        }
        groupList
    }

    // -------------------------------------------------------------------------
    // Dropdown Items
    // -------------------------------------------------------------------------
    val datePresetDropdownItems = remember(languageMode) {
        listOf(
            Pair(LedgerDatePreset.LAST_12_MONTHS, if (isBangla) "গত ১২ মাস (Last 12 Months)" else "Last 12 Months"),
            Pair(LedgerDatePreset.THIS_MONTH, if (isBangla) "চলতি মাস (This Month)" else "This Month"),
            Pair(LedgerDatePreset.LAST_MONTH, if (isBangla) "গত মাস (Last Month)" else "Last Month"),
            Pair(LedgerDatePreset.TODAY, if (isBangla) "আজ (Today)" else "Today"),
            Pair(LedgerDatePreset.YESTERDAY, if (isBangla) "গতকাল (Yesterday)" else "Yesterday"),
            Pair(LedgerDatePreset.THIS_WEEK, if (isBangla) "চলতি সপ্তাহ (This Week)" else "This Week"),
            Pair(LedgerDatePreset.LAST_WEEK, if (isBangla) "গত সপ্তাহ (Last Week)" else "Last Week"),
            Pair(LedgerDatePreset.THIS_QUARTER, if (isBangla) "চলতি কোয়ার্টার (This Quarter)" else "This Quarter"),
            Pair(LedgerDatePreset.THIS_YEAR, if (isBangla) "চলতি বছর (This Year)" else "This Year"),
            Pair(LedgerDatePreset.LAST_YEAR, if (isBangla) "গত বছর (Last Year)" else "Last Year"),
            Pair(LedgerDatePreset.SINCE_LAST_YEAR, if (isBangla) "গত বছর থেকে (Since Last Year)" else "Since Last Year"),
            Pair(LedgerDatePreset.ALL_TIME, if (isBangla) "সব সময় (All Time)" else "All Time"),
            Pair(LedgerDatePreset.CUSTOM, if (isBangla) "কাস্টম সময়কাল (Custom Date Range)" else "Custom Date Range")
        ).map { (preset, label) ->
            DropdownItem(
                id = preset,
                title = label
            )
        }
    }

    val statusDropdownItems = remember(languageMode) {
        listOf(
            Pair(TransactionStatus.CLEARED, if (isBangla) "ক্লিয়ার্ড (Cleared)" else "Cleared"),
            Pair(TransactionStatus.RECONCILED, if (isBangla) "রিকনসাইল্ড (Reconciled)" else "Reconciled"),
            Pair(TransactionStatus.NONE, if (isBangla) "অনির্ধারিত (Unspecified/None)" else "None")
        ).map { (status, label) ->
            DropdownItem(
                id = status.ordinal.toLong(),
                title = label
            )
        }
    }

    val selectedStatusIds = remember(tempFilter.selectedStatuses) {
        tempFilter.selectedStatuses.map { it.ordinal.toLong() }.toSet()
    }

    val labelDropdownItems = remember(allLabels) {
        allLabels.map { lbl ->
            DropdownItem(
                id = lbl.hashCode().toLong(),
                title = lbl
            )
        }
    }

    val selectedLabelIds = remember(tempFilter.labelOrReference, allLabels) {
        val current = tempFilter.labelOrReference
        if (!current.isNullOrBlank() && allLabels.contains(current)) {
            setOf(current.hashCode().toLong())
        } else emptySet()
    }

    // -------------------------------------------------------------------------
    // 1-Tap Quick Filter Shortcuts (Parity with Budget tab)
    // -------------------------------------------------------------------------
    val shortcuts = remember(tempFilter, isBangla) {
        listOf(
            UnifiedShortcutItem(
                id = "this_month",
                label = if (isBangla) "চলতি মাস" else "This Month",
                icon = Icons.Default.CalendarMonth,
                isActive = tempFilter.datePreset == LedgerDatePreset.THIS_MONTH,
                onClick = {
                    tempFilter = if (tempFilter.datePreset == LedgerDatePreset.THIS_MONTH) {
                        tempFilter.copy(datePreset = LedgerDatePreset.LAST_12_MONTHS)
                    } else {
                        tempFilter.copy(datePreset = LedgerDatePreset.THIS_MONTH)
                    }
                }
            ),
            UnifiedShortcutItem(
                id = "only_expense",
                label = if (isBangla) "ব্যয় মাত্র" else "Expense Only",
                color = SolidExpense,
                isActive = tempFilter.selectedTypes == setOf(TransactionType.EXPENSE),
                onClick = {
                    tempFilter = if (tempFilter.selectedTypes == setOf(TransactionType.EXPENSE)) {
                        tempFilter.copy(selectedTypes = emptySet())
                    } else {
                        tempFilter.copy(selectedTypes = setOf(TransactionType.EXPENSE))
                    }
                }
            ),
            UnifiedShortcutItem(
                id = "only_income",
                label = if (isBangla) "আয় মাত্র" else "Income Only",
                color = SolidIncome,
                isActive = tempFilter.selectedTypes == setOf(TransactionType.INCOME),
                onClick = {
                    tempFilter = if (tempFilter.selectedTypes == setOf(TransactionType.INCOME)) {
                        tempFilter.copy(selectedTypes = emptySet())
                    } else {
                        tempFilter.copy(selectedTypes = setOf(TransactionType.INCOME))
                    }
                }
            ),
            UnifiedShortcutItem(
                id = "only_transfer",
                label = if (isBangla) "স্থানান্তর" else "Transfer",
                color = SolidTransfer,
                isActive = tempFilter.selectedTypes == setOf(TransactionType.TRANSFER),
                onClick = {
                    tempFilter = if (tempFilter.selectedTypes == setOf(TransactionType.TRANSFER)) {
                        tempFilter.copy(selectedTypes = emptySet())
                    } else {
                        tempFilter.copy(selectedTypes = setOf(TransactionType.TRANSFER))
                    }
                }
            ),
            UnifiedShortcutItem(
                id = "high_value",
                label = if (isBangla) "উচ্চ মান (≥১০হাজার)" else "High Value (≥10k)",
                icon = Icons.Default.MonetizationOn,
                isActive = tempFilter.onlyHighValue,
                onClick = {
                    tempFilter = tempFilter.copy(onlyHighValue = !tempFilter.onlyHighValue)
                }
            ),
            UnifiedShortcutItem(
                id = "with_notes",
                label = if (isBangla) "নোট সহ" else "With Notes",
                icon = Icons.Default.Description,
                isActive = tempFilter.onlyWithReceiptOrNote,
                onClick = {
                    tempFilter = tempFilter.copy(
                        onlyWithReceiptOrNote = !tempFilter.onlyWithReceiptOrNote,
                        onlyWithoutNote = false
                    )
                }
            )
        )
    }

    val activeCount = tempFilter.activeCount

    // -------------------------------------------------------------------------
    // Unified Modal Container (Anchored Bottom Sheet Modal)
    // -------------------------------------------------------------------------
    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "transaction_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified Header (Budget Tab layout)
            UnifiedFilterHeader(
                title = if (isBangla) "লেনদেন ফিল্টার" else "Transaction Filter",
                activeCount = activeCount,
                languageMode = languageMode,
                onReset = {
                    tempFilter = TransactionTabFilter()
                    Toast.makeText(
                        context,
                        if (isBangla) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset to default",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onSavePreset = { showSavePresetDialog = true },
                onOpenPreset = { showOpenPresetDialog = true },
                onDismiss = onDismiss
            )

            // Horizontal 1-tap Shortcut Bar
            UnifiedFilterShortcutBar(shortcuts = shortcuts)

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Transaction Type Chips (Expense, Income, Transfer)
                UnifiedFilterSection(
                    icon = Icons.Default.SwapHoriz,
                    title = if (isBangla) "লেনদেনের ধরন" else "Transaction Type"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf(
                            Triple(TransactionType.EXPENSE, if (isBangla) "ব্যয়" else "Expense", SolidExpense),
                            Triple(TransactionType.INCOME, if (isBangla) "আয়" else "Income", SolidIncome),
                            Triple(TransactionType.TRANSFER, if (isBangla) "স্থানান্তর" else "Transfer", SolidTransfer)
                        )

                        types.forEach { (type, label, color) ->
                            val isSelected = tempFilter.selectedTypes.contains(type)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val currentTypes = tempFilter.selectedTypes.toMutableSet()
                                    if (isSelected) currentTypes.remove(type) else currentTypes.add(type)
                                    tempFilter = tempFilter.copy(selectedTypes = currentTypes)
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(color, CircleShape)
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.18f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    selectedBorderColor = color
                                ),
                                shape = RoundedCornerShape(9.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Date Range Dropdown
                UnifiedFilterSection(
                    icon = Icons.Default.DateRange,
                    title = if (isBangla) "সময়কাল" else "Date Range"
                ) {
                    UnifiedSingleSelectDropdown(
                        label = if (isBangla) "সময়কাল নির্বাচন করুন" else "Select Date Range",
                        items = datePresetDropdownItems,
                        selectedId = tempFilter.datePreset,
                        onSelectionChanged = { tempFilter = tempFilter.copy(datePreset = it) },
                        leadingIcon = Icons.Default.DateRange,
                        languageMode = languageMode
                    )

                    // Custom Date Range Pickers (shown when Custom is selected)
                    if (tempFilter.datePreset == LedgerDatePreset.CUSTOM) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isBangla) "কাস্টম সময়কাল নির্ধারণ" else "Select Custom Date Range",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showCustomStartDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val startText = if (tempFilter.customStartDateMs > 0L) {
                                            DateUtils.formatDate(tempFilter.customStartDateMs, languageMode)
                                        } else {
                                            if (isBangla) "শুরুর তারিখ" else "Start Date"
                                        }
                                        Text(
                                            text = startText,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showCustomEndDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val endText = if (tempFilter.customEndDateMs > 0L) {
                                            DateUtils.formatDate(tempFilter.customEndDateMs, languageMode)
                                        } else {
                                            if (isBangla) "শেষ তারিখ" else "End Date"
                                        }
                                        Text(
                                            text = endText,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Categories & Accounts side-by-side in compact 2-column row (Exact Budget tab layout)
                UnifiedFilterSection(
                    icon = Icons.Default.Category,
                    title = if (isBangla) "ক্যাটাগরি ও অ্যাকাউন্ট" else "Categories & Accounts"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            UnifiedGroupedMultiSelectDropdown(
                                label = if (isBangla) "ক্যাটাগরি" else "Categories",
                                groups = categoryFilterGroups,
                                selectedIds = tempFilter.selectedCategoryIds,
                                onSelectionChanged = { tempFilter = tempFilter.copy(selectedCategoryIds = it) },
                                placeholder = if (isBangla) "(সকল)" else "(All)",
                                leadingIcon = Icons.Default.Category,
                                languageMode = languageMode
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            UnifiedGroupedMultiSelectDropdown(
                                label = if (isBangla) "অ্যাকাউন্ট" else "Accounts",
                                groups = accountFilterGroups,
                                selectedIds = tempFilter.selectedAccountIds,
                                onSelectionChanged = { tempFilter = tempFilter.copy(selectedAccountIds = it) },
                                placeholder = if (isBangla) "(সকল)" else "(All)",
                                leadingIcon = Icons.Default.AccountBalanceWallet,
                                languageMode = languageMode
                            )
                        }
                    }
                }

                // 4. Status & Labels side-by-side in compact 2-column row (Exact Budget tab layout)
                UnifiedFilterSection(
                    icon = Icons.Default.Check,
                    title = if (isBangla) "স্ট্যাটাস ও লেবেল" else "Status & Labels"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            UnifiedMultiSelectDropdown(
                                label = if (isBangla) "স্ট্যাটাস" else "Status",
                                items = statusDropdownItems,
                                selectedIds = selectedStatusIds,
                                onSelectionChanged = { newIds ->
                                    val selectedStatuses = TransactionStatus.entries.filter { newIds.contains(it.ordinal.toLong()) }.toSet()
                                    tempFilter = tempFilter.copy(selectedStatuses = selectedStatuses)
                                },
                                placeholder = if (isBangla) "(সকল)" else "(All)",
                                leadingIcon = Icons.Default.Check,
                                languageMode = languageMode
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (allLabels.isNotEmpty()) {
                                UnifiedMultiSelectDropdown(
                                    label = if (isBangla) "লেবেল" else "Labels",
                                    items = labelDropdownItems,
                                    selectedIds = selectedLabelIds,
                                    onSelectionChanged = { newIds ->
                                        val selectedString = allLabels.firstOrNull { newIds.contains(it.hashCode().toLong()) }
                                        tempFilter = tempFilter.copy(labelOrReference = selectedString)
                                    },
                                    placeholder = if (isBangla) "(সকল)" else "(All)",
                                    leadingIcon = Icons.Default.Label,
                                    languageMode = languageMode
                                )
                            } else {
                                // Manual Reference / Note text field
                                var labelInput by remember(tempFilter.labelOrReference) {
                                    mutableStateOf(tempFilter.labelOrReference ?: "")
                                }
                                OutlinedTextField(
                                    value = labelInput,
                                    onValueChange = {
                                        labelInput = it
                                        tempFilter = tempFilter.copy(labelOrReference = it.ifBlank { null })
                                    },
                                    label = { Text(if (isBangla) "লেবেল / রেফারেন্স" else "Label / Ref", fontSize = 10.5.sp) },
                                    placeholder = { Text(if (isBangla) "(সকল)" else "(All)", fontSize = 11.5.sp) },
                                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                    trailingIcon = {
                                        if (labelInput.isNotBlank()) {
                                            IconButton(onClick = {
                                                labelInput = ""
                                                tempFilter = tempFilter.copy(labelOrReference = null)
                                            }, modifier = Modifier.size(20.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // 5. Amount Range (Min & Max Bounds)
                UnifiedFilterSection(
                    icon = Icons.Default.MonetizationOn,
                    title = if (isBangla) "পরিমাণের সীমা (৳)" else "Amount Range (৳)"
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                var minInput by remember(tempFilter.minAmount) {
                                    mutableStateOf(tempFilter.minAmount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
                                }
                                var maxInput by remember(tempFilter.maxAmount) {
                                    mutableStateOf(tempFilter.maxAmount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
                                }

                                OutlinedTextField(
                                    value = minInput,
                                    onValueChange = {
                                        minInput = it
                                        val parsed = it.toDoubleOrNull()
                                        tempFilter = tempFilter.copy(minAmount = parsed)
                                    },
                                    label = { Text(if (isBangla) "সর্বনিম্ন (Min ৳)" else "Min ৳", fontSize = 10.5.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = maxInput,
                                    onValueChange = {
                                        maxInput = it
                                        val parsed = it.toDoubleOrNull()
                                        tempFilter = tempFilter.copy(maxAmount = parsed)
                                    },
                                    label = { Text(if (isBangla) "সর্বোচ্চ (Max ৳)" else "Max ৳", fontSize = 10.5.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Quick Amount Presets
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val amountPresets = listOf(
                                    Pair("≥ ১,০০০ ৳", 1000.0),
                                    Pair("≥ ৫,০০০ ৳", 5000.0),
                                    Pair("≥ ১০,০০০ ৳", 10000.0),
                                    Pair("≥ ৫০,০০০ ৳", 50000.0)
                                )
                                amountPresets.forEach { (label, amount) ->
                                    val isSelected = tempFilter.minAmount == amount
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            tempFilter = if (isSelected) {
                                                tempFilter.copy(minAmount = null)
                                            } else {
                                                tempFilter.copy(minAmount = amount)
                                            }
                                        },
                                        label = { Text(label, fontSize = 10.sp) },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Display & Basic Options (Exact Budget tab style with compact toggle rows)
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (isBangla) "প্রদর্শন ও বিকল্প" else "Display & Options"
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            TransactionCompactToggleRow(
                                title = if (isBangla) "শুধু নোট/রসিদযুক্ত লেনদেন" else "Only Transactions with Notes/Receipt",
                                checked = tempFilter.onlyWithReceiptOrNote,
                                onCheckedChange = { isChecked ->
                                    tempFilter = tempFilter.copy(
                                        onlyWithReceiptOrNote = isChecked,
                                        onlyWithoutNote = if (isChecked) false else tempFilter.onlyWithoutNote
                                    )
                                }
                            )
                            TransactionCompactToggleRow(
                                title = if (isBangla) "নোটবিহীন লেনদেন (Empty Notes)" else "Only Without Notes",
                                checked = tempFilter.onlyWithoutNote,
                                onCheckedChange = { isChecked ->
                                    tempFilter = tempFilter.copy(
                                        onlyWithoutNote = isChecked,
                                        onlyWithReceiptOrNote = if (isChecked) false else tempFilter.onlyWithReceiptOrNote
                                    )
                                }
                            )
                            TransactionCompactToggleRow(
                                title = if (isBangla) "শুধু রিভার্সাল/ফেরত লেনদেন (Reversals)" else "Only Reversal Transactions",
                                checked = tempFilter.onlyReversals,
                                onCheckedChange = { tempFilter = tempFilter.copy(onlyReversals = it) }
                            )
                            TransactionCompactToggleRow(
                                title = if (isBangla) "উচ্চ মানের লেনদেন (≥ ১০,০০০ ৳)" else "High Value (≥ 10,000 ৳)",
                                checked = tempFilter.onlyHighValue,
                                onCheckedChange = { tempFilter = tempFilter.copy(onlyHighValue = it) }
                            )
                            TransactionCompactToggleRow(
                                title = if (isBangla) "স্থানান্তর লেনদেন মোট যোগফলে অন্তর্ভুক্ত" else "Include Transfers in Net Sum",
                                checked = tempFilter.displaySettings.showTransfersInTotal,
                                onCheckedChange = { isChecked ->
                                    tempFilter = tempFilter.copy(
                                        displaySettings = tempFilter.displaySettings.copy(showTransfersInTotal = isChecked)
                                    )
                                }
                            )
                            TransactionCompactToggleRow(
                                title = if (isBangla) "মোট সারসংক্ষেপ দেখান" else "Show Total Amount",
                                checked = tempFilter.displaySettings.showTotalAmount,
                                onCheckedChange = { isChecked ->
                                    tempFilter = tempFilter.copy(
                                        displaySettings = tempFilter.displaySettings.copy(showTotalAmount = isChecked)
                                    )
                                }
                            )
                            TransactionCompactToggleRow(
                                title = if (isBangla) "অ্যাকাউন্ট ব্যালেন্স দেখান" else "Show Account Balance",
                                checked = tempFilter.displaySettings.showAccountBalance,
                                onCheckedChange = { isChecked ->
                                    tempFilter = tempFilter.copy(
                                        displaySettings = tempFilter.displaySettings.copy(showAccountBalance = isChecked)
                                    )
                                }
                            )
                            TransactionCompactToggleRow(
                                title = if (isBangla) "পুরনো লেনদেন আগে দেখান (Oldest First)" else "Show Oldest Date First",
                                checked = tempFilter.displaySettings.showOldestDateFirst,
                                onCheckedChange = { isChecked ->
                                    tempFilter = tempFilter.copy(
                                        displaySettings = tempFilter.displaySettings.copy(showOldestDateFirst = isChecked)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Unified Footer (Exact Budget tab layout)
            UnifiedFilterFooter(
                activeCount = activeCount,
                languageMode = languageMode,
                onReset = {
                    tempFilter = TransactionTabFilter()
                },
                onDismiss = onDismiss,
                onApply = { onApplyFilter(tempFilter) }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Date Pickers for Custom Date Range
    // -------------------------------------------------------------------------
    if (showCustomStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (tempFilter.customStartDateMs > 0) tempFilter.customStartDateMs else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempFilter = tempFilter.copy(customStartDateMs = it)
                    }
                    showCustomStartDatePicker = false
                }) {
                    Text(if (isBangla) "ঠিক আছে" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomStartDatePicker = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCustomEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (tempFilter.customEndDateMs > 0) tempFilter.customEndDateMs else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempFilter = tempFilter.copy(customEndDateMs = it)
                    }
                    showCustomEndDatePicker = false
                }) {
                    Text(if (isBangla) "ঠিক আছে" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomEndDatePicker = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // -------------------------------------------------------------------------
    // Save Preset Dialog (Identical to Budget tab)
    // -------------------------------------------------------------------------
    if (showSavePresetDialog) {
        var presetNameInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = {
                Text(
                    text = if (isBangla) "ফিল্টার প্রিসেট সংরক্ষণ" else "Save Filter Preset",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBangla) "বর্তমান ফিল্টার সেটিংস ভবিষ্যতে দ্রুত ব্যবহারের জন্য একটি নামে সংরক্ষণ করুন:" else "Save current filter settings for quick access later:",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text(if (isBangla) "প্রিসেটের নাম" else "Preset Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = presetNameInput.trim().ifEmpty {
                            if (isBangla) "কাস্টম প্রিসেট ${savedPresets.size + 1}" else "Preset ${savedPresets.size + 1}"
                        }
                        val newPreset = SavedTransactionFilterPreset(
                            id = java.util.UUID.randomUUID().toString(),
                            name = name,
                            filter = tempFilter,
                            createdAt = System.currentTimeMillis()
                        )
                        savedPresets.add(newPreset)
                        TransactionFilterPresetsStorage.savePresets(context, savedPresets)
                        showSavePresetDialog = false
                        Toast.makeText(
                            context,
                            if (isBangla) "প্রিসেট সংরক্ষিত হয়েছে" else "Preset saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // Open Preset Dialog (Identical to Budget tab)
    // -------------------------------------------------------------------------
    if (showOpenPresetDialog) {
        AlertDialog(
            onDismissRequest = { showOpenPresetDialog = false },
            title = {
                Text(
                    text = if (isBangla) "সংরক্ষিত প্রিসেটসমূহ" else "Saved Filter Presets",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                if (savedPresets.isEmpty()) {
                    Text(
                        text = if (isBangla) "কোন সংরক্ষিত প্রিসেট পাওয়া যায়নি।" else "No saved presets found.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        savedPresets.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempFilter = preset.filter
                                        showOpenPresetDialog = false
                                        Toast.makeText(
                                            context,
                                            if (isBangla) "\"${preset.name}\" প্রয়োগ করা হয়েছে" else "Loaded \"${preset.name}\"",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isBangla) "সক্রিয় ফিল্টার: ${preset.filter.activeCount}" else "Active rules: ${preset.filter.activeCount}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            savedPresets.remove(preset)
                                            TransactionFilterPresetsStorage.savePresets(context, savedPresets)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOpenPresetDialog = false }) {
                    Text(if (isBangla) "বন্ধ করুন" else "Close")
                }
            }
        )
    }
}

@Composable
private fun TransactionCompactToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.70f)
        )
    }
}

// -----------------------------------------------------------------------------
// Presets Persistence for Transactions Tab
// -----------------------------------------------------------------------------
data class SavedTransactionFilterPreset(
    val id: String,
    val name: String,
    val filter: TransactionTabFilter,
    val createdAt: Long = System.currentTimeMillis()
)

object TransactionFilterPresetsStorage {
    private const val PREFS_KEY = "saved_transaction_filter_presets"

    fun loadPresets(context: android.content.Context): List<SavedTransactionFilterPreset> {
        val prefs = context.getSharedPreferences("budgeter_filter_presets", android.content.Context.MODE_PRIVATE)
        val json = prefs.getString(PREFS_KEY, null) ?: return emptyList()
        val list = mutableListOf<SavedTransactionFilterPreset>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val filterObj = obj.getJSONObject("filter")

                val datePresetName = filterObj.optString("datePreset", LedgerDatePreset.LAST_12_MONTHS.name)
                val datePreset = try { LedgerDatePreset.valueOf(datePresetName) } catch (_: Exception) { LedgerDatePreset.LAST_12_MONTHS }

                val typeArr = filterObj.optJSONArray("selectedTypes")
                val types = mutableSetOf<TransactionType>()
                if (typeArr != null) {
                    for (t in 0 until typeArr.length()) {
                        try { types.add(TransactionType.valueOf(typeArr.getString(t))) } catch (_: Exception) {}
                    }
                }

                val catArr = filterObj.optJSONArray("selectedCategoryIds")
                val catIds = mutableSetOf<Long>()
                if (catArr != null) {
                    for (c in 0 until catArr.length()) catIds.add(catArr.getLong(c))
                }

                val accArr = filterObj.optJSONArray("selectedAccountIds")
                val accIds = mutableSetOf<Long>()
                if (accArr != null) {
                    for (a in 0 until accArr.length()) accIds.add(accArr.getLong(a))
                }

                val filter = TransactionTabFilter(
                    selectedTypes = types,
                    datePreset = datePreset,
                    customStartDateMs = filterObj.optLong("customStartDateMs", 0L),
                    customEndDateMs = filterObj.optLong("customEndDateMs", System.currentTimeMillis()),
                    minAmount = if (filterObj.has("minAmount")) filterObj.getDouble("minAmount") else null,
                    maxAmount = if (filterObj.has("maxAmount")) filterObj.getDouble("maxAmount") else null,
                    selectedCategoryIds = catIds,
                    selectedAccountIds = accIds,
                    onlyWithReceiptOrNote = filterObj.optBoolean("onlyWithReceiptOrNote", false),
                    onlyWithoutNote = filterObj.optBoolean("onlyWithoutNote", false),
                    onlyReversals = filterObj.optBoolean("onlyReversals", false),
                    onlyHighValue = filterObj.optBoolean("onlyHighValue", false)
                )

                list.add(
                    SavedTransactionFilterPreset(
                        id = id,
                        name = name,
                        filter = filter,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun savePresets(context: android.content.Context, presets: List<SavedTransactionFilterPreset>) {
        val prefs = context.getSharedPreferences("budgeter_filter_presets", android.content.Context.MODE_PRIVATE)
        val array = JSONArray()
        presets.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("createdAt", p.createdAt)

            val f = JSONObject()
            f.put("datePreset", p.filter.datePreset.name)
            f.put("customStartDateMs", p.filter.customStartDateMs)
            f.put("customEndDateMs", p.filter.customEndDateMs)
            if (p.filter.minAmount != null) f.put("minAmount", p.filter.minAmount)
            if (p.filter.maxAmount != null) f.put("maxAmount", p.filter.maxAmount)
            f.put("onlyWithReceiptOrNote", p.filter.onlyWithReceiptOrNote)
            f.put("onlyWithoutNote", p.filter.onlyWithoutNote)
            f.put("onlyReversals", p.filter.onlyReversals)
            f.put("onlyHighValue", p.filter.onlyHighValue)

            val typeArr = JSONArray()
            p.filter.selectedTypes.forEach { typeArr.put(it.name) }
            f.put("selectedTypes", typeArr)

            val catArr = JSONArray()
            p.filter.selectedCategoryIds.forEach { catArr.put(it) }
            f.put("selectedCategoryIds", catArr)

            val accArr = JSONArray()
            p.filter.selectedAccountIds.forEach { accArr.put(it) }
            f.put("selectedAccountIds", accArr)

            obj.put("filter", f)
            array.put(obj)
        }
        prefs.edit().putString(PREFS_KEY, array.toString()).apply()
    }
}
