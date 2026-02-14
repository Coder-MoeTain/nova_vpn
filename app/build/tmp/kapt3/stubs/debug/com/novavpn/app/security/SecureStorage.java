package com.novavpn.app.security;

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\n\b\u0007\u0018\u0000 \u001a2\u00020\u0001:\u0001\u001aB\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\t\u001a\u00020\nJ\u0006\u0010\u000b\u001a\u00020\nJ\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\rJ\u0006\u0010\u000f\u001a\u00020\rJ\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011J\b\u0010\u0012\u001a\u0004\u0018\u00010\u0011J\u000e\u0010\u0013\u001a\u00020\n2\u0006\u0010\u0014\u001a\u00020\rJ\u000e\u0010\u0015\u001a\u00020\n2\u0006\u0010\u0014\u001a\u00020\rJ\u000e\u0010\u0016\u001a\u00020\n2\u0006\u0010\u0014\u001a\u00020\rJ\u000e\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0014\u001a\u00020\u0011J\u000e\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/novavpn/app/security/SecureStorage;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "masterKey", "Landroidx/security/crypto/MasterKey;", "prefs", "Landroid/content/SharedPreferences;", "clearPrivateKey", "", "clearProvisionedConfig", "getAlwaysOnGuidanceShown", "", "getAutoConnect", "getKillSwitchGuidanceShown", "getPrivateKey", "", "getProvisionedConfigJson", "setAlwaysOnGuidanceShown", "value", "setAutoConnect", "setKillSwitchGuidanceShown", "setPrivateKey", "setProvisionedConfigJson", "json", "Companion", "app_debug"})
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
    private static final java.lang.String KEY_PROVISIONED_CONFIG = "provisioned_config";
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getProvisionedConfigJson() {
        return null;
    }
    
    public final void setProvisionedConfigJson(@org.jetbrains.annotations.NotNull()
    java.lang.String json) {
    }
    
    public final void clearProvisionedConfig() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/novavpn/app/security/SecureStorage$Companion;", "", "()V", "KEY_ALWAYS_ON_GUIDANCE", "", "KEY_AUTO_CONNECT", "KEY_KILL_SWITCH_GUIDANCE", "KEY_PRIVATE_KEY", "KEY_PROVISIONED_CONFIG", "PREFS_NAME", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}