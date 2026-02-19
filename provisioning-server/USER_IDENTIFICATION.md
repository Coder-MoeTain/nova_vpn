# VPN Client User Identification Guide

## Current Identification Methods

Your system currently collects and stores the following information to identify VPN clients:

### 1. **Public Key (Primary Identifier)**
- **What**: Unique WireGuard public key (44-character base64)
- **Where**: Stored in database, displayed in UI
- **Use**: Primary identifier - each device has a unique key
- **Location**: `peers.public_key` in database

### 2. **Device Information**
Currently collected from Android app:
- **Hostname**: Device codename (e.g., "walleye", "dream2lte")
- **Model**: Manufacturer + Model (e.g., "Samsung SM-G955F")
- **Custom Name**: Can be set via admin UI (rename feature)

### 3. **Location Data**
- **Current Location**: Latitude/longitude (if GPS permission granted)
- **Location History**: Historical locations stored in `location_history` table (if MySQL enabled)

### 4. **Network Information**
- **Client IP**: Assigned VPN IP (e.g., 10.66.66.3)
- **Remote IP**: Client's real IP address (from WireGuard endpoint)
- **Traffic Stats**: RX/TX bytes, last handshake time

## How to View User Identity

### Via Web UI
1. Access: `http://your-server:3000`
2. View the peers table showing:
   - Device name (model + hostname)
   - Custom name (if set)
   - Phone number (if provided)
   - Location (if available)
   - Client IP
   - Remote IP
   - Connection status

### Via API
```bash
# Get all peers with identity info
curl -u admin:password http://your-server:3000/api/peers

# Response includes:
# - publicKey (unique identifier)
# - deviceName (model + hostname)
# - deviceNameOverride (custom name)
# - hostname, model, phoneNumber
# - latitude, longitude
# - clientIp, remoteIp
# - online status, traffic stats
```

### Via Database (MySQL)
```sql
-- View all peer identities
SELECT 
    public_key,
    client_ip,
    device_name_override AS custom_name,
    CONCAT(model, ' (', hostname, ')') AS device_info,
    phone_number,
    latitude,
    longitude,
    created_at,
    banned
FROM peers
ORDER BY created_at DESC;

-- Find peer by custom name
SELECT * FROM peers WHERE device_name_override LIKE '%John%';

-- Find peer by phone number
SELECT * FROM peers WHERE phone_number = '+1234567890';

-- View location history for a peer
SELECT * FROM location_history 
WHERE peer_public_key = 'YOUR_PUBLIC_KEY'
ORDER BY created_at DESC;
```

## Improving User Identification

### Option 1: Add User Registration/Login (Recommended)

Add authentication so users register with:
- Email address
- Username
- Phone number
- Real name

**Implementation:**
1. Add `user_id` or `email` field to `peers` table
2. Modify Android app to collect user info during first connection
3. Link public key to user account

### Option 2: Collect More Device Information

Enhance Android app to send:
- **Android ID**: Unique device identifier
- **IMEI** (if available): Phone's unique identifier
- **Account Email**: User's Google account email
- **App Version**: Track which app version
- **OS Version**: Android version

### Option 3: Add User Tags/Labels

Allow admins to tag peers with:
- User name
- Department/Group
- Purpose (personal, work, etc.)
- Notes

### Option 4: Phone Number Collection

Currently `phoneNumber` is sent as `null` from the app. To collect it:

**Modify Android app** (`VpnViewModel.kt`):
```kotlin
// Get phone number (requires READ_PHONE_STATE permission)
val phoneNumber = try {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    telephonyManager.line1Number?.takeIf { it.isNotBlank() }
} catch (e: SecurityException) {
    null
}

return WireGuardProvisionRequest(
    publicKey = publicKey,
    hostname = hostname,
    model = model,
    phoneNumber = phoneNumber, // Now includes phone number
    latitude = lat,
    longitude = lng
)
```

### Option 5: Add User Registration Endpoint

Create a registration flow:
1. User opens app → enters email/username
2. App sends registration request to server
3. Server creates user account linked to public key
4. Future connections identify user by public key → user account

## Current Limitations

1. **No User Accounts**: Can't link multiple devices to one user
2. **Phone Number Not Collected**: App sends `null` for phone number
3. **Device Info Only**: Relies on device model/hostname (can be generic)
4. **No Persistent Identity**: If user clears app data, new public key = new identity

## Best Practices for Identification

### 1. Use Custom Device Names
- Admin can rename devices in UI
- Use format: "John's Phone", "Office Laptop", etc.
- Makes identification easier

### 2. Track Location History
- Enable MySQL to store location history
- Helps identify users by location patterns
- View via: `/api/peers/:publicKey/location-history`

### 3. Monitor Connection Patterns
- Track connection times
- Monitor traffic patterns
- Identify users by usage behavior

### 4. Use Remote IP for Geo-Location
- Server can resolve remote IP to location
- Helps identify user's real location
- Displayed in UI as "Location" column

## Example: Identifying a User

**Scenario**: User "John" connects from his Samsung phone

1. **First Connection**:
   - Public Key: `abc123...`
   - Model: "Samsung SM-G955F"
   - Hostname: "dream2lte"
   - Location: (40.7128, -74.0060) - New York

2. **Admin Action**:
   - Rename device to "John's Phone" via UI
   - Now identified as: "John's Phone"

3. **Future Connections**:
   - Same public key → Same identity
   - Location updates tracked
   - Traffic stats monitored

4. **Query Identity**:
   ```sql
   SELECT device_name_override, model, phone_number, latitude, longitude
   FROM peers 
   WHERE public_key = 'abc123...';
   ```

## Recommendations

For better user identification, implement:

1. **Short-term**:
   - ✅ Use custom device names (already available)
   - ✅ Enable MySQL for location history
   - ✅ Collect phone numbers (modify Android app)

2. **Medium-term**:
   - Add user registration/login
   - Link multiple devices to one user
   - Add user tags/labels

3. **Long-term**:
   - Full user management system
   - User dashboard
   - Multi-device support per user

## API Endpoints for Identity

```bash
# Get peer identity
GET /api/peers/:publicKey

# Update device name (for identification)
PATCH /api/peers
Body: { "publicKey": "...", "deviceName": "John's Phone" }

# Get location history
GET /api/peers/:publicKey/location-history

# Ban user (by public key)
POST /api/peers/ban
Body: { "publicKey": "..." }
```

## Database Schema

Current `peers` table stores:
- `public_key` - Unique identifier
- `client_ip` - VPN IP address
- `device_name_override` - Custom name (set by admin)
- `hostname` - Device codename
- `model` - Device model
- `phone_number` - Phone number (if provided)
- `latitude`, `longitude` - Current location
- `created_at` - First connection time
- `banned` - Ban status

This provides comprehensive identification capabilities!
