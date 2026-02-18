# NovaVPN — Server-side configuration guide

This guide walks through configuring the server for **WireGuard** (in-app tunnel) and/or **OpenVPN** (config export).

---

## Part 1: WireGuard + provisioning API

### 1.1 Install WireGuard on the server

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install wireguard
```

**Create server config** (one-time):
```bash
sudo mkdir -p /etc/wireguard
cd /etc/wireguard
sudo wg genkey | tee server_private.key | wg pubkey > server_public.key
sudo chmod 600 server_private.key
```

**Get the server public key** (you need it for the provisioning server):
```bash
sudo cat /etc/wireguard/server_public.key
# Or: sudo wg show wg0 public-key   (after interface is up)
```

**Example `/etc/wireguard/wg0.conf`** (edit to match your server IP and key):
```ini
[Interface]
Address = 10.66.66.1/24
ListenPort = 64288
PrivateKey = <paste contents of server_private.key>
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE

# Peers are added by the provisioning server (wg set) or add manually:
# [Peer]
# PublicKey = <client_public_key>
# AllowedIPs = 10.66.66.2/32
```

Start WireGuard:
```bash
sudo systemctl enable wg-quick@wg0
sudo systemctl start wg-quick@wg0
sudo wg show wg0   # check it's running
```

**Firewall:** Open the WireGuard **UDP** port (e.g. 64288). Many clouds only allow TCP 22/80/443 by default.
```bash
# Example (ufw):
sudo ufw allow 64288/udp
sudo ufw reload
```

---

### 1.2 Install and configure the provisioning server (WireGuard)

On the **same machine** as WireGuard:

```bash
cd /path/to/nova_vpn/provisioning-server
cp .env.example .env
nano .env   # or vim
```

**Edit `.env` — minimum for WireGuard:**

| Variable | Example | What to set |
|----------|---------|-------------|
| `PORT` | `3000` | HTTP port for the API. |
| `WG_ENDPOINT_HOST` | `76.13.189.118` | Your server’s **public IP or hostname** (what phones use to connect to WireGuard). |
| `WG_ENDPOINT_PORT` | `64288` | Same as `ListenPort` in `wg0.conf`. |
| `WG_SERVER_PUBLIC_KEY` | (paste from `server_public.key`) | From `sudo cat /etc/wireguard/server_public.key`. |
| `WG_INTERFACE` | `wg0` | Interface name. |
| `WG_NETWORK_IPv4` | `10.66.66` | First three octets of client IPs (e.g. clients get 10.66.66.2, .3, .4). |
| `WG_NEXT_CLIENT_INDEX` | `3` | If you already have one peer at .2, use `3` so new devices get .3, .4, … |

**Run as root** (so `wg set` can add peers):
```bash
npm install
sudo node server.js
# Or: sudo npm start
```

**Firewall:** Open **TCP 3000** if phones are not on the same LAN (so the app can reach the API).

**Point the app at this server:**  
In the Android project, set in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "PROVISIONING_BASE_URL", "\"http://YOUR_SERVER_IP:3000\"")
```
Use your server’s real IP or hostname. For production, use HTTPS (e.g. behind nginx/Caddy).

---

### 1.3 Optional: config-file mode (no root)

If you **cannot** run the provisioning server as root:

1. In `.env` set:
   - `WG_USE_CONFIG_FILE=1`
   - `WG_CONFIG_PATH=/etc/wireguard/wg0.conf`
2. Ensure the Node process can **write** to that path (e.g. run as a user that has write access, or use a copy and then reload).
3. After each provision you must **reload** WireGuard yourself, e.g.:
   ```bash
   sudo wg syncconf wg0 <(sudo wg-quick strip wg0)
   ```

---

## Part 2: OpenVPN + provisioning API

Use this if the app is set to **OpenVPN** in Settings (app fetches a config and opens it in an OpenVPN app).

