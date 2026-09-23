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
            SyncManager.updateSyncLiveState(SyncLiveState.SYNCING, "Syncing budget data...")

            val appPrefs = applicationContext.getSharedPreferences("budgeter_app_prefs", Context.MODE_PRIVATE)
            val isDemoMode = appPrefs.getBoolean("app_is_demo_mode", false)
            if (isDemoMode) {
                Log.i(TAG, "Demo mode is active. Skipping automated JSON sync so demo data is not backed up.")
                SyncManager.updateSyncLiveState(SyncLiveState.IDLE, "Demo mode active")
                return@withContext Result.success()
            }
            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO), isDemoMode = false)

            val accountDao = db.accountDao()
            val categoryDao = db.categoryDao()
            val transactionDao = db.transactionDao()
            val recurringBillDao = db.recurringBillDao()
            val monthlyBudgetDao = db.monthlyBudgetDao()
            val budgetAdjustmentDao = db.budgetAdjustmentDao()
            val savingsGoalDao = db.savingsGoalDao()
            val wishlistDao = db.wishlistDao()

            val accounts = accountDao.getAllAccountsSnapshot()
            val categories = categoryDao.getAllCategoriesSnapshot()
            val transactions = transactionDao.getAllTransactionsSnapshot()
            val recurringBills = recurringBillDao.getAllBillsSnapshot()
            val monthlyBudgets = monthlyBudgetDao.getAllBudgetsSnapshot()
            val budgetAdjustments = budgetAdjustmentDao.getAllAdjustmentsSnapshot()
            val savingsGoals = savingsGoalDao.getAllGoalsSnapshot()
            val goalAllocations = savingsGoalDao.getAllAllocationsSnapshot()
            val wishlistItems = wishlistDao.getAllWishlistItemsSnapshot()

            val latestTxEpoch = transactions.maxOfOrNull { it.dateEpochMs } ?: 0L
            val latestBudgetEpoch = monthlyBudgets.maxOfOrNull { it.updatedAt } ?: 0L
            val currentSignature = "${accounts.size}|${categories.size}|${transactions.size}|${recurringBills.size}|${monthlyBudgets.size}|${budgetAdjustments.size}|${savingsGoals.size}|${goalAllocations.size}|${wishlistItems.size}|$latestTxEpoch|$latestBudgetEpoch"

            val backupDir = File(applicationContext.filesDir, "json_sync")
            if (!backupDir.exists()) backupDir.mkdirs()

            val latestSyncFile = File(backupDir, "latest_synced_data.json")
            val previousSignature = SyncManager.getLastDataSignature(applicationContext)
            val isDataUnchanged = previousSignature.isNotBlank() && previousSignature == currentSignature && latestSyncFile.exists()

            val timestamp = System.currentTimeMillis()
            val backupPrefs = BackupPreferences.getInstance(applicationContext)
            val config = backupPrefs.config.value

            if (isDataUnchanged) {
                Log.d(TAG, "Dataset is unchanged since last sync (delta check). Skipping redundant cloud upload.")
                SyncManager.recordJsonSyncSuccess(applicationContext, timestamp)
                backupPrefs.recordSyncTimestamp(timestamp)
                SyncManager.updateSyncLiveState(SyncLiveState.SUCCESS, "Up to date", timestamp)
                return@withContext Result.success()
            }

            val settingsBackup = BackupManager.captureSettings(applicationContext)

            val backupData = BudgetBackupData(
                version = 4,
                exportedAt = timestamp,
                app = "Budgeter",
                installationId = backupPrefs.getInstallationId(),
                deviceName = backupPrefs.getDeviceName(),
                accounts = accounts,
                categories = categories,
                transactions = transactions,
                recurringBills = recurringBills,
                monthlyBudgets = monthlyBudgets,
                budgetAdjustments = budgetAdjustments,
                savingsGoals = savingsGoals,
                goalAllocations = goalAllocations,
                wishlistItems = wishlistItems,
                settings = settingsBackup
            )

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(BudgetBackupData::class.java)
            val json = adapter.indent("  ").toJson(backupData)

            latestSyncFile.writeText(json)

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
                            savingsGoalDao = savingsGoalDao,
                            wishlistDao = wishlistDao,
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
                                savingsGoalDao = savingsGoalDao,
                                wishlistDao = wishlistDao,
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
                            savingsGoalDao = savingsGoalDao,
                            wishlistDao = wishlistDao,
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
                                savingsGoalDao = savingsGoalDao,
                                wishlistDao = wishlistDao,
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

            SyncManager.recordDataSignature(applicationContext, currentSignature)
            SyncManager.recordJsonSyncSuccess(applicationContext, timestamp)
            backupPrefs.recordSyncTimestamp(timestamp)
            SyncManager.updateSyncLiveState(SyncLiveState.SUCCESS, "Synced", timestamp)
            Log.d(TAG, "JSON sync worker finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "JSON sync worker encountered error: ${e.message}", e)
            SyncManager.updateSyncLiveState(SyncLiveState.ERROR, "Sync error: ${e.localizedMessage}")
            Result.retry()
        }
    }
}
