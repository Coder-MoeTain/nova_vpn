# OpenVPN server is installed — what to do next

You already have the OpenVPN server running. Follow these steps so the NovaVPN app can get configs from your server.

---

## 1. Find or set up easy-rsa (for generating client configs)

The provisioning server uses **easy-rsa** to create a new client cert for each device. You need the path to your easy-rsa directory.

**If you used a script (e.g. openvpn-install / Angristan):**  
easy-rsa is often at:
- `/etc/openvpn/easy-rsa`, or  
- `/root/easy-rsa`, or  
- inside your OpenVPN install directory.

**If easy-rsa is not set up yet:**
```bash
sudo apt install easy-rsa
sudo mkdir -p /etc/openvpn/easy-rsa
sudo cp -r /usr/share/easy-rsa/* /etc/openvpn/easy-rsa/
cd /etc/openvpn/easy-rsa
sudo ./easyrsa init-pki
sudo ./easyrsa build-ca          # enter a name when asked (e.g. "NovaVPN-CA")
sudo ./easyrsa gen-dh            # if your server config needs it
```

**Create at least one client** (so the script’s `./easyrsa` and paths are valid):
```bash
sudo ./easyrsa build-client-full device_test nopass
```

Note the **full path** to this directory (e.g. `/etc/openvpn/easy-rsa`). You’ll use it as `OPENVPN_EASYRSA_DIR`.

---

## 2. Deploy the NovaVPN provisioning server on the same machine

On the **same server** (or a machine that can run the script and read easy-rsa):

```bash
cd /path/to/nova_vpn/provisioning-server
npm install
```

---

## 3. Configure environment for OpenVPN

Create or edit `.env`:

```bash
cp ../.env.example .env   # or from repo root: cp .env.example .env
nano .env
```

**Set these for OpenVPN:**

| Variable | Example | What to use |
|----------|---------|-------------|
| `PORT` | `3000` | Port for the provisioning API. |
| `OPENVPN_ENABLED` | `1` | **Must be `1`** to enable OpenVPN provisioning. |
| `OPENVPN_SERVER_HOST` | `76.13.189.118` | Your server’s **public IP or hostname** (what phones will use in the .ovpn `remote`). |
| `OPENVPN_SERVER_PORT` | `1194` | Your OpenVPN server’s UDP port. |
| `OPENVPN_EASYRSA_DIR` | `/etc/openvpn/easy-rsa` | **Full path** to the easy-rsa directory from step 1. |

**Minimal `.env` for OpenVPN-only:**
```env
PORT=3000
OPENVPN_ENABLED=1
OPENVPN_SERVER_HOST=YOUR_SERVER_PUBLIC_IP
OPENVPN_SERVER_PORT=1194
OPENVPN_EASYRSA_DIR=/etc/openvpn/easy-rsa
```

You do **not** need any `WG_*` variables if you’re only using OpenVPN.

---

## 4. Make the client script executable and run the server

The server calls `scripts/gen-openvpn-client.sh` to generate each client config.

```bash
chmod +x scripts/gen-openvpn-client.sh
```

**Run the provisioning server.**  
Because easy-rsa is often under `/etc/openvpn` (root-owned), run with sudo:

```bash
sudo node server.js
```

You should see something like:
```text
NovaVPN provisioning API listening on port 3000
```

If you see *"OpenVPN provisioning not configured"* when the app connects, check that `OPENVPN_EASYRSA_DIR` is correct and that `scripts/gen-openvpn-client.sh` exists and is executable.

---

## 5. Open firewall for provisioning API (and OpenVPN if needed)

- **TCP 3000** — so phones can reach the provisioning API (e.g. `http://YOUR_IP:3000`).
- **UDP 1194** (or your OpenVPN port) — so phones can connect to the OpenVPN server (if not already open).

Example (ufw):
```bash
sudo ufw allow 3000/tcp
sudo ufw allow 1194/udp
sudo ufw reload
```

---

## 6. Point the NovaVPN app at your server

In the Android project, set the provisioning URL to your server:

**File:** `app/build.gradle.kts`  
**Line:** `buildConfigField("String", "PROVISIONING_BASE_URL", ...)`

Set it to your server’s URL, for example:
```kotlin
buildConfigField("String", "PROVISIONING_BASE_URL", "\"http://YOUR_SERVER_IP:3000\"")
```
Replace `YOUR_SERVER_IP` with your server’s real public IP or hostname. Rebuild the app.

---

## 7. On the phone

1. Install an OpenVPN client (e.g. **OpenVPN for Android** from the Play Store).
2. In NovaVPN: open **Settings** → set **VPN protocol** to **OpenVPN**.
3. Tap **Connect**.  
   The app will call `POST http://YOUR_IP:3000/provision-openvpn`, get a new .ovpn config, and open it (e.g. in OpenVPN for Android). Connect from that app.

Each time a **new** device (or a cleared cache) taps Connect, the server generates a new client (device_1, device_2, …) so multiple devices can connect at the same time.

---

## Troubleshooting

| Problem | What to check |
|--------|----------------|
| App says "OpenVPN provisioning not configured" or 503 | `OPENVPN_ENABLED=1`, `OPENVPN_EASYRSA_DIR` set and path exists, `scripts/gen-openvpn-client.sh` present and executable. |
| 500 "Failed to generate OpenVPN config" | Run the script by hand: `sudo bash scripts/gen-openvpn-client.sh device_1 YOUR_SERVER_IP 1194 /etc/openvpn/easy-rsa`. Fix any missing `easyrsa` or path errors. On some systems use `easyrsa` instead of `./easyrsa` in the script. |
| App can’t reach server | Phone and server on same network? Firewall allows TCP 3000? `PROVISIONING_BASE_URL` uses the correct IP/host and port? |
| OpenVPN app connects but no internet | On the server: IP forwarding enabled, NAT/iptables for the OpenVPN subnet (e.g. `10.8.0.0/24`), and `push "redirect-gateway"` (or equivalent) in server config. |

For full server setup (including OpenVPN server install), see [SERVER_SETUP.md](SERVER_SETUP.md).
