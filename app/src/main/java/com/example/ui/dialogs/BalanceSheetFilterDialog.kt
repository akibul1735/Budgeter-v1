package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.ui.components.DropdownItem
import com.example.ui.components.UnifiedFilterDialogContainer
import com.example.ui.components.UnifiedFilterFooter
import com.example.ui.components.UnifiedFilterGroup
import com.example.ui.components.UnifiedFilterHeader
import com.example.ui.components.UnifiedFilterItem
import com.example.ui.components.UnifiedFilterSection
import com.example.ui.components.UnifiedGroupedMultiSelectDropdown
import com.example.ui.components.UnifiedMultiSelectDropdown
import com.example.ui.components.UnifiedSingleSelectDropdown
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.specs.BalanceSheetFilterSpec
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.BalanceSheetComparisonPreset
import com.example.util.BalanceSheetSortOrder
import com.example.util.DateUtils

/**
 * Compact Balance Sheet Filter Dialog.
 * Ultra-compact layout: Search at top without redundant headers, followed by section chips,
 * comparison timeline, 2-column side-by-side Accounts & Status, sort order dropdown,
 * and space-efficient display toggle preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSheetFilterDialog(
    initialState: FilterState,
    accounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    val context = LocalContext.current
    var draftState by remember { mutableStateOf(initialState) }

    var showBaseDatePicker by remember { mutableStateOf(false) }
    var showCompareDatePicker by remember { mutableStateOf(false) }

    val activeCount = remember(draftState) {
        BalanceSheetFilterSpec.activeFilterCount(draftState)
    }

    // 1. Grouped Accounts
    val accountFilterGroups = remember(accounts, languageMode) {
        val activeAccs = accounts
        val parentAccounts = activeAccs.filter { it.parentId == null }
        val hasParentChild = activeAccs.any { it.parentId != null }
        val groupList = mutableListOf<UnifiedFilterGroup<Long>>()

        if (hasParentChild) {
            parentAccounts.forEach { parent ->
                val children = activeAccs.filter { it.parentId == parent.id }
                val items = if (children.isNotEmpty()) {
                    children.map { ch ->
                        val chName = if (languageMode == LanguageMode.BANGLA) ch.nameBn.ifBlank { ch.nameEn } else ch.nameEn
                        UnifiedFilterItem(
                            id = ch.id,
                            title = chName,
                            subtitle = ch.accountNumber.ifBlank { ch.type.name }
                        )
                    }
                } else {
                    listOf(
                        UnifiedFilterItem(
                            id = parent.id,
                            title = if (languageMode == LanguageMode.BANGLA) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn,
                            subtitle = parent.accountNumber.ifBlank { parent.type.name }
                        )
                    )
                }
                val parentName = if (languageMode == LanguageMode.BANGLA) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn
                groupList.add(
                    UnifiedFilterGroup(
                        groupKey = "acc_parent_${parent.id}",
                        groupTitle = parentName,
                        items = items
                    )
                )
            }
        } else {
            val groupedByType = activeAccs.groupBy { it.type }
            groupedByType.forEach { (type, accList) ->
                val groupTitle = when (type) {
                    AccountType.ASSET -> if (languageMode == LanguageMode.BANGLA) "সম্পদ (Assets)" else "Assets"
                    AccountType.LIABILITY -> if (languageMode == LanguageMode.BANGLA) "দায় (Liabilities)" else "Liabilities"
                    AccountType.EQUITY -> if (languageMode == LanguageMode.BANGLA) "মালিকানাস্বত্ব (Equity)" else "Equity"
                    AccountType.INCOME -> if (languageMode == LanguageMode.BANGLA) "আয় (Income)" else "Income"
                    AccountType.EXPENSE -> if (languageMode == LanguageMode.BANGLA) "ব্যয় (Expense)" else "Expense"
                }
                val groupColor = when (type) {
                    AccountType.ASSET -> Color(0xFF10B981)
                    AccountType.LIABILITY -> Color(0xFFEF4444)
                    AccountType.EQUITY -> Color(0xFF6366F1)
                    AccountType.INCOME -> Color(0xFF10B981)
                    AccountType.EXPENSE -> Color(0xFFEF4444)
                }
                val items = accList.map { acc ->
                    val accName = if (languageMode == LanguageMode.BANGLA) acc.nameBn.ifBlank { acc.nameEn } else acc.nameEn
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
        groupList
    }

    // 2. Status Dropdown Items
    val statusDropdownItems = remember(languageMode) {
        listOf(
            DropdownItem(
                id = TransactionStatus.CLEARED.name,
                title = if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Cleared",
                icon = Icons.Default.CheckCircle
            ),
            DropdownItem(
                id = TransactionStatus.RECONCILED.name,
                title = if (languageMode == LanguageMode.BANGLA) "মিলিত" else "Reconciled",
                icon = Icons.Default.CheckCircle
            ),
            DropdownItem(
                id = TransactionStatus.VOID.name,
                title = if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Void"
            )
        )
    }

    // 3. Date Presets
    val datePresetDropdownItems = remember(languageMode) {
        BalanceSheetFilterSpec.DATE_PRESET_OPTIONS.map { opt ->
            DropdownItem(
                id = opt.id,
                title = if (languageMode == LanguageMode.BANGLA) opt.titleBn else opt.titleEn,
                icon = Icons.Default.CalendarMonth
            )
        }
    }

    // 4. Sort Options
    val sortDropdownItems = remember(languageMode) {
        BalanceSheetFilterSpec.SORT_OPTIONS.map { opt ->
            DropdownItem(
                id = opt.id,
                title = if (languageMode == LanguageMode.BANGLA) opt.titleBn else opt.titleEn,
                icon = opt.icon
            )
        }
    }

    val selectedAccountIds = remember(draftState) {
        BalanceSheetFilterSpec.getSelectedAccountIds(draftState)
    }
    val selectedStatusIds = remember(draftState) {
        BalanceSheetFilterSpec.getSelectedStatuses(draftState).map { it.name }.toSet()
    }

    val currentPresetId = remember(draftState) {
        (draftState[BalanceSheetFilterSpec.FIELD_DATE] as? FilterValue.Date)?.presetId
            ?: BalanceSheetFilterSpec.PRESET_THIS_MONTH
    }

    val currentSort = remember(draftState) {
        BalanceSheetFilterSpec.getSortOrder(draftState)
    }
    val currentSortId = remember(currentSort) {
        when (currentSort) {
            BalanceSheetSortOrder.AMOUNT_ASC -> BalanceSheetFilterSpec.SORT_AMOUNT_ASC
            BalanceSheetSortOrder.NAME_ASC -> BalanceSheetFilterSpec.SORT_NAME_ASC
            BalanceSheetSortOrder.DEFAULT -> BalanceSheetFilterSpec.SORT_DEFAULT
            else -> BalanceSheetFilterSpec.SORT_AMOUNT_DESC
        }
    }

    val activeTab = remember(draftState) {
        BalanceSheetFilterSpec.getActiveTab(draftState)
    }

    val searchQuery = remember(draftState) {
        BalanceSheetFilterSpec.getSearchQuery(draftState)
    }

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "balance_sheet_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            UnifiedFilterHeader(
                title = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স শিট ফিল্টার" else "Balance Sheet Filter",
                activeCount = activeCount,
                languageMode = languageMode,
                onReset = {
                    draftState = emptyMap()
                    Toast.makeText(
                        context,
                        if (languageMode == LanguageMode.BANGLA) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset to default",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDismiss = onDismiss
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Search Bar (at top without header)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { draftState = BalanceSheetFilterSpec.withSearchQuery(draftState, it) },
                    placeholder = {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট খুঁজুন..." else "Search accounts...",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { draftState = BalanceSheetFilterSpec.withSearchQuery(draftState, "") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Section Chips (All, Assets, Liabilities - without header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabOptions = listOf(
                        BalanceSheetFilterSpec.TAB_ALL to (if (languageMode == LanguageMode.BANGLA) "সকল" else "All"),
                        BalanceSheetFilterSpec.TAB_ASSETS to (if (languageMode == LanguageMode.BANGLA) "সম্পদ" else "Assets"),
                        BalanceSheetFilterSpec.TAB_LIABILITIES to (if (languageMode == LanguageMode.BANGLA) "দায়" else "Liabilities")
                    )
                    tabOptions.forEach { (tabKey, title) ->
                        val isSelected = activeTab == tabKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { draftState = BalanceSheetFilterSpec.withActiveTab(draftState, tabKey) },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        when (tabKey) {
                                            BalanceSheetFilterSpec.TAB_ASSETS -> SolidIncome
                                            BalanceSheetFilterSpec.TAB_LIABILITIES -> SolidExpense
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    } else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            } else when (tabKey) {
                                BalanceSheetFilterSpec.TAB_ASSETS -> { { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(13.dp)) } }
                                BalanceSheetFilterSpec.TAB_LIABILITIES -> { { Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, modifier = Modifier.size(13.dp)) } }
                                else -> null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Comparison Timeline Dropdown (without header)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UnifiedSingleSelectDropdown(
                        label = if (languageMode == LanguageMode.BANGLA) "তুলনার সময়সীমা" else "Comparison Timeline",
                        items = datePresetDropdownItems,
                        selectedId = currentPresetId,
                        onSelectionChanged = { newPresetId ->
                            val preset = when (newPresetId) {
                                BalanceSheetFilterSpec.PRESET_END_OF_LAST_MONTH -> BalanceSheetComparisonPreset.END_OF_LAST_MONTH
                                BalanceSheetFilterSpec.PRESET_BEGINNING_OF_MONTH -> BalanceSheetComparisonPreset.BEGINNING_OF_MONTH
                                BalanceSheetFilterSpec.PRESET_LAST_12_MONTHS -> BalanceSheetComparisonPreset.LAST_12_MONTHS
                                BalanceSheetFilterSpec.PRESET_PREVIOUS_MONTH -> BalanceSheetComparisonPreset.PREVIOUS_MONTH
                                BalanceSheetFilterSpec.PRESET_BEGINNING_OF_YEAR -> BalanceSheetComparisonPreset.BEGINNING_OF_YEAR
                                BalanceSheetFilterSpec.PRESET_LAST_30_DAYS -> BalanceSheetComparisonPreset.LAST_30_DAYS
                                BalanceSheetFilterSpec.PRESET_TODAY -> BalanceSheetComparisonPreset.TODAY
                                BalanceSheetFilterSpec.PRESET_CUSTOM -> BalanceSheetComparisonPreset.CUSTOM
                                else -> BalanceSheetComparisonPreset.THIS_MONTH
                            }
                            if (preset == BalanceSheetComparisonPreset.CUSTOM) {
                                val (base, compare) = BalanceSheetFilterSpec.resolveComparisonDates(
                                    spec = BalanceSheetFilterSpec.createSpec(accounts),
                                    state = draftState
                                )
                                draftState = BalanceSheetFilterSpec.withCustomDates(draftState, base, compare)
                            } else {
                                draftState = BalanceSheetFilterSpec.withPreset(draftState, preset)
                            }
                        },
                        leadingIcon = Icons.Default.CalendarMonth,
                        languageMode = languageMode
                    )

                    // Custom Date Range Card if Custom is chosen
                    if (currentPresetId == BalanceSheetFilterSpec.PRESET_CUSTOM) {
                        val (resolvedBaseMs, resolvedCompareMs) = remember(draftState) {
                            BalanceSheetFilterSpec.resolveComparisonDates(
                                spec = BalanceSheetFilterSpec.createSpec(accounts),
                                state = draftState
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কাস্টম তুলনামূলক সময়কাল" else "Select Custom Comparison Range",
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
                                        onClick = { showBaseDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (resolvedBaseMs > 0) DateUtils.formatDate(resolvedBaseMs, languageMode) else "Base Date",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showCompareDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (resolvedCompareMs > 0) DateUtils.formatDate(resolvedCompareMs, languageMode) else "Compare Date",
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

                // 4. Accounts & Status side-by-side in compact 2-column row (without header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        UnifiedGroupedMultiSelectDropdown(
                            label = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Accounts",
                            groups = accountFilterGroups,
                            selectedIds = selectedAccountIds,
                            onSelectionChanged = { newIds ->
                                draftState = BalanceSheetFilterSpec.withAccountIds(draftState, newIds)
                            },
                            placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল)" else "(All)",
                            leadingIcon = Icons.Default.AccountBalance,
                            languageMode = languageMode
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        UnifiedMultiSelectDropdown(
                            label = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status",
                            items = statusDropdownItems,
                            selectedIds = selectedStatusIds,
                            onSelectionChanged = { newIds ->
                                val selectedStatuses = newIds.mapNotNull {
                                    try { TransactionStatus.valueOf(it) } catch (_: Exception) { null }
                                }.toSet()
                                draftState = BalanceSheetFilterSpec.withStatuses(draftState, selectedStatuses)
                            },
                            placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল)" else "(All)",
                            leadingIcon = Icons.Default.CheckCircle,
                            languageMode = languageMode
                        )
                    }
                }

                // 5. Sort Order Dropdown (without header)
                UnifiedSingleSelectDropdown(
                    label = if (languageMode == LanguageMode.BANGLA) "সাজানোর ক্রম" else "Sort Order",
                    items = sortDropdownItems,
                    selectedId = currentSortId,
                    onSelectionChanged = { newSortId ->
                        val nextSort = when (newSortId) {
                            BalanceSheetFilterSpec.SORT_AMOUNT_ASC -> BalanceSheetSortOrder.AMOUNT_ASC
                            BalanceSheetFilterSpec.SORT_NAME_ASC -> BalanceSheetSortOrder.NAME_ASC
                            BalanceSheetFilterSpec.SORT_DEFAULT -> BalanceSheetSortOrder.DEFAULT
                            else -> BalanceSheetSortOrder.AMOUNT_DESC
                        }
                        draftState = BalanceSheetFilterSpec.withSortOrder(draftState, nextSort)
                    },
                    leadingIcon = Icons.AutoMirrored.Filled.Sort,
                    languageMode = languageMode
                )

                // 6. Display & Cleanliness Options (Ultra-compact toggle section)
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (languageMode == LanguageMode.BANGLA) "প্রদর্শন ও বিকল্প" else "Display & Options"
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
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            CompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "শূন্য ব্যালেন্সের অ্যাকাউন্ট বাদ দিন" else "Exclude Zero Balance Accounts",
                                checked = BalanceSheetFilterSpec.getExcludeZero(draftState),
                                onCheckedChange = { draftState = BalanceSheetFilterSpec.withExcludeZero(draftState, it) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "খালি গ্রুপ লুকান" else "Hide Empty Groups",
                                checked = BalanceSheetFilterSpec.getFilterNonZeroGroups(draftState),
                                onCheckedChange = { draftState = BalanceSheetFilterSpec.withFilterNonZeroGroups(draftState, it) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় অ্যাকাউন্ট দেখান" else "Show Inactive Accounts",
                                checked = BalanceSheetFilterSpec.getShowHidden(draftState),
                                onCheckedChange = { draftState = BalanceSheetFilterSpec.withShowHidden(draftState, it) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "শুধু বর্তমান ব্যালেন্স (তুলনাহীন)" else "Current Balance Only",
                                checked = BalanceSheetFilterSpec.getShowOnlyCurrent(draftState),
                                onCheckedChange = { draftState = BalanceSheetFilterSpec.withShowOnlyCurrent(draftState, it) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "গ্রুপ ছাড়া সমতল তালিকা" else "Flatten Accounts (No Groups)",
                                checked = BalanceSheetFilterSpec.getShowWithoutGroups(draftState),
                                onCheckedChange = { draftState = BalanceSheetFilterSpec.withShowWithoutGroups(draftState, it) }
                            )
                        }
                    }
                }
            }

            // Footer
            UnifiedFilterFooter(
                activeRulesCount = activeCount,
                matchCount = accounts.size,
                languageMode = languageMode,
                onReset = {
                    draftState = emptyMap()
                },
                onDismiss = onDismiss,
                onApply = {
                    onApply(draftState)
                }
            )
        }
    }

    // Custom Date Range Pickers
    if (showBaseDatePicker) {
        val (currentBase, currentCompare) = BalanceSheetFilterSpec.resolveComparisonDates(
            spec = BalanceSheetFilterSpec.createSpec(accounts),
            state = draftState
        )
        val dateState = rememberDatePickerState(initialSelectedDateMillis = currentBase)
        DatePickerDialog(
            onDismissRequest = { showBaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        draftState = BalanceSheetFilterSpec.withCustomDates(draftState, it, currentCompare)
                    }
                    showBaseDatePicker = false
                }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "নিশ্চিত" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBaseDatePicker = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showCompareDatePicker) {
        val (currentBase, currentCompare) = BalanceSheetFilterSpec.resolveComparisonDates(
            spec = BalanceSheetFilterSpec.createSpec(accounts),
            state = draftState
        )
        val dateState = rememberDatePickerState(initialSelectedDateMillis = currentCompare)
        DatePickerDialog(
            onDismissRequest = { showCompareDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        draftState = BalanceSheetFilterSpec.withCustomDates(draftState, currentBase, it)
                    }
                    showCompareDatePicker = false
                }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "নিশ্চিত" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompareDatePicker = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }
}

@Composable
private fun CompactToggleRow(
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
            fontSize = 11.5.sp,
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
