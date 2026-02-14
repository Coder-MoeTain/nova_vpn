package com.novavpn.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novavpn.app.util.Logger
import com.novavpn.app.vpn.WireGuardManager
import com.wireguard.android.backend.Statistics
import com.wireguard.android.backend.Tunnel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VpnUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val connectionTimeSeconds: Long = 0L,
    val publicIp: String? = null,
    val lastError: String? = null,
    val statsRxBytes: Long = 0L,
    val statsTxBytes: Long = 0L
)

@HiltViewModel
class VpnViewModel @Inject constructor(
    private val wireGuardManager: WireGuardManager
) : ViewModel() {

    private val httpClient = HttpClient(Android) {}

    private val _state = MutableStateFlow(VpnUiState())
    val state: StateFlow<VpnUiState> = _state.asStateFlow()

    private var connectionStartTime: Long = 0L
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            syncStateFromBackend()
        }
        viewModelScope.launch {
            wireGuardManager.tunnelState.collect { tunnelState ->
                _state.update { current ->
                    val wasConnectedOrConnecting = current.connectionState is ConnectionState.Connected ||
                        current.connectionState is ConnectionState.Connecting
                    val connectionDropped = tunnelState == Tunnel.State.DOWN && wasConnectedOrConnecting
                    val newConnectionState = when {
                        connectionDropped -> ConnectionState.Error("Connection dropped")
                        else -> tunnelState.toConnectionState()
                    }
                    val newError = when {
                        connectionDropped -> "Connection dropped"
                        tunnelState == Tunnel.State.DOWN -> current.lastError
                        else -> null
                    }
                    current.copy(connectionState = newConnectionState, lastError = newError)
                }
                if (tunnelState == Tunnel.State.UP) {
                    connectionStartTime = System.currentTimeMillis()
                    _state.update { it.copy(connectionTimeSeconds = 0L) }
                    startTimer()
                    fetchPublicIp()
                } else {
                    stopTimer()
                }
            }
        }
    }

    private suspend fun syncStateFromBackend() {
        val current = wireGuardManager.getState()
        _state.update {
            it.copy(connectionState = current.toConnectionState())
        }
        if (current == Tunnel.State.UP) {
            connectionStartTime = System.currentTimeMillis()
            _state.update { it.copy(connectionTimeSeconds = 0L) }
            startTimer()
            updateStats()
        } else {
            stopTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val elapsed = (System.currentTimeMillis() - connectionStartTime) / 1000
                _state.update { it.copy(connectionTimeSeconds = elapsed) }
                updateStats()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(connectionTimeSeconds = 0L, statsRxBytes = 0L, statsTxBytes = 0L) }
    }

    private fun updateStats() {
        val stats = wireGuardManager.getStatistics() ?: return
        try {
            val rx: Long
            val tx: Long
            when (stats) {
                is Statistics -> {
                    rx = stats.totalRx()
                    tx = stats.totalTx()
                }
                else -> {
                    rx = stats.javaClass.getMethod("totalRx").invoke(stats)?.let { (it as? Number)?.toLong() ?: 0L } ?: 0L
                    tx = stats.javaClass.getMethod("totalTx").invoke(stats)?.let { (it as? Number)?.toLong() ?: 0L } ?: 0L
                }
            }
            _state.update { it.copy(statsRxBytes = rx, statsTxBytes = tx) }
        } catch (e: Exception) {
            Logger.w(e, "updateStats failed")
        }
    }

    private val connectingTimeoutMs = 20_000L

    fun connect() {
        viewModelScope.launch {
            Logger.d("connect: setting state Connecting")
            _state.update {
                it.copy(connectionState = ConnectionState.Connecting, lastError = null)
            }
            val timeoutJob = launch {
                delay(connectingTimeoutMs)
                _state.update { current ->
                    if (current.connectionState == ConnectionState.Connecting) {
                        Logger.w("connect: timeout reached while still connecting")
                        current.copy(
                            connectionState = ConnectionState.Error("Connection timed out"),
                            lastError = "Connection timed out"
                        )
                    } else current
                }
            }
            try {
                Logger.d("connect: building config (on IO)...")
                val config = withContext(Dispatchers.IO) {
                    wireGuardManager.buildConfig()
                }
                Logger.d("connect: config built, calling setStateUp (on IO)...")
                withContext(Dispatchers.IO) {
                    wireGuardManager.setStateUp(config)
                }
                Logger.d("connect: setStateUp returned")
            } catch (e: Exception) {
                timeoutJob.cancel()
                Logger.e(e, "Connect failed: ${e.javaClass.simpleName}: ${e.message}")
                val msg = userFriendlyMessage(e)
                _state.update {
                    it.copy(connectionState = ConnectionState.Error(msg), lastError = msg)
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _state.update { it.copy(connectionState = ConnectionState.Disconnecting) }
            wireGuardManager.setStateDown()
        }
    }

    fun clearError() {
        _state.update { it.copy(lastError = null, connectionState = ConnectionState.Disconnected) }
    }

    fun refreshPublicIp() {
        viewModelScope.launch { fetchPublicIp() }
    }

    private suspend fun fetchPublicIp() {
        try {
            val ip = withContext(Dispatchers.IO) {
                httpClient.get("https://api.ipify.org").bodyAsText()
            }
            _state.update { it.copy(publicIp = ip) }
        } catch (e: Exception) {
            Logger.w(e, "Could not fetch public IP")
            _state.update { it.copy(publicIp = null) }
        }
    }

    private fun userFriendlyMessage(e: Throwable): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            msg.contains("permission") || msg.contains("vpn") -> "VPN permission denied"
            msg.contains("handshake") -> "Handshake failed"
            msg.contains("network") || msg.contains("unreachable") -> "No internet or server unreachable"
            msg.contains("timeout") -> "Connection timed out"
            else -> e.message ?: "Connection failed"
        }
    }
}
