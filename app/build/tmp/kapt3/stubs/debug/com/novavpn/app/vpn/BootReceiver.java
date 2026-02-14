package com.novavpn.app.vpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.novavpn.app.NovaVpnApplication;
import com.novavpn.app.security.SecureStorage;
import dagger.hilt.android.EntryPointAccessors;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \n2\u00020\u0001:\u0002\t\nB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0016\u00a8\u0006\u000b"}, d2 = {"Lcom/novavpn/app/vpn/BootReceiver;", "Landroid/content/BroadcastReceiver;", "()V", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "BootReceiverEntryPoint", "Companion", "app_debug"})
public final class BootReceiver extends android.content.BroadcastReceiver {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_AUTO_CONNECT = "auto_connect";
    @org.jetbrains.annotations.NotNull()
    public static final com.novavpn.app.vpn.BootReceiver.Companion Companion = null;
    
    public BootReceiver() {
        super();
    }
    
    @java.lang.Override()
    public void onReceive(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&\u00a8\u0006\u0004"}, d2 = {"Lcom/novavpn/app/vpn/BootReceiver$BootReceiverEntryPoint;", "", "secureStorage", "Lcom/novavpn/app/security/SecureStorage;", "app_debug"})
    @dagger.hilt.EntryPoint()
    @dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
    public static abstract interface BootReceiverEntryPoint {
        
        @org.jetbrains.annotations.NotNull()
        public abstract com.novavpn.app.security.SecureStorage secureStorage();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/novavpn/app/vpn/BootReceiver$Companion;", "", "()V", "EXTRA_AUTO_CONNECT", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}