#!/bin/bash
# NovaVPN — Install and configure WireGuard on the server (Ubuntu/Debian).
# Run as root on a fresh server, or when WireGuard is not yet set up.
# Usage: copy this script to the server and run: sudo bash install-wireguard-server.sh

set -e

WG_PORT=64288
WG_NET="10.66.66"
WG_CONF="/etc/wireguard/wg0.conf"
KEY_DIR="/etc/wireguard"

echo "=== NovaVPN WireGuard server install ==="

# Must be root (for wg, systemctl, iptables)
if [ "$(id -u)" -ne 0 ]; then
  echo "Run as root: sudo bash install-wireguard-server.sh"
  exit 1
fi

# Install WireGuard
if command -v apt-get &>/dev/null; then
  apt-get update -qq
  apt-get install -y wireguard
elif command -v dnf &>/dev/null; then
  dnf install -y wireguard-tools
elif command -v yum &>/dev/null; then
  yum install -y wireguard-tools
else
  echo "Unsupported OS. Install WireGuard manually and create $WG_CONF"
  exit 1
fi

mkdir -p "$KEY_DIR"
cd "$KEY_DIR"

# Generate server keys only if not present
if [ ! -f server_private.key ]; then
  wg genkey | tee server_private.key | wg pubkey > server_public.key
  chmod 600 server_private.key
  echo "Generated new server keys in $KEY_DIR"
else
  echo "Using existing server keys in $KEY_DIR"
fi

SERVER_PRIVATE=$(cat server_private.key)

# Detect main outbound interface (e.g. eth0, ens3)
OUT_IF=$(ip route show default | awk '/default/ {print $5}')
if [ -z "$OUT_IF" ]; then
  echo "Could not detect default interface. Set OUT_IF manually (e.g. eth0)."
  exit 1
fi
echo "Using outbound interface: $OUT_IF"

# Write wg0.conf (overwrites if re-run)
cat > "$WG_CONF" << EOF
[Interface]
Address = ${WG_NET}.1/24
ListenPort = ${WG_PORT}
PrivateKey = ${SERVER_PRIVATE}
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -s ${WG_NET}.0/24 -o ${OUT_IF} -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -s ${WG_NET}.0/24 -o ${OUT_IF} -j MASQUERADE
EOF

chmod 600 "$WG_CONF"

# Enable IP forwarding
echo "net.ipv4.ip_forward=1" > /etc/sysctl.d/99-wireguard.conf
sysctl -p /etc/sysctl.d/99-wireguard.conf 2>/dev/null || sysctl -w net.ipv4.ip_forward=1

# Start WireGuard
systemctl enable wg-quick@wg0
systemctl restart wg-quick@wg0

# Open firewall if ufw is active
if command -v ufw &>/dev/null && ufw status 2>/dev/null | grep -q "Status: active"; then
  ufw allow ${WG_PORT}/udp
  ufw reload
  echo "Allowed UDP ${WG_PORT} in ufw"
fi

echo ""
echo "=== WireGuard is running ==="
wg show wg0
echo ""
echo "--- Next steps ---"
echo "1. Put the server public key in your provisioning server .env:"
echo "   WG_SERVER_PUBLIC_KEY=$(cat $KEY_DIR/server_public.key)"
echo ""
echo "2. In .env also set: WG_ENDPOINT_HOST=<this server public IP>, WG_ENDPOINT_PORT=${WG_PORT}"
echo ""
echo "3. If this is a cloud VM, open in the provider firewall: TCP 3000, UDP ${WG_PORT}"
echo ""
