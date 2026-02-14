package com.novavpn.app.api

import com.novavpn.app.BuildConfig
import com.novavpn.app.data.ProvisioningRequest
import com.novavpn.app.data.ProvisioningResponse
import com.novavpn.app.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calls your backend to register this device and get its WireGuard config.
 * Backend should: add a [Peer] with this public key and a unique AllowedIPs, then return the config.
 *
 * Contract:
 * - POST {baseUrl}/provision
 * - Body: { "publicKey": "<base64>" }
 * - Response: ProvisioningResponse JSON
 */
@Singleton
class ProvisioningApi @Inject constructor() {

    private val baseUrl = BuildConfig.PROVISIONING_BASE_URL
        .trimEnd('/')

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun provision(publicKey: String): ProvisioningResponse {
        val url = "$baseUrl/provision"
        Logger.d("ProvisioningApi: POST $url")
        val response = httpClient.post(url) {
            setBody(ProvisioningRequest(publicKey = publicKey))
        }
        val body: ProvisioningResponse = response.body()
        Logger.d("ProvisioningApi: got config for ${body.clientAddress}")
        return body
    }
}
