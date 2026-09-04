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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SyncAlt
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.PaymentSourceCalculator

private enum class PaymentSourceViewMode {
    BY_ACCOUNT,
    BY_CATEGORY
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
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSetCurrentMonth: () -> Unit,
    onExecuteTransfer: (fromAccount: Account, toAccount: Account, amount: Double) -> Unit,
    onAddTransactionWithAccount: (Account, TransactionType) -> Unit,
    onEditAccount: (Account) -> Unit,
    onSaveCategoryAllocations: ((categoryId: Long, allocations: Map<Long, Double>) -> Unit)? = null
) {
    var viewMode by remember { mutableStateOf(PaymentSourceViewMode.BY_ACCOUNT) }
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
                        Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("assign_category", languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 1. Month Navigation Header
            item {
                MonthNavigationCard(
                    year = selectedYear,
                    month = selectedMonth,
                    languageMode = languageMode,
                    onPrev = onPrevMonth,
                    onNext = onNextMonth,
                    onToday = onSetCurrentMonth,
                    onHelp = { showHelpDialog = true }
                )
            }

            // 2. Calculation Basis Selector
            item {
                CalculationBasisSelector(
                    selectedBasis = calculationBasis,
                    languageMode = languageMode,
                    onSelect = { calculationBasis = it }
                )
            }

            // 3. Hero Summary & Answer Card
            item {
                PaymentSourceSummaryCard(
                    overview = analysisOverview,
                    languageMode = languageMode
                )
            }

            // 4. Fund Allocation Insights (Smart Transfer Recommendations)
            item {
                FundAllocationInsightCard(
                    suggestions = analysisOverview.transferSuggestions,
                    accountsNeedingFundsCount = analysisOverview.accountsNeedingFundsCount,
                    totalShortfall = analysisOverview.totalShortfall,
                    totalSurplus = analysisOverview.totalSurplus,
                    languageMode = languageMode,
                    onExecuteTransfer = onExecuteTransfer
                )
            }

            // 5. View Mode Switcher Tab (By Account vs By Category & Splits)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tabIdx = if (viewMode == PaymentSourceViewMode.BY_ACCOUNT) 0 else 1
                    TabRow(
                        selectedTabIndex = tabIdx,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            if (tabIdx in tabPositions.indices) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                                    color = MaterialTheme.colorScheme.primary,
                                    height = 3.dp
                                )
                            }
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = viewMode == PaymentSourceViewMode.BY_ACCOUNT,
                            onClick = { viewMode = PaymentSourceViewMode.BY_ACCOUNT },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${LanguageHelper.getString("by_account", languageMode)} (${analysisOverview.accountAnalyses.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = viewMode == PaymentSourceViewMode.BY_CATEGORY,
                            onClick = { viewMode = PaymentSourceViewMode.BY_CATEGORY },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${LanguageHelper.getString("by_category_split", languageMode)} (${analysisOverview.categoryAllocations.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // 6. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    placeholder = {
                        Text(
                            text = if (viewMode == PaymentSourceViewMode.BY_ACCOUNT)
                                LanguageHelper.getString("search_accounts", languageMode)
                            else
                                LanguageHelper.getString("search_categories", languageMode),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // 7. Filter Bar for By-Account View
            if (viewMode == PaymentSourceViewMode.BY_ACCOUNT) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = accountFilter == AccountFilter.ALL,
                            onClick = { accountFilter = AccountFilter.ALL },
                            label = { Text(LanguageHelper.getString("filter_all", languageMode), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = accountFilter == AccountFilter.SHORTFALL_ONLY,
                            onClick = { accountFilter = AccountFilter.SHORTFALL_ONLY },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolidExpense))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${LanguageHelper.getString("filter_shortfall", languageMode)} (${analysisOverview.accountsNeedingFundsCount})",
                                        fontSize = 12.sp
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
                            onClick = { accountFilter = AccountFilter.SURPLUS_ONLY },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolidIncome))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${LanguageHelper.getString("filter_surplus", languageMode)} (${analysisOverview.accountsWithSurplusCount})",
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidIncome.copy(alpha = 0.15f),
                                selectedLabelColor = SolidIncome
                            )
                        )
                    }
                }

                // Account Requirement Analysis Cards
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
                        AccountRequirementCard(
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
                                val bestSurplus = analysisOverview.accountAnalyses.filter { it.isSurplus && it.account.id != analysis.account.id }
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
                            onAccountClick = { onEditAccount(analysis.account) }
                        )
                    }
                }
            } else {
                // 8. BY_CATEGORY View
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = onlyMultiAccountCategories,
                            onClick = { onlyMultiAccountCategories = !onlyMultiAccountCategories },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = LanguageHelper.getString("multi_account_split", languageMode),
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )

                        Text(
                            text = "${filteredCategoryAllocations.size} ${if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Categories"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (filteredCategoryAllocations.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = LanguageHelper.getString("no_categories_match", languageMode),
                            icon = Icons.Default.Category
                        )
                    }
                } else {
                    items(filteredCategoryAllocations, key = { it.category.id }) { catAlloc ->
                        CategoryAllocationCard(
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
                Spacer(modifier = Modifier.height(70.dp))
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

@Composable
private fun MonthNavigationCard(
    year: Int,
    month: Int,
    languageMode: LanguageMode,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onHelp: () -> Unit
) {
    val monthName = DateUtils.getMonthName(month, languageMode)
    val yearStr = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(year.toString()) else year.toString()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable { onToday() }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$monthName $yearStr",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onHelp) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculationBasisSelector(
    selectedBasis: RequirementCalculationBasis,
    languageMode: LanguageMode,
    onSelect: (RequirementCalculationBasis) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("calculation_basis", languageMode),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedBasis.getDescription(languageMode),
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RequirementCalculationBasis.entries.forEach { basis ->
                    val isSelected = basis == selectedBasis
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(basis) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = basis.getTitle(languageMode),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSourceSummaryCard(
    overview: PaymentSourceAnalysisOverview,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = LanguageHelper.getString("monthly_fund_summary", languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = LanguageHelper.getString("payment_source_subtitle", languageMode),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Box Metrics Row: Required | Available | Surplus / Shortfall
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricBox(
                    title = LanguageHelper.getString("required_amount", languageMode),
                    amount = overview.totalRequired,
                    color = SolidExpense,
                    languageMode = languageMode,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricBox(
                    title = LanguageHelper.getString("available_amount", languageMode),
                    amount = overview.totalAvailable,
                    color = MaterialTheme.colorScheme.primary,
                    languageMode = languageMode,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricBox(
                    title = if (overview.isOverallSurplus) LanguageHelper.getString("surplus", languageMode) else LanguageHelper.getString("shortfall", languageMode),
                    amount = if (overview.isOverallSurplus) overview.totalSurplus else overview.totalShortfall,
                    color = if (overview.isOverallSurplus) SolidIncome else SolidExpense,
                    languageMode = languageMode,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Final answer highlight
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (overview.accountsNeedingFundsCount == 0) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (overview.accountsNeedingFundsCount == 0) SolidIncome else SolidTransfer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = overview.getSummaryAnswer(languageMode),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricBox(
    title: String,
    amount: Double,
    color: Color,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LanguageHelper.formatCurrency(amount, languageMode),
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FundAllocationInsightCard(
    suggestions: List<FundAllocationSuggestion>,
    accountsNeedingFundsCount: Int,
    totalShortfall: Double,
    totalSurplus: Double,
    languageMode: LanguageMode,
    onExecuteTransfer: (fromAccount: Account, toAccount: Account, amount: Double) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SolidTransfer.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = SolidTransfer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = LanguageHelper.getString("fund_allocation_insight", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = LanguageHelper.getString("fund_allocation_subtitle", languageMode),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (suggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, SolidTransfer.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AccountPill(
                                            name = suggestion.fromAccount.localizedName(languageMode),
                                            color = SolidIncome
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = SolidTransfer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        AccountPill(
                                            name = suggestion.toAccount.localizedName(languageMode),
                                            color = SolidExpense
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
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SolidTransfer
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SyncAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${LanguageHelper.getString("move", languageMode)} ${LanguageHelper.formatCurrency(suggestion.transferAmount, languageMode)}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = suggestion.getReason(languageMode),
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (accountsNeedingFundsCount == 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SolidIncome.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SolidIncome,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("all_funded", languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SolidIncome
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SolidExpense.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SolidExpense,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) {
                                "উদ্বৃত্ত হিসাবগুলোতে পর্যাপ্ত তহবিল নেই। ঘাটতি পূরণের জন্য নতুন তহবিল বা আয় জমা করতে হবে।"
                            } else {
                                "Surplus accounts do not have enough funds to cover shortfalls. External funding is required."
                            },
                            fontSize = 12.sp,
                            color = SolidExpense
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountPill(name: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = name,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun AccountRequirementCard(
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

    val statusColor = when {
        analysis.isShortfall -> SolidExpense
        analysis.isSurplus -> SolidIncome
        else -> SolidPrimary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.2.dp,
            if (analysis.isShortfall) SolidExpense.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon, Account Name, Balance
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accountColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(analysis.account.iconName),
                            contentDescription = null,
                            tint = accountColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = analysis.account.localizedName(languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${LanguageHelper.getString("current_balance", languageMode)}: ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(analysis.currentBalance, languageMode),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Coverage Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (analysis.requiredExpenseAmount > 0) {
                            val pct = (analysis.availableAmount / analysis.requiredExpenseAmount * 100).toInt()
                            if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(pct.toString())}% কভার" else "$pct% Covered"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "১০০% প্রস্তুত" else "100% Ready"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Funding Progress Bar
            LinearProgressIndicator(
                progress = { (analysis.fundingCoverageRatio / 1.0f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Required vs Available Figures
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getString("required_amount", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.requiredExpenseAmount, languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = SolidExpense
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = LanguageHelper.getString("available_amount", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.availableAmount, languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Needed Result Banner (e.g. "৳200 surplus in bKash", "Need ৳500 more in bKash")
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                analysis.isShortfall -> Icons.Default.ErrorOutline
                                analysis.isSurplus -> Icons.Default.Savings
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = analysis.getActionMessage(languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = statusColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (analysis.isShortfall) {
                        Button(
                            onClick = onFundAccount,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidExpense),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ফান্ড করুন" else "Fund",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand / Collapse Itemized Breakdown Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) {
                        LanguageHelper.getString("hide_breakdown", languageMode)
                    } else {
                        LanguageHelper.getString("expand_breakdown", languageMode)
                    },
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 1. Assigned Expenses
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LanguageHelper.getString("itemized_expenses", languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SolidExpense
                        )

                        TextButton(
                            onClick = onAssignExpense,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = LanguageHelper.getString("assign_to_account", languageMode),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (analysis.itemizedExpenses.isEmpty()) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো নির্ধারিত ব্যয় নেই" else "No expenses assigned to this account",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        analysis.itemizedExpenses.forEach { item ->
                            ItemizedRow(item = item, languageMode = languageMode)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Expected Incomes
                    Text(
                        text = LanguageHelper.getString("itemized_incomes", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SolidIncome
                    )

                    if (analysis.itemizedIncomes.isEmpty()) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো প্রত্যাশিত আয় নেই" else "No expected income assigned to this account",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        analysis.itemizedIncomes.forEach { item ->
                            ItemizedRow(item = item, languageMode = languageMode)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onAddExpense,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = LanguageHelper.getString("expense", languageMode),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onAddIncome,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = LanguageHelper.getString("income", languageMode),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemizedRow(item: AccountRequirementItem, languageMode: LanguageMode) {
    val itemColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (_: Exception) {
        if (item.isExpense) SolidExpense else SolidIncome
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
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
                    .background(itemColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconHelper.getIconByName(item.iconName),
                    contentDescription = null,
                    tint = itemColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isMultiAccountSplit) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(item.splitAccountCount.toString())}টি হিসাবে বিভক্ত" else "Split (${item.splitAccountCount})",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (item.isRecurring) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বিল" else "Bill",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                if (item.originalBudgetOrExpected > 0 && item.actualSpentOrReceived > 0) {
                    Text(
                        text = "${if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget"}: ${LanguageHelper.formatCurrency(item.originalBudgetOrExpected, languageMode)} • ${if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Actual"}: ${LanguageHelper.formatCurrency(item.actualSpentOrReceived, languageMode)}",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Text(
            text = LanguageHelper.formatCurrency(item.amount, languageMode),
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp,
            color = if (item.isExpense) SolidExpense else SolidIncome
        )
    }
}

@Composable
private fun CategoryAllocationCard(
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Category Icon, Name, Total Budget, Edit Split Button
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(categoryAlloc.category.iconName),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = categoryAlloc.category.localizedName(languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (categoryAlloc.isMultiAccount) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(categoryAlloc.accountSplits.size.toString())}টি হিসাব" else "${categoryAlloc.accountSplits.size} Accounts",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${LanguageHelper.getString("total_category_budget", languageMode)}: ${LanguageHelper.formatCurrency(categoryAlloc.totalBudgetOrRequired, languageMode)}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Button(
                    onClick = onEditSplit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LanguageHelper.getString("split", languageMode),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-color proportional account distribution bar
            if (categoryAlloc.accountSplits.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    val totalAlloc = categoryAlloc.accountSplits.sumOf { it.allocatedAmount }.let { if (it <= 0) 1.0 else it }
                    categoryAlloc.accountSplits.forEach { split ->
                        val weight = (split.allocatedAmount / totalAlloc).toFloat().coerceIn(0.01f, 1f)
                        val accColor = try {
                            Color(android.graphics.Color.parseColor(split.account.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(8.dp)
                                .background(accColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Itemized account splits list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryAlloc.accountSplits.forEach { split ->
                        val accColor = try {
                            Color(android.graphics.Color.parseColor(split.account.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(accColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = split.account.localizedName(languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${split.percentageOfCategory.toInt()}%)",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Text(
                                text = LanguageHelper.formatCurrency(split.allocatedAmount, languageMode),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = LanguageHelper.getString("unallocated", languageMode),
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = LanguageHelper.getString("split_expense_across_accounts", languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedCat.localizedName(languageMode),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selector Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDropdown = !showCategoryDropdown }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = IconHelper.getIconByName(selectedCat.iconName),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCat.localizedName(languageMode),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (showCategoryDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (showCategoryDropdown) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 4.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(4.dp)) {
                            items(allExpenseCategories) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCat = cat
                                            onSelectCategory(cat)
                                            showCategoryDropdown = false
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(cat.iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.localizedName(languageMode),
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Allocation Rows
                Text(
                    text = LanguageHelper.getString("allocated_accounts", languageMode),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            shape = RoundedCornerShape(12.dp),
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
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(accColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(acc.iconName),
                                            contentDescription = null,
                                            tint = accColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = acc.localizedName(languageMode),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${LanguageHelper.getString("available_amount", languageMode)}: ${LanguageHelper.formatCurrency(accBal, languageMode)}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(130.dp)
                                ) {
                                    OutlinedTextField(
                                        value = currentVal,
                                        onValueChange = { newVal ->
                                            val filtered = newVal.filter { it.isDigit() || it == '.' }
                                            allocationInputs = allocationInputs.toMutableMap().apply {
                                                put(acc.id, filtered)
                                            }
                                        },
                                        placeholder = { Text("0 ৳", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
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
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Summary Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${LanguageHelper.getString("total_category_budget", languageMode)} ($assignedAccountCount ${if (languageMode == LanguageMode.BANGLA) "হিসাব" else "accs"})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(totalAllocated, languageMode),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageHelper.getString("cancel", languageMode))
                    }

                    Button(
                        onClick = {
                            val resultMap = allocationInputs.mapNotNull { (accId, valStr) ->
                                val amt = valStr.toDoubleOrNull() ?: 0.0
                                if (amt > 0) accId to amt else null
                            }.toMap()
                            onSave(selectedCat.id, resultMap)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LanguageHelper.getString("save_allocations", languageMode))
                    }
                }
            }
        }
    }
}

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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(accountColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(account.iconName),
                                contentDescription = null,
                                tint = accountColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = LanguageHelper.getString("assign_to_account", languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = account.localizedName(languageMode),
                                fontSize = 11.5.sp,
                                color = accountColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = LanguageHelper.getString("select_category_dialog", languageMode),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(4.dp)) {
                        items(allExpenseCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategoryId = cat.id }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(cat.iconName),
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.localizedName(languageMode),
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = LanguageHelper.getString("autofill_amount", languageMode),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = { Text("0.00 ৳") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageHelper.getString("cancel", languageMode))
                    }

                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && selectedCategoryId != 0L) {
                                onSave(selectedCategoryId, amt)
                            }
                        },
                        enabled = (amountInput.toDoubleOrNull() ?: 0.0) > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageHelper.getString("apply", languageMode))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LanguageHelper.getString("payment_source_analysis", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "পেমেন্ট সোর্স অ্যানালাইসিসের মূল উদ্দেশ্য হলো প্রতিটি অ্যাকাউন্টে ঠিক কত টাকা মজুদ রাখা প্রয়োজন তা নির্ধারণ করা:\n\n" +
                                "• একাধিক অ্যাকাউন্টে বাজেট বণ্টন (Multi-Account Splits):\n" +
                                "একটি খরচ বা বিল (যেমন: বিদ্যুৎ বিল ৳৩৩০০) একাধিক অ্যাকাউন্টে বিভক্ত করা যায়—যেমন বিকাশ থেকে ৳১৩০০ এবং সোনালী ব্যাংক থেকে ৳২০০০।\n\n" +
                                "• প্রয়োজনীয় অর্থ (Required): ওই অ্যাকাউন্টে নির্ধারিত খরচ বা বাজেটের মোট পরিমাণ।\n" +
                                "• উপলব্ধ অর্থ (Available): অ্যাকাউন্টের বর্তমান ব্যালেন্স + ওই অ্যাকাউন্টে প্রত্যাশিত মোট আয়।\n" +
                                "• ঘাটতি (Shortfall): অ্যাকাউন্টে আর কত টাকা অতিরিক্ত প্রয়োজন।\n" +
                                "• উদ্বৃত্ত (Surplus): খরচের পর অ্যাকাউন্টে আর কত টাকা বাড়তি থাকবে।\n\n" +
                                "• তহবিল স্থানান্তর (Fund Allocation Insight):\n" +
                                "কোনো অ্যাকাউন্টে উদ্বৃত্ত এবং অন্যটিতে ঘাটতি থাকলে, সিস্টেম সরাসরি 'স্থানান্তর' করার স্মার্ট পরামর্শ দেয় যাতে কোনো পেমেন্ট আটকে না যায়।"
                    } else {
                        "The primary purpose of Payment Source Analysis is to determine how much money must be available in each account for this month:\n\n" +
                                "• Multi-Account Expense Splits:\n" +
                                "A single budget expense (e.g. Electricity Bill ৳3,300) can be split across multiple accounts—e.g. ৳1,300 from bKash and ৳2,000 from Sonali Bank.\n\n" +
                                "• Required Amount: Total expenses & bills assigned to this account.\n" +
                                "• Available Amount: Current balance + expected income coming into this account.\n" +
                                "• Shortfall: How much more money is needed in this account.\n" +
                                "• Surplus: How much extra money will remain in this account.\n\n" +
                                "• Fund Allocation Insight:\n" +
                                "If one account has a surplus and another has a shortage, the system suggests smart transfers with one-click transfer execution."
                    },
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LanguageHelper.getString("apply", languageMode))
                }
            }
        }
    }
}
