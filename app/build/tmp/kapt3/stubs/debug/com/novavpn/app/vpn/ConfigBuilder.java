package com.novavpn.app.vpn;

import com.novavpn.app.util.Logger;
import com.wireguard.config.Config;
import com.wireguard.config.Interface;
import com.wireguard.config.Peer;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import java.net.InetAddress;

/**
 * Builds WireGuard Config internally. Never exposed to UI or logs.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b\u00a8\u0006\t"}, d2 = {"Lcom/novavpn/app/vpn/ConfigBuilder;", "", "()V", "build", "Lcom/wireguard/config/Config;", "embedded", "Lcom/novavpn/app/data/EmbeddedConfig;", "privateKeyBase64", "", "app_debug"})
public final class ConfigBuilder {
    @org.jetbrains.annotations.NotNull()
    public static final com.novavpn.app.vpn.ConfigBuilder INSTANCE = null;
    
    private ConfigBuilder() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.wireguard.config.Config build(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.data.EmbeddedConfig embedded, @org.jetbrains.annotations.NotNull()
    java.lang.String privateKeyBase64) {
        return null;
    }
}