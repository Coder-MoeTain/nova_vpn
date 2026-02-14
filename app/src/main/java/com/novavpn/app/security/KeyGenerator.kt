package com.novavpn.app.security

import android.util.Base64
import java.security.SecureRandom

/**
 * Generates WireGuard-compatible private key (32 random bytes, base64).
 * Never log or expose private key.
 */
object KeyGenerator {

    private const val WG_KEY_LENGTH = 32
    private val random = SecureRandom()

    fun generatePrivateKeyBase64(): String {
        val bytes = ByteArray(WG_KEY_LENGTH)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
