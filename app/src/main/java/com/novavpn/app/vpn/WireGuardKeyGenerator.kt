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

    /**
     * Gets the public key from a private key.
     */
    fun getPublicKeyFromPrivate(privateKeyBase64: String): String {
        return try {
            // WireGuardKey constructor takes private key and derives public key
            val key = WireGuardKey(privateKeyBase64)
            key.base64PublicKey
        } catch (e: InvalidPrivateKeyException) {
            throw RuntimeException("Failed to get public key from private key", e)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get public key from private key", e)
        }
    }
}
