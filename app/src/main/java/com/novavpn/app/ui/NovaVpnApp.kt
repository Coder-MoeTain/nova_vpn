package com.novavpn.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novavpn.app.viewmodel.VpnViewModel

@Composable
fun NovaVpnApp(
    vpnViewModel: VpnViewModel,
    tryConnect: () -> Unit,
    autoConnectRequested: Boolean = false
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                viewModel = vpnViewModel,
                tryConnect = tryConnect,
                autoConnectRequested = autoConnectRequested,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToLogs = { navController.navigate("logs") }
            )
        }
        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("logs") {
            LogsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
