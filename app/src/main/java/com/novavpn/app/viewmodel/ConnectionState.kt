package com.novavpn.app.viewmodel

import com.wireguard.android.backend.Tunnel

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data object Disconnecting : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

fun Tunnel.State.toConnectionState(): ConnectionState = when (this) {
    Tunnel.State.UP -> ConnectionState.Connected
    Tunnel.State.DOWN -> ConnectionState.Disconnected
    else -> ConnectionState.Disconnected
}
