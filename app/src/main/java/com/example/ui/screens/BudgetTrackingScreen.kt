package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.ActiveBudgetFilterBar
import com.example.ui.components.AppTabHeader
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.components.BudgetDateRangePreset
import com.example.ui.components.BudgetComparisonPreset
import com.example.ui.components.BudgetFilterDialog
import com.example.ui.components.BudgetFilterState
import com.example.ui.components.BudgetSortOrder
import com.example.ui.components.ExportMenuButton
import com.example.ui.components.calculateBudgetFilterRanges
import com.example.ui.components.formatBudgetAmount
import com.example.ui.theme.SolidIncome
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.TabExportHelper
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import kotlin.math.roundToInt

private val CrimsonPink = Color(0xFFE91E63)
private val AlertRed = Color(0xFFF43F5E)
private val BrandBlueLight = Color(0xFF0284C7)
private val SoftCyan = Color(0xFF38BDF8)
private val SlateText = Color(0xFF64748B)

data class CategoryBudgetTrackingItem(
    val category: Category,
    val spentAmount: Double,
    val spentBaseAmount: Double = 0.0,
    val budgetLimit: Double,
    val isEnabled: Boolean,
    val transactions: List<TransactionWithDetails>,
    val baseTransactions: List<TransactionWithDetails> = emptyList()
) {
    val hasBudget: Boolean get() = budgetLimit > 0 && isEnabled
    val progressRatio: Float get() = if (budgetLimit > 0) (spentAmount / budgetLimit).toFloat() else 0f
    val percentageInt: Int get() = if (budgetLimit > 0) ((spentAmount / budgetLimit) * 100).roundToInt() else 0
    val isOverBudget: Boolean get() = hasBudget && spentAmount > budgetLimit
    val diffAmount: Double get() = kotlin.math.abs(budgetLimit - spentAmount)
    val deltaSpent: Double get() = spentAmount - spentBaseAmount
    val deltaPercent: Double get() = if (spentBaseAmount > 0.0) ((spentAmount - spentBaseAmount) / spentBaseAmount) * 100.0 else 0.0
}

data class CategoryGroupBudgetTracking(
    val parentCategory: Category?,
    val groupNameEn: String,
    val groupNameBn: String,
    val items: List<CategoryBudgetTrackingItem>
) {
    val totalSpent: Double get() = items.sumOf { it.spentAmount }
    val totalSpentBase: Double get() = items.sumOf { it.spentBaseAmount }
    val totalBudget: Double get() = items.filter { it.hasBudget }.sumOf { it.budgetLimit }
    val hasBudget: Boolean get() = totalBudget > 0
    val progressRatio: Float get() = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val percentageInt: Int get() = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).roundToInt() else 0
    val isOverBudget: Boolean get() = hasBudget && totalSpent > totalBudget
    val diffAmount: Double get() = kotlin.math.abs(totalBudget - totalSpent)
    val deltaSpent: Double get() = totalSpent - totalSpentBase
    val deltaPercent: Double get() = if (totalSpentBase > 0.0) ((totalSpent - totalSpentBase) / totalSpentBase) * 100.0 else 0.0
}

