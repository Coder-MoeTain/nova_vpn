# Security Implementation Summary

This document summarizes the security improvements implemented for the NovaVPN provisioning server.

## ✅ Implemented Features

### 1. Rate Limiting
**Location**: `middleware/rateLimit.js`

Rate limiting has been implemented for all API endpoints to prevent abuse and DoS attacks:

- **Provisioning endpoint** (`POST /provision`): 5 requests per 15 minutes per IP
- **Management API** (`/api/*`): 100 requests per 15 minutes per IP
- **Location reporting** (`POST /report-location`): 60 requests per minute per IP
- **Health check** (`GET /health`): 30 requests per minute per IP

Rate limit responses include:
- `RateLimit-Limit`: Maximum number of requests allowed
- `RateLimit-Remaining`: Number of requests remaining
- `RateLimit-Reset`: Time when the rate limit resets

### 2. Security Headers (Helmet)
**Location**: `server.js` (top of middleware chain)

Helmet middleware has been configured to add security headers:

- **Content Security Policy (CSP)**: Configured to allow same-origin resources and inline styles/scripts for the UI
- **X-Frame-Options**: Prevents clickjacking attacks
- **X-Content-Type-Options**: Prevents MIME type sniffing
- **Strict-Transport-Security**: Enforces HTTPS (when behind HTTPS proxy)
- **X-XSS-Protection**: Enables XSS filtering
- **Referrer-Policy**: Controls referrer information

### 3. Input Validation
**Location**: `middleware/validation.js`

Comprehensive input validation using `express-validator`:

#### Validated Endpoints:

1. **POST /provision**
   - `publicKey`: Base64 WireGuard public key (44 chars, exact format)
   - `deviceName`: Optional, max 64 chars, alphanumeric + spaces/hyphens/underscores
   - `hostname`: Optional, max 64 chars, valid hostname format
   - `model`: Optional, max 64 chars
   - `phoneNumber`: Optional, E.164 format (e.g., +1234567890)
   - `latitude`: Optional, -90 to 90
   - `longitude`: Optional, -180 to 180

2. **POST /report-location**
   - `publicKey`: Base64 WireGuard public key
   - `latitude`: Required if provided, -90 to 90
   - `longitude`: Required if provided, -180 to 180

3. **PATCH /api/peers**
   - `publicKey`: Base64 WireGuard public key
   - `deviceName`: Optional, max 64 chars

4. **DELETE /api/peers**
   - `publicKey`: Base64 WireGuard public key (in body or query)

5. **POST /api/peers/ban**
   - `publicKey`: Base64 WireGuard public key

6. **GET /api/peers/:publicKey/location-history**
   - `publicKey`: Base64 WireGuard public key (URL parameter)
   - `limit`: Optional query parameter, 1-500

#### Validation Features:
- Automatic HTML escaping to prevent XSS
- Type checking and range validation
- Detailed error messages with field names
- Returns 400 status with validation details on failure

### 4. CORS Configuration
**Location**: `server.js`

CORS middleware configured with:
- Configurable origin (via `CORS_ORIGIN` env var, defaults to `*`)
- Allowed methods: GET, POST, PATCH, DELETE, OPTIONS
- Allowed headers: Content-Type, Authorization, X-API-Key

## 📦 New Dependencies

Added to `package.json`:
- `express-rate-limit`: ^7.1.5
- `helmet`: ^7.1.0
- `express-validator`: ^7.0.1
- `cors`: ^2.8.5

## 🔧 Installation

Install the new dependencies:

```bash
cd provisioning-server
npm install
```

## ⚙️ Configuration

### Environment Variables

Add to `.env` (optional):

```bash
# CORS origin (default: '*' allows all origins)
# For production, set specific origins: CORS_ORIGIN=https://yourdomain.com,https://admin.yourdomain.com
CORS_ORIGIN=*

# Rate limiting can be adjusted in middleware/rateLimit.js if needed
```

## 🧪 Testing

### Test Rate Limiting

```bash
# Test provisioning endpoint rate limit (should fail after 5 requests)
for i in {1..6}; do
  curl -X POST http://localhost:3000/provision \
    -H "Content-Type: application/json" \
    -d '{"publicKey":"test1234567890123456789012345678901234567890="}'
  echo ""
done
```

### Test Input Validation

```bash
# Test invalid public key (should return 400)
curl -X POST http://localhost:3000/provision \
  -H "Content-Type: application/json" \
  -d '{"publicKey":"invalid"}'

# Test valid request
curl -X POST http://localhost:3000/provision \
  -H "Content-Type: application/json" \
  -d '{"publicKey":"test1234567890123456789012345678901234567890="}'
```

### Test Security Headers

```bash
# Check security headers
curl -I http://localhost:3000/health
```

Expected headers:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 0`
- `Content-Security-Policy: ...`

## 📝 Changes Made

### Files Created:
1. `middleware/rateLimit.js` - Rate limiting middleware
2. `middleware/validation.js` - Input validation middleware
3. `SECURITY_IMPLEMENTATION.md` - This documentation

### Files Modified:
1. `server.js` - Added security middleware, rate limiting, and validation
2. `package.json` - Added new dependencies

## 🔒 Security Benefits

1. **DoS Protection**: Rate limiting prevents abuse and DoS attacks
2. **XSS Protection**: Input validation and CSP headers prevent XSS attacks
3. **Clickjacking Protection**: X-Frame-Options prevents UI clickjacking
4. **Data Validation**: All inputs are validated before processing
5. **SQL Injection**: Already protected via parameterized queries, validation adds extra layer
6. **Command Injection**: Input sanitization prevents command injection in execSync calls

## 🚀 Next Steps

For production deployment, consider:

1. **HTTPS**: Set up HTTPS (Let's Encrypt with Caddy/nginx)
2. **Restrict CORS**: Set `CORS_ORIGIN` to specific domains
3. **API Key Authentication**: Add optional API key auth for `/provision` endpoint
4. **Monitoring**: Add logging/monitoring for rate limit violations
5. **IP Whitelisting**: Consider IP whitelisting for admin endpoints

## 📚 References

- [express-rate-limit](https://github.com/express-rate-limit/express-rate-limit)
- [helmet](https://helmetjs.github.io/)
- [express-validator](https://express-validator.github.io/docs/)
- [CORS](https://github.com/expressjs/cors)
