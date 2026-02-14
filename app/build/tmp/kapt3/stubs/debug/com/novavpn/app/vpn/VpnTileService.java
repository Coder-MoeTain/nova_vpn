package com.novavpn.app.vpn;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import com.novavpn.app.util.Logger;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.Dispatchers;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u000b\u001a\u00020\fH\u0016J\b\u0010\r\u001a\u00020\fH\u0016J\u0010\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0010H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\n\u00a8\u0006\u0011"}, d2 = {"Lcom/novavpn/app/vpn/VpnTileService;", "Landroid/service/quicksettings/TileService;", "()V", "scope", "Lkotlinx/coroutines/CoroutineScope;", "wireGuardManager", "Lcom/novavpn/app/vpn/WireGuardManager;", "getWireGuardManager", "()Lcom/novavpn/app/vpn/WireGuardManager;", "setWireGuardManager", "(Lcom/novavpn/app/vpn/WireGuardManager;)V", "onClick", "", "onStartListening", "updateTile", "connected", "", "app_debug"})
public final class VpnTileService extends android.service.quicksettings.TileService {
    @javax.inject.Inject()
    public com.novavpn.app.vpn.WireGuardManager wireGuardManager;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    
    public VpnTileService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.vpn.WireGuardManager getWireGuardManager() {
        return null;
    }
    
    public final void setWireGuardManager(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.vpn.WireGuardManager p0) {
    }
    
    @java.lang.Override()
    public void onStartListening() {
    }
    
    @java.lang.Override()
    public void onClick() {
    }
    
    private final void updateTile(boolean connected) {
    }
}