# NovaVPN

<p align="center">
  <strong>NovaVPN</strong>
</p>
<p align="center">
  Android app with a full in-app VPN client using WireGuard.
</p>
<p align="center">
  <sub>Kotlin · Jetpack Compose · Material 3 · WireGuard</sub>
</p>

---

## Table of Contents

- [About](#about)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Verifying the VPN](#verifying-the-vpn)
- [Troubleshooting](#troubleshooting)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Provisioning API](#provisioning-api)
- [License](#license)

---

## About

NovaVPN is an Android app that **connects you to a VPN inside the app** using the **WireGuard** protocol. One tap: the app gets a unique key and config from your provisioning server and establishes the tunnel. No separate VPN app needed.

- **Target:** Android 8+ (API 26+)
- **In-app VPN:** Uses the official [WireGuard Android tunnel library](https://www.wireguard.com/embedding/) so the tunnel runs inside NovaVPN.
- **Server:** The included Node.js provisioning server adds WireGuard peers and returns config; you run a WireGuard server (e.g. on the same host).

---

## Requirements

- Android Studio (latest stable)
- Android device with **API 26+**
- A **provisioning server** (included under `provisioning-server/`) and a **WireGuard server** (see provisioning-server README)

---

## Getting Started

### 1. Clone and open

```bash
git clone <your-repo-url>
cd nova_vpn
```

Open the project in Android Studio and let Gradle sync.

### 2. Set the provisioning server URL

In `app/build.gradle.kts`, set your server’s URL:

```kotlin
buildConfigField("String", "PROVISIONING_BASE_URL", "\"http://YOUR_SERVER_IP:3000\"")
```

### 3. Run the provisioning server and WireGuard

See [provisioning-server/README.md](provisioning-server/README.md). The server must have **WireGuard** set up and respond to `POST /provision` with a WireGuard config (endpoint, server public key, client address, DNS, etc.). The app sends its public key and receives the config.

### 4. Run the app on device

1. Run the NovaVPN app from Android Studio.
2. Tap **Connect** — grant VPN permission if prompted, then the app will provision (if needed) and establish the WireGuard tunnel.
3. Tap **Disconnect** to bring the tunnel down.

---

## Project Structure

```
nova_vpn/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/novavpn/app/
│       │   ├── api/          # Provisioning API (WireGuard + legacy OpenVPN)
│       │   ├── data/         # Repositories, models
│       │   ├── security/     # Secure storage (keys, config cache)
│       │   ├── ui/           # Compose screens, theme
│       │   ├── util/         # Logger
│       │   ├── viewmodel/    # VpnViewModel, Settings, Logs
│       │   └── vpn/          # NovaTunnel, WireGuard config, BootReceiver, VpnTileService
│       └── res/
├── provisioning-server/      # Node.js WireGuard (and optional OpenVPN) provisioning
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## Configuration

Set **PROVISIONING_BASE_URL** in `app/build.gradle.kts` to your provisioning server (e.g. `http://YOUR_SERVER_IP:3000`). The app calls `POST {PROVISIONING_BASE_URL}/provision` with `{ "publicKey": "<base64>" }` and expects a JSON config (endpointHost, endpointPort, serverPublicKey, clientAddress, dns, allowedIPs, persistentKeepalive). See [Provisioning API](#provisioning-api).

---

## Verifying the VPN

1. In the app: Status shows **Connected** after Connect.
2. In the browser: Open [ifconfig.me](https://ifconfig.me) — the IP should be the VPN server’s exit IP.
3. **System:** Settings → Network & internet → VPN — NovaVPN should appear as connected.

---

## Troubleshooting

| Problem | What to try |
|--------|-------------|
| **Provisioning failed** | Ensure the provisioning server is running and reachable. Open **TCP 3000** on the server firewall. Set **PROVISIONING_BASE_URL** correctly. Server must have **WG_SERVER_PUBLIC_KEY** and WireGuard set up. |
| **Connection timed out** | Check server reachability. Open the **WireGuard port** (e.g. UDP 51820) on the server. |
| **VPN permission** | When first connecting, Android will ask for VPN permission; approve so the app can create the tunnel. |
| **Quick Settings tile** | Add the **NovaVPN** tile in Quick Settings. Tapping it opens the app and triggers Connect. |
| **New key/config** | In Settings, tap **Clear cached VPN config**, then Connect again to provision a new peer. |
| **Log: "UAPIOpen: mkdir ... permission denied"** | Harmless. The WireGuard tunnel library logs this when it can't use its built-in path; the tunnel still works. You can ignore it. |
| **Handshake retries / "stopped hearing back"** | Tunnel is up but the server may not be reachable on the WireGuard UDP port (e.g. 51820). Open that port on the server firewall and ensure the WireGuard server is listening. Check NAT if the device is behind a router. |
| **No internet when VPN shows "Connected"** | If handshakes succeed (see server `wg show`: "latest handshake" recent) but transfer stays tiny, enable **IP forwarding** and **NAT** on the server (see server checklist). If handshakes never complete, fix server reachability (open UDP port, firewall). |
| **Can't ping or connect to the server** | See [Can't reach the server](#cant-reach-the-server) below. |

### Can't reach the server

If you can't ping the server and the app can't connect (provisioning or VPN fails), fix reachability first.

1. **Ping is often blocked**  
   Many servers and firewalls block **ICMP (ping)**. That's normal — failure to ping does **not** mean the server is down. Test with **TCP** or **UDP** instead (see below).

2. **Use the correct server address**  
   - In the app, `PROVISIONING_BASE_URL` in `app/build.gradle.kts` must be the server’s **public IP** or a hostname that resolves to it (e.g. `http://YOUR_SERVER_PUBLIC_IP:3000`).  
   - Find the server’s public IP by running on the server: `curl -s ifconfig.me`.  
   - If you’re on the **same LAN** as the server, you can use the server’s local IP; from the **internet** (e.g. phone on mobile data), use the **public** IP.

3. **Open ports on the server**  
   The app needs:
   - **TCP 3000** — provisioning API (HTTP).
   - **UDP 64288** (or your `WG_ENDPOINT_PORT`) — WireGuard.

   **On the server (host firewall):**
   ```bash
   sudo ufw allow 3000/tcp
   sudo ufw allow 64288/udp
   sudo ufw reload
   ```
   If the server is a **cloud VM** (AWS, GCP, Azure, DigitalOcean, etc.), also open **TCP 3000** and **UDP 64288** in the provider’s **security group / firewall / network rules** (inbound). Otherwise traffic is dropped before it reaches the server.

4. **Test from your PC or phone**  
   - **Provisioning (TCP 3000):**  
     `curl -v http://YOUR_SERVER_IP:3000/`  
     You should get a response (e.g. 404 or a JSON body), not “Connection refused” or timeout.  
   - **WireGuard (UDP 64288):**  
     `nc -u -v YOUR_SERVER_IP 64288`  
     Then tap Connect in the app; you may see nothing in `nc`, but if the app later completes the handshake, the path works.  
   - **Ping:**  
     `ping YOUR_SERVER_IP`  
     If it times out, that’s OK as long as the `curl` and/or WireGuard tests work.

5. **Server must be running**  
   On the server: provisioning app (e.g. `node server.js` or `pm2`) and WireGuard (`wg show` shows the interface). Ensure the process listening on 3000 is bound to `0.0.0.0`, not only `127.0.0.1`.

### No internet / handshake retries — server checklist

When the app shows Connected but there’s no internet (or logs show handshake retries), the device cannot reach the WireGuard server over UDP. On the **server**:

1. **Open the WireGuard UDP port** in the firewall (the one in `WG_ENDPOINT_PORT`, e.g. **64288** or 51820).  
   Example (Linux): `sudo ufw allow 64288/udp && sudo ufw reload` (or your cloud/VM security group).

2. **WireGuard is running and listening:**  
   `sudo wg show` — you should see the interface and peers.  
   `sudo ss -ulnp | grep wg` (or `netstat -ulnp`) — should show the ListenPort.

3. **IP forwarding and NAT** (so client traffic can reach the internet):  
   - Enable forwarding: `sysctl -w net.ipv4.ip_forward=1` (and `net.ipv6.conf.all.forwarding=1` if using IPv6).  
   - If the server has one public IP, enable NAT/masquerade for traffic from the WireGuard subnet (e.g. `iptables -t nat -A POSTROUTING -s 10.66.66.0/24 -o eth0 -j MASQUERADE`).

4. **Reload peers after provisioning:**  
   The provisioning server adds the peer with `wg set`; ensure WireGuard is running and the new peer appears in `wg show`.

### Handshake never completes — "stopped hearing back"

The phone sends handshake initiations but never gets a reply. Either the server is not replying, or the reply is dropped before it reaches the phone.

**On the server:**

1. **Confirm WireGuard is listening:**  
   `sudo ss -ulnp | grep 64288` (use your `WG_ENDPOINT_PORT`). You should see the wg process.

2. **Confirm firewall allows UDP in and out:**  
   - Inbound: UDP port 64288 must be open (phone → server).  
   - Outbound: UDP is usually allowed by default; if you have a strict `ufw` or `iptables` OUTPUT policy, allow outbound UDP so the server can send handshake responses back.  
   - If the server is a **cloud VM**, open UDP 64288 in the provider’s **security group / firewall** (inbound). Outbound is typically allowed.

3. **Capture packets while the phone connects:**  
   Run: `sudo tcpdump -i any -n udp port 64288 -c 30`  
   Then tap Connect in the app. You should see:
   - **Incoming** packets (phone → server) — if you see these, the phone can reach the server.
   - **Outgoing** packets (server → phone) — if these appear but the phone still doesn’t complete the handshake, something between the server and the phone (carrier, WiFi firewall, NAT) is dropping the reply.

**On the phone / network:**

- **Mobile data (4G/5G):** Some carriers block or throttle **incoming** UDP. Try from a **different WiFi** (e.g. home or another network) to see if handshakes complete there.
- **Strict WiFi** (corporate, school, public hotspot): May block incoming UDP. Try from a home or personal WiFi.

---

## Features

- **Full in-app VPN** — WireGuard tunnel runs inside the app (no external VPN app).
- **One-tap Connect** — Provisions (if needed) and connects; Disconnect to bring the tunnel down.
- **Cached config** — First Connect provisions and caches; later Connects reuse it until you clear in Settings.
- **Multi-device** — Each device gets its own key and config from the server.
- **Settings** — Auto-connect on boot (opens app); clear cached config; Always-on VPN and kill switch guidance.
- **Logs** — App events (no secrets logged).
- **Quick Settings tile** — Tap to open app and connect.

---

## Tech Stack

| Area        | Technology |
|------------|------------|
| **VPN**    | WireGuard ([com.wireguard.android:tunnel](https://search.maven.org/artifact/com.wireguard.android/tunnel)) |
| **Keys**   | [wireguard-keytool](https://github.com/moznion/wireguard-keytool-java) (X25519) |
| **UI**     | Jetpack Compose, Material 3, Navigation Compose |
| **DI**     | Hilt |
| **Storage**| AndroidX Security Crypto (EncryptedSharedPreferences) for keys and config |
| **Network**| Ktor client for provisioning API |

---

## Provisioning API

The app uses **WireGuard** provisioning so each device gets a unique config.

### App behaviour

1. User taps **Connect**.
2. If needed, the app generates a WireGuard key pair and stores the private key securely.
3. The app sends **POST {PROVISIONING_BASE_URL}/provision** with body `{ "publicKey": "<base64>" }`.
4. The server adds the peer to WireGuard and returns a JSON config (endpointHost, endpointPort, serverPublicKey, clientAddress, dns, allowedIPs, persistentKeepalive).
5. The app builds a WireGuard `Config` and brings the tunnel **UP** using the official tunnel library. Traffic goes through the VPN.

### Backend contract (WireGuard)

- **URL:** `POST {baseUrl}/provision`
- **Request (JSON):** `{ "publicKey": "<base64 44-char WireGuard public key>" }`
- **Response (JSON):**
  ```json
  {
    "endpointHost": "vpn.example.com",
    "endpointPort": 51820,
    "serverPublicKey": "<base64>",
    "clientAddress": "10.66.66.2/32,fd42:42:42::2/128",
    "dns": "1.1.1.1, 1.0.0.1",
    "allowedIPs": "0.0.0.0/0, ::/0",
    "persistentKeepalive": 25,
    "presharedKey": null
  }
  ```

The provisioning server in this repo (`provisioning-server/`) implements this when **WG_SERVER_PUBLIC_KEY** and WireGuard are configured. See [provisioning-server/README.md](provisioning-server/README.md).

---

## License

Use and modify as needed for your project.
