package com.novavpn.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.novavpn.app.util.Logger
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.novavpn.app.ui.theme.NovaVpnTheme
import com.novavpn.app.vpn.BootReceiver
import com.novavpn.app.viewmodel.VpnViewModel
import com.wireguard.android.backend.GoBackend
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vpnViewModel: VpnViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Logger.d("VPN permission result: resultCode=${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            Logger.d("VPN permission granted, connecting")
            vpnViewModel.connect()
        } else {
            Logger.w("VPN permission denied or cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tryConnect: () -> Unit = {
            Logger.d("tryConnect: checking VPN permission")
            val intent = GoBackend.VpnService.prepare(this)
            if (intent != null) {
                Logger.d("tryConnect: VPN permission needed, launching system dialog")
                vpnPermissionLauncher.launch(intent)
            } else {
                Logger.d("tryConnect: permission OK, calling viewModel.connect()")
                vpnViewModel.connect()
            }
        }
        setContent {
            NovaVpnTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NovaVpnApp(
                        tryConnect = tryConnect,
                        autoConnectRequested = intent?.getBooleanExtra(BootReceiver.EXTRA_AUTO_CONNECT, false) == true
                    )
                }
            }
        }
    }
}
