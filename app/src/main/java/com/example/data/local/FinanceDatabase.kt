package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.BillFrequency
import com.example.data.model.Category
import com.example.data.model.RecurringBill
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Account::class,
        Category::class,
        Transaction::class,
        RecurringBill::class,
        SavingsGoal::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "bluecoins_finance.db"
                )
                    .addCallback(FinanceDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class FinanceDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.financeDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(dao: FinanceDao) {
            // 1. Initial Accounts
            val checkingId = dao.insertAccount(
                Account(
                    name = "Primary Checking",
                    type = AccountType.CHECKING,
                    balance = 4850.75,
                    currency = "USD",
                    colorHex = "#1A73E8",
                    iconName = "AccountBalance"
                )
            )
            val savingsId = dao.insertAccount(
                Account(
                    name = "High-Yield Savings",
                    type = AccountType.SAVINGS,
                    balance = 16240.00,
                    currency = "USD",
                    colorHex = "#10B981",
                    iconName = "Savings"
                )
            )
            val walletId = dao.insertAccount(
                Account(
                    name = "Cash Wallet",
                    type = AccountType.CASH,
                    balance = 280.00,
                    currency = "USD",
                    colorHex = "#F59E0B",
                    iconName = "Wallet"
                )
            )
            val creditCardId = dao.insertAccount(
                Account(
                    name = "Sapphire Credit Card",
                    type = AccountType.CREDIT_CARD,
                    balance = -640.50,
                    currency = "USD",
                    colorHex = "#EC4899",
                    iconName = "CreditCard"
                )
            )
            val investmentId = dao.insertAccount(
                Account(
                    name = "Vanguard Index Fund",
                    type = AccountType.INVESTMENT,
                    balance = 28900.00,
                    currency = "USD",
                    colorHex = "#6366F1",
                    iconName = "TrendingUp"
                )
            )
            val loanId = dao.insertAccount(
                Account(
                    name = "Student Loan",
                    type = AccountType.LOAN,
                    balance = -4200.00,
                    currency = "USD",
                    colorHex = "#EF4444",
                    iconName = "ReceiptLong"
                )
            )

            // 2. Initial Categories
            val catGroceries = dao.insertCategory(
                Category(
                    name = "Groceries & Supermarket",
                    type = TransactionType.EXPENSE,
                    iconName = "ShoppingCart",
                    colorHex = "#10B981",
                    monthlyBudget = 600.0,
                    isDefault = true
                )
            )
            val catDining = dao.insertCategory(
                Category(
                    name = "Dining & Cafes",
                    type = TransactionType.EXPENSE,
                    iconName = "Restaurant",
                    colorHex = "#F97316",
                    monthlyBudget = 350.0,
                    isDefault = true
                )
            )
            val catHousing = dao.insertCategory(
                Category(
                    name = "Housing & Rent",
                    type = TransactionType.EXPENSE,
                    iconName = "Home",
                    colorHex = "#3B82F6",
                    monthlyBudget = 1400.0,
                    isDefault = true
                )
            )
            val catUtilities = dao.insertCategory(
                Category(
                    name = "Utilities & Bills",
                    type = TransactionType.EXPENSE,
                    iconName = "Bolt",
                    colorHex = "#EAB308",
                    monthlyBudget = 220.0,
                    isDefault = true
                )
            )
            val catTransport = dao.insertCategory(
                Category(
                    name = "Transport & Fuel",
                    type = TransactionType.EXPENSE,
                    iconName = "DirectionsCar",
                    colorHex = "#06B6D4",
                    monthlyBudget = 250.0,
                    isDefault = true
                )
            )
            val catEntertainment = dao.insertCategory(
                Category(
                    name = "Entertainment & Subs",
                    type = TransactionType.EXPENSE,
                    iconName = "Movie",
                    colorHex = "#A855F7",
                    monthlyBudget = 150.0,
                    isDefault = true
                )
            )
            val catHealth = dao.insertCategory(
                Category(
                    name = "Health & Fitness",
                    type = TransactionType.EXPENSE,
                    iconName = "FitnessCenter",
                    colorHex = "#EC4899",
                    monthlyBudget = 180.0,
                    isDefault = true
                )
            )
            val catShopping = dao.insertCategory(
                Category(
                    name = "Personal & Shopping",
                    type = TransactionType.EXPENSE,
                    iconName = "Checkroom",
                    colorHex = "#14B8A6",
                    monthlyBudget = 300.0,
                    isDefault = true
                )
            )

            // Income Categories
            val catSalary = dao.insertCategory(
                Category(
                    name = "Salary & Wages",
                    type = TransactionType.INCOME,
                    iconName = "Payments",
                    colorHex = "#10B981",
                    monthlyBudget = 0.0,
                    isDefault = true
                )
            )
            val catFreelance = dao.insertCategory(
                Category(
                    name = "Freelance & Consulting",
                    type = TransactionType.INCOME,
                    iconName = "Work",
                    colorHex = "#6366F1",
                    monthlyBudget = 0.0,
                    isDefault = true
                )
            )
            val catDividends = dao.insertCategory(
                Category(
                    name = "Dividends & Returns",
                    type = TransactionType.INCOME,
                    iconName = "ShowChart",
                    colorHex = "#3B82F6",
                    monthlyBudget = 0.0,
                    isDefault = true
                )
            )

            // 3. Seed Transactions across recent days
            val now = System.currentTimeMillis()
            val dayMs = 24L * 3600 * 1000

            dao.insertTransaction(
                Transaction(
                    title = "Monthly Tech Corp Salary",
                    amount = 4500.00,
                    type = TransactionType.INCOME,
                    accountId = checkingId,
                    categoryId = catSalary,
                    dateEpochMs = now - (2 * dayMs),
                    notes = "Bi-weekly paycheck direct deposit",
                    tags = "Salary,DirectDeposit",
                    status = TransactionStatus.RECONCILED
                )
            )
            dao.insertTransaction(
                Transaction(
                    title = "Whole Foods Market",
                    amount = 124.60,
                    type = TransactionType.EXPENSE,
                    accountId = checkingId,
                    categoryId = catGroceries,
                    dateEpochMs = now - (1 * dayMs),
                    notes = "Weekly organic groceries & pantry items",
                    tags = "Food,Healthy",
                    status = TransactionStatus.CLEARED
                )
            )
            dao.insertTransaction(
                Transaction(
                    title = "Blue Bottle Coffee",
                    amount = 6.75,
                    type = TransactionType.EXPENSE,
                    accountId = walletId,
                    categoryId = catDining,
                    dateEpochMs = now - (4 * 3600 * 1000),
                    notes = "Oat milk cappuccino",
                    tags = "Coffee",
                    status = TransactionStatus.CLEARED
                )
            )
            dao.insertTransaction(
                Transaction(
                    title = "Shell Gas Station",
                    amount = 48.20,
                    type = TransactionType.EXPENSE,
                    accountId = creditCardId,
                    categoryId = catTransport,
                    dateEpochMs = now - (2 * dayMs),
                    notes = "Full tank petrol",
                    tags = "Car,Commute",
                    status = TransactionStatus.CLEARED
                )
            )
            dao.insertTransaction(
                Transaction(
                    title = "Apartment Rent Payment",
                    amount = 1350.00,
                    type = TransactionType.EXPENSE,
                    accountId = checkingId,
                    categoryId = catHousing,
                    dateEpochMs = now - (5 * dayMs),
                    notes = "September apartment rent",
                    tags = "Rent,Fixed",
                    status = TransactionStatus.RECONCILED
                )
            )
            dao.insertTransaction(
                Transaction(
                    title = "Transfer to High-Yield Savings",
                    amount = 750.00,
                    type = TransactionType.TRANSFER,
                    accountId = checkingId,
                    toAccountId = savingsId,
                    dateEpochMs = now - (3 * dayMs),
                    notes = "Monthly automated savings rule",
                    tags = "Savings,Transfer"
                )
            )
            dao.insertTransaction(
                Transaction(
                    title = "Netflix & Spotify Subscription",
                    amount = 26.98,
                    type = TransactionType.EXPENSE,
                    accountId = creditCardId,
                    categoryId = catEntertainment,
                    dateEpochMs = now - (4 * dayMs),
                    notes = "Monthly family plans",
                    tags = "Subscription,Streaming"
                )
            )
            dao.insertTransaction(
                Transaction(
                    title = "Client UI Consulting",
                    amount = 850.00,
                    type = TransactionType.INCOME,
                    accountId = checkingId,
                    categoryId = catFreelance,
                    dateEpochMs = now - (6 * dayMs),
                    notes = "Design system sprint milestone",
                    tags = "Freelance"
                )
            )

            // 4. Recurring Bills
            dao.insertRecurringBill(
                RecurringBill(
                    title = "Internet & Fiber (AT&T)",
                    amount = 70.00,
                    categoryId = catUtilities,
                    accountId = checkingId,
                    frequency = BillFrequency.MONTHLY,
                    nextDueDateEpochMs = now + (3 * dayMs),
                    isAutoPay = true,
                    notes = "Gigabit fiber plan"
                )
            )
            dao.insertRecurringBill(
                RecurringBill(
                    title = "Electricity & Power",
                    amount = 95.50,
                    categoryId = catUtilities,
                    accountId = checkingId,
                    frequency = BillFrequency.MONTHLY,
                    nextDueDateEpochMs = now + (7 * dayMs),
                    isAutoPay = false,
                    notes = "Summer AC usage bill"
                )
            )
            dao.insertRecurringBill(
                RecurringBill(
                    title = "Gym Membership",
                    amount = 55.00,
                    categoryId = catHealth,
                    accountId = creditCardId,
                    frequency = BillFrequency.MONTHLY,
                    nextDueDateEpochMs = now + (12 * dayMs),
                    isAutoPay = true,
                    notes = "Equinox pass"
                )
            )

            // 5. Savings Goals
            dao.insertSavingsGoal(
                SavingsGoal(
                    title = "Emergency Fund (6 Months)",
                    targetAmount = 20000.0,
                    currentAmount = 16240.0,
                    targetDateEpochMs = now + (180L * dayMs),
                    colorHex = "#10B981",
                    iconName = "Shield",
                    notes = "Safety net for living expenses"
                )
            )
            dao.insertSavingsGoal(
                SavingsGoal(
                    title = "Japan Autumn Vacation",
                    targetAmount = 4500.0,
                    currentAmount = 2800.0,
                    targetDateEpochMs = now + (75L * dayMs),
                    colorHex = "#EC4899",
                    iconName = "Flight",
                    notes = "Tokyo & Kyoto travel fund"
                )
            )
            dao.insertSavingsGoal(
                SavingsGoal(
                    title = "New Electric Vehicle Deposit",
                    targetAmount = 8000.0,
                    currentAmount = 3500.0,
                    targetDateEpochMs = now + (240L * dayMs),
                    colorHex = "#3B82F6",
                    iconName = "DirectionsCar",
                    notes = "Down payment target"
                )
            )
        }
    }
}
