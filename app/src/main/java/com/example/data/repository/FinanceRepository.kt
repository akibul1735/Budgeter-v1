package com.example.data.repository

import com.example.data.local.FinanceDao
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.BalanceSheetSummary
import com.example.data.model.CashflowSummary
import com.example.data.model.Category
import com.example.data.model.CategorySpend
import com.example.data.model.RecurringBill
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceRepository(private val dao: FinanceDao) {

    val allAccounts: Flow<List<Account>> = dao.getAllActiveAccounts()
    val allCategories: Flow<List<Category>> = dao.getAllCategories()
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()
    val activeBills: Flow<List<RecurringBill>> = dao.getAllActiveBills()
    val allSavingsGoals: Flow<List<SavingsGoal>> = dao.getAllSavingsGoals()

    val transactionsWithDetails: Flow<List<TransactionWithDetails>> = combine(
        dao.getAllTransactions(),
        dao.getAllActiveAccounts(),
        dao.getAllCategories()
    ) { txs, accounts, categories ->
        val accountMap = accounts.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }

        txs.map { tx ->
            val acc = accountMap[tx.accountId]
            val toAcc = tx.toAccountId?.let { accountMap[it] }
            val cat = tx.categoryId?.let { categoryMap[it] }
            TransactionWithDetails(
                transaction = tx,
                accountName = acc?.name ?: "Unknown Account",
                toAccountName = toAcc?.name,
                categoryName = cat?.name ?: if (tx.type == TransactionType.TRANSFER) "Transfer" else "Uncategorized",
                categoryIcon = cat?.iconName ?: if (tx.type == TransactionType.TRANSFER) "SwapHoriz" else "HelpOutline",
                categoryColor = cat?.colorHex ?: if (tx.type == TransactionType.TRANSFER) "#3B82F6" else "#94A3B8"
            )
        }
    }

    val balanceSheetSummary: Flow<BalanceSheetSummary> = dao.getAllActiveAccounts().combine(dao.getAllActiveAccounts()) { accounts, _ ->
        var assets = 0.0
        var liabilities = 0.0
        for (acc in accounts) {
            when (acc.type) {
                AccountType.CHECKING,
                AccountType.SAVINGS,
                AccountType.CASH,
                AccountType.INVESTMENT,
                AccountType.ASSET -> {
                    if (acc.balance >= 0) assets += acc.balance else liabilities += -acc.balance
                }
                AccountType.CREDIT_CARD,
                AccountType.LOAN -> {
                    if (acc.balance < 0) liabilities += -acc.balance else assets += acc.balance
                }
            }
        }
        BalanceSheetSummary(
            totalAssets = assets,
            totalLiabilities = liabilities,
            netWorth = assets - liabilities
        )
    }

    val currentMonthCashflow: Flow<CashflowSummary> = dao.getAllTransactions().combine(dao.getAllCategories()) { txs, _ ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        val monthTxs = txs.filter { it.dateEpochMs >= startOfMonth }
        var income = 0.0
        var expense = 0.0
        for (tx in monthTxs) {
            when (tx.type) {
                TransactionType.INCOME -> income += tx.amount
                TransactionType.EXPENSE -> expense += tx.amount
                TransactionType.TRANSFER -> {} // Transfers do not affect cashflow
            }
        }
        val net = income - expense
        val rate = if (income > 0) ((net / income) * 100.0).coerceIn(-100.0, 100.0) else 0.0
        CashflowSummary(
            totalIncome = income,
            totalExpense = expense,
            netSavings = net,
            savingsRate = rate
        )
    }

    val categorySpends: Flow<List<CategorySpend>> = combine(
        dao.getAllTransactions(),
        dao.getAllCategories()
    ) { txs, categories ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        val monthExpenses = txs.filter { it.dateEpochMs >= startOfMonth && it.type == TransactionType.EXPENSE }
        val totalSpentAll = monthExpenses.sumOf { it.amount }
        val expenseCats = categories.filter { it.type == TransactionType.EXPENSE }

        val spentByCat = monthExpenses.groupBy { it.categoryId }

        expenseCats.map { cat ->
            val spent = spentByCat[cat.id]?.sumOf { it.amount } ?: 0.0
            val pct = if (totalSpentAll > 0) (spent / totalSpentAll).toFloat() else 0f
            CategorySpend(
                categoryId = cat.id,
                categoryName = cat.name,
                categoryColor = cat.colorHex,
                categoryIcon = cat.iconName,
                totalSpent = spent,
                budgetAmount = cat.monthlyBudget,
                percentageOfTotal = pct
            )
        }.sortedByDescending { it.totalSpent }
    }

    suspend fun addTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction)
        // Update account balances
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                dao.adjustAccountBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.INCOME -> {
                dao.adjustAccountBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                dao.adjustAccountBalance(transaction.accountId, -transaction.amount)
                transaction.toAccountId?.let { toId ->
                    dao.adjustAccountBalance(toId, transaction.amount)
                }
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction)
        // Revert balance changes
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                dao.adjustAccountBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.INCOME -> {
                dao.adjustAccountBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.TRANSFER -> {
                dao.adjustAccountBalance(transaction.accountId, transaction.amount)
                transaction.toAccountId?.let { toId ->
                    dao.adjustAccountBalance(toId, -transaction.amount)
                }
            }
        }
    }

    suspend fun addAccount(account: Account): Long = dao.insertAccount(account)
    suspend fun updateAccount(account: Account) = dao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = dao.deleteAccount(account)

    suspend fun addCategory(category: Category): Long = dao.insertCategory(category)
    suspend fun updateCategory(category: Category) = dao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)

    suspend fun addRecurringBill(bill: RecurringBill): Long = dao.insertRecurringBill(bill)
    suspend fun updateRecurringBill(bill: RecurringBill) = dao.updateRecurringBill(bill)
    suspend fun deleteRecurringBill(bill: RecurringBill) = dao.deleteRecurringBill(bill)

    suspend fun payRecurringBill(bill: RecurringBill) {
        // Record payment as a transaction
        val tx = Transaction(
            title = bill.title,
            amount = bill.amount,
            type = TransactionType.EXPENSE,
            accountId = bill.accountId,
            categoryId = bill.categoryId,
            dateEpochMs = System.currentTimeMillis(),
            notes = "Auto-paid recurring bill: ${bill.title}",
            tags = "BillPayment,Recurring",
            isRecurring = true
        )
        addTransaction(tx)

        // Advance next due date according to frequency
        val cal = Calendar.getInstance()
        cal.timeInMillis = bill.nextDueDateEpochMs
        when (bill.frequency) {
            com.example.data.model.BillFrequency.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
            com.example.data.model.BillFrequency.BIWEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 14)
            com.example.data.model.BillFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            com.example.data.model.BillFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        dao.updateRecurringBill(bill.copy(nextDueDateEpochMs = cal.timeInMillis))
    }

    suspend fun addSavingsGoal(goal: SavingsGoal): Long = dao.insertSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoal) = dao.updateSavingsGoal(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoal) = dao.deleteSavingsGoal(goal)

    suspend fun contributeToGoal(goal: SavingsGoal, amount: Double, fromAccountId: Long) {
        val updatedGoal = goal.copy(currentAmount = (goal.currentAmount + amount).coerceAtLeast(0.0))
        dao.updateSavingsGoal(updatedGoal)
        // Record as transfer / expense from account
        val tx = Transaction(
            title = "Deposit: ${goal.title}",
            amount = amount,
            type = TransactionType.EXPENSE,
            accountId = fromAccountId,
            dateEpochMs = System.currentTimeMillis(),
            notes = "Contribution towards goal: ${goal.title}",
            tags = "SavingsGoal"
        )
        addTransaction(tx)
    }

    fun generateCsvExport(transactions: List<TransactionWithDetails>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Title,Amount,Account,To Account,Category,Status,Notes,Tags\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (item in transactions) {
            val tx = item.transaction
            val dateStr = sdf.format(Date(tx.dateEpochMs))
            sb.append("${tx.id},\"$dateStr\",${tx.type},\"${tx.title.replace("\"", "\"\"")}\",${tx.amount},\"${item.accountName}\",\"${item.toAccountName ?: ""}\",\"${item.categoryName ?: ""}\",${tx.status},\"${tx.notes.replace("\"", "\"\"")}\",\"${tx.tags}\"\n")
        }
        return sb.toString()
    }
}
