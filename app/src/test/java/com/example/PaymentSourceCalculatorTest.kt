package com.example

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.MonthlyBudget
import com.example.data.model.RequirementCalculationBasis
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.util.PaymentSourceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentSourceCalculatorTest {

    @Test
    fun testCollectsActualBalancesFromBalanceSheet() {
        val parentAcc = Account(
            id = 1L,
            nameEn = "Bank Accounts",
            nameBn = "ব্যাংক হিসাব",
            type = AccountType.ASSET,
            initialBalance = 1000.0,
            isActive = true
        )
        val childAcc = Account(
            id = 2L,
            nameEn = "Checking",
            nameBn = "চলতি হিসাব",
            type = AccountType.ASSET,
            parentId = 1L,
            initialBalance = 5000.0,
            isActive = true
        )
        val allAccounts = listOf(parentAcc, childAcc)

        // Transactions: child account receives 2000, spends 1500
        val tx1 = Transaction(
            id = 101L,
            type = TransactionType.INCOME,
            amount = 2000.0,
            dateEpochMs = System.currentTimeMillis(),
            debitAccountId = 2L
        )
        val tx2 = Transaction(
            id = 102L,
            type = TransactionType.EXPENSE,
            amount = 1500.0,
            dateEpochMs = System.currentTimeMillis(),
            creditAccountId = 2L
        )
        val allTransactions = listOf(
            TransactionWithDetails(transaction = tx1),
            TransactionWithDetails(transaction = tx2)
        )

        val awb = listOf(
            AccountWithBalance(
                account = parentAcc,
                currentBalance = 6500.0,
                subAccounts = listOf(
                    AccountWithBalance(account = childAcc, currentBalance = 5500.0)
                )
            )
        )

        val overview = PaymentSourceCalculator.calculateAnalysis(
            year = 2026,
            month = 9,
            basis = RequirementCalculationBasis.BUDGET_AMOUNT,
            allAccounts = allAccounts,
            accountsWithBalances = awb,
            allCategories = emptyList(),
            monthlyBudgets = emptyList(),
            allTransactions = allTransactions,
            selectedPaymentSourceIds = setOf(2L)
        )

        // Child account balance should be 5000 (initial) + 2000 (dr) - 1500 (cr) = 5500
        val childAnalysis = overview.accountAnalyses.first { it.account.id == 2L }
        assertEquals(5500.0, childAnalysis.currentBalance, 0.001)
    }

    @Test
    fun testExpenseDeductedAndIncomeAddedToBalance() {
        val acc = Account(
            id = 10L,
            nameEn = "Wallet",
            nameBn = "ওয়ালেট",
            type = AccountType.ASSET,
            initialBalance = 10000.0,
            isActive = true
        )
        val expenseCat = Category(
            id = 201L,
            nameEn = "Groceries",
            nameBn = "বাজার",
            type = CategoryType.EXPENSE
        )
        val incomeCat = Category(
            id = 301L,
            nameEn = "Salary",
            nameBn = "বেতন",
            type = CategoryType.INCOME
        )

        // Expense budget 4000 allocated to Wallet, Income budget 6000 allocated to Wallet
        val mbExpense = MonthlyBudget(
            year = 2026,
            month = 9,
            itemType = "ALLOC_201",
            itemId = 10L,
            budgetedAmount = 4000.0,
            isEnabled = true
        )
        val mbIncome = MonthlyBudget(
            year = 2026,
            month = 9,
            itemType = "ALLOC_301",
            itemId = 10L,
            budgetedAmount = 6000.0,
            isEnabled = true
        )

        val overview = PaymentSourceCalculator.calculateAnalysis(
            year = 2026,
            month = 9,
            basis = RequirementCalculationBasis.BUDGET_AMOUNT,
            allAccounts = listOf(acc),
            accountsWithBalances = listOf(AccountWithBalance(account = acc, currentBalance = 10000.0)),
            allCategories = listOf(expenseCat, incomeCat),
            monthlyBudgets = listOf(mbExpense, mbIncome),
            allTransactions = emptyList(),
            selectedPaymentSourceIds = setOf(10L)
        )

        val analysis = overview.accountAnalyses.first { it.account.id == 10L }
        assertEquals(10000.0, analysis.currentBalance, 0.001)
        assertEquals(4000.0, analysis.requiredExpenseAmount, 0.001)
        assertEquals(6000.0, analysis.expectedIncomeAmount, 0.001)
        // Projected balance: 10000 - 4000 + 6000 = 12000
        assertEquals(12000.0, analysis.projectedBalance, 0.001)
        assertTrue(analysis.isSurplus)
        assertEquals(12000.0, analysis.surplus, 0.001)
    }

    @Test
    fun testBudgetVsRemainingBasis() {
        val acc = Account(
            id = 5L,
            nameEn = "bKash",
            nameBn = "বিকাশ",
            type = AccountType.ASSET,
            initialBalance = 2000.0,
            isActive = true
        )
        val cat = Category(
            id = 50L,
            nameEn = "Utilities",
            nameBn = "ইউটিলিটি",
            type = CategoryType.EXPENSE
        )
        // Budget = 1000
        val mb = MonthlyBudget(
            year = 2026,
            month = 9,
            itemType = "ALLOC_50",
            itemId = 5L,
            budgetedAmount = 1000.0,
            isEnabled = true
        )
        // Spent 400 this month
        val cal = java.util.Calendar.getInstance()
        cal.set(2026, java.util.Calendar.SEPTEMBER, 15)
        val tx = Transaction(
            id = 5001L,
            type = TransactionType.EXPENSE,
            amount = 400.0,
            dateEpochMs = cal.timeInMillis,
            categoryId = 50L,
            creditAccountId = 5L
        )

        // 1. With BUDGET_AMOUNT basis: required expense = 1000
        val budgetOverview = PaymentSourceCalculator.calculateAnalysis(
            year = 2026,
            month = 9,
            basis = RequirementCalculationBasis.BUDGET_AMOUNT,
            allAccounts = listOf(acc),
            accountsWithBalances = listOf(AccountWithBalance(account = acc, currentBalance = 1600.0)),
            allCategories = listOf(cat),
            monthlyBudgets = listOf(mb),
            allTransactions = listOf(TransactionWithDetails(transaction = tx)),
            selectedPaymentSourceIds = setOf(5L)
        )
        val budgetAnalysis = budgetOverview.accountAnalyses.first { it.account.id == 5L }
        assertEquals(1000.0, budgetAnalysis.requiredExpenseAmount, 0.001)

        // 2. With REMAINING_AMOUNT basis: remaining expense = 1000 - 400 = 600
        val remainingOverview = PaymentSourceCalculator.calculateAnalysis(
            year = 2026,
            month = 9,
            basis = RequirementCalculationBasis.REMAINING_AMOUNT,
            allAccounts = listOf(acc),
            accountsWithBalances = listOf(AccountWithBalance(account = acc, currentBalance = 1600.0)),
            allCategories = listOf(cat),
            monthlyBudgets = listOf(mb),
            allTransactions = listOf(TransactionWithDetails(transaction = tx)),
            selectedPaymentSourceIds = setOf(5L)
        )
        val remainingAnalysis = remainingOverview.accountAnalyses.first { it.account.id == 5L }
        assertEquals(600.0, remainingAnalysis.requiredExpenseAmount, 0.001)
    }
}
