package com.novavpn.app

import android.app.Application
import androidx.work.Configuration
import com.novavpn.app.util.Logger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NovaVpnApplication : Application() {
    
    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        
        // Initialize WorkManager with Hilt worker factory
        // Hilt injection happens during super.onCreate(), so workerFactory is available here
        try {
            androidx.work.WorkManager.initialize(
                this,
                Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build()
            )
            Logger.d("WorkManager initialized successfully")
        } catch (e: IllegalStateException) {
            // WorkManager already initialized (shouldn't happen with auto-init disabled)
            Logger.w(e, "WorkManager already initialized: ${e.message}")
        } catch (e: Exception) {
            Logger.e(e, "Failed to initialize WorkManager: ${e.message}")
            // Don't crash the app if WorkManager initialization fails
        }
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Logger.e(throwable, "NovaVPN crash: ${throwable.message}")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
