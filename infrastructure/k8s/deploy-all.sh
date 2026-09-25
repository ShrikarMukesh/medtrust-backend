#!/bin/bash
set -euo pipefail

# ══════════════════════════════════════════════════════════════
#  MedTrust – Enterprise Deployment Script
#
#  Usage:
#    ./deploy-all.sh dev          Deploy to DEV environment
#    ./deploy-all.sh qa           Deploy to QA environment
#    ./deploy-all.sh all          Deploy to both DEV and QA
#    ./deploy-all.sh dev --skip-build  Skip Docker builds (use existing images)
#    ./deploy-all.sh dev --infra-only  Deploy only infrastructure
#    ./deploy-all.sh dev --svc-only    Deploy only services (infra must exist)
# ══════════════════════════════════════════════════════════════

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
CLUSTER_NAME="medtrust-cluster"
KIND_IMAGE="kindest/node:v1.27.1"

# All MedTrust services
SERVICES=(
    clinical-service
    appointment-service
    patient-service
    auth-service
    consent-service
    audit-service
    notification-service
    integration-service
)

# ── Colors ────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

log()  { echo -e "${CYAN}[INFO]${NC}  $1"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $1"; }
err()  { echo -e "${RED}[ERR]${NC}   $1"; exit 1; }
step() { echo -e "\n${BOLD}${CYAN}── $1 ──${NC}\n"; }

# ── Parse Arguments ───────────────────────────────────────────
SKIP_BUILD=false
INFRA_ONLY=false
SVC_ONLY=false
ENVS=()

parse_args() {
    if [ $# -lt 1 ]; then
        usage
        exit 1
    fi

    case "$1" in
        dev)  ENVS=("dev") ;;
        qa)   ENVS=("qa") ;;
        all)  ENVS=("dev" "qa") ;;
        -h|--help|help) usage; exit 0 ;;
        *)    err "Unknown environment: $1. Use dev, qa, or all." ;;
    esac

    shift
    while [ $# -gt 0 ]; do
        case "$1" in
            --skip-build)   SKIP_BUILD=true ;;
            --infra-only)   INFRA_ONLY=true ;;
            --svc-only)     SVC_ONLY=true ;;
            *)              err "Unknown flag: $1" ;;
        esac
        shift
    done
}

usage() {
    echo ""
    echo -e "${BOLD}MedTrust Enterprise Deployment${NC}"
    echo ""
    echo -e "  ${GREEN}./deploy-all.sh dev${NC}                Deploy to DEV"
    echo -e "  ${GREEN}./deploy-all.sh qa${NC}                 Deploy to QA"
    echo -e "  ${GREEN}./deploy-all.sh all${NC}                Deploy to both"
    echo -e "  ${GREEN}./deploy-all.sh dev --skip-build${NC}   Skip Docker image builds"
    echo -e "  ${GREEN}./deploy-all.sh dev --infra-only${NC}   Deploy only Postgres/Kafka/RabbitMQ"
    echo -e "  ${GREEN}./deploy-all.sh dev --svc-only${NC}     Deploy only microservices"
    echo ""
}

# ── Step 1: Ensure Kind Cluster ──────────────────────────────
ensure_cluster() {
    step "Step 1: Kind Cluster"

    if kind get clusters 2>/dev/null | grep -q "$CLUSTER_NAME"; then
        ok "Cluster '$CLUSTER_NAME' already exists."
    else
        log "Creating Kind cluster '$CLUSTER_NAME'..."
        kind create cluster \
            --config "$SCRIPT_DIR/kind-config.yaml" \
            --image "$KIND_IMAGE"
        ok "Cluster created!"
    fi

    kubectl cluster-info --context "kind-$CLUSTER_NAME" 2>/dev/null || true
    kubectl config use-context "kind-$CLUSTER_NAME" >/dev/null 2>&1
}

