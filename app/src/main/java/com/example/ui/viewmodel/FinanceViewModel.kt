package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FinanceDatabase
import com.example.data.model.AIInsight
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.BalanceSheetSummary
import com.example.data.model.CashflowSummary
import com.example.data.model.Category
import com.example.data.model.CategorySpend
import com.example.data.model.InsightType
import com.example.data.model.RecurringBill
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class FinanceFilterState(
    val searchQuery: String = "",
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val selectedType: TransactionType? = null,
    val dateFilter: DateFilterOption = DateFilterOption.ALL
)

enum class DateFilterOption(val label: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days"),
    THIS_WEEK("This Week")
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val database = FinanceDatabase.getDatabase(application, viewModelScope)
        repository = FinanceRepository(database.financeDao())
    }

    // --- Currency State ---
    private val _selectedCurrency = MutableStateFlow("USD")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    fun setCurrency(currencyCode: String) {
        _selectedCurrency.value = currencyCode
    }

    // --- Filter State ---
    private val _filterState = MutableStateFlow(FinanceFilterState())
    val filterState: StateFlow<FinanceFilterState> = _filterState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun updateAccountFilter(accountId: Long?) {
        _filterState.value = _filterState.value.copy(selectedAccountId = accountId)
    }

    fun updateCategoryFilter(categoryId: Long?) {
        _filterState.value = _filterState.value.copy(selectedCategoryId = categoryId)
    }

    fun updateTypeFilter(type: TransactionType?) {
        _filterState.value = _filterState.value.copy(selectedType = type)
    }

    fun updateDateFilter(dateFilter: DateFilterOption) {
        _filterState.value = _filterState.value.copy(dateFilter = dateFilter)
    }

    fun clearFilters() {
        _filterState.value = FinanceFilterState()
    }

    // --- Flow Data ---
    val allAccounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<RecurringBill>> = repository.activeBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoal>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balanceSheet: StateFlow<BalanceSheetSummary> = repository.balanceSheetSummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSheetSummary(0.0, 0.0, 0.0))

    val cashflow: StateFlow<CashflowSummary> = repository.currentMonthCashflow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CashflowSummary(0.0, 0.0, 0.0, 0.0))

    val categorySpends: StateFlow<List<CategorySpend>> = repository.categorySpends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactionsWithDetails: StateFlow<List<TransactionWithDetails>> = repository.transactionsWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<TransactionWithDetails>> = combine(
        repository.transactionsWithDetails,
        _filterState
    ) { txs, filters ->
        txs.filter { item ->
            val tx = item.transaction
            val matchesQuery = filters.searchQuery.isBlank() ||
                    tx.title.contains(filters.searchQuery, ignoreCase = true) ||
                    item.categoryName?.contains(filters.searchQuery, ignoreCase = true) == true ||
                    tx.notes.contains(filters.searchQuery, ignoreCase = true) ||
                    tx.tags.contains(filters.searchQuery, ignoreCase = true)

            val matchesAccount = filters.selectedAccountId == null ||
                    tx.accountId == filters.selectedAccountId ||
                    tx.toAccountId == filters.selectedAccountId

            val matchesCategory = filters.selectedCategoryId == null ||
                    tx.categoryId == filters.selectedCategoryId

            val matchesType = filters.selectedType == null || tx.type == filters.selectedType

            val now = System.currentTimeMillis()
            val matchesDate = when (filters.dateFilter) {
                DateFilterOption.ALL -> true
                DateFilterOption.THIS_MONTH -> {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }
                    tx.dateEpochMs >= cal.timeInMillis
                }
                DateFilterOption.LAST_30_DAYS -> tx.dateEpochMs >= (now - 30L * 24 * 3600 * 1000)
                DateFilterOption.THIS_WEEK -> {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                    }
                    tx.dateEpochMs >= cal.timeInMillis
                }
            }

            matchesQuery && matchesAccount && matchesCategory && matchesType && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic AI & Smart Insights generator
    val dynamicInsights: StateFlow<List<AIInsight>> = combine(
        allTransactionsWithDetails,
        categorySpends,
        allBills,
        allSavingsGoals,
        cashflow
    ) { _, spends, bills, goals, flow ->
        val insights = mutableListOf<AIInsight>()

        // 1. Budget Warnings
        val overBudget = spends.filter { it.budgetAmount > 0 && it.totalSpent > it.budgetAmount }
        if (overBudget.isNotEmpty()) {
            val topOver = overBudget.first()
            val excess = topOver.totalSpent - topOver.budgetAmount
            insights.add(
                AIInsight(
                    id = "budget_over_${topOver.categoryId}",
                    title = "Overbudget Alert: ${topOver.categoryName}",
                    description = "You've exceeded your monthly budget by $${String.format("%.2f", excess)}. Consider slowing non-essential spending here.",
                    type = InsightType.ALERT,
                    amount = excess,
                    actionText = "Review Category"
                )
            )
        }

        // 2. High Savings Rate recognition
        if (flow.savingsRate >= 25.0) {
            insights.add(
                AIInsight(
                    id = "savings_rate_positive",
                    title = "Strong Savings Rate (${String.format("%.1f", flow.savingsRate)}%)",
                    description = "Excellent discipline! You are saving ${String.format("%.1f", flow.savingsRate)}% of your earnings this month ($${String.format("%.2f", flow.netSavings)} net).",
                    type = InsightType.POSITIVE,
                    actionText = "Add to Goals"
                )
            )
        }

        // 3. Upcoming Bills alert
        val now = System.currentTimeMillis()
        val nextWeek = now + (7L * 24 * 3600 * 1000)
        val upcomingBills = bills.filter { it.nextDueDateEpochMs in now..nextWeek }
        if (upcomingBills.isNotEmpty()) {
            val totalDue = upcomingBills.sumOf { it.amount }
            insights.add(
                AIInsight(
                    id = "upcoming_bills_due",
                    title = "${upcomingBills.size} Upcoming Bills ($${String.format("%.2f", totalDue)})",
                    description = "Bills including '${upcomingBills.first().title}' are scheduled in the next 7 days. Ensure checking account has sufficient liquidity.",
                    type = InsightType.NEUTRAL,
                    amount = totalDue,
                    actionText = "View Bills"
                )
            )
        }

        // 4. Savings Goal progress tip
        val closeGoal = goals.find { it.currentAmount / it.targetAmount >= 0.75 && it.currentAmount < it.targetAmount }
        if (closeGoal != null) {
            val remaining = closeGoal.targetAmount - closeGoal.currentAmount
            insights.add(
                AIInsight(
                    id = "goal_close_${closeGoal.id}",
                    title = "Almost at Goal: ${closeGoal.title}",
                    description = "You're at ${String.format("%.0f", (closeGoal.currentAmount / closeGoal.targetAmount) * 100)}%! Only $${String.format("%.2f", remaining)} left to hit your milestone.",
                    type = InsightType.TIP,
                    amount = remaining,
                    actionText = "Boost Deposit"
                )
            )
        }

        // Fallback default tip
        if (insights.isEmpty()) {
            insights.add(
                AIInsight(
                    id = "smart_tip_general",
                    title = "Smart Cashflow Tip",
                    description = "Log your daily transactions consistently to get accurate double-entry balance sheets and net worth projections.",
                    type = InsightType.TIP,
                    actionText = "Log Transaction"
                )
            )
        }

        insights
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Action Methods ---

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long? = null,
        dateEpochMs: Long = System.currentTimeMillis(),
        notes: String = "",
        tags: String = "",
        status: TransactionStatus = TransactionStatus.CLEARED
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    type = type,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    categoryId = categoryId,
                    dateEpochMs = dateEpochMs,
                    notes = notes,
                    tags = tags,
                    status = status
                )
            )
        }
    }

    fun quickAddPreset(presetTitle: String, amount: Double, type: TransactionType, categoryNameMatch: String) {
        viewModelScope.launch {
            val accounts = allAccounts.value
            val primaryAcc = accounts.find { it.type == AccountType.CHECKING || it.type == AccountType.CASH } ?: accounts.firstOrNull()
            val categories = allCategories.value
            val matchedCat = categories.find { it.name.contains(categoryNameMatch, ignoreCase = true) }

            if (primaryAcc != null) {
                repository.addTransaction(
                    Transaction(
                        title = presetTitle,
                        amount = amount,
                        type = type,
                        accountId = primaryAcc.id,
                        categoryId = matchedCat?.id,
                        dateEpochMs = System.currentTimeMillis(),
                        notes = "Quick-added via 1-tap preset",
                        tags = "QuickPreset"
                    )
                )
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addAccount(
        name: String,
        type: AccountType,
        balance: Double,
        currency: String = "USD",
        colorHex: String = "#1A73E8",
        iconName: String = "AccountBalance"
    ) {
        viewModelScope.launch {
            repository.addAccount(
                Account(
                    name = name,
                    type = type,
                    balance = balance,
                    currency = currency,
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun addCategory(
        name: String,
        type: TransactionType,
        iconName: String,
        colorHex: String,
        monthlyBudget: Double
    ) {
        viewModelScope.launch {
            repository.addCategory(
                Category(
                    name = name,
                    type = type,
                    iconName = iconName,
                    colorHex = colorHex,
                    monthlyBudget = monthlyBudget
                )
            )
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addRecurringBill(
        title: String,
        amount: Double,
        categoryId: Long?,
        accountId: Long,
        frequency: com.example.data.model.BillFrequency,
        dueDateMs: Long,
        isAutoPay: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addRecurringBill(
                RecurringBill(
                    title = title,
                    amount = amount,
                    categoryId = categoryId,
                    accountId = accountId,
                    frequency = frequency,
                    nextDueDateEpochMs = dueDateMs,
                    isAutoPay = isAutoPay,
                    notes = notes
                )
            )
        }
    }

    fun payRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            repository.payRecurringBill(bill)
        }
    }

    fun deleteRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            repository.deleteRecurringBill(bill)
        }
    }

    fun addSavingsGoal(
        title: String,
        targetAmount: Double,
        currentAmount: Double,
        targetDateMs: Long,
        colorHex: String,
        iconName: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addSavingsGoal(
                SavingsGoal(
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDateEpochMs = targetDateMs,
                    colorHex = colorHex,
                    iconName = iconName,
                    notes = notes
                )
            )
        }
    }

    fun contributeToGoal(goal: SavingsGoal, amount: Double, fromAccountId: Long) {
        viewModelScope.launch {
            repository.contributeToGoal(goal, amount, fromAccountId)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    fun exportTransactionsCsv(): String {
        return repository.generateCsvExport(allTransactionsWithDetails.value)
    }
}
