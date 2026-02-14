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

    fun getPrivateKey(): String? = prefs.getString(KEY_PRIVATE_KEY, null)

    fun setPrivateKey(value: String) {
        prefs.edit().putString(KEY_PRIVATE_KEY, value).apply()
    }

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

    fun clearPrivateKey() {
        prefs.edit().remove(KEY_PRIVATE_KEY).apply()
    }

    fun getProvisionedConfigJson(): String? = prefs.getString(KEY_PROVISIONED_CONFIG, null)

    fun setProvisionedConfigJson(json: String) {
        prefs.edit().putString(KEY_PROVISIONED_CONFIG, json).apply()
    }

    fun clearProvisionedConfig() {
        prefs.edit().remove(KEY_PROVISIONED_CONFIG).apply()
    }

    companion object {
        private const val PREFS_NAME = "novavpn_secure"
        private const val KEY_PRIVATE_KEY = "wg_private_key"
        private const val KEY_PROVISIONED_CONFIG = "provisioned_config"
        private const val KEY_AUTO_CONNECT = "auto_connect"
        private const val KEY_ALWAYS_ON_GUIDANCE = "always_on_guidance_shown"
        private const val KEY_KILL_SWITCH_GUIDANCE = "kill_switch_guidance_shown"
    }
}
