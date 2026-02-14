package com.novavpn.app

import android.app.Application
import com.novavpn.app.util.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NovaVpnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Logger.e(throwable, "NovaVPN crash: ${throwable.message}")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
