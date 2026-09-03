package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.CategoryDao
import com.example.data.local.MonthlyBudgetDao
import com.example.data.local.RecurringBillDao
import com.example.data.local.TransactionDao
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.BillStatus
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.MonthlyBudget
import com.example.data.model.RecurringBill
import com.example.data.model.RecurringBillWithDetails
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
    val accountDao: AccountDao,
    val categoryDao: CategoryDao,
    val transactionDao: TransactionDao,
    val recurringBillDao: RecurringBillDao,
    val monthlyBudgetDao: MonthlyBudgetDao
) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allBills: Flow<List<RecurringBill>> = recurringBillDao.getAllBills()

    fun getMonthlyBudgets(year: Int, month: Int): Flow<List<MonthlyBudget>> =
        monthlyBudgetDao.getBudgetsForMonth(year, month)

    suspend fun saveMonthlyBudget(budget: MonthlyBudget): Long =
        monthlyBudgetDao.upsertBudget(budget)

    suspend fun copyBudgets(fromYear: Int, fromMonth: Int, toYear: Int, toMonth: Int) {
        val previous = monthlyBudgetDao.getBudgetsForMonthSnapshot(fromYear, fromMonth)
        if (previous.isNotEmpty()) {
            val copied = previous.map {
                it.copy(id = 0, year = toYear, month = toMonth, updatedAt = System.currentTimeMillis())
            }
            monthlyBudgetDao.upsertBudgets(copied)
        }
    }

    val recurringBillsWithDetails: Flow<List<RecurringBillWithDetails>> = combine(
        allBills,
        allAccounts,
        allCategories
    ) { bills, accounts, categories ->
        val accountMap = accounts.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }

        bills.map { bill ->
            RecurringBillWithDetails(
                bill = bill,
                debitAccount = bill.debitAccountId?.let { accountMap[it] },
                creditAccount = bill.creditAccountId?.let { accountMap[it] },
                category = bill.categoryId?.let { categoryMap[it] },
                subCategory = bill.subCategoryId?.let { categoryMap[it] }
            )
        }
    }

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

    suspend fun insertAccount(account: Account): Long {
        val finalAcc = if (account.parentId == null) account.copy(initialBalance = 0.0) else account
        return accountDao.insertAccount(finalAcc)
    }

    suspend fun updateAccount(account: Account) {
        val finalAcc = if (account.parentId == null) account.copy(initialBalance = 0.0) else account
        accountDao.updateAccount(finalAcc)
    }

    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    suspend fun ensureOthersGroupIntegrity() {
        val catSnapshot = categoryDao.getAllCategoriesSnapshot()

        // 1. Ensure "Others" group & subcategory exist for EXPENSE
        var othersExpenseGroup = catSnapshot.firstOrNull {
            it.type == CategoryType.EXPENSE && it.parentId == null && it.nameEn.equals("Others", ignoreCase = true)
        }
        if (othersExpenseGroup == null) {
            val id = categoryDao.insertCategory(
                Category(
                    nameEn = "Others",
                    nameBn = "অন্যান্য",
                    type = CategoryType.EXPENSE,
                    parentId = null,
                    iconName = "MoreHoriz",
                    colorHex = "#9E9E9E",
                    isSystem = true
                )
            )
            othersExpenseGroup = Category(
                id = id,
                nameEn = "Others",
                nameBn = "অন্যান্য",
                type = CategoryType.EXPENSE,
                parentId = null,
                iconName = "MoreHoriz",
                colorHex = "#9E9E9E",
                isSystem = true
            )
        }

        val hasExpenseSub = catSnapshot.any {
            it.parentId == othersExpenseGroup.id && it.nameEn.equals("Others", ignoreCase = true)
        }
        if (!hasExpenseSub) {
            categoryDao.insertCategory(
                Category(
                    nameEn = "Others",
                    nameBn = "অন্যান্য",
                    type = CategoryType.EXPENSE,
                    parentId = othersExpenseGroup.id,
                    iconName = "MoreHoriz",
                    colorHex = "#9E9E9E",
                    isSystem = true
                )
            )
        }

        // 2. Ensure "Others" group & subcategory exist for INCOME
        var othersIncomeGroup = catSnapshot.firstOrNull {
            it.type == CategoryType.INCOME && it.parentId == null && it.nameEn.equals("Others", ignoreCase = true)
        }
        if (othersIncomeGroup == null) {
            val id = categoryDao.insertCategory(
                Category(
                    nameEn = "Others",
                    nameBn = "অন্যান্য",
                    type = CategoryType.INCOME,
                    parentId = null,
                    iconName = "MoreHoriz",
                    colorHex = "#9E9E9E",
                    isSystem = true
                )
            )
            othersIncomeGroup = Category(
                id = id,
                nameEn = "Others",
                nameBn = "অন্যান্য",
                type = CategoryType.INCOME,
                parentId = null,
                iconName = "MoreHoriz",
                colorHex = "#9E9E9E",
                isSystem = true
            )
        }

        val hasIncomeSub = catSnapshot.any {
            it.parentId == othersIncomeGroup.id && it.nameEn.equals("Others", ignoreCase = true)
        }
        if (!hasIncomeSub) {
            categoryDao.insertCategory(
                Category(
                    nameEn = "Others",
                    nameBn = "অন্যান্য",
                    type = CategoryType.INCOME,
                    parentId = othersIncomeGroup.id,
                    iconName = "MoreHoriz",
                    colorHex = "#9E9E9E",
                    isSystem = true
                )
            )
        }

        // 3. Any category that has parentId == null and has NO children and is not an Others group:
        // Automatically join with the "Others" group of its type
        val freshSnapshot = categoryDao.getAllCategoriesSnapshot()
        val allParentIds = freshSnapshot.mapNotNull { it.parentId }.toSet()
        freshSnapshot.forEach { cat ->
            if (cat.parentId == null && !allParentIds.contains(cat.id)) {
                if (!cat.nameEn.equals("Others", ignoreCase = true)) {
                    val targetGroupId = if (cat.type == CategoryType.EXPENSE) othersExpenseGroup.id else othersIncomeGroup.id
                    categoryDao.updateCategory(cat.copy(parentId = targetGroupId))
                }
            }
        }

        // 4. Ensure account groups (parentId == null) have 0 initial balance
        val accountsSnapshot = accountDao.getAllAccountsSnapshot()
        accountsSnapshot.forEach { acc ->
            if (acc.parentId == null && acc.initialBalance != 0.0) {
                accountDao.updateAccount(acc.copy(initialBalance = 0.0))
            }
        }
    }

    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    suspend fun insertRecurringBill(bill: RecurringBill): Long = recurringBillDao.insertBill(bill)
    suspend fun updateRecurringBill(bill: RecurringBill) = recurringBillDao.updateBill(bill)
    suspend fun deleteRecurringBill(bill: RecurringBill) = recurringBillDao.deleteBill(bill)

    suspend fun payRecurringBill(bill: RecurringBill) {
        // 1. Record journal transaction
        val tx = Transaction(
            type = bill.type,
            amount = bill.amount,
            dateEpochMs = System.currentTimeMillis(),
            debitAccountId = bill.debitAccountId,
            creditAccountId = bill.creditAccountId,
            categoryId = bill.categoryId,
            subCategoryId = bill.subCategoryId,
            payeeOrPayer = bill.payeeOrPayer,
            note = "[Bill Paid] ${bill.title}: ${bill.note}"
        )
        transactionDao.insertTransaction(tx)

        // 2. Advance due date
        val nextDue = com.example.util.DateUtils.calculateNextDueDate(bill.nextDueDateEpochMs, bill.recurrencePeriod)
        recurringBillDao.updateBillDueDate(
            id = bill.id,
            nextDue = nextDue,
            newStatus = BillStatus.PAID,
            recordedTime = System.currentTimeMillis()
        )
    }
}
