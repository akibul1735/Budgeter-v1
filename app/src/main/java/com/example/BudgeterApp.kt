package com.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.example.sync.AppLifecycleObserver
import com.example.sync.SyncManager
import okhttp3.OkHttpClient

class BudgeterApp : Application(), ImageLoaderFactory, Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("BudgeterApp", "Initializing Budgeter Application with ProcessLifecycleObserver")

        // Explicitly initialize WorkManager to ensure it is always ready before background workers are scheduled
        try {
            if (!WorkManager.isInitialized()) {
                WorkManager.initialize(this, workManagerConfiguration)
            }
        } catch (e: Exception) {
            Log.e("BudgeterApp", "WorkManager manual initialization notice: ${e.message}")
        }

        // Register process lifecycle observer for Foreground / Background SQLite DB backup checks
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(applicationContext))

        // Trigger an initial check on app boot and schedule daily auto backup
        SyncManager.checkAndTriggerDatabaseBackup(applicationContext)
        SyncManager.scheduleDailyAutoBackup(applicationContext)
    }

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithUserAgent = originalRequest.newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    )
                    .build()
                chain.proceed(requestWithUserAgent)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
