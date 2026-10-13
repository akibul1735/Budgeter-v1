package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
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
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.DropdownItem
import com.example.ui.components.PopupCalculatorDialog
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
import com.example.ui.components.filter.count
import com.example.ui.components.filter.specs.NetEarningsFilterSpec
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.util.DateUtils

/**
 * Compact Net Earnings Filter Dialog.
 * Ultra-compact layout: Search at top without redundant headers, followed by Cash Flow Scope chips,
 * Hierarchy View chips, Time Period & Comparison Range dropdowns, compact 2-column side-by-side
 * Categories & Accounts, 2-column Status & Labels, 2-column Amount Range, Sort Order dropdown,
 * and space-efficient display toggle preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetEarningsFilterDialog(
    initialState: FilterState,
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    labels: List<String> = emptyList(),
    transactions: List<TransactionWithDetails> = emptyList(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    val context = LocalContext.current
    val isBn = languageMode == LanguageMode.BANGLA
    var draftState by remember { mutableStateOf(initialState) }

    // Text inputs for amounts
    val initialMin = NetEarningsFilterSpec.getMinAmount(initialState)
    val initialMax = NetEarningsFilterSpec.getMaxAmount(initialState)
    var minAmountText by remember {
        mutableStateOf(if (initialMin != null && initialMin > 0) initialMin.toInt().toString() else "")
    }
    var maxAmountText by remember {
        mutableStateOf(if (initialMax != null && initialMax > 0) initialMax.toInt().toString() else "")
    }

    // Sub-dialog states
    var showFromCalc by remember { mutableStateOf(false) }
    var showToCalc by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val spec = remember(categories, accounts, labels) {
        NetEarningsFilterSpec.createSpec(
            accounts = accounts,
            categories = categories,
            labels = labels
        )
    }

    // 1. Grouped Categories
    val categoryFilterGroups = remember(categories, languageMode) {
        val parentCats = categories.filter { it.parentId == null }
        val childCats = categories.filter { it.parentId != null }
        val childrenByParent = childCats.groupBy { it.parentId }

        val groupList = mutableListOf<UnifiedFilterGroup<Long>>()
        parentCats.forEach { parent ->
            val children = childrenByParent[parent.id] ?: emptyList()
            val parentName = if (isBn) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn
            val parentColor = try {
                if (!parent.colorHex.isNullOrBlank()) Color(android.graphics.Color.parseColor(parent.colorHex)) else null
            } catch (_: Exception) { null }

            if (children.isNotEmpty()) {
                val items = children.map { child ->
                    val childName = if (isBn) child.nameBn.ifBlank { child.nameEn } else child.nameEn
                    UnifiedFilterItem(
                        id = child.id,
                        title = childName,
                        subtitle = if (child.type == CategoryType.EXPENSE) (if (isBn) "ব্যয়" else "Expense") else (if (isBn) "আয়" else "Income"),
                        color = try {
                            if (!child.colorHex.isNullOrBlank()) Color(android.graphics.Color.parseColor(child.colorHex)) else parentColor
                        } catch (_: Exception) { parentColor }
                    )
                }
                groupList.add(
                    UnifiedFilterGroup(
                        groupKey = "cat_parent_${parent.id}",
                        groupTitle = parentName,
                        groupSubtitle = if (parent.type == CategoryType.EXPENSE) (if (isBn) "ব্যয় গ্রুপ" else "Expense Group") else (if (isBn) "আয় গ্রুপ" else "Income Group"),
                        color = parentColor,
                        items = items
                    )
                )
            } else {
                val singleItem = UnifiedFilterItem(
                    id = parent.id,
                    title = parentName,
                    subtitle = if (parent.type == CategoryType.EXPENSE) (if (isBn) "ব্যয়" else "Expense") else (if (isBn) "আয়" else "Income"),
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
        groupList
    }

    // 2. Grouped Accounts
    val accountFilterGroups = remember(accounts, languageMode) {
        val activeAccs = accounts.filter { it.isActive }
        val parentAccounts = activeAccs.filter { it.parentId == null }
        val hasParentChild = activeAccs.any { it.parentId != null }
        val groupList = mutableListOf<UnifiedFilterGroup<Long>>()

        if (hasParentChild) {
            parentAccounts.forEach { parent ->
                val children = activeAccs.filter { it.parentId == parent.id }
                val items = if (children.isNotEmpty()) {
                    children.map { ch ->
                        val chName = if (isBn) ch.nameBn.ifBlank { ch.nameEn } else ch.nameEn
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
                            title = if (isBn) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn,
                            subtitle = parent.accountNumber.ifBlank { parent.type.name }
                        )
                    )
                }
                val parentName = if (isBn) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn
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
                    AccountType.ASSET -> if (isBn) "সম্পদ (Assets)" else "Assets"
                    AccountType.LIABILITY -> if (isBn) "দায় (Liabilities)" else "Liabilities"
                    AccountType.EQUITY -> if (isBn) "মালিকানাস্বত্ব (Equity)" else "Equity"
                    AccountType.INCOME -> if (isBn) "আয় (Income)" else "Income"
                    AccountType.EXPENSE -> if (isBn) "ব্যয় (Expense)" else "Expense"
                }
                val items = accList.map { acc ->
                    val accName = if (isBn) acc.nameBn.ifBlank { acc.nameEn } else acc.nameEn
                    UnifiedFilterItem(
                        id = acc.id,
                        title = accName,
                        subtitle = acc.accountNumber.ifBlank { acc.type.name }
                    )
                }
                groupList.add(
                    UnifiedFilterGroup(
                        groupKey = "acc_type_${type.name}",
                        groupTitle = groupTitle,
                        items = items
                    )
                )
            }
        }
        groupList
    }

    // 3. Status Options
    val statusDropdownItems = remember(languageMode) {
        NetEarningsFilterSpec.STATUS_OPTIONS.map { opt ->
            DropdownItem(
                id = opt.id,
                title = if (isBn) opt.titleBn else opt.titleEn,
                icon = opt.icon
            )
        }
    }

    // 4. Label Options
    val allDistinctLabels = remember(labels, transactions, draftState) {
        val set = mutableSetOf<String>()
        val hashtagRegex = Regex("#[\\w\\u0980-\\u09FF]+")
        transactions.forEach { tw ->
            val tx = tw.transaction
            if (tx.referenceNo.isNotBlank()) set.add(tx.referenceNo.trim())
            hashtagRegex.findAll("${tx.note} ${tx.referenceNo}").forEach { set.add(it.value.trim()) }
            tx.note.split(" ", "\n", ",").filter { it.startsWith("#") && it.length > 1 }.forEach { set.add(it.trim()) }
        }
        labels.forEach { if (it.isNotBlank()) set.add(it.trim()) }
        NetEarningsFilterSpec.getSelectedLabels(draftState).forEach { if (it.isNotBlank()) set.add(it.trim()) }
        set.filter { it.isNotBlank() }.sorted()
    }

    val labelDropdownItems = remember(allDistinctLabels) {
        allDistinctLabels.map { lbl ->
            DropdownItem(
                id = lbl,
                title = lbl,
                icon = Icons.AutoMirrored.Filled.Label
            )
        }
    }

    // 5. Date Presets
    val datePresetDropdownItems = remember(languageMode) {
        NetEarningsFilterSpec.DATE_PRESETS.map { opt ->
            DropdownItem(
                id = opt.id,
                title = if (isBn) opt.titleBn else opt.titleEn,
                icon = Icons.Default.DateRange
            )
        }
    }

    // 6. Comparison Options
    val comparisonDropdownItems = remember(languageMode) {
        NetEarningsFilterSpec.COMPARISON_CONDITIONS.map { opt ->
            DropdownItem(
                id = opt.id,
                title = if (isBn) opt.titleBn else opt.titleEn,
                icon = opt.icon ?: Icons.Default.CompareArrows
            )
        }
    }

    // 7. Sort Options
    val sortDropdownItems = remember(languageMode) {
        NetEarningsFilterSpec.SORT_OPTIONS.map { opt ->
            DropdownItem(
                id = opt.id,
                title = if (isBn) opt.titleBn else opt.titleEn,
                icon = opt.icon
            )
        }
    }

    // Current State Values
    val searchQuery = remember(draftState) { NetEarningsFilterSpec.getSearchQuery(draftState) }
    val flowScope = remember(draftState) { NetEarningsFilterSpec.getFlowScope(draftState) }
    val hierarchyView = remember(draftState) { NetEarningsFilterSpec.getHierarchyView(draftState) }
    val currentDatePreset = remember(draftState) { NetEarningsFilterSpec.getDatePreset(draftState) }
    val currentComparison = remember(draftState) { NetEarningsFilterSpec.getComparisonPreset(draftState) }
    val currentSortOrder = remember(draftState) { NetEarningsFilterSpec.getSortOrder(draftState) }
    val selectedCategoryIds = remember(draftState) { NetEarningsFilterSpec.getSelectedCategoryIds(draftState) }
    val selectedAccountIds = remember(draftState) { NetEarningsFilterSpec.getSelectedAccountIds(draftState) }
    val selectedLabels = remember(draftState) { NetEarningsFilterSpec.getSelectedLabels(draftState) }
    val selectedStatuses = remember(draftState) {
        NetEarningsFilterSpec.getSelectedStatuses(draftState).map { it.name }.toSet()
    }

    val customStartMs = remember(draftState) { NetEarningsFilterSpec.getCustomStartDate(draftState) ?: System.currentTimeMillis() }
    val customEndMs = remember(draftState) { NetEarningsFilterSpec.getCustomEndDate(draftState) ?: System.currentTimeMillis() }

    // Active count
    val activeCount = remember(draftState, minAmountText, maxAmountText) {
        val parsedMin = minAmountText.toDoubleOrNull()
        val parsedMax = maxAmountText.toDoubleOrNull()
        val stateWithAmt = draftState.let {
            if (parsedMin != null || parsedMax != null) {
                it + (NetEarningsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = parsedMin, max = parsedMax))
            } else {
                it - NetEarningsFilterSpec.FIELD_AMOUNT_RANGE
            }
        }
        NetEarningsFilterSpec.activeFilterCount(stateWithAmt)
    }

    // Result match count
    val resultMatchCount = remember(draftState, minAmountText, maxAmountText, transactions) {
        val parsedMin = minAmountText.toDoubleOrNull()
        val parsedMax = maxAmountText.toDoubleOrNull()
        val stateWithAmt = draftState.let {
            if (parsedMin != null || parsedMax != null) {
                it + (NetEarningsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = parsedMin, max = parsedMax))
            } else {
                it - NetEarningsFilterSpec.FIELD_AMOUNT_RANGE
            }
        }
        spec.count(transactions, stateWithAmt)
    }

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "net_earnings_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            UnifiedFilterHeader(
                title = if (isBn) "নিট আয় ফিল্টার" else "Net Earnings Filter",
                activeCount = activeCount,
                languageMode = languageMode,
                onReset = {
                    draftState = emptyMap()
                    minAmountText = ""
                    maxAmountText = ""
                    Toast.makeText(
                        context,
                        if (isBn) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset to default",
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
                    onValueChange = { draftState = NetEarningsFilterSpec.withSearchQuery(draftState, it) },
                    placeholder = {
                        Text(
                            text = if (isBn) "নোট, প্রাপক, ক্যাটাগরি খুঁজুন..." else "Search notes, payees, categories...",
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
                            IconButton(onClick = { draftState = NetEarningsFilterSpec.withSearchQuery(draftState, "") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Cash Flow Scope (horizontal scrollable chips row without header)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val flowOptions = listOf(
                        NetEarningsFilterSpec.SCOPE_ALL to (if (isBn) "সকল প্রবাহ" else "All Flow"),
                        NetEarningsFilterSpec.SCOPE_SURPLUS_ONLY to (if (isBn) "উদ্বৃত্ত (লাভ)" else "Surplus"),
                        NetEarningsFilterSpec.SCOPE_DEFICIT_ONLY to (if (isBn) "ঘাটতি (ক্ষতি)" else "Deficit"),
                        NetEarningsFilterSpec.SCOPE_INCOME_ONLY to (if (isBn) "শুধু আয়" else "Incomes"),
                        NetEarningsFilterSpec.SCOPE_EXPENSE_ONLY to (if (isBn) "শুধু ব্যয়" else "Expenses")
                    )
                    flowOptions.forEach { (scopeKey, title) ->
                        val isSelected = flowScope == scopeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { draftState = NetEarningsFilterSpec.withFlowScope(draftState, scopeKey) },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        when (scopeKey) {
                                            NetEarningsFilterSpec.SCOPE_SURPLUS_ONLY, NetEarningsFilterSpec.SCOPE_INCOME_ONLY -> SolidIncome
                                            NetEarningsFilterSpec.SCOPE_DEFICIT_ONLY, NetEarningsFilterSpec.SCOPE_EXPENSE_ONLY -> SolidExpense
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    } else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            } else when (scopeKey) {
                                NetEarningsFilterSpec.SCOPE_SURPLUS_ONLY, NetEarningsFilterSpec.SCOPE_INCOME_ONLY -> {
                                    { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                }
                                NetEarningsFilterSpec.SCOPE_DEFICIT_ONLY, NetEarningsFilterSpec.SCOPE_EXPENSE_ONLY -> {
                                    { Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                }
                                else -> null
                            }
                        )
                    }
                }

                // 3. Hierarchy View Chips (without header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val hierarchyOptions = listOf(
                        NetEarningsFilterSpec.HIERARCHY_GROUPED to (if (isBn) "গ্রুপ ও আইটেম" else "Grouped"),
                        NetEarningsFilterSpec.HIERARCHY_ONLY_GROUPS to (if (isBn) "শুধু গ্রুপ" else "Groups Only"),
                        NetEarningsFilterSpec.HIERARCHY_ONLY_ITEMS to (if (isBn) "শুধু আইটেম" else "Items Only")
                    )
                    hierarchyOptions.forEach { (hKey, title) ->
                        val isSelected = hierarchyView == hKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { draftState = NetEarningsFilterSpec.withHierarchyView(draftState, hKey) },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Time Period Dropdown (without header)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UnifiedSingleSelectDropdown(
                        label = if (isBn) "সময়সীমা" else "Time Period",
                        items = datePresetDropdownItems,
                        selectedId = currentDatePreset,
                        onSelectionChanged = { newPresetId ->
                            draftState = NetEarningsFilterSpec.withDatePreset(draftState, newPresetId)
                        },
                        leadingIcon = Icons.Default.DateRange,
                        languageMode = languageMode
                    )

                    // Custom Date Range Card if Custom is chosen
                    if (currentDatePreset == NetEarningsFilterSpec.PRESET_CUSTOM) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isBn) "কাস্টম সময়কাল নির্ধারণ" else "Select Custom Date Range",
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
                                        onClick = { showStartDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (customStartMs > 0) DateUtils.formatDate(customStartMs, languageMode) else "Start Date",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showEndDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = DateUtils.formatDate(customEndMs, languageMode),
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

                // 5. Comparison Range Dropdown (without header)
                UnifiedSingleSelectDropdown(
                    label = if (isBn) "তুলনার সময়সীমা" else "Comparison Range",
                    items = comparisonDropdownItems,
                    selectedId = currentComparison,
                    onSelectionChanged = { newCompId ->
                        draftState = NetEarningsFilterSpec.withComparison(draftState, newCompId)
                    },
                    leadingIcon = Icons.Default.CompareArrows,
                    languageMode = languageMode
                )

                // 6. Categories & Accounts side-by-side in compact 2-column row (without header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        UnifiedGroupedMultiSelectDropdown(
                            label = if (isBn) "ক্যাটাগরি" else "Categories",
                            groups = categoryFilterGroups,
                            selectedIds = selectedCategoryIds,
                            onSelectionChanged = { newIds ->
                                draftState = NetEarningsFilterSpec.withCategoryIds(draftState, newIds)
                            },
                            placeholder = if (isBn) "(সকল)" else "(All)",
                            leadingIcon = Icons.Default.Category,
                            languageMode = languageMode
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        UnifiedGroupedMultiSelectDropdown(
                            label = if (isBn) "অ্যাকাউন্ট" else "Accounts",
                            groups = accountFilterGroups,
                            selectedIds = selectedAccountIds,
                            onSelectionChanged = { newIds ->
                                draftState = NetEarningsFilterSpec.withAccountIds(draftState, newIds)
                            },
                            placeholder = if (isBn) "(সকল)" else "(All)",
                            leadingIcon = Icons.Default.AccountBalance,
                            languageMode = languageMode
                        )
                    }
                }

                // 7. Status & Labels side-by-side in compact 2-column row (without header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        UnifiedMultiSelectDropdown(
                            label = if (isBn) "স্ট্যাটাস" else "Status",
                            items = statusDropdownItems,
                            selectedIds = selectedStatuses,
                            onSelectionChanged = { newIds ->
                                val statuses = newIds.mapNotNull {
                                    try { TransactionStatus.valueOf(it) } catch (_: Exception) { null }
                                }.toSet()
                                draftState = NetEarningsFilterSpec.withStatuses(draftState, statuses)
                            },
                            placeholder = if (isBn) "(সকল)" else "(All)",
                            leadingIcon = Icons.Default.CheckCircle,
                            languageMode = languageMode
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        UnifiedMultiSelectDropdown(
                            label = if (isBn) "লেবেল" else "Labels",
                            items = labelDropdownItems,
                            selectedIds = selectedLabels,
                            onSelectionChanged = { newLabels ->
                                draftState = NetEarningsFilterSpec.withLabels(draftState, newLabels)
                            },
                            placeholder = if (isBn) "(সকল)" else "(All)",
                            leadingIcon = Icons.AutoMirrored.Filled.Label,
                            languageMode = languageMode
                        )
                    }
                }

                // 8. Amount Range in compact 2-column row (without header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minAmountText,
                        onValueChange = {
                            val filtered = it.filter { c -> c.isDigit() || c == '.' }
                            minAmountText = filtered
                            val minVal = filtered.toDoubleOrNull()
                            val maxVal = maxAmountText.toDoubleOrNull()
                            draftState = NetEarningsFilterSpec.withAmountRange(draftState, minVal, maxVal)
                        },
                        label = { Text(if (isBn) "সর্বনিম্ন (৳)" else "Amount From", fontSize = 11.sp) },
                        placeholder = { Text("0.00", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            IconButton(onClick = { showFromCalc = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(16.dp))
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = maxAmountText,
                        onValueChange = {
                            val filtered = it.filter { c -> c.isDigit() || c == '.' }
                            maxAmountText = filtered
                            val minVal = minAmountText.toDoubleOrNull()
                            val maxVal = filtered.toDoubleOrNull()
                            draftState = NetEarningsFilterSpec.withAmountRange(draftState, minVal, maxVal)
                        },
                        label = { Text(if (isBn) "সর্বোচ্চ (৳)" else "Amount To", fontSize = 11.sp) },
                        placeholder = { Text("∞", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            IconButton(onClick = { showToCalc = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(16.dp))
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 9. Sort Order Dropdown (without header)
                UnifiedSingleSelectDropdown(
                    label = if (isBn) "সাজানোর ক্রম" else "Sort Order",
                    items = sortDropdownItems,
                    selectedId = currentSortOrder,
                    onSelectionChanged = { newSortId ->
                        draftState = NetEarningsFilterSpec.withSortOrder(draftState, newSortId)
                    },
                    leadingIcon = Icons.AutoMirrored.Filled.Sort,
                    languageMode = languageMode
                )

                // 10. Display & Calculation Options (Ultra-compact toggle section)
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (isBn) "প্রদর্শন ও গণনা অপশন" else "Display & Calculation Options"
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
                                title = if (isBn) "স্থানান্তর অন্তর্ভুক্ত" else "Include Transfers",
                                checked = NetEarningsFilterSpec.getIncludeTransfers(draftState),
                                onCheckedChange = {
                                    draftState = NetEarningsFilterSpec.withToggle(draftState, NetEarningsFilterSpec.TOGGLE_INCLUDE_TRANSFERS, it)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (isBn) "শূন্য পরিমাণ বাদ" else "Exclude Zero Amounts",
                                checked = NetEarningsFilterSpec.getExcludeZero(draftState),
                                onCheckedChange = {
                                    draftState = NetEarningsFilterSpec.withToggle(draftState, NetEarningsFilterSpec.TOGGLE_EXCLUDE_ZERO, it)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (isBn) "খালি গ্রুপ লুকান" else "Hide Empty Groups",
                                checked = NetEarningsFilterSpec.getHideEmptyGroups(draftState),
                                onCheckedChange = {
                                    draftState = NetEarningsFilterSpec.withToggle(draftState, NetEarningsFilterSpec.TOGGLE_HIDE_EMPTY_GROUPS, it)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (isBn) "গ্রুপহীন ক্যাটাগরি" else "Only Without Groups",
                                checked = NetEarningsFilterSpec.getShowWithoutGroups(draftState),
                                onCheckedChange = {
                                    draftState = NetEarningsFilterSpec.withToggle(draftState, NetEarningsFilterSpec.TOGGLE_WITHOUT_GROUPS, it)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (isBn) "মুদ্রা প্রদর্শন" else "Show Currency",
                                checked = NetEarningsFilterSpec.getDisplayCurrency(draftState),
                                onCheckedChange = {
                                    draftState = NetEarningsFilterSpec.withToggle(draftState, NetEarningsFilterSpec.TOGGLE_DISPLAY_CURRENCY, it)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            CompactToggleRow(
                                title = if (isBn) "মুদ্রার প্রতীক প্রদর্শন" else "Show Currency Symbol",
                                checked = NetEarningsFilterSpec.getDisplayCurrencySymbol(draftState),
                                onCheckedChange = {
                                    draftState = NetEarningsFilterSpec.withToggle(draftState, NetEarningsFilterSpec.TOGGLE_DISPLAY_SYMBOL, it)
                                }
                            )
                        }
                    }
                }
            }

            // Footer
            UnifiedFilterFooter(
                activeRulesCount = activeCount,
                matchCount = resultMatchCount,
                languageMode = languageMode,
                onReset = {
                    draftState = emptyMap()
                    minAmountText = ""
                    maxAmountText = ""
                },
                onDismiss = onDismiss,
                onApply = {
                    val parsedMin = minAmountText.toDoubleOrNull()
                    val parsedMax = maxAmountText.toDoubleOrNull()
                    val finalState = draftState.let {
                        if (parsedMin != null || parsedMax != null) {
                            it + (NetEarningsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = parsedMin, max = parsedMax))
                        } else {
                            it - NetEarningsFilterSpec.FIELD_AMOUNT_RANGE
                        }
                    }
                    onApply(finalState)
                }
            )
        }
    }

    // Custom Date Range Pickers
    if (showStartDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = customStartMs)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        draftState = NetEarningsFilterSpec.withCustomDates(draftState, it, customEndMs)
                    }
                    showStartDatePicker = false
                }) {
                    Text(if (isBn) "নিশ্চিত" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showEndDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = customEndMs)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        draftState = NetEarningsFilterSpec.withCustomDates(draftState, customStartMs, it)
                    }
                    showEndDatePicker = false
                }) {
                    Text(if (isBn) "নিশ্চিত" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    // Amount Calculators
    if (showFromCalc) {
        PopupCalculatorDialog(
            initialValue = minAmountText.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showFromCalc = false },
            onValueConfirmed = { value ->
                minAmountText = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
                val minVal = minAmountText.toDoubleOrNull()
                val maxVal = maxAmountText.toDoubleOrNull()
                draftState = NetEarningsFilterSpec.withAmountRange(draftState, minVal, maxVal)
                showFromCalc = false
            }
        )
    }

    if (showToCalc) {
        PopupCalculatorDialog(
            initialValue = maxAmountText.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showToCalc = false },
            onValueConfirmed = { value ->
                maxAmountText = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
                val minVal = minAmountText.toDoubleOrNull()
                val maxVal = maxAmountText.toDoubleOrNull()
                draftState = NetEarningsFilterSpec.withAmountRange(draftState, minVal, maxVal)
                showToCalc = false
            }
        )
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
