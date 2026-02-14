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
    /** When connecting, seconds until timeout (e.g. 20, 19, …). Null when not connecting. */
    val connectingTimeoutRemainingSeconds: Int? = null,
    /** Optional hint shown below the error (e.g. what to check). */
    val errorHint: String? = null,
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
                    val wasConnected = current.connectionState is ConnectionState.Connected
                    val connectionDropped = tunnelState == Tunnel.State.DOWN && wasConnected
                    val newConnectionState = when {
                        connectionDropped -> ConnectionState.Error("Connection dropped")
                        current.connectionState is ConnectionState.Connecting && tunnelState == Tunnel.State.DOWN ->
                            current.connectionState
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

    private val connectingTimeoutMs = 45_000L
    private val connectingTimeoutSeconds = (connectingTimeoutMs / 1000).toInt()

    fun connect() {
        viewModelScope.launch {
            Logger.d("connect: setting state Connecting")
            _state.update {
                it.copy(
                    connectionState = ConnectionState.Connecting,
                    lastError = null,
                    errorHint = null,
                    connectingTimeoutRemainingSeconds = connectingTimeoutSeconds
                )
            }
            val timeoutJob = launch {
                var remaining = connectingTimeoutSeconds
                while (remaining > 0) {
                    delay(1_000)
                    remaining--
                    _state.update { current ->
                        if (current.connectionState == ConnectionState.Connecting)
                            current.copy(connectingTimeoutRemainingSeconds = remaining)
                        else current
                    }
                }
                _state.update { current ->
                    if (current.connectionState == ConnectionState.Connecting) {
                        Logger.w("connect: timeout reached while still connecting")
                        current.copy(
                            connectionState = ConnectionState.Error("Connection timed out"),
                            lastError = "Connection timed out",
                            errorHint = "Open the WireGuard UDP port on the server firewall and ensure the device key is on the server. Slow networks may need more time—try again.",
                            connectingTimeoutRemainingSeconds = null
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
                timeoutJob.cancel()
                withContext(Dispatchers.Main.immediate) {
                    connectionStartTime = System.currentTimeMillis()
                    _state.update {
                        it.copy(
                            connectionState = ConnectionState.Connected,
                            connectionTimeSeconds = 0L,
                            lastError = null,
                            errorHint = null,
                            connectingTimeoutRemainingSeconds = null
                        )
                    }
                    startTimer()
                    fetchPublicIp()
                    updateStats()
                }
            } catch (e: Exception) {
                timeoutJob.cancel()
                Logger.e(e, "Connect failed: ${e.javaClass.simpleName}: ${e.message}")
                val (msg, hint) = userFriendlyMessageAndHint(e)
                _state.update {
                    it.copy(
                        connectionState = ConnectionState.Error(msg),
                        lastError = msg,
                        errorHint = hint,
                        connectingTimeoutRemainingSeconds = null
                    )
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _state.update { it.copy(connectionState = ConnectionState.Disconnecting) }
            wireGuardManager.setStateDown()
            // Update UI immediately; do not rely on library callback or getState()
            withContext(Dispatchers.Main.immediate) {
                stopTimer()
                _state.update {
                    it.copy(
                        connectionState = ConnectionState.Disconnected,
                        connectionTimeSeconds = 0L,
                        statsRxBytes = 0L,
                        statsTxBytes = 0L,
                        publicIp = null
                    )
                }
            }
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

    private fun userFriendlyMessageAndHint(e: Throwable): Pair<String, String?> {
        val msg = e.message?.lowercase() ?: ""
        return when {
            msg.contains("permission") || msg.contains("vpn") ->
                "VPN permission denied" to "Grant VPN permission when prompted."
            msg.contains("handshake") ->
                "Handshake failed" to "Check that the server has this device's public key."
            msg.contains("network") || msg.contains("unreachable") || msg.contains("failed to connect") ->
                "Server unreachable" to "Check internet and that the VPN server is up and reachable."
            msg.contains("timeout") || msg.contains("timed out") ->
                "Connection timed out" to "Check that the VPN server is reachable and your device's key is on the server."
            msg.contains("provision") || msg.contains("api") || msg.contains("connection refused") ->
                "Provisioning failed" to "Run the provisioning server on the same machine as WireGuard and set PROVISIONING_BASE_URL in app/build.gradle.kts to that server's URL (e.g. http://SERVER_IP:3000)."
            else -> (e.message ?: "Connection failed") to null
        }
    }
}
