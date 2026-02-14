package com.novavpn.app.security;

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u0007\u0018\u0000 \u00162\u00020\u0001:\u0001\u0016B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\t\u001a\u00020\nJ\u0006\u0010\u000b\u001a\u00020\fJ\u0006\u0010\r\u001a\u00020\fJ\u0006\u0010\u000e\u001a\u00020\fJ\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010J\u000e\u0010\u0011\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\fJ\u000e\u0010\u0013\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\fJ\u000e\u0010\u0014\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\fJ\u000e\u0010\u0015\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\u0010R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/novavpn/app/security/SecureStorage;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "masterKey", "Landroidx/security/crypto/MasterKey;", "prefs", "Landroid/content/SharedPreferences;", "clearPrivateKey", "", "getAlwaysOnGuidanceShown", "", "getAutoConnect", "getKillSwitchGuidanceShown", "getPrivateKey", "", "setAlwaysOnGuidanceShown", "value", "setAutoConnect", "setKillSwitchGuidanceShown", "setPrivateKey", "Companion", "app_debug"})
public final class SecureStorage {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.security.crypto.MasterKey masterKey = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "novavpn_secure";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_PRIVATE_KEY = "wg_private_key";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_AUTO_CONNECT = "auto_connect";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_ALWAYS_ON_GUIDANCE = "always_on_guidance_shown";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_KILL_SWITCH_GUIDANCE = "kill_switch_guidance_shown";
    @org.jetbrains.annotations.NotNull()
    public static final com.novavpn.app.security.SecureStorage.Companion Companion = null;
    
    @javax.inject.Inject()
    public SecureStorage(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPrivateKey() {
        return null;
    }
    
    public final void setPrivateKey(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final boolean getAutoConnect() {
        return false;
    }
    
    public final void setAutoConnect(boolean value) {
    }
    
    public final boolean getAlwaysOnGuidanceShown() {
        return false;
    }
    
    public final void setAlwaysOnGuidanceShown(boolean value) {
    }
    
    public final boolean getKillSwitchGuidanceShown() {
        return false;
    }
    
    public final void setKillSwitchGuidanceShown(boolean value) {
    }
    
    public final void clearPrivateKey() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/novavpn/app/security/SecureStorage$Companion;", "", "()V", "KEY_ALWAYS_ON_GUIDANCE", "", "KEY_AUTO_CONNECT", "KEY_KILL_SWITCH_GUIDANCE", "KEY_PRIVATE_KEY", "PREFS_NAME", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}