/**
 * Budget Screen
 * Displays date card with comparison, top bar filters and timeline navigation,
 * and a permanently fixed Budgeted & Expensed header tab bar when scrolling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackingScreen(
    viewModel: BudgetViewModel,
    allCategories: List<Category>,
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    transactionsWithDetails: List<TransactionWithDetails>,
    monthlyBudgets: List<MonthlyBudget>,
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onNavigateToBudgetMaker: () -> Unit,
    onAddTransactionWithCategory: (Category) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    val context = LocalContext.current
    // Filter State
    var filterState by remember { mutableStateOf(BudgetFilterState()) }
    var showTimelineScreen by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showBaseMonthPicker by remember { mutableStateOf(false) }
    var showBaseDatePicker by remember { mutableStateOf(false) }
    var showBaseStartDatePicker by remember { mutableStateOf(false) }
    var showBaseEndDatePicker by remember { mutableStateOf(false) }
    var showCompareDatePicker by remember { mutableStateOf(false) }
    var showComparisonPresetPicker by remember { mutableStateOf(false) }
    var isDualDateFlow by remember { mutableStateOf(false) }
    var isSpeedDialExpanded by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategoryForDetail by remember { mutableStateOf<CategoryBudgetTrackingItem?>(null) }
    var activeTabMode by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"

    // Group expanded state map (default true: expanded)
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    // Extract labels from transactions for filter dialog
    val allLabels = remember(transactionsWithDetails) {
        transactionsWithDetails.flatMap { details ->
            val note = details.transaction.note
            val hashTags = Regex("#([\\w\\d_-]+)").findAll(note).map { it.value }.toList()
            val commaSeparated = if (note.contains(",")) note.split(",").map { it.trim() }.filter { it.startsWith("#") || it.length in 2..20 } else emptyList()
            (hashTags + commaSeparated).filter { it.isNotBlank() }
        }.distinct().sorted()
    }

    // TIMELINE NAVIGATION
    if (showTimelineScreen) {
        val allTx = remember(transactionsWithDetails) { transactionsWithDetails.map { it.transaction } }
        AccountTimelineScreen(
            accounts = allAccounts,
            transactions = allTx,
            languageMode = languageMode,
            onBack = { showTimelineScreen = false }
        )
        return
    }

    // Determine Base Date Range and Compare Date Range based on Filter State
    val filterRangeResult = remember(
        selectedYear,
        selectedMonth,
        filterState,
        languageMode
    ) {
        calculateBudgetFilterRanges(
            year = selectedYear,
            month = selectedMonth,
            filterState = filterState,
            languageMode = languageMode
        )
    }
    val compareRange = filterRangeResult.primaryRange
    val baseRange = if (filterState.comparisonEnabled) filterRangeResult.compareRange else null
    val compareDateLabel = filterRangeResult.primaryLabel
    val baseDateLabel = filterRangeResult.compareLabel

    // Current (Compare) Month Transactions
    val compareMonthTransactions = remember(transactionsWithDetails, compareRange, filterState) {
        transactionsWithDetails.filter { details ->
            val tx = details.transaction
            val inTime = tx.dateEpochMs in compareRange.first..compareRange.second
            val matchesAcc = filterState.selectedAccountIds.isEmpty() ||
                    filterState.selectedAccountIds.contains(tx.debitAccountId) ||
                    filterState.selectedAccountIds.contains(tx.creditAccountId)
            val matchesStatus = filterState.selectedStatusSet.isEmpty() ||
                    filterState.selectedStatusSet.contains(tx.status)
            val matchesCat = filterState.selectedCategoryIds.isEmpty() ||
                    (tx.categoryId != null && filterState.selectedCategoryIds.contains(tx.categoryId)) ||
                    (tx.subCategoryId != null && filterState.selectedCategoryIds.contains(tx.subCategoryId))
            val matchesLabels = filterState.selectedLabels.isEmpty() ||
                    filterState.selectedLabels.any { tx.note.contains(it, ignoreCase = true) }
            val matchesZero = !filterState.excludeZeroAmounts || tx.amount > 0.0
            val minAmt = filterState.minAmount
            val maxAmt = filterState.maxAmount
            val matchesMin = minAmt == null || tx.amount >= minAmt
            val matchesMax = maxAmt == null || tx.amount <= maxAmt
            inTime && matchesAcc && matchesStatus && matchesCat && matchesLabels && matchesZero && matchesMin && matchesMax
        }
    }

    // Base (Previous / Baseline) Month Transactions
    val baseMonthTransactions = remember(transactionsWithDetails, baseRange, filterState) {
        if (baseRange == null) emptyList()
        else {
            transactionsWithDetails.filter { details ->
                val tx = details.transaction
                val inTime = tx.dateEpochMs in baseRange.first..baseRange.second
                val matchesAcc = filterState.selectedAccountIds.isEmpty() ||
                        filterState.selectedAccountIds.contains(tx.debitAccountId) ||
                        filterState.selectedAccountIds.contains(tx.creditAccountId)
                val matchesStatus = filterState.selectedStatusSet.isEmpty() ||
                        filterState.selectedStatusSet.contains(tx.status)
                val matchesCat = filterState.selectedCategoryIds.isEmpty() ||
                        (tx.categoryId != null && filterState.selectedCategoryIds.contains(tx.categoryId)) ||
                        (tx.subCategoryId != null && filterState.selectedCategoryIds.contains(tx.subCategoryId))
                val matchesLabels = filterState.selectedLabels.isEmpty() ||
                        filterState.selectedLabels.any { tx.note.contains(it, ignoreCase = true) }
                val matchesZero = !filterState.excludeZeroAmounts || tx.amount > 0.0
                val minAmt = filterState.minAmount
                val maxAmt = filterState.maxAmount
                val matchesMin = minAmt == null || tx.amount >= minAmt
                val matchesMax = maxAmt == null || tx.amount <= maxAmt
                inTime && matchesAcc && matchesStatus && matchesCat && matchesLabels && matchesZero && matchesMin && matchesMax
            }
        }
    }

    // Map monthly budget items: "itemType_itemId" -> MonthlyBudget
    val budgetMap = remember(monthlyBudgets) {
        monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }
    }

    // Calculate TODAY marker position (0.0 to 1.0)
    val todayPaceRatio = remember(selectedYear, selectedMonth) {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1
        val currentDay = now.get(Calendar.DAY_OF_MONTH)

        when {
            selectedYear < currentYear || (selectedYear == currentYear && selectedMonth < currentMonth) -> 1.0f
            selectedYear > currentYear || (selectedYear == currentYear && selectedMonth > currentMonth) -> 0.0f
            else -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, selectedYear)
                cal.set(Calendar.MONTH, selectedMonth - 1)
                val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                (currentDay.toFloat() / maxDays.toFloat()).coerceIn(0.01f, 0.99f)
            }
        }
    }

    // Parent categories & child items
    val parentCatMap = remember(allCategories) {
        allCategories.filter { it.parentId == null }.associateBy { it.id }
    }

    val targetCatType = if (activeTabMode == "EXPENSE") CategoryType.EXPENSE else CategoryType.INCOME
    val itemTypeKey = if (activeTabMode == "EXPENSE") "EXPENSE" else "INCOME"

    // Grouping calculations
    val categoryGroups = remember(
        allCategories,
        compareMonthTransactions,
        baseMonthTransactions,
        budgetMap,
        parentCatMap,
        targetCatType,
        itemTypeKey,
        searchQuery,
        filterState
    ) {
        val relevantCategories = allCategories.filter { it.type == targetCatType }
        val parentCategories = relevantCategories.filter { it.parentId == null }
        val childCategories = relevantCategories.filter { it.parentId != null }

        val groupedByParent = childCategories.groupBy { it.parentId }
        val resultList = mutableListOf<CategoryGroupBudgetTracking>()

        // 1. Process regular parent categories with subcategories or standalone parent categories
        parentCategories.forEach { parent ->
            val children = groupedByParent[parent.id] ?: emptyList()
            if (children.isNotEmpty()) {
                val trackingItems = children.map { cat ->
                    val catTxs = compareMonthTransactions.filter {
                        it.transaction.categoryId == cat.id || it.transaction.subCategoryId == cat.id
                    }
                    val baseTxs = baseMonthTransactions.filter {
                        it.transaction.categoryId == cat.id || it.transaction.subCategoryId == cat.id
                    }
                    val spent = catTxs.sumOf { it.transaction.amount }
                    val spentBase = baseTxs.sumOf { it.transaction.amount }
                    val budgetEntry = budgetMap["${itemTypeKey}_${cat.id}"]
                    val budgetLimit = budgetEntry?.budgetedAmount ?: cat.budgetLimit
                    val isEnabled = budgetEntry?.isEnabled ?: true

                    CategoryBudgetTrackingItem(
                        category = cat,
                        spentAmount = spent,
                        spentBaseAmount = spentBase,
                        budgetLimit = budgetLimit,
                        isEnabled = isEnabled,
                        transactions = catTxs,
                        baseTransactions = baseTxs
                    )
                }.filter { item ->
                    val matchesSearch = searchQuery.isEmpty() ||
                            item.category.nameEn.contains(searchQuery, ignoreCase = true) ||
                            item.category.nameBn.contains(searchQuery, ignoreCase = true)
                    val matchesBudgeted = !filterState.filterOnlyBudgeted || item.hasBudget
                    val matchesOver = !filterState.filterOnlyOverBudget || item.isOverBudget
                    val matchesCatFilter = filterState.selectedCategoryIds.isEmpty() || filterState.selectedCategoryIds.contains(item.category.id)
                    val matchesZero = !filterState.excludeZeroAmounts || (item.spentAmount > 0.0 || item.budgetLimit > 0.0)
                    val minAmt = filterState.minAmount
                    val maxAmt = filterState.maxAmount
                    val matchesMin = minAmt == null || (item.spentAmount >= minAmt || item.budgetLimit >= minAmt)
                    val matchesMax = maxAmt == null || (item.spentAmount <= maxAmt || item.budgetLimit <= maxAmt)
                    matchesSearch && matchesBudgeted && matchesOver && matchesCatFilter && matchesZero && matchesMin && matchesMax
                }

                val shouldSortByAmount = filterState.sortByAmount || filterState.sortOrder == BudgetSortOrder.AMOUNT_DESC || filterState.sortOrder == BudgetSortOrder.SPENT_DESC
                val sortedTrackingItems = if (shouldSortByAmount) {
                    trackingItems.sortedByDescending { it.spentAmount }
                } else {
                    when (filterState.sortOrder) {
                        BudgetSortOrder.AMOUNT_DESC, BudgetSortOrder.SPENT_DESC -> trackingItems.sortedByDescending { it.spentAmount }
                        BudgetSortOrder.AMOUNT_ASC -> trackingItems.sortedBy { it.spentAmount }
                        BudgetSortOrder.BUDGET_DESC -> trackingItems.sortedByDescending { it.budgetLimit }
                        BudgetSortOrder.BUDGET_ASC -> trackingItems.sortedBy { it.budgetLimit }
                        BudgetSortOrder.UTILIZATION_DESC -> trackingItems.sortedByDescending { it.percentageInt }
                        BudgetSortOrder.NAME_ASC -> trackingItems.sortedBy { it.category.nameEn.lowercase() }
                        BudgetSortOrder.DEFAULT -> trackingItems
                    }
                }

                val shouldIncludeGroup = if (filterState.excludeZeroAmounts || filterState.hideEmptyGroups) {
                    sortedTrackingItems.isNotEmpty()
                } else {
                    sortedTrackingItems.isNotEmpty() || (searchQuery.isEmpty() && !filterState.filterOnlyBudgeted && !filterState.filterOnlyOverBudget && filterState.selectedCategoryIds.isEmpty())
                }

                if (shouldIncludeGroup) {
                    resultList.add(
                        CategoryGroupBudgetTracking(
                            parentCategory = parent,
                            groupNameEn = parent.nameEn,
                            groupNameBn = parent.nameBn,
                            items = sortedTrackingItems
                        )
                    )
                }
            } else {
                // Standalone parent category
                val catTxs = compareMonthTransactions.filter {
                    it.transaction.categoryId == parent.id || it.transaction.subCategoryId == parent.id
                }
                val baseTxs = baseMonthTransactions.filter {
                    it.transaction.categoryId == parent.id || it.transaction.subCategoryId == parent.id
                }
                val spent = catTxs.sumOf { it.transaction.amount }
                val spentBase = baseTxs.sumOf { it.transaction.amount }
                val budgetEntry = budgetMap["${itemTypeKey}_${parent.id}"]
                val budgetLimit = budgetEntry?.budgetedAmount ?: parent.budgetLimit
                val isEnabled = budgetEntry?.isEnabled ?: true

                val singleItem = CategoryBudgetTrackingItem(
                    category = parent,
                    spentAmount = spent,
                    spentBaseAmount = spentBase,
                    budgetLimit = budgetLimit,
                    isEnabled = isEnabled,
                    transactions = catTxs,
                    baseTransactions = baseTxs
                )

                val matchesSearch = searchQuery.isEmpty() ||
                        parent.nameEn.contains(searchQuery, ignoreCase = true) ||
                        parent.nameBn.contains(searchQuery, ignoreCase = true)
                val matchesBudgeted = !filterState.filterOnlyBudgeted || singleItem.hasBudget
                val matchesOver = !filterState.filterOnlyOverBudget || singleItem.isOverBudget
                val matchesCatFilter = filterState.selectedCategoryIds.isEmpty() || filterState.selectedCategoryIds.contains(parent.id)
                val matchesZero = !filterState.excludeZeroAmounts || (singleItem.spentAmount > 0.0 || singleItem.budgetLimit > 0.0)
                val minAmt = filterState.minAmount
                val maxAmt = filterState.maxAmount
                val matchesMin = minAmt == null || (singleItem.spentAmount >= minAmt || singleItem.budgetLimit >= minAmt)
                val matchesMax = maxAmt == null || (singleItem.spentAmount <= maxAmt || singleItem.budgetLimit <= maxAmt)

                if (matchesSearch && matchesBudgeted && matchesOver && matchesCatFilter && matchesZero && matchesMin && matchesMax) {
                    resultList.add(
                        CategoryGroupBudgetTracking(
                            parentCategory = parent,
                            groupNameEn = parent.nameEn,
                            groupNameBn = parent.nameBn,
                            items = listOf(singleItem)
                        )
                    )
                }
            }
        }

        // 2. Orphaned child categories
        val orphanedChildren = childCategories.filter { child -> parentCategories.none { it.id == child.parentId } }
        if (orphanedChildren.isNotEmpty()) {
            val orphanItems = orphanedChildren.map { cat ->
                val catTxs = compareMonthTransactions.filter {
                    it.transaction.categoryId == cat.id || it.transaction.subCategoryId == cat.id
                }
                val baseTxs = baseMonthTransactions.filter {
                    it.transaction.categoryId == cat.id || it.transaction.subCategoryId == cat.id
                }
                val spent = catTxs.sumOf { it.transaction.amount }
                val spentBase = baseTxs.sumOf { it.transaction.amount }
                val budgetEntry = budgetMap["${itemTypeKey}_${cat.id}"]
                val budgetLimit = budgetEntry?.budgetedAmount ?: cat.budgetLimit
                val isEnabled = budgetEntry?.isEnabled ?: true

                CategoryBudgetTrackingItem(
                    category = cat,
                    spentAmount = spent,
                    spentBaseAmount = spentBase,
                    budgetLimit = budgetLimit,
                    isEnabled = isEnabled,
                    transactions = catTxs,
                    baseTransactions = baseTxs
                )
            }.filter { item ->
                val matchesSearch = searchQuery.isEmpty() ||
                        item.category.nameEn.contains(searchQuery, ignoreCase = true) ||
                        item.category.nameBn.contains(searchQuery, ignoreCase = true)
                val matchesBudgeted = !filterState.filterOnlyBudgeted || item.hasBudget
                val matchesOver = !filterState.filterOnlyOverBudget || item.isOverBudget
                val matchesCatFilter = filterState.selectedCategoryIds.isEmpty() || filterState.selectedCategoryIds.contains(item.category.id)
                val matchesZero = !filterState.excludeZeroAmounts || (item.spentAmount > 0.0 || item.budgetLimit > 0.0)
                val minAmt = filterState.minAmount
                val maxAmt = filterState.maxAmount
                val matchesMin = minAmt == null || (item.spentAmount >= minAmt || item.budgetLimit >= minAmt)
                val matchesMax = maxAmt == null || (item.spentAmount <= maxAmt || item.budgetLimit <= maxAmt)
                matchesSearch && matchesBudgeted && matchesOver && matchesCatFilter && matchesZero && matchesMin && matchesMax
            }

            val shouldSortByAmount = filterState.sortByAmount || filterState.sortOrder == BudgetSortOrder.AMOUNT_DESC || filterState.sortOrder == BudgetSortOrder.SPENT_DESC
            val sortedOrphanItems = if (shouldSortByAmount) {
                orphanItems.sortedByDescending { it.spentAmount }
            } else {
                when (filterState.sortOrder) {
                    BudgetSortOrder.AMOUNT_DESC, BudgetSortOrder.SPENT_DESC -> orphanItems.sortedByDescending { it.spentAmount }
                    BudgetSortOrder.AMOUNT_ASC -> orphanItems.sortedBy { it.spentAmount }
                    BudgetSortOrder.BUDGET_DESC -> orphanItems.sortedByDescending { it.budgetLimit }
                    BudgetSortOrder.BUDGET_ASC -> orphanItems.sortedBy { it.budgetLimit }
                    BudgetSortOrder.UTILIZATION_DESC -> orphanItems.sortedByDescending { it.percentageInt }
                    BudgetSortOrder.NAME_ASC -> orphanItems.sortedBy { it.category.nameEn.lowercase() }
                    BudgetSortOrder.DEFAULT -> orphanItems
                }
            }

            if (sortedOrphanItems.isNotEmpty()) {
                resultList.add(
                    CategoryGroupBudgetTracking(
                        parentCategory = null,
                        groupNameEn = "Others",
                        groupNameBn = "অন্যান্য",
                        items = sortedOrphanItems
                    )
                )
            }
        }

        val shouldSortByAmount = filterState.sortByAmount || filterState.sortOrder == BudgetSortOrder.AMOUNT_DESC || filterState.sortOrder == BudgetSortOrder.SPENT_DESC
        if (shouldSortByAmount) {
            resultList.sortByDescending { it.totalSpent }
        } else if (filterState.sortOrder == BudgetSortOrder.BUDGET_DESC) {
            resultList.sortByDescending { it.totalBudget }
        } else if (filterState.sortOrder == BudgetSortOrder.NAME_ASC) {
            resultList.sortBy { it.groupNameEn.lowercase() }
        }

        resultList
    }

    // Overall Totals
    val totalFlowSpent = remember(categoryGroups) {
        categoryGroups.sumOf { it.totalSpent }
    }
    val totalFlowSpentBase = remember(categoryGroups) {
        categoryGroups.sumOf { it.totalSpentBase }
    }
    val totalFlowBudget = remember(categoryGroups) {
        categoryGroups.sumOf { it.totalBudget }
    }
    val overallPercentage = remember(totalFlowSpent, totalFlowBudget) {
        if (totalFlowBudget > 0) ((totalFlowSpent / totalFlowBudget) * 100).roundToInt() else 0
    }
    val isOverallOver = totalFlowBudget > 0 && totalFlowSpent > totalFlowBudget
    val overallDiff = kotlin.math.abs(totalFlowBudget - totalFlowSpent)
    val overallDeltaSpent = totalFlowSpent - totalFlowSpentBase
    val overallDeltaPercent = if (totalFlowSpentBase > 0.0) ((overallDeltaSpent / totalFlowSpentBase) * 100.0) else 0.0

    // Track scroll state to transition top card between normal and mini mode
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("budget_tracking_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. App Tab Header with Timeline, Search, and Filter buttons
            AppTabHeader(
                title = LanguageHelper.getString("budget", languageMode),
                showTimelineButton = true,
                onTimelineClick = { showTimelineScreen = true },
                showFilterButton = true,
                isFilterActive = filterState.isFilterActive,
                onFilterClick = { showFilterDialog = true },
                onOpenDrawer = onOpenDrawer,
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    ExportMenuButton(
                        languageMode = languageMode,
                        onExport = { format ->
                            val monthYear = "${DateUtils.getMonthName(selectedMonth, languageMode)} $selectedYear"
                            val periodLabel = buildString {
                                if (filterState.datePreset == BudgetDateRangePreset.CUSTOM && compareDateLabel.isNotBlank()) {
                                    append(compareDateLabel)
                                } else {
                                    append(monthYear)
                                }
                                if (filterState.comparisonEnabled && baseDateLabel.isNotBlank()) {
                                    append(" vs $baseDateLabel")
                                }
                                if (filterState.filterOnlyBudgeted) {
                                    append(" • ")
                                    append(if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বাজেটকৃত" else "Budgeted Only")
                                }
                                if (filterState.filterOnlyOverBudget) {
                                    append(" • ")
                                    append(if (languageMode == LanguageMode.BANGLA) "বাজেট অতিক্রান্ত" else "Over Budget")
                                }
                                if (searchQuery.isNotBlank()) {
                                    append(" • \"${searchQuery.trim()}\"")
                                }
                            }
                            TabExportHelper.exportBudget(
                                context = context,
                                format = format,
                                periodLabel = periodLabel,
                                activeTabMode = activeTabMode,
                                groups = categoryGroups,
                                totalBudget = totalFlowBudget,
                                totalSpent = totalFlowSpent,
                                showComparison = filterState.comparisonEnabled && baseRange != null,
                                languageMode = languageMode
                            )
                        }
                    )
                }
            )

            // Search bar expansion
            AnimatedVisibility(
                visible = isSearchActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search categories...") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandBlueLight) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlueLight,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        )
                    }
                }
            }

            // 2. SCROLL-AWARE TOP FIXED CARD (Normal at page start, Mini when scrolling)
            if (!isScrolled) {
                // NORMAL TOP FIXED CARD (Full Summary + Dual Comparison Dates / Month picker + Progress)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .animateContentSize(animationSpec = tween(durationMillis = 200))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        // Top Row: Date Navigation & Comparison
                        if (!filterState.comparisonEnabled || baseRange == null) {
                            // Single Date / Month Selector with "+ Compare" button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.prevBudgetMonth() },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Previous Month",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { showMonthPicker = true }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = BrandBlueLight,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = compareDateLabel,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.nextBudgetMonth() },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next Month",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                // "+ Compare" Quick Action Chip
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    border = BorderStroke(0.5.dp, BrandBlueLight.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            filterState = filterState.copy(
                                                comparisonEnabled = true,
                                                comparisonPreset = BudgetComparisonPreset.LAST_MONTH
                                            )
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CompareArrows,
                                            contentDescription = null,
                                            tint = BrandBlueLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "+ তুলনা" else "+ Compare",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandBlueLight
                                        )
                                    }
                                }
                            }
                        } else {
                            // DUAL COMPARISON DATES / MONTHS ROW
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Date 1: Base Period Box
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 3.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val curStart = baseRange.first
                                                val cal = Calendar.getInstance().apply {
                                                    timeInMillis = curStart
                                                    add(Calendar.MONTH, -1)
                                                }
                                                val y = cal.get(Calendar.YEAR)
                                                val m = cal.get(Calendar.MONTH) + 1
                                                filterState = filterState.copy(
                                                    comparisonEnabled = true,
                                                    comparisonPreset = BudgetComparisonPreset.CUSTOM,
                                                    customCompareStartMs = DateUtils.getStartOfMonth(y, m),
                                                    customCompareEndMs = DateUtils.getEndOfMonth(y, m)
                                                )
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Prev Base Month",
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable { showComparisonPresetPicker = true }
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "বেস সময়কাল" else "Base Period",
                                                fontSize = 8.5.sp,
                                                color = SlateText,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = baseDateLabel,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2,
                                                softWrap = true,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val curStart = baseRange.first
                                                val cal = Calendar.getInstance().apply {
                                                    timeInMillis = curStart
                                                    add(Calendar.MONTH, 1)
                                                }
                                                val y = cal.get(Calendar.YEAR)
                                                val m = cal.get(Calendar.MONTH) + 1
                                                filterState = filterState.copy(
                                                    comparisonEnabled = true,
                                                    comparisonPreset = BudgetComparisonPreset.CUSTOM,
                                                    customCompareStartMs = DateUtils.getStartOfMonth(y, m),
                                                    customCompareEndMs = DateUtils.getEndOfMonth(y, m)
                                                )
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next Base Month",
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Comparison Separator / Close Button
                                Surface(
                                    shape = CircleShape,
                                    color = BrandBlueLight.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            filterState = filterState.copy(comparisonEnabled = false)
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "VS",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BrandBlueLight
                                        )
                                    }
                                }

                                // Date 2: Compare Period Box
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    border = BorderStroke(0.5.dp, BrandBlueLight.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 3.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.prevBudgetMonth() },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Prev Compare Month",
                                                modifier = Modifier.size(12.dp),
                                                tint = BrandBlueLight
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable { showMonthPicker = true }
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "চলতি মাস" else "Compare Period",
                                                fontSize = 8.5.sp,
                                                color = BrandBlueLight,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = compareDateLabel,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2,
                                                softWrap = true,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.nextBudgetMonth() },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next Compare Month",
                                                modifier = Modifier.size(12.dp),
                                                tint = BrandBlueLight
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Middle Row: 3 Summary Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Metric 1: Budget Target
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "বাজেট লক্ষ্য" else "Budgeted",
                                    fontSize = 10.sp,
                                    color = BrandBlueLight,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = formatBudgetAmount(totalFlowBudget, filterState, languageMode, false),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (totalFlowBudget > 0) "$overallPercentage% planned" else "No budget",
                                    fontSize = 8.5.sp,
                                    color = SlateText,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier
                                    .height(34.dp)
                                    .padding(horizontal = 2.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )

                            // Metric 2: Total Spent / Earned
                            Column(
                                modifier = Modifier.weight(1.35f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (activeTabMode == "EXPENSE") {
                                        if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Spent"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Earned"
                                    },
                                    fontSize = 10.sp,
                                    color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = formatBudgetAmount(totalFlowSpent, filterState, languageMode),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (filterState.comparisonEnabled && baseRange != null) {
                                    val isIncrease = overallDeltaSpent > 0
                                    val sign = if (isIncrease) "+" else ""
                                    Text(
                                        text = "Base: ${formatBudgetAmount(totalFlowSpentBase, filterState, languageMode, false)} ($sign${overallDeltaPercent.toInt()}%)",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SlateText,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        softWrap = true,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            VerticalDivider(
                                modifier = Modifier
                                    .height(34.dp)
                                    .padding(horizontal = 2.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )

                            // Metric 3: Remaining / Over Budget
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (totalFlowBudget > 0) {
                                        if (isOverallOver) {
                                            if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত" else "Over Budget"
                                        } else {
                                            if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining"
                                        }
                                    } else "Difference",
                                    fontSize = 10.sp,
                                    color = if (isOverallOver) CrimsonPink else SolidIncome,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = if (totalFlowBudget > 0) {
                                        formatBudgetAmount(overallDiff, filterState, languageMode)
                                    } else "-",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverallOver) CrimsonPink else SolidIncome,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isOverallOver) "Over limit" else "Under limit",
                                    fontSize = 8.5.sp,
                                    color = if (isOverallOver) CrimsonPink else SlateText,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Continuous Progress Bar with | TODAY Marker
                        BudgetProgressBarWithTodayMarker(
                            progressRatio = if (totalFlowBudget > 0) (totalFlowSpent / totalFlowBudget).toFloat() else 0f,
                            todayPaceRatio = todayPaceRatio,
                            isOverBudget = isOverallOver,
                            barHeight = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // MINI TOP FIXED CARD WHEN SCROLLING
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .animateContentSize(animationSpec = tween(durationMillis = 200))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Mini Date / Comparison
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showMonthPicker = true }
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = BrandBlueLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (!filterState.comparisonEnabled || baseRange == null) {
                                        compareDateLabel
                                    } else {
                                        "$baseDateLabel ➔ $compareDateLabel"
                                    },
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Center & Right: Mini spent vs budget & remaining
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = formatBudgetAmount(totalFlowSpent, filterState, languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome
                                )

                                if (totalFlowBudget > 0) {
                                    Text(
                                        text = "/ ${formatBudgetAmount(totalFlowBudget, filterState, languageMode, false)} ($overallPercentage%)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SlateText
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isOverallOver) CrimsonPink.copy(alpha = 0.15f) else SolidIncome.copy(alpha = 0.15f),
                                        modifier = Modifier.padding(start = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isOverallOver) {
                                                "+${formatBudgetAmount(overallDiff, filterState, languageMode, false)}"
                                            } else {
                                                "${formatBudgetAmount(overallDiff, filterState, languageMode, false)} left"
                                            },
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOverallOver) CrimsonPink else SolidIncome,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // Mini slim progress bar
                        BudgetProgressBarWithTodayMarker(
                            progressRatio = if (totalFlowBudget > 0) (totalFlowSpent / totalFlowBudget).toFloat() else 0f,
                            todayPaceRatio = todayPaceRatio,
                            isOverBudget = isOverallOver,
                            barHeight = 2.5.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Active Filters Bar
            ActiveBudgetFilterBar(
                filterState = filterState,
                onFilterChange = { filterState = it },
                onOpenFilterDialog = { showFilterDialog = true },
                languageMode = languageMode,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // Comparison Column Labels (When Comparison is Active)
            if (filterState.comparisonEnabled && baseRange != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateText
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.widthIn(min = 75.dp), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = baseDateLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateText,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.widthIn(min = 85.dp), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = compareDateLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandBlueLight,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 5. SCROLLABLE CATEGORY GROUPS & ITEMS (LazyColumn with listState)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 125.dp)
            ) {
                if (categoryGroups.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp, bottom = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No categories or budgets match the active filters.",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        filterState = BudgetFilterState()
                                        searchQuery = ""
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Reset Filters")
                                }
                                Button(
                                    onClick = onNavigateToBudgetMaker,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlueLight)
                                ) {
                                    Text("Open Budget Maker")
                                }
                            }
                        }
                    }
                } else {
                    if (filterState.showOnlyCategoriesWithoutGroups) {
                        val flatItems = categoryGroups.flatMap { it.items }.let { list ->
                            val shouldSortByAmount = filterState.sortByAmount || filterState.sortOrder == BudgetSortOrder.AMOUNT_DESC || filterState.sortOrder == BudgetSortOrder.SPENT_DESC
                            if (shouldSortByAmount) {
                                list.sortedByDescending { it.spentAmount }
                            } else {
                                when (filterState.sortOrder) {
                                    BudgetSortOrder.AMOUNT_DESC, BudgetSortOrder.SPENT_DESC -> list.sortedByDescending { it.spentAmount }
                                    BudgetSortOrder.AMOUNT_ASC -> list.sortedBy { it.spentAmount }
                                    BudgetSortOrder.BUDGET_DESC -> list.sortedByDescending { it.budgetLimit }
                                    BudgetSortOrder.BUDGET_ASC -> list.sortedBy { it.budgetLimit }
                                    BudgetSortOrder.UTILIZATION_DESC -> list.sortedByDescending { it.percentageInt }
                                    BudgetSortOrder.NAME_ASC -> list.sortedBy { it.category.nameEn.lowercase() }
                                    BudgetSortOrder.DEFAULT -> list
                                }
                            }
                        }
                        items(
                            items = flatItems,
                            key = { "${it.category.id}_${it.category.nameEn}" }
                        ) { item ->
                            CategoryRow(
                                item = item,
                                isSubcategory = false,
                                showComparison = filterState.comparisonEnabled && baseRange != null,
                                todayPaceRatio = todayPaceRatio,
                                languageMode = languageMode,
                                onClick = {
                                    selectedCategoryForDetail = item
                                }
                            )
                        }
                    } else {
                        items(
                            items = categoryGroups,
                            key = { "${it.parentCategory?.id ?: "others"}_${it.groupNameEn}_${it.groupNameBn}" }
                        ) { group ->
                            val groupKey = "${group.parentCategory?.id ?: "others"}_${group.groupNameEn}_${group.groupNameBn}"
                            val isExpanded = expandedGroups[groupKey] ?: true

                            CategoryGroupSection(
                                group = group,
                                isExpanded = isExpanded,
                                showComparison = filterState.comparisonEnabled && baseRange != null,
                                todayPaceRatio = todayPaceRatio,
                                languageMode = languageMode,
                                onToggleExpand = {
                                    expandedGroups[groupKey] = !isExpanded
                                },
                                onCategoryClick = { trackingItem ->
                                    selectedCategoryForDetail = trackingItem
                                }
                            )
                        }
                    }
                }
            }
        }

        // 6. BOTTOM PINNED CONTAINER: EXPENSE / INCOME TOGGLE + FABs ABOVE NAVIGATION TABS
        val headerScrollState = LocalHeaderScrollState.current
        AutoHidingBottomContainer(
            headerScrollState = headerScrollState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Expandable Speed Dial Action FAB above the toggle (Single button on tap shows two)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                AnimatedVisibility(
                    visible = isSpeedDialExpanded,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 10.dp, end = 4.dp)
                    ) {
                        // Option 1: Budget Maker
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.clickable {
                                isSpeedDialExpanded = false
                                onNavigateToBudgetMaker()
                            }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "বাজেট মেকার" else "Budget Maker",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandBlueLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            FloatingActionButton(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    onNavigateToBudgetMaker()
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = BrandBlueLight,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("budget_maker_fab")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Budget Maker",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Option 2: Add Transaction
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.clickable {
                                isSpeedDialExpanded = false
                                val defaultCat = allCategories.firstOrNull { it.type == targetCatType && it.parentId != null }
                                if (defaultCat != null) onAddTransactionWithCategory(defaultCat)
                                else onNavigateToBudgetMaker()
                            }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "নতুন লেনদেন" else "Add Transaction",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (activeTabMode == "EXPENSE") Color(0xFF2563EB) else SolidIncome,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            FloatingActionButton(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    val defaultCat = allCategories.firstOrNull { it.type == targetCatType && it.parentId != null }
                                    if (defaultCat != null) onAddTransactionWithCategory(defaultCat)
                                    else onNavigateToBudgetMaker()
                                },
                                containerColor = if (activeTabMode == "EXPENSE") Color(0xFF2563EB) else SolidIncome,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("budget_add_tx_fab")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Transaction",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Main Unified Speed Dial FAB
                val fabRotation by animateFloatAsState(
                    targetValue = if (isSpeedDialExpanded) 45f else 0f,
                    label = "FabRotation"
                )
                FloatingActionButton(
                    onClick = { isSpeedDialExpanded = !isSpeedDialExpanded },
                    containerColor = if (activeTabMode == "EXPENSE") Color(0xFF2563EB) else SolidIncome,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("budget_speed_dial_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isSpeedDialExpanded) "Close Actions" else "Open Actions",
                        modifier = Modifier
                            .size(26.dp)
                            .rotate(fabRotation)
                    )
                }
            }

            // Segmented Toggle: [ Expenses (ব্যয়) | Incomes (আয়) ]
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expense Button
                    val isExpense = activeTabMode == "EXPENSE"
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isExpense) CrimsonPink.copy(alpha = 0.16f) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { activeTabMode = "EXPENSE" }
                            .testTag("budget_mode_expense")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isExpense) CrimsonPink else SlateText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ব্যয় (Expenses)" else "Expenses",
                                fontSize = 13.sp,
                                fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                                color = if (isExpense) CrimsonPink else SlateText
                            )
                        }
                    }

                    // Income Button
                    val isIncome = activeTabMode == "INCOME"
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isIncome) SolidIncome.copy(alpha = 0.16f) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { activeTabMode = "INCOME" }
                            .testTag("budget_mode_income")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (isIncome) SolidIncome else SlateText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আয় (Incomes)" else "Incomes",
                                fontSize = 13.sp,
                                fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                                color = if (isIncome) SolidIncome else SlateText
                            )
                        }
                    }
                }
            }
        }
    }
}

    // DETAIL MODAL: Shows transactions in selected period for clicked category
    selectedCategoryForDetail?.let { detailItem ->
        CategoryTransactionsDetailDialog(
            item = detailItem,
            languageMode = languageMode,
            onDismiss = { selectedCategoryForDetail = null },
            onEditTransaction = { tx ->
                selectedCategoryForDetail = null
                onEditTransaction(tx)
            },
            onAddTransaction = {
                selectedCategoryForDetail = null
                onAddTransactionWithCategory(detailItem.category)
            },
            onOpenBudgetMaker = {
                selectedCategoryForDetail = null
                onNavigateToBudgetMaker()
            },
            onAccountClick = { acc ->
                selectedCategoryForDetail = null
                onAccountClick?.invoke(acc)
            }
        )
    }

    // MONTH PICKER DIALOG (Compare Period)
    if (showMonthPicker) {
        MonthPickerDropdownDialog(
            title = if (filterState.comparisonEnabled && baseRange != null) {
                if (languageMode == LanguageMode.BANGLA) "চলতি মাস নির্বাচন করুন" else "Select Compare Month"
            } else null,
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onSelect = { y, m ->
                viewModel.setBudgetYearMonth(y, m)
                showMonthPicker = false
            }
        )
    }

    // BASE MONTH PICKER DIALOG
    if (showBaseMonthPicker) {
        val cal = Calendar.getInstance().apply {
            if (baseRange != null) timeInMillis = baseRange.first
        }
        MonthPickerDropdownDialog(
            title = if (languageMode == LanguageMode.BANGLA) "বেস মাস নির্বাচন করুন" else "Select Base Month",
            currentYear = cal.get(Calendar.YEAR),
            currentMonth = cal.get(Calendar.MONTH) + 1,
            onDismiss = { showBaseMonthPicker = false },
            onSelect = { y, m ->
                filterState = filterState.copy(
                    comparisonEnabled = true,
                    comparisonPreset = BudgetComparisonPreset.CUSTOM,
                    customCompareStartMs = DateUtils.getStartOfMonth(y, m),
                    customCompareEndMs = DateUtils.getEndOfMonth(y, m)
                )
                showBaseMonthPicker = false
            }
        )
    }

    // BASE DATE PICKER
    if (showBaseDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = filterState.customCompareStartMs ?: (baseRange?.first ?: System.currentTimeMillis())
        )
        DatePickerDialog(
            onDismissRequest = {
                showBaseDatePicker = false
                isDualDateFlow = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            filterState = filterState.copy(
                                comparisonEnabled = true,
                                comparisonPreset = BudgetComparisonPreset.CUSTOM,
                                customCompareStartMs = selected
                            )
                        }
                        showBaseDatePicker = false
                        if (isDualDateFlow) {
                            isDualDateFlow = false
                            showCompareDatePicker = true
                        }
                    }
                ) {
                    Text(if (isDualDateFlow) "Next: Compare Date" else "Set Base Date")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBaseDatePicker = false
                    isDualDateFlow = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // BASE CUSTOM START DATE PICKER
    if (showBaseStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = filterState.customCompareStartMs ?: (System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L)
        )
        DatePickerDialog(
            onDismissRequest = { showBaseStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis
                    if (selected != null) {
                        filterState = filterState.copy(
                            comparisonEnabled = true,
                            comparisonPreset = BudgetComparisonPreset.CUSTOM,
                            customCompareStartMs = selected
                        )
                    }
                    showBaseStartDatePicker = false
                }) {
                    Text("OK", color = BrandBlueLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBaseStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // BASE CUSTOM END DATE PICKER
    if (showBaseEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = filterState.customCompareEndMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showBaseEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis
                    if (selected != null) {
                        filterState = filterState.copy(
                            comparisonEnabled = true,
                            comparisonPreset = BudgetComparisonPreset.CUSTOM,
                            customCompareEndMs = selected
                        )
                    }
                    showBaseEndDatePicker = false
                }) {
                    Text("OK", color = BrandBlueLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBaseEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // COMPARISON PRESET SELECTION MODAL
    if (showComparisonPresetPicker) {
        Dialog(onDismissRequest = { showComparisonPresetPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "তুলনার সময়কাল নির্বাচন করুন" else "Select Comparison Baseline",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueLight,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(BudgetComparisonPreset.values()) { preset ->
                            val isSelected = filterState.comparisonEnabled && filterState.comparisonPreset == preset
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrandBlueLight.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = if (isSelected) BorderStroke(1.dp, BrandBlueLight) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        filterState = filterState.copy(
                                            comparisonEnabled = true,
                                            comparisonPreset = preset
                                        )
                                        showComparisonPresetPicker = false
                                        if (preset == BudgetComparisonPreset.CUSTOM) {
                                            showBaseStartDatePicker = true
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) preset.titleBn else preset.titleEn,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandBlueLight else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = BrandBlueLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Option to pick specific Month via Month Picker
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showComparisonPresetPicker = false
                                        showBaseMonthPicker = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = BrandBlueLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ক্যালেন্ডার থেকে মাস বাছুন..." else "Pick Specific Month...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrandBlueLight
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showComparisonPresetPicker = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // COMPARE DATE PICKER
    if (showCompareDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = filterState.customStartDateMs ?: compareRange.first
        )
        DatePickerDialog(
            onDismissRequest = { showCompareDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            filterState = filterState.copy(
                                datePreset = BudgetDateRangePreset.CUSTOM,
                                customStartDateMs = selected
                            )
                        }
                        showCompareDatePicker = false
                    }
                ) {
                    Text("Set Compare Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompareDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // BUDGET FILTER DIALOG
    if (showFilterDialog) {
        BudgetFilterDialog(
            currentFilter = filterState,
            categories = allCategories,
            accounts = allAccounts,
            allLabels = allLabels,
            languageMode = languageMode,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilter ->
                filterState = newFilter
                showFilterDialog = false
            }
        )
    }
}

/**
 * Fixed Comparison Dates Card matching Balance Sheet styling.
 */
