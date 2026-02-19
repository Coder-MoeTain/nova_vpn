# Android App Self-Update Implementation Guide

## Current Status

**❌ The app currently does NOT have self-update capability.**

The app relies on manual updates through:
- Google Play Store (if published)
- Manual APK installation
- Android Studio deployment

---

## Self-Update Options

### Option 1: **In-App Update (Recommended for Play Store)**

If your app is published on Google Play Store, use Google Play's **In-App Update API**:

**Pros:**
- ✅ Secure and trusted
- ✅ Handles installation automatically
- ✅ No additional permissions needed
- ✅ Works seamlessly with Play Store

**Cons:**
- ❌ Requires Play Store publishing
- ❌ Not available for sideloaded apps

### Option 2: **Custom APK Download & Install**

For apps distributed outside Play Store (like yours), implement custom update mechanism:

**Pros:**
- ✅ Works for sideloaded apps
- ✅ Full control over update process
- ✅ Can host APK on your own server

**Cons:**
- ❌ Requires `REQUEST_INSTALL_PACKAGES` permission
- ❌ Security considerations (APK verification)
- ❌ User must grant install permission
- ❌ More complex implementation

---

## Implementation: Custom APK Update System

### Step 1: Add Required Permissions

**AndroidManifest.xml:**
```xml
<!-- For Android 8.0+ -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<!-- For downloading APK -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
```

### Step 2: Create Update API Endpoint

**On your provisioning server (`provisioning-server/server.js`):**

```javascript
// GET /api/update/check
app.get('/api/update/check', (req, res) => {
  const currentVersion = req.query.currentVersion || '1.0.0';
  const currentVersionCode = parseInt(req.query.currentVersionCode || '1');
  
  // Your latest version info
  const latestVersion = '1.1.0';
  const latestVersionCode = 2;
  const minRequiredVersion = '1.0.0'; // Minimum version that can update
  
  const updateAvailable = currentVersionCode < latestVersionCode;
  const forceUpdate = currentVersionCode < minRequiredVersion;
  
  res.json({
    updateAvailable,
    forceUpdate,
    latestVersion,
    latestVersionCode,
    downloadUrl: `http://YOUR_SERVER:3000/downloads/nova-vpn-${latestVersion}.apk`,
    releaseNotes: 'Bug fixes and performance improvements',
    fileSize: 12345678, // bytes
    checksum: 'sha256:abc123...' // For verification
  });
});
```

### Step 3: Create Update Checker Service

**Create `app/src/main/java/com/novavpn/app/update/UpdateChecker.kt`:**

```kotlin
package com.novavpn.app.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.novavpn.app.BuildConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UpdateInfo(
    val updateAvailable: Boolean,
    val forceUpdate: Boolean,
    val latestVersion: String,
    val latestVersionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val fileSize: Long,
    val checksum: String? = null
)

class UpdateChecker(private val context: Context) {
    private val httpClient = HttpClient()
    private val baseUrl = BuildConfig.PROVISIONING_BASE_URL
    
