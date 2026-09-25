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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
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
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountObligationAnalysis
import com.example.data.model.AccountRequirementAnalysis
import com.example.data.model.AccountRequirementItem
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryAccountSplit
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
    accountCalcConfig: com.example.util.AccountCalcConfig = com.example.util.AccountCalcConfig(),
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
    var paymentSourceSortOption by remember { mutableStateOf(PaymentSourceSortOption.DEFAULT) }

    // Assigned items tab filters and sorting
    var assignedSectionFilter by remember { mutableStateOf(AssignedItemSectionFilter.ALL) }
    var assignedStatusFilter by remember { mutableStateOf(AssignedItemStatusFilter.ALL) }
    var assignedItemSortOption by remember { mutableStateOf(AssignedItemSortOption.DEFAULT) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialogs state
    var showSourceSelectorDialog by remember { mutableStateOf(false) }
    var showCategorySplitDialog by remember { mutableStateOf<CategoryAllocationAnalysis?>(null) }
    var showOtherAccountSplitDialog by remember { mutableStateOf<OtherAccountAllocationAnalysis?>(null) }
    var showAddObligationDialog by remember { mutableStateOf(false) }
    var presetSourceAccountIdForObligation by remember { mutableStateOf<Long?>(null) }
    var showAssignItemChoiceDialogForAccount by remember { mutableStateOf<Account?>(null) }
    var showSelectExpenseForAccount by remember { mutableStateOf<Account?>(null) }
    var showSelectIncomeForAccount by remember { mutableStateOf<Account?>(null) }
    var showSuggestedTransfersDialog by remember { mutableStateOf(false) }
    var filterAccountIdForAssignedItems by remember { mutableStateOf<Long?>(null) }
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
        paymentSourceConfig.accountObligations,
        accountCalcConfig
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
            accountObligations = paymentSourceConfig.accountObligations,
            accountCalcConfig = accountCalcConfig
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
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                showSearchButton = true,
                searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স বা আইটেম খুঁজুন..." else "Search payment sources or items..."
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
                        searchQuery = searchQuery,
                        calculationBasis = calculationBasis,
                        onCalculationBasisChange = { calculationBasis = it },
                        accountStatusFilter = accountStatusFilter,
                        onStatusFilterChange = { accountStatusFilter = it },
                        sortOption = paymentSourceSortOption,
                        onSortOptionChange = { paymentSourceSortOption = it },
                        onOpenSourceSelector = { showSourceSelectorDialog = true },
                        onOpenSuggestedTransfers = { showSuggestedTransfersDialog = true },
                        onOpenAddObligation = {
                            presetSourceAccountIdForObligation = null
                            showAddObligationDialog = true
                        },
                        onExecuteTransferSuggestion = { transferSuggestionToExecute = it },
                        onAccountClick = { accId ->
                            filterAccountIdForAssignedItems = accId
                            selectedTab = MainPaymentSourceTab.ASSIGNED_ITEMS
                        },
                        onAssignItem = { acc ->
                            showAssignItemChoiceDialogForAccount = acc
                        },
                        onDeleteObligation = onDeleteAccountObligation
                    )
                }
                MainPaymentSourceTab.ASSIGNED_ITEMS -> {
                    AssignedItemsTabContent(
                        overview = analysisOverview,
                        languageMode = languageMode,
                        selectedAccountId = filterAccountIdForAssignedItems,
                        onClearAccountFilter = { filterAccountIdForAssignedItems = null },
                        onSelectAccountFilter = { filterAccountIdForAssignedItems = it },
                        allPaymentSourceAccounts = analysisOverview.accountAnalyses.map { it.account },
                        allAccounts = allAccounts,
                        allCategories = allCategories,
                        sectionFilter = assignedSectionFilter,
                        onSectionFilterChange = { assignedSectionFilter = it },
                        statusFilter = assignedStatusFilter,
                        onStatusFilterChange = { assignedStatusFilter = it },
                        sortOption = assignedItemSortOption,
                        onSortOptionChange = { assignedItemSortOption = it },
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
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
                initialSourceAccountId = presetSourceAccountIdForObligation,
                languageMode = languageMode,
                onDismiss = {
                    showAddObligationDialog = false
                    presetSourceAccountIdForObligation = null
                },
                onSave = { obligation ->
                    onSaveAccountObligation(obligation)
                    showAddObligationDialog = false
                    presetSourceAccountIdForObligation = null
                }
            )
        }

        // 5. Assign Item Choice Dialog (Select Other Account, Income, or Expense)
        showAssignItemChoiceDialogForAccount?.let { account ->
            AssignItemChoiceDialog(
                account = account,
                languageMode = languageMode,
                onDismiss = { showAssignItemChoiceDialogForAccount = null },
                onSelectOtherAccount = {
                    presetSourceAccountIdForObligation = account.id
                    showAddObligationDialog = true
                },
                onSelectExpense = {
                    showSelectExpenseForAccount = account
                },
                onSelectIncome = {
                    showSelectIncomeForAccount = account
                }
            )
        }

        // 6. Select Expense Category Dialog
        showSelectExpenseForAccount?.let { account ->
            SelectCategoryForAccountDialog(
                targetAccount = account,
                availableCategories = allCategories,
                currentAllocations = analysisOverview.categoryAllocations,
                isExpense = true,
                languageMode = languageMode,
                onDismiss = { showSelectExpenseForAccount = null },
                onCategorySelected = { selectedAlloc ->
                    showSelectExpenseForAccount = null
                    showCategorySplitDialog = selectedAlloc
                }
            )
        }

        // 7. Select Income Category Dialog
        showSelectIncomeForAccount?.let { account ->
            SelectCategoryForAccountDialog(
                targetAccount = account,
                availableCategories = allCategories,
                currentAllocations = analysisOverview.incomeAllocations,
                isExpense = false,
                languageMode = languageMode,
                onDismiss = { showSelectIncomeForAccount = null },
                onCategorySelected = { selectedAlloc ->
                    showSelectIncomeForAccount = null
                    showCategorySplitDialog = selectedAlloc
                }
            )
        }

        // 8. Suggested Fund Transfers Dialog
        if (showSuggestedTransfersDialog) {
            SuggestedFundTransfersDialog(
                transferSuggestions = analysisOverview.transferSuggestions,
                languageMode = languageMode,
                onDismiss = { showSuggestedTransfersDialog = false },
                onExecuteTransfer = { suggestion ->
                    showSuggestedTransfersDialog = false
                    transferSuggestionToExecute = suggestion
                }
            )
        }

        // 9. Quick Transfer Execution Confirmation
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

