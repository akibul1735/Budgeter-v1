package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.AppTabHeader
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

private val AmberGold = Color(0xFFD97706)
private val BrandBlue = Color(0xFF1976D2)
private val BrandOrange = Color(0xFFF4511E)

/**
 * Frequency options for budgeting.
 * Periodic flows (Expense & Income) allow converting between Weekly, Monthly, Yearly, etc.
 * Stored database values are always normalized in terms of monthly amounts.
 */
enum class BudgetFrequency(val labelKey: String, val defaultLabel: String, val monthsFactor: Double) {
    WEEKLY("frequency_weekly", "Weekly", 52.0 / 12.0),
    BI_WEEKLY("frequency_bi_weekly", "Bi-weekly", 26.0 / 12.0),
    MONTHLY("frequency_monthly", "Monthly", 1.0),
    QUARTERLY("frequency_quarterly", "Quarterly", 1.0 / 3.0),
    YEARLY("frequency_yearly", "Yearly", 1.0 / 12.0);

    fun toMonthly(amountInThisFreq: Double): Double = amountInThisFreq * monthsFactor
    fun fromMonthly(monthlyAmount: Double): Double = if (monthsFactor > 0.0) monthlyAmount / monthsFactor else monthlyAmount

    fun localizedName(languageMode: LanguageMode): String =
        LanguageHelper.getString(labelKey, languageMode).ifEmpty { defaultLabel }
}

data class BudgetTargetItem(
    val id: Long,
    val nameEn: String,
    val nameBn: String,
    val groupName: String,
    val iconName: String,
    val colorHex: String,
    val itemType: String, // "EXPENSE", "INCOME", "ASSET", "LIABILITY"
    val defaultLimit: Double = 0.0
)

