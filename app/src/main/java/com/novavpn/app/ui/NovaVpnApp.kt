package com.novavpn.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novavpn.app.security.SecureStorage
import com.novavpn.app.viewmodel.LoginViewModel
import com.novavpn.app.viewmodel.VpnViewModel

@Composable
fun NovaVpnApp(
    vpnViewModel: VpnViewModel,
    tryConnect: () -> Unit,
    tryDisconnect: () -> Unit,
    autoConnectRequested: Boolean = false,
    secureStorage: SecureStorage
) {
    val navController = rememberNavController()
    val startDestination = if (secureStorage.isSignedIn) "home" else "login"
    val loginViewModel: LoginViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable("login") {
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable("home") {
            HomeScreen(
                viewModel = vpnViewModel,
                tryConnect = tryConnect,
                tryDisconnect = tryDisconnect,
                autoConnectRequested = autoConnectRequested,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToLogs = { navController.navigate("logs") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    loginViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("logs") {
            LogsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
