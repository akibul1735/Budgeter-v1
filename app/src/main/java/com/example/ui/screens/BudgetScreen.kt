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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    onEditTransaction: (Transaction) -> Unit,
    onAddTransactionWithCategory: (Category) -> Unit,
    onAddTransactionWithAccount: (Account) -> Unit
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

    // 1. EXPENSES
    val expenseItems = remember(allCategories, parentCatMap) {
        allCategories.filter { it.type == CategoryType.EXPENSE && it.parentId != null }.map { cat ->
            val group = parentCatMap[cat.parentId]?.nameEn ?: "Others"
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
    val incomeItems = remember(allCategories, parentCatMap) {
        allCategories.filter { it.type == CategoryType.INCOME && it.parentId != null }.map { cat ->
            val group = parentCatMap[cat.parentId]?.nameEn ?: "Others"
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
    val assetItems = remember(allAccounts, parentAccMap) {
        allAccounts.filter { it.type == AccountType.ASSET && it.parentId != null }.map { acc ->
            val group = parentAccMap[acc.parentId]?.nameEn ?: "Assets"
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
    val liabilityItems = remember(allAccounts, parentAccMap) {
        allAccounts.filter { it.type == AccountType.LIABILITY && it.parentId != null }.map { acc ->
            val group = parentAccMap[acc.parentId]?.nameEn ?: "Liabilities"
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

    // Dashboard calculations
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

    Scaffold(
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
            // Modern Bottom Navigation Bar (Matching Expense & Income design with Assets & Liabilities)
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
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

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = index }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("budget_bottom_tab_$index")
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) activeColor else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                        onSaveBudget = { item, amount, enabled ->
                            viewModel.saveMonthlyBudget(
                                itemType = item.itemType,
                                itemId = item.id,
                                amount = amount,
                                isEnabled = enabled
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
                        onSaveBudget = { item, amount, enabled ->
                            viewModel.saveMonthlyBudget(
                                itemType = item.itemType,
                                itemId = item.id,
                                amount = amount,
                                isEnabled = enabled
                            )
                        },
                        onSaveMultiple = { budgets ->
                            viewModel.saveMultipleMonthlyBudgets(budgets)
                        }
                    )
                }
                2 -> {
                    // ASSET TAB (All features EXCEPT the "monthly" option)
                    CategoriesBudgetEntryView(
                        title = LanguageHelper.getString("assets", languageMode),
                        items = assetItems,
                        monthlyBudgets = monthlyBudgets,
                        allTransactions = transactionsWithDetails,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        languageMode = languageMode,
                        sectionColor = SolidPrimary,
                        isPeriodicFlow = false, // Assets do not have monthly dropdown
                        globalFrequency = BudgetFrequency.MONTHLY,
                        onGlobalFrequencyChange = {},
                        totalBudgetAmount = totalAssetsBudget,
                        onMonthClick = { showMonthYearPicker = true },
                        onPrevMonth = { viewModel.prevBudgetMonth() },
                        onNextMonth = { viewModel.nextBudgetMonth() },
                        onShowHelp = { showHelpDialog = true },
                        onSaveBudget = { item, amount, enabled ->
                            viewModel.saveMonthlyBudget(
                                itemType = item.itemType,
                                itemId = item.id,
                                amount = amount,
                                isEnabled = enabled
                            )
                        },
                        onSaveMultiple = { budgets ->
                            viewModel.saveMultipleMonthlyBudgets(budgets)
                        }
                    )
                }
                3 -> {
                    // LIABILITY TAB (All features EXCEPT the "monthly" option)
                    CategoriesBudgetEntryView(
                        title = LanguageHelper.getString("liabilities", languageMode),
                        items = liabilityItems,
                        monthlyBudgets = monthlyBudgets,
                        allTransactions = transactionsWithDetails,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        languageMode = languageMode,
                        sectionColor = AmberGold,
                        isPeriodicFlow = false, // Liabilities do not have monthly dropdown
                        globalFrequency = BudgetFrequency.MONTHLY,
                        onGlobalFrequencyChange = {},
                        totalBudgetAmount = totalLiabilitiesBudget,
                        onMonthClick = { showMonthYearPicker = true },
                        onPrevMonth = { viewModel.prevBudgetMonth() },
                        onNextMonth = { viewModel.nextBudgetMonth() },
                        onShowHelp = { showHelpDialog = true },
                        onSaveBudget = { item, amount, enabled ->
                            viewModel.saveMonthlyBudget(
                                itemType = item.itemType,
                                itemId = item.id,
                                amount = amount,
                                isEnabled = enabled
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
 * Main Categories & Budget Entry Screen View.
 * Matches the layout from the user's uploaded image with groups, checkboxes, sliding suggestions,
 * and period conversions.
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
    onSaveBudget: (BudgetTargetItem, Double, Boolean) -> Unit,
    onSaveMultiple: (List<MonthlyBudget>) -> Unit
) {
    // Map monthly budget items: "itemType_itemId" -> MonthlyBudget
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

    // Converted total according to active global frequency
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
        // Top Subheader (Directly matching image subheader)
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
                // Row 1: Month switcher & Help icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onMonthClick)
                            .padding(4.dp)
                    ) {
                        IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BrandBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp))
                        }
                    }

                    IconButton(onClick = onShowHelp, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "Help Guide",
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Row 2: Frequency Selector (Left) & Grand Total (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPeriodicFlow) {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showGlobalFreqDropdown = true }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = globalFrequency.localizedName(languageMode),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Frequency",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
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
                                                fontWeight = if (freq == globalFrequency) FontWeight.Bold else FontWeight.Normal
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
                        // Assets and Liabilities don't have frequency dropdown
                        Text(
                            text = "Target Balances",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Grand total on right (e.g. $10,208.00)
                    Text(
                        text = LanguageHelper.formatCurrency(displayedTotal, languageMode),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Categories List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("budget_entry_list_$title"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            groupedItems.forEach { (groupName, catItems) ->
                // Group Header (Group Name & Group Total based on selected categories on left/center, Checkbox on right)
                item(key = "group_$groupName") {
                    val allGroupEnabled = catItems.isNotEmpty() && catItems.all { item ->
                        val saved = budgetMap["${item.itemType}_${item.id}"]
                        saved?.isEnabled ?: true
                    }

                    // Calculate Group Total based ONLY on selected (enabled) categories in this group
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
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Group Name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = groupName.uppercase(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandBlue,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Right: Group Total Amount (based on selected categories) + Group Tick Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Group Total Pill
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BrandBlue.copy(alpha = 0.10f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Total: ",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = BrandBlue.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = LanguageHelper.formatCurrency(displayedGroupTotal, languageMode),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BrandBlue
                                        )
                                    }
                                }

                                // Group Toggle Checkbox:
                                // Tapping selects/ticks all categories in the group (or unselects all if all currently checked).
                                // Only checked if ALL categories in the group are selected.
                                Checkbox(
                                    checked = allGroupEnabled,
                                    onCheckedChange = { targetState ->
                                        val newEnabledState = if (allGroupEnabled) false else true
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
                                        checkedColor = BrandBlue,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Category Items in this group
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
                        onSaveBudget = { amtMonthly, isEnabled ->
                            onSaveBudget(item, amtMonthly, isEnabled)
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        thickness = 0.8.dp
                    )
                }
            }
        }
    }
}

/**
 * Individual Category / Account row matching the specifications:
 * Row 1: Icon, Name, Checkbox
 * Row 2: Frequency dropdown (for expense/income), Touch-swipeable sliding & fading amounts with pretext & dots, Manual Entry calculator button
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
    onSaveBudget: (amountMonthly: Double, isEnabled: Boolean) -> Unit
) {
    var itemFrequency by remember(globalFrequency) { mutableStateOf(globalFrequency) }
    var showFreqDropdown by remember { mutableStateOf(false) }
    var showPopupCalculator by remember { mutableStateOf(false) }

    val isEnabled = savedBudget?.isEnabled ?: true
    val currentMonthlyAmt = savedBudget?.budgetedAmount ?: item.defaultLimit

    // Displayed current amount in active frequency
    val displayedCurrentAmt = if (isPeriodicFlow) {
        itemFrequency.fromMonthly(currentMonthlyAmt)
    } else {
        currentMonthlyAmt
    }

    // Determine active suggestion index (0..n-1, or -1 for custom)
    val activeIndex = remember(currentMonthlyAmt, suggestions) {
        val found = suggestions.indexOfFirst { kotlin.math.abs(currentMonthlyAmt - it.amountMonthly) < 0.5 }
        if (found >= 0) found else -1
    }

    val pretext = when {
        activeIndex >= 0 && activeIndex < suggestions.size -> suggestions[activeIndex].pretext
        else -> "Custom"
    }

    // Indices for left, center, right suggestions in the circular carousel
    val centerIdx = if (activeIndex >= 0) activeIndex else 0
    val prevIdx = if (centerIdx <= 0) suggestions.size - 1 else centerIdx - 1
    val nextIdx = (centerIdx + 1) % suggestions.size

    val prevMonthly = suggestions.getOrNull(prevIdx)?.amountMonthly ?: 500.0
    val centerMonthly = suggestions.getOrNull(centerIdx)?.amountMonthly ?: currentMonthlyAmt
    val nextMonthly = suggestions.getOrNull(nextIdx)?.amountMonthly ?: 1500.0

    val prevDisplay = if (isPeriodicFlow) itemFrequency.fromMonthly(prevMonthly) else prevMonthly
    val nextDisplay = if (isPeriodicFlow) itemFrequency.fromMonthly(nextMonthly) else nextMonthly

    val parsedColor = remember(item.colorHex) {
        try {
            IconHelper.parseColorHex(item.colorHex)
        } catch (_: Exception) {
            sectionColor
        }
    }

    // Horizontal drag accumulator for swipe gesture detection
    var horizontalDragAccumulator by remember { mutableFloatStateOf(0f) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // ROW 1: Icon + Name + Checkbox on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Circular Icon
                    Surface(
                        shape = CircleShape,
                        color = parsedColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = IconHelper.getIconByName(item.iconName),
                                contentDescription = null,
                                tint = parsedColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = LanguageHelper.getLocalizedName(item.nameEn, item.nameBn, languageMode),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Checkbox on far right (Red/Primary checked)
                Checkbox(
                    checked = isEnabled,
                    onCheckedChange = { newState ->
                        onSaveBudget(currentMonthlyAmt, newState)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (item.itemType == "EXPENSE") SolidExpense else sectionColor,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ROW 2: Frequency Dropdown + Touch-Swipeable Sliding Amounts + Manual Entry Calculator Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Frequency dropdown (Only for Expense & Income; omitted for Assets & Liabilities)
                if (isPeriodicFlow) {
                    Box(modifier = Modifier.width(84.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showFreqDropdown = true }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = itemFrequency.localizedName(languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
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
                                            fontSize = 13.sp
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

                // 2. Touch/Swipeable Carousel (< Center > with left & right faded values)
                // Supports horizontal touch swiping (left/right drag) as well as arrow/text clicking
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .pointerInput(suggestions, activeIndex) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (horizontalDragAccumulator < -30f) {
                                        // Swiped Left -> Select Next Suggestion
                                        val targetIdx = (centerIdx + 1) % suggestions.size
                                        onSaveBudget(suggestions[targetIdx].amountMonthly, true)
                                    } else if (horizontalDragAccumulator > 30f) {
                                        // Swiped Right -> Select Previous Suggestion
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
                    // Left faded value (Clickable to switch to prev suggestion)
                    Text(
                        text = formatCompactCurrency(prevDisplay, languageMode),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                onSaveBudget(prevMonthly, true)
                            }
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        maxLines = 1
                    )

                    // Left Chevron
                    IconButton(
                        onClick = {
                            onSaveBudget(prevMonthly, true)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Suggestion",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Center Amount Column (Bold Amount + Pretext + Dynamic Indicator Dots)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = formatCompactCurrency(displayedCurrentAmt, languageMode),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )

                        // Short Pretext about the amount
                        Text(
                            text = pretext,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeIndex >= 0) (if (item.itemType == "EXPENSE") SolidExpense else sectionColor) else AmberGold,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Indicator Dots underneath for all suggestions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dotColor = if (item.itemType == "EXPENSE") SolidExpense else sectionColor
                            val inactiveDotColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

                            suggestions.forEachIndexed { idx, _ ->
                                val isSelected = activeIndex == idx
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) dotColor else inactiveDotColor,
                                    modifier = Modifier.size(if (isSelected) 4.5.dp else 3.dp)
                                ) {}
                            }
                        }
                    }

                    // Right Chevron
                    IconButton(
                        onClick = {
                            onSaveBudget(nextMonthly, true)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Suggestion",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Right faded value (Clickable to switch to next suggestion)
                    Text(
                        text = formatCompactCurrency(nextDisplay, languageMode),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                onSaveBudget(nextMonthly, true)
                            }
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }

                // 3. Manual Entry Button Box on far right (Tapping opens the Popup Calculator)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPopupCalculator = true }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                        .testTag("manual_entry_btn_${item.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Manual Entry Calculator",
                            tint = BrandBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatCompactCurrency(displayedCurrentAmt, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Popup Calculator Dialog (Positioned consistently a few rows above the bottom of the screen)
    if (showPopupCalculator) {
        PopupCalculatorDialog(
            itemName = LanguageHelper.getLocalizedName(item.nameEn, item.nameBn, languageMode),
            currentAmount = displayedCurrentAmt,
            frequency = if (isPeriodicFlow) itemFrequency else BudgetFrequency.MONTHLY,
            isPeriodicFlow = isPeriodicFlow,
            languageMode = languageMode,
            onDismiss = { showPopupCalculator = false },
            onConfirm = { enteredAmt ->
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
 * Popup Calculator Dialog for Manual Entry.
 * Positioned consistently a few rows above the bottom of the screen across all screen sizes.
 * Features full arithmetic capabilities (+, -, ×, ÷, =, C, ⌫), quick increments, live formula preview, and clear confirmation.
 */
@Composable
private fun PopupCalculatorDialog(
    itemName: String,
    currentAmount: Double,
    frequency: BudgetFrequency,
    isPeriodicFlow: Boolean,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var expression by remember {
        mutableStateOf(if (currentAmount > 0) String.format("%.0f", currentAmount) else "0")
    }

    // Safe arithmetic evaluation helper
    fun evalExpression(expr: String): Double {
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/").replace(" ", "")
            if (sanitized.isEmpty()) return 0.0

            // Simple recursive descent / operator precedence evaluator
            val tokens = mutableListOf<String>()
            var numBuf = StringBuilder()
            for (ch in sanitized) {
                if (ch.isDigit() || ch == '.') {
                    numBuf.append(ch)
                } else if (ch in "+-*/") {
                    if (numBuf.isNotEmpty()) {
                        tokens.add(numBuf.toString())
                        numBuf = StringBuilder()
                    }
                    tokens.add(ch.toString())
                }
            }
            if (numBuf.isNotEmpty()) tokens.add(numBuf.toString())
            if (tokens.isEmpty()) return 0.0

            // Pass 1: Handle * and /
            val pass1 = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val token = tokens[i]
                if ((token == "*" || token == "/") && pass1.isNotEmpty() && i + 1 < tokens.size) {
                    val prev = pass1.removeAt(pass1.size - 1).toDoubleOrNull() ?: 0.0
                    val next = tokens[i + 1].toDoubleOrNull() ?: 1.0
                    val res = if (token == "*") prev * next else (if (next != 0.0) prev / next else prev)
                    pass1.add(res.toString())
                    i += 2
                } else {
                    pass1.add(token)
                    i++
                }
            }

            // Pass 2: Handle + and -
            var total = pass1.firstOrNull()?.toDoubleOrNull() ?: 0.0
            var j = 1
            while (j < pass1.size) {
                val op = pass1[j]
                val nextVal = pass1.getOrNull(j + 1)?.toDoubleOrNull() ?: 0.0
                if (op == "+") total += nextVal
                else if (op == "-") total -= nextVal
                j += 2
            }
            total
        } catch (_: Exception) {
            0.0
        }
    }

    val calculatedResult = remember(expression) { evalExpression(expression) }

    fun appendChar(c: String) {
        if (expression == "0" && c != "." && c !in "+-×÷") {
            expression = c
        } else {
            val lastChar = expression.lastOrNull()
            if (c in "+-×÷" && lastChar != null && lastChar in "+-×÷") {
                expression = expression.dropLast(1) + c
            } else {
                expression += c
            }
        }
    }

    fun backspace() {
        if (expression.length > 1) {
            expression = expression.dropLast(1)
        } else {
            expression = "0"
        }
    }

    fun clearAll() {
        expression = "0"
    }

    fun evaluateToResult() {
        val res = evalExpression(expression)
        expression = if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.2f", res)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Position dialog floating consistently a few rows above the bottom of the screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .padding(bottom = 56.dp) // Elevated a few rows above bottom of screen
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // prevent closing when clicking inside
                    )
                    .testTag("popup_calculator_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header: Item Name + Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = itemName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isPeriodicFlow) "Manual Entry • ${frequency.localizedName(languageMode)}" else "Manual Entry • Target Balance",
                                fontSize = 11.5.sp,
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.outline)
                        }
                    }

                    // Calculator Screen / Display Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            // Formula preview
                            Text(
                                text = expression,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Large Result
                            Text(
                                text = formatCompactCurrency(calculatedResult, languageMode),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }

                    // Quick Increment Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(100, 500, 1000, 5000).forEach { inc ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        val cur = evalExpression(expression)
                                        val nextVal = (cur + inc).toLong()
                                        expression = nextVal.toString()
                                    }
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = "+$inc",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Keypad Grid (4 columns)
                    val keyRows = listOf(
                        listOf("C", "÷", "×", "⌫"),
                        listOf("7", "8", "9", "-"),
                        listOf("4", "5", "6", "+"),
                        listOf("1", "2", "3", "="),
                        listOf("0", "00", ".", "OK")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        keyRows.forEach { rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                rowKeys.forEach { key ->
                                    val isOp = key in listOf("÷", "×", "-", "+", "=")
                                    val isSpecial = key in listOf("C", "⌫")
                                    val isOk = key == "OK"

                                    val btnColor = when {
                                        isOk -> BrandBlue
                                        isOp -> BrandBlue.copy(alpha = 0.12f)
                                        isSpecial -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    }
                                    val textColor = when {
                                        isOk -> Color.White
                                        isOp -> BrandBlue
                                        isSpecial -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = btnColor,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                when (key) {
                                                    "C" -> clearAll()
                                                    "⌫" -> backspace()
                                                    "=" -> evaluateToResult()
                                                    "OK" -> {
                                                        val finalVal = evalExpression(expression)
                                                        onConfirm(finalVal)
                                                    }
                                                    else -> appendChar(key)
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (key == "⌫") {
                                                Icon(
                                                    Icons.Default.Backspace,
                                                    contentDescription = "Backspace",
                                                    tint = textColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else if (key == "OK") {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                    Text("Done", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            } else {
                                                Text(
                                                    text = key,
                                                    fontSize = if (isOp || isSpecial) 15.sp else 14.sp,
                                                    fontWeight = if (isOp || isSpecial) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Action Bar: Cancel and Set Budget
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val finalVal = evalExpression(expression)
                                onConfirm(finalVal)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            modifier = Modifier.weight(1.5f).height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Set Budget", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Informative Guide Dialog explaining smart suggestions and conversions.
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
 * Calculates smart budget suggestions for any item including:
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
    // 1. Previous Month Amount
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

    // 2. Compute 3 Frequent Amounts from transaction history / past patterns
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

    // 3. 3-Month Average
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        label = { Text("Budgeted", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        label = { Text("Actual", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }

        // Summary KPI Cards
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
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
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSurplus) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isSurplus) SolidIncome else SolidExpense,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Inflows vs Outflows Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val inVal = if (isBudgeted) totalInflows else (actualIncomes + actualAssets)
                        val outVal = if (isBudgeted) totalOutflows else (actualExpenses + actualLiabilities)

                        Column {
                            Text("Total Inflows", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(LanguageHelper.formatCurrency(inVal, languageMode), fontWeight = FontWeight.Bold, color = SolidIncome, fontSize = 14.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Outflows", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(LanguageHelper.formatCurrency(outVal, languageMode), fontWeight = FontWeight.Bold, color = SolidExpense, fontSize = 14.sp)
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
                modifier = Modifier.padding(top = 4.dp)
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    languageMode: LanguageMode,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = color.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    }
                }
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Actual: ${LanguageHelper.formatCurrency(actual, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = LanguageHelper.formatCurrency(budgeted, languageMode),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = color
                )
                val pct = if (budgeted > 0) (actual / budgeted * 100).toInt() else 0
                Text(
                    text = "$pct% utilized",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
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
