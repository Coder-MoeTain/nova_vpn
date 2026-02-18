package com.novavpn.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.novavpn.app.ui.theme.NovaVpnTheme
import com.novavpn.app.vpn.BootReceiver
import com.novavpn.app.viewmodel.VpnViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vpnViewModel: VpnViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        vpnViewModel.onVpnPrepareResult(result.resultCode == RESULT_OK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.getBooleanExtra(EXTRA_CONNECT_NOW, false) == true) {
            vpnViewModel.connect()
        }
        setContent {
            NovaVpnTheme {
                val state by vpnViewModel.state.collectAsState()
                LaunchedEffect(state.vpnPrepareIntent) {
                    state.vpnPrepareIntent?.let { intent ->
                        vpnPermissionLauncher.launch(intent)
                        vpnViewModel.clearVpnPrepareIntent()
                    }
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    NovaVpnApp(
                        vpnViewModel = vpnViewModel,
                        tryConnect = { vpnViewModel.connect() },
                        tryDisconnect = { vpnViewModel.disconnect() },
                        autoConnectRequested = intent?.getBooleanExtra(BootReceiver.EXTRA_AUTO_CONNECT, false) == true
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_CONNECT_NOW = "connect_now"
    }
}
