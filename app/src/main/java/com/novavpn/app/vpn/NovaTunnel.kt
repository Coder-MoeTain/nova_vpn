package com.novavpn.app.vpn

import com.wireguard.android.backend.Tunnel
import java.util.concurrent.atomic.AtomicReference

/**
 * Tunnel instance for the WireGuard backend. Notifies the current listener when state changes.
 */
class NovaTunnel : Tunnel {

    override fun getName(): String = TUNNEL_NAME

    private val stateListener = AtomicReference<(Tunnel.State) -> Unit>({})

    fun setStateListener(listener: (Tunnel.State) -> Unit) {
        stateListener.set(listener)
    }

    override fun onStateChange(newState: Tunnel.State) {
        stateListener.get().invoke(newState)
    }

    companion object {
        const val TUNNEL_NAME = "novavpn"
    }
}
