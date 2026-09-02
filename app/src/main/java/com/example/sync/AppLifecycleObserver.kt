package com.example.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(
    private val appContext: Context
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AppLifecycleObserver"
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App entered Foreground (onStart). Checking SQLite DB 24h backup threshold...")
        SyncManager.checkAndTriggerDatabaseBackup(appContext)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App entered Background (onStop). Checking SQLite DB 24h backup threshold...")
        SyncManager.checkAndTriggerDatabaseBackup(appContext)
    }
}
