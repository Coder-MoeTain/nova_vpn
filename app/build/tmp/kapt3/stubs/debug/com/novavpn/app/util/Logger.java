package com.novavpn.app.util;

import android.content.Context;
import timber.log.Timber;
import java.util.regex.Pattern;

/**
 * Safe logging with redaction of secrets. Never log private keys or full configs.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\b\u0003\n\u0002\u0010\u0003\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J+\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\b2\u0016\u0010\f\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u00010\u00010\r\"\u0004\u0018\u00010\u0001\u00a2\u0006\u0002\u0010\u000eJ+\u0010\u000f\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\b2\u0016\u0010\f\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u00010\u00010\r\"\u0004\u0018\u00010\u0001\u00a2\u0006\u0002\u0010\u000eJ5\u0010\u000f\u001a\u00020\n2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u000b\u001a\u00020\b2\u0016\u0010\f\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u00010\u00010\r\"\u0004\u0018\u00010\u0001\u00a2\u0006\u0002\u0010\u0012J+\u0010\u0013\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\b2\u0016\u0010\f\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u00010\u00010\r\"\u0004\u0018\u00010\u0001\u00a2\u0006\u0002\u0010\u000eJ\u000e\u0010\u0014\u001a\u00020\n2\u0006\u0010\u0015\u001a\u00020\u0016J\u0010\u0010\u0017\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\bH\u0002J+\u0010\u0018\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\b2\u0016\u0010\f\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u00010\u00010\r\"\u0004\u0018\u00010\u0001\u00a2\u0006\u0002\u0010\u000eJ5\u0010\u0018\u001a\u00020\n2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u000b\u001a\u00020\b2\u0016\u0010\f\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u00010\u00010\r\"\u0004\u0018\u00010\u0001\u00a2\u0006\u0002\u0010\u0012R(\u0010\u0003\u001a\u001c\u0012\u0018\u0012\u0016\u0012\f\u0012\n \u0007*\u0004\u0018\u00010\u00060\u0006\u0012\u0004\u0012\u00020\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/novavpn/app/util/Logger;", "", "()V", "REDACT_PATTERNS", "", "Lkotlin/Pair;", "Ljava/util/regex/Pattern;", "kotlin.jvm.PlatformType", "", "d", "", "message", "args", "", "(Ljava/lang/String;[Ljava/lang/Object;)V", "e", "t", "", "(Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V", "i", "init", "context", "Landroid/content/Context;", "sanitize", "w", "app_debug"})
public final class Logger {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<kotlin.Pair<java.util.regex.Pattern, java.lang.String>> REDACT_PATTERNS = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.novavpn.app.util.Logger INSTANCE = null;
    
    private Logger() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void d(@org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    java.lang.Object... args) {
    }
    
    public final void i(@org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    java.lang.Object... args) {
    }
    
    public final void w(@org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    java.lang.Object... args) {
    }
    
    public final void w(@org.jetbrains.annotations.Nullable()
    java.lang.Throwable t, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    java.lang.Object... args) {
    }
    
    public final void e(@org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    java.lang.Object... args) {
    }
    
    public final void e(@org.jetbrains.annotations.Nullable()
    java.lang.Throwable t, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    java.lang.Object... args) {
    }
    
    private final java.lang.String sanitize(java.lang.String message) {
        return null;
    }
}