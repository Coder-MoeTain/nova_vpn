package com.novavpn.app.viewmodel;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.VpnService;
import android.Manifest;
import com.novavpn.app.vpn.VpnNotificationService;
import androidx.lifecycle.ViewModel;
import android.os.Build;
import com.novavpn.app.BuildConfig;
import com.novavpn.app.api.WireGuardConfigResponse;
import com.novavpn.app.api.WireGuardProvisionRequest;
import com.novavpn.app.util.Logger;
import com.novavpn.app.vpn.NovaTunnel;
import com.novavpn.app.vpn.WireGuardConfigBuilder;
import com.novavpn.app.vpn.WireGuardKeyGenerator;
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.BadConfigException;
import com.wireguard.config.Config;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0084\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u000e\n\u0002\u0010\u0003\n\u0000\b\u0007\u0018\u00002\u00020\u0001B1\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\b\u0010\u001c\u001a\u00020\u001dH\u0002J\u0010\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0010H\u0002J\u0006\u0010!\u001a\u00020\u001dJ\u000e\u0010\"\u001a\u00020\u001dH\u0082@\u00a2\u0006\u0002\u0010#J\u0006\u0010$\u001a\u00020\u001dJ\u0006\u0010%\u001a\u00020\u001dJ\u0006\u0010&\u001a\u00020\u001dJ\u0006\u0010\'\u001a\u00020\u001dJ\b\u0010(\u001a\u00020\u001dH\u0002J\u001c\u0010)\u001a\u000e\u0012\u0004\u0012\u00020+\u0012\u0004\u0012\u00020+0*2\u0006\u0010,\u001a\u00020-H\u0002J\u0014\u0010.\u001a\u000e\u0012\u0004\u0012\u00020+\u0012\u0004\u0012\u00020+0*H\u0002J\b\u0010/\u001a\u000200H\u0002J\u001a\u00101\u001a\u00020\u001d2\u0012\u00102\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020003J\u000e\u00104\u001a\u00020\u001d2\u0006\u00105\u001a\u000200J\b\u00106\u001a\u00020\u001dH\u0002J\b\u00107\u001a\u00020\u001dH\u0002J\u0010\u00108\u001a\u00020\u001d2\u0006\u00109\u001a\u00020\u0010H\u0002J\b\u0010:\u001a\u00020\u001dH\u0002J\b\u0010;\u001a\u00020\u001dH\u0002J\b\u0010<\u001a\u00020\u0010H\u0002J\u0018\u0010=\u001a\u00020\u001d2\u0006\u0010>\u001a\u00020+2\u0006\u0010?\u001a\u00020+H\u0002J\u001e\u0010@\u001a\u0010\u0012\u0004\u0012\u00020\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u00100*2\u0006\u0010A\u001a\u00020BH\u0002R\u001a\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010\u0013\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000f0\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0016R\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006C"}, d2 = {"Lcom/novavpn/app/viewmodel/VpnViewModel;", "Landroidx/lifecycle/ViewModel;", "context", "Landroid/content/Context;", "secureStorage", "Lcom/novavpn/app/security/SecureStorage;", "provisioningApi", "Lcom/novavpn/app/api/ProvisioningApi;", "backend", "Lcom/wireguard/android/backend/Backend;", "tunnel", "Lcom/novavpn/app/vpn/NovaTunnel;", "(Landroid/content/Context;Lcom/novavpn/app/security/SecureStorage;Lcom/novavpn/app/api/ProvisioningApi;Lcom/wireguard/android/backend/Backend;Lcom/novavpn/app/vpn/NovaTunnel;)V", "_permissionRequest", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "", "_state", "Lcom/novavpn/app/viewmodel/VpnUiState;", "permissionRequest", "Lkotlinx/coroutines/flow/StateFlow;", "getPermissionRequest", "()Lkotlinx/coroutines/flow/StateFlow;", "state", "getState", "stateCheckJob", "Lkotlinx/coroutines/Job;", "trafficStatsJob", "autoReconnect", "", "buildProvisionRequest", "Lcom/novavpn/app/api/WireGuardProvisionRequest;", "publicKey", "checkTunnelStateOnResume", "checkTunnelStateOnce", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearError", "clearVpnPrepareIntent", "connect", "disconnect", "doConnect", "extractRxTxFromStats", "Lkotlin/Pair;", "", "stats", "", "getTrafficStats", "isAnyVpnActive", "", "onPermissionsResult", "result", "", "onVpnPrepareResult", "granted", "startPeriodicStateCheck", "startTrafficStatsPolling", "startVpnNotificationService", "status", "stopTrafficStatsPolling", "stopVpnNotificationService", "testConnectionThroughVpn", "updateVpnNotification", "rxBytes", "txBytes", "userFriendlyMessageAndHint", "e", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class VpnViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.security.SecureStorage secureStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.api.ProvisioningApi provisioningApi = null;
    @org.jetbrains.annotations.NotNull()
    private final com.wireguard.android.backend.Backend backend = null;
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.vpn.NovaTunnel tunnel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.novavpn.app.viewmodel.VpnUiState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.novavpn.app.viewmodel.VpnUiState> state = null;
    
    /**
     * When non-null, the UI should request these permissions then call onPermissionsResult. Used before first provision to send device location.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String[]> _permissionRequest = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String[]> permissionRequest = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job trafficStatsJob;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job stateCheckJob;
    
    @javax.inject.Inject()
    public VpnViewModel(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.novavpn.app.security.SecureStorage secureStorage, @org.jetbrains.annotations.NotNull()
    com.novavpn.app.api.ProvisioningApi provisioningApi, @org.jetbrains.annotations.NotNull()
    com.wireguard.android.backend.Backend backend, @org.jetbrains.annotations.NotNull()
    com.novavpn.app.vpn.NovaTunnel tunnel) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.novavpn.app.viewmodel.VpnUiState> getState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String[]> getPermissionRequest() {
        return null;
    }
    
    /**
     * Periodically check VPN state to keep UI in sync and auto-reconnect if needed
     */
    private final void startPeriodicStateCheck() {
    }
    
    /**
     * Auto-reconnect VPN if we have a saved config
     */
    private final void autoReconnect() {
    }
    
    /**
     * Restore UI state if the VPN is active.
     *
     * Note: after process death, `backend.getState(tunnel)` may return DOWN even if the system VPN is still up.
     * We also detect an active VPN via `ConnectivityManager` so UI matches reality.
     */
    private final java.lang.Object checkTunnelStateOnce(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Public method to check tunnel state (called from Activity lifecycle)
     */
    public final void checkTunnelStateOnResume() {
    }
    
    private final boolean isAnyVpnActive() {
        return false;
    }
    
    /**
     * Optional check: if an HTTP request succeeds, we show "Internet OK". If it fails, we don't assume no internet (traffic may still work).
     */
    private final java.lang.String testConnectionThroughVpn() {
        return null;
    }
    
    /**
     * Call after user has completed VPN permission dialog (e.g. Activity result OK).
     */
    public final void onVpnPrepareResult(boolean granted) {
    }
    
    public final void connect() {
    }
    
    private final void doConnect() {
    }
    
    public final void disconnect() {
    }
    
    public final void clearError() {
    }
    
    public final void clearVpnPrepareIntent() {
    }
    
    /**
     * Call after the UI has finished the location permission request. Continues connect (provision with whatever data we have).
     */
    public final void onPermissionsResult(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.Boolean> result) {
    }
    
    /**
     * Builds provision request with device hostname, model, and last known location when available.
     */
    private final com.novavpn.app.api.WireGuardProvisionRequest buildProvisionRequest(java.lang.String publicKey) {
        return null;
    }
    
    private final kotlin.Pair<java.lang.String, java.lang.String> userFriendlyMessageAndHint(java.lang.Throwable e) {
        return null;
    }
    
    /**
     * Start polling WireGuard interface traffic statistics
     */
    private final void startTrafficStatsPolling() {
    }
    
    /**
     * Stop polling traffic statistics
     */
    private final void stopTrafficStatsPolling() {
    }
    
    /**
     * Read traffic statistics from WireGuard interface
     */
    private final kotlin.Pair<java.lang.Long, java.lang.Long> getTrafficStats() {
        return null;
    }
    
    private final kotlin.Pair<java.lang.Long, java.lang.Long> extractRxTxFromStats(java.lang.Object stats) {
        return null;
    }
    
    /**
     * Start VPN notification service
     */
    private final void startVpnNotificationService(java.lang.String status) {
    }
    
    /**
     * Update VPN notification with stats
     */
    private final void updateVpnNotification(long rxBytes, long txBytes) {
    }
    
    /**
     * Stop VPN notification service
     */
    private final void stopVpnNotificationService() {
    }
}