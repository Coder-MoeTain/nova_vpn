package com.novavpn.app.vpn

import net.moznion.wireguard.keytool.InvalidPrivateKeyException
import net.moznion.wireguard.keytool.WireGuardKey

/**
 * Generates WireGuard X25519 key pairs in the format expected by the tunnel and server.
 */
object WireGuardKeyGenerator {

    /**
     * Generates a new key pair. Returns Pair(privateKeyBase64, publicKeyBase64).
     */
    fun generate(): Pair<String, String> {
        return try {
            val key = WireGuardKey.generate()
            key.base64PrivateKey to key.base64PublicKey
        } catch (e: InvalidPrivateKeyException) {
            throw RuntimeException("WireGuard key generation failed", e)
        }
    }
}
