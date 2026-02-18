package com.novavpn.app.vpn;

import com.wireguard.android.backend.Tunnel;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tunnel instance for the WireGuard backend. Notifies the current listener when state changes.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0007\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\b\u001a\u00020\tH\u0016J\u0010\u0010\n\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\u0006H\u0016J\u001a\u0010\f\u001a\u00020\u00072\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005R \u0010\u0003\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/novavpn/app/vpn/NovaTunnel;", "Lcom/wireguard/android/backend/Tunnel;", "()V", "stateListener", "Ljava/util/concurrent/atomic/AtomicReference;", "Lkotlin/Function1;", "Lcom/wireguard/android/backend/Tunnel$State;", "", "getName", "", "onStateChange", "newState", "setStateListener", "listener", "Companion", "app_debug"})
public final class NovaTunnel implements com.wireguard.android.backend.Tunnel {
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicReference<kotlin.jvm.functions.Function1<com.wireguard.android.backend.Tunnel.State, kotlin.Unit>> stateListener = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TUNNEL_NAME = "novavpn";
    @org.jetbrains.annotations.NotNull()
    public static final com.novavpn.app.vpn.NovaTunnel.Companion Companion = null;
    
    public NovaTunnel() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getName() {
        return null;
    }
    
    public final void setStateListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.wireguard.android.backend.Tunnel.State, kotlin.Unit> listener) {
    }
    
    @java.lang.Override()
    public void onStateChange(@org.jetbrains.annotations.NotNull()
    com.wireguard.android.backend.Tunnel.State newState) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/novavpn/app/vpn/NovaTunnel$Companion;", "", "()V", "TUNNEL_NAME", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}