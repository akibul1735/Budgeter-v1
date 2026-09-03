package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.local.AccountDao
import com.example.data.local.CategoryDao
import com.example.data.local.MonthlyBudgetDao
import com.example.data.local.RecurringBillDao
import com.example.data.local.TransactionDao
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.MonthlyBudget
import com.example.data.model.RecurringBill
import com.example.data.model.Transaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BudgetBackupData(
    val version: Int = 3,
    val exportedAt: Long = System.currentTimeMillis(),
    val app: String = "Budgeter",
    val accounts: List<Account>,
    val categories: List<Category>,
    val transactions: List<Transaction>,
    val recurringBills: List<RecurringBill> = emptyList(),
    val monthlyBudgets: List<MonthlyBudget> = emptyList()
)

object BackupManager {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(BudgetBackupData::class.java)

    /**
     * Creates a JSON backup in the app's cache / files dir and returns the file
     */
    suspend fun createLocalBackupFile(
        context: Context,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null
    ): File = withContext(Dispatchers.IO) {
        val backupData = BudgetBackupData(
            accounts = accountDao.getAllAccountsSnapshot(),
            categories = categoryDao.getAllCategoriesSnapshot(),
            transactions = transactionDao.getAllTransactionsSnapshot(),
            recurringBills = recurringBillDao.getAllBillsSnapshot(),
            monthlyBudgets = monthlyBudgetDao?.getAllBudgetsSnapshot() ?: emptyList()
        )
        val json = adapter.indent("  ").toJson(backupData)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Budgeter_Backup_$timeStamp.json"
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val file = File(backupDir, fileName)
        file.writeText(json)
        file
    }

    /**
     * Exports backup data directly to a chosen user destination URI (SAF)
     */
    suspend fun exportBackupToUri(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupData = BudgetBackupData(
                accounts = accountDao.getAllAccountsSnapshot(),
                categories = categoryDao.getAllCategoriesSnapshot(),
                transactions = transactionDao.getAllTransactionsSnapshot(),
                recurringBills = recurringBillDao.getAllBillsSnapshot(),
                monthlyBudgets = monthlyBudgetDao?.getAllBudgetsSnapshot() ?: emptyList()
            )
            val json = adapter.indent("  ").toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Restores the database from a backup JSON string or URI
     */
    suspend fun restoreBackupFromUri(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("Cannot open backup file"))

            val backupData = adapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("Invalid backup file format"))

            // Replace all records safely
            transactionDao.deleteAll()
            recurringBillDao.deleteAll()
            monthlyBudgetDao?.deleteAll()
            categoryDao.deleteAll()
            accountDao.deleteAll()

            accountDao.insertAccounts(backupData.accounts)
            categoryDao.insertCategories(backupData.categories)
            transactionDao.insertTransactions(backupData.transactions)
            if (backupData.recurringBills.isNotEmpty()) {
                recurringBillDao.insertAll(backupData.recurringBills)
            }
            if (backupData.monthlyBudgets.isNotEmpty() && monthlyBudgetDao != null) {
                monthlyBudgetDao.upsertBudgets(backupData.monthlyBudgets)
            }

            val totalCount = backupData.transactions.size + backupData.accounts.size + backupData.categories.size + backupData.monthlyBudgets.size
            Result.success(totalCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * List all local auto/manual backups
     */
    fun listLocalBackups(context: Context): List<File> {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles { file -> file.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
