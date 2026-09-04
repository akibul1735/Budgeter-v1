package com.example.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "DatabaseBackupWorker"
        private const val REAL_DB_NAME = "budgeter_double_entry_db"
        private const val DEMO_DB_NAME = "budgeter_demo_db"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting SQLite DB 24h Backup Worker...")
            val appPrefs = applicationContext.getSharedPreferences("budgeter_app_prefs", Context.MODE_PRIVATE)
            val isDemoMode = appPrefs.getBoolean("app_is_demo_mode", true)
            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO), isDemoMode = isDemoMode)

            // Touch the database to guarantee it is opened and created on disk
            try {
                db.accountDao().getAccountCount()
            } catch (e: Exception) {
                Log.w(TAG, "DB touch warning: ${e.message}")
            }

            // 1. Force a WAL checkpoint to flush all in-memory and WAL logs to disk
            try {
                db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                    if (cursor.moveToFirst()) {
                        Log.d(TAG, "WAL Checkpoint executed: busy=${cursor.getInt(0)}, log=${cursor.getInt(1)}, checkpointed=${cursor.getInt(2)}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not run WAL checkpoint: ${e.message}")
            }

            // 2. Source database file (prefer active DB, fallback to alternative if exists)
            val targetDbName = if (isDemoMode) DEMO_DB_NAME else REAL_DB_NAME
            var dbFile = applicationContext.getDatabasePath(targetDbName)
            if (!dbFile.exists()) {
                val fallbackDbName = if (isDemoMode) REAL_DB_NAME else DEMO_DB_NAME
                val fallbackFile = applicationContext.getDatabasePath(fallbackDbName)
                if (fallbackFile.exists()) {
                    dbFile = fallbackFile
                }
            }

            if (!dbFile.exists()) {
                Log.w(TAG, "Database file does not exist at ${dbFile.absolutePath} yet. Skipping backup until database is populated.")
                return@withContext Result.success()
            }

            // 3. Target backup directory
            val backupDir = File(applicationContext.filesDir, "db_backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            // 4. Create primary latest backup file and timestamped archive
            val latestBackupFile = File(backupDir, "budgeter_db_latest.db")
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val archiveBackupFile = File(backupDir, "budgeter_backup_$timeStamp.db")

            copyFile(dbFile, latestBackupFile)
            copyFile(dbFile, archiveBackupFile)

            // 5. Cleanup older backups (keep last 7)
            val allBackups = backupDir.listFiles { file -> file.name.startsWith("budgeter_backup_") && file.extension == "db" }
            if (allBackups != null && allBackups.size > 7) {
                allBackups.sortedBy { it.lastModified() }
                    .take(allBackups.size - 7)
                    .forEach { it.delete() }
            }

            val timestamp = System.currentTimeMillis()
            SyncManager.recordDbBackupSuccess(applicationContext, timestamp)
            Log.d(TAG, "Database backup successful: ${archiveBackupFile.absolutePath}, size: ${archiveBackupFile.length()} bytes")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Database backup failed: ${e.message}", e)
            Result.retry()
        }
    }

    private fun copyFile(source: File, destination: File) {
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } > 0) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        }
    }
}
