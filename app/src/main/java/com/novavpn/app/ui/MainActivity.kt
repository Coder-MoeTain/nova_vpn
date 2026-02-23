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
import androidx.lifecycle.lifecycleScope
import com.novavpn.app.security.SecureStorage
import com.novavpn.app.ui.theme.NovaVpnTheme
import com.novavpn.app.vpn.BootReceiver
import com.novavpn.app.viewmodel.VpnViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vpnViewModel: VpnViewModel by viewModels()

    @Inject lateinit var secureStorage: SecureStorage

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        vpnViewModel.onVpnPrepareResult(result.resultCode == RESULT_OK)
    }

    private val locationAndPhonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        vpnViewModel.onPermissionsResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.getBooleanExtra(EXTRA_CONNECT_NOW, false) == true) {
            vpnViewModel.connect()
        }
        // Check tunnel state when activity is created
        lifecycleScope.launch {
            vpnViewModel.checkTunnelStateOnResume()
        }
        setContent {
            NovaVpnTheme {
                val state by vpnViewModel.state.collectAsState()
                val permissionRequest by vpnViewModel.permissionRequest.collectAsState()
                LaunchedEffect(state.vpnPrepareIntent) {
                    state.vpnPrepareIntent?.let { intent ->
                        vpnPermissionLauncher.launch(intent)
                        vpnViewModel.clearVpnPrepareIntent()
                    }
                }
                LaunchedEffect(permissionRequest) {
                    permissionRequest?.let { perms ->
                        if (perms.isNotEmpty()) {
                            locationAndPhonePermissionLauncher.launch(perms)
                        }
                    }
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    NovaVpnApp(
                        vpnViewModel = vpnViewModel,
                        tryConnect = { vpnViewModel.connect() },
                        tryDisconnect = { vpnViewModel.disconnect() },
                        autoConnectRequested = intent?.getBooleanExtra(BootReceiver.EXTRA_AUTO_CONNECT, false) == true,
                        secureStorage = secureStorage
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check tunnel state when activity resumes to restore connection state
        lifecycleScope.launch {
            vpnViewModel.checkTunnelStateOnResume()
        }
    }

    companion object {
        const val EXTRA_CONNECT_NOW = "connect_now"
    }
}
