package com.novavpn.app.vpn;

import android.content.Context;
import com.novavpn.app.data.EmbeddedConfig;
import dagger.hilt.android.qualifiers.ApplicationContext;
import com.novavpn.app.security.KeyGenerator;
import com.novavpn.app.security.SecureStorage;
import com.novavpn.app.util.Logger;
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;
import com.wireguard.config.Interface;
import kotlinx.coroutines.flow.SharedFlow;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u001b\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0006\u0010\u001e\u001a\u00020\u001fJ\u000e\u0010 \u001a\u00020!H\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0010\u0010\"\u001a\u0004\u0018\u00010!H\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0006\u0010#\u001a\u00020\tJ\b\u0010$\u001a\u0004\u0018\u00010\u0001J\u0006\u0010%\u001a\u00020&J\u000e\u0010\'\u001a\u00020&2\u0006\u0010(\u001a\u00020\u001cR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0012\u001a\u00020\u00138BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0016\u0010\u000f\u001a\u0004\b\u0014\u0010\u0015R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\t0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001a\u00a8\u0006)"}, d2 = {"Lcom/novavpn/app/vpn/WireGuardManager;", "", "context", "Landroid/content/Context;", "secureStorage", "Lcom/novavpn/app/security/SecureStorage;", "(Landroid/content/Context;Lcom/novavpn/app/security/SecureStorage;)V", "_tunnelState", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/wireguard/android/backend/Tunnel$State;", "backend", "Lcom/wireguard/android/backend/Backend;", "getBackend", "()Lcom/wireguard/android/backend/Backend;", "backend$delegate", "Lkotlin/Lazy;", "mutex", "Lkotlinx/coroutines/sync/Mutex;", "tunnel", "Lcom/novavpn/app/vpn/NovaTunnel;", "getTunnel", "()Lcom/novavpn/app/vpn/NovaTunnel;", "tunnel$delegate", "tunnelState", "Lkotlinx/coroutines/flow/SharedFlow;", "getTunnelState", "()Lkotlinx/coroutines/flow/SharedFlow;", "buildConfig", "Lcom/wireguard/config/Config;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getEmbeddedConfig", "Lcom/novavpn/app/data/EmbeddedConfig;", "getOrCreatePrivateKey", "", "getPublicKeyBase64", "getState", "getStatistics", "setStateDown", "", "setStateUp", "config", "app_debug"})
public final class WireGuardManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.security.SecureStorage secureStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.sync.Mutex mutex = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy backend$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy tunnel$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.wireguard.android.backend.Tunnel.State> _tunnelState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.wireguard.android.backend.Tunnel.State> tunnelState = null;
    
    @javax.inject.Inject()
    public WireGuardManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.novavpn.app.security.SecureStorage secureStorage) {
        super();
    }
    
    private final com.wireguard.android.backend.Backend getBackend() {
        return null;
    }
    
    private final com.novavpn.app.vpn.NovaTunnel getTunnel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.wireguard.android.backend.Tunnel.State> getTunnelState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getOrCreatePrivateKey(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Embedded config for server 194.31.53.236; uses fixed client key and preshared key from config.
     */
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.data.EmbeddedConfig getEmbeddedConfig() {
        return null;
    }
    
    /**
     * Builds tunnel config from embedded config only (no provisioning).
     * Uses single private key from embedded config.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object buildConfig(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.wireguard.config.Config> $completion) {
        return null;
    }
    
    /**
     * Returns the public key (base64) for the current tunnel identity (embedded config key when set).
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getPublicKeyBase64(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    public final void setStateUp(@org.jetbrains.annotations.NotNull()
    com.wireguard.config.Config config) {
    }
    
    public final void setStateDown() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.wireguard.android.backend.Tunnel.State getState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getStatistics() {
        return null;
    }
}