package com.novavpn.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novavpn.app.security.SecureStorage
import com.novavpn.app.vpn.WireGuardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    private val wireGuardManager: WireGuardManager
) : ViewModel() {

    private val _autoConnect = MutableStateFlow(secureStorage.getAutoConnect())
    val autoConnect: StateFlow<Boolean> = _autoConnect.asStateFlow()

    private val _devicePublicKey = MutableStateFlow<String?>(null)
    val devicePublicKey: StateFlow<String?> = _devicePublicKey.asStateFlow()

    init {
        viewModelScope.launch {
            _devicePublicKey.value = wireGuardManager.getPublicKeyBase64()
        }
    }

    fun setAutoConnect(value: Boolean) {
        secureStorage.setAutoConnect(value)
        _autoConnect.value = value
    }
}
