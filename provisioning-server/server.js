/**
 * NovaVPN Provisioning API
 *
 * Run on the same machine as your WireGuard server.
 * POST /provision → adds peer and returns config.
 * Web UI at / for listing and revoking peers (optional admin auth).
 */

// Load .env from project root or from provisioning-server (single .env for the project)
const path = require('path');
const fs = require('fs');
const rootEnv = path.join(__dirname, '..', '.env');
const localEnv = path.join(__dirname, '.env');
if (fs.existsSync(rootEnv)) require('dotenv').config({ path: rootEnv });
else require('dotenv').config({ path: localEnv });
const express = require('express');
const os = require('os');
const { execSync } = require('child_process');
const helmet = require('helmet');
const db = require('./db');
const { provisionLimiter, apiLimiter, locationReportLimiter, healthLimiter } = require('./middleware/rateLimit');
const { validateProvision, validateReportLocation, validateUpdatePeer, validateDeletePeer, validateBanPeer, validateLocationHistory } = require('./middleware/validation');

const app = express();

// IMPORTANT: If browser forces HTTPS due to HSTS cache, you MUST clear browser HSTS cache
// The server only runs on HTTP (port 3000), so HTTPS requests will fail
// See HTTPS_FIX.md for instructions on clearing HSTS cache

// Security headers (helmet) - minimal configuration to avoid HTTPS issues
// Disable CSP and other headers that might force HTTPS upgrades
app.use(helmet({
  contentSecurityPolicy: false, // Disable CSP to prevent HTTPS upgrade issues
  crossOriginEmbedderPolicy: false,
  crossOriginOpenerPolicy: false,
  crossOriginResourcePolicy: false,
  strictTransportSecurity: false, // Never send HSTS header
}));

app.use(express.json());

const PORT = process.env.PORT || 3000;
const STATE_FILE = path.join(__dirname, 'state.json');
const PUBLIC_DIR = path.join(__dirname, 'public');

// WireGuard
const WG_ENDPOINT_HOST = process.env.WG_ENDPOINT_HOST || '76.13.189.118';
const WG_ENDPOINT_PORT = parseInt(process.env.WG_ENDPOINT_PORT || '64288', 10);
const WG_SERVER_PUBLIC_KEY = process.env.WG_SERVER_PUBLIC_KEY;
const WG_INTERFACE = process.env.WG_INTERFACE || 'wg0';
const WG_NETWORK_IPv4 = process.env.WG_NETWORK_IPv4 || '10.66.66';
const WG_NETWORK_IPv6 = process.env.WG_NETWORK_IPv6 || 'fd42:42:42';
let nextClientIndex = parseInt(process.env.WG_NEXT_CLIENT_INDEX || '3', 10);
const WG_DNS = (process.env.WG_DNS || '1.1.1.1,1.0.0.1').trim().replace(/\s*,\s*/g, ','); // comma-separated, no spaces
const WG_ALLOWED_IPS = process.env.WG_ALLOWED_IPS || '0.0.0.0/0, ::/0';
const WG_PERSISTENT_KEEPALIVE = parseInt(process.env.WG_PERSISTENT_KEEPALIVE || '25', 10);
const USE_CONFIG_FILE = process.env.WG_USE_CONFIG_FILE === '1';
const WG_CONFIG_PATH = process.env.WG_CONFIG_PATH || '/etc/wireguard/wg0.conf';

// Optional admin auth for UI and management API
const ADMIN_USERNAME = process.env.ADMIN_USERNAME || 'admin';
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD;

// OpenVPN provisioning (optional)
const OPENVPN_ENABLED = process.env.OPENVPN_ENABLED === '1';
const OPENVPN_SERVER_HOST = process.env.OPENVPN_SERVER_HOST || process.env.WG_ENDPOINT_HOST || '76.13.189.118';
const OPENVPN_SERVER_PORT = parseInt(process.env.OPENVPN_SERVER_PORT || '1194', 10);
const OPENVPN_EASYRSA_DIR = process.env.OPENVPN_EASYRSA_DIR || '';
const OPENVPN_SCRIPT = path.join(__dirname, 'scripts', 'gen-openvpn-client.sh');

