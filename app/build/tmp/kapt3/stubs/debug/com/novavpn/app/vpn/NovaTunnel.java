package com.novavpn.app.vpn;

import android.os.Handler;
import android.os.Looper;
import com.novavpn.app.util.Logger;
import com.wireguard.android.backend.Tunnel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Single tunnel instance for NovaVPN. State changes reported to [onStateChange].
 * Callback is posted to the main queue to avoid re-entrancy and StackOverflowError.
 * Rapid backend callbacks (e.g. UP, DOWN, UP, DOWN in the same moment) are coalesced:
 * only the latest state is delivered once per batch.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00122\u00020\u0001:\u0001\u0012B\u0019\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u000f\u001a\u00020\u0010H\u0016J\u0010\u0010\u0002\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\u0004H\u0016R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/novavpn/app/vpn/NovaTunnel;", "Lcom/wireguard/android/backend/Tunnel;", "onStateChange", "Lkotlin/Function1;", "Lcom/wireguard/android/backend/Tunnel$State;", "", "(Lkotlin/jvm/functions/Function1;)V", "coalescingRunnable", "Ljava/lang/Runnable;", "mainHandler", "Landroid/os/Handler;", "pendingState", "Ljava/util/concurrent/atomic/AtomicReference;", "runnablePosted", "Ljava/util/concurrent/atomic/AtomicBoolean;", "getName", "", "newState", "Companion", "app_debug"})
public final class NovaTunnel implements com.wireguard.android.backend.Tunnel {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.wireguard.android.backend.Tunnel.State, kotlin.Unit> onStateChange = null;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler mainHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicReference<com.wireguard.android.backend.Tunnel.State> pendingState = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicBoolean runnablePosted = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable coalescingRunnable = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TUNNEL_NAME = "novavpn";
    @org.jetbrains.annotations.NotNull()
    public static final com.novavpn.app.vpn.NovaTunnel.Companion Companion = null;
    
    public NovaTunnel(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.wireguard.android.backend.Tunnel.State, kotlin.Unit> onStateChange) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getName() {
        return null;
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