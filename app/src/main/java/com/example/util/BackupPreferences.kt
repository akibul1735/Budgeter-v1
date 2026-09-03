package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class BackupSettingsConfig(
    val cloudProvider: String = "Google Drive",
    val isAccountLinked: Boolean = false,
    val localBackupDirectory: String = "Documents/Budgeter",
    val isAutoPhoneBackupEnabled: Boolean = true,
    val scheduledBackupHour: Int = 23,
    val scheduledBackupMinute: Int = 55,
    val uploadAttachments: Boolean = true,
    val autoSyncData: Boolean = true,
    val wifiOnly: Boolean = false,
    val lastSyncTimestamp: Long = 0L
) {
    val formattedScheduledTime: String
        get() {
            val displayHour = if (scheduledBackupHour == 0) 12 else if (scheduledBackupHour > 12) scheduledBackupHour - 12 else scheduledBackupHour
            val amPm = if (scheduledBackupHour < 12) "AM" else "PM"
            return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, scheduledBackupMinute, amPm)
        }
}

class BackupPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_backup_settings_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<BackupSettingsConfig> = _config.asStateFlow()

    private fun loadConfig(): BackupSettingsConfig {
        return BackupSettingsConfig(
            cloudProvider = prefs.getString(KEY_CLOUD_PROVIDER, "Google Drive") ?: "Google Drive",
            isAccountLinked = prefs.getBoolean(KEY_ACCOUNT_LINKED, false),
            localBackupDirectory = prefs.getString(KEY_LOCAL_DIR, "Documents/Budgeter") ?: "Documents/Budgeter",
            isAutoPhoneBackupEnabled = prefs.getBoolean(KEY_AUTO_PHONE_BACKUP, true),
            scheduledBackupHour = prefs.getInt(KEY_SCHEDULED_HOUR, 23),
            scheduledBackupMinute = prefs.getInt(KEY_SCHEDULED_MINUTE, 55),
            uploadAttachments = prefs.getBoolean(KEY_UPLOAD_ATTACHMENTS, true),
            autoSyncData = prefs.getBoolean(KEY_AUTO_SYNC, true),
            wifiOnly = prefs.getBoolean(KEY_WIFI_ONLY, false),
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC, 0L)
        )
    }

    fun updateConfig(newConfig: BackupSettingsConfig) {
        prefs.edit()
            .putString(KEY_CLOUD_PROVIDER, newConfig.cloudProvider)
            .putBoolean(KEY_ACCOUNT_LINKED, newConfig.isAccountLinked)
            .putString(KEY_LOCAL_DIR, newConfig.localBackupDirectory)
            .putBoolean(KEY_AUTO_PHONE_BACKUP, newConfig.isAutoPhoneBackupEnabled)
            .putInt(KEY_SCHEDULED_HOUR, newConfig.scheduledBackupHour)
            .putInt(KEY_SCHEDULED_MINUTE, newConfig.scheduledBackupMinute)
            .putBoolean(KEY_UPLOAD_ATTACHMENTS, newConfig.uploadAttachments)
            .putBoolean(KEY_AUTO_SYNC, newConfig.autoSyncData)
            .putBoolean(KEY_WIFI_ONLY, newConfig.wifiOnly)
            .putLong(KEY_LAST_SYNC, newConfig.lastSyncTimestamp)
            .apply()
        _config.value = newConfig
    }

    fun setCloudProvider(provider: String) {
        updateConfig(_config.value.copy(cloudProvider = provider))
    }

    fun setAccountLinked(linked: Boolean) {
        updateConfig(_config.value.copy(isAccountLinked = linked))
    }

    fun setLocalBackupDirectory(dir: String) {
        updateConfig(_config.value.copy(localBackupDirectory = dir))
    }

    fun setAutoPhoneBackupEnabled(enabled: Boolean) {
        updateConfig(_config.value.copy(isAutoPhoneBackupEnabled = enabled))
    }

    fun setScheduledTime(hour: Int, minute: Int) {
        updateConfig(_config.value.copy(scheduledBackupHour = hour, scheduledBackupMinute = minute))
    }

    fun setUploadAttachments(enabled: Boolean) {
        updateConfig(_config.value.copy(uploadAttachments = enabled))
    }

    fun setAutoSyncData(enabled: Boolean) {
        updateConfig(_config.value.copy(autoSyncData = enabled))
    }

    fun setWifiOnly(enabled: Boolean) {
        updateConfig(_config.value.copy(wifiOnly = enabled))
    }

    fun recordSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        updateConfig(_config.value.copy(lastSyncTimestamp = timestamp))
    }

    companion object {
        private const val KEY_CLOUD_PROVIDER = "cloud_provider"
        private const val KEY_ACCOUNT_LINKED = "account_linked"
        private const val KEY_LOCAL_DIR = "local_backup_dir"
        private const val KEY_AUTO_PHONE_BACKUP = "auto_phone_backup"
        private const val KEY_SCHEDULED_HOUR = "scheduled_hour"
        private const val KEY_SCHEDULED_MINUTE = "scheduled_minute"
        private const val KEY_UPLOAD_ATTACHMENTS = "upload_attachments"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_WIFI_ONLY = "wifi_only"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"

        @Volatile
        private var instance: BackupPreferences? = null

        fun getInstance(context: Context): BackupPreferences {
            return instance ?: synchronized(this) {
                instance ?: BackupPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
