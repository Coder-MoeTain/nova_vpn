package com.novavpn.app.security;

import android.util.Base64;
import java.security.SecureRandom;

/**
 * Generates WireGuard-compatible private key (32 random bytes, base64).
 * Never log or expose private key.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/novavpn/app/security/KeyGenerator;", "", "()V", "WG_KEY_LENGTH", "", "random", "Ljava/security/SecureRandom;", "generatePrivateKeyBase64", "", "app_debug"})
public final class KeyGenerator {
    private static final int WG_KEY_LENGTH = 32;
    @org.jetbrains.annotations.NotNull()
    private static final java.security.SecureRandom random = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.novavpn.app.security.KeyGenerator INSTANCE = null;
    
    private KeyGenerator() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generatePrivateKeyBase64() {
        return null;
    }
}