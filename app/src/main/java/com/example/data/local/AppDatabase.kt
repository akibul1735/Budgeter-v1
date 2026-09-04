package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.MonthlyBudget
import com.example.data.model.RecurringBill
import com.example.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Account::class,
        Category::class,
        Transaction::class,
        RecurringBill::class,
        MonthlyBudget::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringBillDao(): RecurringBillDao
    abstract fun monthlyBudgetDao(): MonthlyBudgetDao

    companion object {
        @Volatile
        private var REAL_INSTANCE: AppDatabase? = null

        @Volatile
        private var DEMO_INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN status TEXT NOT NULL DEFAULT 'NONE'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `monthly_budgets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `year` INTEGER NOT NULL,
                        `month` INTEGER NOT NULL,
                        `itemType` TEXT NOT NULL,
                        `itemId` INTEGER NOT NULL,
                        `budgetedAmount` REAL NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_monthly_budgets_year_month_itemType_itemId` ON `monthly_budgets` (`year`, `month`, `itemType`, `itemId`)"
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope, isDemoMode: Boolean = true): AppDatabase {
            return if (isDemoMode) {
                getDemoDatabase(context, scope)
            } else {
                getRealDatabase(context, scope)
            }
        }

        fun getRealDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return REAL_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgeter_double_entry_db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            REAL_INSTANCE?.let { database ->
                                scope.launch(Dispatchers.IO) {
                                    DatabaseInitializer.seedInitialData(
                                        database.accountDao(),
                                        database.categoryDao(),
                                        database.transactionDao()
                                    )
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(true)
                    .build()
                REAL_INSTANCE = instance
                instance
            }
        }

        fun getDemoDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return DEMO_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgeter_demo_db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            DEMO_INSTANCE?.let { database ->
                                scope.launch(Dispatchers.IO) {
                                    DemoDatabaseInitializer.seedDemoData(
                                        database.accountDao(),
                                        database.categoryDao(),
                                        database.transactionDao(),
                                        database.recurringBillDao(),
                                        database.monthlyBudgetDao()
                                    )
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(true)
                    .build()
                DEMO_INSTANCE = instance

                // Verify demo data is populated
                scope.launch(Dispatchers.IO) {
                    DemoDatabaseInitializer.seedDemoData(
                        instance.accountDao(),
                        instance.categoryDao(),
                        instance.transactionDao(),
                        instance.recurringBillDao(),
                        instance.monthlyBudgetDao()
                    )
                }

                instance
            }
        }

        suspend fun resetDemoDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            val db = getDemoDatabase(context, scope)
            db.clearAllTables()
            DemoDatabaseInitializer.seedDemoData(
                db.accountDao(),
                db.categoryDao(),
                db.transactionDao(),
                db.recurringBillDao(),
                db.monthlyBudgetDao()
            )
            return db
        }
    }
}
