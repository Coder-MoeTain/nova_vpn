package com.novavpn.app.data;

/**
 * Embedded single-server config (MVP). Not shown to user.
 * Future: replace with provisioning API response.
 *
 * When [clientPrivateKey] is set, the app uses this key (e.g. from server-issued client config)
 * instead of generating one. When [presharedKey] is set, it is applied to the peer.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b \n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001BU\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\u0006\u0010\n\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\rJ\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0003H\u00c6\u0003J\t\u0010!\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\"\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010#\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003Jg\u0010$\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00052\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010%\u001a\u00020&2\b\u0010\'\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010(\u001a\u00020\u0005H\u00d6\u0001J\t\u0010)\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000fR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000fR\u0011\u0010\u0013\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u000fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0017R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u000f\u00a8\u0006*"}, d2 = {"Lcom/novavpn/app/data/EmbeddedConfig;", "", "endpointHost", "", "endpointPort", "", "serverPublicKey", "clientAddress", "dns", "allowedIPs", "persistentKeepalive", "clientPrivateKey", "presharedKey", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", "getAllowedIPs", "()Ljava/lang/String;", "getClientAddress", "getClientPrivateKey", "getDns", "endpoint", "getEndpoint", "getEndpointHost", "getEndpointPort", "()I", "getPersistentKeepalive", "getPresharedKey", "getServerPublicKey", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class EmbeddedConfig {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String endpointHost = null;
    private final int endpointPort = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String serverPublicKey = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String clientAddress = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String dns = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String allowedIPs = null;
    private final int persistentKeepalive = 0;
    
    /**
     * If set, use this client private key (e.g. from server-issued config) instead of generating one.
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String clientPrivateKey = null;
    
    /**
     * Optional preshared key for the peer (from [Peer] PresharedKey in server-issued config).
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String presharedKey = null;
    
    public EmbeddedConfig(@org.jetbrains.annotations.NotNull()
    java.lang.String endpointHost, int endpointPort, @org.jetbrains.annotations.NotNull()
    java.lang.String serverPublicKey, @org.jetbrains.annotations.NotNull()
    java.lang.String clientAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String dns, @org.jetbrains.annotations.NotNull()
    java.lang.String allowedIPs, int persistentKeepalive, @org.jetbrains.annotations.Nullable()
    java.lang.String clientPrivateKey, @org.jetbrains.annotations.Nullable()
    java.lang.String presharedKey) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEndpointHost() {
        return null;
    }
    
    public final int getEndpointPort() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getServerPublicKey() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getClientAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDns() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAllowedIPs() {
        return null;
    }
    
    public final int getPersistentKeepalive() {
        return 0;
    }
    
    /**
     * If set, use this client private key (e.g. from server-issued config) instead of generating one.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getClientPrivateKey() {
        return null;
    }
    
    /**
     * Optional preshared key for the peer (from [Peer] PresharedKey in server-issued config).
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPresharedKey() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEndpoint() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    public final int component7() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.novavpn.app.data.EmbeddedConfig copy(@org.jetbrains.annotations.NotNull()
    java.lang.String endpointHost, int endpointPort, @org.jetbrains.annotations.NotNull()
    java.lang.String serverPublicKey, @org.jetbrains.annotations.NotNull()
    java.lang.String clientAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String dns, @org.jetbrains.annotations.NotNull()
    java.lang.String allowedIPs, int persistentKeepalive, @org.jetbrains.annotations.Nullable()
    java.lang.String clientPrivateKey, @org.jetbrains.annotations.Nullable()
    java.lang.String presharedKey) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}