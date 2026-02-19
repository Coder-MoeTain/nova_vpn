package com.novavpn.app.viewmodel

import androidx.lifecycle.ViewModel
import com.novavpn.app.api.WireGuardConfigResponse
import com.novavpn.app.security.SecureStorage
import com.novavpn.app.vpn.WireGuardConfigBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _autoConnect = MutableStateFlow(secureStorage.getAutoConnect())
    val autoConnect: StateFlow<Boolean> = _autoConnect.asStateFlow()

    fun setAutoConnect(value: Boolean) {
        secureStorage.setAutoConnect(value)
        _autoConnect.value = value
    }

    fun clearCachedVpnConfig() {
        secureStorage.clearOpenVpnConfigCache()
        secureStorage.clearAllWireGuard()
    }

    /** Returns current WireGuard config as wg-quick text for pasting into the official WireGuard app, or null if none. */
    fun getConfigForOfficialApp(): String? {
        val privateKey = secureStorage.getWireGuardPrivateKeyBase64() ?: return null
        val json = secureStorage.getWireGuardConfigJson() ?: return null
        val wg = try {
            Json.decodeFromString<WireGuardConfigResponse>(json)
        } catch (_: Exception) {
            return null
        }
        return WireGuardConfigBuilder.buildWgQuickString(privateKey, wg)
    }
}
