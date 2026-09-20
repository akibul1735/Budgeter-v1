package com.example.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.util.BackupPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class SyncLiveState {
    IDLE,
    SYNCING,
    SUCCESS,
    WAITING_WIFI,
    ERROR
}

data class SyncLiveStatus(
    val state: SyncLiveState = SyncLiveState.IDLE,
    val lastSyncTime: Long = 0L,
    val message: String? = null
)

object SyncManager {
    private const val TAG = "SyncManager"
    private const val PREFS_NAME = "budgeter_sync_prefs"
    private const val KEY_LAST_JSON_SYNC = "last_json_sync_timestamp"
    private const val KEY_LAST_DB_BACKUP = "last_db_backup_timestamp"
    private const val KEY_LAST_DATA_SIGNATURE = "last_synced_data_signature"
    private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"

    const val WORK_JSON_SYNC = "budgeter_json_instant_sync"
    const val WORK_DB_BACKUP = "budgeter_db_24h_backup"
    const val WORK_DAILY_AUTO_BACKUP = "budgeter_daily_auto_phone_backup"

    private val _syncLiveState = MutableStateFlow(SyncLiveStatus())
    val syncLiveState: StateFlow<SyncLiveStatus> = _syncLiveState.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Updates live status observable by UI indicators.
     */
    fun updateSyncLiveState(state: SyncLiveState, message: String? = null, timestamp: Long = System.currentTimeMillis()) {
        _syncLiveState.value = SyncLiveStatus(
            state = state,
            lastSyncTime = if (state == SyncLiveState.SUCCESS) timestamp else _syncLiveState.value.lastSyncTime,
            message = message
        )
    }

    /**
     * Builds network constraints based on user Wi-Fi Only settings.
     */
    fun buildSyncConstraints(context: Context): Constraints {
        val backupPrefs = BackupPreferences.getInstance(context)
        val config = backupPrefs.config.value
        val isWifiOnly = config.wifiOnly ||
                (config.primaryAccount.isLinked && config.primaryAccount.wifiOnly) ||
                (config.secondaryAccount.isLinked && config.secondaryAccount.wifiOnly)

        val networkType = if (isWifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        return Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()
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
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.MINUTES
                )
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
     * Triggers an immediate JSON sync worker on data mutation with Wi-Fi constraints and exponential backoff.
     */
    fun triggerInstantJsonSync(context: Context) {
        try {
            val constraints = buildSyncConstraints(context)
            val workRequest = OneTimeWorkRequestBuilder<JsonSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                WORK_JSON_SYNC,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d(TAG, "Instant JSON sync scheduled via WorkManager with constraints & exponential backoff")
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
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build()
                    )
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        15,
                        TimeUnit.MINUTES
                    )
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
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.MINUTES
                )
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

    fun recordDataSignature(context: Context, signature: String) {
        getPrefs(context).edit().putString(KEY_LAST_DATA_SIGNATURE, signature).apply()
    }

    fun getLastDataSignature(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_DATA_SIGNATURE, "") ?: ""
    }

    fun getLastJsonSyncTime(context: Context): Long {
        val prefTime = getPrefs(context).getLong(KEY_LAST_JSON_SYNC, 0L)
        return if (prefTime > 0) prefTime else _syncLiveState.value.lastSyncTime
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

