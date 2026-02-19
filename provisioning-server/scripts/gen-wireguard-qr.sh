#!/bin/bash
# Generate a new WireGuard client config and show a QR code for the official WireGuard app.
# Run from the server (as root). Usage: sudo bash gen-wireguard-qr.sh [INDEX]
# If INDEX is omitted, uses nextClientIndex from state.json (and increments it), or 10 if no state.
# Requires: wireguard-tools, qrencode (apt install qrencode). Without qrencode, prints config only.

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Project dir: prefer repo root .env, then provisioning-server/.env (single .env for project)
if [ -f "$SCRIPT_DIR/../.env" ]; then
  PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
elif [ -f "$SCRIPT_DIR/../../.env" ]; then
  PROJECT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
elif [ -f "$SCRIPT_DIR/.env" ]; then
  PROJECT_DIR="$SCRIPT_DIR"
else
  echo "No .env found in $SCRIPT_DIR/.. or repo root $SCRIPT_DIR/../.."
  exit 1
fi
cd "$PROJECT_DIR"

# Load .env (allow spaces around = and strip CRLF)
while IFS= read -r line; do
  line="${line%%#*}"
  line="${line%"${line##*[![:space:]]}"}"
  [ -z "$line" ] && continue
  if [[ "$line" =~ ^([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*=[[:space:]]*(.*)$ ]]; then
    key="${BASH_REMATCH[1]}"
    val="${BASH_REMATCH[2]%$'\r'}"
    export "$key=$val"
  fi
done < <(tr -d '\r' < .env 2>/dev/null || cat .env)

WG_NET="${WG_NETWORK_IPv4:-10.66.66}"
WG_PORT="${WG_ENDPOINT_PORT:-64288}"
WG_HOST="${WG_ENDPOINT_HOST:-}"
WG_SERVER_PUBLIC_KEY="${WG_SERVER_PUBLIC_KEY:-}"
WG_INTERFACE="${WG_INTERFACE:-wg0}"
WG_DNS="${WG_DNS:-1.1.1.1,1.0.0.1}"
STATE_FILE="${STATE_FILE:-$PROJECT_DIR/state.json}"

if [ -z "$WG_SERVER_PUBLIC_KEY" ] || [ -z "$WG_HOST" ]; then
  echo "Set WG_SERVER_PUBLIC_KEY and WG_ENDPOINT_HOST in .env (loaded from $PROJECT_DIR/.env)"
  echo "  WG_ENDPOINT_HOST='$WG_HOST'  WG_SERVER_PUBLIC_KEY length=${#WG_SERVER_PUBLIC_KEY}"
  exit 1
fi

# Resolve client index
if [ -n "$1" ]; then
  INDEX="$1"
else
  if [ -f "$STATE_FILE" ] && command -v jq &>/dev/null; then
    INDEX=$(jq -r '.nextClientIndex // 10' "$STATE_FILE")
    # Increment so next provision doesn't clash
    jq '.nextClientIndex += 1' "$STATE_FILE" > "${STATE_FILE}.tmp" && mv "${STATE_FILE}.tmp" "$STATE_FILE"
  else
    INDEX=10
  fi
fi

CLIENT_IP="${WG_NET}.${INDEX}/32"
TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" EXIT

# Generate client key pair
wg genkey | tee "$TMP_DIR/private.key" | wg pubkey > "$TMP_DIR/public.key"
chmod 600 "$TMP_DIR/private.key"
PRIVATE_KEY=$(cat "$TMP_DIR/private.key")
PUBLIC_KEY=$(cat "$TMP_DIR/public.key")

# Add peer to WireGuard
wg set "$WG_INTERFACE" peer "$PUBLIC_KEY" allowed-ips "$CLIENT_IP"
echo "Added peer $CLIENT_IP to $WG_INTERFACE"

# Build config (wg-quick format for official app)
CONFIG="[Interface]
PrivateKey = $PRIVATE_KEY
Address = $CLIENT_IP
DNS = $WG_DNS

[Peer]
PublicKey = $WG_SERVER_PUBLIC_KEY
AllowedIPs = 0.0.0.0/0
Endpoint = $WG_HOST:$WG_PORT
PersistentKeepalive = 25"

echo ""
echo "--- Config (save this if you need it) ---"
echo "$CONFIG"
echo ""
echo "--- QR code (scan with official WireGuard app: Add tunnel → Scan from QR code) ---"
if command -v qrencode &>/dev/null; then
  echo "$CONFIG" | qrencode -t ansiutf8
  QR_PNG="$PROJECT_DIR/wireguard-qr-$INDEX.png"
  echo "$CONFIG" | qrencode -o "$QR_PNG"
  echo ""
  echo "QR saved to: $QR_PNG  (open on another device or transfer to phone to scan)"
else
  echo "Install qrencode to show QR: apt install qrencode"
  echo "Or paste the config above into the official app: Add tunnel → Create from scratch"
fi
echo ""