if (!WG_SERVER_PUBLIC_KEY && !OPENVPN_ENABLED) {
  console.warn('Warning: WG_SERVER_PUBLIC_KEY is not set. Set it in .env (or use OPENVPN_ENABLED=1 for OpenVPN only).');
}

// Fail fast if we cannot run wg (need root for wg set to work later) — skip when OpenVPN-only
if (!OPENVPN_ENABLED && !USE_CONFIG_FILE) {
  try {
    execSync(`wg show ${WG_INTERFACE}`, { stdio: 'pipe' });
  } catch (err) {
    console.error('Cannot run "wg show ' + WG_INTERFACE + '". Run this server as root (e.g. sudo node server.js) so that wg set can add peers.');
    process.exit(1);
  }
}

let state = { nextClientIndex, peers: [] };

function loadState() {
  try {
    const data = fs.readFileSync(STATE_FILE, 'utf8');
    const obj = JSON.parse(data);
    if (typeof obj.nextClientIndex === 'number') nextClientIndex = obj.nextClientIndex;
    if (Array.isArray(obj.peers)) state.peers = obj.peers;
  } catch (_) {}
  state.nextClientIndex = nextClientIndex;
}

function saveState() {
  nextClientIndex = state.nextClientIndex;
  fs.writeFileSync(STATE_FILE, JSON.stringify({
    nextClientIndex: state.nextClientIndex,
    peers: state.peers,
  }, null, 2), 'utf8');
}

function isValidPublicKey(key) {
  if (typeof key !== 'string') return false;
  return /^[A-Za-z0-9+/]{43}=$/.test(key.trim());
}

function getClientAddresses(index) {
  const ipv4 = `${WG_NETWORK_IPv4}.${index}/32`;
  const ipv6 = `${WG_NETWORK_IPv6}::${index}/128`;
  return `${ipv4},${ipv6}`;
}

function addPeerWithWgSet(publicKey, allowedIps) {
  const iface = WG_INTERFACE.replace(/[^a-zA-Z0-9_-]/g, '');
  execSync(`wg set ${iface} peer ${publicKey.trim()} allowed-ips ${allowedIps}`, { stdio: 'inherit' });
}

function removePeerWithWgSet(publicKey) {
  const iface = WG_INTERFACE.replace(/[^a-zA-Z0-9_-]/g, '');
  execSync(`wg set ${iface} peer ${publicKey.trim()} remove`, { stdio: 'inherit' });
}

function addPeerWithConfigFile(publicKey, allowedIps) {
  const block = `\n[Peer]\nPublicKey = ${publicKey.trim()}\nAllowedIPs = ${allowedIps}\n`;
  fs.appendFileSync(WG_CONFIG_PATH, block, 'utf8');
  console.log('Appended peer to', WG_CONFIG_PATH, '- reload WireGuard to apply.');
}

function removePeerFromConfigFile(publicKey) {
  let content = fs.readFileSync(WG_CONFIG_PATH, 'utf8');
  const key = publicKey.trim();
  const blocks = content.split(/\n(?=\[Peer\])/);
  const filtered = blocks.filter(block => {
    const match = block.match(/PublicKey\s*=\s*(\S+)/);
    return !match || match[1] !== key;
  });
  fs.writeFileSync(WG_CONFIG_PATH, filtered.join('').trimEnd() + '\n', 'utf8');
  console.log('Removed peer from', WG_CONFIG_PATH, '- reload WireGuard to apply.');
}

