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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.example.data.model.TransactionType
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
import com.example.ui.components.filter.specs.ItemsFilterSpec
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.util.DateUtils

/**
 * Items Summary Tab Filter Dialog.
 * Redesigned into an ultra-compact layout matching the Transaction, Balance Sheet, and Net Earnings filter popups:
 * - Search bar at top without redundant header
 * - Cash Flow / Type Mode scope chips without header
 * - Time period dropdown with custom date range support
 * - Specific Transaction Type dropdown (All / Expense / Income / Transfer)
 * - 2-column side-by-side Categories & Accounts grouped dropdowns
 * - 2-column side-by-side Status & Amount Range (with calculators)
 * - Sort order dropdown
 * - Compact display toggle for "Exclude Zero Amounts"
 * - Unified footer with active rules counter, live item match counter, reset, and apply
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsFilterDialog(
    initialState: FilterState,
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    countProvider: ((FilterState) -> Int)? = null,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    val context = LocalContext.current
    val isBn = languageMode == LanguageMode.BANGLA
    var draftState by remember { mutableStateOf(initialState) }

    // Text inputs for amount range
    val initialMin = ItemsFilterSpec.getMinAmount(initialState)
    val initialMax = ItemsFilterSpec.getMaxAmount(initialState)
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

    // Build Category groups
    val categoryFilterGroups = remember(categories, isBn) {
        val expenseCats = categories.filter { it.type == CategoryType.EXPENSE }
        val incomeCats = categories.filter { it.type == CategoryType.INCOME }
        val list = mutableListOf<UnifiedFilterGroup<Long>>()
        if (expenseCats.isNotEmpty()) {
            list.add(
                UnifiedFilterGroup(
                    groupKey = "expense",
                    groupTitle = if (isBn) "ব্যয় ক্যাটাগরি" else "Expense Categories",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    items = expenseCats.map { cat ->
                        UnifiedFilterItem(
                            id = cat.id,
                            title = if (isBn) cat.nameBn else cat.nameEn,
                            icon = Icons.Default.Category
                        )
                    }
                )
            )
        }
        if (incomeCats.isNotEmpty()) {
            list.add(
                UnifiedFilterGroup(
                    groupKey = "income",
                    groupTitle = if (isBn) "আয় ক্যাটাগরি" else "Income Categories",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    items = incomeCats.map { cat ->
                        UnifiedFilterItem(
                            id = cat.id,
                            title = if (isBn) cat.nameBn else cat.nameEn,
                            icon = Icons.Default.Category
                        )
                    }
                )
            )
        }
        list
    }

    // Build Account groups
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

    // Status dropdown items
    val statusDropdownItems = remember(isBn) {
        listOf(
            DropdownItem(
                id = TransactionStatus.CLEARED.name,
                title = if (isBn) "সম্পন্ন" else "Cleared",
                icon = Icons.Default.CheckCircle
            ),
            DropdownItem(
                id = TransactionStatus.RECONCILED.name,
                title = if (isBn) "মিলিত" else "Reconciled",
                icon = Icons.Default.CheckCircle
            ),
            DropdownItem(
                id = TransactionStatus.VOID.name,
                title = if (isBn) "বাতিল" else "Void"
            )
        )
    }

    // Specific Transaction Type dropdown items
    val specificTypeDropdownItems = remember(isBn) {
        listOf(
            DropdownItem(
                id = "ALL",
                title = if (isBn) "সকল লেনদেন প্রবাহ" else "All Flow Types",
                icon = Icons.Default.FilterList
            ),
            DropdownItem(
                id = TransactionType.EXPENSE.name,
                title = if (isBn) "ব্যয় (Expense)" else "Expense",
                icon = Icons.AutoMirrored.Filled.TrendingDown
            ),
            DropdownItem(
                id = TransactionType.INCOME.name,
                title = if (isBn) "আয় (Income)" else "Income",
                icon = Icons.AutoMirrored.Filled.TrendingUp
            ),
            DropdownItem(
                id = TransactionType.TRANSFER.name,
                title = if (isBn) "স্থানান্তর (Transfer)" else "Transfer",
                icon = Icons.Default.Payments
            )
        )
    }

    // Date Preset dropdown items
    val datePresetDropdownItems = remember(isBn) {
        listOf(
            DropdownItem("this_month", if (isBn) "চলতি মাস" else "This Month", icon = Icons.Default.DateRange),
            DropdownItem("last_month", if (isBn) "গত মাস" else "Last Month", icon = Icons.Default.DateRange),
            DropdownItem("this_week", if (isBn) "এই সপ্তাহ" else "This Week", icon = Icons.Default.DateRange),
            DropdownItem("today", if (isBn) "আজকে" else "Today", icon = Icons.Default.DateRange),
            DropdownItem("yesterday", if (isBn) "গতকাল" else "Yesterday", icon = Icons.Default.DateRange),
            DropdownItem("last_30_days", if (isBn) "গত ৩০ দিন" else "Last 30 Days", icon = Icons.Default.DateRange),
            DropdownItem("last_90_days", if (isBn) "গত ৯০ দিন" else "Last 90 Days", icon = Icons.Default.DateRange),
            DropdownItem("this_year", if (isBn) "এই বছর" else "This Year", icon = Icons.Default.DateRange),
            DropdownItem("last_year", if (isBn) "গত বছর" else "Last Year", icon = Icons.Default.DateRange),
            DropdownItem("all_time", if (isBn) "সব সময়" else "All Time", icon = Icons.Default.DateRange),
            DropdownItem("custom", if (isBn) "কাস্টম সময়সীমা..." else "Custom Range...", icon = Icons.Default.EditCalendar)
        )
    }

    // Sort order dropdown items
    val sortDropdownItems = remember(isBn) {
        ItemsFilterSpec.SORT_OPTIONS.map { opt ->
            DropdownItem(
                id = opt.id,
                title = if (isBn) opt.titleBn else opt.titleEn,
                icon = Icons.AutoMirrored.Filled.Sort
            )
        }
    }

    // Derived states
    val searchQuery = remember(draftState) { ItemsFilterSpec.getSearchQuery(draftState) }
    val typeMode = remember(draftState) { ItemsFilterSpec.getTypeModeId(draftState) }
    val specificType = remember(draftState) { ItemsFilterSpec.getTransactionType(draftState)?.name ?: "ALL" }
    val currentDatePreset = remember(draftState) {
        (draftState[ItemsFilterSpec.FIELD_DATE] as? FilterValue.Date)?.presetId ?: "this_month"
    }
    val customStartMs = remember(draftState) {
        (draftState[ItemsFilterSpec.FIELD_DATE] as? FilterValue.Date)?.startMs ?: (System.currentTimeMillis() - 30L * 86400000L)
    }
    val customEndMs = remember(draftState) {
        (draftState[ItemsFilterSpec.FIELD_DATE] as? FilterValue.Date)?.endMs ?: System.currentTimeMillis()
    }
    val selectedCategoryIds = remember(draftState) { ItemsFilterSpec.getSelectedCategoryIds(draftState) }
    val selectedAccountIds = remember(draftState) { ItemsFilterSpec.getSelectedAccountIds(draftState) }
    val selectedStatuses = remember(draftState) { ItemsFilterSpec.getSelectedStatuses(draftState).map { it.name }.toSet() }
    val currentSortOrder = remember(draftState) {
        (draftState[ItemsFilterSpec.FIELD_SORT] as? FilterValue.Sort)?.sortId ?: ItemsFilterSpec.SORT_AMOUNT_DESC
    }
    val excludeZero = remember(draftState) { ItemsFilterSpec.getExcludeZero(draftState) }

    // Active count
    val activeCount = remember(draftState, minAmountText, maxAmountText) {
        val parsedMin = minAmountText.toDoubleOrNull()
        val parsedMax = maxAmountText.toDoubleOrNull()
        val stateWithAmt = draftState.let {
            if (parsedMin != null || parsedMax != null) {
                it + (ItemsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = parsedMin, max = parsedMax))
            } else {
                it - ItemsFilterSpec.FIELD_AMOUNT_RANGE
            }
        }
        ItemsFilterSpec.activeFilterCount(stateWithAmt)
    }

    // Match count
    val resultMatchCount = remember(draftState, minAmountText, maxAmountText, countProvider) {
        if (countProvider == null) null
        else {
            val parsedMin = minAmountText.toDoubleOrNull()
            val parsedMax = maxAmountText.toDoubleOrNull()
            val stateWithAmt = draftState.let {
                if (parsedMin != null || parsedMax != null) {
                    it + (ItemsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = parsedMin, max = parsedMax))
                } else {
                    it - ItemsFilterSpec.FIELD_AMOUNT_RANGE
                }
            }
            countProvider.invoke(stateWithAmt)
        }
    }

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "items_summary_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            UnifiedFilterHeader(
                title = if (isBn) "আইটেম সামারি ফিল্টার" else "Item Summary Filter",
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
                // 1. Search Bar at top without redundant header
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { draftState = ItemsFilterSpec.withSearchQuery(draftState, it) },
                    placeholder = {
                        Text(
                            text = if (isBn) "আইটেম, প্রাপক, বিবরণ খুঁজুন..." else "Search items, payees, notes...",
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
                            IconButton(onClick = { draftState = ItemsFilterSpec.withSearchQuery(draftState, "") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Cash Flow / Type Mode Scope Chips without header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modeOptions = listOf(
                        ItemsFilterSpec.MODE_ALL to (if (isBn) "সকল লেনদেন" else "All Items"),
                        ItemsFilterSpec.MODE_EXPENSE to (if (isBn) "ব্যয় সামগ্রী" else "Expenses"),
                        ItemsFilterSpec.MODE_INCOME to (if (isBn) "আয় সামগ্রী" else "Incomes")
                    )
                    modeOptions.forEach { (modeKey, title) ->
                        val isSelected = typeMode == modeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { draftState = ItemsFilterSpec.withTypeMode(draftState, modeKey) },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        when (modeKey) {
                                            ItemsFilterSpec.MODE_EXPENSE -> SolidExpense
                                            ItemsFilterSpec.MODE_INCOME -> SolidIncome
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    } else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            } else when (modeKey) {
                                ItemsFilterSpec.MODE_EXPENSE -> {
                                    { Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                }
                                ItemsFilterSpec.MODE_INCOME -> {
                                    { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                }
                                else -> null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Time Period Dropdown (without header)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UnifiedSingleSelectDropdown(
                        label = if (isBn) "সময়সীমা" else "Time Period",
                        items = datePresetDropdownItems,
                        selectedId = currentDatePreset,
                        onSelectionChanged = { newPresetId ->
                            draftState = ItemsFilterSpec.withDatePreset(draftState, newPresetId)
                        },
                        leadingIcon = Icons.Default.DateRange,
                        languageMode = languageMode
                    )

                    // Custom Date Range Card if Custom is chosen
                    if (currentDatePreset == "custom") {
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

                // 4. Specific Flow Type Dropdown (without header)
                UnifiedSingleSelectDropdown(
                    label = if (isBn) "নির্দিষ্ট লেনদেন প্রবাহ" else "Specific Flow Type",
                    items = specificTypeDropdownItems,
                    selectedId = specificType,
                    onSelectionChanged = { newTypeId ->
                        val parsedType = if (newTypeId == "ALL") null else try { TransactionType.valueOf(newTypeId) } catch (_: Exception) { null }
                        draftState = ItemsFilterSpec.withTransactionType(draftState, parsedType)
                    },
                    leadingIcon = Icons.Default.Payments,
                    languageMode = languageMode
                )

                // 5. Categories & Accounts side-by-side in compact 2-column row (without header)
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
                                draftState = ItemsFilterSpec.withCategoryIds(draftState, newIds)
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
                                draftState = ItemsFilterSpec.withAccountIds(draftState, newIds)
                            },
                            placeholder = if (isBn) "(সকল)" else "(All)",
                            leadingIcon = Icons.Default.AccountBalance,
                            languageMode = languageMode
                        )
                    }
                }

                // 6. Status & Amount Range in compact 2-column layout (without header)
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
                                draftState = ItemsFilterSpec.withStatuses(draftState, statuses)
                            },
                            placeholder = if (isBn) "(সকল)" else "(All)",
                            leadingIcon = Icons.Default.CheckCircle,
                            languageMode = languageMode
                        )
                    }

                    // Compact Amount Range inputs side-by-side inside the other half
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = minAmountText,
                            onValueChange = {
                                val filtered = it.filter { c -> c.isDigit() || c == '.' }
                                minAmountText = filtered
                                val minVal = filtered.toDoubleOrNull()
                                val maxVal = maxAmountText.toDoubleOrNull()
                                draftState = ItemsFilterSpec.withAmountRange(draftState, minVal, maxVal)
                            },
                            label = { Text(if (isBn) "ন্যূনতম" else "Min ৳", fontSize = 10.5.sp) },
                            placeholder = { Text("0", fontSize = 10.5.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                IconButton(onClick = { showFromCalc = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(14.dp))
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
                                draftState = ItemsFilterSpec.withAmountRange(draftState, minVal, maxVal)
                            },
                            label = { Text(if (isBn) "সর্বোচ্চ" else "Max ৳", fontSize = 10.5.sp) },
                            placeholder = { Text("∞", fontSize = 10.5.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                IconButton(onClick = { showToCalc = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(14.dp))
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 7. Sort Order Dropdown (without header)
                UnifiedSingleSelectDropdown(
                    label = if (isBn) "সাজানোর ক্রম" else "Sort Order",
                    items = sortDropdownItems,
                    selectedId = currentSortOrder,
                    onSelectionChanged = { newSortId ->
                        draftState = ItemsFilterSpec.withSortOrderId(draftState, newSortId)
                    },
                    leadingIcon = Icons.AutoMirrored.Filled.Sort,
                    languageMode = languageMode
                )

                // 8. Display Preferences (Ultra-compact toggle section)
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (isBn) "প্রদর্শন পছন্দসমূহ" else "Display Preferences"
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
                                title = if (isBn) "শূন্য পরিমাণ বাদ দিন" else "Exclude Zero Amounts",
                                checked = excludeZero,
                                onCheckedChange = {
                                    draftState = ItemsFilterSpec.withExcludeZero(draftState, it)
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
                            it + (ItemsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = parsedMin, max = parsedMax))
                        } else {
                            it - ItemsFilterSpec.FIELD_AMOUNT_RANGE
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
                        draftState = ItemsFilterSpec.withCustomDates(draftState, it, customEndMs)
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
                        draftState = ItemsFilterSpec.withCustomDates(draftState, customStartMs, it)
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
                draftState = ItemsFilterSpec.withAmountRange(draftState, minVal, maxVal)
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
                draftState = ItemsFilterSpec.withAmountRange(draftState, minVal, maxVal)
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
