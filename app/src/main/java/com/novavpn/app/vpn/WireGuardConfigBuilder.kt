package com.novavpn.app.vpn

import com.novavpn.app.api.WireGuardConfigResponse
import com.novavpn.app.util.Logger
import com.wireguard.config.BadConfigException
import com.wireguard.config.Config
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * Builds WireGuard Config from provisioning response and private key.
 * Uses wg-quick format string then Config.parse() so the Go backend gets the exact format it expects.
 */
object WireGuardConfigBuilder {

    @Throws(BadConfigException::class, IOException::class)
    fun build(
        privateKeyBase64: String,
        wg: WireGuardConfigResponse
    ): Config {
        val allowedIPs = "0.0.0.0/0"
        val dns = wg.dns.trim().ifBlank { "1.1.1.1" }
            .split(',').map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
            .ifBlank { "1.1.1.1" }
        val address = wg.clientAddress.split(',').map { it.trim() }.firstOrNull { it.contains(".") }
            ?: wg.clientAddress.trim()
        val host = wg.endpointHost.trim()
        val endpoint = if (host.contains(":")) "[$host]:${wg.endpointPort}" else "$host:${wg.endpointPort}"

        Logger.d("WireGuardConfigBuilder: endpoint=$endpoint allowedIPs=$allowedIPs dns=$dns")

        val wgQuick = buildWgQuickString(privateKeyBase64, wg)
        return Config.parse(ByteArrayInputStream(wgQuick.toByteArray(StandardCharsets.UTF_8)))
    }

    /** Same config as wg-quick text; use to export for testing in the official WireGuard app. */
    fun buildWgQuickString(privateKeyBase64: String, wg: WireGuardConfigResponse): String {
        val address = wg.clientAddress.split(',').map { it.trim() }.firstOrNull { it.contains(".") } ?: wg.clientAddress.trim()
        val host = wg.endpointHost.trim()
        val endpoint = if (host.contains(":")) "[$host]:${wg.endpointPort}" else "$host:${wg.endpointPort}"
        val dns = wg.dns.trim().ifBlank { "1.1.1.1" }.split(',').map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ").ifBlank { "1.1.1.1" }
        val allowedIPs = "0.0.0.0/0"
        val presharedLine = wg.presharedKey?.takeIf { it.isNotBlank() }?.let { "PresharedKey = $it" } ?: ""
        return buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = $privateKeyBase64")
            appendLine("Address = $address")
            appendLine("DNS = $dns")
            appendLine()
            appendLine("[Peer]")
            appendLine("PublicKey = ${wg.serverPublicKey}")
            appendLine("AllowedIPs = $allowedIPs")
            appendLine("Endpoint = $endpoint")
            appendLine("PersistentKeepalive = ${wg.persistentKeepalive}")
            if (presharedLine.isNotEmpty()) appendLine(presharedLine)
        }
    }
}
