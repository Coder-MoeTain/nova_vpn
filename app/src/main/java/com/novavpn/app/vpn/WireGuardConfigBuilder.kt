package com.novavpn.app.vpn

import com.wireguard.config.BadConfigException
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.novavpn.app.api.WireGuardConfigResponse

/**
 * Builds WireGuard Config from provisioning response and private key.
 */
object WireGuardConfigBuilder {

    @Throws(BadConfigException::class)
    fun build(
        privateKeyBase64: String,
        wg: WireGuardConfigResponse
    ): Config {
        val iface = Interface.Builder()
            .parseAddresses(wg.clientAddress)
            .parseDnsServers(wg.dns)
            .parsePrivateKey(privateKeyBase64)
            .build()

        val endpoint = "${wg.endpointHost}:${wg.endpointPort}"
        val peerBuilder = Peer.Builder()
            .parsePublicKey(wg.serverPublicKey)
            .parseEndpoint(endpoint)
            .parseAllowedIPs(wg.allowedIPs)
            .setPersistentKeepalive(wg.persistentKeepalive)
        wg.presharedKey?.takeIf { it.isNotBlank() }?.let { peerBuilder.parsePreSharedKey(it) }
        val peer = peerBuilder.build()

        return Config.Builder()
            .setInterface(iface)
            .addPeer(peer)
            .build()
    }
}
