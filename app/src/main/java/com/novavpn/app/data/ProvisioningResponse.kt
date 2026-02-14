package com.novavpn.app.data

import kotlinx.serialization.Serializable

/**
 * Response from the provisioning API. Backend adds this device as a [Peer] and returns its config.
 */
@Serializable
data class ProvisioningResponse(
    val endpointHost: String,
    val endpointPort: Int,
    val serverPublicKey: String,
    val clientAddress: String,
    val dns: String,
    val allowedIPs: String,
    val persistentKeepalive: Int = 25,
    val presharedKey: String? = null
) {
    fun toEmbeddedConfig(): EmbeddedConfig = EmbeddedConfig(
        endpointHost = endpointHost,
        endpointPort = endpointPort,
        serverPublicKey = serverPublicKey,
        clientAddress = clientAddress,
        dns = dns,
        allowedIPs = allowedIPs,
        persistentKeepalive = persistentKeepalive,
        clientPrivateKey = null,
        presharedKey = presharedKey
    )
}
