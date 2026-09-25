#!/bin/bash
set -euo pipefail

# ══════════════════════════════════════════════════════════════
#  MedTrust – Kubernetes Dashboard Setup Script
#  Installs the official K8s Dashboard + admin access
#
#  Usage:
#    ./setup-dashboard.sh          # Full install (first time / after cluster recreate)
#    ./setup-dashboard.sh start    # Daily quick-start (just token + proxy)
#    ./setup-dashboard.sh token    # Just print the login token
# ══════════════════════════════════════════════════════════════

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DASHBOARD_URL="http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

log()  { echo -e "${CYAN}[INFO]${NC}  $1"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $1"; }
err()  { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ── Install Kubernetes Dashboard ──────────────────────────────
install_dashboard() {
    log "Deploying Kubernetes Dashboard v2.7.0..."

    kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml

    ok "Kubernetes Dashboard manifests applied!"
}

# ── Create Admin User & RBAC ─────────────────────────────────
create_admin_user() {
    log "Creating admin user with cluster-admin privileges..."

    kubectl apply -f "$SCRIPT_DIR/dashboard-admin.yaml"

    ok "Admin user created!"
}

# ── Wait for Dashboard to be Ready ───────────────────────────
wait_for_dashboard() {
    log "Waiting for Dashboard pods to be ready..."

    kubectl wait --for=condition=ready pod \
        -l k8s-app=kubernetes-dashboard \
        -n kubernetes-dashboard \
        --timeout=120s

    ok "Dashboard is ready!"
}

# ── Get Login Token ──────────────────────────────────────────
# The token is LONG-LIVED (kubernetes.io/service-account-token).
# It does NOT expire. Same token works every day as long as
# the Kind cluster exists. No need to regenerate daily.
get_token() {
    log "Retrieving admin login token..."

    TOKEN=$(kubectl get secret admin-user-token \
        -n kubernetes-dashboard \
        -o jsonpath='{.data.token}' | base64 --decode 2>/dev/null) || true

    if [ -z "$TOKEN" ]; then
        # Secret may need a moment to be populated on first create
        sleep 3
        TOKEN=$(kubectl get secret admin-user-token \
            -n kubernetes-dashboard \
            -o jsonpath='{.data.token}' | base64 --decode)
    fi

    echo ""
    echo -e "${BOLD}═══════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}  🔑 Dashboard Login Token (never expires)${NC}"
    echo -e "${BOLD}═══════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "${YELLOW}$TOKEN${NC}"
    echo ""
    echo -e "${BOLD}═══════════════════════════════════════════════════════${NC}"
    echo ""

    # Save token to file for convenience
    echo "$TOKEN" > "$SCRIPT_DIR/.dashboard-token"
    log "Token saved to: $SCRIPT_DIR/.dashboard-token"

    # Copy to clipboard if xclip is available
    if command -v xclip &>/dev/null; then
        echo -n "$TOKEN" | xclip -selection clipboard
        ok "Token copied to clipboard! Just paste it in the login page."
    elif command -v xsel &>/dev/null; then
        echo -n "$TOKEN" | xsel --clipboard
        ok "Token copied to clipboard! Just paste it in the login page."
    fi
}

# ── Start Dashboard Proxy ────────────────────────────────────
start_proxy() {
    echo ""
    echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}  🚀 Kubernetes Dashboard is Ready!${NC}"
    echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${CYAN}Dashboard URL:${NC}"
    echo -e "  ${BOLD}${DASHBOARD_URL}${NC}"
    echo ""
    echo -e "  ${CYAN}Steps:${NC}"
    echo -e "  1. Copy the token printed above"
    echo -e "  2. Open the URL in your browser"
    echo -e "  3. Select ${BOLD}\"Token\"${NC} and paste the token"
    echo -e "  4. Click ${BOLD}\"Sign In\"${NC}"
    echo ""
    echo -e "  ${YELLOW}Press Ctrl+C to stop the proxy${NC}"
    echo ""
    echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
    echo ""

    # Start kubectl proxy (foreground — blocks)
    kubectl proxy
}

# ── Print Usage ──────────────────────────────────────────────
usage() {
    echo ""
    echo -e "${BOLD}Usage:${NC}"
    echo -e "  ${GREEN}./setup-dashboard.sh${NC}          Full install (first time or after cluster recreate)"
    echo -e "  ${GREEN}./setup-dashboard.sh start${NC}    Daily quick-start: print token + start proxy"
    echo -e "  ${GREEN}./setup-dashboard.sh token${NC}    Just print the login token"
    echo ""
}

# ── Main ─────────────────────────────────────────────────────
main() {
    local cmd="${1:-install}"

    case "$cmd" in
        # ── Daily quick-start: just token + proxy ────────────
        start)
            echo ""
            echo -e "${CYAN}══════════════════════════════════════════════════════${NC}"
            echo -e "${CYAN}  MedTrust – Dashboard Quick Start${NC}"
            echo -e "${CYAN}══════════════════════════════════════════════════════${NC}"
            echo ""

            # Check dashboard is running
            if ! kubectl get ns kubernetes-dashboard &>/dev/null; then
                warn "Dashboard not installed. Running full install..."
                install_dashboard
                create_admin_user
                wait_for_dashboard
            fi

            get_token
            start_proxy
            ;;

        # ── Just print the token ─────────────────────────────
        token)
            get_token
            ;;

        # ── Full install (first time) ────────────────────────
        install|"")
            echo ""
            echo -e "${CYAN}══════════════════════════════════════════════════════${NC}"
            echo -e "${CYAN}  MedTrust – Kubernetes Dashboard Setup${NC}"
            echo -e "${CYAN}══════════════════════════════════════════════════════${NC}"
            echo ""

            install_dashboard
            create_admin_user
            wait_for_dashboard
            get_token
            start_proxy
            ;;

        # ── Help ─────────────────────────────────────────────
        -h|--help|help)
            usage
            ;;

        *)
            err "Unknown command: $cmd"
            usage
            ;;
    esac
}

main "$@"