// Parse "wg show wg0 dump" for peer stats (endpoint, lastHandshake, rx, tx)
function getWgPeerStats() {
  const iface = WG_INTERFACE.replace(/[^a-zA-Z0-9_-]/g, '');
  const out = execSync(`wg show ${iface} dump`, { encoding: 'utf8', maxBuffer: 65536 });
  const lines = out.trim().split('\n');
  const stats = {};
  for (let i = 1; i < lines.length; i++) {
    const parts = lines[i].split('\t');
    if (parts.length < 8) continue;
    const [pubKey, , endpoint, , lastHandshake, rxBytes, txBytes] = parts;
    const endpointIp = endpoint ? endpoint.split(':')[0] : null;
    stats[pubKey] = {
      endpoint: endpoint || null,
      endpointIp,
      lastHandshake: lastHandshake === '0' ? null : parseInt(lastHandshake, 10),
      rxBytes: parseInt(rxBytes, 10) || 0,
      txBytes: parseInt(txBytes, 10) || 0,
    };
  }
  return stats;
}

const ONLINE_MAX_AGE_SEC = 120; // consider online if handshake within 2 min

function formatBytes(n) {
  if (n >= 1073741824) return (n / 1073741824).toFixed(2) + ' GiB';
  if (n >= 1048576) return (n / 1048576).toFixed(2) + ' MiB';
  if (n >= 1024) return (n / 1024).toFixed(2) + ' KiB';
  return n + ' B';
}

// Geo cache: ip -> { city, country, updatedAt }
const geoCache = {};
const GEO_CACHE_TTL_MS = 10 * 60 * 1000;

function getGeoForIp(ip) {
  if (!ip || ip === '0.0.0.0') return Promise.resolve(null);
  const cached = geoCache[ip];
  if (cached && Date.now() - cached.updatedAt < GEO_CACHE_TTL_MS) return Promise.resolve(cached);
  return fetch(`http://ip-api.com/json/${ip}?fields=country,city,regionName`)
    .then(r => r.json())
    .then(data => {
      if (data.country == null) return null;
      const entry = { country: data.country, city: data.city || '', regionName: data.regionName || '', updatedAt: Date.now() };
      geoCache[ip] = entry;
      return entry;
    })
    .catch(() => null);
}

function authMiddleware(req, res, next) {
  if (!ADMIN_PASSWORD) return next();
  const auth = req.headers.authorization;
  if (!auth || !auth.startsWith('Basic ')) {
    res.setHeader('WWW-Authenticate', 'Basic realm="NovaVPN Admin"');
    return res.status(401).send('Authentication required');
  }
  const b64 = auth.slice(6);
  const decoded = Buffer.from(b64, 'base64').toString('utf8');
  const [user, pass] = decoded.split(':');
  if (user !== ADMIN_USERNAME || pass !== ADMIN_PASSWORD) {
    res.setHeader('WWW-Authenticate', 'Basic realm="NovaVPN Admin"');
    return res.status(401).send('Invalid credentials');
  }
  next();
}

