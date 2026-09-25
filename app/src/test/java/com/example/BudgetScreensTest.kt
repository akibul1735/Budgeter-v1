package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import com.example.ui.dialogs.AddEditTransactionSheet
import com.example.ui.screens.CategoryBudgetTrackingItem
import com.example.ui.screens.CategoryGroupBudgetTracking
import com.example.ui.screens.LedgerScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

        assertNotNull(savedTx)
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

        assertNotNull(savedTx)
        assertEquals(TransactionType.INCOME, savedTx?.type)
        assertEquals(-200.0, savedTx?.amount ?: 0.0, 0.001)
    }

    @Test
    fun testLedgerScreenFilteredTotalAmount() {
        val cat = Category(id = 1, nameEn = "Donation", nameBn = "দান", iconName = "VolunteerActivism", colorHex = "#4CAF50", type = CategoryType.EXPENSE, parentId = null)
        val acc = Account(id = 1, nameEn = "Cash", nameBn = "ক্যাশ", type = AccountType.ASSET)
        val tx1 = Transaction(id = 1, amount = 20.0, payeeOrPayer = "Mosque Donation", type = TransactionType.EXPENSE, categoryId = 1, debitAccountId = null, creditAccountId = 1, dateEpochMs = System.currentTimeMillis())
        val tx2 = Transaction(id = 2, amount = 50.0, payeeOrPayer = "Mosque Donation", type = TransactionType.EXPENSE, categoryId = 1, debitAccountId = null, creditAccountId = 1, dateEpochMs = System.currentTimeMillis())
        val tx3 = Transaction(id = 3, amount = 50.0, payeeOrPayer = "Mosque Donation", type = TransactionType.EXPENSE, categoryId = 1, debitAccountId = null, creditAccountId = 1, dateEpochMs = System.currentTimeMillis())
        val tx4 = Transaction(id = 4, amount = 50.0, payeeOrPayer = "Mosque Donation", type = TransactionType.EXPENSE, categoryId = 1, debitAccountId = null, creditAccountId = 1, dateEpochMs = System.currentTimeMillis())
        val otherTx = Transaction(id = 5, amount = 500.0, payeeOrPayer = "Groceries", type = TransactionType.EXPENSE, categoryId = 1, debitAccountId = null, creditAccountId = 1, dateEpochMs = System.currentTimeMillis())

        val txList = listOf(
            TransactionWithDetails(tx1, null, acc, cat, null),
            TransactionWithDetails(tx2, null, acc, cat, null),
            TransactionWithDetails(tx3, null, acc, cat, null),
            TransactionWithDetails(tx4, null, acc, cat, null),
            TransactionWithDetails(otherTx, null, acc, cat, null)
        )

        composeTestRule.setContent {
            LedgerScreen(
                transactions = txList,
                languageMode = LanguageMode.ENGLISH,
                allCategories = listOf(cat),
                allAccounts = listOf(acc),
                accountsWithBalances = listOf(AccountWithBalance(acc, 1000.0)),
                onAddTransactionClick = {},
                onTransactionClick = {}
            )
        }

        // Search for "Mosque Donation" (similar transactions view)
        composeTestRule.onNodeWithTag("header_search_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("header_search_input").performTextInput("Mosque Donation")
        composeTestRule.waitForIdle()

        // Verify the filtered total amount pill and amount text are displayed
        composeTestRule.onNodeWithTag("filtered_total_amount_pill").assertExists()
        composeTestRule.onNodeWithTag("filtered_total_amount_text").assertExists()
    }

    @Test
    fun testGroupRemainingCalculationMatchesUserRule() {
        // User example:
        // Group Budget: 20,000৳
        // Total Actual Spent: 21,000৳
        // Category 1 remaining: 1,000৳
        // Category 2 remaining: 1,000৳
        // Group Remaining: 2,000৳
        // Rule: Group Remaining = SUM(all Category Remaining amounts)
        val catParent = Category(id = 1, nameEn = "Living", nameBn = "জীবনযাপন", iconName = "Home", colorHex = "#2196F3", type = CategoryType.EXPENSE, parentId = null)
        val cat1 = Category(id = 2, nameEn = "Groceries", nameBn = "মুদি", iconName = "ShoppingCart", colorHex = "#2196F3", type = CategoryType.EXPENSE, parentId = 1)
        val cat2 = Category(id = 3, nameEn = "Utilities", nameBn = "ইউটিলিটি", iconName = "Bolt", colorHex = "#2196F3", type = CategoryType.EXPENSE, parentId = 1)
        val cat3 = Category(id = 4, nameEn = "Dining", nameBn = "খাওয়া", iconName = "Restaurant", colorHex = "#2196F3", type = CategoryType.EXPENSE, parentId = 1)

        // Cat 1: Budget 5,000, Spent 4,000 => Remaining 1,000
        val item1 = CategoryBudgetTrackingItem(
            category = cat1,
            spentAmount = 4000.0,
            budgetLimit = 5000.0,
            isEnabled = true,
            transactions = emptyList()
        )
        // Cat 2: Budget 5,000, Spent 4,000 => Remaining 1,000
        val item2 = CategoryBudgetTrackingItem(
            category = cat2,
            spentAmount = 4000.0,
            budgetLimit = 5000.0,
            isEnabled = true,
            transactions = emptyList()
        )
        // Cat 3: Budget 10,000, Spent 13,000 => Over budget by 3,000, Remaining = 0
        val item3 = CategoryBudgetTrackingItem(
            category = cat3,
            spentAmount = 13000.0,
            budgetLimit = 10000.0,
            isEnabled = true,
            transactions = emptyList()
        )

        val group = CategoryGroupBudgetTracking(
            parentCategory = catParent,
            groupNameEn = "Living",
            groupNameBn = "জীবনযাপন",
            items = listOf(item1, item2, item3)
        )

        // Total Group Budget = 20,000
        assertEquals(20000.0, group.totalBudget, 0.001)
        // Total Actual Spent = 21,000
        assertEquals(21000.0, group.totalSpent, 0.001)
        // Group is over budget overall
        assertEquals(true, group.isOverBudget)

        // Category-level remaining
        assertEquals(1000.0, item1.remainingAmount, 0.001)
        assertEquals(1000.0, item2.remainingAmount, 0.001)
        assertEquals(0.0, item3.remainingAmount, 0.001)

        // Group Remaining = SUM(all Category Remaining amounts) = 2,000৳
        assertEquals(2000.0, group.remainingAmount, 0.001)
    }
}
