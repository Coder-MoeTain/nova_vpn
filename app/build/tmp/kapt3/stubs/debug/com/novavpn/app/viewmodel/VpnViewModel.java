package com.novavpn.app.viewmodel;

import android.content.Intent;
import android.net.VpnService;
import androidx.lifecycle.ViewModel;
import com.novavpn.app.BuildConfig;
import com.novavpn.app.api.WireGuardConfigResponse;
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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0000\b\u0007\u0018\u00002\u00020\u0001B1\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0006\u0010\u0014\u001a\u00020\u0015J\u0006\u0010\u0016\u001a\u00020\u0015J\u0006\u0010\u0017\u001a\u00020\u0015J\u0006\u0010\u0018\u001a\u00020\u0015J\b\u0010\u0019\u001a\u00020\u0015H\u0002J\u000e\u0010\u001a\u001a\u00020\u00152\u0006\u0010\u001b\u001a\u00020\u001cJ\u001e\u0010\u001d\u001a\u0010\u0012\u0004\u0012\u00020\u001f\u0012\u0006\u0012\u0004\u0018\u00010\u001f0\u001e2\u0006\u0010 \u001a\u00020!H\u0002R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/novavpn/app/viewmodel/VpnViewModel;", "Landroidx/lifecycle/ViewModel;", "context", "Landroid/content/Context;", "secureStorage", "Lcom/novavpn/app/security/SecureStorage;", "provisioningApi", "Lcom/novavpn/app/api/ProvisioningApi;", "backend", "Lcom/wireguard/android/backend/Backend;", "tunnel", "Lcom/novavpn/app/vpn/NovaTunnel;", "(Landroid/content/Context;Lcom/novavpn/app/security/SecureStorage;Lcom/novavpn/app/api/ProvisioningApi;Lcom/wireguard/android/backend/Backend;Lcom/novavpn/app/vpn/NovaTunnel;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/novavpn/app/viewmodel/VpnUiState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearError", "", "clearVpnPrepareIntent", "connect", "disconnect", "doConnect", "onVpnPrepareResult", "granted", "", "userFriendlyMessageAndHint", "Lkotlin/Pair;", "", "e", "", "app_debug"})
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
    
    private final kotlin.Pair<java.lang.String, java.lang.String> userFriendlyMessageAndHint(java.lang.Throwable e) {
        return null;
    }
}