@Composable
private fun BudgetComparisonDatesCard(
    preset: BudgetComparisonPreset,
    baseDateLabel: String,
    compareDateLabel: String,
    showOnlyCurrentBalance: Boolean,
    languageMode: LanguageMode,
    onOpenFilter: () -> Unit,
    onPickBaseDate: () -> Unit,
    onPickCompareDate: () -> Unit,
    onPickBothDates: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Top Row: Preset Capsule Chip + Quick Shift Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preset Capsule / Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.clickable { onOpenFilter() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = BrandBlueLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val presetName = if (languageMode == LanguageMode.BANGLA) preset.titleBn else preset.titleEn
                        Text(
                            text = presetName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlueLight
                        )
                    }
                }

                // Previous and Next Month Switchers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Period",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Period",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Date Range Display with two clickable dates to compare
            if (showOnlyCurrentBalance) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onPickCompareDate() }
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "বর্তমান বাজেট সময়কাল" else "Active Budget Period",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = compareDateLabel,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueLight
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Change Date",
                            tint = BrandBlueLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clickable Base Date Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPickBaseDate() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "বেস সময়কাল" else "Base Period",
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = baseDateLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Edit Base Date",
                                tint = BrandBlueLight,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Dual arrow separator
                    IconButton(
                        onClick = onPickBothDates,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Pick Both Dates",
                            tint = BrandBlueLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Clickable Compare Date Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPickCompareDate() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "চলতি সময়কাল" else "Compare Period",
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = compareDateLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandBlueLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Edit Compare Date",
                                tint = BrandBlueLight,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category Group Section with Blue Chevron, Group Name, Total Spent,
 * optional Group-level progress bar, and collapsible child rows.
 */
@Composable
private fun CategoryGroupSection(
    group: CategoryGroupBudgetTracking,
    isExpanded: Boolean,
    showComparison: Boolean,
    todayPaceRatio: Float,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onCategoryClick: (CategoryBudgetTrackingItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        // Group Header Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Blue Circle Chevron + Group Icon + Group Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Blue circular chevron toggle
                        Surface(
                            shape = CircleShape,
                            color = BrandBlueLight,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = Color.White,
                                modifier = Modifier.padding(2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Group Indicating Icon Badge
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandBlueLight.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(group.parentCategory?.iconName ?: "Category"),
                                contentDescription = null,
                                tint = BrandBlueLight,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = LanguageHelper.getLocalizedName(group.groupNameEn, group.groupNameBn, languageMode),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (group.hasBudget) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (group.isOverBudget) {
                                    CrimsonPink.copy(alpha = 0.12f)
                                } else {
                                    BrandBlueLight.copy(alpha = 0.12f)
                                }
                            ) {
                                Text(
                                    text = "${group.percentageInt}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (group.isOverBudget) CrimsonPink else BrandBlueLight,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }

                    // Right: Group Total Spent Amount in straight right-aligned columns
                    if (showComparison) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier.widthIn(min = 75.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = LanguageHelper.formatCurrency(group.totalSpentBase, languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateText,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier.widthIn(min = 85.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = LanguageHelper.formatCurrency(group.totalSpent, languageMode),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (group.totalSpent > 0) CrimsonPink else SlateText,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.widthIn(min = 85.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(group.totalSpent, languageMode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (group.totalSpent > 0) CrimsonPink else SlateText,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // If group has an overall budget, show group-level progress bar and stats
                if (group.hasBudget) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${group.percentageInt}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val diffText = if (group.isOverBudget) {
                            "${LanguageHelper.formatCurrency(group.diffAmount, languageMode)} over ${LanguageHelper.formatCurrency(group.totalBudget, languageMode)}"
                        } else {
                            "${LanguageHelper.formatCurrency(group.diffAmount, languageMode)} left from ${LanguageHelper.formatCurrency(group.totalBudget, languageMode)}"
                        }

                        Text(
                            text = diffText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (group.isOverBudget) CrimsonPink else SlateText
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    BudgetProgressBarWithTodayMarker(
                        progressRatio = group.progressRatio,
                        todayPaceRatio = todayPaceRatio,
                        isOverBudget = group.isOverBudget,
                        barHeight = 5.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Collapsible Children Rows indented under the group name
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                group.items.forEach { item ->
                    CategoryRow(
                        item = item,
                        isSubcategory = true,
                        showComparison = showComparison,
                        todayPaceRatio = todayPaceRatio,
                        languageMode = languageMode,
                        onClick = { onCategoryClick(item) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Category Row matching screenshot:
 * Avatar icon (colored circle), category name, actual spent amount on right,
 * and if budgeted, % progress, remaining text, and progress bar with | TODAY marker.
 * Subcategories are indented slightly right of the group header.
 */
@Composable
private fun CategoryRow(
    item: CategoryBudgetTrackingItem,
    isSubcategory: Boolean = false,
    showComparison: Boolean,
    todayPaceRatio: Float,
    languageMode: LanguageMode,
    onClick: () -> Unit
) {
    val parsedColor = remember(item.category.colorHex) {
        try {
            IconHelper.parseColorHex(item.category.colorHex)
        } catch (_: Exception) {
            Color(0xFFE91E63)
        }
    }

    val rowShape = RoundedCornerShape(12.dp)
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = rowShape,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        shadowElevation = 0.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isSubcategory) 28.dp else 12.dp,
                end = 12.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .clip(rowShape)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Main Line: Category Icon + Name + Percentage Badge + Spent Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Circle Category Avatar
                    Surface(
                        shape = CircleShape,
                        color = parsedColor,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = IconHelper.getIconByName(item.category.iconName),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Category Name
                    Text(
                        text = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Percentage next to name, kept close
                    if (item.hasBudget) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.isOverBudget) {
                                CrimsonPink.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            }
                        ) {
                            Text(
                                text = "${item.percentageInt}%",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isOverBudget) CrimsonPink else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }

                // Spent Amount in straight right-aligned columns
                if (showComparison) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier.widthIn(min = 75.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(item.spentBaseAmount, languageMode),
                                fontSize = 11.5.sp,
                                color = SlateText,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier.widthIn(min = 85.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(item.spentAmount, languageMode),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.spentAmount > 0) CrimsonPink else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.widthIn(min = 85.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = LanguageHelper.formatCurrency(item.spentAmount, languageMode),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.spentAmount > 0) CrimsonPink else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Budget Sub-row (if category has an active budget limit)
            if (item.hasBudget) {
                Spacer(modifier = Modifier.height(6.dp))

                val diffText = if (item.isOverBudget) {
                    "${LanguageHelper.formatCurrency(item.diffAmount, languageMode)} over ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)}"
                } else {
                    "${LanguageHelper.formatCurrency(item.diffAmount, languageMode)} left from ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)}"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diffText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.isOverBudget) CrimsonPink else SlateText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                BudgetProgressBarWithTodayMarker(
                    progressRatio = item.progressRatio,
                    todayPaceRatio = todayPaceRatio,
                    isOverBudget = item.isOverBudget,
                    barHeight = 5.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Custom Budget Progress Bar with the "| TODAY" vertical tick marker.
 * Replicates the screenshot's dual-tone visual bar and pace indicator.
 */
@Composable
private fun BudgetProgressBarWithTodayMarker(
    progressRatio: Float,
    todayPaceRatio: Float,
    isOverBudget: Boolean,
    modifier: Modifier = Modifier,
    barHeight: androidx.compose.ui.unit.Dp = 6.dp
) {
    val barColor = if (isOverBudget) CrimsonPink else SoftCyan
    val clampedProgress = progressRatio.coerceIn(0f, 1f)

    Column(modifier = modifier) {
        // Track and Progress Fill
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            val totalWidth = maxWidth
            if (clampedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProgress)
                        .height(barHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                )
            }
        }

        // TODAY Marker Row underneath the progress bar
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            val totalWidth = maxWidth
            val markerFraction = todayPaceRatio.coerceIn(0.02f, 0.90f)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset(x = totalWidth * markerFraction)
            ) {
                // Vertical tick line
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(7.dp)
                        .background(BrandBlueLight)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "TODAY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueLight,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Dialog displaying all transactions recorded for a specific category in the selected period.
 */
@Composable
private fun CategoryTransactionsDetailDialog(
    item: CategoryBudgetTrackingItem,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransaction: () -> Unit,
    onOpenBudgetMaker: () -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                val context = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total Spent: ${LanguageHelper.formatCurrency(item.spentAmount, languageMode)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrimsonPink
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExportMenuButton(
                            languageMode = languageMode,
                            onExport = { format ->
                                TabExportHelper.exportTransactions(
                                    context = context,
                                    format = format,
                                    transactions = item.transactions,
                                    filterSummary = "Category: ${item.category.nameEn}",
                                    languageMode = languageMode
                                )
                            }
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                if (item.hasBudget) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Budget: ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${item.percentageInt}% used",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isOverBudget) CrimsonPink else BrandBlueLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Transactions in this period (${item.transactions.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (item.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found in this period.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(item.transactions) { txDetails ->
                            val tx = txDetails.transaction
                            val dateStr = com.example.util.DateUtils.formatDate(tx.dateEpochMs, languageMode)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditTransaction(tx) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (tx.note.isNotEmpty()) tx.note else "Expense",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = dateStr,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            val acc = txDetails.debitAccount ?: txDetails.creditAccount
                                            if (acc != null) {
                                                Text(
                                                    text = "• ${LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = BrandBlueLight,
                                                    modifier = Modifier.clickable {
                                                        onAccountClick?.invoke(acc)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = LanguageHelper.formatCurrency(tx.amount, languageMode),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPink
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenBudgetMaker,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Set Budget", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onAddTransaction,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlueLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Add Entry", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Month and Year picker dialog for selecting active tracking period.
 */
@Composable
private fun MonthPickerDropdownDialog(
    currentYear: Int,
    currentMonth: Int,
    title: String? = null,
    onDismiss: () -> Unit,
    onSelect: (Int, Int) -> Unit
) {
    var year by remember { mutableIntStateOf(currentYear) }
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueLight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { year-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Year")
                    }
                    Text(
                        text = "$year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { year++ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3x4 Month Grid
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until 3) {
                            val monthIdx = row * 3 + col
                            val monthNum = monthIdx + 1
                            val isSelected = year == currentYear && monthNum == currentMonth

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrandBlueLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelect(year, monthNum) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = months[monthIdx].take(3),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

