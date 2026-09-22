package com.example.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
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
import com.example.receiver.ReminderReceiver
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

    private fun getWorkManager(context: Context): WorkManager? {
        return try {
            WorkManager.getInstance(context.applicationContext)
        } catch (e: Throwable) {
            Log.e(TAG, "WorkManager instance could not be retrieved: ${e.message}")
            null
        }
    }

    /**
     * Schedules or cancels daily auto phone backup based on user preferences.
     * Uses AlarmManager exact alarm (RTC_WAKEUP) to fire on the exact minute even in Doze mode,
     * supplemented by a WorkManager request as an auxiliary fallback.
     */
    fun scheduleDailyAutoBackup(context: Context) {
        try {
            val backupPrefs = BackupPreferences.getInstance(context)
            val config = backupPrefs.config.value
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_SCHEDULED_BACKUP
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (!config.isAutoPhoneBackupEnabled) {
                Log.d(TAG, "Auto phone backup disabled. Cancelling daily auto backup alarm and work.")
                alarmManager?.cancel(pendingIntent)
                getWorkManager(context)?.cancelUniqueWork(WORK_DAILY_AUTO_BACKUP)
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

            val delayMinutes = (targetCal.timeInMillis - now.timeInMillis) / 1000 / 60
            Log.d(TAG, "Scheduling exact daily auto backup alarm for ${config.formattedScheduledTime} (in $delayMinutes mins)")

            // 1. Exact AlarmManager (wakes device up from Doze mode at exact minute)
            if (alarmManager != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                targetCal.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                targetCal.timeInMillis,
                                pendingIntent
                            )
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            targetCal.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            targetCal.timeInMillis,
                            pendingIntent
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not set exact alarm for backup: ${e.message}")
                }
            }

            // 2. Secondary fallback via WorkManager with initial delay (without battery gating to avoid silent cancellations)
            val initialDelayMs = (targetCal.timeInMillis - now.timeInMillis).coerceAtLeast(1000L)
            val workRequest = OneTimeWorkRequestBuilder<DatabaseBackupWorker>()
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.MINUTES
                )
                .build()

            getWorkManager(context)?.enqueueUniqueWork(
                WORK_DAILY_AUTO_BACKUP,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule daily auto backup: ${e.message}", e)
        }
    }

    fun cancelDailyAutoBackup(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_SCHEDULED_BACKUP
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.cancel(pendingIntent)
            getWorkManager(context)?.cancelUniqueWork(WORK_DAILY_AUTO_BACKUP)
            Log.d(TAG, "Daily auto backup cancelled.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel daily auto backup: ${e.message}", e)
        }
    }

    /**
     * Executes the daily scheduled backup worker immediately when the scheduled alarm fires.
     */
    fun triggerScheduledAutoBackupNow(context: Context) {
        try {
            Log.d(TAG, "Triggering scheduled auto backup immediately")
            val workRequest = OneTimeWorkRequestBuilder<DatabaseBackupWorker>()
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.MINUTES
                )
                .build()

            getWorkManager(context)?.enqueueUniqueWork(
                WORK_DAILY_AUTO_BACKUP,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger scheduled auto backup now: ${e.message}", e)
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

            getWorkManager(context)?.enqueueUniqueWork(
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
     * Checks if the scheduled local backup was missed (e.g. phone turned off at night).
     * If missed, triggers a catch-up backup immediately so data is never skipped.
     */
    fun checkAndTriggerDatabaseBackup(context: Context) {
        try {
            val backupPrefs = BackupPreferences.getInstance(context)
            val config = backupPrefs.config.value
            if (!config.isAutoPhoneBackupEnabled) return

            val prefs = getPrefs(context)
            val lastBackupTime = prefs.getLong(KEY_LAST_DB_BACKUP, 0L)
            val now = Calendar.getInstance()

            // Calculate the most recent scheduled backup window that should have taken place
            val targetToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, config.scheduledBackupHour)
                set(Calendar.MINUTE, config.scheduledBackupMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val mostRecentTargetTime = if (now.after(targetToday)) {
                targetToday.timeInMillis
            } else {
                targetToday.apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
            }

            val gracePeriodMs = 15 * 60 * 1000L // 15 mins grace period after scheduled time
            val isMissed = (lastBackupTime < mostRecentTargetTime) && (now.timeInMillis >= mostRecentTargetTime + gracePeriodMs)
            val isNeverBackedUp = (lastBackupTime == 0L)

            if (isMissed || isNeverBackedUp) {
                Log.d(TAG, "Scheduled backup missed (last: $lastBackupTime, expected: $mostRecentTargetTime). Catching up now.")
                forceImmediateDatabaseBackup(context)
            } else {
                Log.d(TAG, "Scheduled backup is up-to-date for cycle ending ${config.formattedScheduledTime}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check or trigger DB backup: ${e.message}", e)
        }
    }

    /**
     * Forces an immediate full SQLite DB backup regardless of schedule.
     */
    fun forceImmediateDatabaseBackup(context: Context) {
        try {
            val workRequest = OneTimeWorkRequestBuilder<DatabaseBackupWorker>()
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.MINUTES
                )
                .build()

            getWorkManager(context)?.enqueueUniqueWork(
                WORK_DB_BACKUP,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d(TAG, "Forced immediate DB backup enqueued successfully")
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