data class BudgetSuggestionOption(
    val index: Int,
    val amountMonthly: Double,
    val pretext: String
)

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    allCategories: List<Category>,
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    transactionsWithDetails: List<TransactionWithDetails>,
    monthlyBudgets: List<MonthlyBudget>,
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    initialTab: Int = 0,
    onOpenDrawer: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransactionWithCategory: (Category) -> Unit,
    onAddTransactionWithAccount: (Account) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var globalExpenseFrequency by remember { mutableStateOf(BudgetFrequency.MONTHLY) }
    var globalIncomeFrequency by remember { mutableStateOf(BudgetFrequency.MONTHLY) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showQuickActionSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Calculate month boundary timestamps
    val startOfMonthMs = remember(selectedYear, selectedMonth) {
        DateUtils.getStartOfMonth(selectedYear, selectedMonth)
    }
    val endOfMonthMs = remember(selectedYear, selectedMonth) {
        DateUtils.getEndOfMonth(selectedYear, selectedMonth)
    }

    // Filter transactions for selected month
    val monthTransactions = remember(transactionsWithDetails, startOfMonthMs, endOfMonthMs) {
        transactionsWithDetails.filter { it.transaction.dateEpochMs in startOfMonthMs..endOfMonthMs }
    }

    // Map monthly budget items: "itemType_itemId" -> MonthlyBudget
    val budgetMap = remember(monthlyBudgets) {
        monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }
    }

    // Prepare categorized items
    val parentCatMap = remember(allCategories) {
        allCategories.filter { it.parentId == null }.associateBy { it.id }
    }
    val parentAccMap = remember(allAccounts) {
        allAccounts.filter { it.parentId == null }.associateBy { it.id }
    }

    val parentExpenseCatIdsWithChildren = remember(allCategories) {
        allCategories.filter { it.type == CategoryType.EXPENSE && it.parentId != null }.mapNotNull { it.parentId }.toSet()
    }
    val parentIncomeCatIdsWithChildren = remember(allCategories) {
        allCategories.filter { it.type == CategoryType.INCOME && it.parentId != null }.mapNotNull { it.parentId }.toSet()
    }
    val parentAssetAccIdsWithChildren = remember(allAccounts) {
        allAccounts.filter { it.type == AccountType.ASSET && it.parentId != null }.mapNotNull { it.parentId }.toSet()
    }
    val parentLiabilityAccIdsWithChildren = remember(allAccounts) {
        allAccounts.filter { it.type == AccountType.LIABILITY && it.parentId != null }.mapNotNull { it.parentId }.toSet()
    }

    // 1. EXPENSES
    val expenseItems = remember(allCategories, parentCatMap, parentExpenseCatIdsWithChildren) {
        allCategories.filter {
            it.type == CategoryType.EXPENSE && it.isActive &&
            (it.parentId != null || !parentExpenseCatIdsWithChildren.contains(it.id))
        }.map { cat ->
            val group = if (cat.parentId != null) parentCatMap[cat.parentId]?.nameEn ?: "Expenses" else "General Expenses"
            BudgetTargetItem(
                id = cat.id,
                nameEn = cat.nameEn,
                nameBn = cat.nameBn,
                groupName = group,
                iconName = cat.iconName,
                colorHex = cat.colorHex,
                itemType = "EXPENSE",
                defaultLimit = cat.budgetLimit
            )
        }
    }

    // 2. INCOMES
    val incomeItems = remember(allCategories, parentCatMap, parentIncomeCatIdsWithChildren) {
        allCategories.filter {
            it.type == CategoryType.INCOME && it.isActive &&
            (it.parentId != null || !parentIncomeCatIdsWithChildren.contains(it.id))
        }.map { cat ->
            val group = if (cat.parentId != null) parentCatMap[cat.parentId]?.nameEn ?: "Incomes" else "General Incomes"
            BudgetTargetItem(
                id = cat.id,
                nameEn = cat.nameEn,
                nameBn = cat.nameBn,
                groupName = group,
                iconName = cat.iconName,
                colorHex = cat.colorHex,
                itemType = "INCOME",
                defaultLimit = cat.budgetLimit
            )
        }
    }

    // 3. ASSETS
    val assetItems = remember(allAccounts, parentAccMap, parentAssetAccIdsWithChildren) {
        allAccounts.filter {
            it.type == AccountType.ASSET && it.isActive &&
            (it.parentId != null || !parentAssetAccIdsWithChildren.contains(it.id))
        }.map { acc ->
            val group = if (acc.parentId != null) parentAccMap[acc.parentId]?.nameEn ?: "Accounts" else "Cash & Assets"
            BudgetTargetItem(
                id = acc.id,
                nameEn = acc.nameEn,
                nameBn = acc.nameBn,
                groupName = group,
                iconName = acc.iconName,
                colorHex = acc.colorHex,
                itemType = "ASSET",
                defaultLimit = 0.0
            )
        }
    }

    // 4. LIABILITIES
    val liabilityItems = remember(allAccounts, parentAccMap, parentLiabilityAccIdsWithChildren) {
        allAccounts.filter {
            it.type == AccountType.LIABILITY && it.isActive &&
            (it.parentId != null || !parentLiabilityAccIdsWithChildren.contains(it.id))
        }.map { acc ->
            val group = if (acc.parentId != null) parentAccMap[acc.parentId]?.nameEn ?: "Liabilities" else "Loans & Liabilities"
            BudgetTargetItem(
                id = acc.id,
                nameEn = acc.nameEn,
                nameBn = acc.nameBn,
                groupName = group,
                iconName = acc.iconName,
                colorHex = acc.colorHex,
                itemType = "LIABILITY",
                defaultLimit = 0.0
            )
        }
    }

    // Calculate totals for dashboard & subheaders
    fun calculateBudgetTotal(items: List<BudgetTargetItem>): Double {
        return items.sumOf { item ->
            val saved = budgetMap["${item.itemType}_${item.id}"]
            if (saved != null) {
                if (saved.isEnabled) saved.budgetedAmount else 0.0
            } else {
                item.defaultLimit
            }
        }
    }

    val totalExpensesBudget = remember(expenseItems, budgetMap) { calculateBudgetTotal(expenseItems) }
    val totalLiabilitiesBudget = remember(liabilityItems, budgetMap) { calculateBudgetTotal(liabilityItems) }
    val totalOutflowsBudget = totalExpensesBudget + totalLiabilitiesBudget

    val totalIncomesBudget = remember(incomeItems, budgetMap) { calculateBudgetTotal(incomeItems) }
    val totalAssetsBudget = remember(assetItems, budgetMap) { calculateBudgetTotal(assetItems) }
    val totalInflowsBudget = totalIncomesBudget + totalAssetsBudget

    val budgetedSurplus = totalInflowsBudget - totalOutflowsBudget
    val budgetedFormulaResult = (totalExpensesBudget + totalLiabilitiesBudget) - (totalAssetsBudget + totalIncomesBudget)

    val totalExpensesActual = remember(monthTransactions) {
        monthTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
    }
    val totalIncomesActual = remember(monthTransactions) {
        monthTransactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
    }
    val totalAssetsActual = remember(accountsWithBalances) {
        accountsWithBalances.filter { it.account.type == AccountType.ASSET }.sumOf { it.currentBalance }
    }
    val totalLiabilitiesActual = remember(accountsWithBalances) {
        accountsWithBalances.filter { it.account.type == AccountType.LIABILITY }.sumOf { it.currentBalance }
    }
    val totalOutflowsActual = totalExpensesActual + totalLiabilitiesActual
    val totalInflowsActual = totalIncomesActual + totalAssetsActual
    val actualSurplus = totalInflowsActual - totalOutflowsActual

    val handleBudgetItemClick: (BudgetTargetItem) -> Unit = { item ->
        if (item.itemType == "ASSET" || item.itemType == "LIABILITY") {
            val account = allAccounts.firstOrNull { it.id == item.id }
            if (account != null) {
                onAccountClick?.invoke(account)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (selectedTab in 0..3) {
                FloatingActionButton(
                    onClick = { showQuickActionSheet = true },
                    containerColor = BrandOrange,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("budget_quick_action_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = "Budget Actions"
                    )
                }
            }
        },
        bottomBar = {
            // Modern Material 3 NavigationBar with system gesture insets
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf(
                    Triple(0, LanguageHelper.getString("expenses", languageMode), Icons.Default.RemoveCircleOutline),
                    Triple(1, LanguageHelper.getString("incomes", languageMode), Icons.Default.AddCircleOutline),
                    Triple(2, LanguageHelper.getString("assets", languageMode), Icons.Default.AccountBalance),
                    Triple(3, LanguageHelper.getString("liabilities", languageMode), Icons.Default.CreditCard),
                    Triple(4, "Dashboard", Icons.Default.Dashboard)
                )

                tabs.forEach { (index, title, icon) ->
                    val isSelected = selectedTab == index
                    val activeColor = when (index) {
                        0 -> BrandOrange
                        1 -> SolidIncome
                        2 -> SolidPrimary
                        3 -> AmberGold
                        else -> MaterialTheme.colorScheme.primary
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            indicatorColor = activeColor.copy(alpha = 0.14f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.testTag("budget_bottom_tab_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("budget_screen")
        ) {
            AppTabHeader(
                title = if (languageMode == LanguageMode.BANGLA) "বাজেট মেকার" else "Budget Maker",
                showCoinIcon = false,
                onOpenDrawer = onOpenDrawer,
                onBack = null,
                actions = null
            )

            when (selectedTab) {
                0 -> {
                    // EXPENSE TAB
                    CategoriesBudgetEntryView(
                        title = LanguageHelper.getString("expenses", languageMode),
                        items = expenseItems,
                        monthlyBudgets = monthlyBudgets,
                        allTransactions = transactionsWithDetails,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        languageMode = languageMode,
                        sectionColor = SolidExpense,
                        isPeriodicFlow = true,
                        globalFrequency = globalExpenseFrequency,
                        onGlobalFrequencyChange = { globalExpenseFrequency = it },
                        totalBudgetAmount = totalExpensesBudget,
                        onMonthClick = { showMonthYearPicker = true },
                        onPrevMonth = { viewModel.prevBudgetMonth() },
                        onNextMonth = { viewModel.nextBudgetMonth() },
                        onShowHelp = { showHelpDialog = true },
                        onCopyPrevious = {
                            viewModel.copyBudgetsFromPreviousMonth()
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied previous month's budgets")
                            }
                        },
                        onItemClick = handleBudgetItemClick,
                        onSaveBudget = { item, amount, enabled ->
                            viewModel.saveBudgetAdjustment(
                                itemType = item.itemType,
                                itemId = item.id,
                                newAmount = amount,
                                isEnabled = enabled
                            )
                        },
                        onResetBudget = { item ->
                            viewModel.resetBudgetToPrevious(
                                itemType = item.itemType,
                                itemId = item.id
                            )
                        },
                        onSaveMultiple = { budgets ->
                            viewModel.saveMultipleMonthlyBudgets(budgets)
                        }
                    )
                }
                1 -> {
                    // INCOME TAB
                    CategoriesBudgetEntryView(
                        title = LanguageHelper.getString("incomes", languageMode),
                        items = incomeItems,
                        monthlyBudgets = monthlyBudgets,
                        allTransactions = transactionsWithDetails,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        languageMode = languageMode,
                        sectionColor = SolidIncome,
                        isPeriodicFlow = true,
                        globalFrequency = globalIncomeFrequency,
                        onGlobalFrequencyChange = { globalIncomeFrequency = it },
                        totalBudgetAmount = totalIncomesBudget,
                        onMonthClick = { showMonthYearPicker = true },
                        onPrevMonth = { viewModel.prevBudgetMonth() },
                        onNextMonth = { viewModel.nextBudgetMonth() },
                        onShowHelp = { showHelpDialog = true },
                        onCopyPrevious = {
                            viewModel.copyBudgetsFromPreviousMonth()
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied previous month's budgets")
                            }
                        },
                        onItemClick = handleBudgetItemClick,
                        onSaveBudget = { item, amount, enabled ->
                            viewModel.saveBudgetAdjustment(
                                itemType = item.itemType,
                                itemId = item.id,
                                newAmount = amount,
                                isEnabled = enabled
                            )
                        },
                        onResetBudget = { item ->
                            viewModel.resetBudgetToPrevious(
                                itemType = item.itemType,
                                itemId = item.id
                            )
                        },
                        onSaveMultiple = { budgets ->
                            viewModel.saveMultipleMonthlyBudgets(budgets)
                        }
                    )
                }
                2 -> {
                    // ASSET TAB
                    CategoriesBudgetEntryView(
                        title = LanguageHelper.getString("assets", languageMode),
                        items = assetItems,
                        monthlyBudgets = monthlyBudgets,
                        allTransactions = transactionsWithDetails,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        languageMode = languageMode,
                        sectionColor = SolidPrimary,
                        isPeriodicFlow = false,
                        globalFrequency = BudgetFrequency.MONTHLY,
                        onGlobalFrequencyChange = {},
                        totalBudgetAmount = totalAssetsBudget,
                        onMonthClick = { showMonthYearPicker = true },
                        onPrevMonth = { viewModel.prevBudgetMonth() },
                        onNextMonth = { viewModel.nextBudgetMonth() },
                        onShowHelp = { showHelpDialog = true },
                        onCopyPrevious = {
                            viewModel.copyBudgetsFromPreviousMonth()
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied previous month's budgets")
                            }
                        },
                        onItemClick = handleBudgetItemClick,
                        onSaveBudget = { item, amount, enabled ->
                            viewModel.saveBudgetAdjustment(
                                itemType = item.itemType,
                                itemId = item.id,
                                newAmount = amount,
                                isEnabled = enabled
                            )
                        },
                        onResetBudget = { item ->
                            viewModel.resetBudgetToPrevious(
                                itemType = item.itemType,
                                itemId = item.id
                            )
                        },
                        onSaveMultiple = { budgets ->
                            viewModel.saveMultipleMonthlyBudgets(budgets)
                        }
                    )
                }
                3 -> {
                    // LIABILITY TAB
                    CategoriesBudgetEntryView(
                        title = LanguageHelper.getString("liabilities", languageMode),
                        items = liabilityItems,
                        monthlyBudgets = monthlyBudgets,
                        allTransactions = transactionsWithDetails,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        languageMode = languageMode,
                        sectionColor = AmberGold,
                        isPeriodicFlow = false,
                        globalFrequency = BudgetFrequency.MONTHLY,
                        onGlobalFrequencyChange = {},
                        totalBudgetAmount = totalLiabilitiesBudget,
                        onMonthClick = { showMonthYearPicker = true },
                        onPrevMonth = { viewModel.prevBudgetMonth() },
                        onNextMonth = { viewModel.nextBudgetMonth() },
                        onShowHelp = { showHelpDialog = true },
                        onCopyPrevious = {
                            viewModel.copyBudgetsFromPreviousMonth()
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied previous month's budgets")
                            }
                        },
                        onItemClick = handleBudgetItemClick,
                        onSaveBudget = { item, amount, enabled ->
                            viewModel.saveBudgetAdjustment(
                                itemType = item.itemType,
                                itemId = item.id,
                                newAmount = amount,
                                isEnabled = enabled
                            )
                        },
                        onResetBudget = { item ->
                            viewModel.resetBudgetToPrevious(
                                itemType = item.itemType,
                                itemId = item.id
                            )
                        },
                        onSaveMultiple = { budgets ->
                            viewModel.saveMultipleMonthlyBudgets(budgets)
                        }
                    )
                }
                4 -> {
                    // DASHBOARD TAB
                    BudgetDashboardView(
                        year = selectedYear,
                        month = selectedMonth,
                        languageMode = languageMode,
                        totalExpenses = totalExpensesBudget,
                        totalLiabilities = totalLiabilitiesBudget,
                        totalOutflows = totalOutflowsBudget,
                        totalIncomes = totalIncomesBudget,
                        totalAssets = totalAssetsBudget,
                        totalInflows = totalInflowsBudget,
                        budgetedSurplus = budgetedSurplus,
                        budgetedFormulaResult = budgetedFormulaResult,
                        actualExpenses = totalExpensesActual,
                        actualLiabilities = totalLiabilitiesActual,
                        actualIncomes = totalIncomesActual,
                        actualAssets = totalAssetsActual,
                        actualSurplus = actualSurplus,
                        onNavigateToTab = { tabIdx -> selectedTab = tabIdx }
                    )
                }
            }
        }
    }

    // Month & Year Picker Dialog
    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            languageMode = languageMode,
            onDismiss = { showMonthYearPicker = false },
            onConfirm = { y, m ->
                viewModel.setBudgetYearMonth(y, m)
                showMonthYearPicker = false
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        BudgetHelpDialog(onDismiss = { showHelpDialog = false })
    }

    // Quick Action FAB Dialog
    if (showQuickActionSheet) {
        Dialog(onDismissRequest = { showQuickActionSheet = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Budget Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage budgets for ${DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    HorizontalDivider()

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.copyBudgetsFromPreviousMonth()
                                showQuickActionSheet = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Copied previous month's budgets")
                                }
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BrandBlue)
                            Column {
                                Text("Copy from Previous Month", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Duplicate all active categories and limits", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showQuickActionSheet = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("All budgets are saved automatically")
                                }
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = SolidIncome)
                            Column {
                                Text("Save Status", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("All adjustments are saved instantly", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showQuickActionSheet = false }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern, Compact Categories & Budget Entry Screen View.
 * Features a structured top subheader with month navigation, frequency pill, section total,
 * clean group headers with select-all toggle, and compact, unscattered budget item cards.
 */
@Composable
private fun CategoriesBudgetEntryView(
    title: String,
    items: List<BudgetTargetItem>,
    monthlyBudgets: List<MonthlyBudget>,
    allTransactions: List<TransactionWithDetails>,
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    sectionColor: Color,
    isPeriodicFlow: Boolean,
    globalFrequency: BudgetFrequency,
    onGlobalFrequencyChange: (BudgetFrequency) -> Unit,
    totalBudgetAmount: Double,
    onMonthClick: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShowHelp: () -> Unit,
    onCopyPrevious: () -> Unit,
    onItemClick: ((BudgetTargetItem) -> Unit)? = null,
    onSaveBudget: (BudgetTargetItem, Double, Boolean) -> Unit,
    onResetBudget: (BudgetTargetItem) -> Unit = {},
    onSaveMultiple: (List<MonthlyBudget>) -> Unit
) {
    val budgetMap = remember(monthlyBudgets) {
        monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }
    }

    // Precalculate suggested amounts for each item
    val suggestionsMap = remember(items, allTransactions, selectedYear, selectedMonth, monthlyBudgets) {
        items.associate { item ->
            item.id to calculateSuggestionsForItem(
                itemId = item.id,
                itemType = item.itemType,
                defaultLimit = item.defaultLimit,
                allTransactions = allTransactions,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                monthlyBudgets = monthlyBudgets
            )
        }
    }

    val groupedItems = remember(items) { items.groupBy { it.groupName } }
    var showGlobalFreqDropdown by remember { mutableStateOf(false) }

    val displayedTotal = if (isPeriodicFlow) {
        globalFrequency.fromMonthly(totalBudgetAmount)
    } else {
        totalBudgetAmount
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern, Compact Header Card
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tier 1: Month Stepper & Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month Switcher Capsule
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = sectionColor.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, sectionColor.copy(alpha = 0.25f)),
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Month",
                                    tint = sectionColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onMonthClick)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = sectionColor
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = sectionColor,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = sectionColor
                                )
                            }

                            IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Month",
                                    tint = sectionColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Quick copy previous month
                        IconButton(
                            onClick = onCopyPrevious,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy from Previous Month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onShowHelp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.HelpOutline,
                                contentDescription = "Help Guide",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Tier 2: Frequency Selector Pill (Left) & Total Target Badge (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPeriodicFlow) {
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showGlobalFreqDropdown = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = globalFrequency.localizedName(languageMode),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Frequency",
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showGlobalFreqDropdown,
                                onDismissRequest = { showGlobalFreqDropdown = false }
                            ) {
                                BudgetFrequency.values().forEach { freq ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = freq.localizedName(languageMode),
                                                fontWeight = if (freq == globalFrequency) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = {
                                            onGlobalFrequencyChange(freq)
                                            showGlobalFreqDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Target Balances",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    // Total Target Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Total:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(displayedTotal, languageMode),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = sectionColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

        // Categories & Items List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("budget_entry_list_$title"),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedItems.forEach { (groupName, catItems) ->
                // Group Header Card
                item(key = "group_$groupName") {
                    val allGroupEnabled = catItems.isNotEmpty() && catItems.all { item ->
                        val saved = budgetMap["${item.itemType}_${item.id}"]
                        saved?.isEnabled ?: true
                    }

                    val groupSelectedMonthlyTotal = catItems.filter { item ->
                        val saved = budgetMap["${item.itemType}_${item.id}"]
                        saved?.isEnabled ?: true
                    }.sumOf { item ->
                        val saved = budgetMap["${item.itemType}_${item.id}"]
                        saved?.budgetedAmount ?: item.defaultLimit
                    }

                    val displayedGroupTotal = if (isPeriodicFlow) {
                        globalFrequency.fromMonthly(groupSelectedMonthlyTotal)
                    } else {
                        groupSelectedMonthlyTotal
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Group Title + Count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(sectionColor)
                                )
                                Text(
                                    text = groupName.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "(${catItems.size})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            // Right: Group Total Pill + Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = sectionColor.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(displayedGroupTotal, languageMode),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = sectionColor,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = allGroupEnabled,
                                    onCheckedChange = { _ ->
                                        val newEnabledState = !allGroupEnabled
                                        val updatedBudgets = catItems.map { item ->
                                            val saved = budgetMap["${item.itemType}_${item.id}"]
                                            val amt = saved?.budgetedAmount ?: item.defaultLimit
                                            MonthlyBudget(
                                                year = selectedYear,
                                                month = selectedMonth,
                                                itemType = item.itemType,
                                                itemId = item.id,
                                                budgetedAmount = amt,
                                                isEnabled = newEnabledState,
                                                updatedAt = System.currentTimeMillis()
                                            )
                                        }
                                        onSaveMultiple(updatedBudgets)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = sectionColor,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Category Items
                items(catItems, key = { "item_${it.id}" }) { item ->
                    val saved = budgetMap["${item.itemType}_${item.id}"]
                    val suggestions = suggestionsMap[item.id] ?: listOf(
                        BudgetSuggestionOption(0, 500.0, "Prev Month"),
                        BudgetSuggestionOption(1, 1000.0, "Frequent 1"),
                        BudgetSuggestionOption(2, 1200.0, "Frequent 2"),
                        BudgetSuggestionOption(3, 1500.0, "Frequent 3"),
                        BudgetSuggestionOption(4, 1800.0, "3-Mo Avg")
                    )

                    BudgetItemRow(
                        item = item,
                        savedBudget = saved,
                        isPeriodicFlow = isPeriodicFlow,
                        globalFrequency = globalFrequency,
                        suggestions = suggestions,
                        languageMode = languageMode,
                        sectionColor = sectionColor,
                        onItemClick = onItemClick,
                        onSaveBudget = { amtMonthly, isEnabled ->
                            onSaveBudget(item, amtMonthly, isEnabled)
                        },
                        onResetBudget = {
                            onResetBudget(item)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Redesigned Individual Budget Item Row.
 * Structured into two stable, compact tiers to prevent font wrapping, scattering, or overflow:
 *
 * Tier 1 (Identity & Active Amount):
 * - Circular Icon, Category Name with ellipsis protection.
 * - Prominent active budget amount chip (tap to calculate) & Checkbox.
 *
 * Tier 2 (Smart Controls & Suggestions Carousel):
 * - Frequency selector dropdown (for periodic flows).
 * - Touch-swipeable carousel with Left/Right chevrons, Center Pill (Pretext & Amount), and indicator dots.
 * - Manual Entry calculator button.
 */
/**
 * Redesigned Individual Budget Item Row.
 * Structured into two stable, compact tiers to prevent font wrapping, scattering, or overflow:
 *
 * Tier 1 (Identity & Active Amount):
 * - Circular Icon, Category Name with ellipsis protection.
 * - Prominent active budget amount chip (tap to calculate) & Checkbox.
 *
 * Tier 2 (Smart Controls & Suggestions Carousel):
 * - Frequency selector dropdown (for periodic flows: Weekly, Bi-weekly, Monthly, Quarterly, Yearly).
 * - Touch-swipeable carousel with Left/Right chevrons, Center Pill (Pretext & Amount), and indicator dots.
 * - Manual Entry calculator button using the app's main PopupCalculatorDialog.
 */
@Composable
private fun BudgetItemRow(
    item: BudgetTargetItem,
    savedBudget: MonthlyBudget?,
    isPeriodicFlow: Boolean,
    globalFrequency: BudgetFrequency,
    suggestions: List<BudgetSuggestionOption>,
    languageMode: LanguageMode,
    sectionColor: Color,
    onItemClick: ((BudgetTargetItem) -> Unit)? = null,
    onSaveBudget: (amountMonthly: Double, isEnabled: Boolean) -> Unit,
    onResetBudget: () -> Unit = {}
) {
    var itemFrequency by remember(globalFrequency) { mutableStateOf(globalFrequency) }
    var showFreqDropdown by remember { mutableStateOf(false) }
    var showPopupCalculator by remember { mutableStateOf(false) }

    val isEnabled = savedBudget?.isEnabled ?: true
    val currentMonthlyAmt = savedBudget?.budgetedAmount ?: item.defaultLimit

    val displayedCurrentAmt = if (isPeriodicFlow) {
        itemFrequency.fromMonthly(currentMonthlyAmt)
    } else {
        currentMonthlyAmt
    }

    // Determine active suggestion index
    val activeIndex = remember(currentMonthlyAmt, suggestions) {
        val found = suggestions.indexOfFirst { kotlin.math.abs(currentMonthlyAmt - it.amountMonthly) < 0.5 }
        if (found >= 0) found else -1
    }

    val pretext = when {
        activeIndex >= 0 && activeIndex < suggestions.size -> suggestions[activeIndex].pretext
        else -> "Custom"
    }

    val centerIdx = if (activeIndex >= 0) activeIndex else 0
    val prevIdx = if (centerIdx <= 0) suggestions.size - 1 else centerIdx - 1
    val nextIdx = (centerIdx + 1) % suggestions.size

    val prevMonthly = suggestions.getOrNull(prevIdx)?.amountMonthly ?: 500.0
    val nextMonthly = suggestions.getOrNull(nextIdx)?.amountMonthly ?: 1500.0

    val parsedColor = remember(item.colorHex) {
        try {
            IconHelper.parseColorHex(item.colorHex)
        } catch (_: Exception) {
            sectionColor
        }
    }

    var horizontalDragAccumulator by remember { mutableFloatStateOf(0f) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // TIER 1: Category Info & Target Amount Display + Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onItemClick?.invoke(item) }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = parsedColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = IconHelper.getIconByName(item.iconName),
                                contentDescription = null,
                                tint = parsedColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = LanguageHelper.getLocalizedName(item.nameEn, item.nameBn, languageMode),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Amount Display Pill & Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clickable Amount Capsule (opens calculator)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isEnabled) sectionColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isEnabled) sectionColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPopupCalculator = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formatCompactCurrency(displayedCurrentAmt, languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (isEnabled) sectionColor else MaterialTheme.colorScheme.outline,
                                maxLines = 1
                            )
                        }
                    }

                    Checkbox(
                        checked = isEnabled,
                        onCheckedChange = { newState ->
                            onSaveBudget(currentMonthlyAmt, newState)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (item.itemType == "EXPENSE") SolidExpense else sectionColor,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Budget Adjustment Banner (if amount adjusted from previous month)
            val prevAmt = savedBudget?.previousAmount
            if (prevAmt != null && kotlin.math.abs(prevAmt - currentMonthlyAmt) > 0.01) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = "${LanguageHelper.getString("previous_budget", languageMode)}: ${formatCompactCurrency(prevAmt, languageMode)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "➔", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "${LanguageHelper.getString("adjusted_budget", languageMode)}: ${formatCompactCurrency(currentMonthlyAmt, languageMode)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onResetBudget() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("reset_to_previous_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = LanguageHelper.getString("reset_to_previous", languageMode),
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // TIER 2: Frequency Selector (Left) + Smart Stepper / Carousel (Center) + Calculator Button (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Frequency selector pill (For periodic flows)
                if (isPeriodicFlow) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showFreqDropdown = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = itemFrequency.localizedName(languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFreqDropdown,
                            onDismissRequest = { showFreqDropdown = false }
                        ) {
                            BudgetFrequency.values().forEach { freq ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = freq.localizedName(languageMode),
                                            fontWeight = if (freq == itemFrequency) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {
                                        itemFrequency = freq
                                        showFreqDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // 2. Center Suggestion Stepper & Swipe Carousel
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .pointerInput(suggestions, activeIndex) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (horizontalDragAccumulator < -25f) {
                                        val targetIdx = (centerIdx + 1) % suggestions.size
                                        onSaveBudget(suggestions[targetIdx].amountMonthly, true)
                                    } else if (horizontalDragAccumulator > 25f) {
                                        val targetIdx = if (centerIdx <= 0) suggestions.size - 1 else centerIdx - 1
                                        onSaveBudget(suggestions[targetIdx].amountMonthly, true)
                                    }
                                    horizontalDragAccumulator = 0f
                                },
                                onDragCancel = {
                                    horizontalDragAccumulator = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    horizontalDragAccumulator += dragAmount
                                }
                            )
                        }
                ) {
                    IconButton(
                        onClick = { onSaveBudget(prevMonthly, true) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Suggestion",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Suggestion Capsule (Pretext + Micro Indicator Dots)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                // Tap cycles to next suggestion
                                val targetIdx = (centerIdx + 1) % suggestions.size
                                onSaveBudget(suggestions[targetIdx].amountMonthly, true)
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = pretext,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeIndex >= 0) sectionColor else AmberGold,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Indicator Dots
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dotColor = sectionColor
                                val inactiveDotColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

                                suggestions.forEachIndexed { idx, _ ->
                                    val isSelected = activeIndex == idx
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) dotColor else inactiveDotColor,
                                        modifier = Modifier.size(if (isSelected) 4.dp else 2.5.dp)
                                    ) {}
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { onSaveBudget(nextMonthly, true) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Suggestion",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 3. Calculator Manual Entry Button (Main app calculator)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showPopupCalculator = true }
                        .testTag("manual_entry_btn_${item.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculator Entry",
                            tint = BrandBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Calc",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Main App Popup Calculator Dialog
    if (showPopupCalculator) {
        PopupCalculatorDialog(
            initialValue = displayedCurrentAmt,
            languageMode = languageMode,
            onDismiss = { showPopupCalculator = false },
            onValueConfirmed = { enteredAmt ->
                val monthlyToSave = if (isPeriodicFlow) {
                    itemFrequency.toMonthly(enteredAmt)
                } else {
                    enteredAmt
                }
                onSaveBudget(monthlyToSave, true)
                showPopupCalculator = false
            }
        )
    }
}

/**
 * Informative Guide Dialog.
 */
@Composable
private fun BudgetHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories & Budget Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Sliding & Fading Suggestions:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Swipe left/right or tap < or > to slide between Previous Month actuals, 3 Frequent suggestions, and 3-Month Averages.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "• Frequency Conversions:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Select Weekly, Bi-weekly, Monthly, Quarterly, or Yearly. All amounts are automatically converted and saved in terms of monthly budgets.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "• Assets & Liabilities:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Assets and liabilities support smart sliding suggestions and direct manual calculator entries, without periodic frequency conversions.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) {
                        Text("Got It")
                    }
                }
            }
        }
    }
}

/**
 * Calculates smart budget suggestions for any item:
 * 1. Previous Month actual spending / balance
 * 2. 3 Frequent suggestions (Frequent 1, Frequent 2, Frequent 3)
 * 3. 3-Month Average
 */
private fun calculateSuggestionsForItem(
    itemId: Long,
    itemType: String,
    defaultLimit: Double,
    allTransactions: List<TransactionWithDetails>,
    selectedYear: Int,
    selectedMonth: Int,
    monthlyBudgets: List<MonthlyBudget>
): List<BudgetSuggestionOption> {
    var prevYear = selectedYear
    var prevMonth = selectedMonth - 1
    if (prevMonth < 1) {
        prevMonth = 12
        prevYear -= 1
    }
    val prevMonthStart = DateUtils.getStartOfMonth(prevYear, prevMonth)
    val prevMonthEnd = DateUtils.getEndOfMonth(prevYear, prevMonth)

    val prevMonthTxs = allTransactions.filter {
        it.transaction.dateEpochMs in prevMonthStart..prevMonthEnd &&
                (it.transaction.categoryId == itemId || it.transaction.subCategoryId == itemId ||
                 it.transaction.debitAccountId == itemId || it.transaction.creditAccountId == itemId)
    }
    val prevMonthActual = prevMonthTxs.sumOf { it.transaction.amount }

    val prevMonthAmt = if (prevMonthActual > 0.0) {
        prevMonthActual
    } else {
        val prevSaved = monthlyBudgets.find { it.year == prevYear && it.month == prevMonth && it.itemType == itemType && it.itemId == itemId }
        if (prevSaved != null && prevSaved.budgetedAmount > 0.0) {
            prevSaved.budgetedAmount
        } else if (defaultLimit > 0.0) {
            defaultLimit * 0.9
        } else {
            500.0
        }
    }

    val allItemTxs = allTransactions.filter {
        it.transaction.categoryId == itemId || it.transaction.subCategoryId == itemId ||
                it.transaction.debitAccountId == itemId || it.transaction.creditAccountId == itemId
    }

    val cal = Calendar.getInstance()
    val monthlyTotals = if (allItemTxs.isNotEmpty()) {
        allItemTxs.groupBy {
            cal.timeInMillis = it.transaction.dateEpochMs
            "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}"
        }.values.map { txList -> txList.sumOf { it.transaction.amount } }
    } else emptyList()

    val freqCounts = monthlyTotals.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }

    val baseFreq = if (freqCounts.isNotEmpty()) freqCounts[0].first else if (defaultLimit > 0) defaultLimit else 1000.0
    val freq1 = baseFreq
    val freq2 = if (freqCounts.size > 1) {
        freqCounts[1].first
    } else {
        (baseFreq * 1.25).roundToInt().toDouble()
    }
    val freq3 = if (freqCounts.size > 2) {
        freqCounts[2].first
    } else {
        (baseFreq * 0.75).roundToInt().coerceAtLeast(100).toDouble()
    }

    val threeMonthStart = DateUtils.getStartOfMonth(
        if (selectedMonth > 3) selectedYear else selectedYear - 1,
        if (selectedMonth > 3) selectedMonth - 3 else selectedMonth + 9
    )
    val threeMonthTxs = allTransactions.filter {
        it.transaction.dateEpochMs in threeMonthStart..DateUtils.getEndOfMonth(selectedYear, selectedMonth) &&
                (it.transaction.categoryId == itemId || it.transaction.subCategoryId == itemId ||
                 it.transaction.debitAccountId == itemId || it.transaction.creditAccountId == itemId)
    }
    val threeMonthGrouped = threeMonthTxs.groupBy {
        cal.timeInMillis = it.transaction.dateEpochMs
        "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}"
    }.values.map { it.sumOf { tx -> tx.transaction.amount } }

    val avgAmt = if (threeMonthGrouped.isNotEmpty()) {
        threeMonthGrouped.average()
    } else if (defaultLimit > 0.0) {
        defaultLimit * 1.15
    } else {
        1500.0
    }

    return listOf(
        BudgetSuggestionOption(0, prevMonthAmt, "Prev Month"),
        BudgetSuggestionOption(1, freq1, "Frequent 1"),
        BudgetSuggestionOption(2, freq2, "Frequent 2"),
        BudgetSuggestionOption(3, freq3, "Frequent 3"),
        BudgetSuggestionOption(4, avgAmt, "3-Mo Avg")
    )
}

/**
 * Clean compact currency formatting.
 */
private fun formatCompactCurrency(amount: Double, languageMode: LanguageMode): String {
    return if (amount % 1.0 == 0.0) {
        val whole = amount.toLong().toString()
        val numStr = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(whole) else whole
        "৳$numStr"
    } else {
        LanguageHelper.formatCurrency(amount, languageMode)
    }
}

/* -------------------------------------------------------------
   TAB 4: BUDGET DASHBOARD & ANALYTICS
   ------------------------------------------------------------- */
@Composable
private fun BudgetDashboardView(
    year: Int,
    month: Int,
    languageMode: LanguageMode,
    totalExpenses: Double,
    totalLiabilities: Double,
    totalOutflows: Double,
    totalIncomes: Double,
    totalAssets: Double,
    totalInflows: Double,
    budgetedSurplus: Double,
    budgetedFormulaResult: Double,
    actualExpenses: Double,
    actualLiabilities: Double,
    actualIncomes: Double,
    actualAssets: Double,
    actualSurplus: Double,
    onNavigateToTab: (Int) -> Unit
) {
    var viewMode by remember { mutableIntStateOf(0) } // 0 = Budgeted Plan, 1 = Actual Realized

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budget_dashboard_view"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top KPI Banner Switcher (Budgeted vs Actual)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        label = { Text("Budgeted", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        label = { Text("Actual", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }

        // Summary KPI Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isBudgeted = viewMode == 0
                    val currentSurplus = if (isBudgeted) budgetedSurplus else actualSurplus
                    val isSurplus = currentSurplus >= 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBudgeted) "Planned Surplus / Deficit" else "Realized Net Cashflow",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = (if (isSurplus) "+" else "") + LanguageHelper.formatCurrency(currentSurplus, languageMode),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSurplus) SolidIncome else SolidExpense
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = (if (isSurplus) SolidIncome else SolidExpense).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSurplus) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isSurplus) SolidIncome else SolidExpense,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Inflows vs Outflows Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val inVal = if (isBudgeted) totalInflows else (actualIncomes + actualAssets)
                        val outVal = if (isBudgeted) totalOutflows else (actualExpenses + actualLiabilities)

                        Column {
                            Text("Total Inflows", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = LanguageHelper.formatCurrency(inVal, languageMode),
                                fontWeight = FontWeight.Bold,
                                color = SolidIncome,
                                fontSize = 14.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Outflows", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = LanguageHelper.formatCurrency(outVal, languageMode),
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 4 Category Breakdowns
        item {
            Text(
                text = "Budget Categories",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardBreakdownCard(
                    title = LanguageHelper.getString("expenses", languageMode),
                    budgeted = totalExpenses,
                    actual = actualExpenses,
                    color = SolidExpense,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    languageMode = languageMode,
                    onClick = { onNavigateToTab(0) }
                )
                DashboardBreakdownCard(
                    title = LanguageHelper.getString("incomes", languageMode),
                    budgeted = totalIncomes,
                    actual = actualIncomes,
                    color = SolidIncome,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    languageMode = languageMode,
                    onClick = { onNavigateToTab(1) }
                )
                DashboardBreakdownCard(
                    title = LanguageHelper.getString("assets", languageMode),
                    budgeted = totalAssets,
                    actual = actualAssets,
                    color = SolidPrimary,
                    icon = Icons.Default.AccountBalance,
                    languageMode = languageMode,
                    onClick = { onNavigateToTab(2) }
                )
                DashboardBreakdownCard(
                    title = LanguageHelper.getString("liabilities", languageMode),
                    budgeted = totalLiabilities,
                    actual = actualLiabilities,
                    color = AmberGold,
                    icon = Icons.Default.CreditCard,
                    languageMode = languageMode,
                    onClick = { onNavigateToTab(3) }
                )
            }
        }
    }
}

@Composable
private fun DashboardBreakdownCard(
    title: String,
    budgeted: Double,
    actual: Double,
    color: Color,
    icon: ImageVector,
    languageMode: LanguageMode,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = CircleShape, color = color.copy(alpha = 0.14f), modifier = Modifier.size(34.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
                        }
                    }
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, maxLines = 1)
                        Text(
                            text = "Actual: ${LanguageHelper.formatCurrency(actual, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = LanguageHelper.formatCurrency(budgeted, languageMode),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = color,
                        maxLines = 1
                    )
                    val pct = if (budgeted > 0) ((actual / budgeted) * 100).toInt() else 0
                    Text(
                        text = "$pct% utilized",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }

            // Allocation progress bar
            val progress = if (budgeted > 0) ((actual / budgeted).toFloat()).coerceIn(0f, 1f) else 0f
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

/* -------------------------------------------------------------
   MONTH & YEAR PICKER DIALOG
   ------------------------------------------------------------- */
@Composable
private fun MonthYearPickerDialog(
    currentYear: Int,
    currentMonth: Int,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    var tempYear by remember { mutableIntStateOf(currentYear) }
    var tempMonth by remember { mutableIntStateOf(currentMonth) }

    val monthNamesEn = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("month_year_picker_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Month & Year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { tempYear -= 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Year")
                    }
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(tempYear.toString()) else tempYear.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { tempYear += 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0..3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0..2) {
                                val m = row * 3 + col + 1
                                val isSelected = tempMonth == m
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { tempMonth = m }
                                        .padding(vertical = 12.dp),
                                    tonalElevation = if (isSelected) 3.dp else 0.dp
                                ) {
                                    Text(
                                        text = monthNamesEn[m - 1],
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(tempYear, tempMonth) },
                        modifier = Modifier.testTag("month_picker_confirm")
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}
