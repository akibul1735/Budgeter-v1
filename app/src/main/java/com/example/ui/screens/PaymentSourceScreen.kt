package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.AccountObligationAnalysis
import com.example.data.model.AccountRequirementAnalysis
import com.example.data.model.AccountRequirementItem
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryAllocationAnalysis
import com.example.data.model.CategoryType
import com.example.data.model.FundAllocationSuggestion
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.OtherAccountAllocationAnalysis
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
import com.example.util.AccountObligation
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.PaymentSourceCalculator
import com.example.util.PaymentSourceConfig
import java.util.Calendar

private enum class MainPaymentSourceTab {
    PAYMENT_SOURCES,
    ASSIGNED_ITEMS
}

private enum class AssignedItemSectionFilter {
    ALL,
    OTHER_ACCOUNTS,
    EXPENSES,
    INCOMES
}

private enum class AssignedItemStatusFilter {
    ALL,
    BUDGETED_ONLY,
    REMAINING_ONLY,
    MOST_FREQUENT,
    SPLIT_ONLY,
    UNASSIGNED_ONLY
}

private enum class AccountStatusFilter {
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
    recurringBills: List<RecurringBill> = emptyList(),
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    paymentSourceConfig: PaymentSourceConfig = PaymentSourceConfig(),
    onOpenDrawer: () -> Unit = {},
    onPrevMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onSetCurrentMonth: () -> Unit = {},
    onExecuteTransfer: (fromAccountId: Long, toAccountId: Long, amount: Double, note: String) -> Unit = { _, _, _, _ -> },
    onAddTransactionWithAccount: (Long, TransactionType) -> Unit = { _, _ -> },
    onEditAccount: (Account) -> Unit = {},
    onSaveCategoryAllocations: (categoryId: Long, allocations: Map<Long, Double>) -> Unit = { _, _ -> },
    onSaveOtherAccountAllocations: (otherAccountId: Long, allocations: Map<Long, Double>) -> Unit = { _, _ -> },
    onSetPaymentSourceAccountIds: (Set<Long>) -> Unit = {},
    onSaveAccountObligation: (AccountObligation) -> Unit = {},
    onDeleteAccountObligation: (String) -> Unit = {},
    onAccountClick: (Long) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MainPaymentSourceTab.PAYMENT_SOURCES) }
    var calculationBasis by remember { mutableStateOf(RequirementCalculationBasis.BUDGET_AMOUNT) }
    var accountStatusFilter by remember { mutableStateOf(AccountStatusFilter.ALL) }

    // Assigned items tab filters
    var assignedSectionFilter by remember { mutableStateOf(AssignedItemSectionFilter.ALL) }
    var assignedStatusFilter by remember { mutableStateOf(AssignedItemStatusFilter.ALL) }
    var assignedSearchQuery by remember { mutableStateOf("") }

    // Dialogs state
    var showSourceSelectorDialog by remember { mutableStateOf(false) }
    var showCategorySplitDialog by remember { mutableStateOf<CategoryAllocationAnalysis?>(null) }
    var showOtherAccountSplitDialog by remember { mutableStateOf<OtherAccountAllocationAnalysis?>(null) }
    var showAddObligationDialog by remember { mutableStateOf(false) }
    var transferSuggestionToExecute by remember { mutableStateOf<FundAllocationSuggestion?>(null) }

    // Selected payment source account IDs
    val selectedSourceAccountIds = remember(paymentSourceConfig, allAccounts) {
        if (paymentSourceConfig.hasCustomizedSelection) {
            paymentSourceConfig.selectedSourceAccountIds
        } else {
            allAccounts.filter { it.isActive && (it.parentId != null || it.type == AccountType.ASSET) }.map { it.id }.toSet()
        }
    }

    // Active leaf accounts map for selection
    val availableSourceAccounts = remember(allAccounts) {
        allAccounts.filter { it.isActive }
    }

    // Frequency usage map for categories and accounts
    val categoryUsageFrequencyMap = remember(allTransactions) {
        val map = mutableMapOf<Long, Int>()
        allTransactions.forEach { tx ->
            tx.transaction.categoryId?.let { map[it] = (map[it] ?: 0) + 1 }
            tx.transaction.subCategoryId?.let { map[it] = (map[it] ?: 0) + 1 }
            tx.transaction.creditAccountId?.let { map[it] = (map[it] ?: 0) + 1 }
            tx.transaction.debitAccountId?.let { map[it] = (map[it] ?: 0) + 1 }
        }
        map
    }

    // Core Analysis Overview Calculation
    val analysisOverview = remember(
        selectedYear,
        selectedMonth,
        calculationBasis,
        allAccounts,
        accountsWithBalances,
        allCategories,
        monthlyBudgets,
        allTransactions,
        recurringBills,
        selectedSourceAccountIds,
        paymentSourceConfig.accountObligations
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
            recurringBills = recurringBills,
            selectedPaymentSourceIds = selectedSourceAccountIds,
            accountObligations = paymentSourceConfig.accountObligations
        )
    }

    val monthName = remember(selectedMonth, languageMode) {
        DateUtils.getMonthName(selectedMonth, languageMode)
    }

    val currentYearMonth = remember {
        val c = Calendar.getInstance()
        c.get(Calendar.YEAR) to (c.get(Calendar.MONTH) + 1)
    }
    val isCurrentMonth = selectedYear == currentYearMonth.first && selectedMonth == currentYearMonth.second

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("payment_source_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTabHeader(
                title = LanguageHelper.getString("payment_source", languageMode),
                onOpenDrawer = onOpenDrawer,
                actions = {
                    // Payment Sources Selector Action
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showSourceSelectorDialog = true }
                            .testTag("btn_select_payment_sources")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Sources",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${analysisOverview.accountAnalyses.size} ${if (languageMode == LanguageMode.BANGLA) "সোর্স" else "Sources"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )

            // Month Selector Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Prev Month",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSetCurrentMonth() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Current Month",
                            tint = if (isCurrentMonth) SolidPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$monthName $selectedYear",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isCurrentMonth) SolidPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Calculation Basis Pill Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        BasisTabPill(
                            selected = calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT,
                            text = calculationBasis.getTitle(languageMode),
                            onClick = { calculationBasis = RequirementCalculationBasis.BUDGET_AMOUNT }
                        )
                        BasisTabPill(
                            selected = calculationBasis == RequirementCalculationBasis.REMAINING_AMOUNT,
                            text = RequirementCalculationBasis.REMAINING_AMOUNT.getTitle(languageMode),
                            onClick = { calculationBasis = RequirementCalculationBasis.REMAINING_AMOUNT }
                        )
                    }
                }
            }

            // TWO MAIN TABS: 1. Payment Source, 2. Assigned Items
            val totalAssignedCount = analysisOverview.otherAccountAllocations.size + analysisOverview.categoryAllocations.size + analysisOverview.incomeAllocations.size

            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SolidPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = SolidPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == MainPaymentSourceTab.PAYMENT_SOURCES,
                    onClick = { selectedTab = MainPaymentSourceTab.PAYMENT_SOURCES },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স (${analysisOverview.accountAnalyses.size})" else "Payment Source (${analysisOverview.accountAnalyses.size})",
                                fontWeight = if (selectedTab == MainPaymentSourceTab.PAYMENT_SOURCES) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == MainPaymentSourceTab.ASSIGNED_ITEMS,
                    onClick = { selectedTab = MainPaymentSourceTab.ASSIGNED_ITEMS },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CallSplit, contentDescription = null, tint = SolidTransfer, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বরাদ্দকৃত আইটেম ($totalAssignedCount)" else "Assigned Items ($totalAssignedCount)",
                                fontWeight = if (selectedTab == MainPaymentSourceTab.ASSIGNED_ITEMS) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }

            // Tab Content
            when (selectedTab) {
                MainPaymentSourceTab.PAYMENT_SOURCES -> {
                    AccountsPaymentSourceTabContent(
                        overview = analysisOverview,
                        languageMode = languageMode,
                        accountStatusFilter = accountStatusFilter,
                        onStatusFilterChange = { accountStatusFilter = it },
                        onOpenSourceSelector = { showSourceSelectorDialog = true },
                        onOpenAddObligation = { showAddObligationDialog = true },
                        onExecuteTransferSuggestion = { transferSuggestionToExecute = it },
                        onAccountClick = onAccountClick,
                        onAddTransaction = onAddTransactionWithAccount,
                        onDeleteObligation = onDeleteAccountObligation
                    )
                }
                MainPaymentSourceTab.ASSIGNED_ITEMS -> {
                    AssignedItemsTabContent(
                        overview = analysisOverview,
                        languageMode = languageMode,
                        sectionFilter = assignedSectionFilter,
                        onSectionFilterChange = { assignedSectionFilter = it },
                        statusFilter = assignedStatusFilter,
                        onStatusFilterChange = { assignedStatusFilter = it },
                        searchQuery = assignedSearchQuery,
                        onSearchChange = { assignedSearchQuery = it },
                        usageFrequencyMap = categoryUsageFrequencyMap,
                        onOpenCategorySplitDialog = { showCategorySplitDialog = it },
                        onOpenOtherAccountSplitDialog = { showOtherAccountSplitDialog = it },
                        onAddTransactionWithAccount = onAddTransactionWithAccount
                    )
                }
            }
        }

        // --- DIALOGS ---

        // 1. Select Payment Source Accounts Dialog (Multi-Select)
        if (showSourceSelectorDialog) {
            PaymentSourceSelectorDialog(
                allAccounts = availableSourceAccounts,
                accountsWithBalances = accountsWithBalances,
                initialSelectedIds = selectedSourceAccountIds,
                languageMode = languageMode,
                onDismiss = { showSourceSelectorDialog = false },
                onSave = { newSelectedIds ->
                    onSetPaymentSourceAccountIds(newSelectedIds)
                    showSourceSelectorDialog = false
                }
            )
        }

        // 2. Category Multi-Account Split & Assignment Dialog
        showCategorySplitDialog?.let { allocAnalysis ->
            CategoryAccountSplitDialog(
                categoryAllocation = allocAnalysis,
                paymentSourceAccounts = analysisOverview.accountAnalyses.map { it.account },
                languageMode = languageMode,
                onDismiss = { showCategorySplitDialog = null },
                onSave = { allocMap ->
                    onSaveCategoryAllocations(allocAnalysis.category.id, allocMap)
                    showCategorySplitDialog = null
                }
            )
        }

        // 3. Other Account Multi-Source Split & Assignment Dialog
        showOtherAccountSplitDialog?.let { otherAccAnalysis ->
            OtherAccountSplitDialog(
                otherAccountAllocation = otherAccAnalysis,
                paymentSourceAccounts = analysisOverview.accountAnalyses.map { it.account },
                languageMode = languageMode,
                onDismiss = { showOtherAccountSplitDialog = null },
                onSave = { allocMap ->
                    onSaveOtherAccountAllocations(otherAccAnalysis.account.id, allocMap)
                    showOtherAccountSplitDialog = null
                }
            )
        }

        // 4. Link Payable / Receivable Obligation Dialog
        if (showAddObligationDialog) {
            LinkAccountObligationDialog(
                paymentSourceAccounts = analysisOverview.accountAnalyses.map { it.account },
                allAccounts = allAccounts,
                languageMode = languageMode,
                onDismiss = { showAddObligationDialog = false },
                onSave = { obligation ->
                    onSaveAccountObligation(obligation)
                    showAddObligationDialog = false
                }
            )
        }

        // 5. Quick Transfer Execution Confirmation
        transferSuggestionToExecute?.let { suggestion ->
            TransferConfirmationDialog(
                suggestion = suggestion,
                languageMode = languageMode,
                onDismiss = { transferSuggestionToExecute = null },
                onConfirm = { note ->
                    onExecuteTransfer(suggestion.fromAccount.id, suggestion.toAccount.id, suggestion.transferAmount, note)
                    transferSuggestionToExecute = null
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 1: PAYMENT SOURCES VIEW
// -----------------------------------------------------------------------------

@Composable
private fun AccountsPaymentSourceTabContent(
    overview: PaymentSourceAnalysisOverview,
    languageMode: LanguageMode,
    accountStatusFilter: AccountStatusFilter,
    onStatusFilterChange: (AccountStatusFilter) -> Unit,
    onOpenSourceSelector: () -> Unit,
    onOpenAddObligation: () -> Unit,
    onExecuteTransferSuggestion: (FundAllocationSuggestion) -> Unit,
    onAccountClick: (Long) -> Unit,
    onAddTransaction: (Long, TransactionType) -> Unit,
    onDeleteObligation: (String) -> Unit
) {
    val filteredAccounts = remember(overview.accountAnalyses, accountStatusFilter) {
        when (accountStatusFilter) {
            AccountStatusFilter.ALL -> overview.accountAnalyses
            AccountStatusFilter.SHORTFALL_ONLY -> overview.accountAnalyses.filter { it.isShortfall }
            AccountStatusFilter.SURPLUS_ONLY -> overview.accountAnalyses.filter { it.isSurplus }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Overview Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_source_overview_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SolidPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স সারসংক্ষেপ" else "Payment Sources Overview",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (overview.isOverallSurplus) {
                                    "${if (languageMode == LanguageMode.BANGLA) "মোট উদ্বৃত্ত" else "Net Surplus"}: +${LanguageHelper.formatCurrency(overview.netStatus, languageMode)}"
                                } else {
                                    "${if (languageMode == LanguageMode.BANGLA) "মোট ঘাটতি" else "Net Shortfall"}: -${LanguageHelper.formatCurrency(overview.totalShortfall, languageMode)}"
                                },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (overview.accountsNeedingFundsCount > 0) SolidExpense else Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (overview.accountsNeedingFundsCount == 0) (if (languageMode == LanguageMode.BANGLA) "সকল সোর্স প্রস্তুত" else "All Funded")
                                else "${overview.accountsNeedingFundsCount} ${if (languageMode == LanguageMode.BANGLA) "টিতে ঘাটতি" else "Need Funds"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "প্রয়োজনীয় ব্যয়" else "Required Expenses",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(overview.totalRequired, languageMode),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট মজুদ ও আয়" else "Total Available & Income",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(overview.totalAvailable, languageMode),
                                color = SolidIncome,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Select Payment Sources & Link Account/Payable)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenSourceSelector,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সোর্স নির্বাচন" else "Select Sources",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onOpenAddObligation,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "+ দেয় / পাওনা" else "+ Link Payable",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Transfer Suggestions (if any shortfall)
        if (overview.transferSuggestions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, SolidTransfer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SyncAlt, contentDescription = null, tint = SolidTransfer, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "প্রস্তাবিত ফান্ড ট্রান্সফার" else "Suggested Fund Transfers",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        overview.transferSuggestions.forEach { suggestion ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = suggestion.getReason(languageMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onExecuteTransferSuggestion(suggestion) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার" else "Transfer",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Account Status Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = accountStatusFilter == AccountStatusFilter.ALL,
                    onClick = { onStatusFilterChange(AccountStatusFilter.ALL) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "সকল একাউন্ট (${overview.accountAnalyses.size})" else "All (${overview.accountAnalyses.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = accountStatusFilter == AccountStatusFilter.SHORTFALL_ONLY,
                    onClick = { onStatusFilterChange(AccountStatusFilter.SHORTFALL_ONLY) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "ঘাটতিযুক্ত (${overview.accountsNeedingFundsCount})" else "Shortfall (${overview.accountsNeedingFundsCount})", fontSize = 11.sp) },
                    leadingIcon = { if (overview.accountsNeedingFundsCount > 0) Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(14.dp)) }
                )
                FilterChip(
                    selected = accountStatusFilter == AccountStatusFilter.SURPLUS_ONLY,
                    onClick = { onStatusFilterChange(AccountStatusFilter.SURPLUS_ONLY) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "উদ্বৃত্তযুক্ত (${overview.accountsWithSurplusCount})" else "Surplus (${overview.accountsWithSurplusCount})", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(14.dp)) }
                )
            }
        }

        // Account List Cards
        if (filteredAccounts.isEmpty()) {
            item {
                EmptyStateCard(
                    message = if (languageMode == LanguageMode.BANGLA) "কোনো পেমেন্ট সোর্স একাউন্ট পাওয়া যায়নি।" else "No payment source accounts found for this filter."
                )
            }
        } else {
            items(filteredAccounts, key = { it.account.id }) { accAnalysis ->
                AccountRequirementCard(
                    analysis = accAnalysis,
                    languageMode = languageMode,
                    onAccountClick = onAccountClick,
                    onAddExpense = { onAddTransaction(accAnalysis.account.id, TransactionType.EXPENSE) },
                    onAddIncome = { onAddTransaction(accAnalysis.account.id, TransactionType.INCOME) }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 2: ASSIGNED ITEMS (OTHER ACCOUNTS, EXPENSES, INCOME) WITH RICH FILTERS
// -----------------------------------------------------------------------------

@Composable
private fun AssignedItemsTabContent(
    overview: PaymentSourceAnalysisOverview,
    languageMode: LanguageMode,
    sectionFilter: AssignedItemSectionFilter,
    onSectionFilterChange: (AssignedItemSectionFilter) -> Unit,
    statusFilter: AssignedItemStatusFilter,
    onStatusFilterChange: (AssignedItemStatusFilter) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    usageFrequencyMap: Map<Long, Int>,
    onOpenCategorySplitDialog: (CategoryAllocationAnalysis) -> Unit,
    onOpenOtherAccountSplitDialog: (OtherAccountAllocationAnalysis) -> Unit,
    onAddTransactionWithAccount: (Long, TransactionType) -> Unit
) {
    val q = searchQuery.trim().lowercase()

    // Filter Other Accounts
    val filteredOtherAccounts = remember(overview.otherAccountAllocations, q, statusFilter, usageFrequencyMap) {
        var list = overview.otherAccountAllocations
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
    val filteredExpenses = remember(overview.categoryAllocations, q, statusFilter, usageFrequencyMap) {
        var list = overview.categoryAllocations
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
    val filteredIncomes = remember(overview.incomeAllocations, q, statusFilter, usageFrequencyMap) {
        var list = overview.incomeAllocations
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

    val totalItemsShown = when (sectionFilter) {
        AssignedItemSectionFilter.ALL -> filteredOtherAccounts.size + filteredExpenses.size + filteredIncomes.size
        AssignedItemSectionFilter.OTHER_ACCOUNTS -> filteredOtherAccounts.size
        AssignedItemSectionFilter.EXPENSES -> filteredExpenses.size
        AssignedItemSectionFilter.INCOMES -> filteredIncomes.size
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Search Box
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assigned_items_search_input"),
                placeholder = {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট, ক্যাটাগরি বা সোর্স অনুসন্ধান..." else "Search account, category, or payment source...",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolidPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // 2. Section Selector Pills (All | Other Accounts | Expense | Income)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = sectionFilter == AssignedItemSectionFilter.ALL,
                    onClick = { onSectionFilterChange(AssignedItemSectionFilter.ALL) },
                    label = {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সকল আইটেম" else "All Items",
                            fontSize = 11.sp,
                            fontWeight = if (sectionFilter == AssignedItemSectionFilter.ALL) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                FilterChip(
                    selected = sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS,
                    onClick = { onSectionFilterChange(AssignedItemSectionFilter.OTHER_ACCOUNTS) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(13.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "অন্যান্য একাউন্ট (${overview.otherAccountAllocations.size})" else "Other Accounts (${overview.otherAccountAllocations.size})",
                                fontSize = 11.sp,
                                fontWeight = if (sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
                FilterChip(
                    selected = sectionFilter == AssignedItemSectionFilter.EXPENSES,
                    onClick = { onSectionFilterChange(AssignedItemSectionFilter.EXPENSES) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(13.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ব্যয় (${overview.categoryAllocations.size})" else "Expenses (${overview.categoryAllocations.size})",
                                fontSize = 11.sp,
                                fontWeight = if (sectionFilter == AssignedItemSectionFilter.EXPENSES) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
                FilterChip(
                    selected = sectionFilter == AssignedItemSectionFilter.INCOMES,
                    onClick = { onSectionFilterChange(AssignedItemSectionFilter.INCOMES) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(13.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আয় (${overview.incomeAllocations.size})" else "Income (${overview.incomeAllocations.size})",
                                fontSize = 11.sp,
                                fontWeight = if (sectionFilter == AssignedItemSectionFilter.INCOMES) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }
        }

        // 3. Status Filters Row (All | Budgeted | Remaining | Most Used | Split Across Sources | Unassigned)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = statusFilter == AssignedItemStatusFilter.ALL,
                    onClick = { onStatusFilterChange(AssignedItemStatusFilter.ALL) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "সকল" else "All", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == AssignedItemStatusFilter.BUDGETED_ONLY,
                    onClick = { onStatusFilterChange(AssignedItemStatusFilter.BUDGETED_ONLY) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "বাজেটকৃত / দেনা" else "Budgeted / Due", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == AssignedItemStatusFilter.REMAINING_ONLY,
                    onClick = { onStatusFilterChange(AssignedItemStatusFilter.REMAINING_ONLY) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == AssignedItemStatusFilter.MOST_FREQUENT,
                    onClick = { onStatusFilterChange(AssignedItemStatusFilter.MOST_FREQUENT) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "সর্বাধিক ব্যবহৃত" else "Most Used", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == AssignedItemStatusFilter.SPLIT_ONLY,
                    onClick = { onStatusFilterChange(AssignedItemStatusFilter.SPLIT_ONLY) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "একাধিক সোর্সে বিভক্ত" else "Split Across Sources", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(13.dp)) }
                )
                FilterChip(
                    selected = statusFilter == AssignedItemStatusFilter.UNASSIGNED_ONLY,
                    onClick = { onStatusFilterChange(AssignedItemStatusFilter.UNASSIGNED_ONLY) },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "বরাদ্দহীন" else "Unassigned", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(13.dp)) }
                )
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

        // 4. Section: Other Accounts
        if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.OTHER_ACCOUNTS) && filteredOtherAccounts.isNotEmpty()) {
            if (sectionFilter == AssignedItemSectionFilter.ALL) {
                item {
                    SectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "অন্যান্য অ্যাকাউন্ট (ব্যক্তি / ঋণ / দেনা)" else "Other Accounts (Persons, Loans, Liabilities)",
                        count = filteredOtherAccounts.size,
                        icon = Icons.Default.People,
                        color = SolidTransfer
                    )
                }
            }

            items(filteredOtherAccounts, key = { "other_acc_${it.account.id}" }) { otherAccAlloc ->
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

        // 5. Section: Expense Categories
        if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.EXPENSES) && filteredExpenses.isNotEmpty()) {
            if (sectionFilter == AssignedItemSectionFilter.ALL) {
                item {
                    SectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "ব্যয় ক্যাটাগরি" else "Expense Categories",
                        count = filteredExpenses.size,
                        icon = Icons.Default.MonetizationOn,
                        color = SolidExpense
                    )
                }
            }

            items(filteredExpenses, key = { "expense_${it.category.id}" }) { catAlloc ->
                CategoryAllocationCard(
                    allocation = catAlloc,
                    isExpense = true,
                    languageMode = languageMode,
                    onOpenSplit = { onOpenCategorySplitDialog(catAlloc) }
                )
            }
        }

        // 6. Section: Income Categories
        if ((sectionFilter == AssignedItemSectionFilter.ALL || sectionFilter == AssignedItemSectionFilter.INCOMES) && filteredIncomes.isNotEmpty()) {
            if (sectionFilter == AssignedItemSectionFilter.ALL) {
                item {
                    SectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "আয় ক্যাটাগরি" else "Income Categories",
                        count = filteredIncomes.size,
                        icon = Icons.Default.Category,
                        color = SolidIncome
                    )
                }
            }

            items(filteredIncomes, key = { "income_${it.category.id}" }) { catAlloc ->
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

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = "$count",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: OTHER ACCOUNT ALLOCATION CARD (TREATED SAME AS CATEGORIES)
// -----------------------------------------------------------------------------

@Composable
private fun OtherAccountAllocationCard(
    allocation: OtherAccountAllocationAnalysis,
    languageMode: LanguageMode,
    onOpenSplit: () -> Unit,
    onAddTransaction: () -> Unit
) {
    val acc = allocation.account
    val accColor = remember(acc.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(acc.colorHex))
        } catch (e: Exception) {
            if (allocation.isExpense) SolidExpense else SolidIncome
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("other_acc_card_${acc.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Account Icon, Name, Type Badge, Split Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(acc.iconName),
                            contentDescription = null,
                            tint = accColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = acc.localizedName(languageMode),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Type / Liability badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (allocation.isExpense) SolidExpense.copy(alpha = 0.12f) else SolidIncome.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (allocation.isExpense) (if (languageMode == LanguageMode.BANGLA) "দেনা / দায়" else "Payable")
                                    else (if (languageMode == LanguageMode.BANGLA) "পাওনা / আয়" else "Receivable"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allocation.isExpense) SolidExpense else SolidIncome,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "${if (allocation.isExpense) "Due/Target" else "Expected"}: ${LanguageHelper.formatCurrency(allocation.totalBudgeted, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Button(
                    onClick = onOpenSplit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allocation.isMultiAccount) SolidTransfer else SolidPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (allocation.isMultiAccount) Icons.Default.CallSplit else Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (allocation.isMultiAccount) "Split (${allocation.accountSplits.size})"
                        else if (allocation.accountSplits.isNotEmpty()) (if (languageMode == LanguageMode.BANGLA) "বরাদ্দকৃত" else "Assigned")
                        else (if (languageMode == LanguageMode.BANGLA) "বরাদ্দ করুন" else "Assign"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned Payment Sources Breakdown
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (allocation.accountSplits.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোনো সোর্স নির্ধারিত নেই (ডিফল্ট হিসাব ব্যবহৃত হবে)" else "No payment source assigned (fallback used)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        allocation.accountSplits.forEach { split ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(split.account.iconName),
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = split.account.localizedName(languageMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (allocation.isMultiAccount && split.percentageOfCategory > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${split.percentageOfCategory.toInt()}%)",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Text(
                                    text = LanguageHelper.formatCurrency(split.allocatedAmount, languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allocation.isExpense) SolidExpense else SolidIncome
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: ACCOUNT REQUIREMENT CARD
// -----------------------------------------------------------------------------

@Composable
private fun AccountRequirementCard(
    analysis: AccountRequirementAnalysis,
    languageMode: LanguageMode,
    onAccountClick: (Long) -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .testTag("acc_req_card_${analysis.account.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Account Name, Balance & Shortfall/Surplus Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SolidPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(analysis.account.iconName),
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = analysis.account.localizedName(languageMode),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Balance: ${LanguageHelper.formatCurrency(analysis.currentBalance, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (analysis.isShortfall) SolidExpense.copy(alpha = 0.15f)
                    else if (analysis.isSurplus) SolidIncome.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (analysis.isShortfall) "Need ${LanguageHelper.formatCurrency(analysis.shortfall, languageMode)}"
                        else if (analysis.isSurplus) "Surplus ${LanguageHelper.formatCurrency(analysis.surplus, languageMode)}"
                        else "Balanced",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.isShortfall) SolidExpense else if (analysis.isSurplus) SolidIncome else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Requirement Progress & Figures
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "প্রয়োজনীয় ব্যয়" else "Required Expenses",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.requiredExpenseAmount, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidExpense
                    )
                }

                if (analysis.expectedIncomeAmount > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "প্রত্যাশিত আয়" else "Expected Income",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "+${LanguageHelper.formatCurrency(analysis.expectedIncomeAmount, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidIncome
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কার্যকর মজুদ" else "Available Funds",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.availableAmount, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.availableAmount >= analysis.requiredExpenseAmount) SolidIncome else SolidExpense
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { analysis.fundingCoverageRatio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (analysis.isShortfall) SolidExpense else SolidIncome,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            // Expand / Collapse Itemized Breakdown
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "এই সোর্সের অন্তর্ভুক্ত বরাদ্দসমূহ" else "Assigned Items to this Source",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (analysis.itemizedExpenses.isEmpty() && analysis.itemizedIncomes.isEmpty()) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো ব্যয় বা আয় বরাদ্দ করা হয়নি।" else "No items assigned to this source yet.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        // Expenses breakdown
                        analysis.itemizedExpenses.forEach { item ->
                            ItemizedRequirementRow(item = item, languageMode = languageMode)
                        }

                        // Incomes breakdown
                        analysis.itemizedIncomes.forEach { item ->
                            ItemizedRequirementRow(item = item, languageMode = languageMode)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onAddIncome,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+ ${if (languageMode == LanguageMode.BANGLA) "আয়" else "Income"}", fontSize = 11.sp, color = SolidIncome)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = onAddExpense,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+ ${if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense"}", fontSize = 11.sp, color = SolidExpense)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemizedRequirementRow(
    item: AccountRequirementItem,
    languageMode: LanguageMode
) {
    val itemColor = remember(item.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(item.colorHex))
        } catch (e: Exception) {
            if (item.isExpense) SolidExpense else SolidIncome
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.isMultiAccountSplit) {
                    Text(
                        text = "Split (${item.splitAccountCount} sources)",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Text(
            text = (if (item.isExpense) "-" else "+") + LanguageHelper.formatCurrency(item.amount, languageMode),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.isExpense) SolidExpense else SolidIncome
        )
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: CATEGORY ALLOCATION CARD
// -----------------------------------------------------------------------------

@Composable
private fun CategoryAllocationCard(
    allocation: CategoryAllocationAnalysis,
    isExpense: Boolean,
    languageMode: LanguageMode,
    onOpenSplit: () -> Unit
) {
    val cat = allocation.category
    val catColor = remember(cat.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(cat.colorHex))
        } catch (e: Exception) {
            if (isExpense) SolidExpense else SolidIncome
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cat_alloc_card_${cat.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Category Icon, Name, Split Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(catColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(cat.iconName),
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = cat.localizedName(languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${if (isExpense) "Budget" else "Expected"}: ${LanguageHelper.formatCurrency(allocation.totalBudgeted, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Button(
                    onClick = onOpenSplit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allocation.isMultiAccount) SolidTransfer else SolidPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (allocation.isMultiAccount) Icons.Default.CallSplit else Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (allocation.isMultiAccount) "Split (${allocation.accountSplits.size})"
                        else if (allocation.accountSplits.isNotEmpty()) (if (languageMode == LanguageMode.BANGLA) "বরাদ্দকৃত" else "Assigned")
                        else (if (languageMode == LanguageMode.BANGLA) "বরাদ্দ করুন" else "Assign"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned Account(s) Breakdown
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (allocation.accountSplits.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোনো একাউন্ট নির্ধারিত নেই (ডিফল্ট হিসাব ব্যবহৃত হবে)" else "No account assigned (fallback account will be used)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        allocation.accountSplits.forEach { split ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(split.account.iconName),
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = split.account.localizedName(languageMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (allocation.isMultiAccount && split.percentageOfCategory > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${split.percentageOfCategory.toInt()}%)",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Text(
                                    text = LanguageHelper.formatCurrency(split.allocatedAmount, languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpense) SolidExpense else SolidIncome
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: OTHER ACCOUNT MULTI-SOURCE SPLIT & ASSIGNMENT
// -----------------------------------------------------------------------------

@Composable
private fun OtherAccountSplitDialog(
    otherAccountAllocation: OtherAccountAllocationAnalysis,
    paymentSourceAccounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (Map<Long, Double>) -> Unit
) {
    val totalBudget = otherAccountAllocation.totalBudgeted
    val allocMap = remember {
        val map = mutableStateMapOf<Long, String>()
        otherAccountAllocation.accountSplits.forEach { split ->
            map[split.account.id] = split.allocatedAmount.toString()
        }
        if (map.isEmpty() && paymentSourceAccounts.isNotEmpty()) {
            map[paymentSourceAccounts.first().id] = totalBudget.toString()
        }
        map
    }

    val isExpense = otherAccountAllocation.isExpense

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_other_account_split"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${if (languageMode == LanguageMode.BANGLA) "সোর্স নির্ধারণ:" else "Assign Sources:"} ${otherAccountAllocation.account.localizedName(languageMode)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isExpense) "Due / Required Amount" else "Expected Inflow"}: ${LanguageHelper.formatCurrency(totalBudget, languageMode)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(paymentSourceAccounts, key = { it.id }) { acc ->
                        val currentAmtStr = allocMap[acc.id] ?: ""
                        val isIncluded = allocMap.containsKey(acc.id)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isIncluded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = isIncluded,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                val existingCount = allocMap.size + 1
                                                val share = if (totalBudget > 0) (totalBudget / existingCount) else 0.0
                                                allocMap[acc.id] = if (share > 0) String.format("%.2f", share) else "0.00"
                                            } else {
                                                allocMap.remove(acc.id)
                                            }
                                        }
                                    )
                                    Text(
                                        text = acc.localizedName(languageMode),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (isIncluded) {
                                    OutlinedTextField(
                                        value = currentAmtStr,
                                        onValueChange = { allocMap[acc.id] = it },
                                        modifier = Modifier.width(100.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(6.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, textAlign = TextAlign.End)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val resultMap = mutableMapOf<Long, Double>()
                            allocMap.forEach { (accId, amtStr) ->
                                val amt = amtStr.toDoubleOrNull() ?: 0.0
                                if (amt > 0) resultMap[accId] = amt
                            }
                            onSave(resultMap)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save")
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: CATEGORY MULTI-ACCOUNT SPLIT
// -----------------------------------------------------------------------------

@Composable
private fun CategoryAccountSplitDialog(
    categoryAllocation: CategoryAllocationAnalysis,
    paymentSourceAccounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (Map<Long, Double>) -> Unit
) {
    val totalBudget = categoryAllocation.totalBudgeted
    val allocMap = remember {
        val map = mutableStateMapOf<Long, String>()
        categoryAllocation.accountSplits.forEach { split ->
            map[split.account.id] = split.allocatedAmount.toString()
        }
        if (map.isEmpty() && paymentSourceAccounts.isNotEmpty()) {
            map[paymentSourceAccounts.first().id] = totalBudget.toString()
        }
        map
    }

    val isExpense = categoryAllocation.isExpense

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_category_split"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${if (languageMode == LanguageMode.BANGLA) "সোর্স নির্ধারণ:" else "Assign:"} ${categoryAllocation.category.localizedName(languageMode)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isExpense) "Budget Amount" else "Expected Amount"}: ${LanguageHelper.formatCurrency(totalBudget, languageMode)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(paymentSourceAccounts, key = { it.id }) { acc ->
                        val currentAmtStr = allocMap[acc.id] ?: ""
                        val isIncluded = allocMap.containsKey(acc.id)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isIncluded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = isIncluded,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                val existingCount = allocMap.size + 1
                                                val share = if (totalBudget > 0) (totalBudget / existingCount) else 0.0
                                                allocMap[acc.id] = if (share > 0) String.format("%.2f", share) else "0.00"
                                            } else {
                                                allocMap.remove(acc.id)
                                            }
                                        }
                                    )
                                    Text(
                                        text = acc.localizedName(languageMode),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (isIncluded) {
                                    OutlinedTextField(
                                        value = currentAmtStr,
                                        onValueChange = { allocMap[acc.id] = it },
                                        modifier = Modifier.width(100.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(6.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, textAlign = TextAlign.End)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val resultMap = mutableMapOf<Long, Double>()
                            allocMap.forEach { (accId, amtStr) ->
                                val amt = amtStr.toDoubleOrNull() ?: 0.0
                                if (amt > 0) resultMap[accId] = amt
                            }
                            onSave(resultMap)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save")
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: PAYMENT SOURCE SELECTOR (MULTI-SELECT)
// -----------------------------------------------------------------------------

@Composable
private fun PaymentSourceSelectorDialog(
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    initialSelectedIds: Set<Long>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (Set<Long>) -> Unit
) {
    var selectedIds by remember { mutableStateOf(initialSelectedIds) }
    var searchQuery by remember { mutableStateOf("") }

    val balanceMap = remember(accountsWithBalances) {
        accountsWithBalances.associate { it.account.id to it.currentBalance }
    }

    val filteredAccounts = remember(allAccounts, searchQuery) {
        if (searchQuery.isBlank()) allAccounts
        else {
            val q = searchQuery.trim().lowercase()
            allAccounts.filter { it.nameEn.lowercase().contains(q) || it.nameBn.lowercase().contains(q) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_select_payment_sources"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স একাউন্ট নির্বাচন" else "Select Payment Source Accounts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র নির্বাচিত একাউন্টগুলো পেমেন্ট সোর্সে প্রদর্শিত হবে।" else "Only selected accounts will appear as payment sources.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "একাউন্ট খুঁজুন..." else "Search accounts...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredAccounts, key = { it.id }) { acc ->
                        val isChecked = selectedIds.contains(acc.id)
                        val balance = balanceMap[acc.id] ?: 0.0

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedIds = if (isChecked) selectedIds - acc.id else selectedIds + acc.id
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedIds = if (checked) selectedIds + acc.id else selectedIds - acc.id
                                        }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(SolidPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(acc.iconName),
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = acc.localizedName(languageMode), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = LanguageHelper.formatCurrency(balance, languageMode),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedIds.size} ${if (languageMode == LanguageMode.BANGLA) "টি নির্বাচিত" else "Selected"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row {
                        OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSave(selectedIds) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save")
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: LINK ACCOUNT OBLIGATION (PAYABLE / RECEIVABLE)
// -----------------------------------------------------------------------------

@Composable
private fun LinkAccountObligationDialog(
    paymentSourceAccounts: List<Account>,
    allAccounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (AccountObligation) -> Unit
) {
    var selectedSourceId by remember { mutableStateOf(paymentSourceAccounts.firstOrNull()?.id ?: 0L) }
    var selectedTargetId by remember { mutableStateOf(allAccounts.firstOrNull { it.id != selectedSourceId }?.id ?: 0L) }
    var amountText by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) } // true: Pay Payable; false: Receive Inflow
    var noteText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_link_obligation"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "অন্য একাউন্টের দেনা / পাওনা লিংক করুন" else "Link Account Payable / Receivable",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "যেমন: ব্যাংক থেকে ক্রেডিট কার্ড বিল বা দেনা পরিশোধের ব্যালেন্স ট্র্যাকিং।" else "e.g. Track paying loan/credit card due from your bank account.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type Toggle: Pay vs Receive
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isExpense,
                        onClick = { isExpense = true },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "দেনা পরিশোধ (Pay)" else "Pay Liability", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "পাওনা আদায় (Receive)" else "Receive Due", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Source Account Dropdown
                Text(text = "Payment Source Account:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyColumn(modifier = Modifier.height(80.dp)) {
                    items(paymentSourceAccounts) { acc ->
                        val isSelected = acc.id == selectedSourceId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { selectedSourceId = acc.id },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) SolidPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Text(text = acc.localizedName(languageMode), fontSize = 11.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Target Account Dropdown
                Text(text = "Target Account (Payable / Due):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyColumn(modifier = Modifier.height(80.dp)) {
                    items(allAccounts.filter { it.id != selectedSourceId }) { acc ->
                        val isSelected = acc.id == selectedTargetId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { selectedTargetId = acc.id },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) SolidTransfer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Text(text = acc.localizedName(languageMode), fontSize = 11.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Note Field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (Optional)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && selectedSourceId > 0 && selectedTargetId > 0) {
                                onSave(
                                    AccountObligation(
                                        id = java.util.UUID.randomUUID().toString(),
                                        sourceAccountId = selectedSourceId,
                                        targetAccountId = selectedTargetId,
                                        amount = amt,
                                        isExpense = isExpense,
                                        note = noteText
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save")
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: TRANSFER EXECUTION CONFIRMATION
// -----------------------------------------------------------------------------

@Composable
private fun TransferConfirmationDialog(
    suggestion: FundAllocationSuggestion,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (note: String) -> Unit
) {
    var note by remember { mutableStateOf("Payment Source Balancing Transfer") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ফান্ড ট্রান্সফার নিশ্চিতকরণ" else "Confirm Fund Transfer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = suggestion.getReason(languageMode),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(note) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার সম্পন্ন করুন" else "Execute Transfer")
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPOSABLES
// -----------------------------------------------------------------------------

@Composable
private fun BasisTabPill(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) SolidPrimary else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = message, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}
