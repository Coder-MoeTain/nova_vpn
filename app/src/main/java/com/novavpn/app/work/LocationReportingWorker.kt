package com.novavpn.app.work

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.novavpn.app.api.ProvisioningApi
import com.novavpn.app.security.SecureStorage
import com.novavpn.app.util.Logger
import com.novavpn.app.vpn.WireGuardKeyGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that reports device location to the server every 5 minutes when VPN is connected.
 */
@HiltWorker
class LocationReportingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val provisioningApi: ProvisioningApi,
    private val secureStorage: SecureStorage
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Check location permission
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Logger.d("LocationReportingWorker: location permission not granted")
                return Result.retry() // Retry later in case permission is granted
            }

            // Get public key from stored private key
            val privateKeyBase64 = secureStorage.getWireGuardPrivateKeyBase64()
            if (privateKeyBase64 == null) {
                Logger.d("LocationReportingWorker: no private key found, skipping")
                return Result.success() // No key means not provisioned yet
            }

            val publicKey = try {
                WireGuardKeyGenerator.getPublicKeyFromPrivate(privateKeyBase64)
            } catch (e: Exception) {
                Logger.e(e, "LocationReportingWorker: failed to get public key")
                return Result.success() // Don't retry if key is invalid
            }

            // Get current location
            val location = getCurrentLocation()
            if (location == null) {
                Logger.d("LocationReportingWorker: no location available")
                return Result.retry() // Retry later when location becomes available
            }

            // Report to server
            val success = provisioningApi.reportLocation(
                publicKey = publicKey,
                latitude = location.latitude,
                longitude = location.longitude
            )

            if (success) {
                Logger.d("LocationReportingWorker: location reported successfully")
                Result.success()
            } else {
                Logger.w("LocationReportingWorker: failed to report location")
                Result.retry() // Retry on failure
            }
        } catch (e: Exception) {
            Logger.e(e, "LocationReportingWorker: error")
            Result.retry() // Retry on exception
        }
    }

    private fun getCurrentLocation(): android.location.Location? {
        return try {
            val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null

            // Try GPS first, then network
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (gpsLocation != null && isLocationValid(gpsLocation)) {
                return gpsLocation
            }

            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (networkLocation != null && isLocationValid(networkLocation)) {
                return networkLocation
            }

            null
        } catch (e: SecurityException) {
            Logger.w(e, "LocationReportingWorker: security exception getting location")
            null
        }
    }

    private fun isLocationValid(location: android.location.Location): Boolean {
        // Check if location is recent (within last hour) and has valid coordinates
        val age = System.currentTimeMillis() - location.time
        return age < 3600000 && // Less than 1 hour old
                location.latitude != 0.0 &&
                location.longitude != 0.0 &&
                location.accuracy > 0
    }
}
