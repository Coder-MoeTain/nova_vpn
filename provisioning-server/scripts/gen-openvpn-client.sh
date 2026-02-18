#!/bin/bash
# Generates a new OpenVPN client and prints inline .ovpn to stdout.
# Usage: ./gen-openvpn-client.sh <client_name> <server_host> <server_port> [easyrsa_dir]
# Requires: easy-rsa initialized (build-ca done).
NAME="${1:-client}"
SERVER_HOST="${2:-127.0.0.1}"
SERVER_PORT="${3:-1194}"
# Prefer env (set by Node); fallback to 4th argument
EASYRSA_DIR="${EASYRSA_DIR:-$4}"
if [ -z "$EASYRSA_DIR" ] || [ ! -d "$EASYRSA_DIR" ]; then
  echo "EASYRSA_DIR not set or missing. Set it to your easy-rsa path (e.g. /etc/openvpn/easy-rsa)." >&2
  exit 1
fi
cd "$EASYRSA_DIR" || exit 1
if [ -x ./easyrsa ]; then
  EASYRSA_CMD=./easyrsa
elif command -v easyrsa >/dev/null 2>&1; then
  EASYRSA_CMD=easyrsa
else
  echo "easyrsa not found in $EASYRSA_DIR or PATH" >&2
  exit 1
fi
# Some installs require vars to be sourced (e.g. openvpn-install / Angristan)
if [ -f vars ]; then
  set -a
  # shellcheck source=/dev/null
  . ./vars 2>/dev/null || true
  set +a
fi
CLIENT_CN="nova_${NAME}"
# Pipe 'yes' so easyrsa does not prompt for "Type the word 'yes' to continue" (non-interactive)
build_out="$(echo "yes" | $EASYRSA_CMD build-client-full "$CLIENT_CN" nopass 2>&1)" || {
  echo "easyrsa build-client-full failed: $build_out" >&2
  exit 1
}
# Easy-RSA 3 uses pki/issued/; some setups use pki/issued/ relative to EASYRSA_PKI or cwd
CRT_PATH=""
for try in "pki/issued/${CLIENT_CN}.crt" "${EASYRSA_PKI:-pki}/issued/${CLIENT_CN}.crt"; do
  if [ -f "$try" ]; then CRT_PATH="$try"; break; fi
done
if [ -z "$CRT_PATH" ]; then
  echo "easyrsa did not create pki/issued/${CLIENT_CN}.crt (build output: $build_out)" >&2
  exit 1
fi
# Resolve paths: EASYRSA_PKI may be set by vars
PKI_DIR="${EASYRSA_PKI:-$EASYRSA_DIR/pki}"
[ -d "$PKI_DIR" ] || PKI_DIR="$EASYRSA_DIR/pki"
[ -d "$PKI_DIR" ] || PKI_DIR="pki"
CA="$(cat "$PKI_DIR/ca.crt")"
CERT="$(cat "$PKI_DIR/issued/${CLIENT_CN}.crt")"
KEY="$(cat "$PKI_DIR/private/${CLIENT_CN}.key")"
echo "client
dev tun
proto udp
remote ${SERVER_HOST} ${SERVER_PORT}
resolv-retry infinite
nobind
persist-key
persist-tun
remote-cert-tls server
cipher AES-256-GCM
auth SHA256
verb 3
key-direction 1

<ca>
${CA}
</ca>

<cert>
${CERT}
</cert>

<key>
${KEY}
</key>
"
