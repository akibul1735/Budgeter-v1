package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidIncome
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar
import kotlin.math.roundToInt

private val CrimsonPink = Color(0xFFE91E63)
private val AlertRed = Color(0xFFF43F5E)
private val BrandBlueLight = Color(0xFF0284C7)
private val SoftCyan = Color(0xFF38BDF8)
private val SlateText = Color(0xFF64748B)

enum class BudgetComparisonPreset(val titleEn: String, val titleBn: String) {
    LAST_MONTH("Current vs Last Month", "চলতি বনাম গত মাস"),
    SAME_MONTH_LAST_YEAR("Current vs Same Month Last Year", "চলতি বনাম গত বছরের একই মাস"),
    LAST_3_MONTHS("Current vs Last 3 Months", "চলতি বনাম গত ৩ মাস"),
    YEAR_TO_DATE("Year to Date", "বছরের শুরু থেকে আজ পর্যন্ত"),
    CUSTOM("Custom Period", "কাস্টম সময়কাল"),
    ONLY_CURRENT("This Month Only", "শুধুমাত্র চলতি মাস")
}

data class BudgetFilterState(
    val preset: BudgetComparisonPreset = BudgetComparisonPreset.LAST_MONTH,
    val customBaseDateMs: Long? = null,
    val customCompareDateMs: Long? = null,
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedStatusSet: Set<TransactionStatus> = emptySet(),
    val filterOnlyBudgeted: Boolean = false,
    val filterOnlyOverBudget: Boolean = false,
    val excludeZeroAmounts: Boolean = false,
    val displayCurrency: Boolean = true,
    val showOnlyCurrentBalance: Boolean = false
) {
    val isFilterActive: Boolean
        get() = selectedCategoryIds.isNotEmpty() ||
                selectedAccountIds.isNotEmpty() ||
                selectedStatusSet.isNotEmpty() ||
                filterOnlyBudgeted ||
                filterOnlyOverBudget ||
                excludeZeroAmounts ||
                preset != BudgetComparisonPreset.LAST_MONTH ||
                customBaseDateMs != null ||
                customCompareDateMs != null
}

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
    // Filter State
    var filterState by remember { mutableStateOf(BudgetFilterState()) }
    var showTimelineScreen by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showBaseDatePicker by remember { mutableStateOf(false) }
    var showCompareDatePicker by remember { mutableStateOf(false) }
    var isDualDateFlow by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategoryForDetail by remember { mutableStateOf<CategoryBudgetTrackingItem?>(null) }
    var activeTabMode by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"

    // Group expanded state map (default true: expanded)
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

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

    // Determine Base Date Range and Compare Date Range based on Preset
    val (baseRange, compareRange, baseDateLabel, compareDateLabel) = remember(
        selectedYear,
        selectedMonth,
        filterState.preset,
        filterState.customBaseDateMs,
        filterState.customCompareDateMs,
        languageMode
    ) {
        calculateBudgetComparisonRanges(
            year = selectedYear,
            month = selectedMonth,
            preset = filterState.preset,
            customBaseMs = filterState.customBaseDateMs,
            customCompareMs = filterState.customCompareDateMs,
            languageMode = languageMode
        )
    }

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
            val matchesZero = !filterState.excludeZeroAmounts || tx.amount > 0.0
            inTime && matchesAcc && matchesStatus && matchesCat && matchesZero
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
                val matchesZero = !filterState.excludeZeroAmounts || tx.amount > 0.0
                inTime && matchesAcc && matchesStatus && matchesCat && matchesZero
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
                    matchesSearch && matchesBudgeted && matchesOver && matchesCatFilter
                }

                if (trackingItems.isNotEmpty() || (searchQuery.isEmpty() && !filterState.filterOnlyBudgeted && !filterState.filterOnlyOverBudget && filterState.selectedCategoryIds.isEmpty())) {
                    resultList.add(
                        CategoryGroupBudgetTracking(
                            parentCategory = parent,
                            groupNameEn = parent.nameEn,
                            groupNameBn = parent.nameBn,
                            items = trackingItems
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

                if (matchesSearch && matchesBudgeted && matchesOver && matchesCatFilter) {
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
                matchesSearch && matchesBudgeted && matchesOver && matchesCatFilter
            }

            if (orphanItems.isNotEmpty()) {
                resultList.add(
                    CategoryGroupBudgetTracking(
                        parentCategory = null,
                        groupNameEn = "Others",
                        groupNameBn = "অন্যান্য",
                        items = orphanItems
                    )
                )
            }
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

            // 2. FIXED COMPARISON DATES CARD (Like Balance Sheet)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
            ) {
                BudgetComparisonDatesCard(
                    preset = filterState.preset,
                    baseDateLabel = baseDateLabel,
                    compareDateLabel = compareDateLabel,
                    showOnlyCurrentBalance = filterState.showOnlyCurrentBalance,
                    languageMode = languageMode,
                    onOpenFilter = { showFilterDialog = true },
                    onPickBaseDate = { showBaseDatePicker = true },
                    onPickCompareDate = { showCompareDatePicker = true },
                    onPickBothDates = {
                        isDualDateFlow = true
                        showBaseDatePicker = true
                    },
                    onPrevMonth = { viewModel.prevBudgetMonth() },
                    onNextMonth = { viewModel.nextBudgetMonth() }
                )
            }

            // 3. FIXED TAB ROW (Expenses vs Incomes)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                TabRow(
                    selectedTabIndex = if (activeTabMode == "EXPENSE") 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = BrandBlueLight,
                    indicator = { tabPositions ->
                        val tabIdx = if (activeTabMode == "EXPENSE") 0 else 1
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                            color = BrandBlueLight,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = activeTabMode == "EXPENSE",
                        onClick = { activeTabMode = "EXPENSE" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (activeTabMode == "EXPENSE") CrimsonPink else SlateText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ব্যয় (Expenses)" else "Expenses",
                                    fontWeight = if (activeTabMode == "EXPENSE") FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (activeTabMode == "EXPENSE") MaterialTheme.colorScheme.onSurface else SlateText
                                )
                            }
                        }
                    )
                    Tab(
                        selected = activeTabMode == "INCOME",
                        onClick = { activeTabMode = "INCOME" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = if (activeTabMode == "INCOME") SolidIncome else SlateText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "আয় (Incomes)" else "Incomes",
                                    fontWeight = if (activeTabMode == "INCOME") FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (activeTabMode == "INCOME") MaterialTheme.colorScheme.onSurface else SlateText
                                )
                            }
                        }
                    )
                }
            }

            // 4. FIXED SINGLE-ROW SUMMARY CARD (Budgeted | Expensed | Remaining/Over + Progress Bar)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Single Row Summary (3 Columns with Vertical Dividers)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Total Budgeted (Left)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বাজেট লক্ষ্য" else "Budgeted",
                                fontSize = 11.sp,
                                color = BrandBlueLight,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "BDT ${LanguageHelper.formatNumber(totalFlowBudget, languageMode, false)}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (totalFlowBudget > 0) "$overallPercentage% planned" else "No budget",
                                fontSize = 9.sp,
                                color = SlateText,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        // Divider 1
                        VerticalDivider(
                            modifier = Modifier
                                .height(36.dp)
                                .padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // 2. Total Expensed / Actual (Center)
                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (activeTabMode == "EXPENSE") {
                                    if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Spent"
                                } else {
                                    if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Earned"
                                },
                                fontSize = 11.sp,
                                color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "BDT ${LanguageHelper.formatNumber(totalFlowSpent, languageMode)}",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Comparison Delta Badge
                            if (!filterState.showOnlyCurrentBalance && baseRange != null) {
                                Spacer(modifier = Modifier.height(1.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val isIncrease = overallDeltaSpent > 0
                                    val isExpense = activeTabMode == "EXPENSE"
                                    val badgeColor = if (isExpense) {
                                        if (isIncrease) AlertRed else SolidIncome
                                    } else {
                                        if (isIncrease) SolidIncome else AlertRed
                                    }
                                    Icon(
                                        imageVector = if (isIncrease) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    val sign = if (isIncrease) "+" else ""
                                    Text(
                                        text = "$sign${LanguageHelper.formatNumber(overallDeltaPercent, languageMode)}% vs base",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor
                                    )
                                }
                            }
                        }

                        // Divider 2
                        VerticalDivider(
                            modifier = Modifier
                                .height(36.dp)
                                .padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // 3. Remaining / Over (Right)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (totalFlowBudget > 0) {
                                    if (isOverallOver) {
                                        if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত ব্যয়" else "Over Budget"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট বাজেট" else "Remaining"
                                    }
                                } else "Difference",
                                fontSize = 11.sp,
                                color = if (isOverallOver) CrimsonPink else SolidIncome,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (totalFlowBudget > 0) {
                                    "BDT ${LanguageHelper.formatNumber(overallDiff, languageMode)}"
                                } else "-",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverallOver) CrimsonPink else SolidIncome,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (isOverallOver) "Over limit" else "Under limit",
                                fontSize = 9.sp,
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 5. SCROLLABLE CATEGORY GROUPS & ITEMS (LazyColumn)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 90.dp)
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
                    items(
                        items = categoryGroups,
                        key = { "${it.parentCategory?.id ?: "others"}_${it.groupNameEn}_${it.groupNameBn}" }
                    ) { group ->
                        val groupKey = "${group.parentCategory?.id ?: "others"}_${group.groupNameEn}_${group.groupNameBn}"
                        val isExpanded = expandedGroups[groupKey] ?: true

                        CategoryGroupSection(
                            group = group,
                            isExpanded = isExpanded,
                            showComparison = !filterState.showOnlyCurrentBalance && baseRange != null,
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

        // 6. FLOATING ACTION BUTTONS (Floating Shopping Bag / Budget Maker & Quick Add +)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Floating Budget Maker Shortcut Button
            Surface(
                shape = CircleShape,
                color = BrandBlueLight,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToBudgetMaker() }
                    .testTag("budget_maker_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Budget Maker",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Main Add Transaction FAB
            FloatingActionButton(
                onClick = {
                    val defaultCat = allCategories.firstOrNull { it.type == targetCatType && it.parentId != null }
                    if (defaultCat != null) onAddTransactionWithCategory(defaultCat)
                    else onNavigateToBudgetMaker()
                },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("budget_add_tx_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
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

    // MONTH PICKER DIALOG
    if (showMonthPicker) {
        MonthPickerDropdownDialog(
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onSelect = { y, m ->
                viewModel.setBudgetYearMonth(y, m)
                showMonthPicker = false
            }
        )
    }

    // BASE DATE PICKER
    if (showBaseDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = filterState.customBaseDateMs ?: (baseRange?.first ?: System.currentTimeMillis())
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
                                preset = BudgetComparisonPreset.CUSTOM,
                                customBaseDateMs = selected
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

    // COMPARE DATE PICKER
    if (showCompareDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = filterState.customCompareDateMs ?: compareRange.first
        )
        DatePickerDialog(
            onDismissRequest = { showCompareDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            filterState = filterState.copy(
                                preset = BudgetComparisonPreset.CUSTOM,
                                customCompareDateMs = selected
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
 * Helper to calculate start & end date milliseconds and formatted labels for comparison ranges.
 */
private fun calculateBudgetComparisonRanges(
    year: Int,
    month: Int,
    preset: BudgetComparisonPreset,
    customBaseMs: Long?,
    customCompareMs: Long?,
    languageMode: LanguageMode
): ComparisonRangeResult {
    val compareStart = DateUtils.getStartOfMonth(year, month)
    val compareEnd = DateUtils.getEndOfMonth(year, month)
    val compareLabel = DateUtils.formatMonthYear(year, month, languageMode)

    return when (preset) {
        BudgetComparisonPreset.LAST_MONTH -> {
            val prevMonth = if (month == 1) 12 else month - 1
            val prevYear = if (month == 1) year - 1 else year
            val baseStart = DateUtils.getStartOfMonth(prevYear, prevMonth)
            val baseEnd = DateUtils.getEndOfMonth(prevYear, prevMonth)
            val baseLabel = DateUtils.formatMonthYear(prevYear, prevMonth, languageMode)
            ComparisonRangeResult(
                baseRange = Pair(baseStart, baseEnd),
                compareRange = Pair(compareStart, compareEnd),
                baseLabel = baseLabel,
                compareLabel = compareLabel
            )
        }
        BudgetComparisonPreset.SAME_MONTH_LAST_YEAR -> {
            val prevYear = year - 1
            val baseStart = DateUtils.getStartOfMonth(prevYear, month)
            val baseEnd = DateUtils.getEndOfMonth(prevYear, month)
            val baseLabel = DateUtils.formatMonthYear(prevYear, month, languageMode)
            ComparisonRangeResult(
                baseRange = Pair(baseStart, baseEnd),
                compareRange = Pair(compareStart, compareEnd),
                baseLabel = baseLabel,
                compareLabel = compareLabel
            )
        }
        BudgetComparisonPreset.LAST_3_MONTHS -> {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -3)
            }
            val baseStart = cal.timeInMillis
            val calEnd = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.DAY_OF_MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            val baseEnd = calEnd.timeInMillis
            ComparisonRangeResult(
                baseRange = Pair(baseStart, baseEnd),
                compareRange = Pair(compareStart, compareEnd),
                baseLabel = if (languageMode == LanguageMode.BANGLA) "গত ৩ মাস" else "Last 3 Mos",
                compareLabel = compareLabel
            )
        }
        BudgetComparisonPreset.YEAR_TO_DATE -> {
            val baseStart = DateUtils.getStartOfMonth(year, 1)
            val baseEnd = compareEnd
            ComparisonRangeResult(
                baseRange = Pair(baseStart, baseEnd),
                compareRange = Pair(compareStart, compareEnd),
                baseLabel = if (languageMode == LanguageMode.BANGLA) "চলতি বছর (YTD)" else "Year To Date",
                compareLabel = compareLabel
            )
        }
        BudgetComparisonPreset.CUSTOM -> {
            val bDateMs = customBaseMs ?: (compareStart - 30L * 86400000L)
            val cDateMs = customCompareMs ?: compareEnd
            val bCal = Calendar.getInstance().apply { timeInMillis = bDateMs }
            val cCal = Calendar.getInstance().apply { timeInMillis = cDateMs }
            val bLabel = "${bCal.get(Calendar.MONTH) + 1}/${bCal.get(Calendar.DAY_OF_MONTH)}/${bCal.get(Calendar.YEAR) % 100}"
            val cLabel = "${cCal.get(Calendar.MONTH) + 1}/${cCal.get(Calendar.DAY_OF_MONTH)}/${cCal.get(Calendar.YEAR) % 100}"
            ComparisonRangeResult(
                baseRange = Pair(bDateMs - 86400000L * 15, bDateMs),
                compareRange = Pair(bDateMs, cDateMs),
                baseLabel = bLabel,
                compareLabel = cLabel
            )
        }
        BudgetComparisonPreset.ONLY_CURRENT -> {
            ComparisonRangeResult(
                baseRange = null,
                compareRange = Pair(compareStart, compareEnd),
                baseLabel = "-",
                compareLabel = compareLabel
            )
        }
    }
}

private data class ComparisonRangeResult(
    val baseRange: Pair<Long, Long>?,
    val compareRange: Pair<Long, Long>,
    val baseLabel: String,
    val compareLabel: String
)

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
            if (showOnlyCurrentBalance || preset == BudgetComparisonPreset.ONLY_CURRENT) {
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
            .padding(vertical = 4.dp)
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
                    // Left: Blue Circle Chevron + Arrow Symbol + Group Name
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
                    }

                    // Right: Group Total Spent Amount + Delta
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BDT ${LanguageHelper.formatNumber(group.totalSpent, languageMode)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (group.totalSpent > 0) CrimsonPink else SlateText
                        )
                        if (showComparison && group.totalSpentBase > 0.0) {
                            val isIncrease = group.deltaSpent > 0
                            val sign = if (isIncrease) "+" else ""
                            Text(
                                text = "Base: BDT ${LanguageHelper.formatNumber(group.totalSpentBase, languageMode, false)} ($sign${group.deltaPercent.roundToInt()}%)",
                                fontSize = 9.sp,
                                color = SlateText
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
                            "BDT ${LanguageHelper.formatNumber(group.diffAmount, languageMode)} over ${LanguageHelper.formatNumber(group.totalBudget, languageMode, false)}"
                        } else {
                            "BDT ${LanguageHelper.formatNumber(group.diffAmount, languageMode)} left from ${LanguageHelper.formatNumber(group.totalBudget, languageMode, false)}"
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

        // Collapsible Children Rows
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                group.items.forEach { item ->
                    CategoryRow(
                        item = item,
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
 */
@Composable
private fun CategoryRow(
    item: CategoryBudgetTrackingItem,
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

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Line: Category Icon + Name + Spent Amount
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
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = IconHelper.getIconByName(item.category.iconName),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Category Name
                    Text(
                        text = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Spent Amount & Delta
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BDT ${LanguageHelper.formatNumber(item.spentAmount, languageMode)}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.spentAmount > 0) CrimsonPink else MaterialTheme.colorScheme.onSurface
                    )
                    if (showComparison && item.spentBaseAmount > 0.0) {
                        val isIncrease = item.deltaSpent > 0
                        val sign = if (isIncrease) "+" else ""
                        Text(
                            text = "Prev: ${LanguageHelper.formatNumber(item.spentBaseAmount, languageMode, false)} ($sign${item.deltaPercent.roundToInt()}%)",
                            fontSize = 8.5.sp,
                            color = SlateText
                        )
                    }
                }
            }

            // Budget Sub-row (if category has an active budget limit)
            if (item.hasBudget) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.percentageInt}%",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val diffText = if (item.isOverBudget) {
                        "BDT ${LanguageHelper.formatNumber(item.diffAmount, languageMode)} over ${LanguageHelper.formatNumber(item.budgetLimit, languageMode, false)}"
                    } else {
                        "BDT ${LanguageHelper.formatNumber(item.diffAmount, languageMode)} left from ${LanguageHelper.formatNumber(item.budgetLimit, languageMode, false)}"
                    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total Spent: BDT ${LanguageHelper.formatNumber(item.spentAmount, languageMode)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrimsonPink
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                                text = "Budget: BDT ${LanguageHelper.formatNumber(item.budgetLimit, languageMode)}",
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

/**
 * Budget Filter Dialog with Comparison Presets, Account and Category multi-select,
 * status filtering, and display options.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetFilterDialog(
    currentFilter: BudgetFilterState,
    categories: List<Category>,
    accounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (BudgetFilterState) -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = BrandBlueLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বাজেট ফিল্টার ও তুলনা" else "Budget Filter & Comparison",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Section 1: Comparison Preset
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক সময়কাল প্রিসেট" else "Comparison Preset",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BudgetComparisonPreset.values().forEach { preset ->
                            val isSelected = tempFilter.preset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    tempFilter = tempFilter.copy(
                                        preset = preset,
                                        showOnlyCurrentBalance = preset == BudgetComparisonPreset.ONLY_CURRENT
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) preset.titleBn else preset.titleEn,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlueLight,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section 2: Display & Comparison Toggles
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "প্রদর্শন অপশন" else "Display Options",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Only Budgeted Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বাজেট নির্ধারণ করা ক্যাটাগরি" else "Only Budgeted Categories",
                            fontSize = 12.5.sp
                        )
                        Switch(
                            checked = tempFilter.filterOnlyBudgeted,
                            onCheckedChange = { tempFilter = tempFilter.copy(filterOnlyBudgeted = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandBlueLight)
                        )
                    }

                    // Only Over Budget Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র ওভার বাজেট ক্যাটাগরি" else "Only Over Budget Categories",
                            fontSize = 12.5.sp
                        )
                        Switch(
                            checked = tempFilter.filterOnlyOverBudget,
                            onCheckedChange = { tempFilter = tempFilter.copy(filterOnlyOverBudget = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AlertRed)
                        )
                    }

                    // Exclude Zero Amounts Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "শূন্য পরিমাণ বাদ দিন" else "Exclude Zero Amounts",
                            fontSize = 12.5.sp
                        )
                        Switch(
                            checked = tempFilter.excludeZeroAmounts,
                            onCheckedChange = { tempFilter = tempFilter.copy(excludeZeroAmounts = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandBlueLight)
                        )
                    }

                    // Show Only Current Month (Hide Baseline comparison)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বর্তমান মাস দেখান (তুলনা বন্ধ)" else "Show Only Current Period (No Comparison)",
                            fontSize = 12.5.sp
                        )
                        Switch(
                            checked = tempFilter.showOnlyCurrentBalance,
                            onCheckedChange = { tempFilter = tempFilter.copy(showOnlyCurrentBalance = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandBlueLight)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section 3: Filter by Category (Multi-select)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি নির্বাচন" else "Filter by Categories",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueLight
                        )
                        if (tempFilter.selectedCategoryIds.isNotEmpty()) {
                            TextButton(onClick = { tempFilter = tempFilter.copy(selectedCategoryIds = emptySet()) }) {
                                Text("Clear", fontSize = 11.5.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.filter { it.isActive }.take(12).forEach { cat ->
                            val isSelected = tempFilter.selectedCategoryIds.contains(cat.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val current = tempFilter.selectedCategoryIds.toMutableSet()
                                    if (isSelected) current.remove(cat.id) else current.add(cat.id)
                                    tempFilter = tempFilter.copy(selectedCategoryIds = current)
                                },
                                label = {
                                    Text(
                                        text = LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode),
                                        fontSize = 11.5.sp
                                    )
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Bottom Action Buttons: Reset & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempFilter = BudgetFilterState()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (languageMode == LanguageMode.BANGLA) "রিসেট" else "Reset", fontSize = 12.5.sp)
                    }

                    Button(
                        onClick = { onApply(tempFilter) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlueLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply", fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}
