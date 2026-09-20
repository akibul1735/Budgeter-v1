package com.example.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.util.BackupPreferences
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SyncManager {
    private const val TAG = "SyncManager"
    private const val PREFS_NAME = "budgeter_sync_prefs"
    private const val KEY_LAST_JSON_SYNC = "last_json_sync_timestamp"
    private const val KEY_LAST_DB_BACKUP = "last_db_backup_timestamp"
    private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"

    const val WORK_JSON_SYNC = "budgeter_json_instant_sync"
    const val WORK_DB_BACKUP = "budgeter_db_24h_backup"
    const val WORK_DAILY_AUTO_BACKUP = "budgeter_daily_auto_phone_backup"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Schedules or cancels daily auto phone backup based on user preferences.
     */
    fun scheduleDailyAutoBackup(context: Context) {
        try {
            val backupPrefs = BackupPreferences.getInstance(context)
            val config = backupPrefs.config.value
            val workManager = WorkManager.getInstance(context.applicationContext)

            if (!config.isAutoPhoneBackupEnabled) {
                Log.d(TAG, "Auto phone backup disabled. Cancelling daily auto backup work.")
                workManager.cancelUniqueWork(WORK_DAILY_AUTO_BACKUP)
                return
            }

            val now = Calendar.getInstance()
            val targetCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, config.scheduledBackupHour)
                set(Calendar.MINUTE, config.scheduledBackupMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (targetCal.timeInMillis <= now.timeInMillis) {
                targetCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val initialDelayMs = (targetCal.timeInMillis - now.timeInMillis).coerceAtLeast(1000L)
            Log.d(TAG, "Scheduling daily auto backup with initial delay of ${initialDelayMs / 1000 / 60} minutes to run at ${config.formattedScheduledTime}")

            val periodicRequest = PeriodicWorkRequestBuilder<DatabaseBackupWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_DAILY_AUTO_BACKUP,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule daily auto backup: ${e.message}", e)
        }
    }

    fun cancelDailyAutoBackup(context: Context) {
        try {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_DAILY_AUTO_BACKUP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel daily auto backup: ${e.message}", e)
        }
    }

    /**
     * Triggers an immediate JSON sync worker on data mutation
     */
    fun triggerInstantJsonSync(context: Context) {
        try {
            val workRequest = OneTimeWorkRequestBuilder<JsonSyncWorker>()
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                WORK_JSON_SYNC,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d(TAG, "Instant JSON sync scheduled via WorkManager")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule JSON sync: ${e.message}", e)
        }
    }

    /**
     * Checks if 24 hours have passed since the last SQLite DB backup.
     * If so, triggers a OneTimeWorkRequest for full DB backup.
     */
    fun checkAndTriggerDatabaseBackup(context: Context) {
        try {
            val prefs = getPrefs(context)
            val lastBackupTime = prefs.getLong(KEY_LAST_DB_BACKUP, 0L)
            val currentTime = System.currentTimeMillis()
            val twentyFourHoursMs = 24 * 60 * 60 * 1000L

            if (currentTime - lastBackupTime >= twentyFourHoursMs || lastBackupTime == 0L) {
                Log.d(TAG, "24 hours elapsed since last DB backup. Triggering DatabaseBackupWorker.")
                val workRequest = OneTimeWorkRequestBuilder<DatabaseBackupWorker>()
                    .build()

                WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                    WORK_DB_BACKUP,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
            } else {
                val hoursRemaining = (twentyFourHoursMs - (currentTime - lastBackupTime)) / (1000 * 60 * 60)
                Log.d(TAG, "DB backup up to date. Next backup in approx $hoursRemaining hours.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check or trigger DB backup: ${e.message}", e)
        }
    }

    /**
     * Forces an immediate full SQLite DB backup regardless of 24h timer
     */
    fun forceImmediateDatabaseBackup(context: Context) {
        try {
            val workRequest = OneTimeWorkRequestBuilder<DatabaseBackupWorker>()
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                WORK_DB_BACKUP,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to force DB backup: ${e.message}", e)
        }
    }

    fun recordJsonSyncSuccess(context: Context, timestamp: Long = System.currentTimeMillis()) {
        getPrefs(context).edit().putLong(KEY_LAST_JSON_SYNC, timestamp).apply()
    }

    fun recordDbBackupSuccess(context: Context, timestamp: Long = System.currentTimeMillis()) {
        getPrefs(context).edit().putLong(KEY_LAST_DB_BACKUP, timestamp).apply()
    }

    fun getLastJsonSyncTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_JSON_SYNC, 0L)
    }

    fun getLastDbBackupTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_DB_BACKUP, 0L)
    }

    fun listDatabaseBackups(context: Context): List<File> {
        val backupDir = File(context.filesDir, "db_backups")
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles { file -> file.extension == "db" || file.name.endsWith(".db.bak") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
