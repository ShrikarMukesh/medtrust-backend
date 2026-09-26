#!/bin/bash
set -euo pipefail

# ══════════════════════════════════════════════════════════════
#  MedTrust – Cluster & Services Lifecycle Manager
# ══════════════════════════════════════════════════════════════

CLUSTER_NAME="medtrust-cluster"
CONTAINERS=$(docker ps -a --filter "name=${CLUSTER_NAME}" --format '{{.Names}}')

CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'

log()  { echo -e "${CYAN}[INFO]${NC}  $1"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $1"; }

usage() {
    echo -e "${BOLD}MedTrust Cluster & Services Controller${NC}"
    echo ""
    echo -e "  ${GREEN}./cluster.sh stop${NC}                     Stop entire Kind cluster (0 RAM/CPU used)"
    echo -e "  ${GREEN}./cluster.sh start${NC}                    Start Kind cluster (resumes previous state)"
    echo -e "  ${GREEN}./cluster.sh status${NC}                   Show cluster and container status"
    echo -e "  ${GREEN}./cluster.sh disable-autostart${NC}        Ensure Docker never starts cluster on boot"
    echo ""
    echo -e "  ${GREEN}./cluster.sh stop-services [dev|qa]${NC}   Scale down microservices to 0 replicas (keeps DB/Kafka running)"
    echo -e "  ${GREEN}./cluster.sh start-services [dev|qa]${NC}  Scale up all microservices to 1 replica"
    echo -e "  ${GREEN}./cluster.sh start-svc <service> [dev]${NC} Start only a specific service (e.g. appointment-service)"
    echo -e "  ${GREEN}./cluster.sh stop-svc <service> [dev]${NC}  Stop a specific service"
    echo ""
}

case "${1:-}" in
    stop)
        log "Stopping MedTrust Kind cluster containers..."
        for c in $CONTAINERS; do
            docker stop "$c" >/dev/null && ok "Stopped $c"
        done
        ok "MedTrust cluster is stopped. No CPU or RAM is consumed."
        ;;

    start)
        log "Starting MedTrust Kind cluster containers..."
        for c in $CONTAINERS; do
            docker start "$c" >/dev/null && ok "Started $c"
        done
        # Ensure Docker never auto-starts them
        docker update --restart=no $CONTAINERS >/dev/null 2>&1 || true
        ok "MedTrust cluster started. (Docker auto-start is disabled)"
        ;;

    status)
        echo -e "${BOLD}Cluster Containers:${NC}"
        docker ps -a --filter "name=${CLUSTER_NAME}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        ;;

    disable-autostart)
        log "Disabling Docker auto-start on all MedTrust containers..."
        docker update --restart=no $CONTAINERS >/dev/null
        ok "Done! Cluster containers will never start automatically when Docker starts."
        ;;

    stop-services)
        ENV="${2:-dev}"
        NS="medtrust-${ENV}"
        log "Scaling down all microservices in ${NS} to 0 replicas (infrastructure stays intact)..."
        kubectl scale deployment -n "$NS" \
            clinical-service \
            appointment-service \
            patient-service \
            auth-service \
            consent-service \
            audit-service \
            notification-service \
            integration-service \
            --replicas=0 2>/dev/null || true
        ok "All microservices in ${NS} scaled to 0."
        ;;

    start-services)
        ENV="${2:-dev}"
        NS="medtrust-${ENV}"
        log "Scaling up all microservices in ${NS} to 1 replica..."
        kubectl scale deployment -n "$NS" \
            clinical-service \
            appointment-service \
            patient-service \
            auth-service \
            consent-service \
            audit-service \
            notification-service \
            integration-service \
            --replicas=1 2>/dev/null || true
        ok "All microservices in ${NS} scaled to 1."
        ;;

    start-svc)
        SVC="${2:-}"
        ENV="${3:-dev}"
        NS="medtrust-${ENV}"
        if [ -z "$SVC" ]; then
            warn "Please specify service name. Example: ./cluster.sh start-svc appointment-service"
            exit 1
        fi
        log "Starting $SVC in ${NS}..."
        kubectl scale deployment "$SVC" -n "$NS" --replicas=1
        ok "$SVC scaled to 1."
        ;;

    stop-svc)
        SVC="${2:-}"
        ENV="${3:-dev}"
        NS="medtrust-${ENV}"
        if [ -z "$SVC" ]; then
            warn "Please specify service name. Example: ./cluster.sh stop-svc appointment-service"
            exit 1
        fi
        log "Stopping $SVC in ${NS}..."
        kubectl scale deployment "$SVC" -n "$NS" --replicas=0
        ok "$SVC scaled to 0."
        ;;

    *)
        usage
        exit 0
        ;;
esac
