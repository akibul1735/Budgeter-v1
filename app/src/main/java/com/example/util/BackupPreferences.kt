package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class CloudAccountInfo(
    val email: String = "",
    val displayName: String = "",
    val isLinked: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

data class BackupSettingsConfig(
    val cloudProvider: String = "Google Drive",
    val isAccountLinked: Boolean = false,
    val primaryAccount: CloudAccountInfo = CloudAccountInfo(),
    val secondaryAccount: CloudAccountInfo = CloudAccountInfo(),
    val isDualSyncEnabled: Boolean = true,
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
        val primaryLinked = prefs.getBoolean(KEY_PRIMARY_LINKED, prefs.getBoolean(KEY_ACCOUNT_LINKED, false))
        val primaryEmail = prefs.getString(KEY_PRIMARY_EMAIL, "") ?: ""
        val primaryName = prefs.getString(KEY_PRIMARY_NAME, "") ?: ""
        val primaryLastSync = prefs.getLong(KEY_PRIMARY_LAST_SYNC, prefs.getLong(KEY_LAST_SYNC, 0L))

        val secondaryLinked = prefs.getBoolean(KEY_SECONDARY_LINKED, false)
        val secondaryEmail = prefs.getString(KEY_SECONDARY_EMAIL, "") ?: ""
        val secondaryName = prefs.getString(KEY_SECONDARY_NAME, "") ?: ""
        val secondaryLastSync = prefs.getLong(KEY_SECONDARY_LAST_SYNC, 0L)

        return BackupSettingsConfig(
            cloudProvider = prefs.getString(KEY_CLOUD_PROVIDER, "Google Drive") ?: "Google Drive",
            isAccountLinked = primaryLinked || secondaryLinked,
            primaryAccount = CloudAccountInfo(
                email = primaryEmail,
                displayName = primaryName,
                isLinked = primaryLinked,
                lastSyncTimestamp = primaryLastSync
            ),
            secondaryAccount = CloudAccountInfo(
                email = secondaryEmail,
                displayName = secondaryName,
                isLinked = secondaryLinked,
                lastSyncTimestamp = secondaryLastSync
            ),
            isDualSyncEnabled = prefs.getBoolean(KEY_DUAL_SYNC_ENABLED, true),
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
            .putString(KEY_PRIMARY_EMAIL, newConfig.primaryAccount.email)
            .putString(KEY_PRIMARY_NAME, newConfig.primaryAccount.displayName)
            .putBoolean(KEY_PRIMARY_LINKED, newConfig.primaryAccount.isLinked)
            .putLong(KEY_PRIMARY_LAST_SYNC, newConfig.primaryAccount.lastSyncTimestamp)
            .putString(KEY_SECONDARY_EMAIL, newConfig.secondaryAccount.email)
            .putString(KEY_SECONDARY_NAME, newConfig.secondaryAccount.displayName)
            .putBoolean(KEY_SECONDARY_LINKED, newConfig.secondaryAccount.isLinked)
            .putLong(KEY_SECONDARY_LAST_SYNC, newConfig.secondaryAccount.lastSyncTimestamp)
            .putBoolean(KEY_DUAL_SYNC_ENABLED, newConfig.isDualSyncEnabled)
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

    fun setPrimaryAccount(email: String, displayName: String, isLinked: Boolean) {
        val updatedPrimary = _config.value.primaryAccount.copy(
            email = email,
            displayName = displayName,
            isLinked = isLinked
        )
        updateConfig(
            _config.value.copy(
                primaryAccount = updatedPrimary,
                isAccountLinked = isLinked || _config.value.secondaryAccount.isLinked
            )
        )
    }

    fun setSecondaryAccount(email: String, displayName: String, isLinked: Boolean) {
        val updatedSecondary = _config.value.secondaryAccount.copy(
            email = email,
            displayName = displayName,
            isLinked = isLinked
        )
        updateConfig(
            _config.value.copy(
                secondaryAccount = updatedSecondary,
                isAccountLinked = _config.value.primaryAccount.isLinked || isLinked
            )
        )
    }

    fun setDualSyncEnabled(enabled: Boolean) {
        updateConfig(_config.value.copy(isDualSyncEnabled = enabled))
    }

    fun recordPrimarySync(timestamp: Long = System.currentTimeMillis()) {
        val updatedPrimary = _config.value.primaryAccount.copy(lastSyncTimestamp = timestamp)
        updateConfig(_config.value.copy(primaryAccount = updatedPrimary, lastSyncTimestamp = timestamp))
    }

    fun recordSecondarySync(timestamp: Long = System.currentTimeMillis()) {
        val updatedSecondary = _config.value.secondaryAccount.copy(lastSyncTimestamp = timestamp)
        updateConfig(_config.value.copy(secondaryAccount = updatedSecondary))
    }

    fun setCloudProvider(provider: String) {
        updateConfig(_config.value.copy(cloudProvider = provider))
    }

    fun setAccountLinked(linked: Boolean) {
        setPrimaryAccount(
            email = _config.value.primaryAccount.email,
            displayName = _config.value.primaryAccount.displayName,
            isLinked = linked
        )
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

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _config.value = loadConfig()
    }

    companion object {
        private const val KEY_CLOUD_PROVIDER = "cloud_provider"
        private const val KEY_ACCOUNT_LINKED = "account_linked"
        private const val KEY_PRIMARY_EMAIL = "primary_email"
        private const val KEY_PRIMARY_NAME = "primary_name"
        private const val KEY_PRIMARY_LINKED = "primary_linked"
        private const val KEY_PRIMARY_LAST_SYNC = "primary_last_sync"
        private const val KEY_SECONDARY_EMAIL = "secondary_email"
        private const val KEY_SECONDARY_NAME = "secondary_name"
        private const val KEY_SECONDARY_LINKED = "secondary_linked"
        private const val KEY_SECONDARY_LAST_SYNC = "secondary_last_sync"
        private const val KEY_DUAL_SYNC_ENABLED = "dual_sync_enabled"
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
