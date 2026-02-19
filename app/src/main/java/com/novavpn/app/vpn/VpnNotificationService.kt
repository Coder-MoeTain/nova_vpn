package com.novavpn.app.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.novavpn.app.ui.MainActivity

class VpnNotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "vpn_notification_channel"
        private const val NOTIFICATION_ID = 1
        
        const val ACTION_UPDATE_STATS = "com.novavpn.app.UPDATE_STATS"
        const val EXTRA_RX_BYTES = "rx_bytes"
        const val EXTRA_TX_BYTES = "tx_bytes"
        const val EXTRA_STATUS = "status"
    }

    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_STATS -> {
                val rxBytes = intent.getLongExtra(EXTRA_RX_BYTES, 0L)
                val txBytes = intent.getLongExtra(EXTRA_TX_BYTES, 0L)
                val status = intent.getStringExtra(EXTRA_STATUS) ?: "Connected"
                updateNotification(rxBytes, txBytes, status)
            }
            else -> {
                // Start foreground service with initial notification
                startForeground(NOTIFICATION_ID, createNotification("Connected", 0L, 0L))
            }
        }
        return START_STICKY // Restart if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Connection Status",
                NotificationManager.IMPORTANCE_LOW // Low priority - persistent but not intrusive
            ).apply {
                description = "Shows VPN connection status"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(status: String, rxBytes: Long, txBytes: Long): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = when {
            rxBytes > 0 || txBytes > 0 -> {
                "↓ ${formatBytes(rxBytes)}  ↑ ${formatBytes(txBytes)}"
            }
            else -> status
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NovaVPN")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // You can replace with custom icon
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Cannot be dismissed
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setShowWhen(false)
            .build()
    }

    private fun updateNotification(status: String, rxBytes: Long, txBytes: Long) {
        val notification = createNotification(status, rxBytes, txBytes)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager?.cancel(NOTIFICATION_ID)
    }
}
