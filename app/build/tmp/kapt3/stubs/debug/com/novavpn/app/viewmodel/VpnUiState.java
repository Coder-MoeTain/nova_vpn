package com.novavpn.app.viewmodel;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.VpnService;
import android.Manifest;
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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0018\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B_\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\f\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010!\u001a\u00020\fH\u00c6\u0003J\t\u0010\"\u001a\u00020\fH\u00c6\u0003Jc\u0010#\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\fH\u00c6\u0001J\u0013\u0010$\u001a\u00020%2\b\u0010&\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\'\u001a\u00020(H\u00d6\u0001J\t\u0010)\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0013\u0010\n\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0012R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0012R\u0011\u0010\r\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0016R\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001a\u00a8\u0006*"}, d2 = {"Lcom/novavpn/app/viewmodel/VpnUiState;", "", "connectionState", "Lcom/novavpn/app/viewmodel/ConnectionState;", "lastError", "", "errorHint", "successMessage", "vpnPrepareIntent", "Landroid/content/Intent;", "connectionTestResult", "rxBytes", "", "txBytes", "(Lcom/novavpn/app/viewmodel/ConnectionState;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/content/Intent;Ljava/lang/String;JJ)V", "getConnectionState", "()Lcom/novavpn/app/viewmodel/ConnectionState;", "getConnectionTestResult", "()Ljava/lang/String;", "getErrorHint", "getLastError", "getRxBytes", "()J", "getSuccessMessage", "getTxBytes", "getVpnPrepareIntent", "()Landroid/content/Intent;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
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
    
    /**
     * When Connected: "Checking…", "Internet OK", or "No internet" (traffic not going through VPN).
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String connectionTestResult = null;
    
    /**
     * Traffic statistics: received and transmitted bytes
     */
    private final long rxBytes = 0L;
    private final long txBytes = 0L;
    
    public VpnUiState(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.viewmodel.ConnectionState connectionState, @org.jetbrains.annotations.Nullable()
    java.lang.String lastError, @org.jetbrains.annotations.Nullable()
    java.lang.String errorHint, @org.jetbrains.annotations.Nullable()
    java.lang.String successMessage, @org.jetbrains.annotations.Nullable()
    android.content.Intent vpnPrepareIntent, @org.jetbrains.annotations.Nullable()
    java.lang.String connectionTestResult, long rxBytes, long txBytes) {
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
    
    /**
     * When Connected: "Checking…", "Internet OK", or "No internet" (traffic not going through VPN).
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getConnectionTestResult() {
        return null;
    }
    
    /**
     * Traffic statistics: received and transmitted bytes
     */
    public final long getRxBytes() {
        return 0L;
    }
    
    public final long getTxBytes() {
        return 0L;
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    public final long component7() {
        return 0L;
    }
    
    public final long component8() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.viewmodel.VpnUiState copy(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.viewmodel.ConnectionState connectionState, @org.jetbrains.annotations.Nullable()
    java.lang.String lastError, @org.jetbrains.annotations.Nullable()
    java.lang.String errorHint, @org.jetbrains.annotations.Nullable()
    java.lang.String successMessage, @org.jetbrains.annotations.Nullable()
    android.content.Intent vpnPrepareIntent, @org.jetbrains.annotations.Nullable()
    java.lang.String connectionTestResult, long rxBytes, long txBytes) {
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