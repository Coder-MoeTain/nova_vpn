package com.novavpn.app.viewmodel

import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novavpn.app.BuildConfig
import com.novavpn.app.api.WireGuardConfigResponse
import com.novavpn.app.util.Logger
import com.novavpn.app.vpn.NovaTunnel
import com.novavpn.app.vpn.WireGuardConfigBuilder
import com.novavpn.app.vpn.WireGuardKeyGenerator
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.BadConfigException
import com.wireguard.config.Config
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class VpnUiState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val lastError: String? = null,
    val errorHint: String? = null,
    val successMessage: String? = null,
    /** Non-null when VPN permission is needed; Activity should launch this intent. */
    val vpnPrepareIntent: Intent? = null
)

@HiltViewModel
class VpnViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val secureStorage: com.novavpn.app.security.SecureStorage,
    private val provisioningApi: com.novavpn.app.api.ProvisioningApi,
    private val backend: Backend,
    private val tunnel: NovaTunnel
) : ViewModel() {

    private val _state = MutableStateFlow(VpnUiState())
    val state: StateFlow<VpnUiState> = _state.asStateFlow()

    init {
        tunnel.setStateListener { newState ->
            viewModelScope.launch {
                _state.update { s ->
                    when (newState) {
                        Tunnel.State.UP -> s.copy(
                            connectionState = ConnectionState.Connected,
                            lastError = null,
                            errorHint = null,
                            successMessage = "Connected",
                            vpnPrepareIntent = null
                        )
                        Tunnel.State.DOWN -> s.copy(
                            connectionState = if (s.connectionState == ConnectionState.Disconnecting)
                                ConnectionState.Idle else s.connectionState,
                            vpnPrepareIntent = null
                        )
                        else -> s.copy(vpnPrepareIntent = null)
                    }
                }
            }
        }
    }

    /** Call after user has completed VPN permission dialog (e.g. Activity result OK). */
    fun onVpnPrepareResult(granted: Boolean) {
        _state.update { it.copy(vpnPrepareIntent = null) }
        if (granted) doConnect()
    }

    fun connect() {
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            _state.update {
                it.copy(
                    connectionState = ConnectionState.Idle,
                    lastError = null,
                    errorHint = null,
                    vpnPrepareIntent = prepareIntent
                )
            }
            return
        }
        doConnect()
    }

    private fun doConnect() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    connectionState = ConnectionState.Connecting,
                    lastError = null,
                    errorHint = null,
                    successMessage = null,
                    vpnPrepareIntent = null
                )
            }
            try {
                var privateKey = secureStorage.getWireGuardPrivateKeyBase64()
                var publicKey: String
                if (privateKey.isNullOrBlank()) {
                    val (priv, pub) = withContext(Dispatchers.Default) { WireGuardKeyGenerator.generate() }
                    privateKey = priv
                    publicKey = pub
                    secureStorage.setWireGuardPrivateKeyBase64(privateKey)
                } else {
                    val key = net.moznion.wireguard.keytool.WireGuardKey(privateKey)
                    publicKey = key.base64PublicKey
                }

                var wgConfig: WireGuardConfigResponse? = secureStorage.getWireGuardConfigJson()?.let { json ->
                    try {
                        Json.decodeFromString<WireGuardConfigResponse>(json)
                    } catch (_: Exception) { null }
                }
                if (wgConfig == null) {
                    if (BuildConfig.PROVISIONING_BASE_URL.isBlank())
                        throw Exception("Set PROVISIONING_BASE_URL in app/build.gradle.kts to your provisioning server URL.")
                    wgConfig = withContext(Dispatchers.IO) { provisioningApi.provisionWireGuard(publicKey) }
                    secureStorage.setWireGuardConfigJson(Json.encodeToString(wgConfig))
                }

                val config: Config = withContext(Dispatchers.Default) {
                    WireGuardConfigBuilder.build(privateKey, wgConfig)
                }
                withContext(Dispatchers.IO) {
                    backend.setState(tunnel, Tunnel.State.UP, config)
                }
                // State will update to Connected via tunnel.setStateListener when UP is applied
            } catch (e: BadConfigException) {
                Logger.e(e, "WireGuard config error")
                _state.update {
                    it.copy(
                        connectionState = ConnectionState.Error("Invalid config"),
                        lastError = e.message,
                        errorHint = "Clear cached config in Settings and try again."
                    )
                }
            } catch (e: Exception) {
                Logger.e(e, "Connect failed")
                val (msg, hint) = userFriendlyMessageAndHint(e)
                _state.update {
                    it.copy(
                        connectionState = ConnectionState.Error(msg),
                        lastError = msg,
                        errorHint = hint,
                        successMessage = null
                    )
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _state.update { it.copy(connectionState = ConnectionState.Disconnecting) }
            try {
                withContext(Dispatchers.IO) {
                    backend.setState(tunnel, Tunnel.State.DOWN, null)
                }
            } catch (e: Exception) {
                Logger.e(e, "Disconnect failed")
                _state.update {
                    it.copy(
                        connectionState = ConnectionState.Idle,
                        lastError = null
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update {
            it.copy(
                connectionState = ConnectionState.Idle,
                lastError = null,
                errorHint = null,
                successMessage = null,
                vpnPrepareIntent = null
            )
        }
    }

    fun clearVpnPrepareIntent() {
        _state.update { it.copy(vpnPrepareIntent = null) }
    }

    private fun userFriendlyMessageAndHint(e: Throwable): Pair<String, String?> {
        val msg = e.message?.lowercase() ?: ""
        val fullMessage = e.message ?: "Connection failed"
        return when {
            msg.contains("provisioning failed:") ->
                "Provisioning failed" to fullMessage.removePrefix("Provisioning failed: ").trim().ifBlank {
                    "Ensure the server is running and PROVISIONING_BASE_URL points to it (e.g. http://YOUR_SERVER_IP:3000)."
                }
            msg.contains("provision") || msg.contains("connection refused") ->
                "Provisioning failed" to "Ensure the provisioning server is running and PROVISIONING_BASE_URL in build.gradle.kts points to it (e.g. http://YOUR_SERVER_IP:3000)."
            msg.contains("timeout") || msg.contains("timed out") ->
                "Connection timed out" to "Check that the server is reachable and port 3000 is open."
            msg.contains("network") || msg.contains("unreachable") ->
                "Server unreachable" to "Check internet connection and server URL."
            else -> (e.message ?: "Connection failed") to null
        }
    }
}
