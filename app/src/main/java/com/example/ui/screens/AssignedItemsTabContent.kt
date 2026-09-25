package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.AccountObligation
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
internal fun AssignedItemsTabContent(
    overview: PaymentSourceAnalysisOverview,
    languageMode: LanguageMode,
    selectedAccountId: Long?,
    onClearAccountFilter: () -> Unit,
    onSelectAccountFilter: (Long) -> Unit,
    allPaymentSourceAccounts: List<Account>,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    sectionFilter: AssignedItemSectionFilter,
    onSectionFilterChange: (AssignedItemSectionFilter) -> Unit,
    statusFilter: AssignedItemStatusFilter,
    onStatusFilterChange: (AssignedItemStatusFilter) -> Unit,
    sortOption: AssignedItemSortOption,
    onSortOptionChange: (AssignedItemSortOption) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    usageFrequencyMap: Map<Long, Int>,
    onOpenCategorySplitDialog: (CategoryAllocationAnalysis) -> Unit,
    onOpenOtherAccountSplitDialog: (OtherAccountAllocationAnalysis) -> Unit,
    onAddTransactionWithAccount: (Long, TransactionType) -> Unit
) {
    val q = searchQuery.trim().lowercase()

    // Filter Other Accounts
    val filteredOtherAccounts = remember(overview.otherAccountAllocations, q, statusFilter, usageFrequencyMap, selectedAccountId) {
        var list = overview.otherAccountAllocations
        if (selectedAccountId != null) {
            list = list.filter { it.accountSplits.any { s -> s.account.id == selectedAccountId } }
        }
        if (q.isNotEmpty()) {
            list = list.filter {
                it.account.nameEn.lowercase().contains(q) ||
                it.account.nameBn.lowercase().contains(q) ||
                it.accountSplits.any { s -> s.account.nameEn.lowercase().contains(q) || s.account.nameBn.lowercase().contains(q) }
            }
        }
        when (statusFilter) {
            AssignedItemStatusFilter.ALL -> list
            AssignedItemStatusFilter.BUDGETED_ONLY -> list.filter { it.totalBudgeted > 0 }
            AssignedItemStatusFilter.REMAINING_ONLY -> list.filter { it.totalRemaining > 0 }
            AssignedItemStatusFilter.MOST_FREQUENT -> list.sortedByDescending { usageFrequencyMap[it.account.id] ?: 0 }
            AssignedItemStatusFilter.SPLIT_ONLY -> list.filter { it.isMultiAccount }
            AssignedItemStatusFilter.UNASSIGNED_ONLY -> list.filter { it.accountSplits.isEmpty() }
        }
    }

    // Filter Expense Categories
    val filteredExpenses = remember(overview.categoryAllocations, q, statusFilter, usageFrequencyMap, selectedAccountId) {
        var list = overview.categoryAllocations
        if (selectedAccountId != null) {
            list = list.filter { it.accountSplits.any { s -> s.account.id == selectedAccountId } }
        }
        if (q.isNotEmpty()) {
            list = list.filter {
                it.category.nameEn.lowercase().contains(q) ||
                it.category.nameBn.lowercase().contains(q) ||
                it.accountSplits.any { s -> s.account.nameEn.lowercase().contains(q) || s.account.nameBn.lowercase().contains(q) }
            }
        }
        when (statusFilter) {
            AssignedItemStatusFilter.ALL -> list
            AssignedItemStatusFilter.BUDGETED_ONLY -> list.filter { it.totalBudgeted > 0 }
            AssignedItemStatusFilter.REMAINING_ONLY -> list.filter { it.totalRemaining > 0 }
            AssignedItemStatusFilter.MOST_FREQUENT -> list.sortedByDescending { usageFrequencyMap[it.category.id] ?: 0 }
            AssignedItemStatusFilter.SPLIT_ONLY -> list.filter { it.isMultiAccount }
            AssignedItemStatusFilter.UNASSIGNED_ONLY -> list.filter { it.accountSplits.isEmpty() }
        }
    }

    // Filter Income Categories
    val filteredIncomes = remember(overview.incomeAllocations, q, statusFilter, usageFrequencyMap, selectedAccountId) {
        var list = overview.incomeAllocations
        if (selectedAccountId != null) {
            list = list.filter { it.accountSplits.any { s -> s.account.id == selectedAccountId } }
        }
        if (q.isNotEmpty()) {
            list = list.filter {
                it.category.nameEn.lowercase().contains(q) ||
                it.category.nameBn.lowercase().contains(q) ||
                it.accountSplits.any { s -> s.account.nameEn.lowercase().contains(q) || s.account.nameBn.lowercase().contains(q) }
            }
        }
        when (statusFilter) {
            AssignedItemStatusFilter.ALL -> list
            AssignedItemStatusFilter.BUDGETED_ONLY -> list.filter { it.totalBudgeted > 0 }
            AssignedItemStatusFilter.REMAINING_ONLY -> list.filter { it.totalRemaining > 0 }
            AssignedItemStatusFilter.MOST_FREQUENT -> list.sortedByDescending { usageFrequencyMap[it.category.id] ?: 0 }
            AssignedItemStatusFilter.SPLIT_ONLY -> list.filter { it.isMultiAccount }
            AssignedItemStatusFilter.UNASSIGNED_ONLY -> list.filter { it.accountSplits.isEmpty() }
        }
    }

    // Apply Sorting Options
    val sortedOtherAccounts = remember(filteredOtherAccounts, sortOption) {
        when (sortOption) {
            AssignedItemSortOption.DEFAULT -> filteredOtherAccounts
            AssignedItemSortOption.BUDGET_DESC -> filteredOtherAccounts.sortedByDescending { it.totalBudgeted }
            AssignedItemSortOption.BUDGET_ASC -> filteredOtherAccounts.sortedBy { it.totalBudgeted }
            AssignedItemSortOption.REMAINING_DESC -> filteredOtherAccounts.sortedByDescending { it.totalRemaining }
            AssignedItemSortOption.MOST_USED -> filteredOtherAccounts.sortedByDescending { usageFrequencyMap[it.account.id] ?: 0 }
            AssignedItemSortOption.NAME_ASC -> filteredOtherAccounts.sortedBy { it.account.nameEn.lowercase() }
        }
    }

    val sortedExpenses = remember(filteredExpenses, sortOption) {
        when (sortOption) {
            AssignedItemSortOption.DEFAULT -> filteredExpenses
            AssignedItemSortOption.BUDGET_DESC -> filteredExpenses.sortedByDescending { it.totalBudgeted }
            AssignedItemSortOption.BUDGET_ASC -> filteredExpenses.sortedBy { it.totalBudgeted }
            AssignedItemSortOption.REMAINING_DESC -> filteredExpenses.sortedByDescending { it.totalRemaining }
            AssignedItemSortOption.MOST_USED -> filteredExpenses.sortedByDescending { usageFrequencyMap[it.category.id] ?: 0 }
            AssignedItemSortOption.NAME_ASC -> filteredExpenses.sortedBy { it.category.nameEn.lowercase() }
        }
    }

    val sortedIncomes = remember(filteredIncomes, sortOption) {
        when (sortOption) {
            AssignedItemSortOption.DEFAULT -> filteredIncomes
            AssignedItemSortOption.BUDGET_DESC -> filteredIncomes.sortedByDescending { it.totalBudgeted }
            AssignedItemSortOption.BUDGET_ASC -> filteredIncomes.sortedBy { it.totalBudgeted }
            AssignedItemSortOption.REMAINING_DESC -> filteredIncomes.sortedByDescending { it.totalRemaining }
            AssignedItemSortOption.MOST_USED -> filteredIncomes.sortedByDescending { usageFrequencyMap[it.category.id] ?: 0 }
            AssignedItemSortOption.NAME_ASC -> filteredIncomes.sortedBy { it.category.nameEn.lowercase() }
        }
    }

    val parentAccountMap = remember(allAccounts) { allAccounts.associateBy { it.id } }
    val parentCategoryMap = remember(allCategories) { allCategories.associateBy { it.id } }

    val totalItemsShown = when (sectionFilter) {
        AssignedItemSectionFilter.ALL -> sortedOtherAccounts.size + sortedExpenses.size + sortedIncomes.size
        AssignedItemSectionFilter.OTHER_ACCOUNTS -> sortedOtherAccounts.size
        AssignedItemSectionFilter.EXPENSES -> sortedExpenses.size
        AssignedItemSectionFilter.INCOMES -> sortedIncomes.size
    }

    val selectedAccount = remember(selectedAccountId, allPaymentSourceAccounts) {
        allPaymentSourceAccounts.find { it.id == selectedAccountId }
    }

    val groupedOtherAccounts = remember(sortedOtherAccounts, parentAccountMap) {
        sortedOtherAccounts.groupBy { it.account.parentId }
    }

    val groupedExpenses = remember(sortedExpenses, parentCategoryMap) {
        sortedExpenses.groupBy { it.category.parentId }
    }

    val groupedIncomes = remember(sortedIncomes, parentCategoryMap) {
        sortedIncomes.groupBy { it.category.parentId }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Search Bar & All Filter Rows Grouped
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // 1. Slim Compact Search Input
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট, ক্যাটাগরি অনুসন্ধান..." else "Search account, category...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("assigned_items_search_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. Source Accounts Micro-Chips Row
                if (allPaymentSourceAccounts.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CompactAssignedFilterChip(
                            selected = selectedAccountId == null,
                            onClick = onClearAccountFilter,
                            label = if (languageMode == LanguageMode.BANGLA) "সকল সোর্স" else "All Sources"
                        )
                        allPaymentSourceAccounts.forEach { acc ->
                            CompactAssignedFilterChip(
                                selected = selectedAccountId == acc.id,
                                onClick = {
                                    if (selectedAccountId == acc.id) onClearAccountFilter()
                                    else onSelectAccountFilter(acc.id)
                                },
                                label = acc.localizedName(languageMode),
                                leadingIcon = {
                                    Icon(
                                        IconHelper.getIconByName(acc.iconName),
                                        contentDescription = null,
                                        tint = if (selectedAccountId == acc.id) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // 3. Section Selector Micro-Chips Row (All | Other Accounts | Expense | Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CompactAssignedFilterChip(
                        selected = sectionFilter == AssignedItemSectionFilter.ALL,
                        onClick = { onSectionFilterChange(AssignedItemSectionFilter.ALL) },
                        label = if (languageMode == LanguageMode.BANGLA) "সকল আইটেম" else "All Items"
                    )
                    CompactAssignedFilterChip(
                        selected = sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS,
                        onClick = { onSectionFilterChange(AssignedItemSectionFilter.OTHER_ACCOUNTS) },
                        label = if (languageMode == LanguageMode.BANGLA) "অন্যান্য (${overview.otherAccountAllocations.size})" else "Other Accounts (${overview.otherAccountAllocations.size})",
                        leadingIcon = {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = if (sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    )
                    CompactAssignedFilterChip(
                        selected = sectionFilter == AssignedItemSectionFilter.EXPENSES,
                        onClick = { onSectionFilterChange(AssignedItemSectionFilter.EXPENSES) },
                        label = if (languageMode == LanguageMode.BANGLA) "ব্যয় (${overview.categoryAllocations.size})" else "Expenses (${overview.categoryAllocations.size})",
                        leadingIcon = {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = if (sectionFilter == AssignedItemSectionFilter.EXPENSES) SolidExpense else SolidExpense.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    )
                    CompactAssignedFilterChip(
                        selected = sectionFilter == AssignedItemSectionFilter.INCOMES,
                        onClick = { onSectionFilterChange(AssignedItemSectionFilter.INCOMES) },
                        label = if (languageMode == LanguageMode.BANGLA) "আয় (${overview.incomeAllocations.size})" else "Income (${overview.incomeAllocations.size})",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = if (sectionFilter == AssignedItemSectionFilter.INCOMES) SolidIncome else SolidIncome.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    )
                }

                // 4. Status & Sort Micro-Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var showSortMenu by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactAssignedFilterChip(
                            selected = statusFilter == AssignedItemStatusFilter.ALL,
                            onClick = { onStatusFilterChange(AssignedItemStatusFilter.ALL) },
                            label = if (languageMode == LanguageMode.BANGLA) "সকল" else "All"
                        )
                        CompactAssignedFilterChip(
                            selected = statusFilter == AssignedItemStatusFilter.BUDGETED_ONLY,
                            onClick = { onStatusFilterChange(AssignedItemStatusFilter.BUDGETED_ONLY) },
                            label = if (languageMode == LanguageMode.BANGLA) "বাজেটকৃত / দেনা" else "Budgeted / Due"
                        )
                        CompactAssignedFilterChip(
                            selected = statusFilter == AssignedItemStatusFilter.REMAINING_ONLY,
                            onClick = { onStatusFilterChange(AssignedItemStatusFilter.REMAINING_ONLY) },
                            label = if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining"
                        )
                        CompactAssignedFilterChip(
                            selected = statusFilter == AssignedItemStatusFilter.MOST_FREQUENT,
                            onClick = { onStatusFilterChange(AssignedItemStatusFilter.MOST_FREQUENT) },
                            label = if (languageMode == LanguageMode.BANGLA) "সর্বাধিক ব্যবহৃত" else "Most Used"
                        )
                        CompactAssignedFilterChip(
                            selected = statusFilter == AssignedItemStatusFilter.SPLIT_ONLY,
                            onClick = { onStatusFilterChange(AssignedItemStatusFilter.SPLIT_ONLY) },
                            label = if (languageMode == LanguageMode.BANGLA) "একাধিক সোর্সে বিভক্ত" else "Split Across Sources",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CallSplit,
                                    contentDescription = null,
                                    tint = if (statusFilter == AssignedItemStatusFilter.SPLIT_ONLY) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                        CompactAssignedFilterChip(
                            selected = statusFilter == AssignedItemStatusFilter.UNASSIGNED_ONLY,
                            onClick = { onStatusFilterChange(AssignedItemStatusFilter.UNASSIGNED_ONLY) },
                            label = if (languageMode == LanguageMode.BANGLA) "বরাদ্দহীন" else "Unassigned",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SolidExpense,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sorting Option Dropdown Button (Moved to right side)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSortMenu = true },
                            color = if (sortOption != AssignedItemSortOption.DEFAULT) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (sortOption != AssignedItemSortOption.DEFAULT) SolidPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = if (sortOption != AssignedItemSortOption.DEFAULT) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) sortOption.titleBn else sortOption.titleEn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (sortOption != AssignedItemSortOption.DEFAULT) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            AssignedItemSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (option == sortOption) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                            } else {
                                                Spacer(modifier = Modifier.size(16.dp))
                                            }
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) option.titleBn else option.titleEn,
                                                fontSize = 12.sp,
                                                fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal,
                                                color = if (option == sortOption) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSortOptionChange(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Empty state check
        if (totalItemsShown == 0) {
            item {
                EmptyStateCard(
                    message = if (languageMode == LanguageMode.BANGLA) "এই ফিল্টারে কোনো আইটেম পাওয়া যায়নি।" else "No items match this filter."
                )
            }
        }

        // 4. Section: Other Accounts (Grouped by original Account Group)
        if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS) && sortedOtherAccounts.isNotEmpty()) {
            if (sectionFilter == AssignedItemSectionFilter.ALL) {
                item {
                    SectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "অন্যান্য অ্যাকাউন্ট (ব্যক্তি / ঋণ / দেনা)" else "Other Accounts (Persons, Loans, Liabilities)",
                        count = sortedOtherAccounts.size,
                        icon = Icons.Default.People,
                        color = SolidTransfer
                    )
                }
            }

            groupedOtherAccounts.forEach { (parentId, itemsInGroup) ->
                val parentAccount = parentId?.let { parentAccountMap[it] }
                val groupTitle = parentAccount?.localizedName(languageMode)
                    ?: if (languageMode == LanguageMode.BANGLA) "সাধারণ অ্যাকাউন্ট" else "General Accounts"
                val groupIcon = parentAccount?.iconName ?: "account_balance"

                item(key = "hdr_other_acc_group_${parentId ?: -1L}") {
                    ItemGroupHeader(
                        title = groupTitle,
                        count = itemsInGroup.size,
                        iconName = groupIcon,
                        color = SolidTransfer
                    )
                }

                items(itemsInGroup, key = { "other_acc_${it.account.id}" }) { otherAccAlloc ->
                    OtherAccountAllocationCard(
                        allocation = otherAccAlloc,
                        languageMode = languageMode,
                        onOpenSplit = { onOpenOtherAccountSplitDialog(otherAccAlloc) },
                        onAddTransaction = {
                            val txType = if (otherAccAlloc.isExpense) TransactionType.EXPENSE else TransactionType.INCOME
                            onAddTransactionWithAccount(otherAccAlloc.account.id, txType)
                        }
                    )
                }
            }
        }

        // 5. Section: Expense Categories (Grouped by original Category Group)
        if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.EXPENSES) && sortedExpenses.isNotEmpty()) {
            if (sectionFilter == AssignedItemSectionFilter.ALL) {
                item {
                    SectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "ব্যয় ক্যাটাগরি" else "Expense Categories",
                        count = sortedExpenses.size,
                        icon = Icons.Default.MonetizationOn,
                        color = SolidExpense
                    )
                }
            }

            groupedExpenses.forEach { (parentId, itemsInGroup) ->
                val parentCat = parentId?.let { parentCategoryMap[it] }
                val groupTitle = parentCat?.localizedName(languageMode)
                    ?: if (languageMode == LanguageMode.BANGLA) "সাধারণ ব্যয়" else "General Expenses"
                val groupIcon = parentCat?.iconName ?: "category"
                val groupColor = try {
                    Color(android.graphics.Color.parseColor(parentCat?.colorHex))
                } catch (e: Exception) {
                    SolidExpense
                }

                item(key = "hdr_expense_group_${parentId ?: -1L}") {
                    ItemGroupHeader(
                        title = groupTitle,
                        count = itemsInGroup.size,
                        iconName = groupIcon,
                        color = groupColor
                    )
                }

                items(itemsInGroup, key = { "expense_${it.category.id}" }) { catAlloc ->
                    CategoryAllocationCard(
                        allocation = catAlloc,
                        isExpense = true,
                        languageMode = languageMode,
                        onOpenSplit = { onOpenCategorySplitDialog(catAlloc) }
                    )
                }
            }
        }

        // 6. Section: Income Categories (Grouped by original Category Group)
        if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.INCOMES) && sortedIncomes.isNotEmpty()) {
            if (sectionFilter == AssignedItemSectionFilter.ALL) {
                item {
                    SectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "আয় ক্যাটাগরি" else "Income Categories",
                        count = sortedIncomes.size,
                        icon = Icons.Default.Category,
                        color = SolidIncome
                    )
                }
            }

            groupedIncomes.forEach { (parentId, itemsInGroup) ->
                val parentCat = parentId?.let { parentCategoryMap[it] }
                val groupTitle = parentCat?.localizedName(languageMode)
                    ?: if (languageMode == LanguageMode.BANGLA) "সাধারণ আয়" else "General Income"
                val groupIcon = parentCat?.iconName ?: "payments"
                val groupColor = try {
                    Color(android.graphics.Color.parseColor(parentCat?.colorHex))
                } catch (e: Exception) {
                    SolidIncome
                }

                item(key = "hdr_income_group_${parentId ?: -1L}") {
                    ItemGroupHeader(
                        title = groupTitle,
                        count = itemsInGroup.size,
                        iconName = groupIcon,
                        color = groupColor
                    )
                }

                items(itemsInGroup, key = { "income_${it.category.id}" }) { catAlloc ->
                    CategoryAllocationCard(
                        allocation = catAlloc,
                        isExpense = false,
                        languageMode = languageMode,
                        onOpenSplit = { onOpenCategorySplitDialog(catAlloc) }
                    )
                }
            }
        }
    }
}
