package com.novavpn.app.vpn

import android.content.Context
import com.novavpn.app.data.EmbeddedConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import com.novavpn.app.security.KeyGenerator
import com.novavpn.app.security.SecureStorage
import com.novavpn.app.util.Logger
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.Interface
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WireGuardManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage
) {
    private val mutex = Mutex()
    private val backend: Backend by lazy { GoBackend(context) }
    private val tunnel: NovaTunnel by lazy {
        NovaTunnel { state ->
            _tunnelState.tryEmit(state)
        }
    }

    private val _tunnelState = MutableSharedFlow<Tunnel.State>(replay = 1, extraBufferCapacity = 2)
    val tunnelState: SharedFlow<Tunnel.State> = _tunnelState

    suspend fun getOrCreatePrivateKey(): String = mutex.withLock {
        Logger.d("getOrCreatePrivateKey: reading from storage")
        secureStorage.getPrivateKey() ?: run {
            Logger.d("getOrCreatePrivateKey: generating new key")
            val key = KeyGenerator.generatePrivateKeyBase64()
            secureStorage.setPrivateKey(key)
            Logger.d("getOrCreatePrivateKey: new key stored")
            key
        }
    }

    /**
     * Embedded config for server 194.31.53.236; uses fixed client key and preshared key from config.
     */
    fun getEmbeddedConfig(): EmbeddedConfig = EmbeddedConfig(
        endpointHost = "76.13.189.118",
        endpointPort = 64288,
        serverPublicKey = "PBoRD+7wiog1JQmMCmraHA+DoWLMpdlPxD/LcHmeLwo=",
        clientAddress = "10.66.66.2/32,fd42:42:42::2/128",
        dns = "1.1.1.1, 1.0.0.1",
        allowedIPs = "0.0.0.0/0, ::/0",
        persistentKeepalive = 25,
        clientPrivateKey = "YNye/ELBtadYI2sDDEl/9wpJch8OOW8C1pPrUrKNl3Q=",
        presharedKey = "PqQ0uPVXrHlN/tD0N6ZFedm0Tju04Ue4arylyyYiYLg="
    )

    /**
     * Builds tunnel config from embedded config only (no provisioning).
     * Uses single private key from embedded config.
     */
    suspend fun buildConfig(): Config = mutex.withLock {
        val embedded = getEmbeddedConfig()
        val keyToUse = embedded.clientPrivateKey ?: getOrCreatePrivateKey()
        Logger.d("buildConfig: endpoint=${embedded.endpointHost}:${embedded.endpointPort} (embedded only)")
        ConfigBuilder.build(embedded, keyToUse)
    }

    /**
     * Returns the public key (base64) for the current tunnel identity (embedded config key when set).
     */
    suspend fun getPublicKeyBase64(): String? = mutex.withLock {
        val embedded = getEmbeddedConfig()
        val privateKey = embedded.clientPrivateKey ?: secureStorage.getPrivateKey() ?: return@withLock null
        return@withLock try {
            val iface = Interface.Builder().parsePrivateKey(privateKey).build()
            iface.keyPair.publicKey.toBase64()
        } catch (e: Exception) {
            Logger.e(e, "getPublicKeyBase64 failed")
            null
        }
    }

    fun setStateUp(config: Config) {
        Logger.d("setStateUp: calling backend.setState UP")
        try {
            backend.setState(tunnel, Tunnel.State.UP, config)
            Logger.d("setStateUp: backend.setState UP returned")
        } catch (e: Exception) {
            Logger.e(e, "setStateUp failed: ${e.message}")
            throw e
        }
    }

    fun setStateDown() {
        try {
            backend.setState(tunnel, Tunnel.State.DOWN, null)
        } catch (e: Exception) {
            Logger.e(e, "setState DOWN failed")
        }
    }

    fun getState(): Tunnel.State = backend.getState(tunnel)

    fun getStatistics(): Any? {
        return try {
            backend.getStatistics(tunnel)
        } catch (e: Exception) {
            null
        }
    }
}
