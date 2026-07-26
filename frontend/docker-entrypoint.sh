#!/bin/sh
set -eu

cat > /usr/share/nginx/html/env-config.js <<EOF
window.__APP_CONFIG__ = window.__APP_CONFIG__ || {};
window.__APP_CONFIG__.VITE_RAZORPAY_KEY_ID = "${VITE_RAZORPAY_KEY_ID:-}";
EOF

exec "$@"