// CORS configuration (allow all origins for public API, restrict in production)
const cors = require('cors');
app.use(cors({
  origin: process.env.CORS_ORIGIN || '*', // Set CORS_ORIGIN in .env to restrict origins
  methods: ['GET', 'POST', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-API-Key'],
  credentials: false
}));

// Serve static files BEFORE auth middleware (CSS, JS, images must be accessible)
if (fs.existsSync(PUBLIC_DIR)) {
  // Add headers to prevent HTTPS upgrade for static files
  app.use(express.static(PUBLIC_DIR, {
    setHeaders: (res, path) => {
      // Explicitly set Content-Type for CSS and JS
      if (path.endsWith('.css')) {
        res.setHeader('Content-Type', 'text/css');
      } else if (path.endsWith('.js')) {
        res.setHeader('Content-Type', 'application/javascript');
      }
      // Prevent any HTTPS upgrade headers
      res.removeHeader('Strict-Transport-Security');
      res.removeHeader('Upgrade-Insecure-Requests');
    }
  }));
}

// Ensure only one provision at a time so each device gets a unique IP (no race)
let provisionLock = Promise.resolve();

// Public API (for the Android app)
app.post('/provision', provisionLimiter, validateProvision, (req, res) => {
  const publicKey = req.body.publicKey.trim();
  if (!WG_SERVER_PUBLIC_KEY) {
    return res.status(500).json({ error: 'Server misconfigured: WG_SERVER_PUBLIC_KEY not set' });
  }

  const keyTrimmed = publicKey; // Already trimmed by validation

  provisionLock = provisionLock.then(async () => {
    loadState();
    let index;
    let existing = null;
    if (db.isEnabled()) {
      existing = await db.getPeerByPublicKey(keyTrimmed);
      // Check if peer is banned
      if (existing && existing.banned) {
        return res.status(403).json({ error: 'This device has been banned from connecting to the server' });
      }
      if (existing) index = parseInt(existing.clientIp.split('.').pop(), 10);
      else index = await db.getNextClientIndex();
    } else {
      existing = state.peers.find(p => p.publicKey === keyTrimmed);
      if (existing) index = parseInt(existing.clientIp.split('.').pop(), 10);
      else { index = state.nextClientIndex; state.nextClientIndex = index + 1; }
    }
    const ipv4Only = `${WG_NETWORK_IPv4}.${index}/32`;
    const clientAddress = getClientAddresses(index);

    try {
      if (USE_CONFIG_FILE) {
        addPeerWithConfigFile(keyTrimmed, ipv4Only);
      } else {
        addPeerWithWgSet(keyTrimmed, ipv4Only);
      }
    } catch (err) {
      console.error('Failed to add peer:', err.message);
      res.status(500).json({ error: 'Failed to add peer to WireGuard', detail: err.message });
      return;
    }

    // Values already validated and sanitized by validation middleware
    const deviceName = req.body.deviceName || null;
    const hostname = req.body.hostname || null;
    const model = req.body.model || null;
    const phoneNumber = req.body.phoneNumber || req.body.phone || null;
    const lat = req.body.latitude != null ? Number(req.body.latitude) : (req.body.lat != null ? Number(req.body.lat) : null);
    const lng = req.body.longitude != null ? Number(req.body.longitude) : (req.body.lng != null ? Number(req.body.lng) : null);
    const latitude = typeof lat === 'number' && !Number.isNaN(lat) ? lat : null;
    const longitude = typeof lng === 'number' && !Number.isNaN(lng) ? lng : null;

    if (!existing) {
      const clientIp = `${WG_NETWORK_IPv4}.${index}`;
      const peerRecord = {
        publicKey: keyTrimmed,
        clientIp,
        deviceNameOverride: deviceName || null,
        hostname: hostname || null,
        model: model || null,
        phoneNumber: phoneNumber || null,
        latitude,
        longitude,
      };
      if (db.isEnabled()) {
        await db.upsertPeer(peerRecord);
        await db.setNextClientIndex(index + 1);
        if (latitude != null && longitude != null) await db.addLocationHistory(keyTrimmed, latitude, longitude);
      } else {
        state.peers.push({
          publicKey: keyTrimmed,
          clientIp,
          deviceName: deviceName || null,
          hostname: hostname || null,
          model: model || null,
          phoneNumber: phoneNumber || null,
          latitude,
          longitude,
          createdAt: new Date().toISOString(),
        });
        saveState();
      }
    } else if (db.isEnabled() && (latitude != null && longitude != null)) {
      await db.addLocationHistory(keyTrimmed, latitude, longitude);
    }

    const response = {
      endpointHost: WG_ENDPOINT_HOST,
      endpointPort: WG_ENDPOINT_PORT,
      serverPublicKey: WG_SERVER_PUBLIC_KEY,
      clientAddress,
      dns: WG_DNS,
      allowedIPs: WG_ALLOWED_IPS,
      persistentKeepalive: WG_PERSISTENT_KEEPALIVE,
      presharedKey: null,
    };
    console.log('Provisioned peer', keyTrimmed.slice(0, 8) + '...', '→', clientAddress, 'allowedIPs=', response.allowedIPs);
    res.json(response);
  }).catch(err => {
    console.error('Provision error:', err);
    if (!res.headersSent) res.status(500).json({ error: 'Provision failed', detail: err.message });
  });
});

app.get('/health', healthLimiter, (req, res) => {
  res.json({ ok: true, service: 'novavpn-provisioning' });
});

// Public: app reports current location (no auth) for location history
app.post('/report-location', locationReportLimiter, validateReportLocation, (req, res) => {
  const publicKey = req.body.publicKey.trim();
  const lat = req.body.latitude != null ? Number(req.body.latitude) : (req.body.lat != null ? Number(req.body.lat) : null);
  const lng = req.body.longitude != null ? Number(req.body.longitude) : (req.body.lng != null ? Number(req.body.lng) : null);
  const latitude = typeof lat === 'number' && !Number.isNaN(lat) ? lat : null;
  const longitude = typeof lng === 'number' && !Number.isNaN(lng) ? lng : null;
  
  if (latitude === null || longitude === null) {
    return res.status(400).json({ error: 'Missing or invalid latitude/longitude' });
  }
  if (!db.isEnabled()) {
    return res.json({ ok: true, saved: false, message: 'Location history disabled (MySQL not configured)' });
  }
  db.addLocationHistory(publicKey, latitude, longitude).then(() => {
    res.json({ ok: true, saved: true });
  }).catch(err => {
    console.error('report-location error:', err);
    res.status(500).json({ error: 'Failed to save location' });
  });
});

// OpenVPN provisioning: always register route so app gets 503 + message instead of 404 when disabled
let openvpnClientIndex = parseInt(process.env.OPENVPN_NEXT_CLIENT_INDEX || '1', 10);
const openvpnStatePath = path.join(__dirname, 'openvpn-state.json');

app.post('/provision-openvpn', (req, res) => {
  if (!OPENVPN_ENABLED) {
    return res.status(503).json({
      error: 'OpenVPN provisioning is disabled',
      detail: 'Set OPENVPN_ENABLED=1 in .env on the server and restart.'
    });
  }
  if (!OPENVPN_EASYRSA_DIR || !fs.existsSync(OPENVPN_SCRIPT)) {
    return res.status(503).json({
      error: 'OpenVPN provisioning not configured',
      detail: 'Set OPENVPN_EASYRSA_DIR in .env and ensure scripts/gen-openvpn-client.sh exists (run: sed -i "s/\\r$//" scripts/gen-openvpn-client.sh).'
    });
  }
  try {
    const ov = JSON.parse(fs.readFileSync(openvpnStatePath, 'utf8'));
    if (typeof ov.nextClientIndex === 'number') openvpnClientIndex = ov.nextClientIndex;
  } catch (_) {}

  const name = `device_${openvpnClientIndex}`;
  try {
    // Run script with Unix line endings (strip CRLF) so it works when deployed from Windows
    let scriptContent = fs.readFileSync(OPENVPN_SCRIPT, 'utf8');
    if (scriptContent.includes('\r')) {
      scriptContent = scriptContent.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
      const tmpScript = path.join(os.tmpdir(), `gen-openvpn-client-${Date.now()}.sh`);
      fs.writeFileSync(tmpScript, scriptContent, 'utf8');
      try {
        const config = execSync(
          `bash "${tmpScript}" "${name}" "${OPENVPN_SERVER_HOST}" "${OPENVPN_SERVER_PORT}" "${OPENVPN_EASYRSA_DIR}"`,
          {
            encoding: 'utf8',
            maxBuffer: 256 * 1024,
            env: { ...process.env, EASYRSA_DIR: OPENVPN_EASYRSA_DIR }
          }
        );
        openvpnClientIndex += 1;
        fs.writeFileSync(openvpnStatePath, JSON.stringify({ nextClientIndex: openvpnClientIndex }, null, 2));
        console.log('Provisioned OpenVPN client', name);
        return res.json({ config: config.trim() });
      } finally {
        try { fs.unlinkSync(tmpScript); } catch (_) {}
      }
    }
    const config = execSync(
      `bash "${OPENVPN_SCRIPT}" "${name}" "${OPENVPN_SERVER_HOST}" "${OPENVPN_SERVER_PORT}" "${OPENVPN_EASYRSA_DIR}"`,
      {
        encoding: 'utf8',
        maxBuffer: 256 * 1024,
        env: { ...process.env, EASYRSA_DIR: OPENVPN_EASYRSA_DIR }
      }
    );
    openvpnClientIndex += 1;
    fs.writeFileSync(openvpnStatePath, JSON.stringify({ nextClientIndex: openvpnClientIndex }, null, 2));
    console.log('Provisioned OpenVPN client', name);
    res.json({ config: config.trim() });
  } catch (err) {
    console.error('OpenVPN provision failed:', err.message);
    res.status(500).json({ error: 'Failed to generate OpenVPN config', detail: err.message });
  }
});

// Serve static files BEFORE auth middleware (CSS, JS, images)
if (fs.existsSync(PUBLIC_DIR)) {
  app.use(express.static(PUBLIC_DIR));
}

// Management API and UI (optional auth)
app.use('/api', apiLimiter, authMiddleware);
// Apply auth to root route only for HTML (static assets are served above without auth)
app.get('/', authMiddleware, (req, res) => {
  res.sendFile(path.join(PUBLIC_DIR, 'index.html'));
});

app.get('/api/peers', (req, res) => {
  loadState();
  const fetchPeersAndRespond = (peersList, nextIdx) => {
  let wgStats = {};
  try {
    wgStats = getWgPeerStats();
  } catch (e) {
    // wg not available or not root
  }
  const now = Math.floor(Date.now() / 1000);
  const peersWithStats = peersList.map((p, i) => {
    const s = wgStats[p.publicKey] || {};
    const lastHandshake = s.lastHandshake || null;
    const online = lastHandshake != null && (now - lastHandshake) <= ONLINE_MAX_AGE_SEC;
    const deviceOverride = (p.deviceNameOverride && String(p.deviceNameOverride).trim()) ? String(p.deviceNameOverride).trim() : null;
    const deviceBase = (p.model && p.hostname) ? `${p.model} (${p.hostname})` : ((p.model || p.hostname) || `Device ${i + 1}`);
    return {
      ...p,
      deviceName: deviceBase,
      deviceNameOverride: deviceOverride,
      banned: p.banned || false,
      bannedAt: p.bannedAt || null,
      endpoint: s.endpoint || null,
      endpointIp: s.endpointIp || null,
      remoteIp: s.endpointIp || null,
      lastHandshake,
      online,
      rxBytes: s.rxBytes || 0,
      txBytes: s.txBytes || 0,
      trafficRx: formatBytes(s.rxBytes || 0),
      trafficTx: formatBytes(s.txBytes || 0),
      locationDevice: (p.latitude != null && p.longitude != null) ? { latitude: p.latitude, longitude: p.longitude } : null,
    };
  });
  Promise.all(peersWithStats.map(p => {
    if (p.locationDevice) return Promise.resolve({ ...p, location: `${p.latitude.toFixed(5)}, ${p.longitude.toFixed(5)}`, locationGeo: null });
    return getGeoForIp(p.endpointIp).then(geo => ({
      ...p,
      location: geo ? [geo.city, geo.regionName, geo.country].filter(Boolean).join(', ') : null,
      locationGeo: geo,
    }));
  }))
    .then(peersResolved => {
      const peers = peersResolved.map(({ locationGeo, locationDevice, ...p }) => p);
      res.json({
        peers,
        nextClientIndex: nextIdx,
        nextClientIp: `${WG_NETWORK_IPv4}.${nextIdx}`,
      });
    })
    .catch(err => {
      console.error('Geo lookup error:', err);
      const peers = peersWithStats.map(p => ({
        ...p,
        location: (p.locationDevice && p.latitude != null && p.longitude != null) ? `${p.latitude.toFixed(5)}, ${p.longitude.toFixed(5)}` : null,
      }));
      const out = peers.map(({ locationDevice, ...q }) => q);
      res.json({
        peers: out,
        nextClientIndex: nextIdx,
        nextClientIp: `${WG_NETWORK_IPv4}.${nextIdx}`,
      });
    });
  };
  if (db.isEnabled()) {
    db.getPeers().then(peersList => db.getNextClientIndex().then(nextIdx => fetchPeersAndRespond(peersList, nextIdx))).catch(err => {
      console.error('DB getPeers error:', err);
      res.status(500).json({ error: 'Failed to load peers' });
    });
  } else {
    fetchPeersAndRespond(state.peers, state.nextClientIndex);
  }
});

app.patch('/api/peers', validateUpdatePeer, (req, res) => {
  const publicKey = req.body.publicKey.trim();
  const deviceName = req.body.deviceName ? req.body.deviceName.trim().slice(0, 128) || null : null;
  if (db.isEnabled()) {
    db.updateDeviceName(publicKey, deviceName).then(updated => {
      if (!updated) return res.status(404).json({ error: 'Peer not found' });
      res.json({ ok: true, deviceName: deviceName || null });
    }).catch(err => {
      console.error('PATCH peers error:', err);
      res.status(500).json({ error: 'Failed to update device name' });
    });
    return;
  }
  loadState();
  const p = state.peers.find(x => x.publicKey === publicKey);
  if (!p) return res.status(404).json({ error: 'Peer not found' });
  p.deviceName = deviceName;
  saveState();
  res.json({ ok: true, deviceName: deviceName || null });
});

app.get('/api/peers/:publicKey/location-history', validateLocationHistory, (req, res) => {
  const publicKey = req.params.publicKey.trim();
  if (!db.isEnabled()) {
    return res.json({ locations: [], message: 'Location history requires MySQL' });
  }
  const limit = req.query.limit ? Math.min(parseInt(req.query.limit, 10), 500) : 100;
  db.getLocationHistory(publicKey, limit).then(locations => {
    res.json({ locations });
  }).catch(err => {
    console.error('location-history error:', err);
    res.status(500).json({ error: 'Failed to load location history' });
  });
});

// Delete from database only (keeps WireGuard peer active)
app.delete('/api/peers/database-only', (req, res) => {
  const publicKey = (req.body?.publicKey || req.query?.publicKey || '').trim();
  if (!publicKey || !isValidPublicKey(publicKey)) {
    return res.status(400).json({ error: 'Missing or invalid publicKey' });
  }
  if (!db.isEnabled()) {
    return res.status(400).json({ error: 'Database-only delete requires MySQL' });
  }
  db.getPeerByPublicKey(publicKey).then(exists => {
    if (!exists) return res.status(404).json({ error: 'Peer not found in database' });
    return db.deletePeer(publicKey).then(() => {
      console.log('Deleted peer from database', publicKey.slice(0, 8) + '...');
      res.json({ ok: true });
    });
  }).catch(err => {
    console.error('DELETE database-only error:', err);
    res.status(500).json({ error: 'Failed to delete peer from database' });
  });
});

// Ban peer: removes from WireGuard and marks as banned in database
app.post('/api/peers/ban', validateBanPeer, (req, res) => {
  const publicKey = req.body.publicKey.trim();

  loadState();
  const peersList = db.isEnabled() ? null : state.peers;
  const idx = peersList ? state.peers.findIndex(p => p.publicKey === publicKey) : -1;
  const checkExists = db.isEnabled()
    ? db.getPeerByPublicKey(publicKey).then(p => !!p)
    : Promise.resolve(idx !== -1);

  checkExists.then(exists => {
    if (!exists) return res.status(404).json({ error: 'Peer not found' });
    
    // Remove peer from WireGuard
    try {
      if (USE_CONFIG_FILE) {
        removePeerFromConfigFile(publicKey);
      } else {
        removePeerWithWgSet(publicKey);
      }
    } catch (err) {
      console.error('Failed to remove peer from WireGuard:', err.message);
      return res.status(500).json({ error: 'Failed to remove peer from WireGuard', detail: err.message });
    }
    
    // Mark as banned in database
    if (db.isEnabled()) {
      return db.banPeer(publicKey).then(() => {
        console.log('Banned peer', publicKey.slice(0, 8) + '...');
        res.json({ ok: true, message: 'Peer banned successfully' });
      }).catch(err => {
        console.error('Failed to ban peer in database:', err);
        res.status(500).json({ error: 'Failed to ban peer in database' });
      });
    }
    
    // For file-based state, we can't track banned status, so just revoke
    state.peers.splice(idx, 1);
    saveState();
    console.log('Revoked peer (file-based, cannot track ban)', publicKey.slice(0, 8) + '...');
    res.json({ ok: true, message: 'Peer revoked (ban tracking requires MySQL)' });
  }).catch(err => {
    console.error('Ban peer error:', err);
    res.status(500).json({ error: 'Failed to ban peer' });
  });
});

app.delete('/api/peers', validateDeletePeer, (req, res) => {
  const publicKey = (req.body.publicKey || req.query.publicKey || '').trim();

  loadState();
  const peersList = db.isEnabled() ? null : state.peers;
  const idx = peersList ? state.peers.findIndex(p => p.publicKey === publicKey) : -1;
  const checkExists = db.isEnabled()
    ? db.getPeerByPublicKey(publicKey).then(p => !!p)
    : Promise.resolve(idx !== -1);

  checkExists.then(exists => {
    if (!exists) return res.status(404).json({ error: 'Peer not found in state' });
    try {
      if (USE_CONFIG_FILE) {
        removePeerFromConfigFile(publicKey);
      } else {
        removePeerWithWgSet(publicKey);
      }
    } catch (err) {
      console.error('Failed to remove peer:', err.message);
      return res.status(500).json({ error: 'Failed to remove peer', detail: err.message });
    }
    if (db.isEnabled()) {
      return db.deletePeer(publicKey).then(() => {
        console.log('Revoked peer', publicKey.slice(0, 8) + '...');
        res.json({ ok: true });
      });
    }
    state.peers.splice(idx, 1);
    saveState();
    console.log('Revoked peer', publicKey.slice(0, 8) + '...');
    res.json({ ok: true });
  }).catch(err => {
    console.error('DELETE peer error:', err);
    res.status(500).json({ error: 'Failed to revoke peer' });
  });
});

loadState();

async function start() {
  if (db.isEnabled()) {
    try {
      await db.ensureSchema();
      console.log('MySQL connected and schema ready');
    } catch (err) {
      console.error('MySQL init failed:', err.message);
      process.exit(1);
    }
  }
  app.listen(PORT, '0.0.0.0', () => {
    console.log('NovaVPN provisioning API listening on port', PORT, '(0.0.0.0)');
    console.log('⚠️  Server is running on HTTP (not HTTPS)');
    console.log('   Access at: http://' + (process.env.WG_ENDPOINT_HOST || 'localhost') + ':' + PORT);
    console.log('POST /provision — API for app');
    if (db.isEnabled()) console.log('POST /report-location — App location reports (saved to MySQL)');
    if (fs.existsSync(PUBLIC_DIR)) console.log('GET / — Management UI');
    if (ADMIN_PASSWORD) console.log('Admin UI protected with Basic auth');
    if (!WG_SERVER_PUBLIC_KEY) console.warn('Set WG_SERVER_PUBLIC_KEY in .env');
  });
}

start();
