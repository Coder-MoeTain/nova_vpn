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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B?\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0014\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u0015\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u0016\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u0017\u001a\u0004\u0018\u00010\tH\u00c6\u0003JC\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\tH\u00c6\u0001J\u0013\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001c\u001a\u00020\u001dH\u00d6\u0001J\t\u0010\u001e\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000eR\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001f"}, d2 = {"Lcom/novavpn/app/viewmodel/VpnUiState;", "", "connectionState", "Lcom/novavpn/app/viewmodel/ConnectionState;", "lastError", "", "errorHint", "successMessage", "vpnPrepareIntent", "Landroid/content/Intent;", "(Lcom/novavpn/app/viewmodel/ConnectionState;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/content/Intent;)V", "getConnectionState", "()Lcom/novavpn/app/viewmodel/ConnectionState;", "getErrorHint", "()Ljava/lang/String;", "getLastError", "getSuccessMessage", "getVpnPrepareIntent", "()Landroid/content/Intent;", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class VpnUiState {
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.viewmodel.ConnectionState connectionState = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String lastError = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorHint = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String successMessage = null;
    
    /**
     * Non-null when VPN permission is needed; Activity should launch this intent.
     */
    @org.jetbrains.annotations.Nullable()
    private final android.content.Intent vpnPrepareIntent = null;
    
    public VpnUiState(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.viewmodel.ConnectionState connectionState, @org.jetbrains.annotations.Nullable()
    java.lang.String lastError, @org.jetbrains.annotations.Nullable()
    java.lang.String errorHint, @org.jetbrains.annotations.Nullable()
    java.lang.String successMessage, @org.jetbrains.annotations.Nullable()
    android.content.Intent vpnPrepareIntent) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.viewmodel.ConnectionState getConnectionState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLastError() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorHint() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSuccessMessage() {
        return null;
    }
    
    /**
     * Non-null when VPN permission is needed; Activity should launch this intent.
     */
    @org.jetbrains.annotations.Nullable()
    public final android.content.Intent getVpnPrepareIntent() {
        return null;
    }
    
    public VpnUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.viewmodel.ConnectionState component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.content.Intent component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.viewmodel.VpnUiState copy(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.viewmodel.ConnectionState connectionState, @org.jetbrains.annotations.Nullable()
    java.lang.String lastError, @org.jetbrains.annotations.Nullable()
    java.lang.String errorHint, @org.jetbrains.annotations.Nullable()
    java.lang.String successMessage, @org.jetbrains.annotations.Nullable()
    android.content.Intent vpnPrepareIntent) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}