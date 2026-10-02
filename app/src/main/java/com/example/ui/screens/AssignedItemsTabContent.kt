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

enum class AssignedItemActiveBottomTab {
    ACTIVE,
    INACTIVE
}

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
    onAddTransactionWithAccount: (Long, TransactionType) -> Unit,
    onToggleCategoryActive: (Long, Boolean) -> Unit = { _, _ -> },
    onToggleOtherAccountActive: (Long, Boolean) -> Unit = { _, _ -> },
    onBulkToggleActive: ((accountIds: List<Long>, categoryIds: List<Long>, isActive: Boolean) -> Unit)? = null,
    onBulkAssignSource: ((accountIds: List<Long>, categoryIds: List<Long>, sourceAccountId: Long) -> Unit)? = null
) {
    val q = searchQuery.trim().lowercase()

    var bottomActiveTab by remember { mutableStateOf(AssignedItemActiveBottomTab.ACTIVE) }
    var selectedItemKeys by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedItemKeys.isNotEmpty()
    var showBulkAssignDialog by remember { mutableStateOf(false) }

    val totalActiveCount = remember(overview) {
        overview.otherAccountAllocations.count { it.isActive } +
        overview.categoryAllocations.count { it.isActive } +
        overview.incomeAllocations.count { it.isActive }
    }
    val totalInactiveCount = remember(overview) {
        overview.otherAccountAllocations.count { !it.isActive } +
        overview.categoryAllocations.count { !it.isActive } +
        overview.incomeAllocations.count { !it.isActive }
    }

    val isTargetActive = bottomActiveTab == AssignedItemActiveBottomTab.ACTIVE

    // Filter Other Accounts
    val filteredOtherAccounts = remember(overview.otherAccountAllocations, q, statusFilter, usageFrequencyMap, selectedAccountId, isTargetActive) {
        var list = overview.otherAccountAllocations.filter { it.isActive == isTargetActive }
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
            AssignedItemStatusFilter.ALL, AssignedItemStatusFilter.ACTIVE_ONLY, AssignedItemStatusFilter.INACTIVE_ONLY -> list
            AssignedItemStatusFilter.BUDGETED_ONLY -> list.filter { it.totalBudgeted > 0 }
            AssignedItemStatusFilter.REMAINING_ONLY -> list.filter { it.totalRemaining > 0 }
            AssignedItemStatusFilter.MOST_FREQUENT -> list.sortedByDescending { usageFrequencyMap[it.account.id] ?: 0 }
            AssignedItemStatusFilter.SPLIT_ONLY -> list.filter { it.isMultiAccount }
            AssignedItemStatusFilter.UNASSIGNED_ONLY -> list.filter { it.accountSplits.isEmpty() }
        }
    }

    // Filter Expense Categories
    val filteredExpenses = remember(overview.categoryAllocations, q, statusFilter, usageFrequencyMap, selectedAccountId, isTargetActive) {
        var list = overview.categoryAllocations.filter { it.isActive == isTargetActive }
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
            AssignedItemStatusFilter.ALL, AssignedItemStatusFilter.ACTIVE_ONLY, AssignedItemStatusFilter.INACTIVE_ONLY -> list
            AssignedItemStatusFilter.BUDGETED_ONLY -> list.filter { it.totalBudgeted > 0 }
            AssignedItemStatusFilter.REMAINING_ONLY -> list.filter { it.totalRemaining > 0 }
            AssignedItemStatusFilter.MOST_FREQUENT -> list.sortedByDescending { usageFrequencyMap[it.category.id] ?: 0 }
            AssignedItemStatusFilter.SPLIT_ONLY -> list.filter { it.isMultiAccount }
            AssignedItemStatusFilter.UNASSIGNED_ONLY -> list.filter { it.accountSplits.isEmpty() }
        }
    }

    // Filter Income Categories
    val filteredIncomes = remember(overview.incomeAllocations, q, statusFilter, usageFrequencyMap, selectedAccountId, isTargetActive) {
        var list = overview.incomeAllocations.filter { it.isActive == isTargetActive }
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
            AssignedItemStatusFilter.ALL, AssignedItemStatusFilter.ACTIVE_ONLY, AssignedItemStatusFilter.INACTIVE_ONLY -> list
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
        AssignedItemSectionFilter.ALL, AssignedItemSectionFilter.ONLY_ITEMS -> sortedOtherAccounts.size + sortedExpenses.size + sortedIncomes.size
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

    val visibleKeys = remember(sortedOtherAccounts, sortedExpenses, sortedIncomes, sectionFilter) {
        val keys = mutableSetOf<String>()
        if (sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS || sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS) {
            sortedOtherAccounts.forEach { keys.add("OTHER_ACC_${it.account.id}") }
        }
        if (sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS || sectionFilter == AssignedItemSectionFilter.EXPENSES) {
            sortedExpenses.forEach { keys.add("EXPENSE_${it.category.id}") }
        }
        if (sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS || sectionFilter == AssignedItemSectionFilter.INCOMES) {
            sortedIncomes.forEach { keys.add("INCOME_${it.category.id}") }
        }
        keys
    }

    fun executeBulkToggleActive(newActive: Boolean) {
        val otherAccIds = selectedItemKeys.filter { it.startsWith("OTHER_ACC_") }.map { it.removePrefix("OTHER_ACC_").toLong() }
        val catIds = selectedItemKeys.filter { it.startsWith("EXPENSE_") || it.startsWith("INCOME_") }.map {
            if (it.startsWith("EXPENSE_")) it.removePrefix("EXPENSE_").toLong() else it.removePrefix("INCOME_").toLong()
        }
        if (onBulkToggleActive != null) {
            onBulkToggleActive(otherAccIds, catIds, newActive)
        } else {
            otherAccIds.forEach { onToggleOtherAccountActive(it, newActive) }
            catIds.forEach { onToggleCategoryActive(it, newActive) }
        }
        selectedItemKeys = emptySet()
    }

    fun executeBulkAssignSource(sourceAccountId: Long) {
        val otherAccIds = selectedItemKeys.filter { it.startsWith("OTHER_ACC_") }.map { it.removePrefix("OTHER_ACC_").toLong() }
        val catIds = selectedItemKeys.filter { it.startsWith("EXPENSE_") || it.startsWith("INCOME_") }.map {
            if (it.startsWith("EXPENSE_")) it.removePrefix("EXPENSE_").toLong() else it.removePrefix("INCOME_").toLong()
        }
        if (onBulkAssignSource != null) {
            onBulkAssignSource(otherAccIds, catIds, sourceAccountId)
        }
        selectedItemKeys = emptySet()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Multi-Selection Action Bar
        if (isSelectionMode) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedItemKeys = emptySet() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection", modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "${selectedItemKeys.size} টি নির্বাচিত"
                            else
                                "${selectedItemKeys.size} selected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = {
                                selectedItemKeys = if (selectedItemKeys.size == visibleKeys.size) emptySet() else visibleKeys
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (selectedItemKeys.size == visibleKeys.size)
                                    (if (languageMode == LanguageMode.BANGLA) "সব বাতিল" else "Deselect All")
                                else
                                    (if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Bulk Action 1: Toggle Active/Inactive
                        if (bottomActiveTab == AssignedItemActiveBottomTab.ACTIVE) {
                            FilledTonalButton(
                                onClick = { executeBulkToggleActive(false) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় করুন" else "Make Inactive",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            FilledTonalButton(
                                onClick = { executeBulkToggleActive(true) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = SolidIncome.copy(alpha = 0.2f),
                                    contentColor = SolidIncome
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সক্রিয় করুন" else "Make Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Bulk Action 2: Assign to Payment Source
                        if (allPaymentSourceAccounts.isNotEmpty()) {
                            FilledTonalButton(
                                onClick = { showBulkAssignDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সোর্সে বরাদ্দ" else "Assign Source",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
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
                            selected = sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS,
                            onClick = { onSectionFilterChange(AssignedItemSectionFilter.ONLY_ITEMS) },
                            label = if (languageMode == LanguageMode.BANGLA) "শুধু আইটেম" else "Only Items",
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = if (sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                        CompactAssignedFilterChip(
                            selected = sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS,
                            onClick = { onSectionFilterChange(AssignedItemSectionFilter.OTHER_ACCOUNTS) },
                            label = if (languageMode == LanguageMode.BANGLA) "অন্যান্য (${overview.otherAccountAllocations.count { it.isActive == isTargetActive }})" else "Other Accounts (${overview.otherAccountAllocations.count { it.isActive == isTargetActive }})",
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
                            label = if (languageMode == LanguageMode.BANGLA) "ব্যয় (${overview.categoryAllocations.count { it.isActive == isTargetActive }})" else "Expenses (${overview.categoryAllocations.count { it.isActive == isTargetActive }})",
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
                            label = if (languageMode == LanguageMode.BANGLA) "আয় (${overview.incomeAllocations.count { it.isActive == isTargetActive }})" else "Income (${overview.incomeAllocations.count { it.isActive == isTargetActive }})",
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

                        // Sorting Option Dropdown Button (Compact icon only)
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSortMenu = true },
                                color = if (sortOption != AssignedItemSortOption.DEFAULT) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (sortOption != AssignedItemSortOption.DEFAULT) SolidPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort",
                                        tint = if (sortOption != AssignedItemSortOption.DEFAULT) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
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

            // Selected Account banner if filtered by an account from payment source tab
            if (selectedAccountId != null && selectedAccount != null) {
                item(key = "selected_account_banner") {
                    val basisLabel = if (overview.calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                        if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget"
                    } else {
                        if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining"
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, SolidPrimary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    IconHelper.getIconByName(selectedAccount.iconName),
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "${selectedAccount.localizedName(languageMode)} এর বরাদ্দ ($basisLabel)"
                                    else
                                        "Assigned to ${selectedAccount.localizedName(languageMode)} ($basisLabel)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(
                                onClick = onClearAccountFilter,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Empty state check
            if (totalItemsShown == 0) {
                item {
                    PaymentSourceEmptyCard(
                        message = if (languageMode == LanguageMode.BANGLA) "এই ফিল্টারে কোনো আইটেম পাওয়া যায়নি।" else "No items match this filter."
                    )
                }
            }

            val isWithoutGroups = sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS || selectedAccountId != null

            // 4. Section: Other Accounts
            if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS || sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS) && sortedOtherAccounts.isNotEmpty()) {
                if (sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS) {
                    item {
                        SectionHeader(
                            title = if (languageMode == LanguageMode.BANGLA) "অন্যান্য অ্যাকাউন্ট (ব্যক্তি / ঋণ / দেনা)" else "Other Accounts (Persons, Loans, Liabilities)",
                            count = sortedOtherAccounts.size,
                            icon = Icons.Default.People,
                            color = SolidTransfer
                        )
                    }
                }

                if (isWithoutGroups) {
                    items(sortedOtherAccounts, key = { "compact_other_${it.account.id}" }) { otherAccAlloc ->
                        val itemKey = "OTHER_ACC_${otherAccAlloc.account.id}"
                        val isSelected = selectedItemKeys.contains(itemKey)
                        val split = if (selectedAccountId != null) otherAccAlloc.accountSplits.find { it.account.id == selectedAccountId } else null
                        val isBudgetBasis = overview.calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT
                        val effectiveDueAmt = if (otherAccAlloc.totalBudgeted > 0) otherAccAlloc.totalBudgeted else Math.abs(otherAccAlloc.currentBalance)
                        val amt = if (selectedAccountId != null) {
                            if (isBudgetBasis) (split?.allocatedAmount ?: effectiveDueAmt) else (split?.remaining ?: otherAccAlloc.totalRemaining)
                        } else {
                            if (isBudgetBasis) effectiveDueAmt else otherAccAlloc.totalRemaining
                        }
                        val amtLabel = if (isBudgetBasis) {
                            if (otherAccAlloc.accountSplits.isNotEmpty()) {
                                if (languageMode == LanguageMode.BANGLA) "বরাদ্দ বাজেট" else "Assigned Budget"
                            } else {
                                if (otherAccAlloc.isExpense) (if (languageMode == LanguageMode.BANGLA) "বকেয়া দেনা" else "Due Balance")
                                else (if (languageMode == LanguageMode.BANGLA) "প্রাপ্য পাওনা" else "Receivable")
                            }
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "বরাদ্দ অবশিষ্ট" else "Assigned Remaining"
                        }
                        CompactAssignedItemRow(
                            title = otherAccAlloc.account.localizedName(languageMode),
                            subtitle = if (otherAccAlloc.isExpense) (if (languageMode == LanguageMode.BANGLA) "দেনা / ঋণ" else "Payable") else (if (languageMode == LanguageMode.BANGLA) "পাওনা" else "Receivable"),
                            iconName = otherAccAlloc.account.iconName,
                            iconColor = SolidTransfer,
                            amount = amt,
                            amountTypeLabel = amtLabel,
                            amountColor = SolidTransfer,
                            languageMode = languageMode,
                            isSplitAcrossSources = otherAccAlloc.isMultiAccount,
                            splitBadgeText = if (otherAccAlloc.isMultiAccount) "${otherAccAlloc.accountSplits.size} Sources" else null,
                            onClick = { onOpenOtherAccountSplitDialog(otherAccAlloc) },
                            onLongClick = {
                                selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                            },
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onSelectToggle = {
                                selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                            }
                        )
                    }
                } else {
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
                            val itemKey = "OTHER_ACC_${otherAccAlloc.account.id}"
                            val isSelected = selectedItemKeys.contains(itemKey)
                            OtherAccountAllocationCard(
                                allocation = otherAccAlloc,
                                languageMode = languageMode,
                                onOpenSplit = { onOpenOtherAccountSplitDialog(otherAccAlloc) },
                                onAddTransaction = {
                                    val txType = if (otherAccAlloc.isExpense) TransactionType.EXPENSE else TransactionType.INCOME
                                    onAddTransactionWithAccount(otherAccAlloc.account.id, txType)
                                },
                                onToggleActive = { active ->
                                    onToggleOtherAccountActive(otherAccAlloc.account.id, active)
                                },
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onSelectToggle = {
                                    selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                                },
                                onLongClick = {
                                    selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                                }
                            )
                        }
                    }
                }
            }

            // 5. Section: Expense Categories
            if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS || sectionFilter == AssignedItemSectionFilter.EXPENSES) && sortedExpenses.isNotEmpty()) {
                if (sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS) {
                    item {
                        SectionHeader(
                            title = if (languageMode == LanguageMode.BANGLA) "ব্যয় ক্যাটাগরি" else "Expense Categories",
                            count = sortedExpenses.size,
                            icon = Icons.Default.MonetizationOn,
                            color = SolidExpense
                        )
                    }
                }

                if (isWithoutGroups) {
                    items(sortedExpenses, key = { "compact_expense_${it.category.id}" }) { catAlloc ->
                        val itemKey = "EXPENSE_${catAlloc.category.id}"
                        val isSelected = selectedItemKeys.contains(itemKey)
                        val split = if (selectedAccountId != null) catAlloc.accountSplits.find { it.account.id == selectedAccountId } else null
                        val isBudgetBasis = overview.calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT
                        val amt = if (selectedAccountId != null) {
                            if (isBudgetBasis) (split?.allocatedAmount ?: catAlloc.totalBudgeted) else (split?.remaining ?: catAlloc.totalRemaining)
                        } else {
                            if (isBudgetBasis) catAlloc.totalBudgeted else catAlloc.totalRemaining
                        }
                        val amtLabel = if (isBudgetBasis) {
                            if (languageMode == LanguageMode.BANGLA) "বরাদ্দ বাজেট" else "Assigned Budget"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "বরাদ্দ অবশিষ্ট" else "Assigned Remaining"
                        }
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(catAlloc.category.colorHex))
                        } catch (e: Exception) {
                            SolidExpense
                        }
                        val parentCat = catAlloc.category.parentId?.let { parentCategoryMap[it] }
                        CompactAssignedItemRow(
                            title = catAlloc.category.localizedName(languageMode),
                            subtitle = parentCat?.localizedName(languageMode),
                            iconName = catAlloc.category.iconName,
                            iconColor = catColor,
                            amount = amt,
                            amountTypeLabel = amtLabel,
                            amountColor = SolidExpense,
                            languageMode = languageMode,
                            isSplitAcrossSources = catAlloc.isMultiAccount,
                            splitBadgeText = if (catAlloc.isMultiAccount) "${catAlloc.accountSplits.size} Sources" else null,
                            onClick = { onOpenCategorySplitDialog(catAlloc) },
                            onLongClick = {
                                selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                            },
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onSelectToggle = {
                                selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                            }
                        )
                    }
                } else {
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
                            val itemKey = "EXPENSE_${catAlloc.category.id}"
                            val isSelected = selectedItemKeys.contains(itemKey)
                            CategoryAllocationCard(
                                allocation = catAlloc,
                                isExpense = true,
                                languageMode = languageMode,
                                onOpenSplit = { onOpenCategorySplitDialog(catAlloc) },
                                onToggleActive = { active ->
                                    onToggleCategoryActive(catAlloc.category.id, active)
                                },
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onSelectToggle = {
                                    selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                                },
                                onLongClick = {
                                    selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                                }
                            )
                        }
                    }
                }
            }

            // 6. Section: Income Categories
            if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS || sectionFilter == AssignedItemSectionFilter.INCOMES) && sortedIncomes.isNotEmpty()) {
                if (sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.ONLY_ITEMS) {
                    item {
                        SectionHeader(
                            title = if (languageMode == LanguageMode.BANGLA) "আয় ক্যাটাগরি" else "Income Categories",
                            count = sortedIncomes.size,
                            icon = Icons.Default.Category,
                            color = SolidIncome
                        )
                    }
                }

                if (isWithoutGroups) {
                    items(sortedIncomes, key = { "compact_income_${it.category.id}" }) { catAlloc ->
                        val itemKey = "INCOME_${catAlloc.category.id}"
                        val isSelected = selectedItemKeys.contains(itemKey)
                        val split = if (selectedAccountId != null) catAlloc.accountSplits.find { it.account.id == selectedAccountId } else null
                        val isBudgetBasis = overview.calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT
                        val amt = if (selectedAccountId != null) {
                            if (isBudgetBasis) (split?.allocatedAmount ?: catAlloc.totalBudgeted) else (split?.remaining ?: catAlloc.totalRemaining)
                        } else {
                            if (isBudgetBasis) catAlloc.totalBudgeted else catAlloc.totalRemaining
                        }
                        val amtLabel = if (isBudgetBasis) {
                            if (languageMode == LanguageMode.BANGLA) "প্রত্যাশিত আয়" else "Assigned Income"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট আয়" else "Remaining Income"
                        }
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(catAlloc.category.colorHex))
                        } catch (e: Exception) {
                            SolidIncome
                        }
                        val parentCat = catAlloc.category.parentId?.let { parentCategoryMap[it] }
                        CompactAssignedItemRow(
                            title = catAlloc.category.localizedName(languageMode),
                            subtitle = parentCat?.localizedName(languageMode),
                            iconName = catAlloc.category.iconName,
                            iconColor = catColor,
                            amount = amt,
                            amountTypeLabel = amtLabel,
                            amountColor = SolidIncome,
                            languageMode = languageMode,
                            isSplitAcrossSources = catAlloc.isMultiAccount,
                            splitBadgeText = if (catAlloc.isMultiAccount) "${catAlloc.accountSplits.size} Sources" else null,
                            onClick = { onOpenCategorySplitDialog(catAlloc) },
                            onLongClick = {
                                selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                            },
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onSelectToggle = {
                                selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                            }
                        )
                    }
                } else {
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
                            val itemKey = "INCOME_${catAlloc.category.id}"
                            val isSelected = selectedItemKeys.contains(itemKey)
                            CategoryAllocationCard(
                                allocation = catAlloc,
                                isExpense = false,
                                languageMode = languageMode,
                                onOpenSplit = { onOpenCategorySplitDialog(catAlloc) },
                                onToggleActive = { active ->
                                    onToggleCategoryActive(catAlloc.category.id, active)
                                },
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onSelectToggle = {
                                    selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                                },
                                onLongClick = {
                                    selectedItemKeys = if (isSelected) selectedItemKeys - itemKey else selectedItemKeys + itemKey
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Two Tabs: Active / Inactive
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active Tab Button
                val isActiveSelected = bottomActiveTab == AssignedItemActiveBottomTab.ACTIVE
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            bottomActiveTab = AssignedItemActiveBottomTab.ACTIVE
                            selectedItemKeys = emptySet()
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActiveSelected) SolidIncome.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = if (isActiveSelected) BorderStroke(1.5.dp, SolidIncome) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isActiveSelected) SolidIncome else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সক্রিয়" else "Active",
                            fontSize = 13.sp,
                            fontWeight = if (isActiveSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActiveSelected) SolidIncome else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isActiveSelected) SolidIncome else MaterialTheme.colorScheme.outlineVariant
                        ) {
                            Text(
                                text = totalActiveCount.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActiveSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Inactive Tab Button
                val isInactiveSelected = bottomActiveTab == AssignedItemActiveBottomTab.INACTIVE
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            bottomActiveTab = AssignedItemActiveBottomTab.INACTIVE
                            selectedItemKeys = emptySet()
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isInactiveSelected) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = if (isInactiveSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isInactiveSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয়" else "Inactive",
                            fontSize = 13.sp,
                            fontWeight = if (isInactiveSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isInactiveSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isInactiveSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                        ) {
                            Text(
                                text = totalInactiveCount.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInactiveSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Bulk Assign Source Dialog
    if (showBulkAssignDialog) {
        AlertDialog(
            onDismissRequest = { showBulkAssignDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্সে বরাদ্দ করুন" else "Assign to Payment Source",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "নির্বাচিত ${selectedItemKeys.size} টি আইটেম কোন পেমেন্ট সোর্সে সম্পূর্ণ বরাদ্দ করবেন?"
                        else
                            "Select a payment source account to assign all ${selectedItemKeys.size} selected items to:",
                        fontSize = 13.sp
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                        items(allPaymentSourceAccounts) { acc ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        executeBulkAssignSource(acc.id)
                                        showBulkAssignDialog = false
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        IconHelper.getIconByName(acc.iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = SolidPrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        acc.localizedName(languageMode),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBulkAssignDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
