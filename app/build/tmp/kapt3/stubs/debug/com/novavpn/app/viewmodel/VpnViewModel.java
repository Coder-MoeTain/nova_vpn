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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0015\u001a\u00020\u0016J\u0006\u0010\u0017\u001a\u00020\u0016J\u0006\u0010\u0018\u001a\u00020\u0016J\u000e\u0010\u0019\u001a\u00020\u0016H\u0082@\u00a2\u0006\u0002\u0010\u001aJ\u0006\u0010\u001b\u001a\u00020\u0016J\b\u0010\u001c\u001a\u00020\u0016H\u0002J\b\u0010\u001d\u001a\u00020\u0016H\u0002J\u000e\u0010\u001e\u001a\u00020\u0016H\u0082@\u00a2\u0006\u0002\u0010\u001aJ\b\u0010\u001f\u001a\u00020\u0016H\u0002J\u001e\u0010 \u001a\u0010\u0012\u0004\u0012\u00020\"\u0012\u0006\u0012\u0004\u0018\u00010\"0!2\u0006\u0010#\u001a\u00020$H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00070\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/novavpn/app/viewmodel/VpnViewModel;", "Landroidx/lifecycle/ViewModel;", "wireGuardManager", "Lcom/novavpn/app/vpn/WireGuardManager;", "(Lcom/novavpn/app/vpn/WireGuardManager;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/novavpn/app/viewmodel/VpnUiState;", "connectingTimeoutMs", "", "connectingTimeoutSeconds", "", "connectionStartTime", "httpClient", "Lio/ktor/client/HttpClient;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "timerJob", "Lkotlinx/coroutines/Job;", "clearError", "", "connect", "disconnect", "fetchPublicIp", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshPublicIp", "startTimer", "stopTimer", "syncStateFromBackend", "updateStats", "userFriendlyMessageAndHint", "Lkotlin/Pair;", "", "e", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class VpnViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.vpn.WireGuardManager wireGuardManager = null;
    @org.jetbrains.annotations.NotNull()
    private final io.ktor.client.HttpClient httpClient = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.novavpn.app.viewmodel.VpnUiState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.novavpn.app.viewmodel.VpnUiState> state = null;
    private long connectionStartTime = 0L;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job timerJob;
    private final long connectingTimeoutMs = 45000L;
    private final int connectingTimeoutSeconds = 0;
    
    @javax.inject.Inject()
    public VpnViewModel(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.vpn.WireGuardManager wireGuardManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.novavpn.app.viewmodel.VpnUiState> getState() {
        return null;
    }
    
    private final java.lang.Object syncStateFromBackend(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void startTimer() {
    }
    
    private final void stopTimer() {
    }
    
    private final void updateStats() {
    }
    
    public final void connect() {
    }
    
    public final void disconnect() {
    }
    
    public final void clearError() {
    }
    
    public final void refreshPublicIp() {
    }
    
    private final java.lang.Object fetchPublicIp(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final kotlin.Pair<java.lang.String, java.lang.String> userFriendlyMessageAndHint(java.lang.Throwable e) {
        return null;
    }
}