package com.example.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.util.BackupManager
import com.example.util.BackupPreferences
import com.example.util.BudgetBackupData
import com.example.util.DropboxService
import com.example.util.GoogleDriveService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JsonSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "JsonSyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting JSON sync worker...")
            val appPrefs = applicationContext.getSharedPreferences("budgeter_app_prefs", Context.MODE_PRIVATE)
            val isDemoMode = appPrefs.getBoolean("app_is_demo_mode", true)
            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO), isDemoMode = isDemoMode)

            val accountDao = db.accountDao()
            val categoryDao = db.categoryDao()
            val transactionDao = db.transactionDao()
            val recurringBillDao = db.recurringBillDao()
            val monthlyBudgetDao = db.monthlyBudgetDao()
            val budgetAdjustmentDao = db.budgetAdjustmentDao()

            val backupDir = File(applicationContext.filesDir, "json_sync")
            if (!backupDir.exists()) backupDir.mkdirs()

            val latestSyncFile = File(backupDir, "latest_synced_data.json")

            val settingsBackup = BackupManager.captureSettings(applicationContext)

            val backupData = BudgetBackupData(
                version = 4,
                exportedAt = System.currentTimeMillis(),
                app = "Budgeter",
                accounts = accountDao.getAllAccountsSnapshot(),
                categories = categoryDao.getAllCategoriesSnapshot(),
                transactions = transactionDao.getAllTransactionsSnapshot(),
                recurringBills = recurringBillDao.getAllBillsSnapshot(),
                monthlyBudgets = monthlyBudgetDao.getAllBudgetsSnapshot(),
                budgetAdjustments = budgetAdjustmentDao.getAllAdjustmentsSnapshot(),
                settings = settingsBackup
            )

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(BudgetBackupData::class.java)
            val json = adapter.indent("  ").toJson(backupData)

            latestSyncFile.writeText(json)

            val timestamp = System.currentTimeMillis()
            val backupPrefs = BackupPreferences.getInstance(applicationContext)
            val config = backupPrefs.config.value

            // Cloud Sync Primary Account if configured
            if (config.primaryAccount.isLinked && config.primaryAccount.autoSync) {
                try {
                    if (config.primaryAccount.provider.contains("Google", ignoreCase = true) && config.primaryAccount.email.isNotBlank()) {
                        val res = GoogleDriveService.uploadBackupToDriveForEmail(
                            context = applicationContext,
                            email = config.primaryAccount.email,
                            accountDao = accountDao,
                            categoryDao = categoryDao,
                            transactionDao = transactionDao,
                            recurringBillDao = recurringBillDao,
                            monthlyBudgetDao = monthlyBudgetDao,
                            budgetAdjustmentDao = budgetAdjustmentDao,
                            folderType = config.primaryAccount.driveFolderType,
                            includeSettings = true
                        )
                        if (res.isSuccess) {
                            backupPrefs.recordPrimarySync(timestamp)
                        }
                    } else if (config.primaryAccount.provider.contains("Dropbox", ignoreCase = true)) {
                        val validToken = DropboxService.getValidAccessToken(applicationContext, 1)
                        if (validToken.isNotBlank()) {
                            val res = DropboxService.uploadBackup(
                                context = applicationContext,
                                accessToken = validToken,
                                accountDao = accountDao,
                                categoryDao = categoryDao,
                                transactionDao = transactionDao,
                                recurringBillDao = recurringBillDao,
                                monthlyBudgetDao = monthlyBudgetDao,
                                budgetAdjustmentDao = budgetAdjustmentDao,
                                includeSettings = true
                            )
                            if (res.isSuccess) {
                                backupPrefs.recordPrimarySync(timestamp)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Primary cloud sync error: ${e.message}")
                }
            }

            // Cloud Sync Secondary Account if configured
            if ((config.isDualSyncEnabled || config.secondaryAccount.isLinked) && config.secondaryAccount.isLinked && config.secondaryAccount.autoSync) {
                try {
                    if (config.secondaryAccount.provider.contains("Google", ignoreCase = true) && config.secondaryAccount.email.isNotBlank()) {
                        val res = GoogleDriveService.uploadBackupToDriveForEmail(
                            context = applicationContext,
                            email = config.secondaryAccount.email,
                            accountDao = accountDao,
                            categoryDao = categoryDao,
                            transactionDao = transactionDao,
                            recurringBillDao = recurringBillDao,
                            monthlyBudgetDao = monthlyBudgetDao,
                            budgetAdjustmentDao = budgetAdjustmentDao,
                            folderType = config.secondaryAccount.driveFolderType,
                            includeSettings = true
                        )
                        if (res.isSuccess) {
                            backupPrefs.recordSecondarySync(timestamp)
                        }
                    } else if (config.secondaryAccount.provider.contains("Dropbox", ignoreCase = true)) {
                        val validToken = DropboxService.getValidAccessToken(applicationContext, 2)
                        if (validToken.isNotBlank()) {
                            val res = DropboxService.uploadBackup(
                                context = applicationContext,
                                accessToken = validToken,
                                accountDao = accountDao,
                                categoryDao = categoryDao,
                                transactionDao = transactionDao,
                                recurringBillDao = recurringBillDao,
                                monthlyBudgetDao = monthlyBudgetDao,
                                budgetAdjustmentDao = budgetAdjustmentDao,
                                includeSettings = true
                            )
                            if (res.isSuccess) {
                                backupPrefs.recordSecondarySync(timestamp)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Secondary cloud sync error: ${e.message}")
                }
            }

            SyncManager.recordJsonSyncSuccess(applicationContext, timestamp)
            backupPrefs.recordSyncTimestamp(timestamp)
            Log.d(TAG, "JSON sync worker finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "JSON sync worker encountered error: ${e.message}", e)
            Result.retry()
        }
    }
}
