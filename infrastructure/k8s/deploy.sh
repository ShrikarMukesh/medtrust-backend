#!/bin/bash
set -euo pipefail

# ══════════════════════════════════════════════════════════════
#  MedTrust – Kind Cluster Deployment Script
# ══════════════════════════════════════════════════════════════

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
CLUSTER_NAME="medtrust-cluster"
NAMESPACE="medtrust"
KIND_IMAGE="kindest/node:v1.27.1"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log()  { echo -e "${CYAN}[INFO]${NC}  $1"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $1"; }
err()  { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ── Step 1: Create Kind Cluster ───────────────────────────────
create_cluster() {
    log "Checking if Kind cluster '$CLUSTER_NAME' exists..."
    if kind get clusters 2>/dev/null | grep -q "$CLUSTER_NAME"; then
        warn "Cluster '$CLUSTER_NAME' already exists. Skipping creation."
    else
        log "Creating Kind cluster '$CLUSTER_NAME'..."
        kind create cluster \
            --config "$SCRIPT_DIR/kind-config.yaml" \
            --image "$KIND_IMAGE"
        ok "Kind cluster '$CLUSTER_NAME' created!"
    fi

    kubectl cluster-info --context "kind-$CLUSTER_NAME"
}

# ── Step 2: Build Docker Images ───────────────────────────────
build_images() {
    log "Building clinical-service Docker image..."
    docker build -t medtrust/clinical-service:latest \
        "$ROOT_DIR/services/clinical-service"
    ok "clinical-service image built!"

    log "Building appointment-service Docker image..."
    docker build -t medtrust/appointment-service:latest \
        "$ROOT_DIR/services/appointment-service"
    ok "appointment-service image built!"
}

# ── Step 3: Load Images into Kind ─────────────────────────────
load_images() {
    log "Loading images into Kind cluster..."
    kind load docker-image medtrust/clinical-service:latest --name "$CLUSTER_NAME"
    kind load docker-image medtrust/appointment-service:latest --name "$CLUSTER_NAME"
    ok "Images loaded into Kind!"
}

# ── Step 4: Deploy Infrastructure ─────────────────────────────
deploy_infra() {
    log "Creating namespace '$NAMESPACE'..."
    kubectl apply -f "$SCRIPT_DIR/namespace.yaml"

    log "Deploying PostgreSQL..."
    kubectl apply -f "$SCRIPT_DIR/postgres.yaml"

    log "Deploying RabbitMQ..."
    kubectl apply -f "$SCRIPT_DIR/rabbitmq.yaml"

    log "Deploying Kafka (KRaft)..."
    kubectl apply -f "$SCRIPT_DIR/kafka.yaml"

    ok "Infrastructure deployed!"

    log "Waiting for Postgres to be ready..."
    kubectl wait --for=condition=ready pod -l app=medtrust-postgres \
        -n "$NAMESPACE" --timeout=120s

    log "Waiting for RabbitMQ to be ready..."
    kubectl wait --for=condition=ready pod -l app=medtrust-rabbitmq \
        -n "$NAMESPACE" --timeout=120s

    log "Waiting for Kafka to be ready..."
    kubectl wait --for=condition=ready pod -l app=medtrust-kafka \
        -n "$NAMESPACE" --timeout=120s

    ok "All infrastructure is ready!"
}

# ── Step 5: Deploy Services with Helm ─────────────────────────
deploy_services() {
    log "Deploying clinical-service via Helm..."
    helm upgrade --install clinical-service \
        "$ROOT_DIR/services/clinical-service/helm" \
        --namespace "$NAMESPACE" \
        --wait --timeout 180s

    log "Deploying appointment-service via Helm..."
    helm upgrade --install appointment-service \
        "$ROOT_DIR/services/appointment-service/helm" \
        --namespace "$NAMESPACE" \
        --wait --timeout 180s

    ok "All services deployed!"
}

# ── Step 6: Verify Deployment ─────────────────────────────────
verify() {
    echo ""
    log "═══════════════════════════════════════════════"
    log "  Deployment Summary"
    log "═══════════════════════════════════════════════"
    echo ""

    kubectl get pods -n "$NAMESPACE" -o wide
    echo ""
    kubectl get svc -n "$NAMESPACE"

    echo ""
    ok "MedTrust deployed successfully!"
    echo ""
    log "Access services:"
    log "  clinical-service:    http://localhost:30001/actuator/health"
    log "  appointment-service: http://localhost:30002/actuator/health"
    echo ""
}

# ── Main ──────────────────────────────────────────────────────
main() {
    echo ""
    echo -e "${CYAN}══════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  MedTrust Kubernetes Deployment${NC}"
    echo -e "${CYAN}══════════════════════════════════════════════════${NC}"
    echo ""

    create_cluster
    build_images
    load_images
    deploy_infra
    deploy_services
    verify
}

# ── Run ───────────────────────────────────────────────────────
main "$@"
