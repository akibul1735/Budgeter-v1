package com.example.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.util.BackupPreferences

class AppLifecycleObserver(
    private val appContext: Context
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AppLifecycleObserver"
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App entered Foreground (onStart).")
        val backupPrefs = BackupPreferences.getInstance(appContext)
        val config = backupPrefs.config.value

        if (config.autoSyncOnAppStart) {
            Log.d(TAG, "Auto-Sync on App Start triggered.")
            SyncManager.triggerInstantJsonSync(appContext)
        }

        // Local SQLite DB 24h backup check (strictly once in a day)
        SyncManager.checkAndTriggerDatabaseBackup(appContext)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App entered Background (onStop).")
        val backupPrefs = BackupPreferences.getInstance(appContext)
        val config = backupPrefs.config.value

        if (config.autoSyncOnAppClose) {
            Log.d(TAG, "Auto-Sync on App Close triggered.")
            SyncManager.triggerInstantJsonSync(appContext)
        }

        // Local SQLite DB 24h backup check (strictly once in a day)
        SyncManager.checkAndTriggerDatabaseBackup(appContext)
    }
}
