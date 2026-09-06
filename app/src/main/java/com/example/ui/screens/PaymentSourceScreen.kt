package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.AccountRequirementAnalysis
import com.example.data.model.AccountRequirementItem
import com.example.data.model.Category
import com.example.data.model.CategoryAllocationAnalysis
import com.example.data.model.CategoryType
import com.example.data.model.FundAllocationSuggestion
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentSourceAnalysisOverview
import com.example.data.model.RecurringBill
import com.example.data.model.RequirementCalculationBasis
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.PaymentSourceCalculator
import java.util.Calendar

private enum class PaymentSourceTab {
    ACCOUNTS,
    CATEGORIES
}

private enum class AccountFilter {
    ALL,
    SHORTFALL_ONLY,
    SURPLUS_ONLY
}

@Composable
fun PaymentSourceScreen(
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    allCategories: List<Category>,
    monthlyBudgets: List<MonthlyBudget>,
    allTransactions: List<TransactionWithDetails>,
    recurringBills: List<RecurringBill>,
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSetCurrentMonth: () -> Unit,
    onExecuteTransfer: (fromAccount: Account, toAccount: Account, amount: Double) -> Unit,
    onAddTransactionWithAccount: (Account, TransactionType) -> Unit,
    onEditAccount: (Account) -> Unit,
    onSaveCategoryAllocations: ((categoryId: Long, allocations: Map<Long, Double>) -> Unit)? = null,
    onAccountClick: ((Account) -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(PaymentSourceTab.ACCOUNTS) }
    var calculationBasis by remember { mutableStateOf(RequirementCalculationBasis.BUDGET_AMOUNT) }
    var accountFilter by remember { mutableStateOf(AccountFilter.ALL) }
    var expandedAccountIds by remember { mutableStateOf(setOf<Long>()) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var onlyMultiAccountCategories by remember { mutableStateOf(false) }

    // Dialog States
    var editingCategoryForSplit by remember { mutableStateOf<Category?>(null) }
    var assigningAccountForCategory by remember { mutableStateOf<Account?>(null) }

    val analysisOverview = remember(
        selectedYear,
        selectedMonth,
        calculationBasis,
        allAccounts,
        accountsWithBalances,
        allCategories,
        monthlyBudgets,
        allTransactions,
        recurringBills
    ) {
        PaymentSourceCalculator.calculateAnalysis(
            year = selectedYear,
            month = selectedMonth,
            basis = calculationBasis,
            allAccounts = allAccounts,
            accountsWithBalances = accountsWithBalances,
            allCategories = allCategories,
            monthlyBudgets = monthlyBudgets,
            allTransactions = allTransactions,
            recurringBills = recurringBills
        )
    }

    // Budgeted and Remaining Amounts across all categories/requirements
    val totalBudgetedAmount = remember(analysisOverview.categoryAllocations, analysisOverview.totalRequired) {
        val catBudgetSum = analysisOverview.categoryAllocations.sumOf { it.totalBudgetOrRequired }
        if (catBudgetSum > 0) catBudgetSum else analysisOverview.totalRequired
    }

    val totalSpentAmount = remember(analysisOverview.categoryAllocations) {
        analysisOverview.categoryAllocations.sumOf { it.totalActualSpent }
    }

    val totalRemainingAmount = remember(analysisOverview.categoryAllocations, totalBudgetedAmount, totalSpentAmount) {
        val catRemainingSum = analysisOverview.categoryAllocations.sumOf { it.totalRemaining }
        if (catRemainingSum > 0) catRemainingSum else maxOf(0.0, totalBudgetedAmount - totalSpentAmount)
    }

    val filteredAnalyses = remember(analysisOverview.accountAnalyses, accountFilter, searchQuery) {
        analysisOverview.accountAnalyses.filter { analysis ->
            val matchesFilter = when (accountFilter) {
                AccountFilter.ALL -> true
                AccountFilter.SHORTFALL_ONLY -> analysis.isShortfall
                AccountFilter.SURPLUS_ONLY -> analysis.isSurplus
            }
            val matchesSearch = searchQuery.isBlank() ||
                    analysis.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                    analysis.account.nameBn.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val filteredCategoryAllocations = remember(analysisOverview.categoryAllocations, searchQuery, onlyMultiAccountCategories) {
        analysisOverview.categoryAllocations.filter { catAlloc ->
            val matchesMulti = !onlyMultiAccountCategories || catAlloc.isMultiAccount
            val matchesSearch = searchQuery.isBlank() ||
                    catAlloc.category.nameEn.contains(searchQuery, ignoreCase = true) ||
                    catAlloc.category.nameBn.contains(searchQuery, ignoreCase = true) ||
                    catAlloc.accountSplits.any {
                        it.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                                it.account.nameBn.contains(searchQuery, ignoreCase = true)
                    }
            matchesMulti && matchesSearch
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (onSaveCategoryAllocations != null) {
                FloatingActionButton(
                    onClick = {
                        val firstExp = allCategories.firstOrNull { it.type == CategoryType.EXPENSE && it.parentId != null }
                            ?: allCategories.firstOrNull { it.type == CategoryType.EXPENSE }
                        editingCategoryForSplit = firstExp
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("split_budget_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageHelper.getString("assign_category", languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen Header
            AppTabHeader(
                title = LanguageHelper.getString("payment_source", languageMode),
                onOpenDrawer = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Compact Top Control & Dual Tab Card with Budgeted & Remaining Amount
                item {
                    CompactPaymentSourceHeaderCard(
                        year = selectedYear,
                        month = selectedMonth,
                        selectedTab = selectedTab,
                        calculationBasis = calculationBasis,
                        budgetedAmount = totalBudgetedAmount,
                        remainingAmount = totalRemainingAmount,
                        totalAvailable = analysisOverview.totalAvailable,
                        totalShortfall = analysisOverview.totalShortfall,
                        totalSurplus = analysisOverview.totalSurplus,
                        accountsNeedingFundsCount = analysisOverview.accountsNeedingFundsCount,
                        accountsCount = analysisOverview.accountAnalyses.size,
                        categoriesCount = analysisOverview.categoryAllocations.size,
                        languageMode = languageMode,
                        onPrevMonth = onPrevMonth,
                        onNextMonth = onNextMonth,
                        onSetCurrentMonth = onSetCurrentMonth,
                        onSelectTab = { selectedTab = it },
                        onToggleBasis = {
                            calculationBasis = if (calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                                RequirementCalculationBasis.REMAINING_AMOUNT
                            } else {
                                RequirementCalculationBasis.BUDGET_AMOUNT
                            }
                        },
                        onHelp = { showHelpDialog = true }
                    )
                }

                // 2. Compact Fund Transfer Recommendation (If Shortfall Exists)
                if (analysisOverview.transferSuggestions.isNotEmpty()) {
                    item {
                        CompactTransferSuggestionBanner(
                            suggestions = analysisOverview.transferSuggestions,
                            languageMode = languageMode,
                            onExecuteTransfer = onExecuteTransfer
                        )
                    }
                }

                // 3. Compact Search & Filter Toolbar
                item {
                    CompactSearchAndFilterToolbar(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedTab = selectedTab,
                        accountFilter = accountFilter,
                        onAccountFilterChange = { accountFilter = it },
                        onlyMultiAccount = onlyMultiAccountCategories,
                        onToggleMultiAccount = { onlyMultiAccountCategories = !onlyMultiAccountCategories },
                        shortfallCount = analysisOverview.accountsNeedingFundsCount,
                        surplusCount = analysisOverview.accountsWithSurplusCount,
                        languageMode = languageMode
                    )
                }

                // 4. Content Section based on selected tab
                if (selectedTab == PaymentSourceTab.ACCOUNTS) {
                    // Accounts Tab Content
                    if (filteredAnalyses.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = LanguageHelper.getString("no_accounts_match", languageMode),
                                icon = Icons.Default.FilterList
                            )
                        }
                    } else {
                        items(filteredAnalyses, key = { it.account.id }) { analysis ->
                            val isExpanded = expandedAccountIds.contains(analysis.account.id)
                            CompactAccountRequirementCard(
                                analysis = analysis,
                                isExpanded = isExpanded,
                                languageMode = languageMode,
                                onToggleExpand = {
                                    expandedAccountIds = if (isExpanded) {
                                        expandedAccountIds - analysis.account.id
                                    } else {
                                        expandedAccountIds + analysis.account.id
                                    }
                                },
                                onFundAccount = {
                                    val bestSurplus = analysisOverview.accountAnalyses
                                        .filter { it.isSurplus && it.account.id != analysis.account.id }
                                        .maxByOrNull { it.surplus }
                                    if (bestSurplus != null) {
                                        val amountToMove = minOf(analysis.shortfall, bestSurplus.surplus)
                                        onExecuteTransfer(bestSurplus.account, analysis.account, amountToMove)
                                    } else {
                                        onAddTransactionWithAccount(analysis.account, TransactionType.INCOME)
                                    }
                                },
                                onAssignExpense = {
                                    assigningAccountForCategory = analysis.account
                                },
                                onAddExpense = { onAddTransactionWithAccount(analysis.account, TransactionType.EXPENSE) },
                                onAddIncome = { onAddTransactionWithAccount(analysis.account, TransactionType.INCOME) },
                                onAccountClick = {
                                    if (onAccountClick != null) onAccountClick(analysis.account) else onEditAccount(analysis.account)
                                }
                            )
                        }
                    }
                } else {
                    // Categories Tab Content
                    if (filteredCategoryAllocations.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = LanguageHelper.getString("no_categories_match", languageMode),
                                icon = Icons.Default.Category
                            )
                        }
                    } else {
                        items(filteredCategoryAllocations, key = { it.category.id }) { catAlloc ->
                            CompactCategoryAllocationCard(
                                categoryAlloc = catAlloc,
                                languageMode = languageMode,
                                onEditSplit = {
                                    editingCategoryForSplit = catAlloc.category
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        PaymentSourceHelpDialog(
            languageMode = languageMode,
            onDismiss = { showHelpDialog = false }
        )
    }

    // Category Account Allocation / Split Dialog
    if (editingCategoryForSplit != null && onSaveCategoryAllocations != null) {
        CategoryAccountAllocationDialog(
            category = editingCategoryForSplit!!,
            allExpenseCategories = allCategories.filter { it.type == CategoryType.EXPENSE && it.parentId != null }
                .ifEmpty { allCategories.filter { it.type == CategoryType.EXPENSE } },
            allAccounts = allAccounts.filter { it.parentId != null && it.isActive }.ifEmpty { allAccounts.filter { it.isActive } },
            accountsWithBalances = accountsWithBalances,
            currentAllocations = monthlyBudgets.filter {
                it.itemType == "ALLOC_${editingCategoryForSplit!!.id}" && it.isEnabled
            }.associate { it.itemId to it.budgetedAmount },
            languageMode = languageMode,
            onDismiss = { editingCategoryForSplit = null },
            onSelectCategory = { newCat -> editingCategoryForSplit = newCat },
            onSave = { catId, allocMap ->
                onSaveCategoryAllocations(catId, allocMap)
                editingCategoryForSplit = null
            }
        )
    }

    // Quick Assign to Account Dialog
    if (assigningAccountForCategory != null && onSaveCategoryAllocations != null) {
        QuickAssignToAccountDialog(
            account = assigningAccountForCategory!!,
            allExpenseCategories = allCategories.filter { it.type == CategoryType.EXPENSE && it.parentId != null }
                .ifEmpty { allCategories.filter { it.type == CategoryType.EXPENSE } },
            languageMode = languageMode,
            onDismiss = { assigningAccountForCategory = null },
            onSave = { categoryId, amount ->
                val currentForCat = monthlyBudgets.filter {
                    it.itemType == "ALLOC_$categoryId" && it.isEnabled
                }.associate { it.itemId to it.budgetedAmount }.toMutableMap()

                currentForCat[assigningAccountForCategory!!.id] = amount
                onSaveCategoryAllocations(categoryId, currentForCat)
                assigningAccountForCategory = null
            }
        )
    }
}

/**
 * Compact Top Header Card:
 * 1. Month Navigation + Basis Toggle + Help
 * 2. Divided into Two Tabs: Account and Categories
 * 3. In Between: Budgeted Amount and Remaining Amount dual metrics with Available / Status bar
 */
@Composable
private fun CompactPaymentSourceHeaderCard(
    year: Int,
    month: Int,
    selectedTab: PaymentSourceTab,
    calculationBasis: RequirementCalculationBasis,
    budgetedAmount: Double,
    remainingAmount: Double,
    totalAvailable: Double,
    totalShortfall: Double,
    totalSurplus: Double,
    accountsNeedingFundsCount: Int,
    accountsCount: Int,
    categoriesCount: Int,
    languageMode: LanguageMode,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSetCurrentMonth: () -> Unit,
    onSelectTab: (PaymentSourceTab) -> Unit,
    onToggleBasis: () -> Unit,
    onHelp: () -> Unit
) {
    val monthName = DateUtils.getMonthName(month, languageMode)
    val yearStr = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(year.toString()) else year.toString()

    val cal = Calendar.getInstance()
    val isCurrentMonth = year == cal.get(Calendar.YEAR) && month == (cal.get(Calendar.MONTH) + 1)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Row 1: Month Navigation & Quick Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Month Arrows & Month Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onSetCurrentMonth() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$monthName $yearStr",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (!isCurrentMonth) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { onSetCurrentMonth() }
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "চলতি" else "Today",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Right Actions: Basis Toggle & Help
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { onToggleBasis() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                                    if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Full Budget"
                                } else {
                                    if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onHelp,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Top Division into Two Tabs: Accounts and Categories
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabIdx = if (selectedTab == PaymentSourceTab.ACCOUNTS) 0 else 1
                TabRow(
                    selectedTabIndex = tabIdx,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (tabIdx in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 2.5.dp
                            )
                        }
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == PaymentSourceTab.ACCOUNTS,
                        onClick = { onSelectTab(PaymentSourceTab.ACCOUNTS) },
                        modifier = Modifier.height(38.dp),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (selectedTab == PaymentSourceTab.ACCOUNTS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${LanguageHelper.getString("accounts", languageMode)} ($accountsCount)",
                                    fontWeight = if (selectedTab == PaymentSourceTab.ACCOUNTS) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = if (selectedTab == PaymentSourceTab.ACCOUNTS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == PaymentSourceTab.CATEGORIES,
                        onClick = { onSelectTab(PaymentSourceTab.CATEGORIES) },
                        modifier = Modifier.height(38.dp),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (selectedTab == PaymentSourceTab.CATEGORIES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${LanguageHelper.getString("categories", languageMode)} ($categoriesCount)",
                                    fontWeight = if (selectedTab == PaymentSourceTab.CATEGORIES) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = if (selectedTab == PaymentSourceTab.CATEGORIES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: In between these two -> Budgeted Amount and Remaining Amount dual summary
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    // Dual Metrics: Budgeted Amount vs Remaining Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Budgeted Amount
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SolidExpense)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = LanguageHelper.getString("budgeted_amount", languageMode),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(budgetedAmount, languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SolidExpense,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Divider
                        VerticalDivider(
                            modifier = Modifier
                                .height(32.dp)
                                .padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Right: Remaining Amount
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SolidTransfer)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = LanguageHelper.getString("remaining_amount", languageMode),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(remainingAmount, languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (remainingAmount > 0) SolidTransfer else SolidIncome,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Bottom Companion Metrics: Available Balance & Overall Surplus / Shortfall
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${LanguageHelper.getString("available_amount", languageMode)}: ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(totalAvailable, languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Status Badge
                        if (accountsNeedingFundsCount == 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SolidIncome,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${LanguageHelper.getString("surplus", languageMode)}: ${LanguageHelper.formatCurrency(totalSurplus, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidIncome
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SolidExpense,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${LanguageHelper.getString("shortfall", languageMode)}: ${LanguageHelper.formatCurrency(totalShortfall, languageMode)} (${accountsNeedingFundsCount})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Transfer Suggestion Banner
 */
@Composable
private fun CompactTransferSuggestionBanner(
    suggestions: List<FundAllocationSuggestion>,
    languageMode: LanguageMode,
    onExecuteTransfer: (fromAccount: Account, toAccount: Account, amount: Double) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SolidTransfer.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, SolidTransfer.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = SolidTransfer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) {
                            "স্মার্ট তহবিল স্থানান্তর পরামর্শ (${suggestions.size})"
                        } else {
                            "Smart Transfer Insights (${suggestions.size})"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidTransfer
                    )
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = SolidTransfer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded || suggestions.size == 1,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = suggestion.fromAccount.localizedName(languageMode),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidIncome,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = SolidTransfer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = suggestion.toAccount.localizedName(languageMode),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidExpense,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Button(
                                    onClick = {
                                        onExecuteTransfer(
                                            suggestion.fromAccount,
                                            suggestion.toAccount,
                                            suggestion.transferAmount
                                        )
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${LanguageHelper.getString("move", languageMode)} ${LanguageHelper.formatCurrency(suggestion.transferAmount, languageMode)}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Search and Filter Toolbar
 */
@Composable
private fun CompactSearchAndFilterToolbar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedTab: PaymentSourceTab,
    accountFilter: AccountFilter,
    onAccountFilterChange: (AccountFilter) -> Unit,
    onlyMultiAccount: Boolean,
    onToggleMultiAccount: () -> Unit,
    shortfallCount: Int,
    surplusCount: Int,
    languageMode: LanguageMode
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            placeholder = {
                Text(
                    text = if (selectedTab == PaymentSourceTab.ACCOUNTS)
                        LanguageHelper.getString("search_accounts", languageMode)
                    else
                        LanguageHelper.getString("search_categories", languageMode),
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(15.dp))
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTab == PaymentSourceTab.ACCOUNTS) {
                FilterChip(
                    selected = accountFilter == AccountFilter.ALL,
                    onClick = { onAccountFilterChange(AccountFilter.ALL) },
                    modifier = Modifier.height(28.dp),
                    label = { Text(LanguageHelper.getString("filter_all", languageMode), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                FilterChip(
                    selected = accountFilter == AccountFilter.SHORTFALL_ONLY,
                    onClick = { onAccountFilterChange(AccountFilter.SHORTFALL_ONLY) },
                    modifier = Modifier.height(28.dp),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SolidExpense))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${LanguageHelper.getString("filter_shortfall", languageMode)} ($shortfallCount)",
                                fontSize = 11.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolidExpense.copy(alpha = 0.15f),
                        selectedLabelColor = SolidExpense
                    )
                )

                FilterChip(
                    selected = accountFilter == AccountFilter.SURPLUS_ONLY,
                    onClick = { onAccountFilterChange(AccountFilter.SURPLUS_ONLY) },
                    modifier = Modifier.height(28.dp),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SolidIncome))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${LanguageHelper.getString("filter_surplus", languageMode)} ($surplusCount)",
                                fontSize = 11.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolidIncome.copy(alpha = 0.15f),
                        selectedLabelColor = SolidIncome
                    )
                )
            } else {
                FilterChip(
                    selected = onlyMultiAccount,
                    onClick = onToggleMultiAccount,
                    modifier = Modifier.height(28.dp),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = LanguageHelper.getString("multi_account_split", languageMode),
                                fontSize = 11.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }
    }
}

/**
 * Compact Account Requirement Card (Under Accounts Tab)
 */
@Composable
private fun CompactAccountRequirementCard(
    analysis: AccountRequirementAnalysis,
    isExpanded: Boolean,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onFundAccount: () -> Unit,
    onAssignExpense: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAccountClick: () -> Unit
) {
    val accountColor = try {
        Color(android.graphics.Color.parseColor(analysis.account.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (analysis.isShortfall) SolidExpense.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Row: Account Icon, Name, Type Pill & Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAccountClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accountColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(analysis.account.iconName),
                            contentDescription = null,
                            tint = accountColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = analysis.account.localizedName(languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = analysis.account.type.name,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.currentBalance, languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (analysis.currentBalance >= 0) MaterialTheme.colorScheme.onSurface else SolidExpense
                    )
                    Text(
                        text = LanguageHelper.getString("current_balance", languageMode),
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar & Coverage Ratio
            val coverage = analysis.fundingCoverageRatio
            val progressColor = when {
                analysis.isShortfall -> SolidExpense
                analysis.isSurplus -> SolidIncome
                else -> MaterialTheme.colorScheme.primary
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${LanguageHelper.getString("required_amount", languageMode)}: ${LanguageHelper.formatCurrency(analysis.requiredExpenseAmount, languageMode)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = progressColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (analysis.isShortfall) {
                            "-${LanguageHelper.formatCurrency(analysis.shortfall, languageMode)}"
                        } else if (analysis.isSurplus) {
                            "+${LanguageHelper.formatCurrency(analysis.surplus, languageMode)}"
                        } else {
                            "100%"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { coverage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Action message & itemized toggle row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = analysis.getActionMessage(languageMode),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = progressColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (analysis.itemizedExpenses.isNotEmpty() || analysis.itemizedIncomes.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onToggleExpand() }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) LanguageHelper.getString("hide_breakdown", languageMode) else LanguageHelper.getString("expand_breakdown", languageMode),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Expandable Itemized Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (analysis.itemizedExpenses.isNotEmpty()) {
                        Text(
                            text = LanguageHelper.getString("itemized_expenses", languageMode),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidExpense
                        )
                        analysis.itemizedExpenses.forEach { expItem ->
                            CompactItemizedRow(item = expItem, languageMode = languageMode)
                        }
                    }

                    if (analysis.itemizedIncomes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = LanguageHelper.getString("itemized_incomes", languageMode),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidIncome
                        )
                        analysis.itemizedIncomes.forEach { incItem ->
                            CompactItemizedRow(item = incItem, languageMode = languageMode)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (analysis.isShortfall) {
                    Button(
                        onClick = onFundAccount,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(LanguageHelper.getString("move_funds", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onAssignExpense,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(LanguageHelper.getString("assign_category", languageMode), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onAddExpense,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(11.dp), tint = SolidExpense)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(LanguageHelper.getString("expense", languageMode), fontSize = 10.5.sp, color = SolidExpense)
                }

                OutlinedButton(
                    onClick = onAddIncome,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(11.dp), tint = SolidIncome)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(LanguageHelper.getString("income", languageMode), fontSize = 10.5.sp, color = SolidIncome)
                }
            }
        }
    }
}

@Composable
private fun CompactItemizedRow(item: AccountRequirementItem, languageMode: LanguageMode) {
    val itemColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (_: Exception) {
        if (item.isExpense) SolidExpense else SolidIncome
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(itemColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconHelper.getIconByName(item.iconName),
                    contentDescription = null,
                    tint = itemColor,
                    modifier = Modifier.size(11.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isMultiAccountSplit) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${item.splitAccountCount} accs)",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Text(
            text = LanguageHelper.formatCurrency(item.amount, languageMode),
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = if (item.isExpense) SolidExpense else SolidIncome
        )
    }
}

/**
 * Compact Category Allocation Card (Under Categories Tab)
 */
@Composable
private fun CompactCategoryAllocationCard(
    categoryAlloc: CategoryAllocationAnalysis,
    languageMode: LanguageMode,
    onEditSplit: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(categoryAlloc.category.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Row: Category Icon, Name, Multi-Account badge, and Split Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(categoryAlloc.category.iconName),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = categoryAlloc.category.localizedName(languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (categoryAlloc.isMultiAccount) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${categoryAlloc.accountSplits.size} accounts",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${LanguageHelper.getString("budgeted_amount", languageMode)}: ${LanguageHelper.formatCurrency(categoryAlloc.totalBudgetOrRequired, languageMode)}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Button(
                    onClick = onEditSplit,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = LanguageHelper.getString("split", languageMode),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Assigned Account Badges
            if (categoryAlloc.accountSplits.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryAlloc.accountSplits.forEach { split ->
                        val accColor = try {
                            Color(android.graphics.Color.parseColor(split.account.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accColor.copy(alpha = 0.1f),
                            border = BorderStroke(0.8.dp, accColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(accColor))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${split.account.localizedName(languageMode)}: ${LanguageHelper.formatCurrency(split.allocatedAmount, languageMode)} (${split.percentageOfCategory.toInt()}%)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = LanguageHelper.getString("unallocated", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Category Account Allocation / Split Dialog
 */
@Composable
private fun CategoryAccountAllocationDialog(
    category: Category,
    allExpenseCategories: List<Category>,
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    currentAllocations: Map<Long, Double>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelectCategory: (Category) -> Unit,
    onSave: (categoryId: Long, allocations: Map<Long, Double>) -> Unit
) {
    val balanceMap = remember(accountsWithBalances) {
        accountsWithBalances.associate { it.account.id to it.currentBalance }
    }

    var selectedCat by remember { mutableStateOf(category) }
    var allocationInputs by remember(selectedCat, currentAllocations) {
        mutableStateOf(
            allAccounts.associate { acc ->
                acc.id to (currentAllocations[acc.id]?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
            }.toMutableMap()
        )
    }

    var showCategoryDropdown by remember { mutableStateOf(false) }

    val totalAllocated = allocationInputs.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val assignedAccountCount = allocationInputs.count { (it.value.toDoubleOrNull() ?: 0.0) > 0 }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = LanguageHelper.getString("split_expense_across_accounts", languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedCat.localizedName(languageMode),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Selector Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDropdown = !showCategoryDropdown }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = IconHelper.getIconByName(selectedCat.iconName),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedCat.localizedName(languageMode),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (showCategoryDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showCategoryDropdown) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(top = 4.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(2.dp)) {
                            items(allExpenseCategories) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCat = cat
                                            onSelectCategory(cat)
                                            showCategoryDropdown = false
                                        }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(cat.iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.localizedName(languageMode),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Account Allocation Rows
                Text(
                    text = LanguageHelper.getString("allocated_accounts", languageMode),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allAccounts, key = { it.id }) { acc ->
                        val accBal = balanceMap[acc.id] ?: 0.0
                        val currentVal = allocationInputs[acc.id] ?: ""
                        val accColor = try {
                            Color(android.graphics.Color.parseColor(acc.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                1.dp,
                                if (currentVal.isNotBlank() && (currentVal.toDoubleOrNull() ?: 0.0) > 0)
                                    accColor.copy(alpha = 0.4f)
                                else
                                    Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(accColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(acc.iconName),
                                            contentDescription = null,
                                            tint = accColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = acc.localizedName(languageMode),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${LanguageHelper.getString("available_amount", languageMode)}: ${LanguageHelper.formatCurrency(accBal, languageMode)}",
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(110.dp)
                                ) {
                                    OutlinedTextField(
                                        value = currentVal,
                                        onValueChange = { newVal ->
                                            val filtered = newVal.filter { it.isDigit() || it == '.' }
                                            allocationInputs = allocationInputs.toMutableMap().apply {
                                                put(acc.id, filtered)
                                            }
                                        },
                                        placeholder = { Text("0 ৳", fontSize = 10.5.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(75.dp)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )

                                    if (currentVal.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                allocationInputs = allocationInputs.toMutableMap().apply {
                                                    put(acc.id, "")
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Summary Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${LanguageHelper.getString("total_category_budget", languageMode)} ($assignedAccountCount ${if (languageMode == LanguageMode.BANGLA) "হিসাব" else "accs"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(totalAllocated, languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageHelper.getString("cancel", languageMode), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val resultMap = allocationInputs.mapNotNull { (accId, valStr) ->
                                val amt = valStr.toDoubleOrNull() ?: 0.0
                                if (amt > 0) accId to amt else null
                            }.toMap()
                            onSave(selectedCat.id, resultMap)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(LanguageHelper.getString("save_allocations", languageMode), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Quick Assign to Account Dialog
 */
@Composable
private fun QuickAssignToAccountDialog(
    account: Account,
    allExpenseCategories: List<Category>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, amount: Double) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(allExpenseCategories.firstOrNull()?.id ?: 0L) }
    var amountInput by remember { mutableStateOf("") }

    val accountColor = try {
        Color(android.graphics.Color.parseColor(account.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(accountColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(account.iconName),
                                contentDescription = null,
                                tint = accountColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = LanguageHelper.getString("assign_to_account", languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = account.localizedName(languageMode),
                                fontSize = 11.sp,
                                color = accountColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = LanguageHelper.getString("select_category_dialog", languageMode),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(2.dp)) {
                        items(allExpenseCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategoryId = cat.id }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(cat.iconName),
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.localizedName(languageMode),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = LanguageHelper.getString("autofill_amount", languageMode),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = { Text("0.00 ৳", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageHelper.getString("cancel", languageMode), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && selectedCategoryId != 0L) {
                                onSave(selectedCategoryId, amt)
                            }
                        },
                        enabled = (amountInput.toDoubleOrNull() ?: 0.0) > 0,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageHelper.getString("apply", languageMode), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PaymentSourceHelpDialog(
    languageMode: LanguageMode,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("payment_source", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "পেমেন্ট সোর্স স্ক্রিনের মূল উদ্দেশ্য হলো প্রতিটি অ্যাকাউন্টে কত টাকা তহবিল মজুদ রাখা প্রয়োজন তা দেখা:\n\n" +
                                "• দুটি ট্যাব: অ্যাকাউন্ট ও ক্যাটাগরি অনুসারে বণ্টন বিশ্লেষণ।\n" +
                                "• বাজেটকৃত পরিমাণ: ওই মাসের মোট নির্ধারিত খরচ বা বাজেট।\n" +
                                "• অবশিষ্ট পরিমাণ: বকেয়া খরচ বা অবশিষ্ট বাজেট যা এখনো ব্যয় হয়নি।\n" +
                                "• ঘাটতি ও উদ্বৃত্ত: কোন অ্যাকাউন্টে অতিরিক্ত টাকা প্রয়োজন এবং কোথায় বাড়তি তহবিল আছে।\n" +
                                "• স্মার্ট স্থানান্তর: ঘাটতি পূরণের জন্য উদ্বৃত্ত অ্যাকাউন্ট থেকে ১-ট্যাপে সরাসরি ফান্ড ট্রান্সফার করুন।"
                    } else {
                        "Payment Source helps track and ensure each account is adequately funded for the month's expenses:\n\n" +
                                "• Two Tabs: Accounts and Categories for clear allocation analysis.\n" +
                                "• Budgeted Amount: Total monthly budgeted expenses.\n" +
                                "• Remaining Amount: Unspent budget / remaining funds to settle.\n" +
                                "• Shortfall & Surplus: Identifies accounts needing funds vs accounts with excess cash.\n" +
                                "• Smart Transfer Insights: 1-tap transfer from surplus accounts to cover shortfalls immediately."
                    },
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LanguageHelper.getString("apply", languageMode), fontSize = 12.sp)
                }
            }
        }
    }
}
