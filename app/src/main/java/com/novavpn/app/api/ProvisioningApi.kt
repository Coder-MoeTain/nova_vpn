package com.novavpn.app.api

import com.novavpn.app.BuildConfig
import com.novavpn.app.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches config from the provisioning server.
 * - POST {baseUrl}/provision (WireGuard) with { publicKey } → returns tunnel config.
 * - POST {baseUrl}/provision-openvpn → returns { "config": "<inline .ovpn>" } (legacy).
 */
@Singleton
class ProvisioningApi @Inject constructor() {

    private val baseUrl = BuildConfig.PROVISIONING_BASE_URL.trimEnd('/')

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = false
    }

    private val httpClient = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    /** WireGuard: register peer and get tunnel config. Retries once on timeout or 5xx. */
    suspend fun provisionWireGuard(request: WireGuardProvisionRequest): WireGuardConfigResponse {
        val url = "$baseUrl/provision"
        val bodyJson = json.encodeToString(request)
        var lastException: Exception? = null
        repeat(2) { attempt ->
            try {
                Logger.d("ProvisioningApi: POST $url (WireGuard) attempt ${attempt + 1}")
                val response = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(bodyJson)
                }
                if (response.status.value >= 400) {
                    val errorBody = response.bodyAsText()
                    Logger.w("ProvisioningApi: ${response.status.value} $errorBody")
                    throw Exception(parseServerError(errorBody))
                }
                val bodyText = response.bodyAsText()
                val body = json.decodeFromString<WireGuardConfigResponse>(bodyText)
                Logger.d("ProvisioningApi: got WireGuard config ${body.endpointHost}:${body.endpointPort}")
                return body
            } catch (e: Exception) {
                lastException = e
                val retryable = e.message?.lowercase()?.let {
                    it.contains("timeout") || it.contains("timed out") || it.contains("502") || it.contains("503") || it.contains("504")
                } ?: false
                if (!retryable || attempt == 1) throw e
                Logger.d("ProvisioningApi: retrying after $e")
            }
        }
        throw lastException ?: Exception("Provisioning failed")
    }

    suspend fun provisionOpenVpn(): String {
        val url = "$baseUrl/provision-openvpn"
        Logger.d("ProvisioningApi: POST $url")
        val response = httpClient.post(url)
        if (response.status.value >= 400) {
            val errorBody = response.bodyAsText()
            Logger.w("ProvisioningApi: ${response.status.value} $errorBody")
            val serverMessage = parseServerError(errorBody)
            throw Exception(serverMessage)
        }
        val body = response.body<OpenVpnProvisionResponse>()
        Logger.d("ProvisioningApi: got config (${body.config.length} chars)")
        return body.config
    }

    private fun parseServerError(body: String): String {
        return try {
            val err = json.decodeFromString<ServerErrorBody>(body)
            val detail = err.detail?.takeIf { it.isNotBlank() } ?: err.error?.takeIf { it.isNotBlank() }
            if (!detail.isNullOrBlank()) "Provisioning failed: $detail" else "Provisioning failed: $body"
        } catch (_: Exception) {
            "Provisioning failed (${body.take(200)})"
        }
    }
}

@kotlinx.serialization.Serializable
data class WireGuardProvisionRequest(
    val publicKey: String,
    val hostname: String? = null,
    val model: String? = null,
    val phoneNumber: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@kotlinx.serialization.Serializable
data class WireGuardConfigResponse(
    val endpointHost: String,
    val endpointPort: Int,
    val serverPublicKey: String,
    val clientAddress: String,
    val dns: String,
    val allowedIPs: String,
    val persistentKeepalive: Int,
    val presharedKey: String? = null
)

@kotlinx.serialization.Serializable
private data class OpenVpnProvisionResponse(val config: String)

@kotlinx.serialization.Serializable
private data class ServerErrorBody(val error: String? = null, val detail: String? = null)