# ── Step 2: Build Docker Images ──────────────────────────────
build_images() {
    if [ "$SKIP_BUILD" = true ]; then
        warn "Skipping Docker builds (--skip-build)"
        return
    fi

    step "Step 2: Building Docker Images"

    local built=0
    local failed=0

    for svc in "${SERVICES[@]}"; do
        local svc_dir="$ROOT_DIR/services/$svc"
        if [ -f "$svc_dir/Dockerfile" ]; then
            log "Building $svc..."
            # Compile JAR if not present
            if ! ls "$svc_dir"/target/*.jar >/dev/null 2>&1; then
                log "Packaging $svc with Maven..."
                (cd "$svc_dir" && ./mvnw clean package -DskipTests -B -q)
            fi
            if docker build -t "medtrust/$svc:latest" "$svc_dir" >/dev/null; then
                ok "$svc image built!"
                ((built++))
            else
                warn "Failed to build $svc (continuing...)"
                ((failed++))
            fi
        else
            warn "$svc has no Dockerfile, skipping"
        fi
    done

    echo ""
    ok "Built $built images ($failed failed)"
}

# ── Step 3: Load Images into Kind ────────────────────────────
load_images() {
    if [ "$SKIP_BUILD" = true ]; then
        return
    fi

    step "Step 3: Loading Images into Kind"

    for svc in "${SERVICES[@]}"; do
        if docker image inspect "medtrust/$svc:latest" >/dev/null 2>&1; then
            log "Loading $svc..."
            kind load docker-image "medtrust/$svc:latest" --name "$CLUSTER_NAME" 2>/dev/null || true
        fi
    done

    ok "Images loaded!"
}

# ── Step 4: Deploy Namespaces ────────────────────────────────
deploy_namespaces() {
    step "Step 4: Creating Namespaces"
    kubectl apply -f "$SCRIPT_DIR/environments/namespaces.yaml"
    ok "Namespaces created!"
}

# ── Step 5: Deploy Infrastructure ────────────────────────────
deploy_infra() {
    local env=$1
    local ns="medtrust-$env"

    step "Step 5: Deploying Infrastructure ($env)"

    log "Deploying PostgreSQL to $ns..."
    kubectl apply -f "$SCRIPT_DIR/environments/$env/postgres.yaml"

    log "Deploying Kafka to $ns..."
    kubectl apply -f "$SCRIPT_DIR/environments/$env/kafka.yaml"

    log "Deploying RabbitMQ to $ns..."
    kubectl apply -f "$SCRIPT_DIR/environments/$env/rabbitmq.yaml"

    ok "Infrastructure deployed to $ns!"

    log "Waiting for Postgres to be ready..."
    kubectl wait --for=condition=ready pod -l app=medtrust-postgres \
        -n "$ns" --timeout=120s 2>/dev/null || warn "Postgres timeout (may still be starting)"

    log "Waiting for Kafka to be ready..."
    kubectl wait --for=condition=ready pod -l app=medtrust-kafka \
        -n "$ns" --timeout=120s 2>/dev/null || warn "Kafka timeout (may still be starting)"

    log "Waiting for RabbitMQ to be ready..."
    kubectl wait --for=condition=ready pod -l app=medtrust-rabbitmq \
        -n "$ns" --timeout=120s 2>/dev/null || warn "RabbitMQ timeout (may still be starting)"

    ok "Infrastructure ready in $ns!"
}

# ── Step 6: Deploy Services via Helm ─────────────────────────
deploy_services() {
    local env=$1
    local ns="medtrust-$env"

    step "Step 6: Deploying Microservices ($env)"

    local deployed=0
    local failed=0

    for svc in "${SERVICES[@]}"; do
        local helm_dir="$ROOT_DIR/services/$svc/helm"
        local values_file="$helm_dir/values-${env}.yaml"

        if [ ! -f "$values_file" ]; then
            warn "$svc has no values-${env}.yaml, skipping"
            continue
        fi

        log "Deploying $svc to $ns..."
        if helm upgrade --install "$svc" "$helm_dir" \
            -f "$values_file" \
            --namespace "$ns" \
            --timeout 180s \
            --wait=false 2>/dev/null; then
            ok "$svc deployed!"
            ((deployed++))
        else
            warn "Failed to deploy $svc (continuing...)"
            ((failed++))
        fi
    done

    echo ""
    ok "Deployed $deployed services to $ns ($failed failed)"
}

# ── Step 7: Verify ───────────────────────────────────────────
verify() {
    local env=$1
    local ns="medtrust-$env"

    step "Deployment Summary ($env)"

    echo -e "${BOLD}Pods:${NC}"
    kubectl get pods -n "$ns" -o wide 2>/dev/null || true
    echo ""
    echo -e "${BOLD}Services:${NC}"
    kubectl get svc -n "$ns" 2>/dev/null || true
    echo ""
    echo -e "${BOLD}ConfigMaps:${NC}"
    kubectl get configmap -n "$ns" 2>/dev/null || true
    echo ""
    echo -e "${BOLD}Secrets:${NC}"
    kubectl get secrets -n "$ns" 2>/dev/null || true
}

# ── Print Access URLs ────────────────────────────────────────
print_urls() {
    local env=$1

    echo ""
    echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}  🚀 MedTrust ${env^^} Environment – Access URLs${NC}"
    echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
    echo ""

    local base_port
    if [ "$env" = "dev" ]; then
        base_port=30000
    else
        base_port=31000
    fi

    local ports=(
        "clinical-service:8080:$((base_port + 1))"
        "appointment-service:8082:$((base_port + 2))"
        "patient-service:8081:$((base_port + 3))"
        "auth-service:8083:$((base_port + 4))"
        "consent-service:8084:$((base_port + 5))"
        "audit-service:8085:$((base_port + 6))"
        "notification-service:8086:$((base_port + 7))"
        "integration-service:8087:$((base_port + 8))"
    )

    for item in "${ports[@]}"; do
        IFS=':' read -r name app_port node_port <<< "$item"
        echo -e "  ${CYAN}$name${NC}"
        echo -e "    Health: ${BOLD}http://localhost:${node_port}/actuator/health${NC}"
        echo -e "    Port-forward: ${YELLOW}kubectl port-forward svc/$name ${app_port}:${app_port} -n medtrust-${env}${NC}"
        echo ""
    done

    echo -e "  ${CYAN}Kubernetes Dashboard:${NC}"
    echo -e "    ${BOLD}http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/${NC}"
    echo ""
    echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
}

# ── Deploy Single Environment ────────────────────────────────
deploy_env() {
    local env=$1

    echo ""
    echo -e "${BOLD}${CYAN}══════════════════════════════════════════════════════${NC}"
    echo -e "${BOLD}${CYAN}  Deploying MedTrust – ${env^^} Environment${NC}"
    echo -e "${BOLD}${CYAN}══════════════════════════════════════════════════════${NC}"
    echo ""

    if [ "$SVC_ONLY" = false ]; then
        deploy_infra "$env"
    fi

    if [ "$INFRA_ONLY" = false ]; then
        deploy_services "$env"
    fi

    verify "$env"
    print_urls "$env"
}

# ── Main ─────────────────────────────────────────────────────
main() {
    parse_args "$@"

    echo ""
    echo -e "${BOLD}${CYAN}══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BOLD}${CYAN}  MedTrust Enterprise Kubernetes Deployment${NC}"
    echo -e "${BOLD}${CYAN}  Environments: ${ENVS[*]}${NC}"
    echo -e "${BOLD}${CYAN}══════════════════════════════════════════════════════════════${NC}"
    echo ""

    ensure_cluster
    deploy_namespaces

    if [ "$SVC_ONLY" = false ] && [ "$INFRA_ONLY" = false ]; then
        build_images
        load_images
    fi

    for env in "${ENVS[@]}"; do
        deploy_env "$env"
    done

    echo ""
    ok "🎉 Deployment complete!"
    echo ""
}

main "$@"
