# NovaVPN Provisioning Server

Node.js backend for the NovaVPN app. When a user taps **Connect**, the app sends the device's public key to this server; the server adds a new WireGuard peer and returns the config so the app can connect without any manual key exchange.

## Requirements

- **Node.js 18+**
- This server must run on the **same machine** as your WireGuard server (so it can run `wg set`), or use config-file mode and reload WireGuard yourself.
- WireGuard installed and an interface (e.g. `wg0`) already set up with a working server config.

## Quick start

1. **Copy env and edit:**

   ```bash
   cd provisioning-server
   cp .env.example .env
   # Edit .env:
   # - WG_SERVER_PUBLIC_KEY: run on the WireGuard server: wg show wg0 public-key
   # - WG_ENDPOINT_HOST is already set to 76.13.189.118 (your server); change if different.
   # - WG_ENDPOINT_PORT=64288. Clients will get IPs from 10.66.66.3 (server uses .2).
   ```

2. **Install and run:**

   ```bash
   npm install
   npm start
   ```

3. **Point the app at this server:**  
   In the Android project, set `PROVISIONING_BASE_URL` in `app/build.gradle.kts` to this server's URL, e.g.:
   - `http://YOUR_SERVER_IP:3000` (if the phone can reach that IP and port)
   - or `https://provision.yourdomain.com` if you put this behind nginx/Caddy with HTTPS.

## Configuration (.env)

| Variable | Description |
|----------|-------------|
| `PORT` | HTTP port for this API (default 3000). |
| `WG_ENDPOINT_HOST` | IP or hostname of the WireGuard server (what clients connect to). |
| `WG_ENDPOINT_PORT` | WireGuard UDP port (e.g. 64120). |
| `WG_SERVER_PUBLIC_KEY` | Server's WireGuard public key (base64). Get it with `wg show wg0 public-key`. |
| `WG_INTERFACE` | WireGuard interface name (default `wg0`). |
| `WG_NETWORK_IPv4` | First three octets for client IPs (e.g. `10.66.66` → 10.66.66.2, 10.66.66.3, …). |
| `WG_NETWORK_IPv6` | IPv6 prefix for clients (e.g. `fd42:42:42`). |
| `WG_NEXT_CLIENT_INDEX` | Next client index (default 2 → 10.66.66.2). Increase if you already have peers. |
| `WG_DNS` | DNS servers returned to the app (e.g. `1.1.1.1, 1.0.0.1`). |
| `WG_ALLOWED_IPS` | AllowedIPs for the tunnel (default full tunnel). |
| `WG_PERSISTENT_KEEPALIVE` | Keepalive seconds (default 25). |

### Config-file mode (optional)

If you cannot run `wg` from this process (e.g. different machine or no root):

- Set `WG_USE_CONFIG_FILE=1` and `WG_CONFIG_PATH` to your WireGuard config path.
- The server will **append** a `[Peer]` block to that file instead of running `wg set`.
- You must **reload WireGuard** yourself after each provision (e.g. `wg syncconf wg0 <(wg-quick strip wg0)` or restart the interface).

## Management UI

Open **http://localhost:3000** (or your server URL) in a browser to:

- View all provisioned peers (public key, client IP, created date).
- See the next client IP that will be assigned.
- **Revoke** a peer (removes it from WireGuard and from the list).

If you set `ADMIN_PASSWORD` in `.env`, the UI and the management API require HTTP Basic auth (username from `ADMIN_USERNAME`, default `admin`). The **POST /provision** and **GET /health** endpoints stay public so the Android app can connect without auth.

## API

- **POST /provision** (public)  
  Body: `{ "publicKey": "<base64 WireGuard public key>" }`  
  Returns the config JSON expected by the NovaVPN app.

- **GET /health** (public)  
  Returns `{ "ok": true }`.

- **GET /api/peers** (optional auth)  
  Returns `{ peers: [...], nextClientIndex, nextClientIp }`. Each peer has `publicKey`, `clientIp`, `createdAt`.

- **DELETE /api/peers** (optional auth)  
  Body: `{ "publicKey": "<base64>" }` — revokes the peer (removes from WireGuard and state).

## State

The server stores `state.json` in this directory:

- **nextClientIndex** — next client IP index (e.g. 3 → 10.66.66.3).
- **peers** — list of provisioned peers (`publicKey`, `clientIp`, `createdAt`) for the management UI and revoke.

You can edit or reset `state.json` if needed. Revoking a peer removes it from WireGuard and from this list but does not reuse its IP (nextClientIndex is not decreased).

## Firewall (connection timeout from the app)

The provisioning UI uses **TCP port 3000**; the VPN itself uses **UDP**. If the app shows "Connection timed out", the phone likely cannot reach the WireGuard port:

- **On the server (76.13.189.118):** open **UDP port 64288** (or your `WG_ENDPOINT_PORT`) in the firewall / security group. Many clouds only allow SSH, HTTP, HTTPS by default.
- Ensure WireGuard is running: `sudo wg show wg0`.

## Production

- Run behind a reverse proxy (nginx, Caddy) with **HTTPS** so the app can use `https://...` as `PROVISIONING_BASE_URL`.
- Optionally add auth (e.g. API key header) and restrict CORS.
- Use a process manager (systemd, pm2) to keep the server running.
