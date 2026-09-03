package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.example.ui.theme.SolidTransfer
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar

private val AmberGold = Color(0xFFD97706)
private val DarkCyan = Color(0xFF0D9488)

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
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var editingBudgetItem by remember { mutableStateOf<BudgetTargetItem?>(null) }
    var editingBudgetAmount by remember { mutableDoubleStateOf(0.0) }
    var editingBudgetEnabled by remember { mutableStateOf(true) }

    val currentCal = remember { Calendar.getInstance() }
    val isCurrentMonth = selectedYear == currentCal.get(Calendar.YEAR) &&
            selectedMonth == (currentCal.get(Calendar.MONTH) + 1)

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

    // Calculate totals for dashboard
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

    // Actuals from transactions and balances
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budget_screen")
    ) {
        // Top Month-Year Navigation Bar
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.prevBudgetMonth() },
                        modifier = Modifier.testTag("budget_prev_month")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }

                    // Clickable Month Title
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showMonthYearPicker = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("budget_month_year_selector")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isCurrentMonth) {
                            IconButton(
                                onClick = {
                                    val now = Calendar.getInstance()
                                    viewModel.setBudgetYearMonth(
                                        now.get(Calendar.YEAR),
                                        now.get(Calendar.MONTH) + 1
                                    )
                                },
                                modifier = Modifier.testTag("budget_jump_today")
                            ) {
                                Icon(
                                    Icons.Default.Today,
                                    contentDescription = "Current Month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.nextBudgetMonth() },
                            modifier = Modifier.testTag("budget_next_month")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }
                }

                // Quick copy hint if month has no custom budgets yet
                if (monthlyBudgets.isEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.copyBudgetsFromPreviousMonth() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "No custom budgets set for this month yet. Tap to copy from previous month",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 5 TABS: Dashboard, Expenses, Incomes, Assets, Liabilities
        val tabs = listOf(
            TabInfo("Dashboard", Icons.Default.Dashboard, MaterialTheme.colorScheme.primary),
            TabInfo(LanguageHelper.getString("expenses", languageMode), Icons.AutoMirrored.Filled.TrendingDown, SolidExpense),
            TabInfo(LanguageHelper.getString("incomes", languageMode), Icons.AutoMirrored.Filled.TrendingUp, SolidIncome),
            TabInfo(LanguageHelper.getString("assets", languageMode), Icons.Default.AccountBalance, SolidPrimary),
            TabInfo(LanguageHelper.getString("liabilities", languageMode), Icons.Default.CreditCard, AmberGold)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = tabs[selectedTab].color,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("budget_5_tabs")
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("budget_tab_$index"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSelected) tab.color else MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) tab.color else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> BudgetDashboardView(
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
                1 -> BudgetPurposeCategoryList(
                    title = LanguageHelper.getString("expenses", languageMode),
                    items = expenseItems,
                    budgetMap = budgetMap,
                    monthTransactions = monthTransactions,
                    languageMode = languageMode,
                    sectionColor = SolidExpense,
                    onEditBudget = { item, currentAmt, enabled ->
                        editingBudgetItem = item
                        editingBudgetAmount = currentAmt
                        editingBudgetEnabled = enabled
                    },
                    onEditTransaction = onEditTransaction,
                    onAddNewTransaction = { item ->
                        val cat = allCategories.find { it.id == item.id }
                        if (cat != null) onAddTransactionWithCategory(cat)
                    }
                )
                2 -> BudgetPurposeCategoryList(
                    title = LanguageHelper.getString("incomes", languageMode),
                    items = incomeItems,
                    budgetMap = budgetMap,
                    monthTransactions = monthTransactions,
                    languageMode = languageMode,
                    sectionColor = SolidIncome,
                    onEditBudget = { item, currentAmt, enabled ->
                        editingBudgetItem = item
                        editingBudgetAmount = currentAmt
                        editingBudgetEnabled = enabled
                    },
                    onEditTransaction = onEditTransaction,
                    onAddNewTransaction = { item ->
                        val cat = allCategories.find { it.id == item.id }
                        if (cat != null) onAddTransactionWithCategory(cat)
                    }
                )
                3 -> BudgetPurposeAccountList(
                    title = LanguageHelper.getString("assets", languageMode),
                    items = assetItems,
                    budgetMap = budgetMap,
                    monthTransactions = monthTransactions,
                    accountsWithBalances = accountsWithBalances,
                    languageMode = languageMode,
                    sectionColor = SolidPrimary,
                    onEditBudget = { item, currentAmt, enabled ->
                        editingBudgetItem = item
                        editingBudgetAmount = currentAmt
                        editingBudgetEnabled = enabled
                    },
                    onEditTransaction = onEditTransaction,
                    onAddNewTransaction = { item ->
                        val acc = allAccounts.find { it.id == item.id }
                        if (acc != null) onAddTransactionWithAccount(acc)
                    }
                )
                4 -> BudgetPurposeAccountList(
                    title = LanguageHelper.getString("liabilities", languageMode),
                    items = liabilityItems,
                    budgetMap = budgetMap,
                    monthTransactions = monthTransactions,
                    accountsWithBalances = accountsWithBalances,
                    languageMode = languageMode,
                    sectionColor = AmberGold,
                    onEditBudget = { item, currentAmt, enabled ->
                        editingBudgetItem = item
                        editingBudgetAmount = currentAmt
                        editingBudgetEnabled = enabled
                    },
                    onEditTransaction = onEditTransaction,
                    onAddNewTransaction = { item ->
                        val acc = allAccounts.find { it.id == item.id }
                        if (acc != null) onAddTransactionWithAccount(acc)
                    }
                )
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

    // Edit Budget Dialog
    if (editingBudgetItem != null) {
        val item = editingBudgetItem!!
        EditBudgetDialog(
            item = item,
            initialAmount = editingBudgetAmount,
            initialEnabled = editingBudgetEnabled,
            year = selectedYear,
            month = selectedMonth,
            languageMode = languageMode,
            onDismiss = { editingBudgetItem = null },
            onSave = { amount, enabled ->
                viewModel.saveMonthlyBudget(
                    itemType = item.itemType,
                    itemId = item.id,
                    amount = amount,
                    isEnabled = enabled
                )
                editingBudgetItem = null
            }
        )
    }
}

private data class TabInfo(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

/* -------------------------------------------------------------
   TAB 0: BUDGET DASHBOARD
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Outflows Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SolidExpense.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(18.dp))
                            Text("Outflows (-)", fontSize = 12.sp, color = SolidExpense, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val outflowVal = if (viewMode == 0) totalOutflows else (actualExpenses + actualLiabilities)
                        Text(
                            text = LanguageHelper.formatCurrency(outflowVal, languageMode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SolidExpense
                        )
                        Text(
                            text = "Expenses + Liabilities",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Inflows Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SolidIncome.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(18.dp))
                            Text("Inflows (+)", fontSize = 12.sp, color = SolidIncome, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val inflowVal = if (viewMode == 0) totalInflows else (actualIncomes + actualAssets)
                        Text(
                            text = LanguageHelper.formatCurrency(inflowVal, languageMode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SolidIncome
                        )
                        Text(
                            text = "Incomes + Assets",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Net Surplus Banner
        item {
            val netVal = if (viewMode == 0) budgetedSurplus else actualSurplus
            val isSurplus = netVal >= 0
            val containerColor = if (isSurplus) DarkCyan.copy(alpha = 0.12f) else SolidExpense.copy(alpha = 0.12f)
            val textColor = if (isSurplus) DarkCyan else SolidExpense

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSurplus) "» Net Surplus" else "» Net Deficit",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = if (viewMode == 0) "Budgeted Net for Month" else "Actual Net Difference",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = "${if (isSurplus) "⊕ " else "- "}${LanguageHelper.formatCurrency(Math.abs(netVal), languageMode)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                }
            }
        }

        // EXACT APPSHEET TABLE FROM SCREENSHOT
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("budget_table_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    // Header: Type | Total Amount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Type",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (viewMode == 0) "Total Amount (Budgeted)" else "Total Amount (Actual)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Row 1: ⯆ Total Expenses (tap -> tab 1)
                    val dispExpenses = if (viewMode == 0) totalExpenses else actualExpenses
                    BudgetTableRow(
                        iconSymbol = "⯆",
                        title = "Total Expenses",
                        amountText = "-${LanguageHelper.formatCurrency(dispExpenses, languageMode)}",
                        color = SolidExpense,
                        isClickable = true,
                        onClick = { onNavigateToTab(1) }
                    )

                    // Row 2: ⯆ Total Liabilities (tap -> tab 4)
                    val dispLiabilities = if (viewMode == 0) totalLiabilities else actualLiabilities
                    BudgetTableRow(
                        iconSymbol = "⯆",
                        title = "Total Liabilities",
                        amountText = "-${LanguageHelper.formatCurrency(dispLiabilities, languageMode)}",
                        color = AmberGold,
                        isClickable = true,
                        onClick = { onNavigateToTab(4) }
                    )

                    // Row 3: (-) Subtotal Outflows (Gold/Amber styled)
                    val dispOutflows = if (viewMode == 0) totalOutflows else (actualExpenses + actualLiabilities)
                    BudgetTableSubtotalRow(
                        prefix = "(-)",
                        amountText = "-${LanguageHelper.formatCurrency(dispOutflows, languageMode)}",
                        color = AmberGold
                    )

                    // Divider Row
                    BudgetTableDividerRow()

                    // Row 4: ⯅ Total Incomes (tap -> tab 2)
                    val dispIncomes = if (viewMode == 0) totalIncomes else actualIncomes
                    BudgetTableRow(
                        iconSymbol = "⯅",
                        title = "Total Incomes",
                        amountText = LanguageHelper.formatCurrency(dispIncomes, languageMode),
                        color = SolidIncome,
                        isClickable = true,
                        onClick = { onNavigateToTab(2) }
                    )

                    // Row 5: ⯅ Total Assets (tap -> tab 3)
                    val dispAssets = if (viewMode == 0) totalAssets else actualAssets
                    BudgetTableRow(
                        iconSymbol = "⯅",
                        title = "Total Assets",
                        amountText = LanguageHelper.formatCurrency(dispAssets, languageMode),
                        color = SolidPrimary,
                        isClickable = true,
                        onClick = { onNavigateToTab(3) }
                    )

                    // Row 6: (+) Subtotal Inflows (Green styled)
                    val dispInflows = if (viewMode == 0) totalInflows else (actualIncomes + actualAssets)
                    BudgetTableSubtotalRow(
                        prefix = "(+)",
                        amountText = LanguageHelper.formatCurrency(dispInflows, languageMode),
                        color = SolidIncome
                    )

                    // Divider Row
                    BudgetTableDividerRow()

                    // Row 7: » Surplus / Deficit (Cyan/Teal styled)
                    val dispSurplus = if (viewMode == 0) budgetedSurplus else actualSurplus
                    val isPos = dispSurplus >= 0
                    BudgetTableNetRow(
                        title = if (isPos) "» Surplus" else "» Deficit",
                        amountText = "${if (isPos) "⊕ " else "- "}${LanguageHelper.formatCurrency(Math.abs(dispSurplus), languageMode)}",
                        color = if (isPos) DarkCyan else SolidExpense
                    )
                }
            }
        }

        // SPECIFIC USER FORMULA CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Budgeted Formula",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Budgeted = ((Total Expenses + Total Liabilities) - (Total Assets + Total Incomes))",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "= (${LanguageHelper.formatNumber(totalExpenses, languageMode, false)} + ${LanguageHelper.formatNumber(totalLiabilities, languageMode, false)}) - (${LanguageHelper.formatNumber(totalAssets, languageMode, false)} + ${LanguageHelper.formatNumber(totalIncomes, languageMode, false)})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "= ${LanguageHelper.formatCurrency(budgetedFormulaResult, languageMode)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (budgetedFormulaResult <= 0) SolidIncome else SolidExpense
                    )
                }
            }
        }

        // Quick Navigation to Purpose Tabs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Manage Budget Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text("Expenses", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { onNavigateToTab(2) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text("Incomes", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { onNavigateToTab(3) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text("Assets", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { onNavigateToTab(4) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text("Liabilities", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetTableRow(
    iconSymbol: String,
    title: String,
    amountText: String,
    color: Color,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = iconSymbol, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            if (isClickable) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
            }
        }
        Text(
            text = amountText,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun BudgetTableSubtotalRow(
    prefix: String,
    amountText: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prefix,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline
            )
            Text(
                text = amountText,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontSize = 15.sp,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
private fun BudgetTableDividerRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("-", color = MaterialTheme.colorScheme.outlineVariant, fontWeight = FontWeight.Bold)
        Text("-", color = MaterialTheme.colorScheme.outlineVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BudgetTableNetRow(
    title: String,
    amountText: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontSize = 15.sp
            )
            Text(
                text = amountText,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontSize = 16.sp
            )
        }
    }
}

/* -------------------------------------------------------------
   TABS 1 & 2: CATEGORY BUDGET LIST (EXPENSES / INCOMES)
   ------------------------------------------------------------- */
@Composable
private fun BudgetPurposeCategoryList(
    title: String,
    items: List<BudgetTargetItem>,
    budgetMap: Map<String, MonthlyBudget>,
    monthTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    sectionColor: Color,
    onEditBudget: (BudgetTargetItem, Double, Boolean) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddNewTransaction: (BudgetTargetItem) -> Unit
) {
    val totalBudgeted = items.sumOf { item ->
        val saved = budgetMap["${item.itemType}_${item.id}"]
        if (saved != null) (if (saved.isEnabled) saved.budgetedAmount else 0.0) else item.defaultLimit
    }

    val totalActual = items.sumOf { item ->
        monthTransactions.filter {
            it.transaction.categoryId == item.id || it.transaction.subCategoryId == item.id
        }.sumOf { it.transaction.amount }
    }

    val groupedItems = remember(items) { items.groupBy { it.groupName } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budget_category_list_$title"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = sectionColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total $title Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = sectionColor
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(totalBudgeted, languageMode),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = sectionColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val ratio = if (totalBudgeted > 0) (totalActual / totalBudgeted).coerceIn(0.0, 1.0) else 0.0
                    LinearProgressIndicator(
                        progress = { ratio.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = sectionColor,
                        trackColor = sectionColor.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Actual: ${LanguageHelper.formatCurrency(totalActual, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        val percent = if (totalBudgeted > 0) (totalActual / totalBudgeted * 100).toInt() else 0
                        Text(
                            text = "$percent% utilized",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Grouped Categories
        groupedItems.forEach { (groupName, catItems) ->
            item {
                Text(
                    text = groupName.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            items(catItems, key = { "cat_${it.id}" }) { item ->
                val savedBudget = budgetMap["${item.itemType}_${item.id}"]
                val isEnabled = savedBudget?.isEnabled ?: true
                val budgetedAmt = savedBudget?.budgetedAmount ?: item.defaultLimit

                // Find 2 or 3 previous transactions for this category in this month
                val categoryTxs = remember(monthTransactions, item.id) {
                    monthTransactions.filter {
                        it.transaction.categoryId == item.id || it.transaction.subCategoryId == item.id
                    }.sortedByDescending { it.transaction.dateEpochMs }.take(3)
                }

                val actualAmt = remember(monthTransactions, item.id) {
                    monthTransactions.filter {
                        it.transaction.categoryId == item.id || it.transaction.subCategoryId == item.id
                    }.sumOf { it.transaction.amount }
                }

                CategoryBudgetCard(
                    item = item,
                    budgetedAmount = budgetedAmt,
                    actualAmount = actualAmt,
                    isEnabled = isEnabled,
                    previousTransactions = categoryTxs,
                    languageMode = languageMode,
                    sectionColor = sectionColor,
                    onEditBudget = { onEditBudget(item, budgetedAmt, isEnabled) },
                    onEditTransaction = onEditTransaction,
                    onAddNewTransaction = { onAddNewTransaction(item) }
                )
            }
        }
    }
}

/* -------------------------------------------------------------
   TABS 3 & 4: ACCOUNT BUDGET LIST (ASSETS / LIABILITIES)
   ------------------------------------------------------------- */
@Composable
private fun BudgetPurposeAccountList(
    title: String,
    items: List<BudgetTargetItem>,
    budgetMap: Map<String, MonthlyBudget>,
    monthTransactions: List<TransactionWithDetails>,
    accountsWithBalances: List<AccountWithBalance>,
    languageMode: LanguageMode,
    sectionColor: Color,
    onEditBudget: (BudgetTargetItem, Double, Boolean) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddNewTransaction: (BudgetTargetItem) -> Unit
) {
    val totalBudgeted = items.sumOf { item ->
        val saved = budgetMap["${item.itemType}_${item.id}"]
        if (saved != null) (if (saved.isEnabled) saved.budgetedAmount else 0.0) else 0.0
    }

    val groupedItems = remember(items) { items.groupBy { it.groupName } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budget_account_list_$title"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = sectionColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total $title Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = sectionColor
                        )
                        Text(
                            text = "Planned for this month",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = LanguageHelper.formatCurrency(totalBudgeted, languageMode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = sectionColor
                    )
                }
            }
        }

        // Grouped Accounts
        groupedItems.forEach { (groupName, accItems) ->
            item {
                Text(
                    text = groupName.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            items(accItems, key = { "acc_${it.id}" }) { item ->
                val savedBudget = budgetMap["${item.itemType}_${item.id}"]
                val isEnabled = savedBudget?.isEnabled ?: true
                val budgetedAmt = savedBudget?.budgetedAmount ?: 0.0

                // Current balance from account
                val currentBal = accountsWithBalances.find { it.account.id == item.id }?.currentBalance ?: 0.0

                // Find 2 or 3 previous transactions for this account in this month
                val accountTxs = remember(monthTransactions, item.id) {
                    monthTransactions.filter {
                        it.transaction.debitAccountId == item.id || it.transaction.creditAccountId == item.id
                    }.sortedByDescending { it.transaction.dateEpochMs }.take(3)
                }

                CategoryBudgetCard(
                    item = item,
                    budgetedAmount = budgetedAmt,
                    actualAmount = currentBal,
                    isEnabled = isEnabled,
                    previousTransactions = accountTxs,
                    languageMode = languageMode,
                    sectionColor = sectionColor,
                    onEditBudget = { onEditBudget(item, budgetedAmt, isEnabled) },
                    onEditTransaction = onEditTransaction,
                    onAddNewTransaction = { onAddNewTransaction(item) }
                )
            }
        }
    }
}

/* -------------------------------------------------------------
   CATEGORY / ACCOUNT CARD WITH 2-3 PREVIOUS TRANSACTIONS
   ------------------------------------------------------------- */
@Composable
private fun CategoryBudgetCard(
    item: BudgetTargetItem,
    budgetedAmount: Double,
    actualAmount: Double,
    isEnabled: Boolean,
    previousTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    sectionColor: Color,
    onEditBudget: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddNewTransaction: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 1.5.dp else 0.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_item_card_${item.id}")
            .animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Row 1: Icon, Name, Monthly Budget pill, and tap to edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Category Icon with Circle container
                    val parsedColor = remember(item.colorHex) { IconHelper.parseColorHex(item.colorHex) }
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

                    Column {
                        Text(
                            text = LanguageHelper.getLocalizedName(item.nameEn, item.nameBn, languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.groupName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Monthly Budget Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (budgetedAmount > 0) sectionColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onEditBudget)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (budgetedAmount > 0) "Monthly: ${LanguageHelper.formatCurrency(budgetedAmount, languageMode)}" else "+ Set Budget",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (budgetedAmount > 0) sectionColor else MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            modifier = Modifier.size(13.dp),
                            tint = if (budgetedAmount > 0) sectionColor else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Progress Bar (if budget is set)
            if (budgetedAmount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                val ratio = (actualAmount / budgetedAmount).coerceIn(0.0, 1.0)
                LinearProgressIndicator(
                    progress = { ratio.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (actualAmount > budgetedAmount) SolidExpense else sectionColor,
                    trackColor = sectionColor.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Actual: ${LanguageHelper.formatCurrency(actualAmount, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val pct = (actualAmount / budgetedAmount * 100).toInt()
                    Text(
                        text = "$pct% of budget",
                        fontSize = 11.sp,
                        color = if (actualAmount > budgetedAmount) SolidExpense else MaterialTheme.colorScheme.outline,
                        fontWeight = if (actualAmount > budgetedAmount) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // MANDATORY REQUIREMENT: Show 2 or 3 previous transactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Previous Transactions (${previousTransactions.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = sectionColor.copy(alpha = 0.08f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddNewTransaction)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp), tint = sectionColor)
                        Text("Add", fontSize = 11.sp, color = sectionColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (previousTransactions.isEmpty()) {
                Text(
                    text = "No transactions recorded yet this month.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    previousTransactions.forEach { txDetails ->
                        val tx = txDetails.transaction
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEditTransaction(tx) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.note.ifEmpty { tx.payeeOrPayer.ifEmpty { "Transaction" } },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = DateUtils.formatShortDate(tx.dateEpochMs, languageMode),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        val accountName = txDetails.creditAccount?.nameEn
                                            ?: txDetails.debitAccount?.nameEn
                                            ?: ""
                                        if (accountName.isNotEmpty()) {
                                            Text(
                                                text = "• $accountName",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }

                                val isExpense = tx.type == TransactionType.EXPENSE
                                Text(
                                    text = "${if (isExpense) "-" else "+"}${LanguageHelper.formatCurrency(tx.amount, languageMode)}",
                                    fontSize = 12.sp,
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

/* -------------------------------------------------------------
   EDIT BUDGET DIALOG
   ------------------------------------------------------------- */
@Composable
private fun EditBudgetDialog(
    item: BudgetTargetItem,
    initialAmount: Double,
    initialEnabled: Boolean,
    year: Int,
    month: Int,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (amount: Double, enabled: Boolean) -> Unit
) {
    var amountText by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "") }
    var isEnabled by remember { mutableStateOf(initialEnabled) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("edit_budget_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Set Monthly Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = DateUtils.formatMonthYear(year, month, languageMode),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item info
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val parsedColor = IconHelper.parseColorHex(item.colorHex)
                        Surface(shape = CircleShape, color = parsedColor.copy(alpha = 0.2f), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(IconHelper.getIconByName(item.iconName), contentDescription = null, tint = parsedColor, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text(
                                text = LanguageHelper.getLocalizedName(item.nameEn, item.nameBn, languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(text = "${item.groupName} • ${item.itemType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount text field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Budget Amount (৳)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("budget_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Increment buttons (+500, +1000, +5000, Clear)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(500, 1000, 5000).forEach { inc ->
                        OutlinedButton(
                            onClick = {
                                val current = amountText.toDoubleOrNull() ?: 0.0
                                amountText = (current + inc).toString()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+$inc", fontSize = 11.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = { amountText = "0" },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Enable/Disable in Budget Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Include in Budget", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Enable this item in monthly calculations", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            onSave(amt, isEnabled)
                        },
                        modifier = Modifier.testTag("budget_save_button")
                    ) {
                        Text("Save Budget")
                    }
                }
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
                // Header
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

                // Year Selector
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

                // 12 Months in a 3x4 Grid
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

                // Actions
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
