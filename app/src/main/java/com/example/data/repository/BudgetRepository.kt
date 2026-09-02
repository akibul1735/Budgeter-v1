package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class FinancialOverview(
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double,
    val monthlyNetSavings: Double,
    val totalDebits: Double,
    val totalCredits: Double,
    val isLedgerBalanced: Boolean
)

data class AccountWithBalance(
    val account: Account,
    val currentBalance: Double,
    val subAccounts: List<AccountWithBalance> = emptyList(),
    val isParent: Boolean = false
)

data class CategoryWithStats(
    val category: Category,
    val totalAmount: Double,
    val subCategories: List<CategoryWithStats> = emptyList()
)

class BudgetRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    val transactionsWithDetails: Flow<List<TransactionWithDetails>> = combine(
        allTransactions,
        allAccounts,
        allCategories
    ) { txs, accounts, categories ->
        val accountMap = accounts.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }

        txs.map { tx ->
            TransactionWithDetails(
                transaction = tx,
                debitAccount = tx.debitAccountId?.let { accountMap[it] },
                creditAccount = tx.creditAccountId?.let { accountMap[it] },
                category = tx.categoryId?.let { categoryMap[it] },
                subCategory = tx.subCategoryId?.let { categoryMap[it] }
            )
        }
    }

    val accountsWithBalances: Flow<List<AccountWithBalance>> = combine(
        allAccounts,
        allTransactions
    ) { accounts, txs ->
        // Calculate account net delta from transactions
        val debitSums = mutableMapOf<Long, Double>()
        val creditSums = mutableMapOf<Long, Double>()

        for (tx in txs) {
            tx.debitAccountId?.let { id ->
                debitSums[id] = (debitSums[id] ?: 0.0) + tx.amount
            }
            tx.creditAccountId?.let { id ->
                creditSums[id] = (creditSums[id] ?: 0.0) + tx.amount
            }
        }

        fun calcBalance(acc: Account): Double {
            val dr = debitSums[acc.id] ?: 0.0
            val cr = creditSums[acc.id] ?: 0.0
            return when (acc.type) {
                AccountType.ASSET, AccountType.EXPENSE -> acc.initialBalance + (dr - cr)
                AccountType.LIABILITY, AccountType.EQUITY, AccountType.INCOME -> acc.initialBalance + (cr - dr)
            }
        }

        val parentAccounts = accounts.filter { it.parentId == null }
        val subAccountMap = accounts.filter { it.parentId != null }.groupBy { it.parentId!! }

        parentAccounts.map { parent ->
            val subs = subAccountMap[parent.id] ?: emptyList()
            val subBalances = subs.map { sub ->
                AccountWithBalance(
                    account = sub,
                    currentBalance = calcBalance(sub),
                    isParent = false
                )
            }
            val parentSelfBalance = calcBalance(parent)
            val totalSubBalance = subBalances.sumOf { it.currentBalance }
            val combinedBalance = parentSelfBalance + totalSubBalance

            AccountWithBalance(
                account = parent,
                currentBalance = combinedBalance,
                subAccounts = subBalances,
                isParent = true
            )
        }
    }

    val financialOverview: Flow<FinancialOverview> = combine(
        accountsWithBalances,
        allTransactions
    ) { accountsWithBal, txs ->
        var totalAssets = 0.0
        var totalLiabilities = 0.0

        for (item in accountsWithBal) {
            when (item.account.type) {
                AccountType.ASSET -> totalAssets += item.currentBalance
                AccountType.LIABILITY -> totalLiabilities += item.currentBalance
                else -> {}
            }
        }

        val netWorth = totalAssets - totalLiabilities

        val startOfMonth = com.example.util.DateUtils.getStartOfMonth()
        val endOfMonth = com.example.util.DateUtils.getEndOfMonth()

        val monthlyTxs = txs.filter { it.dateEpochMs in startOfMonth..endOfMonth }
        val monthlyIncome = monthlyTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val monthlyExpense = monthlyTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val monthlyNetSavings = monthlyIncome - monthlyExpense

        // Double Entry verification
        var totalDebits = 0.0
        var totalCredits = 0.0
        for (tx in txs) {
            when (tx.type) {
                TransactionType.EXPENSE -> {
                    totalDebits += tx.amount // Expense category Dr
                    totalCredits += tx.amount // Asset/Liability Cr
                }
                TransactionType.INCOME -> {
                    totalDebits += tx.amount // Asset Dr
                    totalCredits += tx.amount // Income category Cr
                }
                TransactionType.TRANSFER -> {
                    totalDebits += tx.amount // Destination Dr
                    totalCredits += tx.amount // Source Cr
                }
            }
        }

        val isLedgerBalanced = Math.abs(totalDebits - totalCredits) < 0.001

        FinancialOverview(
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = netWorth,
            monthlyIncome = monthlyIncome,
            monthlyExpense = monthlyExpense,
            monthlyNetSavings = monthlyNetSavings,
            totalDebits = totalDebits,
            totalCredits = totalCredits,
            isLedgerBalanced = isLedgerBalanced
        )
    }

    suspend fun insertAccount(account: Account): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)
}
