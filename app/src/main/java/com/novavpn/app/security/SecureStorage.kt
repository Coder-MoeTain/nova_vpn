package com.novavpn.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getAutoConnect(): Boolean = prefs.getBoolean(KEY_AUTO_CONNECT, false)

    fun setAutoConnect(value: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT, value).apply()
    }

    fun getAlwaysOnGuidanceShown(): Boolean = prefs.getBoolean(KEY_ALWAYS_ON_GUIDANCE, false)

    fun setAlwaysOnGuidanceShown(value: Boolean) {
        prefs.edit().putBoolean(KEY_ALWAYS_ON_GUIDANCE, value).apply()
    }

    fun getKillSwitchGuidanceShown(): Boolean = prefs.getBoolean(KEY_KILL_SWITCH_GUIDANCE, false)

    fun setKillSwitchGuidanceShown(value: Boolean) {
        prefs.edit().putBoolean(KEY_KILL_SWITCH_GUIDANCE, value).apply()
    }

    /** User-defined device label (e.g. "John's Phone") sent to server to identify this device. */
    fun getDeviceLabel(): String? = prefs.getString(KEY_DEVICE_LABEL, null)?.takeIf { it.isNotBlank() }

    fun setDeviceLabel(value: String?) {
        prefs.edit().putString(KEY_DEVICE_LABEL, value?.trim()?.take(128) ?: "").apply()
    }

    fun getOpenVpnConfigCache(): String? = prefs.getString(KEY_OPENVPN_CONFIG_CACHE, null)

    fun setOpenVpnConfigCache(config: String) {
        prefs.edit().putString(KEY_OPENVPN_CONFIG_CACHE, config).apply()
    }

    fun clearOpenVpnConfigCache() {
        prefs.edit().remove(KEY_OPENVPN_CONFIG_CACHE).apply()
    }

    fun getWireGuardPrivateKeyBase64(): String? = prefs.getString(KEY_WG_PRIVATE_KEY, null)

    fun setWireGuardPrivateKeyBase64(key: String) {
        prefs.edit().putString(KEY_WG_PRIVATE_KEY, key).apply()
    }

    fun getWireGuardConfigJson(): String? = prefs.getString(KEY_WG_CONFIG_JSON, null)

    fun setWireGuardConfigJson(json: String) {
        prefs.edit().putString(KEY_WG_CONFIG_JSON, json).apply()
    }

    fun clearWireGuardConfig() {
        prefs.edit().remove(KEY_WG_CONFIG_JSON).apply()
    }

    /** Clears WireGuard config and private key so the next connect will provision a new peer. */
    fun clearAllWireGuard() {
        prefs.edit()
            .remove(KEY_WG_CONFIG_JSON)
            .remove(KEY_WG_PRIVATE_KEY)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "novavpn_secure"
        private const val KEY_OPENVPN_CONFIG_CACHE = "openvpn_config_cache"
        private const val KEY_WG_PRIVATE_KEY = "wg_private_key_base64"
        private const val KEY_WG_CONFIG_JSON = "wg_config_json"
        private const val KEY_AUTO_CONNECT = "auto_connect"
        private const val KEY_ALWAYS_ON_GUIDANCE = "always_on_guidance_shown"
        private const val KEY_KILL_SWITCH_GUIDANCE = "kill_switch_guidance_shown"
        private const val KEY_DEVICE_LABEL = "device_label"
    }
}
