-keep class com.novavpn.app.vpn.** { *; }
-keep class com.novavpn.app.api.** { *; }
-keep class com.novavpn.app.BuildConfig { *; }
# WireGuard tunnel library
-keep class com.wireguard.android.backend.** { *; }
-keep class com.wireguard.config.** { *; }
-keep class com.wireguard.crypto.** { *; }

# Fix R8 missing classes errors
-keep class com.google.errorprone.annotations.** { *; }
-dontwarn com.google.errorprone.annotations.**

# SLF4J logger (used by WireGuard library)
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**
-keep class org.slf4j.impl.** { *; }
-dontwarn org.slf4j.impl.**

# Keep all annotation classes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
