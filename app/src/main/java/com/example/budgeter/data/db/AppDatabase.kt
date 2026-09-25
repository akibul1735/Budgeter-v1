package com.example.budgeter.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgeter.data.model.Account
import com.example.budgeter.data.model.Budget
import com.example.budgeter.data.model.Category
import com.example.budgeter.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Category::class, Account::class, Budget::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgeter_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: AppDatabase) {
                // Default Categories
                val defaultCategories = listOf(
                    Category(name = "Groceries & Food", iconName = "restaurant", colorHex = "#006C4C", isExpense = true, isIncome = false),
                    Category(name = "Housing & Rent", iconName = "home", colorHex = "#1B5E20", isExpense = true, isIncome = false),
                    Category(name = "Transportation", iconName = "directions_car", colorHex = "#0288D1", isExpense = true, isIncome = false),
                    Category(name = "Utilities & Bills", iconName = "bolt", colorHex = "#F57C00", isExpense = true, isIncome = false),
                    Category(name = "Shopping & Personal", iconName = "shopping_bag", colorHex = "#7B1FA2", isExpense = true, isIncome = false),
                    Category(name = "Healthcare", iconName = "medical_services", colorHex = "#D32F2F", isExpense = true, isIncome = false),
                    Category(name = "Education", iconName = "school", colorHex = "#303F9F", isExpense = true, isIncome = false),
                    Category(name = "Entertainment", iconName = "movie", colorHex = "#C2185B", isExpense = true, isIncome = false),
                    Category(name = "Salary & Wage", iconName = "payments", colorHex = "#2E7D32", isExpense = false, isIncome = true),
                    Category(name = "Freelance & Business", iconName = "work", colorHex = "#00796B", isExpense = false, isIncome = true),
                    Category(name = "Investments & Dividends", iconName = "trending_up", colorHex = "#D4AF37", isExpense = false, isIncome = true),
                    Category(name = "Gift & Others", iconName = "card_giftcard", colorHex = "#5D4037", isExpense = true, isIncome = true)
                )
                db.categoryDao().insertAll(defaultCategories)

                // Default Accounts (Cash, Bank, Mobile Money)
                val defaultAccounts = listOf(
                    Account(name = "Cash Wallet", type = "CASH", initialBalance = 5000.0, currentBalance = 5000.0, colorHex = "#006C4C"),
                    Account(name = "Main Bank Account", type = "BANK", initialBalance = 45000.0, currentBalance = 45000.0, colorHex = "#0288D1"),
                    Account(name = "bKash / Nagad Wallet", type = "MOBILE_MONEY", initialBalance = 8500.0, currentBalance = 8500.0, colorHex = "#E91E63")
                )
                db.accountDao().insertAll(defaultAccounts)
            }
        }
    }
}
