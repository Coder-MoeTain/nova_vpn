package com.novavpn.app.viewmodel

import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.Manifest
import com.novavpn.app.vpn.VpnNotificationService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.Build
import com.novavpn.app.BuildConfig
import com.novavpn.app.api.WireGuardConfigResponse
import com.novavpn.app.api.WireGuardProvisionRequest
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class VpnUiState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val lastError: String? = null,
    val errorHint: String? = null,
    val successMessage: String? = null,
    /** Non-null when VPN permission is needed; Activity should launch this intent. */
    val vpnPrepareIntent: Intent? = null,
    /** When Connected: "Checking…", "Internet OK", or "No internet" (traffic not going through VPN). */
    val connectionTestResult: String? = null,
    /** Traffic statistics: received and transmitted bytes */
    val rxBytes: Long = 0,
    val txBytes: Long = 0,
    /** Flag to indicate state check is in progress (prevents showing wrong state) */
    val isCheckingState: Boolean = false
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

    /** When non-null, the UI should request these permissions then call onPermissionsResult. Used before first provision to send device location. */
    private val _permissionRequest = MutableStateFlow<Array<String>?>(null)
    val permissionRequest: StateFlow<Array<String>?> = _permissionRequest.asStateFlow()

    private var trafficStatsJob: kotlinx.coroutines.Job? = null
    private var stateCheckJob: kotlinx.coroutines.Job? = null
    private val workManager: WorkManager? by lazy { 
        try {
            WorkManager.getInstance(context)
        } catch (e: IllegalStateException) {
            Logger.w(e, "WorkManager not initialized yet")
            null
        }
    }
    private val LOCATION_WORK_TAG = "location_reporting"

    init {
        tunnel.setStateListener { newState ->
            viewModelScope.launch {
                when (newState) {
                    Tunnel.State.UP -> {
                        _state.update { s ->
                            s.copy(
                                connectionState = ConnectionState.Connected,
                                lastError = null,
                                errorHint = null,
                                successMessage = "Connected",
                                vpnPrepareIntent = null,
                                connectionTestResult = "Checking…",
                                isCheckingState = false
                            )
                        }
                        // Start foreground service with notification
                        startVpnNotificationService("Connected")
                        // Give the tunnel a moment to apply routes before testing
                        delay(2500)
                        val testResult = withContext(Dispatchers.IO) { testConnectionThroughVpn() }
                        _state.update { it.copy(connectionTestResult = testResult) }
                        // Start polling traffic stats
                        startTrafficStatsPolling()
                        // Start periodic location reporting
                        startLocationReporting()
                    }
                    Tunnel.State.DOWN -> {
                        // Only update to DOWN if we're actually disconnecting, not if it's just a state check issue
                        // Check if VPN is actually active via system before setting to DOWN
                        val vpnActuallyActive = isAnyVpnActive()
                        val hasConfig = secureStorage.getWireGuardConfigJson() != null
                        
                        if (!vpnActuallyActive) {
                            stopTrafficStatsPolling()
                            stopLocationReporting()
                            stopVpnNotificationService()
                            val wasDisconnecting = _state.value.connectionState == ConnectionState.Disconnecting
                            
                            _state.update { s ->
                                s.copy(
                                    connectionState = if (wasDisconnecting)
                                        ConnectionState.Idle else s.connectionState,
                                    vpnPrepareIntent = null,
                                    connectionTestResult = null,
                                    rxBytes = 0,
                                    txBytes = 0
                                )
                            }
                            
                            // If VPN went down but we have config and user didn't explicitly disconnect, auto-reconnect
                            if (hasConfig && !wasDisconnecting) {
                                Logger.d("VPN disconnected unexpectedly, attempting auto-reconnect")
                                delay(2000) // Wait a bit before reconnecting
                                autoReconnect()
                            }
                        } else {
                            // Backend says DOWN but system says VPN is active - keep UI as Connected
                            Logger.d("Backend reports DOWN but VPN is active via system, keeping Connected state")
                        }
                    }
                    else -> _state.update { it.copy(vpnPrepareIntent = null) }
                }
            }
        }
        // Restore UI state if VPN is already active (e.g. after reopening app)
        viewModelScope.launch { 
            checkTunnelStateOnce()
            // Also periodically check state while app is running (in case VPN state changes externally)
            // This also handles auto-reconnect if VPN disconnects
            startPeriodicStateCheck()
            
            // If we have a config but VPN is not connected, try to reconnect
            delay(2000) // Wait a bit for initial state check
            val hasConfig = secureStorage.getWireGuardConfigJson() != null
            val currentState = _state.value.connectionState
            val vpnActive = isAnyVpnActive()
            
            if (hasConfig && currentState !is ConnectionState.Connected && !vpnActive) {
                Logger.d("App reopened with config but VPN disconnected, attempting auto-reconnect")
                autoReconnect()
            }
        }
    }
    
    /** Periodically check VPN state to keep UI in sync and auto-reconnect if needed */
    private fun startPeriodicStateCheck() {
        stateCheckJob?.cancel()
        stateCheckJob = viewModelScope.launch {
            while (true) {
                delay(10000) // Check every 10 seconds
                val currentState = _state.value.connectionState
                val hasConfig = secureStorage.getWireGuardConfigJson() != null
                
                // If we have a config (meaning user connected before), check if VPN should be active
                if (hasConfig) {
                    if (currentState !is ConnectionState.Connected && currentState !is ConnectionState.Connecting && currentState !is ConnectionState.Disconnecting) {
                        // Check if VPN is actually active via system
                        val vpnActive = isAnyVpnActive()
                        if (vpnActive) {
                            // VPN is active but UI shows disconnected - restore UI state
                            Logger.d("VPN is active but UI shows disconnected, restoring state")
                            checkTunnelStateOnce()
                        } else {
                            // VPN is down but we have config - auto-reconnect
                            Logger.d("VPN disconnected but config exists, auto-reconnecting...")
                            autoReconnect()
                        }
                    } else if (currentState is ConnectionState.Connected) {
                        // Verify VPN is still actually active
                        val vpnActive = isAnyVpnActive()
                        if (!vpnActive) {
                            Logger.d("UI shows connected but VPN is down, reconnecting...")
                            autoReconnect()
                        }
                    }
                }
            }
        }
    }
    
    /** Auto-reconnect VPN if we have a saved config */
    private fun autoReconnect() {
        viewModelScope.launch {
            try {
                val hasConfig = secureStorage.getWireGuardConfigJson() != null
                if (!hasConfig) {
                    Logger.d("No config found, cannot auto-reconnect")
                    return@launch
                }
                
                // Check if VPN permission is still granted
                val prepareIntent = VpnService.prepare(context)
                if (prepareIntent != null) {
                    Logger.d("VPN permission needed for auto-reconnect")
                    // Can't auto-reconnect without permission, user needs to grant it
                    return@launch
                }
                
                Logger.d("Auto-reconnecting VPN...")
                // Use doConnect() which will use the saved config
                doConnect()
            } catch (e: Exception) {
                Logger.e(e, "Auto-reconnect failed")
            }
        }
    }

    /**
     * Restore UI state if the VPN is active.
     *
     * Note: after process death, `backend.getState(tunnel)` may return DOWN even if the system VPN is still up.
     * We also detect an active VPN via `ConnectivityManager` so UI matches reality.
     */
    private suspend fun checkTunnelStateOnce() {
        try {
            // Set checking flag to prevent showing wrong state
            _state.update { it.copy(isCheckingState = true) }
            
            val hasConfig = secureStorage.getWireGuardConfigJson() != null
            if (!hasConfig) {
                Logger.d("No config found, VPN not connected")
                _state.update { it.copy(isCheckingState = false) }
                return
            }
            
            // Give the system a moment to initialize
            delay(300)
            
            val backendState = try {
                withContext(Dispatchers.IO) { backend.getState(tunnel) }
            } catch (e: Exception) {
                Logger.d("Backend getState failed: ${e.message}")
                null
            }

            val vpnActiveViaBackend = backendState == Tunnel.State.UP
            val vpnActiveViaSystem = isAnyVpnActive()
            
            Logger.d("VPN state check: backend=$backendState, system=$vpnActiveViaSystem, hasConfig=$hasConfig")
            
            // VPN is active if:
            // 1. Backend says UP, OR
            // 2. System detects VPN is active (we have config, so it's likely ours)
            // Note: We trust system VPN detection if we have a config, because WireGuard VPNs persist
            val vpnActive = vpnActiveViaBackend || vpnActiveViaSystem
            
            if (vpnActive) {
                Logger.d("VPN is active, restoring UI state to Connected (backend=$backendState, system=$vpnActiveViaSystem)")
                _state.update { s ->
                    s.copy(
                        connectionState = ConnectionState.Connected,
                        connectionTestResult = "Connected",
                        successMessage = "Connected",
                        lastError = null,
                        errorHint = null,
                        vpnPrepareIntent = null,
                        isCheckingState = false
                    )
                }
                startVpnNotificationService("Connected")
                startTrafficStatsPolling()
            } else {
                Logger.d("VPN not detected as active (backend=$backendState, system=$vpnActiveViaSystem, hasConfig=$hasConfig)")
                // If we have config but VPN not detected, check one more time after a delay
                // Sometimes the system needs more time to report VPN state
                if (hasConfig) {
                    delay(1000)
                    val retryVpnActive = isAnyVpnActive()
                    if (retryVpnActive) {
                        Logger.d("VPN detected on retry, restoring UI state to Connected")
                        _state.update { s ->
                            s.copy(
                                connectionState = ConnectionState.Connected,
                                connectionTestResult = "Connected",
                                successMessage = "Connected",
                                lastError = null,
                                errorHint = null,
                                vpnPrepareIntent = null,
                                isCheckingState = false
                            )
                        }
                        startTrafficStatsPolling()
                    } else {
                        _state.update { it.copy(isCheckingState = false) }
                    }
                } else {
                    _state.update { it.copy(isCheckingState = false) }
                }
            }
        } catch (e: Exception) {
            Logger.e(e, "Could not restore VPN state")
            _state.update { it.copy(isCheckingState = false) }
        }
    }

    /** Public method to check tunnel state (called from Activity lifecycle) */
    fun checkTunnelStateOnResume() {
        viewModelScope.launch { 
            // Check immediately
            checkTunnelStateOnce()
            // Check again after delays to catch cases where system needs time to report VPN state
            delay(800)
            checkTunnelStateOnce()
            delay(1200)
            checkTunnelStateOnce()
        }
    }

    private fun isAnyVpnActive(): Boolean {
        return try {
            val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            
            // Method 1: Check active network (most reliable - this is the network currently being used)
            val activeNetwork = cm.activeNetwork
            if (activeNetwork != null) {
                val caps = cm.getNetworkCapabilities(activeNetwork)
                if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    Logger.d("VPN detected via active network")
                    return true
                }
            }
            
            // Method 2: Check all networks for VPN transport
            // Be lenient - if ANY VPN transport exists, consider it active
            // (WireGuard VPNs persist even if backend state is unclear)
            val networks = cm.allNetworks
            if (networks != null) {
                for (network in networks) {
                    val caps = cm.getNetworkCapabilities(network) ?: continue
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        // Don't require validation - if VPN transport exists, it's likely active
                        // This is important because after app restart, validation might not be immediate
                        Logger.d("VPN detected via allNetworks scan (VPN transport found)")
                        return true
                    }
                }
            }
            
            Logger.d("No VPN detected via any method")
            return false
        } catch (e: Exception) {
            Logger.e(e, "Error checking VPN status")
            false
        }
    }

    /** Optional check: if an HTTP request succeeds, we show "Internet OK". If it fails, we don't assume no internet (traffic may still work). */
    private fun testConnectionThroughVpn(): String {
        return try {
            val url = java.net.URL("http://142.250.80.46")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            conn.instanceFollowRedirects = true
            val code = conn.responseCode
            conn.disconnect()
            if (code in 200..399) "Internet OK" else "Connection check inconclusive (try browsing)"
        } catch (e: Exception) {
            Logger.d("Connection check failed: ${e.message}")
            "Connection check inconclusive (try browsing)"
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
                    val missing = mutableListOf<String>()
                    if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        missing.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    if (missing.isNotEmpty()) {
                        _permissionRequest.value = missing.toTypedArray()
                        return@launch
                    }
                    val provisionRequest = withContext(Dispatchers.IO) { buildProvisionRequest(publicKey) }
                    wgConfig = withContext(Dispatchers.IO) { provisioningApi.provisionWireGuard(provisionRequest) }
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
                vpnPrepareIntent = null,
                connectionTestResult = null
            )
        }
    }

    fun clearVpnPrepareIntent() {
        _state.update { it.copy(vpnPrepareIntent = null) }
    }

    /** Call after the UI has finished the location permission request. Continues connect (provision with whatever data we have). */
    fun onPermissionsResult(result: Map<String, Boolean>) {
        _permissionRequest.value = null
        viewModelScope.launch { doConnect() }
    }

    /** Builds provision request with device hostname, model, and last known location when available. */
    private fun buildProvisionRequest(publicKey: String): WireGuardProvisionRequest {
        val hostname = Build.DEVICE
        val model = listOf(Build.MANUFACTURER, Build.MODEL).filter { it.isNotBlank() }.joinToString(" ").trim()
            .ifBlank { Build.MODEL }
        val (lat, lng) = try {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
            val loc = lm?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            if (loc != null) Pair(loc.latitude, loc.longitude) else Pair(null, null)
        } catch (_: SecurityException) {
            Pair(null, null)
        }
        return WireGuardProvisionRequest(
            publicKey = publicKey,
            hostname = hostname,
            model = model,
            phoneNumber = null,
            latitude = lat,
            longitude = lng
        )
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

    /** Start polling WireGuard interface traffic statistics */
    private fun startTrafficStatsPolling() {
        stopTrafficStatsPolling()
        trafficStatsJob = viewModelScope.launch {
            while (true) {
                try {
                    val stats = getTrafficStats()
                    _state.update { it.copy(rxBytes = stats.first, txBytes = stats.second) }
                    // Update notification with stats
                    updateVpnNotification(stats.first, stats.second)
                } catch (e: Exception) {
                    Logger.d("Failed to get traffic stats: ${e.message}")
                }
                delay(2000) // Poll every 2 seconds
            }
        }
    }

    /** Stop polling traffic statistics */
    private fun stopTrafficStatsPolling() {
        trafficStatsJob?.cancel()
        trafficStatsJob = null
    }

    /** Start periodic location reporting every 5 minutes */
    private fun startLocationReporting() {
        val wm = workManager ?: run {
            Logger.w("WorkManager not available, skipping location reporting setup")
            // Still schedule immediate reports via coroutine
            scheduleImmediateLocationReport()
            return
        }
        
        try {
            // Cancel any existing location reporting work
            wm.cancelAllWorkByTag(LOCATION_WORK_TAG)
            
            // Create constraints: require network connection
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            // Create periodic work request: every 15 minutes (Android's minimum for periodic work)
            // Note: Android enforces minimum 15 minutes for PeriodicWorkRequest
            // For more frequent updates (5 minutes), we'd need to use OneTimeWorkRequest with chaining
            val locationWorkRequest = PeriodicWorkRequestBuilder<com.novavpn.app.work.LocationReportingWorker>(
                15, TimeUnit.MINUTES  // Minimum allowed is 15 minutes
            )
                .setConstraints(constraints)
                .addTag(LOCATION_WORK_TAG)
                .build()
            
            wm.enqueue(locationWorkRequest)
            Logger.d("VpnViewModel: Started periodic location reporting (every 15 minutes)")
            
            // For 5-minute intervals, also schedule immediate work and chain it
            scheduleImmediateLocationReport()
        } catch (e: Exception) {
            Logger.e(e, "VpnViewModel: Failed to start location reporting")
            // Still try immediate reports
            scheduleImmediateLocationReport()
        }
    }

    /** Schedule immediate location report and chain for 5-minute intervals */
    private fun scheduleImmediateLocationReport() {
        viewModelScope.launch {
            // Report immediately
            reportLocationNow()
            
            // Then schedule recurring 5-minute reports
            // This allows us to report every 5 minutes despite Android's 15-minute minimum for periodic work
            repeat(Int.MAX_VALUE) { // Continue until disconnected
                delay(5 * 60 * 1000L) // Wait 5 minutes
                if (_state.value.connectionState == ConnectionState.Connected) {
                    reportLocationNow()
                } else {
                    return@launch // Stop if disconnected
                }
            }
        }
    }

    /** Report location immediately */
    private suspend fun reportLocationNow() {
        try {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            
            val privateKeyBase64 = secureStorage.getWireGuardPrivateKeyBase64() ?: return
            val publicKey = WireGuardKeyGenerator.getPublicKeyFromPrivate(privateKeyBase64)
            
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
            val loc = lm?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            
            if (loc != null && loc.latitude != 0.0 && loc.longitude != 0.0) {
                withContext(Dispatchers.IO) {
                    provisioningApi.reportLocation(publicKey, loc.latitude, loc.longitude)
                }
            }
        } catch (e: Exception) {
            Logger.w(e, "VpnViewModel: Failed to report location immediately")
        }
    }

    /** Stop periodic location reporting */
    private fun stopLocationReporting() {
        val wm = workManager ?: return
        try {
            wm.cancelAllWorkByTag(LOCATION_WORK_TAG)
            Logger.d("VpnViewModel: Stopped periodic location reporting")
        } catch (e: Exception) {
            Logger.w(e, "VpnViewModel: Failed to stop location reporting")
        }
    }

    /** Read traffic statistics from WireGuard interface */
    private fun getTrafficStats(): Pair<Long, Long> {
        val interfaceName = com.novavpn.app.vpn.NovaTunnel.TUNNEL_NAME
        var rxBytes = 0L
        var txBytes = 0L

        // Preferred: ask the WireGuard backend for tunnel statistics (most reliable across devices)
        try {
            val stats = backend.javaClass.methods.firstOrNull { m ->
                m.name == "getStatistics" && m.parameterTypes.size == 1
            }?.invoke(backend, tunnel)
            if (stats != null) {
                // Try common shapes:
                // - object with totalRx/totalTx fields or getters
                // - map of peer->(rx,tx) which we can sum
                val (rx, tx) = extractRxTxFromStats(stats)
                if (rx >= 0 && tx >= 0 && (rx > 0 || tx > 0)) return Pair(rx, tx)
            }
        } catch (_: Exception) {
            // fall back to filesystem parsing
        }
        
        // First try: direct interface statistics files
        try {
            val rxFile = java.io.File("/sys/class/net/$interfaceName/statistics/rx_bytes")
            val txFile = java.io.File("/sys/class/net/$interfaceName/statistics/tx_bytes")
            if (rxFile.exists() && txFile.exists()) {
                rxBytes = rxFile.readText().trim().toLongOrNull() ?: 0L
                txBytes = txFile.readText().trim().toLongOrNull() ?: 0L
                return Pair(rxBytes, txBytes)
            }
        } catch (e: Exception) {
            Logger.d("Failed to read stats from /sys/class/net: ${e.message}")
        }
        
        // Second try: parse /proc/net/dev (works even if interface name differs)
        try {
            val procNetDev = java.io.File("/proc/net/dev")
            if (procNetDev.exists()) {
                val lines = procNetDev.readText().lines()
                for (line in lines) {
                    // Format: "  interface_name: rx_bytes rx_packets ... tx_bytes tx_packets ..."
                    // Skip header lines
                    if (!line.contains(":") || line.contains("Inter-") || line.contains("face")) continue
                    
                    val colonIndex = line.indexOf(':')
                    if (colonIndex < 0) continue
                    
                    val ifaceName = line.substring(0, colonIndex).trim()
                    val statsPart = line.substring(colonIndex + 1).trim()
                    
                    // Check if this interface matches our tunnel name or common WireGuard names
                    if (ifaceName.contains(interfaceName, ignoreCase = true) || 
                        ifaceName.contains("wg", ignoreCase = true) ||
                        ifaceName.contains("tun", ignoreCase = true)) {
                        val parts = statsPart.split(Regex("\\s+"))
                        if (parts.size >= 9) {
                            // parts[0] is rx_bytes, parts[8] is tx_bytes
                            val ifaceRx = parts[0].toLongOrNull() ?: 0L
                            val ifaceTx = parts[8].toLongOrNull() ?: 0L
                            rxBytes = ifaceRx
                            txBytes = ifaceTx
                            return Pair(rxBytes, txBytes)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.d("Failed to read stats from /proc/net/dev: ${e.message}")
        }
        
        // Third try: try common WireGuard interface names
        val commonNames = listOf("wg0", "wg1", "novavpn", "tun0", "tun1")
        for (name in commonNames) {
            try {
                val rxFile = java.io.File("/sys/class/net/$name/statistics/rx_bytes")
                val txFile = java.io.File("/sys/class/net/$name/statistics/tx_bytes")
                if (rxFile.exists() && txFile.exists()) {
                    rxBytes = rxFile.readText().trim().toLongOrNull() ?: 0L
                    txBytes = txFile.readText().trim().toLongOrNull() ?: 0L
                    if (rxBytes > 0 || txBytes > 0) {
                        return Pair(rxBytes, txBytes)
                    }
                }
            } catch (e: Exception) {
                // Continue to next name
            }
        }
        
        return Pair(rxBytes, txBytes)
    }

    private fun extractRxTxFromStats(stats: Any): Pair<Long, Long> {
        // Case 1: fields
        try {
            val c = stats.javaClass
            val rxField = c.declaredFields.firstOrNull { it.name.equals("totalRx", true) || it.name.equals("rxBytes", true) }
            val txField = c.declaredFields.firstOrNull { it.name.equals("totalTx", true) || it.name.equals("txBytes", true) }
            if (rxField != null && txField != null) {
                rxField.isAccessible = true
                txField.isAccessible = true
                val rx = (rxField.get(stats) as? Number)?.toLong() ?: 0L
                val tx = (txField.get(stats) as? Number)?.toLong() ?: 0L
                return Pair(rx, tx)
            }
        } catch (_: Exception) {}

        // Case 2: getters
        try {
            val c = stats.javaClass
            val rxMethod = c.methods.firstOrNull { it.name.equals("getTotalRx", true) || it.name.equals("getRxBytes", true) || it.name.equals("totalRx", true) }
            val txMethod = c.methods.firstOrNull { it.name.equals("getTotalTx", true) || it.name.equals("getTxBytes", true) || it.name.equals("totalTx", true) }
            if (rxMethod != null && txMethod != null) {
                val rx = (rxMethod.invoke(stats) as? Number)?.toLong() ?: 0L
                val tx = (txMethod.invoke(stats) as? Number)?.toLong() ?: 0L
                return Pair(rx, tx)
            }
        } catch (_: Exception) {}

        // Case 3: Map-like (sum all numeric values)
        try {
            if (stats is Map<*, *>) {
                var rx = 0L
                var tx = 0L
                stats.values.forEach { v ->
                    if (v == null) return@forEach
                    val (vrx, vtx) = extractRxTxFromStats(v)
                    rx += vrx
                    tx += vtx
                }
                return Pair(rx, tx)
            }
        } catch (_: Exception) {}

        return Pair(0L, 0L)
    }

    /** Start VPN notification service */
    private fun startVpnNotificationService(status: String) {
        try {
            val intent = Intent(context, VpnNotificationService::class.java).apply {
                action = VpnNotificationService.ACTION_UPDATE_STATS
                putExtra(VpnNotificationService.EXTRA_STATUS, status)
                putExtra(VpnNotificationService.EXTRA_RX_BYTES, _state.value.rxBytes)
                putExtra(VpnNotificationService.EXTRA_TX_BYTES, _state.value.txBytes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Logger.e(e, "Failed to start VPN notification service")
        }
    }

    /** Update VPN notification with stats */
    private fun updateVpnNotification(rxBytes: Long, txBytes: Long) {
        try {
            val intent = Intent(context, VpnNotificationService::class.java).apply {
                action = VpnNotificationService.ACTION_UPDATE_STATS
                putExtra(VpnNotificationService.EXTRA_STATUS, "Connected")
                putExtra(VpnNotificationService.EXTRA_RX_BYTES, rxBytes)
                putExtra(VpnNotificationService.EXTRA_TX_BYTES, txBytes)
            }
            context.startService(intent)
        } catch (e: Exception) {
            Logger.d("Failed to update VPN notification: ${e.message}")
        }
    }

    /** Stop VPN notification service */
    private fun stopVpnNotificationService() {
        try {
            val intent = Intent(context, VpnNotificationService::class.java)
            context.stopService(intent)
        } catch (e: Exception) {
            Logger.d("Failed to stop VPN notification service: ${e.message}")
        }
    }
}
