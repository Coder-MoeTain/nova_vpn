/**
 * NovaVPN Provisioning API
 *
 * Run on the same machine as your WireGuard server.
 * POST /provision → adds peer and returns config.
 * Web UI at / for listing and revoking peers (optional admin auth).
 */

require('dotenv').config();
const express = require('express');
const fs = require('fs');
const os = require('os');
const path = require('path');
const { execSync } = require('child_process');

const app = express();
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
const WG_DNS = process.env.WG_DNS || '1.1.1.1, 1.0.0.1';
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

app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  if (req.method === 'OPTIONS') return res.sendStatus(204);
  next();
});

// Public API (for the Android app)
app.post('/provision', (req, res) => {
  const publicKey = req.body?.publicKey;
  if (!publicKey || !isValidPublicKey(publicKey)) {
    return res.status(400).json({ error: 'Missing or invalid publicKey (base64, 44 chars)' });
  }
  if (!WG_SERVER_PUBLIC_KEY) {
    return res.status(500).json({ error: 'Server misconfigured: WG_SERVER_PUBLIC_KEY not set' });
  }

  loadState();
  const index = state.nextClientIndex;
  const ipv4Only = `${WG_NETWORK_IPv4}.${index}/32`;
  const clientAddress = getClientAddresses(index);

  try {
    if (USE_CONFIG_FILE) {
      addPeerWithConfigFile(publicKey, ipv4Only);
    } else {
      addPeerWithWgSet(publicKey, ipv4Only);
    }
  } catch (err) {
    console.error('Failed to add peer:', err.message);
    return res.status(500).json({ error: 'Failed to add peer to WireGuard', detail: err.message });
  }

  state.nextClientIndex = index + 1;
  state.peers.push({
    publicKey: publicKey.trim(),
    clientIp: `${WG_NETWORK_IPv4}.${index}`,
    createdAt: new Date().toISOString(),
  });
  saveState();

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

  console.log('Provisioned peer', publicKey.slice(0, 8) + '...', '→', clientAddress);
  res.json(response);
});

app.get('/health', (req, res) => {
  res.json({ ok: true, service: 'novavpn-provisioning' });
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

// Management API and UI (optional auth)
app.use('/api', authMiddleware);
app.use('/', authMiddleware);

app.get('/api/peers', (req, res) => {
  loadState();
  res.json({
    peers: state.peers,
    nextClientIndex: state.nextClientIndex,
    nextClientIp: `${WG_NETWORK_IPv4}.${state.nextClientIndex}`,
  });
});

app.delete('/api/peers', (req, res) => {
  const publicKey = (req.body?.publicKey || req.query?.publicKey || '').trim();
  if (!publicKey || !isValidPublicKey(publicKey)) {
    return res.status(400).json({ error: 'Missing or invalid publicKey' });
  }

  loadState();
  const idx = state.peers.findIndex(p => p.publicKey === publicKey);
  if (idx === -1) {
    return res.status(404).json({ error: 'Peer not found in state' });
  }

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

  state.peers.splice(idx, 1);
  saveState();
  console.log('Revoked peer', publicKey.slice(0, 8) + '...');
  res.json({ ok: true });
});

if (fs.existsSync(PUBLIC_DIR)) {
  app.use(express.static(PUBLIC_DIR));
  app.get('/', (req, res) => {
    res.sendFile(path.join(PUBLIC_DIR, 'index.html'));
  });
}

loadState();

app.listen(PORT, () => {
  console.log('NovaVPN provisioning API listening on port', PORT);
  console.log('POST /provision — API for app');
  if (fs.existsSync(PUBLIC_DIR)) console.log('GET / — Management UI');
  if (ADMIN_PASSWORD) console.log('Admin UI protected with Basic auth');
  if (!WG_SERVER_PUBLIC_KEY) console.warn('Set WG_SERVER_PUBLIC_KEY in .env');
});
