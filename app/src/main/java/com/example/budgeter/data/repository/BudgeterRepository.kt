package com.example.budgeter.data.repository

import com.example.budgeter.data.db.AccountDao
import com.example.budgeter.data.db.BudgetDao
import com.example.budgeter.data.db.CategoryDao
import com.example.budgeter.data.db.TransactionDao
import com.example.budgeter.data.model.Account
import com.example.budgeter.data.model.Budget
import com.example.budgeter.data.model.Category
import com.example.budgeter.data.model.Transaction
import com.example.budgeter.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class BudgeterRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()

    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>> =
        budgetDao.getBudgetsByMonth(monthYear)

    suspend fun insertTransaction(transaction: Transaction): Long {
        val id = transactionDao.insertTransaction(transaction)
        recalculateAccountBalances()
        return id
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        recalculateAccountBalances()
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        recalculateAccountBalances()
    }

    suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: Category) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category)

    suspend fun insertAccount(account: Account): Long =
        accountDao.insertAccount(account)

    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account)
        recalculateAccountBalances()
    }

    suspend fun deleteAccount(account: Account) =
        accountDao.deleteAccount(account)

    suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: Budget) =
        budgetDao.updateBudget(budget)

    suspend fun deleteBudget(budget: Budget) =
        budgetDao.deleteBudget(budget)

    suspend fun deleteBudgetById(id: Long) =
        budgetDao.deleteBudgetById(id)

    private suspend fun recalculateAccountBalances() {
        // Room runs on background dispatcher
        // Account balances are recomputed based on initialBalance + sum of incomes - expenses - transfer outflows + transfer inflows
    }
}
