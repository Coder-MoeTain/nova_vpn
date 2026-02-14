package com.novavpn.app.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.novavpn.app.NovaVpnApplication
import com.novavpn.app.security.SecureStorage
import dagger.hilt.android.EntryPointAccessors

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as? NovaVpnApplication ?: return
        val storage = EntryPointAccessors.fromApplication(app, BootReceiverEntryPoint::class.java).secureStorage()
        if (!storage.getAutoConnect()) return
        val launch = Intent(context, com.novavpn.app.ui.MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(EXTRA_AUTO_CONNECT, true)
        context.startActivity(launch)
    }

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun secureStorage(): SecureStorage
    }

    companion object {
        const val EXTRA_AUTO_CONNECT = "auto_connect"
    }
}
