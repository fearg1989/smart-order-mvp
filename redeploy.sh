#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# redeploy.sh — Re-build frontend and upload to Hostinger via FTP
#
# Usage:
#   ./redeploy.sh <NGROK_URL> <FTP_USER> <FTP_PASS> <FTP_HOST>
#
# Example:
#   ./redeploy.sh \
#     https://parrot-genuine-rarely.ngrok-free.app \
#     u123456789 \
#     mypassword \
#     ftp.tudominio.com
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

NGROK_URL="${1:-}"
FTP_USER="${2:-}"
FTP_PASS="${3:-}"
FTP_HOST="${4:-}"

# ── Validation ────────────────────────────────────────────────────────────────
if [[ -z "$NGROK_URL" || -z "$FTP_USER" || -z "$FTP_PASS" || -z "$FTP_HOST" ]]; then
  echo "Error: faltan argumentos."
  echo ""
  echo "Uso: $0 <NGROK_URL> <FTP_USER> <FTP_PASS> <FTP_HOST>"
  echo ""
  echo "Ejemplo:"
  echo "  $0 https://parrot-genuine-rarely.ngrok-free.app u123456 mipassword ftp.tudominio.com"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$SCRIPT_DIR/frontend"
DIST_DIR="$FRONTEND_DIR/dist"

# ── Step 1: Write PUBLIC_API_URL ──────────────────────────────────────────────
echo "→ [1/3] Escribiendo PUBLIC_API_URL=$NGROK_URL en frontend/.env ..."
{
  # Keep any existing vars except PUBLIC_API_URL, then append the new one
  grep -v '^PUBLIC_API_URL=' "$FRONTEND_DIR/.env" 2>/dev/null || true
  echo "PUBLIC_API_URL=$NGROK_URL"
} > "$FRONTEND_DIR/.env.tmp"
mv "$FRONTEND_DIR/.env.tmp" "$FRONTEND_DIR/.env"

# ── Step 2: Build ─────────────────────────────────────────────────────────────
echo "→ [2/3] Construyendo frontend ..."
cd "$FRONTEND_DIR"
npm run build
cd "$SCRIPT_DIR"

if [[ ! -d "$DIST_DIR" ]]; then
  echo "Error: el directorio dist/ no existe después del build."
  exit 1
fi

# ── Step 3: Upload via FTP ────────────────────────────────────────────────────
echo "→ [3/3] Subiendo $DIST_DIR a ftp://$FTP_HOST/public_html/ ..."

if ! command -v lftp &>/dev/null; then
  echo ""
  echo "⚠  lftp no está instalado. Instálalo con: brew install lftp"
  echo ""
  echo "O sube manualmente el contenido de frontend/dist/ a public_html/ en Hostinger."
  exit 1
fi

lftp -e "
  set ftp:ssl-allow yes;
  set ssl:verify-certificate no;
  open ftp://$FTP_USER:$FTP_PASS@$FTP_HOST;
  mirror -R --delete $DIST_DIR/ /public_html/;
  bye
"

echo ""
echo "✓ Deploy completado."
echo "  Frontend: https://$FTP_HOST"
echo "  Backend:  $NGROK_URL"
