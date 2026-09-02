package com.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.sync.AppLifecycleObserver
import com.example.sync.SyncManager

class BudgeterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("BudgeterApp", "Initializing Budgeter Application with ProcessLifecycleObserver")

        // Register process lifecycle observer for Foreground / Background SQLite DB backup checks
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(applicationContext))

        // Trigger an initial check on app boot
        SyncManager.checkAndTriggerDatabaseBackup(applicationContext)
    }
}
