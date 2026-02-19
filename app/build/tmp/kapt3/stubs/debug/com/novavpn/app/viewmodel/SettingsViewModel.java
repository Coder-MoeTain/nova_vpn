package com.novavpn.app.viewmodel;

import androidx.lifecycle.ViewModel;
import com.novavpn.app.api.WireGuardConfigResponse;
import com.novavpn.app.security.SecureStorage;
import com.novavpn.app.vpn.WireGuardConfigBuilder;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\f\u001a\u00020\rJ\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fJ\u000e\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u0007R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/novavpn/app/viewmodel/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "secureStorage", "Lcom/novavpn/app/security/SecureStorage;", "(Lcom/novavpn/app/security/SecureStorage;)V", "_autoConnect", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "autoConnect", "Lkotlinx/coroutines/flow/StateFlow;", "getAutoConnect", "()Lkotlinx/coroutines/flow/StateFlow;", "clearCachedVpnConfig", "", "getConfigForOfficialApp", "", "setAutoConnect", "value", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.security.SecureStorage secureStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _autoConnect = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> autoConnect = null;
    
    @javax.inject.Inject()
    public SettingsViewModel(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.security.SecureStorage secureStorage) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getAutoConnect() {
        return null;
    }
    
    public final void setAutoConnect(boolean value) {
    }
    
    public final void clearCachedVpnConfig() {
    }
    
    /**
     * Returns current WireGuard config as wg-quick text for pasting into the official WireGuard app, or null if none.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getConfigForOfficialApp() {
        return null;
    }
}