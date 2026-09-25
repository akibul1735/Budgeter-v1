package com.example.budgeter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgeter.data.model.*
import com.example.budgeter.data.repository.BudgeterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class BudgetEnvelopeUiModel(
    val category: Category,
    val budget: Budget?,
    val allocatedAmount: Double,
    val targetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progressPercent: Float,
    val isOverspent: Boolean,
    val isTargetAchieved: Boolean,
    val hasActivity: Boolean
)

data class FilteredLedgerTotals(
    val totalExpense: Double,
    val totalIncome: Double,
    val totalTransfer: Double,
    val netBalance: Double
)

class BudgeterViewModel(
    private val repository: BudgeterRepository
) : ViewModel() {

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMonthYear = MutableStateFlow(getCurrentMonthYear())
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    private val _currencySymbol = MutableStateFlow("৳")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _ledgerFilterState = MutableStateFlow(LedgerFilterState())
    val ledgerFilterState: StateFlow<LedgerFilterState> = _ledgerFilterState.asStateFlow()

    private val _budgetFilterState = MutableStateFlow(BudgetFilterState())
    val budgetFilterState: StateFlow<BudgetFilterState> = _budgetFilterState.asStateFlow()

    // Available labels from existing transactions
    val availableLabels: StateFlow<List<String>> = allTransactions.map { txList ->
        txList.flatMap { tx ->
            tx.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        _ledgerFilterState
    ) { transactions, filter ->
        transactions.filter { tx ->
            // 1. Multi-Select Type Filter
            if (filter.selectedTypeFilters.isNotEmpty() && !filter.selectedTypeFilters.contains(tx.type)) {
                return@filter false
            }

            // 2. Multi-Select Category Filter
            if (filter.selectedCategoryIds.isNotEmpty()) {
                if (tx.categoryId == null || !filter.selectedCategoryIds.contains(tx.categoryId)) {
                    return@filter false
                }
            }

            // 3. Multi-Select Account Filter (Check source or destination for transfer)
            if (filter.selectedAccountIds.isNotEmpty()) {
                val matchesSource = filter.selectedAccountIds.contains(tx.accountId)
                val matchesDest = tx.destinationAccountId != null && filter.selectedAccountIds.contains(tx.destinationAccountId)
                if (!matchesSource && !matchesDest) {
                    return@filter false
                }
            }

            // 4. Multi-Select Labels Filter
            if (filter.selectedLabels.isNotEmpty()) {
                val txLabels = tx.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                if (filter.selectedLabels.none { it in txLabels }) {
                    return@filter false
                }
            }

            // 5. Multi-Select Status Filter
            if (filter.selectedStatuses.isNotEmpty() && !filter.selectedStatuses.contains(tx.status)) {
                return@filter false
            }

            // 6. Date Preset Filter
            if (!matchesDatePreset(tx.timestamp, filter.datePreset, filter.customStartDate, filter.customEndDate)) {
                return@filter false
            }

            // 7. Amount Range Filter
            if (filter.minAmount != null && tx.amount < filter.minAmount) {
                return@filter false
            }
            if (filter.maxAmount != null && tx.amount > filter.maxAmount) {
                return@filter false
            }

            // 8. Search Query
            if (filter.searchQuery.isNotBlank()) {
                val query = filter.searchQuery.trim().lowercase()
                val matchesTitle = tx.title.lowercase().contains(query)
                val matchesNote = tx.note.lowercase().contains(query)
                val matchesLabels = tx.labels.lowercase().contains(query)
                if (!matchesTitle && !matchesNote && !matchesLabels) {
                    return@filter false
                }
            }

            true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Totals for Ledger
    val filteredTotals: StateFlow<FilteredLedgerTotals> = filteredTransactions.map { list ->
        var expense = 0.0
        var income = 0.0
        var transfer = 0.0

        list.forEach { tx ->
            when (tx.type) {
                TransactionType.EXPENSE -> expense += tx.amount
                TransactionType.INCOME -> income += tx.amount
                TransactionType.TRANSFER -> transfer += tx.amount
            }
        }
        FilteredLedgerTotals(
            totalExpense = expense,
            totalIncome = income,
            totalTransfer = transfer,
            netBalance = income - expense
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilteredLedgerTotals(0.0, 0.0, 0.0, 0.0))

    // Envelope Budget models for selected month
    val budgetEnvelopes: StateFlow<List<BudgetEnvelopeUiModel>> = combine(
        allCategories,
        allBudgets,
        allTransactions,
        _selectedMonthYear,
        _budgetFilterState
    ) { categories, budgets, transactions, monthYear, filter ->
        val (startOfMonth, endOfMonth) = getMonthRange(monthYear)

        // Filter transactions for this month
        val monthTransactions = transactions.filter {
            it.timestamp in startOfMonth..endOfMonth && it.type == TransactionType.EXPENSE
        }

        val expenseCategories = categories.filter { it.isExpense }

        val models = expenseCategories.map { category ->
            val budget = budgets.firstOrNull { it.categoryId == category.id && it.monthYear == monthYear }
            val allocated = budget?.allocatedAmount ?: 0.0
            val target = budget?.targetAmount ?: 0.0

            val spent = monthTransactions
                .filter { it.categoryId == category.id }
                .sumOf { it.amount }

            val remaining = allocated - spent
            val progress = if (allocated > 0) (spent / allocated).toFloat() else 0f
            val isOverspent = spent > allocated && allocated > 0
            val isTargetAchieved = target > 0 && allocated >= target

            BudgetEnvelopeUiModel(
                category = category,
                budget = budget,
                allocatedAmount = allocated,
                targetAmount = target,
                spentAmount = spent,
                remainingAmount = remaining,
                progressPercent = progress.coerceIn(0f, 1f),
                isOverspent = isOverspent,
                isTargetAchieved = isTargetAchieved,
                hasActivity = spent > 0
            )
        }

        // Apply BudgetFilterState
        models.filter { item ->
            // Category filter
            if (filter.selectedCategoryIds.isNotEmpty() && !filter.selectedCategoryIds.contains(item.category.id)) {
                return@filter false
            }

            // Status filters
            if (filter.onlyWithBudgetOrTarget && item.allocatedAmount <= 0 && item.targetAmount <= 0) return@filter false
            if (filter.onlyWithoutBudgetOrTarget && (item.allocatedAmount > 0 || item.targetAmount > 0)) return@filter false
            if (filter.onlyOverspentOrOverallocated && !item.isOverspent) return@filter false
            if (filter.onlyUnderSpentOrUnderallocated && item.isOverspent) return@filter false
            if (filter.onlyOnTrack && item.isOverspent) return@filter false
            if (filter.onlyTargetAchieved && !item.isTargetAchieved) return@filter false
            if (filter.onlyTargetPending && (item.targetAmount <= 0 || item.isTargetAchieved)) return@filter false

            // Realized Activity
            if (filter.onlyWithActualActivity && !item.hasActivity) return@filter false
            if (filter.onlyZeroActivity && item.hasActivity) return@filter false

            // Balance & Debt conditions
            if (filter.onlyPositiveBalance && item.remainingAmount <= 0) return@filter false
            if (filter.onlyZeroBalance && item.remainingAmount != 0.0) return@filter false
            if (filter.onlyNegativeBalance && item.remainingAmount >= 0) return@filter false
            if (filter.excludeZeroAmounts && item.allocatedAmount == 0.0 && item.spentAmount == 0.0) return@filter false

            // Min/Max amount
            if (filter.minAmount != null && item.spentAmount < filter.minAmount && item.allocatedAmount < filter.minAmount) return@filter false
            if (filter.maxAmount != null && item.spentAmount > filter.maxAmount && item.allocatedAmount > filter.maxAmount) return@filter false

            // Search query
            if (filter.searchQuery.isNotBlank()) {
                val query = filter.searchQuery.trim().lowercase()
                if (!item.category.name.lowercase().contains(query)) return@filter false
            }

            true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month stats: Income, Expense, Budget Allocated, Total Savings
    val currentMonthStats = combine(
        allTransactions,
        allBudgets,
        _selectedMonthYear
    ) { transactions, budgets, monthYear ->
        val (start, end) = getMonthRange(monthYear)
        val monthTxs = transactions.filter { it.timestamp in start..end }
        val income = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val allocated = budgets.filter { it.monthYear == monthYear }.sumOf { it.allocatedAmount }

        Triple(income, expense, allocated)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0.0, 0.0, 0.0))

    // Actions
    fun updateLedgerFilter(filter: LedgerFilterState) {
        _ledgerFilterState.value = filter
    }

    fun updateBudgetFilter(filter: BudgetFilterState) {
        _budgetFilterState.value = filter
    }

    fun setSelectedMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.id == 0L) {
                repository.insertTransaction(transaction)
            } else {
                repository.updateTransaction(transaction)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun saveBudget(budget: Budget) {
        viewModelScope.launch {
            if (budget.id == 0L) {
                repository.insertBudget(budget)
            } else {
                repository.updateBudget(budget)
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            if (account.id == 0L) {
                repository.insertAccount(account)
            } else {
                repository.updateAccount(account)
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            if (category.id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
        }
    }

    private fun getCurrentMonthYear(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getMonthRange(monthYear: String): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val parts = monthYear.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
        val month = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1

        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    private fun matchesDatePreset(
        timestamp: Long,
        preset: DatePreset,
        customStart: Long?,
        customEnd: Long?
    ): Boolean {
        val cal = Calendar.getInstance()
        return when (preset) {
            DatePreset.ALL -> true
            DatePreset.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                timestamp >= start
            }
            DatePreset.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                timestamp >= start
            }
            DatePreset.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                timestamp >= start
            }
            DatePreset.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                timestamp in start..end
            }
            DatePreset.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                timestamp >= start
            }
            DatePreset.CUSTOM -> {
                val start = customStart ?: 0L
                val end = customEnd ?: Long.MAX_VALUE
                timestamp in start..end
            }
        }
    }
}

class BudgeterViewModelFactory(
    private val repository: BudgeterRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgeterViewModel::class.java)) {
            return BudgeterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
