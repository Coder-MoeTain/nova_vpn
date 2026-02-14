package com.novavpn.app.viewmodel

import androidx.lifecycle.ViewModel
import com.novavpn.app.security.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
}
