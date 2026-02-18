package com.novavpn.app.vpn

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.novavpn.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VpnTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile(false)
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(MainActivity.EXTRA_CONNECT_NOW, true)
        startActivityAndCollapse(intent)
    }

    private fun updateTile(connected: Boolean) {
        qsTile?.apply {
            state = if (connected) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            subtitle = "Tap to get config"
            updateTile()
        }
    }
}
