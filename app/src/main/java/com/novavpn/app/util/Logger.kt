package com.novavpn.app.util

import android.content.Context
import timber.log.Timber
import java.util.regex.Pattern

/**
 * Safe logging with redaction of secrets. Never log private keys or full configs.
 */
object Logger {

    private val REDACT_PATTERNS = listOf(
        "private[_-]?key" to "***REDACTED***",
        "PrivateKey" to "***REDACTED***",
        "preshared[_-]?key" to "***REDACTED***",
        "([A-Za-z0-9+/]{42}=)" to "***BASE64_REDACTED***", // base64 key/cert-like
    ).map { (regex, replacement) -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE) to replacement }

    fun init(context: Context) {
        Timber.plant(Timber.DebugTree())
    }

    fun d(message: String, vararg args: Any?) {
        Timber.d(sanitize(message), *args)
    }

    fun i(message: String, vararg args: Any?) {
        Timber.i(sanitize(message), *args)
    }

    fun w(message: String, vararg args: Any?) {
        Timber.w(sanitize(message), *args)
    }

    fun w(t: Throwable?, message: String, vararg args: Any?) {
        Timber.w(t, sanitize(message), *args)
    }

    fun e(message: String, vararg args: Any?) {
        Timber.e(sanitize(message), *args)
    }

    fun e(t: Throwable?, message: String, vararg args: Any?) {
        Timber.e(t, sanitize(message), *args)
    }

    private fun sanitize(message: String): String {
        var out = message
        for ((pattern, replacement) in REDACT_PATTERNS) {
            out = pattern.matcher(out).replaceAll(replacement)
        }
        return out
    }
}
