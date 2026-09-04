package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
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
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.BudgetTrackingScreen
import com.example.ui.screens.MainAppContainer
import com.example.ui.viewmodel.BudgetViewModel
import androidx.compose.ui.test.performTextInput
import com.example.ui.dialogs.AddEditTransactionSheet
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BudgetScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRevertExpenseWithPlusSign() {
        val cat = Category(id = 1, nameEn = "Food", nameBn = "খাবার", iconName = "Restaurant", colorHex = "#FF5722", type = CategoryType.EXPENSE, parentId = null)
        val acc = Account(id = 1, nameEn = "Cash", nameBn = "ক্যাশ", type = AccountType.ASSET)
        var savedTx: Transaction? = null

        composeTestRule.setContent {
            AddEditTransactionSheet(
                accounts = listOf(acc),
                categories = listOf(cat),
                languageMode = LanguageMode.ENGLISH,
                onDismiss = {},
                onSave = { savedTx = it }
            )
        }

        // Enter amount 150
        composeTestRule.onNodeWithTag("tx_amount_input").performTextInput("150")
        // Tap the '+' button to revert/decrease expense (refund)
        composeTestRule.onNodeWithTag("tx_sign_plus_btn").performClick()
        // Save
        composeTestRule.onNodeWithTag("save_transaction_btn").performClick()

        org.junit.Assert.assertNotNull(savedTx)
        assertEquals(TransactionType.EXPENSE, savedTx?.type)
        assertEquals(-150.0, savedTx?.amount ?: 0.0, 0.001)
    }

    @Test
    fun testRevertIncomeWithMinusSign() {
        val cat = Category(id = 2, nameEn = "Salary", nameBn = "বেতন", iconName = "Payments", colorHex = "#4CAF50", type = CategoryType.INCOME, parentId = null)
        val acc = Account(id = 1, nameEn = "Cash", nameBn = "ক্যাশ", type = AccountType.ASSET)
        var savedTx: Transaction? = null

        composeTestRule.setContent {
            AddEditTransactionSheet(
                accounts = listOf(acc),
                categories = listOf(cat),
                languageMode = LanguageMode.ENGLISH,
                existingTransaction = Transaction(
                    id = 0,
                    amount = 200.0,
                    type = TransactionType.INCOME,
                    categoryId = 2,
                    debitAccountId = 1,
                    dateEpochMs = System.currentTimeMillis()
                ),
                onDismiss = {},
                onSave = { savedTx = it }
            )
        }

        // Tap the '-' button to revert/decrease income
        composeTestRule.onNodeWithTag("tx_sign_minus_btn").performClick()
        // Save
        composeTestRule.onNodeWithTag("save_transaction_btn").performClick()

        org.junit.Assert.assertNotNull(savedTx)
        assertEquals(TransactionType.INCOME, savedTx?.type)
        assertEquals(-200.0, savedTx?.amount ?: 0.0, 0.001)
    }

    @Test
    fun testBudgetTrackingScreenWithMultipleCategoriesAndDuplicateNames() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = BudgetViewModel(app)

        val cat1 = Category(id = 1, nameEn = "Food", nameBn = "খাবার", iconName = "Restaurant", colorHex = "#FF5722", type = CategoryType.EXPENSE, parentId = null)
        val cat2 = Category(id = 2, nameEn = "Food", nameBn = "খাবার", iconName = "Restaurant", colorHex = "#FF5722", type = CategoryType.EXPENSE, parentId = null)
        val cat3 = Category(id = 3, nameEn = "", nameBn = "অন্যান্য", iconName = "Category", colorHex = "#9E9E9E", type = CategoryType.EXPENSE, parentId = null)
        val cat4 = Category(id = 4, nameEn = "Dining Out", nameBn = "রেস্টুরেন্ট", iconName = "Restaurant", colorHex = "#FF5722", type = CategoryType.EXPENSE, parentId = 1)
        val account = Account(id = 1, nameEn = "Cash", nameBn = "ক্যাশ", type = AccountType.ASSET)
        val tx = Transaction(id = 1, amount = 100.0, type = TransactionType.EXPENSE, categoryId = 1, subCategoryId = 4, debitAccountId = null, creditAccountId = 1, dateEpochMs = System.currentTimeMillis())
        val txWithDetails = TransactionWithDetails(tx, debitAccount = null, creditAccount = account, category = cat1, subCategory = cat4)

        composeTestRule.setContent {
            BudgetTrackingScreen(
                viewModel = viewModel,
                allCategories = listOf(cat1, cat2, cat3, cat4),
                allAccounts = listOf(account),
                accountsWithBalances = listOf(AccountWithBalance(account, 1000.0)),
                transactionsWithDetails = listOf(txWithDetails),
                monthlyBudgets = listOf(MonthlyBudget(id = 1, year = 2026, month = 9, itemType = "EXPENSE", itemId = 4, budgetedAmount = 500.0, isEnabled = true)),
                selectedYear = 2026,
                selectedMonth = 9,
                languageMode = LanguageMode.BANGLA,
                onNavigateToBudgetMaker = {},
                onAddTransactionWithCategory = {},
                onEditTransaction = {}
            )
        }

        composeTestRule.onNodeWithTag("budget_tracking_screen").assertExists()
    }

    @Test
    fun testNavigateToBudgetTabInMainAppContainer() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = BudgetViewModel(app)

        composeTestRule.setContent {
            MainAppContainer(viewModel = viewModel)
        }

        // Tap the Budget tab in the navigation bar using its unique test tag
        composeTestRule.onNodeWithTag("bottom_nav_budget").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("budget_tracking_screen").assertExists()
    }
}
