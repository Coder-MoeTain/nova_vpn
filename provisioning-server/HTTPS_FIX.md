# Fixing HTTPS Redirect Issue

## Problem
Browser is automatically redirecting HTTP to HTTPS, causing `ERR_CONNECTION_RESET` errors because the server only runs on HTTP (port 3000).

## Root Cause
Your browser has cached HSTS (HTTP Strict Transport Security) for `76.13.189.118`, forcing all connections to use HTTPS.

## Solutions

### Option 1: Clear Browser HSTS Cache (Recommended)

**Chrome/Edge:**
1. Go to `chrome://net-internals/#hsts`
2. Under "Delete domain security policies", enter: `76.13.189.118`
3. Click "Delete"
4. Try accessing `http://76.13.189.118:3000` again

**Firefox:**
1. Go to `about:config`
2. Search for `security.tls.insecure_fallback_hosts`
3. Add `76.13.189.118` to the list
4. Or clear site data: Settings → Privacy → Clear Data → Cookies and Site Data

**Safari:**
1. Safari → Preferences → Privacy
2. Click "Manage Website Data"
3. Search for `76.13.189.118` and remove it
4. Or use: `rm ~/Library/Cookies/HSTS.plist` (then restart Safari)

### Option 2: Use Incognito/Private Mode
Open the site in incognito/private browsing mode (HSTS cache doesn't apply).

### Option 3: Use Different Browser
Try accessing in a browser you haven't used for this domain before.

### Option 4: Access via IP with Different Port
Try accessing via `http://76.13.189.118:3000` directly (not through bookmarks that might force HTTPS).

### Option 5: Set Up HTTPS Properly (Production Solution)

If you need HTTPS in production, set up a reverse proxy:

**Using Caddy (easiest):**
```bash
# Install Caddy
sudo apt install caddy

# Create Caddyfile
cat > /etc/caddy/Caddyfile << EOF
76.13.189.118 {
    reverse_proxy localhost:3000
}
EOF

# Start Caddy
sudo systemctl start caddy
```

**Using Nginx:**
```nginx
server {
    listen 443 ssl http2;
    server_name 76.13.189.118;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## Quick Test

After clearing HSTS cache, test with:
```bash
curl -I http://76.13.189.118:3000
```

Should return HTTP 200 or 401 (if auth enabled), not SSL errors.

## Current Server Status

The server is configured to:
- ✅ Disable HSTS headers (won't force HTTPS)
- ✅ Disable Cross-Origin-Opener-Policy (prevents warnings)
- ✅ Serve static files (CSS/JS) correctly
- ⚠️ Run on HTTP only (port 3000)

For production, use a reverse proxy (Caddy/Nginx) with SSL certificates (Let's Encrypt).
