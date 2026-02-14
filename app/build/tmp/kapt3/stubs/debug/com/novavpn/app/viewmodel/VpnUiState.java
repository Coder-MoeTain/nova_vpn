package com.novavpn.app.viewmodel;

import androidx.lifecycle.ViewModel;
import com.novavpn.app.util.Logger;
import com.novavpn.app.vpn.WireGuardManager;
import com.wireguard.android.backend.Statistics;
import com.wireguard.android.backend.Tunnel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.ktor.client.engine.android.Android;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001BE\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\b\b\u0002\u0010\n\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u0017\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010\u0018\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003JI\u0010\u001b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001J\t\u0010!\u001a\u00020\u0007H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0013\u0010\b\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000fR\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000f\u00a8\u0006\""}, d2 = {"Lcom/novavpn/app/viewmodel/VpnUiState;", "", "connectionState", "Lcom/novavpn/app/viewmodel/ConnectionState;", "connectionTimeSeconds", "", "publicIp", "", "lastError", "statsRxBytes", "statsTxBytes", "(Lcom/novavpn/app/viewmodel/ConnectionState;JLjava/lang/String;Ljava/lang/String;JJ)V", "getConnectionState", "()Lcom/novavpn/app/viewmodel/ConnectionState;", "getConnectionTimeSeconds", "()J", "getLastError", "()Ljava/lang/String;", "getPublicIp", "getStatsRxBytes", "getStatsTxBytes", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class VpnUiState {
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.viewmodel.ConnectionState connectionState = null;
    private final long connectionTimeSeconds = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String publicIp = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String lastError = null;
    private final long statsRxBytes = 0L;
    private final long statsTxBytes = 0L;
    
    public VpnUiState(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.viewmodel.ConnectionState connectionState, long connectionTimeSeconds, @org.jetbrains.annotations.Nullable()
    java.lang.String publicIp, @org.jetbrains.annotations.Nullable()
    java.lang.String lastError, long statsRxBytes, long statsTxBytes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.viewmodel.ConnectionState getConnectionState() {
        return null;
    }
    
    public final long getConnectionTimeSeconds() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPublicIp() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLastError() {
        return null;
    }
    
    public final long getStatsRxBytes() {
        return 0L;
    }
    
    public final long getStatsTxBytes() {
        return 0L;
    }
    
    public VpnUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.viewmodel.ConnectionState component1() {
        return null;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    public final long component5() {
        return 0L;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.viewmodel.VpnUiState copy(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.viewmodel.ConnectionState connectionState, long connectionTimeSeconds, @org.jetbrains.annotations.Nullable()
    java.lang.String publicIp, @org.jetbrains.annotations.Nullable()
    java.lang.String lastError, long statsRxBytes, long statsTxBytes) {
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