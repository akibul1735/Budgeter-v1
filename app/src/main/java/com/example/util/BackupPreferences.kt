package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class CloudAccountInfo(
    val provider: String = "Google Drive",
    val email: String = "",
    val displayName: String = "",
    val serverUrl: String = "",
    val accessToken: String = "",
    val appKey: String = "",
    val refreshToken: String = "",
    val tokenExpiresAt: Long = 0L,
    val isLinked: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val autoSync: Boolean = true,
    val wifiOnly: Boolean = false,
    val uploadAttachments: Boolean = true,
    val driveFolderType: String = "Visible 'Budgeter' Folder"
)

data class BackupSettingsConfig(
    val cloudProvider: String = "Google Drive",
    val isAccountLinked: Boolean = false,
    val primaryAccount: CloudAccountInfo = CloudAccountInfo(provider = "Google Drive"),
    val secondaryAccount: CloudAccountInfo = CloudAccountInfo(provider = "Google Drive"),
    val isDualSyncEnabled: Boolean = true,
    val localBackupDirectory: String = "Documents/Budgeter",
    val isAutoPhoneBackupEnabled: Boolean = true,
    val scheduledBackupHour: Int = 23,
    val scheduledBackupMinute: Int = 55,
    val uploadAttachments: Boolean = true,
    val autoSyncData: Boolean = true,
    val autoSyncOnAppStart: Boolean = true,
    val autoSyncOnAppClose: Boolean = true,
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
        val primaryProvider = prefs.getString(KEY_PRIMARY_PROVIDER, prefs.getString(KEY_CLOUD_PROVIDER, "Google Drive")) ?: "Google Drive"
        val primaryLinked = prefs.getBoolean(KEY_PRIMARY_LINKED, prefs.getBoolean(KEY_ACCOUNT_LINKED, false))
        val primaryEmail = prefs.getString(KEY_PRIMARY_EMAIL, "") ?: ""
        val primaryName = prefs.getString(KEY_PRIMARY_NAME, "") ?: ""
        val primaryServer = prefs.getString(KEY_PRIMARY_SERVER, "") ?: ""
        val primaryAccessToken = prefs.getString(KEY_PRIMARY_ACCESS_TOKEN, "") ?: ""
        val primaryLastSync = prefs.getLong(KEY_PRIMARY_LAST_SYNC, prefs.getLong(KEY_LAST_SYNC, 0L))
        val primaryAutoSync = prefs.getBoolean(KEY_PRIMARY_AUTO_SYNC, prefs.getBoolean(KEY_AUTO_SYNC, true))
        val primaryWifiOnly = prefs.getBoolean(KEY_PRIMARY_WIFI_ONLY, prefs.getBoolean(KEY_WIFI_ONLY, false))
        val primaryUploadAtt = prefs.getBoolean(KEY_PRIMARY_UPLOAD_ATTACHMENTS, prefs.getBoolean(KEY_UPLOAD_ATTACHMENTS, true))
        val primaryFolderType = prefs.getString(KEY_PRIMARY_FOLDER_TYPE, "Visible 'Budgeter' Folder") ?: "Visible 'Budgeter' Folder"

        val primaryAppKey = prefs.getString(KEY_PRIMARY_APP_KEY, "") ?: ""
        val primaryRefreshToken = prefs.getString(KEY_PRIMARY_REFRESH_TOKEN, "") ?: ""
        val primaryTokenExpiresAt = prefs.getLong(KEY_PRIMARY_EXPIRES_AT, 0L)

        val secondaryProvider = prefs.getString(KEY_SECONDARY_PROVIDER, "Dropbox") ?: "Dropbox"
        val secondaryLinked = prefs.getBoolean(KEY_SECONDARY_LINKED, false)
        val secondaryEmail = prefs.getString(KEY_SECONDARY_EMAIL, "") ?: ""
        val secondaryName = prefs.getString(KEY_SECONDARY_NAME, "") ?: ""
        val secondaryServer = prefs.getString(KEY_SECONDARY_SERVER, "") ?: ""
        val secondaryAccessToken = prefs.getString(KEY_SECONDARY_ACCESS_TOKEN, "") ?: ""
        val secondaryAppKey = prefs.getString(KEY_SECONDARY_APP_KEY, "") ?: ""
        val secondaryRefreshToken = prefs.getString(KEY_SECONDARY_REFRESH_TOKEN, "") ?: ""
        val secondaryTokenExpiresAt = prefs.getLong(KEY_SECONDARY_EXPIRES_AT, 0L)
        val secondaryLastSync = prefs.getLong(KEY_SECONDARY_LAST_SYNC, 0L)
        val secondaryAutoSync = prefs.getBoolean(KEY_SECONDARY_AUTO_SYNC, true)
        val secondaryWifiOnly = prefs.getBoolean(KEY_SECONDARY_WIFI_ONLY, false)
        val secondaryUploadAtt = prefs.getBoolean(KEY_SECONDARY_UPLOAD_ATTACHMENTS, true)
        val secondaryFolderType = prefs.getString(KEY_SECONDARY_FOLDER_TYPE, "Visible 'Budgeter' Folder") ?: "Visible 'Budgeter' Folder"

        val autoStartSaved = prefs.getBoolean(KEY_AUTO_SYNC_ON_START, true)
        val autoCloseSaved = prefs.getBoolean(KEY_AUTO_SYNC_ON_CLOSE, true)
        // Ensure at least one is enabled
        val (finalAutoStart, finalAutoClose) = if (!autoStartSaved && !autoCloseSaved) {
            Pair(true, false)
        } else {
            Pair(autoStartSaved, autoCloseSaved)
        }

        return BackupSettingsConfig(
            cloudProvider = primaryProvider,
            isAccountLinked = primaryLinked || secondaryLinked,
            primaryAccount = CloudAccountInfo(
                provider = primaryProvider,
                email = primaryEmail,
                displayName = primaryName,
                serverUrl = primaryServer,
                accessToken = primaryAccessToken,
                appKey = primaryAppKey,
                refreshToken = primaryRefreshToken,
                tokenExpiresAt = primaryTokenExpiresAt,
                isLinked = primaryLinked,
                lastSyncTimestamp = primaryLastSync,
                autoSync = primaryAutoSync,
                wifiOnly = primaryWifiOnly,
                uploadAttachments = primaryUploadAtt,
                driveFolderType = primaryFolderType
            ),
            secondaryAccount = CloudAccountInfo(
                provider = secondaryProvider,
                email = secondaryEmail,
                displayName = secondaryName,
                serverUrl = secondaryServer,
                accessToken = secondaryAccessToken,
                appKey = secondaryAppKey,
                refreshToken = secondaryRefreshToken,
                tokenExpiresAt = secondaryTokenExpiresAt,
                isLinked = secondaryLinked,
                lastSyncTimestamp = secondaryLastSync,
                autoSync = secondaryAutoSync,
                wifiOnly = secondaryWifiOnly,
                uploadAttachments = secondaryUploadAtt,
                driveFolderType = secondaryFolderType
            ),
            isDualSyncEnabled = prefs.getBoolean(KEY_DUAL_SYNC_ENABLED, true),
            localBackupDirectory = prefs.getString(KEY_LOCAL_DIR, "Documents/Budgeter") ?: "Documents/Budgeter",
            isAutoPhoneBackupEnabled = prefs.getBoolean(KEY_AUTO_PHONE_BACKUP, true),
            scheduledBackupHour = prefs.getInt(KEY_SCHEDULED_HOUR, 23),
            scheduledBackupMinute = prefs.getInt(KEY_SCHEDULED_MINUTE, 55),
            uploadAttachments = prefs.getBoolean(KEY_UPLOAD_ATTACHMENTS, true),
            autoSyncData = prefs.getBoolean(KEY_AUTO_SYNC, true),
            autoSyncOnAppStart = finalAutoStart,
            autoSyncOnAppClose = finalAutoClose,
            wifiOnly = prefs.getBoolean(KEY_WIFI_ONLY, false),
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC, 0L)
        )
    }

    fun updateConfig(newConfig: BackupSettingsConfig) {
        // Enforce constraint: at least one of autoSyncOnAppStart or autoSyncOnAppClose must be true
        val safeConfig = if (!newConfig.autoSyncOnAppStart && !newConfig.autoSyncOnAppClose) {
            newConfig.copy(autoSyncOnAppStart = true)
        } else {
            newConfig
        }

        prefs.edit()
            .putString(KEY_CLOUD_PROVIDER, safeConfig.primaryAccount.provider)
            .putString(KEY_PRIMARY_PROVIDER, safeConfig.primaryAccount.provider)
            .putString(KEY_PRIMARY_EMAIL, safeConfig.primaryAccount.email)
            .putString(KEY_PRIMARY_NAME, safeConfig.primaryAccount.displayName)
            .putString(KEY_PRIMARY_SERVER, safeConfig.primaryAccount.serverUrl)
            .putString(KEY_PRIMARY_ACCESS_TOKEN, safeConfig.primaryAccount.accessToken)
            .putString(KEY_PRIMARY_APP_KEY, safeConfig.primaryAccount.appKey)
            .putString(KEY_PRIMARY_REFRESH_TOKEN, safeConfig.primaryAccount.refreshToken)
            .putLong(KEY_PRIMARY_EXPIRES_AT, safeConfig.primaryAccount.tokenExpiresAt)
            .putBoolean(KEY_PRIMARY_LINKED, safeConfig.primaryAccount.isLinked)
            .putLong(KEY_PRIMARY_LAST_SYNC, safeConfig.primaryAccount.lastSyncTimestamp)
            .putBoolean(KEY_PRIMARY_AUTO_SYNC, safeConfig.primaryAccount.autoSync)
            .putBoolean(KEY_PRIMARY_WIFI_ONLY, safeConfig.primaryAccount.wifiOnly)
            .putBoolean(KEY_PRIMARY_UPLOAD_ATTACHMENTS, safeConfig.primaryAccount.uploadAttachments)
            .putString(KEY_PRIMARY_FOLDER_TYPE, safeConfig.primaryAccount.driveFolderType)
            .putString(KEY_SECONDARY_PROVIDER, safeConfig.secondaryAccount.provider)
            .putString(KEY_SECONDARY_EMAIL, safeConfig.secondaryAccount.email)
            .putString(KEY_SECONDARY_NAME, safeConfig.secondaryAccount.displayName)
            .putString(KEY_SECONDARY_SERVER, safeConfig.secondaryAccount.serverUrl)
            .putString(KEY_SECONDARY_ACCESS_TOKEN, safeConfig.secondaryAccount.accessToken)
            .putString(KEY_SECONDARY_APP_KEY, safeConfig.secondaryAccount.appKey)
            .putString(KEY_SECONDARY_REFRESH_TOKEN, safeConfig.secondaryAccount.refreshToken)
            .putLong(KEY_SECONDARY_EXPIRES_AT, safeConfig.secondaryAccount.tokenExpiresAt)
            .putBoolean(KEY_SECONDARY_LINKED, safeConfig.secondaryAccount.isLinked)
            .putLong(KEY_SECONDARY_LAST_SYNC, safeConfig.secondaryAccount.lastSyncTimestamp)
            .putBoolean(KEY_SECONDARY_AUTO_SYNC, safeConfig.secondaryAccount.autoSync)
            .putBoolean(KEY_SECONDARY_WIFI_ONLY, safeConfig.secondaryAccount.wifiOnly)
            .putBoolean(KEY_SECONDARY_UPLOAD_ATTACHMENTS, safeConfig.secondaryAccount.uploadAttachments)
            .putString(KEY_SECONDARY_FOLDER_TYPE, safeConfig.secondaryAccount.driveFolderType)
            .putBoolean(KEY_DUAL_SYNC_ENABLED, safeConfig.isDualSyncEnabled)
            .putString(KEY_LOCAL_DIR, safeConfig.localBackupDirectory)
            .putBoolean(KEY_AUTO_PHONE_BACKUP, safeConfig.isAutoPhoneBackupEnabled)
            .putInt(KEY_SCHEDULED_HOUR, safeConfig.scheduledBackupHour)
            .putInt(KEY_SCHEDULED_MINUTE, safeConfig.scheduledBackupMinute)
            .putBoolean(KEY_UPLOAD_ATTACHMENTS, safeConfig.uploadAttachments)
            .putBoolean(KEY_AUTO_SYNC, safeConfig.autoSyncData)
            .putBoolean(KEY_AUTO_SYNC_ON_START, safeConfig.autoSyncOnAppStart)
            .putBoolean(KEY_AUTO_SYNC_ON_CLOSE, safeConfig.autoSyncOnAppClose)
            .putBoolean(KEY_WIFI_ONLY, safeConfig.wifiOnly)
            .putLong(KEY_LAST_SYNC, safeConfig.lastSyncTimestamp)
            .apply()
        _config.value = safeConfig
    }

    fun setPrimaryProvider(provider: String) {
        val updated = _config.value.primaryAccount.copy(provider = provider)
        updateConfig(_config.value.copy(primaryAccount = updated, cloudProvider = provider))
    }

    fun setSecondaryProvider(provider: String) {
        val updated = _config.value.secondaryAccount.copy(provider = provider)
        updateConfig(_config.value.copy(secondaryAccount = updated))
    }

    fun setPrimaryAccount(email: String, displayName: String, isLinked: Boolean, serverUrl: String = "", accessToken: String = "") {
        val updatedPrimary = _config.value.primaryAccount.copy(
            email = email,
            displayName = displayName,
            serverUrl = serverUrl,
            accessToken = if (accessToken.isNotBlank()) accessToken else _config.value.primaryAccount.accessToken,
            isLinked = isLinked
        )
        updateConfig(
            _config.value.copy(
                primaryAccount = updatedPrimary,
                isAccountLinked = isLinked || _config.value.secondaryAccount.isLinked
            )
        )
    }

    fun setSecondaryAccount(email: String, displayName: String, isLinked: Boolean, serverUrl: String = "", accessToken: String = "") {
        val updatedSecondary = _config.value.secondaryAccount.copy(
            email = email,
            displayName = displayName,
            serverUrl = serverUrl,
            accessToken = if (accessToken.isNotBlank()) accessToken else _config.value.secondaryAccount.accessToken,
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
        setPrimaryProvider(provider)
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

    fun setAutoSyncOnAppStart(enabled: Boolean) {
        var onStart = enabled
        var onClose = _config.value.autoSyncOnAppClose
        if (!onStart && !onClose) {
            onClose = true // Enforce at least one is enabled
        }
        updateConfig(_config.value.copy(autoSyncOnAppStart = onStart, autoSyncOnAppClose = onClose))
    }

    fun setAutoSyncOnAppClose(enabled: Boolean) {
        var onClose = enabled
        var onStart = _config.value.autoSyncOnAppStart
        if (!onClose && !onStart) {
            onStart = true // Enforce at least one is enabled
        }
        updateConfig(_config.value.copy(autoSyncOnAppStart = onStart, autoSyncOnAppClose = onClose))
    }

    fun setWifiOnly(enabled: Boolean) {
        updateConfig(_config.value.copy(wifiOnly = enabled))
    }

    fun setPrimaryAutoSync(enabled: Boolean) {
        val updated = _config.value.primaryAccount.copy(autoSync = enabled)
        updateConfig(_config.value.copy(primaryAccount = updated))
    }

    fun setPrimaryWifiOnly(enabled: Boolean) {
        val updated = _config.value.primaryAccount.copy(wifiOnly = enabled)
        updateConfig(_config.value.copy(primaryAccount = updated))
    }

    fun setPrimaryUploadAttachments(enabled: Boolean) {
        val updated = _config.value.primaryAccount.copy(uploadAttachments = enabled)
        updateConfig(_config.value.copy(primaryAccount = updated))
    }

    fun setPrimaryFolderType(type: String) {
        val updated = _config.value.primaryAccount.copy(driveFolderType = type)
        updateConfig(_config.value.copy(primaryAccount = updated))
    }

    fun setSecondaryAutoSync(enabled: Boolean) {
        val updated = _config.value.secondaryAccount.copy(autoSync = enabled)
        updateConfig(_config.value.copy(secondaryAccount = updated))
    }

    fun setSecondaryWifiOnly(enabled: Boolean) {
        val updated = _config.value.secondaryAccount.copy(wifiOnly = enabled)
        updateConfig(_config.value.copy(secondaryAccount = updated))
    }

    fun setSecondaryUploadAttachments(enabled: Boolean) {
        val updated = _config.value.secondaryAccount.copy(uploadAttachments = enabled)
        updateConfig(_config.value.copy(secondaryAccount = updated))
    }

    fun setSecondaryFolderType(type: String) {
        val updated = _config.value.secondaryAccount.copy(driveFolderType = type)
        updateConfig(_config.value.copy(secondaryAccount = updated))
    }

    fun setDropboxOAuthTokens(
        driveIndex: Int,
        appKey: String,
        accessToken: String,
        refreshToken: String,
        expiresInSeconds: Long,
        email: String,
        displayName: String
    ) {
        val expiresAt = if (expiresInSeconds > 0) System.currentTimeMillis() + (expiresInSeconds * 1000L) else 0L
        if (driveIndex == 1) {
            val updated = _config.value.primaryAccount.copy(
                provider = "Dropbox",
                email = email,
                displayName = displayName,
                accessToken = accessToken,
                appKey = appKey,
                refreshToken = refreshToken,
                tokenExpiresAt = expiresAt,
                isLinked = true
            )
            updateConfig(_config.value.copy(primaryAccount = updated, isAccountLinked = true, cloudProvider = "Dropbox"))
        } else {
            val updated = _config.value.secondaryAccount.copy(
                provider = "Dropbox",
                email = email,
                displayName = displayName,
                accessToken = accessToken,
                appKey = appKey,
                refreshToken = refreshToken,
                tokenExpiresAt = expiresAt,
                isLinked = true
            )
            updateConfig(_config.value.copy(secondaryAccount = updated, isAccountLinked = true))
        }
    }

    fun updateDropboxAccessToken(driveIndex: Int, accessToken: String, expiresInSeconds: Long) {
        val expiresAt = if (expiresInSeconds > 0) System.currentTimeMillis() + (expiresInSeconds * 1000L) else 0L
        if (driveIndex == 1) {
            val updated = _config.value.primaryAccount.copy(
                accessToken = accessToken,
                tokenExpiresAt = expiresAt
            )
            updateConfig(_config.value.copy(primaryAccount = updated))
        } else {
            val updated = _config.value.secondaryAccount.copy(
                accessToken = accessToken,
                tokenExpiresAt = expiresAt
            )
            updateConfig(_config.value.copy(secondaryAccount = updated))
        }
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
        private const val KEY_PRIMARY_PROVIDER = "primary_provider"
        private const val KEY_PRIMARY_EMAIL = "primary_email"
        private const val KEY_PRIMARY_NAME = "primary_name"
        private const val KEY_PRIMARY_SERVER = "primary_server"
        private const val KEY_PRIMARY_ACCESS_TOKEN = "primary_access_token"
        private const val KEY_PRIMARY_APP_KEY = "primary_app_key"
        private const val KEY_PRIMARY_REFRESH_TOKEN = "primary_refresh_token"
        private const val KEY_PRIMARY_EXPIRES_AT = "primary_token_expires_at"
        private const val KEY_PRIMARY_LINKED = "primary_linked"
        private const val KEY_PRIMARY_LAST_SYNC = "primary_last_sync"
        private const val KEY_PRIMARY_AUTO_SYNC = "primary_auto_sync"
        private const val KEY_PRIMARY_WIFI_ONLY = "primary_wifi_only"
        private const val KEY_PRIMARY_UPLOAD_ATTACHMENTS = "primary_upload_attachments"
        private const val KEY_PRIMARY_FOLDER_TYPE = "primary_folder_type"
        private const val KEY_SECONDARY_PROVIDER = "secondary_provider"
        private const val KEY_SECONDARY_EMAIL = "secondary_email"
        private const val KEY_SECONDARY_NAME = "secondary_name"
        private const val KEY_SECONDARY_SERVER = "secondary_server"
        private const val KEY_SECONDARY_ACCESS_TOKEN = "secondary_access_token"
        private const val KEY_SECONDARY_APP_KEY = "secondary_app_key"
        private const val KEY_SECONDARY_REFRESH_TOKEN = "secondary_refresh_token"
        private const val KEY_SECONDARY_EXPIRES_AT = "secondary_token_expires_at"
        private const val KEY_SECONDARY_LINKED = "secondary_linked"
        private const val KEY_SECONDARY_LAST_SYNC = "secondary_last_sync"
        private const val KEY_SECONDARY_AUTO_SYNC = "secondary_auto_sync"
        private const val KEY_SECONDARY_WIFI_ONLY = "secondary_wifi_only"
        private const val KEY_SECONDARY_UPLOAD_ATTACHMENTS = "secondary_upload_attachments"
        private const val KEY_SECONDARY_FOLDER_TYPE = "secondary_folder_type"
        private const val KEY_DUAL_SYNC_ENABLED = "dual_sync_enabled"
        private const val KEY_LOCAL_DIR = "local_backup_dir"
        private const val KEY_AUTO_PHONE_BACKUP = "auto_phone_backup"
        private const val KEY_SCHEDULED_HOUR = "scheduled_hour"
        private const val KEY_SCHEDULED_MINUTE = "scheduled_minute"
        private const val KEY_UPLOAD_ATTACHMENTS = "upload_attachments"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_AUTO_SYNC_ON_START = "auto_sync_on_start"
        private const val KEY_AUTO_SYNC_ON_CLOSE = "auto_sync_on_close"
        private const val KEY_WIFI_ONLY = "wifi_only"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_INSTALLATION_ID = "installation_id"
        private const val KEY_DISMISSED_BACKUP_IDS = "dismissed_cloud_backup_ids"
        private const val KEY_FIRST_LAUNCH_SHOWN = "first_launch_cloud_check_done"

        @Volatile
        private var instance: BackupPreferences? = null

        fun getInstance(context: Context): BackupPreferences {
            return instance ?: synchronized(this) {
                instance ?: BackupPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Unique ID generated once per app installation.
     */
    fun getInstallationId(): String {
        val existing = prefs.getString(KEY_INSTALLATION_ID, null)
        if (!existing.isNullOrBlank()) return existing
        val newId = "inst_" + java.util.UUID.randomUUID().toString().replace("-", "").take(8)
        prefs.edit().putString(KEY_INSTALLATION_ID, newId).apply()
        return newId
    }

    /**
     * Formatted human-readable device model name (e.g. "Google Pixel 8", "Samsung SM-S911B")
     */
    fun getDeviceName(): String {
        val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val model = android.os.Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }.trim()
    }

    fun isBackupBannerDismissed(backupId: String): Boolean {
        val dismissedSet = prefs.getStringSet(KEY_DISMISSED_BACKUP_IDS, emptySet()) ?: emptySet()
        return dismissedSet.contains(backupId)
    }

    fun dismissBackupBanner(backupId: String) {
        val dismissedSet = (prefs.getStringSet(KEY_DISMISSED_BACKUP_IDS, emptySet()) ?: emptySet()).toMutableSet()
        dismissedSet.add(backupId)
        prefs.edit().putStringSet(KEY_DISMISSED_BACKUP_IDS, dismissedSet).apply()
    }

    fun resetDismissedBanners() {
        prefs.edit().remove(KEY_DISMISSED_BACKUP_IDS).apply()
    }

    fun isFirstLaunchCheckDone(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH_SHOWN, false)
    }

    fun setFirstLaunchCheckDone(done: Boolean = true) {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH_SHOWN, done).apply()
    }

    fun getLocalBackupDirectory(): String = config.value.localBackupDirectory
    fun getPrimaryEmail(): String = config.value.primaryAccount.email
    fun getSecondaryEmail(): String = config.value.secondaryAccount.email
    fun getPrimaryProvider(): String = config.value.primaryAccount.provider
    fun isBackupDismissed(backupId: String): Boolean = isBackupBannerDismissed(backupId)
    fun dismissBackup(backupId: String) = dismissBackupBanner(backupId)
}

