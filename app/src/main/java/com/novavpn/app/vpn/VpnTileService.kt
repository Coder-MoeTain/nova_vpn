package com.novavpn.app.vpn

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.novavpn.app.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class VpnTileService : TileService() {

    @Inject
    lateinit var wireGuardManager: WireGuardManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStartListening() {
        super.onStartListening()
        scope.launch {
            val state = wireGuardManager.getState()
            updateTile(state == com.wireguard.android.backend.Tunnel.State.UP)
        }
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val state = wireGuardManager.getState()
            when (state) {
                com.wireguard.android.backend.Tunnel.State.UP -> {
                    wireGuardManager.setStateDown()
                    updateTile(false)
                }
                else -> {
                    try {
                        val config = wireGuardManager.buildConfig()
                        wireGuardManager.setStateUp(config)
                        updateTile(true)
                    } catch (e: Exception) {
                        Logger.e(e, "Quick tile connect failed")
                        updateTile(false)
                    }
                }
            }
        }
    }

    private fun updateTile(connected: Boolean) {
        qsTile?.apply {
            state = if (connected) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            subtitle = if (connected) "Connected" else "Tap to connect"
            updateTile()
        }
    }
}
