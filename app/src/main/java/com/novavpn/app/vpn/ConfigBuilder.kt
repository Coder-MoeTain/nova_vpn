package com.novavpn.app.vpn

import com.novavpn.app.util.Logger
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import java.net.InetAddress

/**
 * Builds WireGuard Config internally. Never exposed to UI or logs.
 */
object ConfigBuilder {

    fun build(
        embedded: com.novavpn.app.data.EmbeddedConfig,
        privateKeyBase64: String
    ): Config {
        Logger.d("ConfigBuilder: building interface")
        val ifaceBuilder = Interface.Builder()
        try {
            ifaceBuilder.parsePrivateKey(privateKeyBase64)
        } catch (e: Exception) {
            Logger.e(e, "ConfigBuilder: parsePrivateKey failed")
            throw IllegalArgumentException("Invalid private key format", e)
        }
        try {
            for (addr in embedded.clientAddress.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
                ifaceBuilder.addAddress(InetNetwork.parse(addr))
            }
            for (dnsStr in embedded.dns.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
                ifaceBuilder.addDnsServer(InetAddress.getByName(dnsStr))
            }
        } catch (e: Exception) {
            Logger.e(e, "ConfigBuilder: addAddress/addDnsServer failed")
            throw e
        }
        val iface = ifaceBuilder.build()
        Logger.d("ConfigBuilder: building peer")

        val peerBuilder = Peer.Builder()
        try {
            peerBuilder.parsePublicKey(embedded.serverPublicKey)
        } catch (e: Exception) {
            Logger.e(e, "ConfigBuilder: parsePublicKey failed")
            throw IllegalArgumentException("Invalid server public key", e)
        }
        try {
            peerBuilder.setEndpoint(InetEndpoint.parse(embedded.endpoint))
            for (allowed in embedded.allowedIPs.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
                peerBuilder.addAllowedIp(InetNetwork.parse(allowed))
            }
            peerBuilder.setPersistentKeepalive(embedded.persistentKeepalive)
            embedded.presharedKey?.let { psk ->
                peerBuilder.parsePreSharedKey(psk)
            }
        } catch (e: Exception) {
            Logger.e(e, "ConfigBuilder: endpoint/allowedIPs/presharedKey failed")
            throw e
        }
        val peer = peerBuilder.build()
        Logger.d("ConfigBuilder: building Config")
        return Config.Builder()
            .setInterface(iface)
            .addPeer(peer)
            .build()
    }
}
