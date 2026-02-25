#!/bin/bash
set -euo pipefail

# ══════════════════════════════════════════════════════════════
#  MedTrust – Teardown Script
# ══════════════════════════════════════════════════════════════

CLUSTER_NAME="medtrust-cluster"
NAMESPACE="medtrust"

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

log()  { echo -e "${CYAN}[INFO]${NC}  $1"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $1"; }

echo ""
echo -e "${RED}══════════════════════════════════════════════════${NC}"
echo -e "${RED}  MedTrust – Teardown${NC}"
echo -e "${RED}══════════════════════════════════════════════════${NC}"
echo ""

read -p "This will DELETE the Kind cluster '$CLUSTER_NAME'. Continue? (y/N) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    log "Uninstalling Helm releases..."
    helm uninstall clinical-service --namespace "$NAMESPACE" 2>/dev/null || true
    helm uninstall appointment-service --namespace "$NAMESPACE" 2>/dev/null || true

    log "Deleting Kind cluster '$CLUSTER_NAME'..."
    kind delete cluster --name "$CLUSTER_NAME"

    ok "Cluster '$CLUSTER_NAME' deleted!"
else
    log "Teardown cancelled."
fi
