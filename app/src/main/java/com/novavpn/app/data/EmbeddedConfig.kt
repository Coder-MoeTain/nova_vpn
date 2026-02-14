package com.novavpn.app.data

/**
 * Embedded single-server config (MVP). Not shown to user.
 * Future: replace with provisioning API response.
 *
 * When [clientPrivateKey] is set, the app uses this key (e.g. from server-issued client config)
 * instead of generating one. When [presharedKey] is set, it is applied to the peer.
 */
data class EmbeddedConfig(
    val endpointHost: String,
    val endpointPort: Int,
    val serverPublicKey: String,
    val clientAddress: String,      // e.g. "10.66.66.2/32,fd42:42:42::2/128"
    val dns: String,                 // e.g. "1.1.1.1, 1.0.0.1"
    val allowedIPs: String,          // e.g. "0.0.0.0/0, ::/0"
    val persistentKeepalive: Int,
    /** If set, use this client private key (e.g. from server-issued config) instead of generating one. */
    val clientPrivateKey: String? = null,
    /** Optional preshared key for the peer (from [Peer] PresharedKey in server-issued config). */
    val presharedKey: String? = null
) {
    val endpoint: String get() = "$endpointHost:$endpointPort"
}
