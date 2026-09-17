package com.example

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.screens.CashFlowScreen
import com.example.util.CashFlowHelper
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CashFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testCashFlowMathematicalEngine() {
        val cashAcc = Account(id = 1, nameEn = "Cash", nameBn = "ক্যাশ", type = AccountType.ASSET, initialBalance = 1000.0)
        val bankAcc = Account(id = 2, nameEn = "Bank", nameBn = "ব্যাংক", type = AccountType.ASSET, initialBalance = 5000.0)
        val loanAcc = Account(id = 3, nameEn = "Personal Loan", nameBn = "ব্যক্তিগত ঋণ", type = AccountType.LIABILITY, initialBalance = 2000.0)

        val salaryCat = Category(id = 10, nameEn = "Salary", nameBn = "বেতন", iconName = "Payments", colorHex = "#4CAF50", type = CategoryType.INCOME)
        val foodCat = Category(id = 20, nameEn = "Food", nameBn = "খাবার", iconName = "Restaurant", colorHex = "#FF5722", type = CategoryType.EXPENSE)

        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val startMs = now - (10 * oneDayMs)
        val endMs = now + (10 * oneDayMs)

        val txPrior = TransactionWithDetails(
            transaction = Transaction(id = 1, type = TransactionType.INCOME, amount = 2000.0, dateEpochMs = startMs - 5000, debitAccountId = 1, categoryId = 10),
            category = salaryCat,
            subCategory = null,
            debitAccount = cashAcc,
            creditAccount = null
        )

        val txInflow = TransactionWithDetails(
            transaction = Transaction(id = 2, type = TransactionType.INCOME, amount = 8000.0, dateEpochMs = now - oneDayMs, debitAccountId = 2, categoryId = 10),
            category = salaryCat,
            subCategory = null,
            debitAccount = bankAcc,
            creditAccount = null
        )

        val txOutflow = TransactionWithDetails(
            transaction = Transaction(id = 3, type = TransactionType.EXPENSE, amount = 1500.0, dateEpochMs = now, creditAccountId = 1, categoryId = 20),
            category = foodCat,
            subCategory = null,
            debitAccount = null,
            creditAccount = cashAcc
        )

        val allTxs = listOf(txPrior, txInflow, txOutflow)
        val allAccounts = listOf(cashAcc, bankAcc, loanAcc)
        val allCategories = listOf(salaryCat, foodCat)

        val summary = CashFlowHelper.calculateCashFlow(
            allTransactions = allTxs,
            allAccounts = allAccounts,
            allCategories = allCategories,
            startMs = startMs,
            endMs = endMs,
            languageMode = LanguageMode.ENGLISH
        )

        // Opening balance should include initial balances (1000 + 5000) + prior income (2000) = 8000.0
        assertEquals(8000.0, summary.openingBalance, 0.001)

        // Total Inflow in period = 8000.0
        assertEquals(8000.0, summary.totalInflow, 0.001)

        // Total Outflow in period = 1500.0
        assertEquals(1500.0, summary.totalOutflow, 0.001)

        // Net Cash Flow = 8000 - 1500 = 6500.0
        assertEquals(6500.0, summary.netCashFlow, 0.001)

        // Closing Balance = 8000 + 6500 = 14500.0
        assertEquals(14500.0, summary.closingBalance, 0.001)
    }

    @Test
    fun testCashFlowScreenRenders() {
        val cashAcc = Account(id = 1, nameEn = "Cash", nameBn = "ক্যাশ", type = AccountType.ASSET, initialBalance = 1000.0)
        val accWithBal = AccountWithBalance(account = cashAcc, currentBalance = 1000.0)

        composeTestRule.setContent {
            CashFlowScreen(
                transactionsWithDetails = emptyList(),
                allAccounts = listOf(cashAcc),
                accountsWithBalances = listOf(accWithBal),
                allCategories = emptyList(),
                languageMode = LanguageMode.ENGLISH,
                onOpenDrawer = {},
                onTransactionClick = {},
                onAddTransactionClick = {}
            )
        }

        composeTestRule.onNodeWithTag("cash_flow_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cash_flow_hero_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cash_flow_fab_add").assertIsDisplayed()
    }
}