### 2.1 Install OpenVPN and easy-rsa

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openvpn easy-rsa
```

**Set up PKI (one-time):**
```bash
sudo mkdir -p /etc/openvpn/easy-rsa
sudo cp -r /usr/share/easy-rsa/* /etc/openvpn/easy-rsa/
cd /etc/openvpn/easy-rsa
sudo ./easyrsa init-pki
sudo ./easyrsa build-ca          # enter a CA name when asked
sudo ./easyrsa gen-dh            # optional for TLS
sudo ./easyrsa gen-crl
```

**Create server key and cert:**
```bash
sudo ./easyrsa build-server-full server nopass
```

**Example server config** `/etc/openvpn/server.conf` (adjust paths and IPs):
```conf
port 1194
proto udp
dev tun
ca /etc/openvpn/easy-rsa/pki/ca.crt
cert /etc/openvpn/easy-rsa/pki/issued/server.crt
key /etc/openvpn/easy-rsa/pki/private/server.key
dh /etc/openvpn/easy-rsa/pki/dh.pem
server 10.8.0.0 255.255.255.0
push "redirect-gateway def1 bypass-dhcp"
push "dhcp-option DNS 1.1.1.1"
keepalive 10 120
persist-key
persist-tun
status openvpn-status.log
verb 3
```

Start OpenVPN:
```bash
sudo systemctl enable openvpn@server
sudo systemctl start openvpn@server
```

**Firewall:** Open **UDP 1194** (or whatever port you use).

---

### 2.2 Configure the provisioning server for OpenVPN

In the **same** `provisioning-server` directory:

1. **Copy and make the script executable:**
   ```bash
   chmod +x scripts/gen-openvpn-client.sh
   ```

2. **Edit `.env`** and set:
   | Variable | Example | Description |
   |----------|---------|-------------|
   | `OPENVPN_ENABLED` | `1` | Enables OpenVPN provisioning. |
   | `OPENVPN_SERVER_HOST` | `76.13.189.118` | Your server’s public IP/hostname (for the .ovpn `remote`). |
   | `OPENVPN_SERVER_PORT` | `1194` | OpenVPN port. |
   | `OPENVPN_EASYRSA_DIR` | `/etc/openvpn/easy-rsa` | Path to your easy-rsa directory. |

   With `OPENVPN_ENABLED=1`, the server **skips** the WireGuard `wg show` check at startup, so you can run **OpenVPN-only** (no WireGuard needed).

3. **Run the provisioning server** (does **not** need root for OpenVPN; the script will need to run `easyrsa` — either run as root or give the Node user access to easy-rsa and `sudo` for the script):
   ```bash
   node server.js
   ```
   If the script is under a root-owned path, you may need:
   ```bash
   sudo node server.js
   ```

4. **Test:**  
   `POST http://YOUR_SERVER:3000/provision-openvpn` (no body) should return `{ "config": "<long .ovpn string>" }`.

---

## Part 3: Summary checklist

### WireGuard only
- [ ] WireGuard installed and `wg0` up (`sudo wg show wg0`).
- [ ] UDP port (e.g. 64288) open in firewall.
- [ ] `.env`: `WG_SERVER_PUBLIC_KEY`, `WG_ENDPOINT_HOST`, `WG_ENDPOINT_PORT`, `WG_NEXT_CLIENT_INDEX`.
- [ ] Run provisioning server **as root**: `sudo node server.js`.
- [ ] TCP 3000 open if phones are remote.
- [ ] App’s `PROVISIONING_BASE_URL` points to `http://YOUR_SERVER:3000`.

### OpenVPN only
- [ ] OpenVPN + easy-rsa installed; PKI inited; CA and server cert built.
- [ ] OpenVPN server running (e.g. `systemctl start openvpn@server`).
- [ ] UDP 1194 (or your port) open in firewall.
- [ ] `.env`: `OPENVPN_ENABLED=1`, `OPENVPN_SERVER_HOST`, `OPENVPN_SERVER_PORT`, `OPENVPN_EASYRSA_DIR`.
- [ ] `scripts/gen-openvpn-client.sh` executable; server can run it (e.g. `sudo node server.js` if easy-rsa is under `/etc/openvpn`).
- [ ] App’s `PROVISIONING_BASE_URL` points to this server; in app Settings choose **OpenVPN**.

### Both
- Set both WireGuard and OpenVPN variables in `.env`. Do **not** set `OPENVPN_ENABLED=1` if you want the WireGuard check at startup; if you set it, the server skips the `wg` check and can still serve WireGuard provisioning.

---

## Quick reference: .env variables

```bash
# === Provisioning API ===
PORT=3000

# === WireGuard ===
WG_ENDPOINT_HOST=your.server.ip
WG_ENDPOINT_PORT=64288
WG_SERVER_PUBLIC_KEY=<from: wg show wg0 public-key>
WG_INTERFACE=wg0
WG_NETWORK_IPv4=10.66.66
WG_NETWORK_IPv6=fd42:42:42
WG_NEXT_CLIENT_INDEX=3
WG_DNS=1.1.1.1,1.0.0.1
WG_ALLOWED_IPS=0.0.0.0/0, ::/0
WG_PERSISTENT_KEEPALIVE=25

# === OpenVPN (optional) ===
OPENVPN_ENABLED=1
OPENVPN_SERVER_HOST=your.server.ip
OPENVPN_SERVER_PORT=1194
OPENVPN_EASYRSA_DIR=/etc/openvpn/easy-rsa

# === Optional: protect management UI ===
# ADMIN_USERNAME=admin
# ADMIN_PASSWORD=secret
```
