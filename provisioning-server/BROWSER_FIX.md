# Fix Browser HTTPS Redirect Issue

## The Problem
Your browser is **automatically redirecting** `http://76.13.189.118:3000` to `https://76.13.189.118:3000`, but the server only supports HTTP. This causes `ERR_SSL_PROTOCOL_ERROR`.

## Why This Happens
Your browser has **HSTS (HTTP Strict Transport Security)** cached for `76.13.189.118` from a previous visit. The browser remembers "this domain should always use HTTPS" and forces it.

## ✅ Solution: Clear Browser HSTS Cache

### Chrome/Edge (Windows/Mac/Linux)
1. Open a new tab
2. Go to: `chrome://net-internals/#hsts`
3. Scroll down to **"Delete domain security policies"**
4. Enter: `76.13.189.118`
5. Click **"Delete"**
6. **Close ALL browser windows** (important!)
7. Reopen browser
8. Try: `http://76.13.189.118:3000` (make sure it's HTTP, not HTTPS)

### Firefox
1. Open a new tab
2. Go to: `about:config`
3. Search for: `security.tls.insecure_fallback_hosts`
4. Click "Edit" and add: `76.13.189.118`
5. Or clear site data:
   - Settings → Privacy & Security → Cookies and Site Data → Manage Data
   - Search for `76.13.189.118` → Remove

### Safari (Mac)
1. Safari → Preferences → Privacy
2. Click "Manage Website Data"
3. Search for `76.13.189.118`
4. Click "Remove"
5. Or run in terminal: `rm ~/Library/Cookies/HSTS.plist` then restart Safari

## 🚀 Quick Alternative: Use Incognito/Private Mode

**Easiest solution** - HSTS cache doesn't apply in private/incognito mode:

- **Chrome/Edge**: `Ctrl+Shift+N` (Windows) or `Cmd+Shift+N` (Mac)
- **Firefox**: `Ctrl+Shift+P` (Windows) or `Cmd+Shift+P` (Mac)
- **Safari**: `Cmd+Shift+N`

Then access: `http://76.13.189.118:3000`

## 🧪 Test if Server is Working

After clearing HSTS, test with curl (bypasses browser):

```bash
# Test HTTP access (should work)
curl -I http://76.13.189.118:3000

# Should return HTTP 200 or 401 (if auth enabled)
# NOT SSL errors
```

If curl works but browser doesn't, it's definitely HSTS cache.

## ⚠️ Important Notes

1. **Always use HTTP** - The server runs on `http://76.13.189.118:3000` (not HTTPS)
2. **Bookmarks** - If you bookmarked the HTTPS version, delete it and create a new HTTP bookmark
3. **Browser extensions** - Some security extensions force HTTPS - disable them temporarily
4. **Type manually** - Don't rely on autocomplete, type `http://` explicitly

## 🔒 For Production (Future)

When you're ready for production, set up HTTPS properly:
- Use **Caddy** (automatic HTTPS with Let's Encrypt)
- Or **Nginx** with SSL certificates
- See `HTTPS_FIX.md` for setup instructions

## Still Not Working?

If clearing HSTS doesn't work:

1. **Try a different browser** you haven't used for this IP
2. **Use curl/wget** to test: `curl http://76.13.189.118:3000`
3. **Check firewall** - ensure port 3000 is open
4. **Check server logs** - see if requests are reaching the server

The server code is correct - this is purely a browser cache issue.
