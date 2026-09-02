package com.example.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.util.BackupManager
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
            Log.d(TAG, "Starting instant JSON sync worker...")
            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))

            // Create latest auto-sync json backup
            val accountDao = db.accountDao()
            val categoryDao = db.categoryDao()
            val transactionDao = db.transactionDao()
            val recurringBillDao = db.recurringBillDao()

            val backupDir = File(applicationContext.filesDir, "json_sync")
            if (!backupDir.exists()) backupDir.mkdirs()

            val latestSyncFile = File(backupDir, "latest_synced_data.json")

            val backupData = com.example.util.BudgetBackupData(
                version = 3,
                exportedAt = System.currentTimeMillis(),
                app = "Budgeter",
                accounts = accountDao.getAllAccountsSnapshot(),
                categories = categoryDao.getAllCategoriesSnapshot(),
                transactions = transactionDao.getAllTransactionsSnapshot(),
                recurringBills = recurringBillDao.getAllBillsSnapshot()
            )

            val moshi = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(com.example.util.BudgetBackupData::class.java)
            val json = adapter.indent("  ").toJson(backupData)

            latestSyncFile.writeText(json)

            // Also keep standard historical backup record
            val mainBackupDir = File(applicationContext.filesDir, "backups")
            if (!mainBackupDir.exists()) mainBackupDir.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val historyFile = File(mainBackupDir, "AutoSync_$timeStamp.json")
            historyFile.writeText(json)

            // Clean up old auto-sync files if too many (> 10)
            val existingBackups = mainBackupDir.listFiles { f -> f.name.startsWith("AutoSync_") }
            if (existingBackups != null && existingBackups.size > 10) {
                existingBackups.sortedBy { it.lastModified() }
                    .take(existingBackups.size - 10)
                    .forEach { it.delete() }
            }

            val timestamp = System.currentTimeMillis()
            SyncManager.recordJsonSyncSuccess(applicationContext, timestamp)
            Log.d(TAG, "Instant JSON sync completed successfully: ${latestSyncFile.absolutePath}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "JSON sync worker encountered error: ${e.message}", e)
            Result.retry()
        }
    }
}
