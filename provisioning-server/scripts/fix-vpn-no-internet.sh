#!/bin/bash
# Run on the WireGuard server when VPN shows "Connected" but phone has no internet.
# Usage: sudo bash fix-vpn-no-internet.sh

set -e
[ "$(id -u)" -ne 0 ] && { echo "Run as root: sudo bash fix-vpn-no-internet.sh"; exit 1; }

WG_NET="10.66.66.0/24"
OUT_IF=$(ip route show default | awk '/default/ {print $5}')
[ -z "$OUT_IF" ] && { echo "Could not detect default interface."; exit 1; }

echo "=== Fix VPN no-internet (outbound interface: $OUT_IF) ==="

# 1. IP forwarding and relax reverse-path filter (often blocks forwarded VPN traffic)
sysctl -w net.ipv4.ip_forward=1
sysctl -w net.ipv4.conf.all.rp_filter=0
sysctl -w net.ipv4.conf.default.rp_filter=0
sysctl -w net.ipv4.conf.wg0.rp_filter=0 2>/dev/null || true
cat > /etc/sysctl.d/99-wireguard.conf << 'SYSCTL'
net.ipv4.ip_forward=1
net.ipv4.conf.all.rp_filter=0
net.ipv4.conf.default.rp_filter=0
SYSCTL
echo "[OK] ip_forward=1, rp_filter relaxed"

# 2. iptables: NAT and FORWARD (in case PostUp didn't run or was overwritten)
iptables -C FORWARD -i wg0 -j ACCEPT 2>/dev/null || iptables -A FORWARD -i wg0 -j ACCEPT
iptables -C FORWARD -i "$OUT_IF" -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT 2>/dev/null || iptables -A FORWARD -i "$OUT_IF" -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT
iptables -t nat -C POSTROUTING -s $WG_NET -o "$OUT_IF" -j MASQUERADE 2>/dev/null || iptables -t nat -A POSTROUTING -s $WG_NET -o "$OUT_IF" -j MASQUERADE
echo "[OK] iptables FORWARD + NAT"

# 3. UFW: allow route both ways
if command -v ufw &>/dev/null && ufw status 2>/dev/null | grep -q "Status: active"; then
  ufw route allow in on wg0 out on "$OUT_IF"
  ufw route allow in on "$OUT_IF" out on wg0
  grep -q 'net/ipv4/ip_forward=1' /etc/ufw/sysctl.conf 2>/dev/null || echo 'net/ipv4/ip_forward=1' >> /etc/ufw/sysctl.conf
  ufw reload
  echo "[OK] UFW route allow wg0 <-> $OUT_IF"
else
  echo "[SKIP] UFW not active"
fi

echo ""
echo "=== Verify ==="
echo "ip_forward: $(sysctl -n net.ipv4.ip_forward)"
echo "FORWARD chain (first 5):"
iptables -L FORWARD -n -v | head -5
echo "NAT POSTROUTING:"
iptables -t nat -L POSTROUTING -n -v
echo ""
echo "Done. Disconnect and reconnect the VPN in the app, then try the internet."
echo ""
echo "If still no internet: run 'tcpdump -i wg0 -n' and on the phone load a webpage."
echo "  - If you see packets: traffic reaches the server; check NAT/routing."
echo "  - If no packets: phone may not be sending traffic through the VPN (try another app or clear VPN config)."