    suspend fun checkForUpdate(): UpdateInfo? {
        return try {
            val currentVersionCode = context.packageManager
                .getPackageInfo(context.packageName, 0).longVersionCode.toInt()
            val currentVersion = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
            
            val response: HttpResponse = httpClient.get("$baseUrl/api/update/check") {
                parameter("currentVersion", currentVersion)
                parameter("currentVersionCode", currentVersionCode)
            }
            
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<UpdateInfo>(response.body())
        } catch (e: Exception) {
            null
        }
    }
    
    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // Pre-Oreo always allowed
        }
    }
}
```

### Step 4: Create Update Manager

**Create `app/src/main/java/com/novavpn/app/update/UpdateManager.kt`:**

```kotlin
package com.novavpn.app.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.novavpn.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class UpdateManager(private val context: Context) {
    private val updateChecker = UpdateChecker(context)
    
    suspend fun checkAndPromptUpdate(
        onUpdateAvailable: (UpdateInfo) -> Unit,
        onNoUpdate: () -> Unit = {}
    ) {
        val updateInfo = updateChecker.checkForUpdate()
        
        if (updateInfo?.updateAvailable == true) {
            onUpdateAvailable(updateInfo)
        } else {
            onNoUpdate()
        }
    }
    
    fun downloadUpdate(updateInfo: UpdateInfo, onComplete: (Uri?) -> Unit) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl))
            .setTitle("NovaVPN Update")
            .setDescription("Downloading version ${updateInfo.latestVersion}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "nova-vpn-update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        
        val downloadId = downloadManager.enqueue(request)
        
        // Register receiver to handle download completion
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            onComplete(Uri.parse(uri))
                        }
                    }
                    context?.unregisterReceiver(this)
                }
            }
        }
        
        context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    
    fun installApk(apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val file = File(apkUri.path ?: return)
                val fileProviderUri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
                setDataAndType(fileProviderUri, "application/vnd.android.package-archive")
            }
        }
        
        context.startActivity(intent)
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun requestInstallPermission(onResult: (Boolean) -> Unit) {
        // This will show system dialog to request install permission
        // You need to handle the result in Activity
    }
}
```

### Step 5: Add Update UI Component

**Create `app/src/main/java/com/novavpn/app/ui/UpdateDialog.kt`:**

```kotlin
package com.novavpn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novavpn.app.update.UpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    forceUpdate: Boolean = false
) {
    AlertDialog(
        onDismissRequest = if (forceUpdate) {} else onDismiss,
        title = { Text("Update Available") },
        text = {
            Column {
                Text("Version ${updateInfo.latestVersion} is available.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Release notes:")
                Text(updateInfo.releaseNotes, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onUpdate) {
                Text("Update Now")
            }
        },
        dismissButton = if (forceUpdate) null else {
            {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}
```

### Step 6: Integrate into Settings Screen

**Update `SettingsScreen.kt`:**

```kotlin
// Add to SettingsScreen composable
Card(...) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("App Updates", ...)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                // Check for updates
                viewModel.checkForUpdates()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check for Updates")
        }
        Text(
            "Current version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
```

### Step 7: Add FileProvider Configuration

**Update `app/src/main/res/xml/file_paths.xml`:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="downloads" path="Download/" />
    <external-path name="external" path="." />
</paths>
```

---

## Security Considerations

### 1. **APK Verification**

Always verify downloaded APK:

```kotlin
fun verifyApk(apkFile: File, expectedChecksum: String): Boolean {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = apkFile.readBytes()
    val hash = digest.digest(bytes)
    val hexHash = hash.joinToString("") { "%02x".format(it) }
    return hexHash == expectedChecksum.removePrefix("sha256:")
}
```

### 2. **HTTPS Only**

Always download APK over HTTPS:

```kotlin
val downloadUrl = updateInfo.downloadUrl.replace("http://", "https://")
```

### 3. **Server Authentication**

Verify server certificate:

```kotlin
// Use certificate pinning in your HTTP client
```

### 4. **User Consent**

Always show update dialog - never install silently.

---

## Implementation Checklist

- [ ] Add `REQUEST_INSTALL_PACKAGES` permission
- [ ] Create update check API endpoint on server
- [ ] Implement `UpdateChecker` class
- [ ] Implement `UpdateManager` class
- [ ] Create update dialog UI
- [ ] Add update check to Settings screen
- [ ] Add periodic update check (optional)
- [ ] Implement APK verification
- [ ] Test on Android 8.0+ devices
- [ ] Handle permission requests properly

---

## Testing

1. **Test update flow:**
   - Lower app version code in `build.gradle.kts`
   - Deploy APK to server
   - Run app and check for updates
   - Verify download and install

2. **Test permission handling:**
   - Test on Android 8.0+ (requires permission)
   - Test on Android 7.1 and below (no permission needed)

3. **Test force update:**
   - Set minimum required version
   - Verify user cannot dismiss dialog

---

## Alternative: Use Existing Libraries

Consider using libraries like:
- **AppUpdater**: https://github.com/javiersantos/AppUpdater
- **Siren**: Android version checking library

---

## Recommendation

For your use case (VPN app distributed outside Play Store):

1. **Implement custom update system** (as described above)
2. **Add update check on app startup** (optional, can be manual)
3. **Show update notification** when available
4. **Always verify APK** before installation
5. **Use HTTPS** for all downloads

This gives you full control while maintaining security.

---

*Note: Self-updating apps require careful security considerations. Always verify APKs and use HTTPS.*
